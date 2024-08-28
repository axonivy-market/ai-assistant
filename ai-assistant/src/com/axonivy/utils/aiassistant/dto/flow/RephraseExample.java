package com.axonivy.utils.aiassistant.dto.flow;

public class RephraseExample {
  private String condition;
  private String before;
  private String after;

  public String getBefore() {
    return before;
  }

  public void setBefore(String before) {
    this.before = before;
  }

  public String getAfter() {
    return after;
  }

  public void setAfter(String after) {
    this.after = after;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }
}