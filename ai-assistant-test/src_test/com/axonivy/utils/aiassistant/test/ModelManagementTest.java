package com.axonivy.utils.aiassistant.test;

import static com.codeborne.selenide.Selenide.open;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.EngineUrl;
import com.axonivy.ivy.webtest.engine.WebAppFixture;
import com.axonivy.utils.aiassistant.test.page.AssistantManagementPage;
import com.axonivy.utils.aiassistant.test.page.ModelDetailsPage;
import com.axonivy.utils.aiassistant.test.page.ModelManagementPage;

@IvyWebTest(headless = false)
public class ModelManagementTest extends BaseTest {

  @BeforeEach
  public void setup(WebAppFixture fixture) {
    fixture.login("Developer", "Developer");
  }

  @Test
  public void manageAssistant(WebAppFixture fixture) {
    open(EngineUrl.createProcessUrl("/ai-assistant/19198CAD6D9F2F9E/AiManagement.ivp"));
    AssistantManagementPage assistantManagementPage = new AssistantManagementPage();
    ModelManagementPage modelManagementPage = assistantManagementPage.goToModelManagementPage();
    ModelDetailsPage modelDetailsPage = modelManagementPage.openModelDetails(1);
    modelDetailsPage.close();
  }

}
