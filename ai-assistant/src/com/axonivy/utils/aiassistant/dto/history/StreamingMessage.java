package com.axonivy.utils.aiassistant.dto.history;

import com.axonivy.portal.components.enums.AIState;

public class StreamingMessage {
  private String conversationId;
  private AIState status;
  private String token;

  public StreamingMessage(String conversationId, AIState status, String token) {
    this.conversationId = conversationId;
    this.status = status;
    this.token = token;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public AIState getStatus() {
    return status;
  }

  public void setStatus(AIState status) {
    this.status = status;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}