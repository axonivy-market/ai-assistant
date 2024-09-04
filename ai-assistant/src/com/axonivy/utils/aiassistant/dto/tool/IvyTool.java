package com.axonivy.utils.aiassistant.dto.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.portal.components.service.IvyAdapterService;
import com.axonivy.utils.aiassistant.core.AbstractAIBot;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.flow.AiStep;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.history.Conversation;
import com.axonivy.utils.aiassistant.dto.history.StreamingMessage;
import com.axonivy.utils.aiassistant.dto.payload.ErrorPayload;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.history.ChatMessageManager;
import com.axonivy.utils.aiassistant.prompts.BasicPromptTemplates;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import ch.ivyteam.ivy.environment.Ivy;

public class IvyTool extends AiFunction {

  private static final long serialVersionUID = -5362479525475837795L;

  private List<IvyToolAttribute> attributes;
  private String signature;
  private String useTool;

  public List<IvyToolAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<IvyToolAttribute> attributes) {
    this.attributes = attributes;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  @Override
  public void init() {
  }

  @Override
  public ToolType getType() {
    return ToolType.IVY;
  }

  @JsonIgnore
  public AiResultDTO getResult() {
    if (!hasPermision()) {
      return createNoPermisisonError();
    }

    Map<String, Object> params = new HashMap<>();
    getAttributes().forEach(attr -> {
      params.put(attr.getName(), attr.getValue());
    });
    Map<String, Object> result = IvyAdapterService
        .startSubProcessInApplication(getSignature(), params);

    if (result != null && !result.isEmpty()) {
      return (AiResultDTO) result.get("result");
    }

    return createSomethingWentWrongError();
  }

  @JsonIgnore
  public String run(String message, Conversation conversation,
      Assistant assistant, IvyTool selectedIvyTool, AbstractAIBot bot,
      String metadata) {
    try {
      selectedIvyTool = selectedIvyTool.fullfilIvyTool(conversation.getMemory(),
          bot, metadata);

      String result = generateFinalResultFromIvyTool(selectedIvyTool,
          assistant);
      String resultForMemory = selectedIvyTool.getResult().getResultForAI();

      String aiConversation = BusinessEntityConverter.entityToJsonValue(
          new StreamingMessage(conversation.getId(), AIState.DONE, result));

      conversation.getHistory().add(ChatMessage.newAIMessage(result));
      conversation.getMemory().add(ChatMessage.newAIMessage(resultForMemory));
      ChatMessageManager.saveConversation(assistant.getId(), conversation);
      return aiConversation;
    } catch (Exception e) {
      return BusinessEntityConverter.entityToJsonValue(new ErrorPayload(
          Ivy.cms().co("/Labels/AI/Error/SomethingWentWrong")));
    }
  }

  @JsonIgnore
  private String generateFinalResultFromIvyTool(IvyTool tool,
      Assistant assistant) {
    AiResultDTO result = tool.getResult();
    if (result == null || result.getState() == AIState.ERROR) {
      return "Error when processing your request";
    }

    return result.getResult();
  }

  @JsonIgnore
  public IvyTool fullfilIvyTool(List<ChatMessage> memory,
      AbstractAIBot bot, String metadata) throws JsonProcessingException {
    Map<String, Object> params = new HashMap<>();
    params.put("toolJson", BusinessEntityConverter.getObjectMapper()
        .writeValueAsString(BusinessEntityConverter.entityToJsonNode(this)));

    params.put("memory", getFormattedMemory(memory));
    params.put("metadata", metadata);

    return BusinessEntityConverter.jsonValueToEntity(
        AiStep.extractTextInsideTag(
            bot.chat(params, BasicPromptTemplates.FULFILL_IVY_TOOL)),
        IvyTool.class);
  }

  @Override
  public JsonNode buildJsonNode() {
    return BusinessEntityConverter.entityToJsonNode(this);
  }

  public String getUseTool() {
    return useTool;
  }

  public void setUseTool(String useTool) {
    this.useTool = useTool;
  }
}