package com.axonivy.utils.aiassistant.test.page;

import static com.codeborne.selenide.Selenide.$;

public class FunctionManagementPage extends BasePage {

  @Override
  protected String getLoadedLocator() {
    return "div[id$='toggle-non-startable-ai-function']";
  }

  public void showNonStartableFunctions() {
    $(".ui-toggleswitch-handler").click();
  }
}
