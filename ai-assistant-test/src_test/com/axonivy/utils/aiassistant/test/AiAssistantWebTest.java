package com.axonivy.utils.aiassistant.test;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.EngineUrl;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.WebElementCondition;

@IvyWebTest
public class AiAssistantWebTest {
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);


  @Test
  public void checkUIOverall() {
    open(EngineUrl.createProcessUrl("/ai-assistant-demo/1919CEDEF7828B77/startComplexDemo.ivp"));
    open(EngineUrl.createProcessUrl("/ai-assistant/19198CAD6D9F2F9E/AssistantDashboard.ivp"));
    $("textarea[id$='message-input-field']").shouldBe(visible, DEFAULT_TIMEOUT);
    $("button[id$='send-button']").shouldBe(getClickableCondition(), DEFAULT_TIMEOUT);
    $("button[id$='ai-management']").shouldBe(getClickableCondition(), DEFAULT_TIMEOUT).click();
    $("button[id$='new-assistant']").shouldBe(getClickableCondition(), DEFAULT_TIMEOUT).click();
    $("a[id$='close-link']").shouldBe(getClickableCondition(), DEFAULT_TIMEOUT).click();
    $("a[href$='model-tab']").shouldBe(getClickableCondition(), DEFAULT_TIMEOUT).click();
    $("span[id$='model-item-panel']").shouldBe(getClickableCondition(), DEFAULT_TIMEOUT).click();
    $("a[id$='close-link']").shouldBe(getClickableCondition(), DEFAULT_TIMEOUT).click();
    $("a[href$='function-tab']").shouldBe(getClickableCondition(), DEFAULT_TIMEOUT).click();
}

  private WebElementCondition getClickableCondition() {
    return Condition.and("should be clickable", visible, enabled);
  }

}
