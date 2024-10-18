package com.axonivy.utils.aiassistant.test;

import static com.codeborne.selenide.Selenide.open;

import com.axonivy.ivy.webtest.engine.EngineUrl;
import com.axonivy.utils.aiassistant.test.page.AssistantManagementPage;

public abstract class BaseTest {

  protected AssistantManagementPage navigateToAiManagementPage() {
    open(EngineUrl.createProcessUrl("/ai-assistant/19198CAD6D9F2F9E/AiManagement.ivp"));
    return new AssistantManagementPage();
  }
}
