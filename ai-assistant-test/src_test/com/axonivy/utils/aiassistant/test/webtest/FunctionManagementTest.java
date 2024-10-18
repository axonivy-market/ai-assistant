package com.axonivy.utils.aiassistant.test.webtest;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.WebAppFixture;
import com.axonivy.utils.aiassistant.test.page.AssistantManagementPage;
import com.axonivy.utils.aiassistant.test.page.FunctionManagementPage;
import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Selenide;

@IvyWebTest(headless = false)
public class FunctionManagementTest extends BaseTest {

  @BeforeEach
  public void setup(WebAppFixture fixture) {
    fixture.login("Developer", "Developer");
  }

  @Test
  public void manageAssistant(WebAppFixture fixture) {
    AssistantManagementPage assistantManagementPage = navigateToAiManagementPage();
    FunctionManagementPage functionManagementPage = assistantManagementPage.goToFunctionManagementPage();
    Selenide.$$("span[id$='function-name']").shouldHave(CollectionCondition.size(1));
    functionManagementPage.showNonStartableFunctions();
    Selenide.$$("span[id$='function-name']").shouldHave(sizeGreaterThan(1));
  }

}
