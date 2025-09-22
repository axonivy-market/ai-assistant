package com.axonivy.utils.aiassistant.enums;

public enum EmbeddingDocumentStatus {
  DRAFT, EMBEDDED;

  public String getCmsLabel() {
    return "/Labels/Enums/EmbeddingDocumentStatus/" + name();
  }
}
