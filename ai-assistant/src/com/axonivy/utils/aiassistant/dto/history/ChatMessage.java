package com.axonivy.utils.aiassistant.dto.history;

import java.util.Optional;

import ch.ivyteam.ivy.scripting.objects.DateTime;

public class ChatMessage {
  private static final String USER = "User";
  private static final String AI = "AI";
  private static final String ERROR = "Error";
  private static final String SYSTEM = "System";
  private static final String NOTIFICATION = "Notification";
  private static final String CHAT_MESSAGE_FORMAT = "%s: %s";

  private String content;
  private String role;
  private String time;
  private Boolean isAiFlowMessage;

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public static ChatMessage newUserMessage(String content) {
    ChatMessage result = new ChatMessage();
    result.setContent(content);
    result.setRole(USER);
    result.setTime((new DateTime()).toString());
    result.setIsAiFlowMessage(false);
    return result;
  }

  public static ChatMessage newAIMessage(String content) {
    ChatMessage result = new ChatMessage();
    result.setContent(content);
    result.setRole(AI);
    result.setTime((new DateTime()).toString());
    result.setIsAiFlowMessage(false);
    return result;
  }

  public static ChatMessage newAIFlowMessage(String content) {
    ChatMessage result = new ChatMessage();
    result.setContent(content);
    result.setRole(AI);
    result.setTime((new DateTime()).toString());
    result.setIsAiFlowMessage(true);
    return result;
  }

  public static ChatMessage newErrorMessage(String content) {
    ChatMessage result = new ChatMessage();
    result.setContent(content);
    result.setRole(ERROR);
    result.setTime((new DateTime()).toString());
    result.setIsAiFlowMessage(false);
    return result;
  }

  public static ChatMessage newNotificationMessage(String content) {
    ChatMessage result = new ChatMessage();
    result.setContent(content);
    result.setRole(NOTIFICATION);
    result.setTime((new DateTime()).toString());
    result.setIsAiFlowMessage(false);
    return result;
  }

  public static ChatMessage newSystemMessage(String content) {
    ChatMessage result = new ChatMessage();
    result.setContent(content);
    result.setRole(SYSTEM);
    result.setTime((new DateTime()).toString());
    result.setIsAiFlowMessage(false);
    return result;
  }

  public String getFormattedMessage() {
    return String.format(CHAT_MESSAGE_FORMAT, this.role, this.content);
  }

  public Boolean getIsAiFlowMessage() {
    return isAiFlowMessage;
  }

  public void setIsAiFlowMessage(Boolean isAiFlowMessage) {
    this.isAiFlowMessage = isAiFlowMessage;
  }

  public boolean isSystemMessage() {
    return Optional.ofNullable(this.role).orElse("").contentEquals(SYSTEM);
  }

  public boolean isNotificationMessage() {
    return Optional.ofNullable(this.role).orElse("")
        .contentEquals(NOTIFICATION);
  }

  public boolean isUserMessage() {
    return Optional.ofNullable(this.role).orElse("").contentEquals(USER);
  }
}