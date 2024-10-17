package com.axonivy.utils.aiassistant.dto.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.prompts.BasicPromptTemplates;

import ch.ivyteam.ivy.environment.Ivy;

public class RetrievalQATool extends AiFunction {

  private static final long serialVersionUID = -6486736943933821211L;

  private String collection;

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  @Override
  public void init() {
    setDisabled(false);
  }

  @Override
  public ToolType getType() {
    return ToolType.RETRIEVAL_QA;
  }

  public String testConnection(Assistant assistant) {
    return assistant.getAiModel().getAiBot()
        .testEmbeddingStoreConnection(collection);
  }

  public String answer(Assistant assistant, String message) {
    String context = assistant.getAiModel().getAiBot()
        .retrieveDocumentsAsString(collection, message).strip();

    if (StringUtils.isBlank(context)) {
      return "";
    }

    Map<String, Object> params = new HashMap<>();
    params.put("input", message);

    params.put("language",
        Ivy.session().getContentLocale().getDisplayCountry());
    params.put("info", assistant.getInfo());
    params.put("ethicalRules",
        Optional.ofNullable(assistant.formatEthicalRules()).orElse("<None>"));
    params.put("context", context);
    params.put("contactPart", BasicPromptTemplates.generateContactPrompt(
        assistant.getContactEmail(), assistant.getContactWebsite()));

    return assistant.getAiModel().getAiBot().chat(params,
        BasicPromptTemplates.RAG_PROMPT_TEMPLATE);
  }
}