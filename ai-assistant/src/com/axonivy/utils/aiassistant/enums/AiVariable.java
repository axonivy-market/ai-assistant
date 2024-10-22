package com.axonivy.utils.aiassistant.enums;

public enum AiVariable {
  AI_ASSISTANT("AiAssistant.Assistants"),
  AI_FUNCTIONS("AiAssistant.AiFunctions"),
  ELASTIC_SEARCH_URL("AiAssistant.ElasticSearchUrl"),
  SUGGESTIONS("AiAssistant.Suggestions"),
  AI_ASSISTANT_TEMPLATES("AiAssistant.AssistantTemplates");

  public String key;

  private AiVariable(String key) {
    this.key = key;
  }
}