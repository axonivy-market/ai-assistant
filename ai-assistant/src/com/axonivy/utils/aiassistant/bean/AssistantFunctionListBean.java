package com.axonivy.utils.aiassistant.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;

import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.service.AiFunctionService;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class AssistantFunctionListBean extends AbstractFunctionListBean
    implements Serializable {

  private static final long serialVersionUID = 5540463644765110915L;

  private List<AiFunction> functionsToAdd;
  private List<AiFunction> functionsCanAddToAssistant;

  @Override
  public void init(Assistant selectedAssistant) {
    assistant = selectedAssistant;
    functionsToAdd = new ArrayList<>();

    filteredFunctions = assistant.getAllTools();
    if (CollectionUtils.isNotEmpty(filteredFunctions)) {
      filteredFunctions.forEach(function -> function.init());

      nonStartables = filteredFunctions.stream()
          .filter(function -> (function == null || function.isDisabled()))
          .collect(Collectors.toList());
    }

    updateFunctionsCanAddToAssistant();
  }

  public String getRemoveAiFunctionMessage() {
    String assistantName = Optional.ofNullable(assistant)
        .map(Assistant::getName).orElse("");
    return Ivy.cms().co(
        "/Dialogs/com/axonivy/utils/aiassistant/component/AIFunctionList/RemoveFunctionForAssistantMessage",
        Arrays.asList(assistantName));
  }

  @Override
  public void deleteFunction() {
    if (CollectionUtils.isNotEmpty(assistant.getAllTools())) {
      assistant.getAllTools().remove(selectedFunction);
      selectedFunction = null;
      updateFunctionsCanAddToAssistant();
    }
  }

  public void addFunctions() {
    if (assistant.getAllTools() == null) {
      assistant.setAllTools(new ArrayList<>());
    }
    assistant.getAllTools().addAll(functionsToAdd);
    functionsToAdd.clear();
  }

  public void updateFunctionsCanAddToAssistant() {
    Predicate<AiFunction> isEnabled = (func) -> {
      func.init();
      return !func.isDisabled() && func.hasPermision();
    };

    Predicate<AiFunction> notAddedTool = (func) -> {
      for (AiFunction existingFunctions : Optional.ofNullable(filteredFunctions)
          .orElse(new ArrayList<>())) {
        if (existingFunctions.getId().contentEquals(func.getId())) {
          return false;
        }
      }
      return true;
    };

    Predicate<AiFunction> notIvyTool = (func) -> func.getType() != ToolType.IVY;

    functionsCanAddToAssistant = AiFunctionService.getInstance().findAll()
        .stream()
        .filter(func -> isEnabled.and(notAddedTool).and(notIvyTool).test(func))
        .collect(Collectors.toList());
  }

  public AiFunction getSelectedFunction() {
    return selectedFunction;
  }

  public void setSelectedFunction(AiFunction selectedFunction) {
    this.selectedFunction = selectedFunction;
  }

  public List<AiFunction> getFunctionsToAdd() {
    return functionsToAdd;
  }

  public void setFunctionsToAdd(List<AiFunction> functionsToAdd) {
    this.functionsToAdd = functionsToAdd;
  }

  public List<AiFunction> getFunctionsCanAddToAssistant() {
    return functionsCanAddToAssistant;
  }

  public void setFunctionsCanAddToAssistant(
      List<AiFunction> functionsCanAddToAssistant) {
    this.functionsCanAddToAssistant = functionsCanAddToAssistant;
  }
}