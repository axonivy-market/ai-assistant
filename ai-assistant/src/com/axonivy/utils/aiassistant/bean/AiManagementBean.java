package com.axonivy.utils.aiassistant.bean;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;

import com.axonivy.utils.aiassistant.dto.AiModel;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.navigation.AiNavigator;
import com.axonivy.utils.aiassistant.service.AiFunctionService;
import com.axonivy.utils.aiassistant.service.AiModelService;
import com.axonivy.utils.aiassistant.service.AssistantService;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class AiManagementBean implements Serializable {

  private static final long serialVersionUID = -6788847237975538678L;

  private List<Assistant> assistants;
  private List<AiModel> models;
  private List<AiFunction> functions;
  private Assistant assistant;
  private AiModel model;

  @PostConstruct
  public void init() {
    assistants = AssistantService.getInstance()
        .findAvailableAssistantForUser(Ivy.session().getSessionUserName());
    models = AiModelService.getInstance().findAll();
    functions = AiFunctionService.getInstance().findAll();
    functions.sort((firstFunction, secondFunction) -> firstFunction.getType()
        .name().compareTo(secondFunction.getType().name()));
  }

  public List<Assistant> getAssistants() {
    return assistants;
  }

  public void setAssistants(List<Assistant> assistants) {
    this.assistants = assistants;
  }

  public List<AiModel> getModels() {
    return models;
  }

  public void setModels(List<AiModel> models) {
    this.models = models;
  }

  public Assistant getAssistant() {
    return assistant;
  }

  public void setAssistant(Assistant assistant) {
    this.assistant = assistant;
  }

  public AiModel getModel() {
    return model;
  }

  public void setModel(AiModel model) {
    this.model = model;
  }

  public void navigateToAssistantConfiguration(String assistantId) {
    AiNavigator.navigateToAssistantConfiguration(assistantId);
  }

  public void navigateToModelConfiguration(String modelId) {
    AiNavigator.navigateToAIModelConfiguration(modelId);
  }

  public void navigateToAssistantDashboard() {
    AiNavigator.navigateToAssistantDashboard();
  }

  public boolean isEmptyAssistants() {
    return CollectionUtils.isEmpty(assistants);
  }

  public boolean isEmptyModels() {
    return CollectionUtils.isEmpty(models);
  }

  public List<AiFunction> getFunctions() {
    return functions;
  }

  public void setFunctions(List<AiFunction> functions) {
    this.functions = functions;
  }
}