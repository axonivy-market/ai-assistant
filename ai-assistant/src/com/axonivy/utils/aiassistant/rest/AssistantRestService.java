package com.axonivy.utils.aiassistant.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.enums.AIState;
import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.constant.AiConstants;
import com.axonivy.utils.aiassistant.core.AiStreamingMessageHandler;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.flow.AiFlow;
import com.axonivy.utils.aiassistant.dto.flow.AiStep;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.history.Conversation;
import com.axonivy.utils.aiassistant.dto.history.StreamingMessage;
import com.axonivy.utils.aiassistant.dto.payload.AiFlowPayload;
import com.axonivy.utils.aiassistant.dto.payload.AssistantChatPayload;
import com.axonivy.utils.aiassistant.dto.payload.ErrorPayload;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.dto.tool.IvyTool;
import com.axonivy.utils.aiassistant.dto.tool.RetrievalQATool;
import com.axonivy.utils.aiassistant.enums.StepType;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.history.ChatMessageManager;
import com.axonivy.utils.aiassistant.prompts.BasicPromptTemplates;
import com.axonivy.utils.aiassistant.prompts.RagPromptTemplates;
import com.axonivy.utils.aiassistant.service.AiFunctionService;
import com.axonivy.utils.aiassistant.service.AssistantService;
import com.axonivy.utils.aiassistant.utils.AiFunctionUtils;
import com.axonivy.utils.aiassistant.utils.AssistantUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import ch.ivyteam.ivy.security.ISecurityConstants;

@Singleton
@Path(value = "assistant")
@RolesAllowed(value = { ISecurityConstants.TOP_LEVEL_ROLE_NAME })
public class AssistantRestService {

  private static final String ERROR_REGEX = "<error>(.*?)</error>";
  private Map<String, Queue<String>> messageMap = new HashMap<>();

  @GET
  @Path(value = "{assistantId}/{conversationId}/initialize")
  @Produces(MediaType.APPLICATION_JSON)
  public Response initConversation(
      @PathParam(AiConstants.ASSISTANT_ID) String assistantId,
      @PathParam(AiConstants.CONVERSATION_ID) String conversationId) {
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation = ChatMessageManager.loadConversation(conversationId);
    return Response.ok(BusinessEntityConverter.entityToJsonValue(conversation))
        .build();
  }

  @POST
  @Path(value = "{conversationId}/request")
  @Produces(MediaType.APPLICATION_JSON)
  public void handleUserRequest(@Suspended AsyncResponse response,
      @PathParam(AiConstants.CONVERSATION_ID) String conversationId,
      AssistantChatPayload payload)
      throws WebApplicationException, IOException, InterruptedException {

    Conversation conversation = ChatMessageManager
        .loadConversation(conversationId);

    if (conversation == null) {
      conversation = new Conversation();
      conversation.setId(conversationId);
    }

    String message = payload.getMessage();

    ChatMessage userMessage = ChatMessage.newUserMessage(message);
    conversation.getHistory().add(userMessage);
    conversation.getMemory().add(userMessage);

    ChatMessageManager.saveConversation(conversation);

    Assistant assistant = AssistantService.getInstance()
        .findById(payload.getAssistantId());
    AiFunction selectedFunction = AiFunctionService.getInstance()
        .findById(chooseFunction(assistant, conversation, message));

    if (selectedFunction == null) {
      handleDefaultTool(response, conversation, assistant, message);
      return;
    }

    String selectedFunctionMessage = selectedFunction
        .generateSelectedFunctionMessage();

    payload.setSelectedFunctionId(selectedFunction.getId());
    payload.setSelectedFunctionMessage(selectedFunction.getName());
    payload.setType(selectedFunction.getType().name());

    ChatMessage systemMessage = ChatMessage
        .newSystemMessage(selectedFunctionMessage,
            selectedFunction.getType().name());

    ChatMessage systemMessageHistory = ChatMessage.newSystemMessage(
        selectedFunction.getName(), selectedFunction.getType().name());

    conversation.getHistory().add(systemMessageHistory);
    conversation.getMemory().add(systemMessage);
    ChatMessageManager.saveConversation(conversation);

    response.resume(BusinessEntityConverter.entityToJsonValue(payload));

  }

