package com.axonivy.utils.aiassistant.test;

import static com.codeborne.selenide.Selenide.open;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.EngineUrl;
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
    open(EngineUrl.createProcessUrl("/ai-assistant/19198CAD6D9F2F9E/AiManagement.ivp"));
    AssistantManagementPage assistantManagementPage = new AssistantManagementPage();
    AssistantDetailsPage assistantDetailsPage = assistantManagementPage.addAssistant();
    assistantDetailsPage.useTemplate(0);
    assistantDetailsPage.inputPermission();
    assistantDetailsPage.inputModel();
    assistantDetailsPage.save();
    assistantManagementPage = new AssistantManagementPage();
    Selenide.$$("div[id$='assistant-tab'] div.assistant-name").last().shouldBe(Condition.text("Portal Assistant"));
  }

}
