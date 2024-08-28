package com.axonivy.utils.aiassistant.dto.history;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.axonivy.utils.aiassistant.exception.AiChatException;

public class Conversation {
  private String id;
  private List<ChatMessage> history;
  private List<ChatMessage> memory;

  public Conversation() {
    this.history = new ArrayList<>();
    this.memory = new ArrayList<>();
  };

  private static final String ENCRYPT_ALGORITHM = "MD5";

  public Conversation(String username, String assistantId) {
    setId(generateConversationId(username, assistantId));
    this.history = new ArrayList<>();
    this.memory = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<ChatMessage> getHistory() {
    return history;
  }

  public void setHistory(List<ChatMessage> history) {
    this.history = history;
  }

  public List<ChatMessage> getMemory() {
    return memory;
  }

  public void setMemory(List<ChatMessage> memory) {
    this.memory = memory;
  }

  public static String generateConversationId(String username,
      String assistantId) {
    try {
      MessageDigest md = MessageDigest.getInstance(ENCRYPT_ALGORITHM);
      md.update((username + "_" + assistantId).getBytes());
      byte[] digest = md.digest();
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b & 0xff));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      new AiChatException("Could not generate conversation ID.", e);
    }
    return null;
  }
}