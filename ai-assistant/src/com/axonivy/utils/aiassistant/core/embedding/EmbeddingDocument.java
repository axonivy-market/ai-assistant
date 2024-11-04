package com.axonivy.utils.aiassistant.core.embedding;

import java.util.Map;

public class EmbeddingDocument {
  private float[] vector;
  private String text;
  private Map<String, String> metadata;

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

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }
}