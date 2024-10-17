package com.axonivy.utils.aiassistant.test.page;

import static com.codeborne.selenide.Selenide.$;

public class ModelManagementPage extends BasePage {

  @Override
  protected String getLoadedLocator() {
    return "a[href$='model-tab']";
  }

  public ModelDetailsPage openModelDetails(int index) {
    $("span[id$='" + index + ":ai-model:model-item-panel']").click();
    return new ModelDetailsPage();
  }
}
