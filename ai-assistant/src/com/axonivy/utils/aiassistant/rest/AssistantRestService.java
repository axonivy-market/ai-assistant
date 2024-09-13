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
import com.axonivy.utils.aiassistant.history.ChatMessageManager;
import com.axonivy.utils.aiassistant.prompts.BasicPromptTemplates;
import com.axonivy.utils.aiassistant.service.AiFunctionService;
import com.axonivy.utils.aiassistant.service.AssistantService;
import com.axonivy.utils.aiassistant.utils.AssistantUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import ch.ivyteam.ivy.environment.Ivy;
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
  public Response initConversation(@PathParam("assistantId") String assistantId,
      @PathParam("conversationId") String conversationId) {
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation = ChatMessageManager.loadConversation(assistantId,
        conversationId);
    return Response.ok(BusinessEntityConverter.entityToJsonValue(conversation))
        .build();
  }

  @POST
  @Path(value = "{conversationId}/request")
  @Produces(MediaType.APPLICATION_JSON)
  public void handleUserRequest(@Suspended AsyncResponse response,
      @PathParam("conversationId") String conversationId,
      AssistantChatPayload payload)
      throws WebApplicationException, IOException, InterruptedException {

    Conversation conversation = ChatMessageManager
        .loadConversation(payload.getAssistantId(), conversationId);

    if (conversation == null) {
      conversation = new Conversation();
      conversation.setId(conversationId);
    }

    String message = payload.getMessage();

    ChatMessage userMessage = ChatMessage.newUserMessage(message);
    conversation.getHistory().add(userMessage);
    conversation.getMemory().add(userMessage);

    ChatMessageManager.saveConversation(payload.getAssistantId(), conversation);

    Assistant assistant = AssistantService.getInstance()
        .findById(payload.getAssistantId());
    AiFunction selectedFunction = AiFunctionService.getInstance()
        .findById(chooseFunction(assistant, conversation));

    if (selectedFunction == null) {
      handleDefaultTool(response, conversation, assistant, message);
      return;
    }

    String selectedFunctionMessage = selectedFunction
        .generateSelectedFunctionMessage();

    payload.setSelectedFunctionId(selectedFunction.getId());
    payload.setSelectedFunctionMessage(selectedFunctionMessage);

    ChatMessage systemMessage = ChatMessage
        .newNotificationMessage(selectedFunctionMessage);
    conversation.getHistory().add(systemMessage);
    conversation.getMemory().add(systemMessage);
    ChatMessageManager.saveConversation(assistant.getId(), conversation);

    response.resume(BusinessEntityConverter.entityToJsonValue(payload));

  }

  @POST
  @Path(value = "{conversationId}/continueRequest")
  @Produces(MediaType.APPLICATION_JSON)
  public void continueHandleUserRequest(@Suspended AsyncResponse response,
      @PathParam("conversationId") String conversationId,
      AssistantChatPayload payload)
      throws WebApplicationException, IOException, InterruptedException {

    Conversation conversation = ChatMessageManager
        .loadConversation(payload.getAssistantId(), conversationId);

    if (conversation == null) {
      conversation = new Conversation();
      conversation.setId(conversationId);
    }

    String message = payload.getMessage();
    Assistant assistant = AssistantService.getInstance()
        .findById(payload.getAssistantId());
    String selectedFunctionId = payload.getSelectedFunctionId();

    if (StringUtils.isBlank(selectedFunctionId)) {
      selectedFunctionId = chooseFunction(assistant, conversation);
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

  private void handleDefaultTool(AsyncResponse response,
      Conversation conversation, Assistant assistant, String request)
      throws JsonProcessingException {

    AiStreamingMessageHandler messageHandler = initStreamingMessageHandler(
        conversation);

    Map<String, Object> languageParam = new HashMap<>();
    languageParam.put("input", request);

    Map<String, Object> params = new HashMap<>();
    params.put("language",
        Ivy.session().getContentLocale().getDisplayCountry());
    params.put("tools", BusinessEntityConverter.getObjectMapper()
        .writeValueAsString(assistant.getToolkit()));
    params.put("ethicalRules", assistant.formatEthicalRules());
    params.put("request", request);
    params.put("info", assistant.getInfo());

    response.resume(BusinessEntityConverter.entityToJsonValue(
        new StreamingMessage(conversation.getId(), AIState.IN_PROGRESS, "")));

    String error = assistant.getAiModel().getAiBot().streamChat(params,
        BasicPromptTemplates.DEFAULT_ANSWER, messageHandler);
    if (StringUtils.isNotBlank(error)) {
      conversation.getHistory().add(ChatMessage.newErrorMessage(error));
      ChatMessageManager.saveConversation(assistant.getId(), conversation);
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
      payload.setSelectedFunctionMessage(
          flow.getFunctionToTrigger().generateSelectedFunctionMessage());

      response.resume(payload);
    } else {
      response.resume(BusinessEntityConverter.entityToJsonValue(flow));
    }
  }

  @POST
  @Path(value = "{conversationId}/resume")
  @Produces(MediaType.APPLICATION_JSON)
  public void resumeAiFlow(@Suspended AsyncResponse response,
      @PathParam("conversationId") String conversationId, AiFlowPayload payload)
      throws WebApplicationException, IOException, InterruptedException {

    AiFlow flow = BusinessEntityConverter.jsonValueToEntity(payload.getAiFlow(),
        AiFlow.class);
    Assistant assistant = AssistantService.getInstance()
        .findById(payload.getAssistantId());
    Conversation conversation = ChatMessageManager
        .loadConversation(payload.getAssistantId(), conversationId);

    if (payload.getIsSkipMessage()) {
      flow.proceed(null, conversation, assistant);
    } else {
      conversation.getMemory()
          .add(ChatMessage.newUserMessage(payload.getMessage()));
      conversation.getHistory()
          .add(ChatMessage.newUserMessage(payload.getMessage()));
      ChatMessageManager.saveConversation(payload.getAssistantId(),
          conversation);

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
          flow.getFunctionToTrigger().generateSelectedFunctionMessage());

      response.resume(chatPayload);
    } else {
      response.resume(BusinessEntityConverter.entityToJsonValue(flow));
    }
  }

  private void initTriggerToolMessage(Conversation conversation, AiFlow flow,
      Assistant assistant) {
    ChatMessage systemMessage = ChatMessage.newNotificationMessage(
        flow.getFunctionToTrigger().generateSelectedFunctionMessage());
    conversation.getHistory().add(systemMessage);
    conversation.getMemory().add(systemMessage);
    ChatMessageManager.saveConversation(assistant.getId(), conversation);
  }

  private void handleRetrievalQATool(AsyncResponse response, String message,
      Conversation conversation, Assistant assistant, AiFunction selectedTool) {

    RetrievalQATool qaTool = (RetrievalQATool) selectedTool;
    AiStreamingMessageHandler messageHandler = initStreamingMessageHandler(
        conversation);

    String testEmbeddingConnectionResult = assistant.getAiModel().getAiBot()
        .testEmbeddingStoreConnection(qaTool.getCollection());

    if (StringUtils.isNotBlank(testEmbeddingConnectionResult)) {
      conversation.getHistory()
          .add(ChatMessage.newErrorMessage(testEmbeddingConnectionResult));
      ChatMessageManager.saveConversation(assistant.getId(), conversation);
      response.resume(BusinessEntityConverter
          .entityToJsonValue(new ErrorPayload(testEmbeddingConnectionResult)));
      return;
    }

    Map<String, Object> params = new HashMap<>();
    params.put("input", message);

    params.put("language",
        Ivy.session().getContentLocale().getDisplayCountry());
    params.put("info", assistant.getInfo());
    params.put("ethicalRules",
        Optional.ofNullable(assistant.formatEthicalRules()).orElse("<None>"));
    params.put("context", assistant.getAiModel().getAiBot()
        .retrieveDocumentsAsString(qaTool.getCollection(), message));
    params.put("contactPart", BasicPromptTemplates.generateContactPrompt(
        assistant.getContactEmail(), assistant.getContactWebsite()));

    response.resume(BusinessEntityConverter.entityToJsonValue(
        new StreamingMessage(conversation.getId(), AIState.IN_PROGRESS, "")));

    assistant.getAiModel().getAiBot().streamChat(params,
        BasicPromptTemplates.RAG_PROMPT_TEMPLATE, messageHandler);
  }

  @GET
  @Path(value = "{assistantId}/{conversationId}/stream")
  @Produces(MediaType.APPLICATION_JSON)
  public void streamReply(@Suspended AsyncResponse response,
      @PathParam("assistantId") String assistantId,
      @PathParam("conversationId") String conversationId) {

    if (StringUtils.isBlank(conversationId)) {
      response.cancel();
    }

    synchronized (messageMap) {
      Queue<String> queue = messageMap.get(conversationId);
      if (queue == null || queue.isEmpty()) {
        response.resume(BusinessEntityConverter.entityToJsonValue(
            new StreamingMessage(conversationId, AIState.IN_PROGRESS, null)));
      }

      String result = Optional.ofNullable(queue.poll()).orElse("");

      Pattern pattern = Pattern.compile(ERROR_REGEX);
      Matcher matcher = pattern.matcher(result);
      if (matcher.find()) {

        Conversation conversation = ChatMessageManager
            .loadConversation(assistantId, conversationId);
        conversation.getHistory()
            .add(ChatMessage.newErrorMessage(matcher.group(1)));
        ChatMessageManager.saveConversation(assistantId, conversation);

        response.resume(BusinessEntityConverter
            .entityToJsonValue(new ErrorPayload(matcher.group(1))));
        return;
      }

      if (result.startsWith(conversationId)) {
        Conversation conversation = ChatMessageManager
            .loadConversation(assistantId, conversationId);
        conversation.getHistory()
            .add(ChatMessage.newAIMessage(result.replace(conversationId, "")));
        ChatMessageManager.saveConversation(assistantId, conversation);
      }

      response.resume(BusinessEntityConverter.entityToJsonValue(
          new StreamingMessage(conversationId, AIState.IN_PROGRESS, result)));
    }
  }

  private String chooseFunction(Assistant assistant, Conversation conversation)
      throws JsonProcessingException {
    Map<String, Object> params = new HashMap<>();
    params.put("functions", assistant.formatFunctionsForChoose());
    params.put("memory",
        AiFunction.getFormattedMemory(conversation.getMemory()));

    return getFunctionIdFromBotAnswer(assistant.getAiModel().getAiBot().chat(
        params, BasicPromptTemplates.CHOOSE_FUNCTION), assistant.getTools());
  }

  private String getFunctionIdFromBotAnswer(String answer,
      List<String> functionsIds) {
    if (StringUtils.isBlank(answer)) {
      return answer;
    }

    List<String> sortedFunctionIds = Optional.ofNullable(functionsIds)
        .orElseGet(() -> new ArrayList<>()).stream()
        .sorted((s1, s2) -> Integer.compare(s2.length(), s1.length()))
        .collect(Collectors.toList());

    for (String functionId : sortedFunctionIds) {
      if (answer.contains(functionId)) {
        return functionId;
      }
    }

    return "";
  }
}