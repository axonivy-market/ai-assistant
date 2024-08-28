package com.axonivy.utils.aiassistant.prompts;

public class AiFlowPromptTemplates {
  public static final String FULFILL_CONDITIONAL_STEP = """
      I have chat history between two people, "User" and "AI":

      AI: Hello User
      User: Hi AI
      {{memory}}

      Conditions to decide how to continue the conversation like this:

      {{conditions}}

      Instruction:
      1. Read the chat history carefully. The lastest message of "User" is the request that "AI" should handle
      2. Choose the right condition.
      3. ONLY show the value of the "action" field from the selected condition as a tag <>.
         Example: If the correct action is "2", then you should show "<2>"
      """;

  public static final String CHECK_USER_MESSAGE_STEP = """
      Instructions:

      Show <0> when:
      User's last message indicates they want to cancel the conversation or request.
      Examples:
        - The message of AI is not a question but the last message of User is a message similar to "cancel" or "never mind".
          AI: I found some tasks for you
          User: Nevermind

        - The message ò AI is a question but the answer of User not make sense.
          AI: Could you provide me some infomation about the user you are looking for?
          User: No

        - User said he want to cancel.
          AI: Do you want to start this task?
          User: No, let's cancel.

      Show <1> when:
      User's last message is not in the same context or aligned with the chat history.
      Examples:
        - If the chat history is about "cases" and the user's last message is about "process," they are not aligned.
          AI: Can you provide me info about this case?
          User: This process name is 'XYZ'

        - The chat history is about "weather" and the user's last message is about "sports."

      Show <2> when:
      User's last message is aligned with and continues the context of the chat history.
      Examples:
        - If the chat history is about "cases" and the user's last message continues to discuss "cases"
          AI: Can you provide me info about this case?
          User: The case name is 'XYZ'

        - If the in the chat history, AI asked something and User don't agree but the answer make sense.
          AI: Do you want to start this process now?
          User: No

        - User's last message agrees with a question from the AI
          AI: Do you want to proceed?
          User: Yes

      This is the chat history you should use to give the result:

      AI: Hello User
      User: Hi AI
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

      I have chat history between two people, "User" and "AI":

      AI: Hello User
      User: Hi AI
      {{memory}}

      Please follow the instructions to generate a message:

      - {{customInstruction}}
      - ONLY use English
      - ONLY show the message inside <>. Example: <a message>
      """;
}