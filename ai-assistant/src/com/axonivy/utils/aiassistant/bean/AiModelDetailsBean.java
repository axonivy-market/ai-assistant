package com.axonivy.utils.aiassistant.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.util.FacesMessageUtils;
import com.axonivy.utils.aiassistant.dto.AiModel;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.navigation.AiNavigator;
import com.axonivy.utils.aiassistant.service.AiModelService;
import com.axonivy.utils.aiassistant.service.AssistantService;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class AiModelDetailsBean implements Serializable {

  private static final long serialVersionUID = -4096519022068610675L;

  private AiModel selectedModel;
  private String pageTitle;
  private List<Assistant> usingByAssistants;

  public void init(String modelName) {
    selectedModel = AiModelService.getInstance().findAll().stream()
        .filter(a -> a.getName().contentEquals(modelName)).findFirst()
        .orElse(null);
    pageTitle = selectedModel.getName();

    initUsingByAssistants();
  }

  private void initUsingByAssistants() {
    if (Optional.ofNullable(selectedModel).map(AiModel::getName).isEmpty()) {
      usingByAssistants = Collections.emptyList();
      return;
    }

    List<Assistant> assistants = AssistantService.getInstance().findAll();
    usingByAssistants = assistants.stream().filter(assistant -> assistant
        .getAiModelName().contentEquals(selectedModel.getName())).toList();
  }

  private String performTest() {
    return selectedModel.getAiBot().testConnection();
  }

  public void testConnection() {
    String error = performTest();
    if (StringUtils.isBlank(error)) {
      FacesContext.getCurrentInstance().addMessage(null, FacesMessageUtils
          .sanitizedMessage(FacesMessage.SEVERITY_INFO, Ivy.cms().co(
              "/Dialogs/com/axonivy/utils/aiassistant/management/AiModelConfiguration/Connected"),
              ""));
      return;
    }

    FacesContext.getCurrentInstance().addMessage(null, FacesMessageUtils
        .sanitizedMessage(FacesMessage.SEVERITY_ERROR, error, ""));
    FacesContext.getCurrentInstance().isValidationFailed();
  }

  public void save() {
    String error = performTest();
    if (StringUtils.isBlank(error)) {
      AiModelService.getInstance().saveApiKey(selectedModel.getName(),
          selectedModel.getApiKey());

      FacesContext.getCurrentInstance().addMessage(null, FacesMessageUtils
          .sanitizedMessage(FacesMessage.SEVERITY_INFO, Ivy.cms().co(
              "/Dialogs/com/axonivy/utils/aiassistant/management/AiModelConfiguration/ModelSavedMessage",
              Arrays.asList(selectedModel.getDisplayName())), ""));
    } else {
      FacesContext.getCurrentInstance().addMessage(null, FacesMessageUtils
          .sanitizedMessage(FacesMessage.SEVERITY_ERROR, error, ""));
      FacesContext.getCurrentInstance().isValidationFailed();
    }
  }

  public void navigateBack() {
    AiNavigator.navigateToAIManagement();
  }

  public String getPageTitle() {
    return pageTitle;
  }

  public void setPageTitle(String pageTitle) {
    this.pageTitle = pageTitle;
  }

  public AiModel getSelectedModel() {
    return selectedModel;
  }

  public void setSelectedModel(AiModel selectedModel) {
    this.selectedModel = selectedModel;
  }

  public List<Assistant> getUsingByAssistants() {
    return usingByAssistants;
  }

  public void setUsingByAssistants(List<Assistant> usingByAssistants) {
    this.usingByAssistants = usingByAssistants;
  }

  public Boolean canShowUsingByAssistant() {
    return CollectionUtils.isNotEmpty(usingByAssistants);
  }
}