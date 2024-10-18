package com.axonivy.utils.aiassistant.test.webtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.WebAppFixture;
import com.axonivy.utils.aiassistant.test.page.AssistantDetailsPage;
import com.axonivy.utils.aiassistant.test.page.AssistantManagementPage;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;

@IvyWebTest(headless = false)
public class AssistantManagementTest extends BaseTest {

  @BeforeEach
  public void setup(WebAppFixture fixture) {
    fixture.login("Developer", "Developer");
  }

  @Test
  public void manageAssistant(WebAppFixture fixture) {
    AssistantManagementPage assistantManagementPage = navigateToAiManagementPage();
    AssistantDetailsPage assistantDetailsPage = assistantManagementPage.addAssistant();
    assistantDetailsPage.useTemplate(0);
    assistantDetailsPage.inputPermission();
    assistantDetailsPage.inputModel();
    assistantDetailsPage.save();
    assistantManagementPage = new AssistantManagementPage();
    Selenide.$$("div[id$='assistant-tab'] div.assistant-name").last().shouldBe(Condition.text("Portal Assistant"));
  }



}
