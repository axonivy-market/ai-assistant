package com.axonivy.utils.aiassistant.dto.payload;

public class AiFlowPayload {
  private String message;
  private String assistantId;
  private String aiFlow;
  private Boolean isSkipMessage;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getAiFlow() {
    return aiFlow;
  }

  public void setAiFlow(String aiFlow) {
    this.aiFlow = aiFlow;
  }

  public String getAssistantId() {
    return assistantId;
  }

  public void setAssistantId(String assistantId) {
    this.assistantId = assistantId;
  }

  public Boolean getIsSkipMessage() {
    return isSkipMessage;
  }

  public void setIsSkipMessage(Boolean isSkipMessage) {
    this.isSkipMessage = isSkipMessage;
  }
}
