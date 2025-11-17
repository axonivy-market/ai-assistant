package com.axonivy.utils.aiassistant.enums;

public enum AiVariable {
  AI_ASSISTANT("AiAssistant.Assistants"),
  AI_FUNCTIONS("AiAssistant.AiFunctions"),
  OPEN_SEARCH_VECTOR_STORE_URL("AiAssistant.OpenSearchVectorStoreUrl"),
  SUGGESTIONS("AiAssistant.Suggestions"),
  AI_ASSISTANT_TEMPLATES("AiAssistant.AssistantTemplates"),
  KNOWLEDGE_BASES("AiAssistant.KnowledgeBases");

  public String key;

  private AiVariable(String key) {
    this.key = key;
  }
}