package com.axonivy.utils.aiassistant.prompts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.aiassistant.constant.AiConstants;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.store.embedding.EmbeddingMatch;

public class RagPromptTemplates {

  private static final int QUESTION_TYPE_WHAT = 1;
  private static final int QUESTION_TYPE_HOW = 2;
  private static final int QUESTION_TYPE_LIST = 3;
  private static final int QUESTION_TYPE_COMPARE = 4;
  private static final int QUESTION_TYPE_WHY_CANNOT = 5;

  public static final String RAG_PROMPT_TEMPLATE = """
      Context:
      -------
      {{context}}
      -------
      Persona Information:
      -------
      {{info}}
      -------
      Ethical Rules:
      You must strictly adhere to these ethical rules:
      {{ethicalRules}}
      -------
      Contact:
      -------
      {{contactPart}}
      -------
      Instructions:
      - Use only the provided context to generate your answer; do not rely on your own knowledge.
      - If the context does not relate to the query, respond that you don't know.
      - Make sure your answer is clear and easy to understand by restructuring it if necessary.
      - When formulating your answer, prioritize information from contexts with higher scores.
      - Respond in {{language}}, regardless of the language of the user's message.
      - If your answer mentions or refers to an image, provide an explanation of what it depicts or its relevance.
      - Ensure your answer is accurate and supported by the context. If there are any inaccuracies, inform the user that you don't know and suggest they ask another question or contact the provided contact information. Otherwise, present the answer.
      {{structureGuidelines}}
      -------
      Query:
      -------
      {{request}}""";

  public static final String STRUCTURE_GUIDELINES = """
      -------
      How to structure the answer:
      {{structureGuidelines}}

      Double check the result, don't show anything didn't mentioned in the context, don't made up answer. And also hide parts which don't have information to show.
      """;

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

  public static final String ANALYZE_QUESTION_TYPE_TEMPLATE = """
      Query: {{request}}

      Conditions:
        - 1: Question about what is something
        - 2: Question about how to do something
        - 3: Request to list out something
        - 4: Request to compare something
        - 5: Question why cannot do something

      Instruction:
      1. Analyze the query
      2. Choose the right condition.
      3. Put the value of the selected condition inside '<<' and '>>'. Example: <<1>>, <<2>>
      """;

  private static final String QUESTION_TYPE_WHAT_INSTRUCTION = """
      The query is about the definition. Structure your format as follow:

      - The definition of the subject
      - Only show if provided by the context, list out aspects of the subject such as its subtypes, use case, example,...
      - Only show if provided by the context, list out aspect of details of features or subtypes
      - Only show if provided by the context, list out restrictions or limitations of the subject
      - Only show if provided by the context, show all images related to the subject

      Only show avaiable information, each information above should be separated by line seperator, except the last info: "---"
      """;

  private static final String QUESTION_TYPE_HOW_INSTRUCTION = """
      The query is about how to do something. Structure your format as follow:

      - Only show if provided by the context, list out steps to archive the goal, analyze and explain them.
      - Only show if provided by the context, show related images for each step as much as possible
      - Only show if provided by the context, list out limitations or risks if any
      - Summary

      Only show avaiable information, each information above should be separated by line seperator, except the last info: "---"
      """;

  private static final String QUESTION_TYPE_COMPARE_INSTRUCTION = """
      The query is about compare something. Structure your format as follow:

      The header about the comparison

      Then show a table about the all the differences and their details or related information
      Highlight the important differences points

      ---

      Give a brief summary
      """;

  private static final String QUESTION_TYPE_LIST_INSTRUCTION = """
      The query is about list out something. Structure your format as follow:

      - Only show if provided by the context, list out details of the related list.
      - Only show if provided by the context, sub lists if you can found.
      - a brief summary of the list above

      Only show avaiable information, each information above should be separated by line seperator, except the last info: "---"
      """;

  private static final String QUESTION_TYPE_WHY_CANNOT_INSTRUCTION = """
      The query is about asking why something cannot be achived. Structure your format as follow:

      Only show if provided by the context, list out found solutions and their details, how to perform the solution
      """;

  private static final String PORTAL_QUESTION_TYPE_WHAT_INSTRUCTION = """
      The query is about the definition. Structure your format as follow:

      The definition of the subject

      ---

      List out aspects of the subject such as its subtypes, use case, example,...
      List out aspect of details of features or subtypes

      ---

      List out restrictions or limitations of the subject

      ---

      Show all images related to the subject
      """;

  private static final String PORTAL_QUESTION_TYPE_HOW_INSTRUCTION = """
      The query is about how to do something. Structure your format as follow:

      List out steps to archive the goal, analyze and explain them. Show related images for each step as much as possible

      ---

      List out limitations or risks if any

      ---

      Summary
      """;

  private static final String PORTAL_QUESTION_TYPE_COMPARE_INSTRUCTION = """
      The query is about compare something. Structure your format as follow:

      The header about the comparison

      Then show a table about the all the differences and their details or related information
      Highlight the important differences points

      ---

      Give a brief summary
      """;

  private static final String PORTAL_QUESTION_TYPE_LIST_INSTRUCTION = """
      The query is about list out something. Structure your format as follow:

      List out details of the related list. Even sub lists if you can found.
      ---

      a brief summary of the list above
      """;

  private static final String PORTAL_QUESTION_TYPE_WHY_CANNOT_INSTRUCTION = """
      The query is about asking why something cannot be achived. Structure your format as follow:

      List out found solutions and their details, how to perform the solution
      ---

      If the contact point above has website or email, tell user he can contact if the answer does not help
      """;

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
  
  public static String getAnalyzeRequestTypePromptTemplate(String request) {
    Map<String, Object> params = new HashMap<>();
    params.put(AiConstants.REQUEST, request);
    return PromptTemplate.from(ANALYZE_QUESTION_TYPE_TEMPLATE).apply(params)
        .text();
  }

  public static String getStructuredOutputInstruction(int requestType) {
    return switch (requestType) {
      case QUESTION_TYPE_WHAT -> QUESTION_TYPE_WHAT_INSTRUCTION;
      case QUESTION_TYPE_HOW -> QUESTION_TYPE_HOW_INSTRUCTION;
      case QUESTION_TYPE_LIST -> QUESTION_TYPE_LIST_INSTRUCTION;
      case QUESTION_TYPE_COMPARE -> QUESTION_TYPE_COMPARE_INSTRUCTION;
      case QUESTION_TYPE_WHY_CANNOT -> QUESTION_TYPE_WHY_CANNOT_INSTRUCTION;
      default -> StringUtils.EMPTY;
    };
  }
  
  public static String getPortalOutputInstruction(int requestType) {
    return switch (requestType) {
      case QUESTION_TYPE_WHAT -> PORTAL_QUESTION_TYPE_WHAT_INSTRUCTION;
      case QUESTION_TYPE_HOW -> PORTAL_QUESTION_TYPE_HOW_INSTRUCTION;
      case QUESTION_TYPE_LIST -> PORTAL_QUESTION_TYPE_LIST_INSTRUCTION;
      case QUESTION_TYPE_COMPARE -> PORTAL_QUESTION_TYPE_COMPARE_INSTRUCTION;
      case QUESTION_TYPE_WHY_CANNOT -> PORTAL_QUESTION_TYPE_WHY_CANNOT_INSTRUCTION;
      default -> StringUtils.EMPTY;
    };
  }
}
