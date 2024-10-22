package com.axonivy.utils.aiassistant.jsonversion;

public class SuggestionJsonVersion extends AbstractJsonVersion {

  public static final SuggestionJsonVersion LATEST_VERSION = new SuggestionJsonVersion(
      LATEST);
  public static final SuggestionJsonVersion OLDEST_VERSION = new SuggestionJsonVersion(
      OLDEST);

  public SuggestionJsonVersion(String value) {
    super(value);
  }
}