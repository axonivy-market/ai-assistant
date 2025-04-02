package com.axonivy.utils.aiassistant.dto.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.axonivy.utils.aiassistant.constant.AiConstants;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.prompts.BasicPromptTemplates;
import com.axonivy.utils.aiassistant.prompts.RagPromptTemplates;
import com.axonivy.utils.aiassistant.utils.StringProcessingUtils;
import com.axonivy.utils.aiassistant.utils.TextSplitter;

import ch.ivyteam.ivy.environment.Ivy;

public class RetrievalQATool extends AiFunction {

  private static final long serialVersionUID = -6486736943933821211L;

  private String collection;
  private boolean enableStructuredAnswer;

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public boolean isEnableStructuredAnswer() {
    return enableStructuredAnswer;
  }

  public void setEnableStructuredAnswer(boolean enableStructuredAnswer) {
    this.enableStructuredAnswer = enableStructuredAnswer;
  }

  @Override
  public void init() {
    setDisabled(false);
    setEnableStructuredAnswer(BooleanUtils.isTrue(enableStructuredAnswer));
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

    applyStructureGuidelines(message, assistant, context, params);
    return assistant.getAiModel().getAiBot().chat(params,
        RagPromptTemplates.RAG_PROMPT_TEMPLATE);
  }

  /**
   * Analyzes the request type based on the assistant's AI response to a prompt.
   *
   * @param assistant The AI assistant processing the request.
   * @param message   The input message to analyze.
   * @return An integer representing the detected request type, defaulting to 1
   *         if parsing fails.
   */
  private static int analyzeRequestType(Assistant assistant, String message) {
    String analysisResult = assistant.getAiModel().getAiBot()
        .chat(RagPromptTemplates.getAnalyzeRequestTypePromptTemplate(message));

    // Standardize and extract the numerical request type from the AI's response
    // Default value is 1 if extraction fails or response is invalid
    return NumberUtils
        .toInt(StringProcessingUtils.standardizeResult(analysisResult), 1);
  }

  /**
   * Applies structure guidelines to the assistant's response based on the
   * context length and analyzed request type.
   *
   * @param message   The input message to analyze.
   * @param assistant The AI assistant handling the request.
   * @param context   The retrieved context.
   * @param params    The parameters map of prompt template..
   */
  public void applyStructureGuidelines(String message,
      Assistant assistant,
      String context, Map<String, Object> params) {

    String structureGuidelines = StringUtils.EMPTY;

    // If the option enable structure answer is turned off, skip apply
    // guidelines
    if (!enableStructuredAnswer) {
      params.put("structureGuidelines", structureGuidelines);
      return;
    }

    // Split the context into words using whitespace and count them
    int wordCount = TextSplitter.splitByWhitespace(context).length;

    // Apply structure guidelines only if context has more than 50 words
    if (wordCount > 50) {
      int requestType = RetrievalQATool.analyzeRequestType(assistant, message);
      if (this.collection
          .contentEquals(AiConstants.DEFAULT_PORTAL_GUIDE_KNOWLEDGE_BASE_ID)) {
        structureGuidelines = RagPromptTemplates
            .getPortalOutputInstruction(requestType);
      } else {
        structureGuidelines = RagPromptTemplates
            .getStructuredOutputInstruction(requestType);
      }

    }
    params.put("structureGuidelines", structureGuidelines);
  }
}