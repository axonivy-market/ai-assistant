package com.axonivy.utils.aiassistant.test.page;

import static com.codeborne.selenide.Condition.appear;
import static com.codeborne.selenide.Selenide.$;

import java.time.Duration;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.WebElementCondition;

public abstract class BasePage {
  protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

  protected BasePage() {
    waitPageLoaded();
  }

  public void waitPageLoaded() {
    $(getLoadedLocator()).shouldBe(appear, DEFAULT_TIMEOUT);
  }

  /**
   * This abstract method is used to determine identity of a page.
   * 
   * @return A unique CSS selector for the particular page.
   */
  protected abstract String getLoadedLocator();

  protected WebElementCondition getClickableCondition() {
    return Condition.and("should be clickable", Condition.visible, Condition.exist);
  }

}
