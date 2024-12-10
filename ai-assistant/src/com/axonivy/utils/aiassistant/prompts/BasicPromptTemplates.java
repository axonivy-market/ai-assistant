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

  public static final String CHOOSE_FUNCTION = """
      Tool list:

      {{functions}}

      Request

      {{request}}

      Instructions:

      1. If the request seem to be related to other messages such as "start the second task" or "run the fix car process above", show <none>.
      2. If user just make some casual chatting such as "hello", "how are you?", show <<none>>
      3. Otherwise, analyze carefully and choose the suitable tool
      4. Together with the analysis result from step 3, also show the ID of the selected tool inside '<<' and '>>'. Example:
        + ID: handle-case-flow, output should be <<handle-case-flow>>
        + ID: 1, output should be <<1>>
        + ID: testingPortal, output should be <<testingPortal>>
      5. Show result from step 3 and 4 together""";

  public static final String CHOOSE_FUNCTION_WITH_HISTORY = """
      Tool list:

     {{functions}}


     Chat history:

     User: Hello
      AI: Hello
      {{memory}}

      Instructions:

      1. If user just make some casual chatting such as "hello", "how are you?", show <none>
      2. Otherwise, analyze and choose the suitable tool
      3. Put the result inside '<<' and '>>'. Example:
  		+ ID: handle-case-flow, output should be <<handle-case-flow>>
  		+ ID: 1, output should be <<1>>
  		+ ID: testingPortal, output should be <<testingPortal>>
      """;

  public static final String FULFILL_IVY_TOOL = """
      Metadata:
      {{metadata}}

      Function:

      {{tool}}

      Chat history:

      {{memory}}

      If the last message of the chat history is a confirmation such as 'yes', 'agree', or you don't understand the request of the last message, use all messages as the request.
      Otherwise, only use the last message in the chat history as the request.

      Instruction:
      - Reasoning and anylyze
      - Use the request and the attribute info to generate a json array inside '<' and '>' characters.
      This is the template:
      <[{"name" : "attribute1", "value": "attribute1_value"},{"name" : "attribute2", "value": "attribute2_value"}]>

      Show the result exactly same as the above format.

      Example:

      Function:
      name: Find car
      description: Find information of a process
      Example:

      attribute:
      - name: carColor ; description: color of the car
      - name: type; description: type of the car

      And user message:
      find the red sedan car

      result should be: <[{"name" : "carColor", "value": "red"}, {"name" : "type", "value": "sedan"}]>
      """;

  public static final String FULFILL_IVY_ATTRIBUTE = """
      Metadata:
      {{metadata}}

      You're a computer, don't have ability to talk or explain.

      Attribute:

      {{attribute}}

      Chat history:

      {{memory}}

      If the last message of the chat history is a confirmation such as 'yes', 'agree', or you don't understand the request of the last message, use all messages as the request.
      Otherwise, only use the last message in the chat history as the request.

      Instruction:
      I want to use the request and the attribute info to generate a json array inside '<' and '>' characters.
      This is the template:
      <{"name" : "attribute_name", "value": "attribute1_value"}>
      Show the result exactly same as the above format.

      Example:

      attribute:
      - name: carColor ; description: color of the car

      And user message:
      find the red sedan car

      result should be: <{"name" : "carColor", "value": "red"}>
      """;

  public static final String DEFAULT_ANSWER = """
      Your info:
      {{info}}

      MUST answer in this language regardless the language of user message: {{language}}

      And as an assistant, you MUST STRICTLY follow these ethical rules:
      {{ethicalRules}}

      User message:
      {{request}}

      Instructions:
      1. If the user message is casual chatting such as "hello", "how are you?", answer him politely.
      2. If user asked what you can do, do step 4
      3. Otherwise, I want you to say sorry and tell him you cannot fulfill his request. After that, do step 4
      4. Take a look at these tool:

        {{functions}}

        IF the tools are empty: Don't need to say anything else. Don't tell them that there are no tools.

        Otherwise:
        Read the description and attributes of tools above.
        List them out as your functions, what you can do.
        ONLY show attributes and descriptions of tools.
        you can modify descriptions of tools to make it easier to understand.
      """;

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