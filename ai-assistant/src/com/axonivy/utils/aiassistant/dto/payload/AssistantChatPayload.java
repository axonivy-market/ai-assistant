package com.axonivy.utils.aiassistant.dto.payload;

public class AssistantChatPayload {
  private String message;
  private String assistantId;
  private String conversationId;
  private String selectedFunctionMessage;
  private String selectedFunctionId;
  private String type;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getAssistantId() {
    return assistantId;
  }

  public void setAssistantId(String assistantId) {
    this.assistantId = assistantId;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public String getSelectedFunctionMessage() {
    return selectedFunctionMessage;
  }

  public void setSelectedFunctionMessage(String selectedFunctionMessage) {
    this.selectedFunctionMessage = selectedFunctionMessage;
  }

  public String getSelectedFunctionId() {
    return selectedFunctionId;
  }

  public void setSelectedFunctionId(String selectedFunctionId) {
    this.selectedFunctionId = selectedFunctionId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}