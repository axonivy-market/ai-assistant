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
      2. Analyze the request and all conditions then choose the right condition. One action could have multiple conditions, so choose carefully.
      3. Put the value of the "action" field of the selected condition inside '<<' and '>>'. Example: <<1>>, <<2>>
      4. Show result from step 2 and 3 together
      """;

  public static final String RE_PHRASE_STEP = """
      Metadata:
      {{metadata}}

      Chat history:

      AI: Hello User
      User: Hi AI
      {{memory}}

      Function:

      {{tool}}

      Follow the instruction and help me rephrase the last message of user.

      Instruction:

      Check if the last message from User is clear to fulfill any value of the attributes.

      If the message is clear enough to fulfill, don't need to rephrase, just show " ".

      {{customInstruction}}

      If you cannot use the message to fulfill any of the attribute, rephrase it to make it possible to fulfill the values of the attributes above then ONLY show the rephrased message inside <>.
      The rephrased message must be in English.
      The rephrased message must include meaning of the attributes you choose to fulfill.

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