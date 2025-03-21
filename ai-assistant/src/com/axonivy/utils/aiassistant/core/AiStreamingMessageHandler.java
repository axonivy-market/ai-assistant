package com.axonivy.utils.aiassistant.core;

import java.util.Queue;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.core.error.OpenAIErrorResponse;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

public class AiStreamingMessageHandler
    implements StreamingChatResponseHandler {

  private static final String ERROR_PATTERN = "<error>%s</error>";
  private static final String COMPLETE_PATTERN = "%s %s";

  private Queue<String> contents;
  private String completedToken;

  public AiStreamingMessageHandler(Queue<String> contents,
      String completedToken) {
    this.contents = contents;
    this.completedToken = completedToken;
  }

  @Override
  public void onPartialResponse(String partialResponse) {
    synchronized (this.contents) {
      this.contents.offer(partialResponse);
    }
  }

  @Override
  public void onError(Throwable error) {
    synchronized (this.contents) {
      this.contents.clear();
      OpenAIErrorResponse errorResponse = BusinessEntityConverter
          .jsonValueToEntity(error.getMessage(), OpenAIErrorResponse.class);
      if (errorResponse != null) {
        this.contents.offer(String.format(ERROR_PATTERN,
            errorResponse.getError().getMessage()));
      } else {
        this.contents.offer(String.format(ERROR_PATTERN, error.getMessage()));
      }

      this.contents.offer(completedToken);
    }
  }

  @Override
  public void onCompleteResponse(ChatResponse completeResponse) {
    synchronized (this.contents) {
      this.contents.clear();
      this.contents.offer(String.format(COMPLETE_PATTERN, this.completedToken,
          completeResponse.aiMessage().text()));
    }
  }

  public Queue<String> getContents() {
    return contents;
  }

  public void setContents(Queue<String> contents) {
    this.contents = contents;
  }

  public String getCompletedToken() {
    return completedToken;
  }

  public void setCompletedToken(String completedToken) {
    this.completedToken = completedToken;
}
}