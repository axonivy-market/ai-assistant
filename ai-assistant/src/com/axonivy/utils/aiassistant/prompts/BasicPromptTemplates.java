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

  public static final String CHOOSE_FUNCTION = """
       Tool list:

      {{functions}}


      Chat history:

      User: Hello
       AI: Hello
       {{memory}}

       Instructions:

       1. Choose the most suitable tool
       2. ONLY respond the Id of the selected tool
       """;

  public static final String FULFILL_IVY_TOOL = """
      Metadata:
      {{metadata}}

      You're a computer, don't have ability to talk or explain.

      Function:

      {{tool}}

      Chat history:

      {{memory}}

      If the last message of the chat history is a confirmation such as 'yes', 'agree', or you don't understand the request of the last message, use all messages as the request.
      Otherwise, only use the last message in the chat history as the request.

      Instruction:
      I want to use the request and the tool to generate a json array inside '<' and '>' characters.
      This is the template:
      <[{"name" : "attribute1", "value": "attribute1_value"},{"name" : "attribute2", "value": "attribute2_value"}]>

      Show the result exactly same as the above format.

      Example:

      we have function
      name: Find car
      description: Find information of a process
      attributes:
         - name: carName ; description: Name of the car
         - name: carColor ; description: color of the car

      And user message:
      find red car

      result should be: <[{"name" : "carColor", "value": "red"}]>
      """;

  public static final String DEFAULT_ANSWER = """
      Your info:
      {{info}}

      MUST answer in this language: {{language}}

      And as an assistant, you MUST STRICTLY follow these ethical rules:
      {{ethicalRules}}

      The customer has this request:

      {{request}}

      I want you to say sorry and tell him you cannot fulfill his request.

      After that, take a look at these tool:

      {{tools}}

      IF the tools are empty: Don't need to say anything else. Don't tell them that there are no tools.

      Otherwise:
      Read the description and attributes of tools above.
      List them out as your functions, what you can do.
      ONLY show attributes and descriptions of tools.
      you can modify descriptions of tools to make it easier to understand.
      please answer in readable format, don't show special characters.
      """;

  public static final String DEFAULT_ANSWER_RETRIEVAL_QA = """
      MUST answer in this language: {{language}}

      And as an assistant, you MUST STRICTLY follow these ethical rules:
      {{ethicalRules}}

      {{contactPart}}

      User request:
      {{request}}

      Instruction:
      Tell user that you don't know the answer for his question, please ask something else or try to contact provided contact info
      Don't format URLs. Example:
        - Good: https://test.com/configuration.png
        - Bad: ![Configuration](https://test.com/configuration.png)""";

  public static String generateContactPrompt(String email, String website) {
    String emailPart = "";
    String websitePart = "";

    if (StringUtils.isNotBlank(email)) {
      Map<String, Object> emailMap = new HashMap<>();
      emailMap.put("contactEmail", email);
      emailPart = PromptTemplate.from(BasicPromptTemplates.EMAIL_PART)
          .apply(emailMap).text().strip();
    }

    if (StringUtils.isNotBlank(website)) {
      Map<String, Object> websiteMap = new HashMap<>();
      websiteMap.put("contactWebsite", website);
      websitePart = PromptTemplate.from(BasicPromptTemplates.WEBSITE_PART)
          .apply(websiteMap).text().strip();
    }

    Map<String, Object> contactMap = new HashMap<>();
    contactMap.put("emailPart", emailPart);
    contactMap.put("websitePart", websitePart);
    return PromptTemplate.from(BasicPromptTemplates.CONTACT_PART)
        .apply(contactMap).text();
  }
}