  @POST
  @Path(value = "{conversationId}/continueRequest")
  @Produces(MediaType.APPLICATION_JSON)
  public void continueHandleUserRequest(@Suspended AsyncResponse response,
      @PathParam(AiConstants.CONVERSATION_ID) String conversationId,
      AssistantChatPayload payload)
      throws WebApplicationException, IOException, InterruptedException {

    Conversation conversation = ChatMessageManager
        .loadConversation(conversationId);

    if (conversation == null) {
      conversation = new Conversation();
      conversation.setId(conversationId);
    }

    String message = payload.getMessage();
    Assistant assistant = AssistantService.getInstance()
        .findById(payload.getAssistantId());
    String selectedFunctionId = payload.getSelectedFunctionId();

    if (StringUtils.isBlank(selectedFunctionId)) {
      selectedFunctionId = chooseFunction(assistant, conversation, message);
    }

    if (StringUtils.isBlank(selectedFunctionId)) {
      handleDefaultTool(response, conversation, assistant, selectedFunctionId);
      return;
    }

    AiFunction selectedFunction = AiFunctionService.getInstance()
        .findById(selectedFunctionId);

    switch (selectedFunction.getType()) {
    case IVY -> handleIvyTool(response, message, conversation, assistant,
        selectedFunction);
    case FLOW -> handleAiFlow(response, message, conversation, assistant,
        selectedFunction);
    case RETRIEVAL_QA -> handleRetrievalQATool(response, message, conversation,
        assistant, selectedFunction);
    default -> handleDefaultTool(response, conversation, assistant, message);
    }

  }

  private void handleDefaultRetrievalQAAnswer(AsyncResponse response,
      Conversation conversation, Assistant assistant, String request)
      throws JsonProcessingException {

    AiStreamingMessageHandler messageHandler = initStreamingMessageHandler(
        conversation);

    Map<String, Object> params = initParamsForDefaultAnswer(assistant, request);
    params.put(AiConstants.CONTACT_PART,
        BasicPromptTemplates.generateContactPrompt(
        assistant.getContactEmail(), assistant.getContactWebsite()));

    response.resume(BusinessEntityConverter.entityToJsonValue(
        new StreamingMessage(conversation.getId(), AIState.IN_PROGRESS,
            StringUtils.EMPTY)));

    String error = assistant.getAiModel().getAiBot().streamChat(params,
        RagPromptTemplates.DEFAULT_RAG_ANSWER, messageHandler);
    if (StringUtils.isNotBlank(error)) {
      conversation.getHistory().add(ChatMessage.newErrorMessage(error));
      ChatMessageManager.saveConversation(conversation);
      response.resume(
          BusinessEntityConverter.entityToJsonValue(new ErrorPayload(error)));
      messageHandler = null;
    }
  }

  private void handleDefaultTool(AsyncResponse response,
      Conversation conversation, Assistant assistant, String request)
      throws JsonProcessingException {

    AiStreamingMessageHandler messageHandler = initStreamingMessageHandler(
        conversation);

    Map<String, Object> params = initParamsForDefaultAnswer(assistant, request);
    params.put(AiConstants.INFO, assistant.getInfo());

    response.resume(BusinessEntityConverter.entityToJsonValue(
        new StreamingMessage(conversation.getId(), AIState.IN_PROGRESS,
            StringUtils.EMPTY)));

    String error = assistant.getAiModel().getAiBot().streamChat(params,
        BasicPromptTemplates.DEFAULT_ANSWER, messageHandler);
    if (StringUtils.isNotBlank(error)) {
      conversation.getHistory().add(ChatMessage.newErrorMessage(error));
      ChatMessageManager.saveConversation(conversation);
      response.resume(
          BusinessEntityConverter.entityToJsonValue(new ErrorPayload(error)));
      messageHandler = null;
    }
  }

