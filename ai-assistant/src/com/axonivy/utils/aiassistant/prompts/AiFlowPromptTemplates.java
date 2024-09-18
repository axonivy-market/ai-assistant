package com.axonivy.utils.aiassistant.prompts;

public class AiFlowPromptTemplates {
  public static final String FULFILL_CONDITIONAL_STEP = """
      Chat history between "User" and "AI":

      AI: Hello User
      {{memory}}

      Conditions:
      {{conditions}}

      Instruction:
      1. Read the chat history carefully. The last message of "User" is the request.
      2. Choose the right condition.
      3. ONLY show the value of the "action" field from the selected condition as a tag <>.
         Example: If the correct action is "2", then you should show "<2>"
      """;

  public static final String CHECK_USER_MESSAGE_STEP = """
      Instructions:
      - Show <1> when: User's last message is off-topic or unrelated to the chat history.
      - Show <2> when: User's last message continues the context of the chat.

      This is the chat history:

      AI: Hello User
      {{memory}}
      """;

  public static final String RE_PHRASE_STEP = """
      This is information about the person you should act as:
      {{info}}

      Metadata:
      {{metadata}}

      I have chat history between two people, "User" and "AI":

      AI: Hello User
      User: Hi AI
      {{memory}}

      And I have a tool:

      {{tool}}

      Follow the instruction and help me rephrase the last message of user.

      Instruction:

      Check if the last message from User is clear to fulfill any value of the tool's attributes.

      If the message is clear enough to fulfill the tool, don't need to rephrase, just show " ".

      {{customInstruction}}

      If you cannot use the message to fulfill the tool json, rephrase it to make it possible to fulfill the values in the tool json above then ONLY show the rephrased message inside <>.
      The rephrased message must be in English.
      Example: <a message>, otherwise, just show a " "

      Some example how to rephrase:
      {{examples}}
      """;

  public static final String TEXT_STEP_USE_AI = """
      Metadata:
      {{metadata}}

      Chat history:

      AI: Hello User
      {{memory}}

      Instructions to generate a message:

      - {{customInstruction}}
      - ONLY use English
      - Wrap result inside '<' and '>' characters. Examples: <**name: Peter**\n\nDo you want something else?>

      Check the result, does it put inside <> properly? if not, please correct.
      """;
}