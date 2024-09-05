package com.axonivy.utils.aiassistant.demo.enums;

public enum DemoVariables {
  AI_ASSISTANT("AiAssistant.Assistants"),
  AI_FUNCTIONS("AiAssistant.AiFunctions"),
  COMPLEX_DEMO_FUNCTIONS("AiAssistantDemo.ComplexDemoFunctions"),
  COMPLEX_DEMO_ASSISTANT("AiAssistantDemo.ComplexDemoAssistant"),
  ERROR_HANDLING_DEMO_FUNCTIONS("AiAssistantDemo.ErrorHandlingDemoFunctions"),
  ERROR_HANDLING_DEMO_ASSISTANT("AiAssistantDemo.ErrorHandlingDemoAssistant");

  public String key;

  private DemoVariables(String key) {
    this.key = key;
  }
}
