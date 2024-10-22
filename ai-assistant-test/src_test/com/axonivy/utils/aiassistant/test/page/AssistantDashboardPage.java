package com.axonivy.utils.aiassistant.test.page;

import static com.codeborne.selenide.Selenide.$;

public class AssistantDashboardPage extends BasePage {

  @Override
  protected String getLoadedLocator() {
    return "button[id$='send-button']";
  }

  public void sendMessage(String message) {
    $("textarea[id$='message-input-field']").sendKeys(message);
    $("button[id$='send-button']").click();
  }

  public void choosePreDefinedMessage() {
    $(".js-candidate-question-block").click();
  }
}
