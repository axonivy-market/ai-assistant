package com.axonivy.utils.aiassistant.dto.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.axonivy.utils.aiassistant.constant.AiConstants;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.prompts.BasicPromptTemplates;
import com.axonivy.utils.aiassistant.prompts.RagPromptTemplates;
import com.axonivy.utils.aiassistant.utils.AiFunctionUtils;

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
    params.put(AiConstants.REQUEST, message);

    params.put(AiConstants.LANGUAGE,
        Ivy.session().getContentLocale().getDisplayCountry());
    params.put(AiConstants.INFO, assistant.getInfo());
    params.put(AiConstants.ETHICAL_RULES,
        Optional.ofNullable(assistant.formatEthicalRules())
            .orElse(AiConstants.NONE_RESULT));
    params.put(AiConstants.CONTEXT, context);
    params.put(AiConstants.CONTACT_PART,
        BasicPromptTemplates.generateContactPrompt(
        assistant.getContactEmail(), assistant.getContactWebsite()));

    int requestType = analyzeRequestType(assistant, message);
    params.put("structureGuidelines",
        RagPromptTemplates.getStructuredOutputInstruction(requestType));

    return assistant.getAiModel().getAiBot().chat(params,
        RagPromptTemplates.RAG_PROMPT_TEMPLATE);
  }

  public static int analyzeRequestType(Assistant assistant, String message) {
    String messageFromAI = assistant.getAiModel().getAiBot()
        .chat(RagPromptTemplates.getAnalyzeRequestTypePromptTemplate(message));
    return NumberUtils
        .toInt(AiFunctionUtils.extractTextInsideTag(messageFromAI), 1);
  }
}