  private AiStreamingMessageHandler initStreamingMessageHandler(
      Conversation conversation) {
    Queue<String> conversationContent = new LinkedList<>();
    messageMap.put(conversation.getId(), conversationContent);
    AiStreamingMessageHandler messageHandler = new AiStreamingMessageHandler(
        conversationContent, conversation.getId());
    return messageHandler;
  }

  private void handleIvyTool(AsyncResponse response, String userMessage,
      Conversation conversation, Assistant assistant, AiFunction selectedTool)
      throws JsonProcessingException {

    // Run the ivy tool
    IvyTool selectedIvyTool = (IvyTool) selectedTool;
    response.resume(selectedIvyTool.run(userMessage, conversation, assistant,
        selectedIvyTool, assistant.getAiModel().getAiBot(),
        AiStep.getFormattedMetadatas(AssistantUtils.getMetadatas())));
  }

  private void handleAiFlow(AsyncResponse response, String message,
      Conversation conversation, Assistant assistant, AiFunction selectedTool)
      throws JsonProcessingException {

    // Fill the AI flow
    AiFlow flow = (AiFlow) selectedTool;
    flow.proceed(message, conversation, assistant);
    if (flow.getState() == AIState.TRIGGER) {

      initTriggerToolMessage(conversation, flow, assistant);
      AssistantChatPayload payload = new AssistantChatPayload();
      payload.setAssistantId(assistant.getId());
      payload.setConversationId(conversation.getId());
      payload.setMessage(flow.getFinalResult().getResult());
      payload.setSelectedFunctionId(flow.getFunctionToTrigger().getId());
      payload.setType(StepType.TRIGGER_FLOW.name());
      payload.setSelectedFunctionMessage(
          flow.getFunctionToTrigger().getId());

      response.resume(payload);
    } else {
      response.resume(BusinessEntityConverter.entityToJsonValue(flow));
    }
  }

  @POST
  @Path(value = "{conversationId}/resume")
  @Produces(MediaType.APPLICATION_JSON)
  public void resumeAiFlow(@Suspended AsyncResponse response,
      @PathParam(AiConstants.CONVERSATION_ID) String conversationId,
      AiFlowPayload payload)
      throws WebApplicationException, IOException, InterruptedException {

    AiFlow flow = BusinessEntityConverter.jsonValueToEntity(payload.getAiFlow(),
        AiFlow.class);
    Assistant assistant = AssistantService.getInstance()
        .findById(payload.getAssistantId());
    Conversation conversation = ChatMessageManager
        .loadConversation(conversationId);

    if (payload.getIsSkipMessage()) {
      flow.proceed(null, conversation, assistant);
    } else {
      conversation.getMemory()
          .add(ChatMessage.newUserMessage(payload.getMessage()));
      conversation.getHistory()
          .add(ChatMessage.newUserMessage(payload.getMessage()));
      ChatMessageManager.saveConversation(conversation);

      flow.proceed(payload.getMessage(), conversation, assistant);
    }


    if (flow.getState() == AIState.TRIGGER) {
      initTriggerToolMessage(conversation, flow, assistant);
      AssistantChatPayload chatPayload = new AssistantChatPayload();
      chatPayload.setAssistantId(assistant.getId());
      chatPayload.setConversationId(conversation.getId());
      chatPayload.setMessage(flow.getFinalResult().getResult());
      chatPayload.setSelectedFunctionId(flow.getFunctionToTrigger().getId());
      chatPayload.setSelectedFunctionMessage(
          flow.getFunctionToTrigger().getName());
      chatPayload.setType(StepType.TRIGGER_FLOW.name());

      response.resume(chatPayload);
    } else {
      response.resume(BusinessEntityConverter.entityToJsonValue(flow));
    }
  }

