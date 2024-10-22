package com.axonivy.utils.aiassistant.test.webtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.WebAppFixture;
import com.axonivy.utils.aiassistant.test.page.AssistantManagementPage;
import com.axonivy.utils.aiassistant.test.page.ModelDetailsPage;
import com.axonivy.utils.aiassistant.test.page.ModelManagementPage;

@IvyWebTest
public class ModelManagementTest extends BaseTest {

  @BeforeEach
  public void setup(WebAppFixture fixture) {
    fixture.login("Developer", "Developer");
  }

  @Test
  public void manageModels(WebAppFixture fixture) {
    AssistantManagementPage assistantManagementPage = navigateToAiManagementPage();
    ModelManagementPage modelManagementPage = assistantManagementPage.goToModelManagementPage();
    ModelDetailsPage modelDetailsPage = modelManagementPage.openModelDetails(1);
    modelDetailsPage.close();
  }

}
