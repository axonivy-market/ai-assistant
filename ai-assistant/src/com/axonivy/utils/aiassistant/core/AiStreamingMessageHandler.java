package com.axonivy.utils.aiassistant.core;

import java.util.Queue;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.core.error.OpenAIErrorResponse;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;

public class AiStreamingMessageHandler
    implements StreamingResponseHandler<AiMessage> {

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
  public void onNext(String token) {
    synchronized (this.contents) {
      this.contents.offer(token);
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
  public void onComplete(Response<AiMessage> response) {
    synchronized (this.contents) {
      this.contents.clear();
      this.contents.offer(String.format(COMPLETE_PATTERN, this.completedToken,
          response.content().text()));
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