package com.axonivy.utils.aiassistant.history;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axonivy.portal.components.service.IvyCacheService;
import com.axonivy.utils.aiassistant.dto.history.Conversation;

import ch.ivyteam.ivy.environment.Ivy;

public class ChatMessageManager {

  private static final String IDENTIFIER = "AI_ASSISTANT_SESSION_IDENTIFIER";
  private static final String AI_MESSAGE = "AI_MESSAGE";

  private ChatMessageManager() {
  }

  @SuppressWarnings("unchecked")
  private static List<Conversation> loadAllConversations() {
    IvyCacheService cacheService = IvyCacheService.getInstance();
    Optional<Object> result = cacheService.getSessionCacheValue(AI_MESSAGE,
        getSessionIdentifier());

    if (result.isEmpty()) {
      return new ArrayList<>();
    }

    return (List<Conversation>) result.get();
  }

  public static Conversation loadConversation(String conversationId) {
    List<Conversation> conversations = loadAllConversations();
    return conversations.stream()
        .filter(
            conversation -> conversation.getId().contentEquals(conversationId))
        .findFirst().orElse(null);
  }

  public static void saveConversation(Conversation conversation) {
    List<Conversation> conversations = loadAllConversations();
    boolean existing = false;
    for (Conversation existingConversation: conversations) {
      if (existingConversation.getId().contentEquals(conversation.getId())) {
        existing = true;
        existingConversation = conversation;
        break;
      }
    }

    if (!existing) {
      conversations.add(conversation);
    }

    saveConversationsToCache(conversations);
  }

  public static void clearConversation(String conversationId)
      throws IOException {
    List<Conversation> conversations = loadAllConversations();

    // Duplicate the conversation list to avoid concurrent modification
    List<Conversation> newConversations = new ArrayList<>();
    for (Conversation conversation : conversations) {
      if (!conversation.getId().contentEquals(conversationId)) {
        newConversations.add(conversation);
      }
    }

    saveConversationsToCache(newConversations);
  }

  public static void clearAllConversations() {
    IvyCacheService cacheService = IvyCacheService.getInstance();
    cacheService.invalidateSessionEntry(AI_MESSAGE, getSessionIdentifier());
  }

  private static String getSessionIdentifier() {
    if (Ivy.session().getAttribute(IDENTIFIER) == null) {
      Ivy.session().setAttribute(IDENTIFIER, UUID.randomUUID().toString());
    }
    return (String) Ivy.session().getAttribute(IDENTIFIER);
  }

  private static void saveConversationsToCache(
      List<Conversation> conversations) {
    IvyCacheService cacheService = IvyCacheService.getInstance();
    cacheService.setSessionCache(AI_MESSAGE, getSessionIdentifier(),
        conversations);
  }
}
