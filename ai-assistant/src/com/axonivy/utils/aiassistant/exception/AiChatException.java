package com.axonivy.utils.aiassistant.exception;

public class AiChatException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AiChatException() {
    super();
  }

  public AiChatException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public AiChatException(String message, Throwable cause) {
    super(message, cause);
  }

  public AiChatException(String message) {
    super(message);
  }

  public AiChatException(Throwable cause) {
    super(cause);
  }

}