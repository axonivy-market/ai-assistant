package com.axonivy.utils.aiassistant.test.page;

import static com.codeborne.selenide.Selenide.$;

public class AssistantDetailsPage extends BasePage {

  @Override
  protected String getLoadedLocator() {
    return "button[id$='use-template-button']";
  }

  public void useTemplate(int index) {
    $("button[id$='use-template-button']").click();
    $("span[id$='" + index + ":assistant-template:template-card']").click();
    $("button[id$='use-template-button']").shouldBe(getClickableCondition());
  }

  public void inputPermission() {
    $("button[id$='permissions_button']").click();
    $("tr[id$='permissions_item_1']").click();

  }

  public void inputModel() {
    $("div[id$='assistant-details-form:model']").click();
    $("li[id$='assistant-details-form:model_2']").click();
  }

  public void save() {
    $("button[id$='save-button']").click();

  }

}
