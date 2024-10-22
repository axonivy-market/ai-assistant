package com.axonivy.utils.aiassistant.dto.payload;

public class ErrorPayload {

  public ErrorPayload(String error) {
    this.error = error;
  }

  private String error;

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}