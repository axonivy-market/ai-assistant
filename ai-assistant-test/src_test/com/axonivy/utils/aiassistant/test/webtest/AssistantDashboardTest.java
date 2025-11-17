package com.axonivy.utils.aiassistant.test.webtest;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.EngineUrl;
import com.axonivy.ivy.webtest.engine.WebAppFixture;
import com.axonivy.utils.aiassistant.test.page.AssistantDashboardPage;

@IvyWebTest
public class AssistantDashboardTest extends BaseTest {

  @BeforeEach
  public void setup(WebAppFixture fixture) {
    fixture.login("Developer", "Developer");
  }

  @Test
  public void chatWithAssistant(WebAppFixture fixture) {
    open(EngineUrl.createProcessUrl("/ai-assistant-demo/1919CEDEF7828B77/startComplexDemo.ivp"));
    open(EngineUrl.createProcessUrl("/ai-assistant/19198CAD6D9F2F9E/AssistantDashboard.ivp"));
    AssistantDashboardPage assistantDashboardPage = new AssistantDashboardPage();
    assistantDashboardPage.sendMessage("Hello");
    $("div.chat-message-container.error-response")
        .shouldHave(text("You didn't provide an API key"));
  }

  @Test
  public void choosePreDefinedMessage() {
    open(EngineUrl.createProcessUrl("/ai-assistant-demo/1919CEDEF7828B77/startComplexDemo.ivp"));
    open(EngineUrl.createProcessUrl("/ai-assistant/19198CAD6D9F2F9E/AssistantDashboard.ivp"));
    AssistantDashboardPage assistantDashboardPage = new AssistantDashboardPage();
    assistantDashboardPage.choosePreDefinedMessage();
    $("div.chat-message-container.error-response")
        .shouldHave(text("You didn't provide an API key"));
  }

}
