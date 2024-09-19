package com.axonivy.utils.aiassistant.dto.tool;

import com.axonivy.utils.aiassistant.enums.ToolType;

public class RetrievalQATool extends AiFunction {

  private static final long serialVersionUID = -6486736943933821211L;

  private String collection;

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  @Override
  public void init() {
    setDisabled(false);
  }

  @Override
  public ToolType getType() {
    return ToolType.RETRIEVAL_QA;
  }
}