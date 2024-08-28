package com.axonivy.utils.aiassistant.prompts;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import dev.langchain4j.model.input.PromptTemplate;

public class BasicPromptTemplates {

  public static final String CONTACT_PART = """
      If you cannot answer the question, please guide user to the below contacts:
      {{emailPart}}
      {{websitePart}}
      """;

  public static final String EMAIL_PART = """
      - Email: {{contactEmail}}
      """;

  public static final String WEBSITE_PART = """
      - Website: {{contactWebsite}}
      """;

  public static final String DETECT_LANGUAGE = """
      I have a message:

      {{input}}

      I want you to detect language of the message and response it inside <>. Example: <English>
      """;

  public static final String RAG_PROMPT_TEMPLATE = """
      This is information about the person you should act as:
      {{info}}

      MUST answer in this language: {{language}}

      And as an assistant, you MUST STRICTLY follow these ethical rules:
      {{ethicalRules}}

      {{contactPart}}

      You should answer question based on some rules:

      1. Don't try to create the answer from your own knowledge.

      2. Keep the tag <image></image>, don't modify it.

      3. Don't format URLs.
      Example 2:
        - Good: https://test.com/configuration.png
        - Bad: ![Configuration](https://test.com/configuration.png)

      4. Double check the answer:
        - remove unnecessary images.
        - restructure the answer to make it easier to understand.

      Now, Please only use the following Context to answer the question.
      Context: {{context}}

      Message From User: {{input}}
      Answer:
      """;

  public static final String EXTRACT_KEYWORD = """
      You are a perfect search engine, and text analyst.
      You are capable of extracting meaningful keywords from documents so later I can find correct document based on the mentioned keywords.
      Please help me extract keywords from this document. Keywords should matched criteria:
      - Ignore "\n" character
      - Keywords should have meaning, or a field name. Don't just get meaningless characters
      - If you cannot find keywords, show "NA", don't try to create a new keyword that not existed in the given document.

      If there are many keywords, only get top 5 keywords which most relevant to the document.
      Please don't chat, just show keywords separated by comma.

      This is the document you need to analyze:

      user message: {{doc}}
      """;

  public static final String CHOOSE_FUNCTION = """
       You are a professional assistant. Your job is categorize message is a question or a request to do something.
       Then you will choose a tool from a list of tools to handle the message.

       These are the tools you can use:

      {{functions}}

       Instructions:
       1. Detect if the message is a question or a request to do something.

       This is the chat history. The message you should use to choose tool is the last message of this chat history:

       AI: Hello User
       User: Hi AI
       {{memory}}

       2. After that, use tool usage to choose a tool from above tools to fulfill the message
          If the message about yourself
            + respond null.
          If the message is a question:
            + ONLY choose tool has type 'RETRIEVAL_QA'.
          If the message is a request to do something:
            + ONLY choose the tool if the object in the request same as in the description of the tool.
              Example: If the subject of the request is to find task, then ONLY choose tool if the request mentioned 'task'.

            + ONLY choose the tool if the action in the request same as in the description of the tool.
              Example: If the action of the request is to find task, then ONLY choose the tool if the request mentioned words similiar to 'find'.

       3. Double-check the result.
          If the chosen tool is not correct, choose again.

       4. ONLY respond the Id of the selected tool.
       """;

  public static final String FULFILL_IVY_TOOL = """
      Metadata:
      {{metadata}}

      You are a computer, you can only respond in JSON format.
      Now, I have a tool in JSON format.
      Please read the tool carefully.

      {{toolJson}}

      Instruction:
      1. Try to fulfill value of the tool's attributes by using the request.
      2. If the request is empty or a confirmation such as 'yes', 'agree',... use the history to fulfill the tool.
      3. If the request is not empty and it synchronized with the chat history, try to fulfill the request from history. Otherwise, just use the request.
      4. Adapt the full version of the JSON above with fulfilled values.
      5. Characters inside the fulfilled JSON should be parseable.
      6. Wrap the result inside '<' and '>' characters. Example: <{"name" : "value"}>
      7. Does the result of the above step wrapped correctly? If not, try to wrap the result then respond it.

      This is chat history:

      {{memory}}
      """;

  public static final String DEFAULT_ANSWER = """
      This is information about the person you should act as:
      {{info}}

      And as an assistant, you MUST STRICTLY follow these ethical rules:
      {{ethicalRules}}

      The customer has this request:

      {{request}}

      I want you to say say sorry and tell him you cannot fulfill his request. Please response in the same language as the request above.

      After that, I want you to take a look at these tool:

      {{tools}}

      IF the tools are empty: Don't need to say anything else. Don't tell them that there are no tools.

      Otherwise:
      Read the tools above, especially the description and attributes.
      List them out as your functions, what you can do.
      ONLY show attributes and descriptions of tools.
      you can modify descriptions of tools to make it easier to understand.
      please answer in readable format, don't show special characters.
      """;

  public static String generateContactPrompt(String email, String website) {
    String emailPart = "";
    String websitePart = "";

    if (StringUtils.isNotBlank(email)) {
      Map<String, Object> emailMap = new HashMap<>();
      emailMap.put("contactEmail", email);
      emailPart = PromptTemplate.from(BasicPromptTemplates.EMAIL_PART)
          .apply(emailMap).text();
    }

    if (StringUtils.isNotBlank(website)) {
      Map<String, Object> websiteMap = new HashMap<>();
      websiteMap.put("contactWebsite", website);
      websitePart = PromptTemplate.from(BasicPromptTemplates.WEBSITE_PART)
          .apply(websiteMap).text();
    }

    Map<String, Object> contactMap = new HashMap<>();
    contactMap.put("emailPart", emailPart);
    contactMap.put("websitePart", websitePart);
    return PromptTemplate.from(BasicPromptTemplates.CONTACT_PART)
        .apply(contactMap).text();
  }
}