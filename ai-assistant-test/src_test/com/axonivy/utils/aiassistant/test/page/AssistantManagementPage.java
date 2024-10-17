package com.axonivy.utils.aiassistant.test.page;

import static com.codeborne.selenide.Selenide.$;

public class AssistantManagementPage extends BasePage {

  @Override
  protected String getLoadedLocator() {
    return "a[href$='assistant-tab']";
  }

  public AssistantDetailsPage addAssistant() {
    $("div[id$='add-assistant']").click();
    return new AssistantDetailsPage();
  }

  public ModelManagementPage goToModelManagementPage() {
    $("a[href$='model-tab']").click();
    return new ModelManagementPage();
  }
}
