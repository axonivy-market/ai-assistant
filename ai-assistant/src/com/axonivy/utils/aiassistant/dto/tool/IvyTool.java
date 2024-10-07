package com.axonivy.utils.aiassistant.dto.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

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
import com.axonivy.utils.aiassistant.utils.AiFunctionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import ch.ivyteam.ivy.environment.Ivy;

public class IvyTool extends AiFunction {

  private static final long serialVersionUID = -5362479525475837795L;

  private static final String TOOL_TO_FULFILL_FORMAT = "name: %s \r\ndescription: %s \r\nattributes: \r\n";
  private static final String ATTRIBUTE_LINE_FORMAT = "    - name: %s ; description: %s";

  private static final String FULFILL_ATTRIBUTE_LINE_FORMAT = "name: %s ; description: %s";

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
    setDisabled(!AiFunctionUtils.checkIvyToolInSecurityContext(signature));
  }

  @Override
  public ToolType getType() {
    return ToolType.IVY;
  }

  @JsonIgnore
  public AiResultDTO getResult() {
    if (isDisabled()) {
      return createIvyToolNotFoundError();
    }

    if (!hasPermision()) {
      return createNoPermisisonError();
    }

    Map<String, Object> params = new HashMap<>();
    getAttributes().forEach(attr -> {
      params.put(attr.getName(), attr.getValue());
    });
    Map<String, Object> result = IvyAdapterService
        .startSubProcessInApplication(getSignature(), params);

    AiResultDTO resultDTO = null;

    if (result != null && !result.isEmpty()) {
      resultDTO = (AiResultDTO) result.get("result");
    }

    return resultDTO == null ? createSomethingWentWrongError() : resultDTO;
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
    String fulfillRequestData = buildDataFulfillRequest();

    if (StringUtils.isNotBlank(fulfillRequestData)) {
      fulfillNormalAttributes(memory, bot, metadata, params);
    }

    fulfillImportantAttributes(memory, bot, metadata);

    return this;
  }

  private boolean fulfillNormalAttributes(List<ChatMessage> memory,
      AbstractAIBot bot, String metadata, Map<String, Object> params) {
    params.put("tool", buildDataFulfillRequest());

    params.put("memory", getFormattedMemory(memory));
    params.put("metadata", metadata);

    List<IvyToolAttribute> fulfilled = new ArrayList<>();

    try {
      fulfilled = BusinessEntityConverter
        .jsonValueToEntities(
            AiStep.extractJsonArray(AiStep.extractTextInsideTag(
                bot.chat(params, BasicPromptTemplates.FULFILL_IVY_TOOL))),
            IvyToolAttribute.class);
    } catch (Exception e) {
      return false;
    }

    if (CollectionUtils.isEmpty(fulfilled)) {
      return false;
    }

    for (IvyToolAttribute fulfilledAttribute : fulfilled) {
      for (IvyToolAttribute realAttribute : attributes) {
        if (realAttribute.getName()
            .contentEquals(Optional.ofNullable(fulfilledAttribute)
                .map(IvyToolAttribute::getName).orElse(""))) {
          realAttribute.setValue(Optional.ofNullable(fulfilledAttribute)
              .map(IvyToolAttribute::getValue).orElse(""));
        }
      }
    }

    return true;
  }

  private void fulfillImportantAttributes(List<ChatMessage> memory,
      AbstractAIBot bot, String metadata) throws JsonProcessingException {
    for (IvyToolAttribute realAttribute : attributes) {
      if (BooleanUtils
          .isTrue(BooleanUtils.toBoolean(realAttribute.getIsImportant()))) {
       IvyToolAttribute newAttribute = fulfillIvyToolAttribute(memory, bot, metadata, realAttribute);
       attributes.stream()
           .filter(
               attr -> attr.getName().contentEquals(realAttribute.getName()))
           .findFirst().get().setValue(newAttribute.getValue());
      }
    }
  }

  public IvyToolAttribute fulfillIvyToolAttribute(List<ChatMessage> memory,
      AbstractAIBot bot, String metadata, IvyToolAttribute attr)
      throws JsonProcessingException {
      Map<String, Object> params = new HashMap<>();
      params.put("attribute", buildDataFulfillAttributeRequest(attr));
      params.put("memory", getFormattedMemory(memory));
      params.put("metadata", metadata);

      return BusinessEntityConverter.jsonValueToEntity(
          AiStep.extractTextInsideTag(
              bot.chat(params, BasicPromptTemplates.FULFILL_IVY_ATTRIBUTE)),
          IvyToolAttribute.class);

  }

  public String buildDataFulfillRequest() {
    if (CollectionUtils.isNotEmpty(attributes)) {
      String result = String.format(TOOL_TO_FULFILL_FORMAT, getName(),
          getDescription());
      String attributesStr = "";
      for (IvyToolAttribute attr : attributes) {
        if (BooleanUtils
            .isTrue(BooleanUtils.toBoolean(attr.getIsImportant()))) {
          continue;
        }
        attributesStr = attributesStr
            .concat(String.format(ATTRIBUTE_LINE_FORMAT, attr.getName(),
                attr.getDescription()))
            .concat(System.lineSeparator());
      }
      return result.concat(attributesStr);
    }
    return "";
  }

  private String buildDataFulfillAttributeRequest(
      IvyToolAttribute attributeToFill) {
    return String.format(FULFILL_ATTRIBUTE_LINE_FORMAT,
        attributeToFill.getName(),
        attributeToFill.getDescription());
  }

  @JsonIgnore
  private AiResultDTO createIvyToolNotFoundError() {
    AiResultDTO result = new AiResultDTO();
    result
        .setResult(String.format(
            "Cannot find any callable process with signature %s.", signature));
    result.setState(AIState.ERROR);
    return result;
  }

  public String getUseTool() {
    return useTool;
  }

  public void setUseTool(String useTool) {
    this.useTool = useTool;
  }
}