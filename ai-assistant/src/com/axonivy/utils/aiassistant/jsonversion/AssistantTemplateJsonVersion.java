package com.axonivy.utils.aiassistant.jsonversion;

public class AssistantTemplateJsonVersion extends AbstractJsonVersion {

  public static final AssistantTemplateJsonVersion LATEST_VERSION = new AssistantTemplateJsonVersion(
      LATEST);
  public static final AssistantTemplateJsonVersion OLDEST_VERSION = new AssistantTemplateJsonVersion(
      OLDEST);

  public AssistantTemplateJsonVersion(String value) {
    super(value);
  }
}