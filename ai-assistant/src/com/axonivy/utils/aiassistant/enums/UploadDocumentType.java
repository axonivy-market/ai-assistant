package com.axonivy.utils.aiassistant.enums;

public enum UploadDocumentType {
  PORTAL_SUPPORT("Portal Support"), OTHERS("Others");

  private String value;

  private UploadDocumentType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
