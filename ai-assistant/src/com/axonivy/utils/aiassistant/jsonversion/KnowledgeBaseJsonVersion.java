package com.axonivy.utils.aiassistant.jsonversion;

public class KnowledgeBaseJsonVersion extends AbstractJsonVersion {

  public static final KnowledgeBaseJsonVersion LATEST_VERSION = new KnowledgeBaseJsonVersion(
      LATEST);
  public static final KnowledgeBaseJsonVersion OLDEST_VERSION = new KnowledgeBaseJsonVersion(
      OLDEST);

  public KnowledgeBaseJsonVersion(String value) {
    super(value);
  }
}