  private void initTriggerToolMessage(Conversation conversation, AiFlow flow,
      Assistant assistant) {
    ChatMessage systemMessage = ChatMessage.newUseAIFlowSystemMessage(
        flow.getFunctionToTrigger().generateSelectedFunctionMessage());
    conversation.getHistory().add(systemMessage);
    conversation.getMemory().add(systemMessage);
    ChatMessageManager.saveConversation(conversation);
  }

  private void handleRetrievalQATool(AsyncResponse response, String message,
      Conversation conversation, Assistant assistant, AiFunction selectedTool)
      throws JsonProcessingException {

    RetrievalQATool qaTool = (RetrievalQATool) selectedTool;
    AiStreamingMessageHandler messageHandler = initStreamingMessageHandler(
        conversation);

    String testEmbeddingConnectionResult = qaTool.testConnection(assistant);

    if (StringUtils.isNotBlank(testEmbeddingConnectionResult)) {
      conversation.getHistory()
          .add(ChatMessage.newErrorMessage(testEmbeddingConnectionResult));
      ChatMessageManager.saveConversation(conversation);
      response.resume(BusinessEntityConverter
          .entityToJsonValue(new ErrorPayload(testEmbeddingConnectionResult)));
      return;
    }

    String context = assistant.getAiModel().getAiBot()
        .retrieveDocumentsAsString(qaTool.getCollection(), message).strip();

    if (StringUtils.isBlank(context)) {
      handleDefaultRetrievalQAAnswer(response, conversation, assistant,
          message);
      return;
    }

    Map<String, Object> params = new HashMap<>();
    params.put(AiConstants.REQUEST, message);
    params.put(AiConstants.LANGUAGE, AssistantUtils.getDefaultLanguage());
    params.put(AiConstants.INFO, assistant.getInfo());
    params.put(AiConstants.ETHICAL_RULES,
        Optional.ofNullable(assistant.formatEthicalRules())
            .orElse(AiConstants.NONE_RESULT));
    params.put(AiConstants.CONTEXT, context);
    params.put(AiConstants.CONTACT_PART,
        BasicPromptTemplates.generateContactPrompt(
        assistant.getContactEmail(), assistant.getContactWebsite()));
    
    int requestType = RetrievalQATool.analyzeRequestType(assistant, message);
    params.put("structureGuidelines",
        RagPromptTemplates.getStructuredOutputInstruction(requestType));

    response.resume(BusinessEntityConverter.entityToJsonValue(
        new StreamingMessage(conversation.getId(), AIState.IN_PROGRESS,
            StringUtils.EMPTY)));

    assistant.getAiModel().getAiBot().streamChat(params,
        RagPromptTemplates.RAG_PROMPT_TEMPLATE, messageHandler);
  }

  @GET
  @Path(value = "{assistantId}/{conversationId}/stream")
  @Produces(MediaType.APPLICATION_JSON)
  public void streamReply(@Suspended AsyncResponse response,
      @PathParam(AiConstants.ASSISTANT_ID) String assistantId,
      @PathParam(AiConstants.CONVERSATION_ID) String conversationId) {

    if (StringUtils.isBlank(conversationId)) {
      response.cancel();
    }

    synchronized (messageMap) {
      Queue<String> queue = messageMap.get(conversationId);
      if (queue == null || queue.isEmpty()) {
        response.resume(BusinessEntityConverter.entityToJsonValue(
            new StreamingMessage(conversationId, AIState.IN_PROGRESS, null)));
      }

      String result = Optional.ofNullable(queue.poll())
          .orElse(StringUtils.EMPTY);

      Pattern pattern = Pattern.compile(ERROR_REGEX);
      Matcher matcher = pattern.matcher(result);
      if (matcher.find()) {

        Conversation conversation = ChatMessageManager
            .loadConversation(conversationId);
        conversation.getHistory()
            .add(ChatMessage.newErrorMessage(matcher.group(1)));
        ChatMessageManager.saveConversation(conversation);

        response.resume(BusinessEntityConverter
            .entityToJsonValue(new ErrorPayload(matcher.group(1))));
        return;
      }

      if (result.startsWith(conversationId)) {
        Conversation conversation = ChatMessageManager
            .loadConversation(conversationId);
        conversation.getHistory()
            .add(ChatMessage.newAIMessage(
                result.replace(conversationId, StringUtils.EMPTY)));
        ChatMessageManager.saveConversation(conversation);
      }

      response.resume(BusinessEntityConverter.entityToJsonValue(
          new StreamingMessage(conversationId, AIState.IN_PROGRESS, result)));
    }
  }

