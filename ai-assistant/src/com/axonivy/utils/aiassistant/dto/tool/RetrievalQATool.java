package com.axonivy.utils.aiassistant.dto.tool;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.fasterxml.jackson.databind.JsonNode;

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
  }

  @Override
  public ToolType getType() {
    return ToolType.RETRIEVAL_QA;
  }

  @Override
  public JsonNode buildJsonNode() {
    return BusinessEntityConverter.entityToJsonNode(this);
  }
}