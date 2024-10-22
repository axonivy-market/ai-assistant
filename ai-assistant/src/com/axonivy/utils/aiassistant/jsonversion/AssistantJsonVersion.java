package com.axonivy.utils.aiassistant.jsonversion;

public class AssistantJsonVersion extends AbstractJsonVersion {

  public static final AssistantJsonVersion LATEST_VERSION = new AssistantJsonVersion(
      LATEST);
  public static final AssistantJsonVersion OLDEST_VERSION = new AssistantJsonVersion(
      OLDEST);

  public AssistantJsonVersion(String value) {
    super(value);
  }
}