  @POST
  @Path(value = "{conversationId}/useKnowledgeBase")
  @Produces(MediaType.APPLICATION_JSON)
  public void useKnowledgeBase(@Suspended AsyncResponse response,
      AssistantChatPayload payload) throws JsonProcessingException {

    Conversation conversation = ChatMessageManager
        .loadConversation(payload.getConversationId());
    if (conversation == null) {
      return;
    }

    Assistant assistant = AssistantService.getInstance()
        .findById(payload.getAssistantId());
    String selectedFunctionId = payload.getSelectedFunctionId();

    if (StringUtils.isBlank(selectedFunctionId) || assistant == null) {
      return;
    }

    AiFunction selectedFunction = AiFunctionService
        .getInstance().findAll()
        .stream()
        .filter(func -> func.getType() == ToolType.RETRIEVAL_QA
            && func.getId().contentEquals(selectedFunctionId))
        .findFirst().get();

    if (selectedFunction != null) {
      handleRetrievalQATool(response, payload.getMessage(), conversation,
          assistant, selectedFunction);
    }


  }

  private String chooseFunction(Assistant assistant, Conversation conversation,
      String request)
      throws JsonProcessingException {
    Map<String, Object> params = new HashMap<>();
    params.put(AiConstants.FUNCTIONS, assistant.formatFunctionsForChoose());
    params.put(AiConstants.REQUEST, request);

    String selectedFunction = getFunctionIdFromBotAnswer(
        assistant.getAiModel().getAiBot().chat(
            params, BasicPromptTemplates.CHOOSE_FUNCTION),
        assistant.getTools());

    if (StringUtils.isBlank(selectedFunction)) {
      params.put(AiConstants.FUNCTIONS, assistant.formatFunctionsForChoose());
      params.put(AiConstants.MEMORY,
          AiFunction.getFormattedMemory(conversation.getMemory()));
      selectedFunction = getFunctionIdFromBotAnswer(
          assistant.getAiModel().getAiBot().chat(params,
              BasicPromptTemplates.CHOOSE_FUNCTION_WITH_HISTORY),
          assistant.getTools());
    }

    return selectedFunction;
  }

  private String getFunctionIdFromBotAnswer(String answer,
      List<String> functionsIds) {
    if (StringUtils.isBlank(answer)) {
      return answer;
    }

    String selectedFunction = AiFunctionUtils
        .extractTextInsideDoubleTag(answer);

    List<String> sortedFunctionIds = Optional.ofNullable(functionsIds)
        .orElseGet(() -> new ArrayList<>()).stream()
        .sorted((s1, s2) -> Integer.compare(s2.length(), s1.length()))
        .collect(Collectors.toList());

    for (String functionId : sortedFunctionIds) {
      if (selectedFunction.contains(functionId)) {
        return functionId;
      }
    }

    return StringUtils.EMPTY;
  }

  private Map<String, Object> initParamsForDefaultAnswer(Assistant assistant,
      String request) throws JsonProcessingException {
    Map<String, Object> params = new HashMap<>();
    params.put(AiConstants.LANGUAGE, AssistantUtils.getDefaultLanguage());
    params.put(AiConstants.FUNCTIONS, BusinessEntityConverter.getObjectMapper()
        .writeValueAsString(assistant.getToolkit()));
    params.put(AiConstants.ETHICAL_RULES, assistant.formatEthicalRules());
    params.put(AiConstants.REQUEST, request);
    return params;
  }
}