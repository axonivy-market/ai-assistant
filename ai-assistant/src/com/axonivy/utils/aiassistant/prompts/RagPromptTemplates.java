package com.axonivy.utils.aiassistant.prompts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axonivy.utils.aiassistant.constant.AiConstants;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.store.embedding.EmbeddingMatch;

public class RagPromptTemplates {

  public static final String RAG_PROMPT_TEMPLATE = """
      Context:
      *************************
      {{context}}
      *************************
      This is information about the person you should act as:
      {{info}}
      *************************
      And as an assistant, you MUST STRICTLY follow these ethical rules:
      {{ethicalRules}}
      *************************
      Contact information:
      {{contactPart}}
      *************************
      Instruction:
      - Don't try to create the answer from your own knowledge
      - Restructure the answer to make it easier to understand
      - Prefer the context has higher matching score
      - Prefer to answer with image from the Context part
      - MUST answer in this language regardless the language of user message: {{language}}
      *************************
      Query: {{request}}""";

  public static final String DEFAULT_RAG_ANSWER = """
      And as an assistant, you MUST STRICTLY follow these ethical rules:
      {{ethicalRules}}
      *************************
      Contact information:
      {{contactPart}}
      *************************
      User request:
      {{request}}
      *************************
      Instruction:
      - MUST answer in this language regardless the language of user message: {{language}}
      - Tell user that you don't know the answer for his question, please ask something else or try to contact provided contact info""";

  private static final String CONTEXT_TEMPLATE = """
      Context {{position}} (Matching score: {{score}})

      {{context}}
      --------------------------""";

  public static String formatRetrievedDocuments(
      List<EmbeddingMatch<TextSegment>> retrievedDocuments) {
    String result = "";

    // Loop the documents to generate the formatted result
    for (int i = 0; i < retrievedDocuments.size(); i++) {
      EmbeddingMatch<TextSegment> match = retrievedDocuments.get(i);
      Map<String, Object> params = new HashMap<>();
      params.put("position", i);
      params.put("score", Double.toString(match.score()));
      params.put(AiConstants.CONTEXT, match.embedded().text());

      // Leverage the PromptTemplate of LangChain4J to format the context
      String formattedContext = PromptTemplate.from(CONTEXT_TEMPLATE)
          .apply(params).text();

      // Append the formatted context to result
      result = result.concat(formattedContext).concat(System.lineSeparator());
    }
    return result;
  }
}
