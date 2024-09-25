package com.axonivy.utils.aiassistant.jsonversion;

public class AiModelJsonVersion extends AbstractJsonVersion {

  public static final AiModelJsonVersion LATEST_VERSION = new AiModelJsonVersion(
      LATEST);
  public static final AiModelJsonVersion OLDEST_VERSION = new AiModelJsonVersion(
      OLDEST);

  public AiModelJsonVersion(String value) {
    super(value);
  }
}