package com.axonivy.utils.aiassistant.dto.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AICondition {
  private Integer selectedStep;

  @JsonProperty("case")
  private String condition;
  private String action;

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public Integer getSelectedStep() {
    return selectedStep;
  }

  public void setSelectedStep(Integer selectedStep) {
    this.selectedStep = selectedStep;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}