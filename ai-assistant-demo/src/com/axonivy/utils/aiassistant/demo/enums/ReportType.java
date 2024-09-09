package com.axonivy.utils.aiassistant.demo.enums;

public enum ReportType {
  HARDWARE("Hardware"), SOFWARE("Software"), INTERNET("Internet"),
  OTHERS("Others");

  private String displayName;

  private ReportType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
