package com.axonivy.utils.aiassistant.core.embedding;

import java.util.Map;

public class EsDocument {
  private float[] vector;
  private String text;
  private Map<String, Object> metadata;

  public float[] getVector() {
    return vector;
  }

  public void setVector(float[] vector) {
    this.vector = vector;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }
}