package com.axonivy.utils.aiassistant.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ModelType {
  OPEN_AI("/Logo/OpenAI/Logo", "/Logo/OpenAI/LogoDark", "Open AI");

  private ModelType(String logoCms, String darkLogoCms, String displayName) {
    this.logoCms = logoCms;
    this.darkLogoCms = darkLogoCms;
    this.displayName = displayName;
  }

  private String logoCms;
  private String darkLogoCms;
  private String displayName;

  @JsonValue
  public String getModelType() {
    return this.name().toLowerCase();
  }

  public String getLogoCms() {
    return logoCms;
  }

  public String getDarkLogoCms() {
    return darkLogoCms;
  }

  public String getDisplayName() {
    return displayName;
  }
}