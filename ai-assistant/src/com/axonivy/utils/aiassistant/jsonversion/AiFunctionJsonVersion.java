package com.axonivy.utils.aiassistant.jsonversion;

public class AiFunctionJsonVersion extends AbstractJsonVersion {

  public static final AiFunctionJsonVersion LATEST_VERSION = new AiFunctionJsonVersion(
      LATEST);
  public static final AiFunctionJsonVersion OLDEST_VERSION = new AiFunctionJsonVersion(
      OLDEST);

  public AiFunctionJsonVersion(String value) {
    super(value);
  }
}