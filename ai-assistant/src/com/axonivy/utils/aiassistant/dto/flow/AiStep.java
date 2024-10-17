package com.axonivy.utils.aiassistant.dto.flow;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.enums.StepType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ch.ivyteam.ivy.environment.Ivy;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = IvyToolStep.class, name = "IVY_TOOL"),
    @Type(value = SwitchStep.class, name = "SWITCH"),
    @Type(value = TextStep.class, name = "TEXT"),
    @Type(value = RephraseStep.class, name = "RE_PHRASE"),
    @Type(value = TriggerFlowStep.class, name = "TRIGGER_FLOW") })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class AiStep implements Serializable, Cloneable {

  private static final long serialVersionUID = -8890600536780298209L;

  private Integer stepNo;
  private AiResultDTO result;
  private Integer onSuccess;
  private Integer onError;
  private Boolean useConversationMemory;
  private Boolean saveToHistory;
  private String customInstruction;
  private String notificationMessage;
  private Boolean isHidden;

  public AiStep clone() throws CloneNotSupportedException {
    return (AiStep) super.clone();
  }

  public abstract StepType getType();

  public Integer getStepNo() {
    return stepNo;
  }

  public void setStepNo(Integer stepNo) {
    this.stepNo = stepNo;
  }

  public AiResultDTO getResult() {
    return result;
  }

  public void setResult(AiResultDTO result) {
    this.result = result;
  }

  public abstract void run(String message, List<ChatMessage> memory,
      Map<String, String> metadatas, Assistant assistant);

  public static AiResultDTO createSomethingWentWrongError() {
    AiResultDTO result = new AiResultDTO();
    result.setResult(Ivy.cms().co("/Labels/AI/Error/SomethingWentWrong"));
    result.setState(AIState.ERROR);
    return result;
  }

  public Boolean getUseConversationMemory() {
    return useConversationMemory;
  }

  public void setUseConversationMemory(Boolean useConversationMemory) {
    this.useConversationMemory = useConversationMemory;
  }

  public Integer getOnError() {
    return onError;
  }

  public void setOnError(Integer onError) {
    this.onError = onError;
  }

  public Integer getOnSuccess() {
    return onSuccess;
  }

  public void setOnSuccess(Integer onSuccess) {
    this.onSuccess = onSuccess;
  }

  public Boolean getSaveToHistory() {
    return saveToHistory;
  }

  public void setSaveToHistory(Boolean saveToHistory) {
    this.saveToHistory = saveToHistory;
  }

  public Boolean getIsHidden() {
    return isHidden;
  }

  public void setIsHidden(Boolean isHidden) {
    this.isHidden = isHidden;
  }

  public static String getFormattedMetadatas(Map<String, String> metadatas) {
    if (CollectionUtils.isEmpty(metadatas.entrySet())) {
      return "";
    }
    String result = "";
    for (Entry<String, String> entry : metadatas.entrySet()) {
      result += entry.getKey() + ": " + entry.getValue()
          + System.lineSeparator();
    }
    return result;
  }

  public static String extractTextInsideTag(String text) {
    String tagPattern = "<([^>]+)>"; // Regex pattern to match characters inside
                                     // <>
    Pattern pattern = Pattern.compile(tagPattern);
    Matcher matcher = pattern.matcher(text);

    if (matcher.find()) {
      return matcher.group(1); // Return the first captured group
    }
    return "";
  }

  public static String extractJsonArray(String text) {
    String tagPattern = "\\[([^\\]]+)]"; // Regex pattern to match characters
                                         // inside []
    Pattern pattern = Pattern.compile(tagPattern);
    Matcher matcher = pattern.matcher(text);

    if (matcher.find()) {
      return "[" + matcher.group(1) + "]"; // Return the first captured group
                                           // inside array characters
    }
    return "";
  }


  public static String extractTextInsideDoubleTag(String text) {
    String tagPattern = "<<([^>]+)>>"; // Regex pattern to match characters
                                       // inside <<>>
    Pattern pattern = Pattern.compile(tagPattern);
    Matcher matcher = pattern.matcher(text);

    if (matcher.find()) {
      return matcher.group(1); // Return the first captured group
    }
    return "";
  }

  public String getCustomInstruction() {
    return customInstruction;
  }

  public void setCustomInstruction(String customInstruction) {
    this.customInstruction = customInstruction;
  }

  public String getNotificationMessage() {
    return notificationMessage;
  }

  public void setNotificationMessage(String notificationMessage) {
    this.notificationMessage = notificationMessage;
  }
}