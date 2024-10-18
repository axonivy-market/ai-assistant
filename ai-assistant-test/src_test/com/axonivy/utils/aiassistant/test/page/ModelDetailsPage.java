package com.axonivy.utils.aiassistant.test.page;

import static com.codeborne.selenide.Selenide.$;

public class ModelDetailsPage extends BasePage {

  @Override
  protected String getLoadedLocator() {
    return "input[id$='api-key']";
  }

  public AiManagementPage close() {
    $("a[id$='close-link']").click();
    return new AiManagementPage();

  }

}
