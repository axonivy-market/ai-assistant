package com.axonivy.utils.aiassistant.history;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.aiassistant.dto.history.Conversation;
import com.axonivy.utils.aiassistant.exception.AiChatException;
import com.google.gson.Gson;

import ch.ivyteam.ivy.scripting.objects.File;
import ch.ivyteam.util.crypto.CryptoUtil;
import ch.ivyteam.util.crypto.CryptoUtil.EncryptionFailedException;

public class ChatMessageManager {

  private static final String CONVERSATION_FILE_PATH = StringUtils.join(
      "ai_conversations", java.io.File.separator, "%s", java.io.File.separator,
      "%s.token");
  private static final String ASSISTANT_FOLDER_PATH = StringUtils
      .join("ai_conversations", java.io.File.separator, "%s");

  private static final String UTF_8 = StandardCharsets.UTF_8.name();

  private ChatMessageManager() {
  }

  private static String encrypt(String dataToEncrypt) {
    try {
      return CryptoUtil.encrypt(dataToEncrypt);
    } catch (EncryptionFailedException e) {
      throw new AiChatException("The give data was not encrypted successfully.",
          e);
    }
  }

  private static String decrypt(String dataToDecrypt) {
    return CryptoUtil.decrypt(dataToDecrypt);
  }

  public static Conversation loadConversation(String assistantId,
      String conversationId) {
    String filepath = generateFilePath(assistantId, conversationId);
    return loadEncryptedConversationFromFilePath(filepath);
  }

  public static void saveConversation(String assistantId,
      Conversation conversation) {
    String filepath = generateFilePath(assistantId, conversation.getId());
    encryptAndSaveConversationToFile(conversation, filepath);
  }

  public static void clearConversation(String assistantId,
      String conversationId) throws IOException {
    File conversationFile = new File(
        generateFilePath(assistantId, conversationId));
    conversationFile.delete();

  }

  private static String generateFilePath(String assistantId, String filename) {
    return String.format(CONVERSATION_FILE_PATH, assistantId, filename);
  }

  private static Conversation loadEncryptedConversationFromFilePath(
      String filepath) {
    try {
      File file = new File(filepath);
      String conversation = file.read(UTF_8);
      if (StringUtils.isNotEmpty(conversation)) {
        String decryptedConversation = decrypt(conversation);

        if (StringUtils.isBlank(decryptedConversation)) {
          return null;
        }

        return new Gson().fromJson(decryptedConversation, Conversation.class);
      }
    } catch (IOException e) {
      throw new AiChatException(
          "Could not load previous conversation. Chat history file could not be decoded",
          e);
    }
    return null;
  }

  private static void encryptAndSaveConversationToFile(
      Conversation conversation, String filepath) {
    String converted = new Gson().toJson(conversation);
    String encrypted = encrypt(converted);
    try {
      File conversationFile = new File(filepath);

      if (!conversationFile.exists()) {
        conversationFile.createNewFile();
      }

      conversationFile.write(encrypted, UTF_8);
    } catch (IOException e) {
      new AiChatException("Could not save the conversation", e);
    }
  }

  public static void deleteAssistantConversationFolder(String assistantId) {
    try {
      String filePath = String.format(ASSISTANT_FOLDER_PATH, assistantId);
      File assistantFolder = new File(filePath);
      FileUtils.deleteDirectory(assistantFolder.getJavaFile());
    } catch (IOException e) {
      new AiChatException(
          "Could not delete the conversations of the assistant " + assistantId,
          e);
    }
  }
}
