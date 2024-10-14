package com.axonivy.utils.aiassistant.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import com.axonivy.portal.components.util.RoleUtils;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.flow.AiFlow;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.dto.tool.RetrievalQATool;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.navigation.AiNavigator;
import com.axonivy.utils.aiassistant.service.AiFunctionService;
import com.axonivy.utils.aiassistant.service.AssistantService;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class FunctionListBean implements Serializable {

  private static final long serialVersionUID = 5915528319848497892L;

  private static final String TOOL_TYPE_CMS_PATTERN = "/Labels/Enums/ToolType/%s";

  private List<AiFunction> filteredFunctions;
  private List<AiFunction> allFunctions;
  private List<String> functionToolTypes;
  private AiFunction selectedFunction;
  private boolean assistantFunctionList;
  private boolean showNonStartableAiFunction;
  private List<AiFunction> functionsCanAddToAssistant;
  private List<AiFunction> functionsToAdd;
  private List<AiFunction> nonStartables;

  private Assistant assistant;

  public void init(Assistant selectedAssistant) {
    functionToolTypes = Arrays.asList(ToolType.values()).stream()
        .map(type -> getToolType(type.name())).toList();

    assistantFunctionList = false;

    if (selectedAssistant != null) {
      assistant = selectedAssistant;
      assistantFunctionList = true;
    }

    allFunctions = assistantFunctionList ? assistant.getAllTools()
        : AiFunctionService.getInstance().findAll();
    allFunctions.forEach(function -> function.init());

    filterAllFunctions();
    toggleNonStartable();
  }

  private void filterAllFunctions() {
    List<AiFunction> startables = filterStartableAiFunctions(allFunctions);
    nonStartables = (List<AiFunction>) CollectionUtils
        .subtract(allFunctions,
        startables);

    startables.sort(Comparator.comparing(AiFunction::getName));
    nonStartables.sort(Comparator.comparing(AiFunction::isEnabled)
        .thenComparing(AiFunction::getType)
        .thenComparing(AiFunction::getName));
    startables.addAll(nonStartables);
    allFunctions = startables;
  }

  /**
   * If turn on: show ivy tools and non-startable AI flows
   * 
   */
  public void toggleNonStartable() {
    if (showNonStartableAiFunction) {
      filteredFunctions = allFunctions;
    } else {
      filteredFunctions = filterStartableAiFunctions(allFunctions);
    }
  }

  private List<AiFunction> filterStartableAiFunctions(
      List<AiFunction> functions) {
    List<AiFunction> result = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(functions)) {
      for (AiFunction function : functions) {
        switch (function) {
          case AiFlow flow -> {
            flow.init();
            if (!flow.isDisabled()) {
              result.add(function);
            }
          }
          case RetrievalQATool knowledgeBase -> {
            if (!knowledgeBase.isDisabled()) {
              result.add(function);
            }
          }
          default ->  {}
        };
      }
    }
    return result;

  }

  public String getToolType(String typeName) {
    return Ivy.cms().co(String.format(TOOL_TYPE_CMS_PATTERN, typeName));
  }

  public String generatePermisisonForDisplay(AiFunction tool) {
    return Optional.ofNullable(tool).map(AiFunction::getPermissions)
        .filter(l -> CollectionUtils.isNotEmpty(l)).isPresent()
            ? String.join(", ",
                RoleUtils.getDisplayNameOfRoles(tool.getPermissions()))
            : "";
  }

  public void navigateToFunctionDetails(String functionId) {
    AiNavigator.navigateToFunctionConfiguration(functionId);
  }

  public void navigateToSelectedFunctionDetails() {
    AiNavigator.navigateToFunctionConfiguration(selectedFunction.getId());
  }

  public String getRemoveAiFunctionMessage() {
    if (BooleanUtils.isTrue(assistantFunctionList)) {
      String assistantName = Optional.ofNullable(assistant)
          .map(Assistant::getName).orElse("");
      return Ivy.cms().co(
          "/Dialogs/com/axonivy/utils/aiassistant/component/AIFunctionList/RemoveFunctionForAssistantMessage",
          Arrays.asList(assistantName));
    }
    if (Optional.ofNullable(selectedFunction).map(AiFunction::getId)
        .isEmpty()) {
      return "";
    }

    List<Assistant> assistants = AssistantService.getInstance().findAssistantByFunctionId(selectedFunction.getId());

    String formattedUsingAssistants = String.join(",",
        assistants.stream().map(Assistant::getName).toList());

    return Ivy.cms().co(
        "/Dialogs/com/axonivy/utils/aiassistant/component/AIFunctionList/RemoveFunctionMessage",
        Arrays.asList(formattedUsingAssistants));
  }

  public void deleteFunction() {
    if (assistantFunctionList) {
      for (String tool : assistant.getTools()) {
        if (tool.contentEquals(selectedFunction.getId())) {
          assistant.getTools().remove(tool);
          assistant.getAllTools().remove(selectedFunction);
          break;
        }
      }
      allFunctions.remove(allFunctions.stream()
          .filter(func -> func.getId().contentEquals(selectedFunction.getId()))
          .findFirst().get());

      filteredFunctions.remove(filteredFunctions.stream()
          .filter(func -> func.getId().contentEquals(selectedFunction.getId()))
          .findFirst().get());
    } else {
      List<AiFunction> allFunctions = AiFunctionService.getInstance()
          .getPublicConfig();
      for (AiFunction function : allFunctions) {
        if (function.getId().contentEquals(selectedFunction.getId())) {
          AiFunctionService.getInstance().delete(function.getId());
          allFunctions.remove(function);
          toggleNonStartable();
          break;
        }
      }
    }
  }

  public List<AiFunction> getFilteredFunctions() {
    return filteredFunctions;
  }

  public void setFilteredFunctions(List<AiFunction> filteredFunctions) {
    this.filteredFunctions = filteredFunctions;
  }

  public List<String> getFunctionToolTypes() {
    return functionToolTypes;
  }

  public void setFunctionToolTypes(List<String> functionToolTypes) {
    this.functionToolTypes = functionToolTypes;
  }

  public AiFunction getSelectedFunction() {
    return selectedFunction;
  }

  public void setSelectedFunction(AiFunction selectedFunction) {
    this.selectedFunction = selectedFunction;
  }

  public List<AiFunction> getAllFunctions() {
    return allFunctions;
  }

  public void setAllFunctions(List<AiFunction> allFunctions) {
    this.allFunctions = allFunctions;
  }

  public boolean isAssistantFunctionList() {
    return assistantFunctionList;
  }

  public void setAssistantFunctionList(boolean assistantFunctionList) {
    this.assistantFunctionList = assistantFunctionList;
  }

  public boolean isShowNonStartableAiFunction() {
    return showNonStartableAiFunction;
  }

  public void setShowNonStartableAiFunction(
      boolean showNonStartableAiFunction) {
    this.showNonStartableAiFunction = showNonStartableAiFunction;
  }

  public List<AiFunction> getFunctionsCanAddToAssistant() {
    return functionsCanAddToAssistant;
  }

  public void setFunctionsCanAddToAssistant(
      List<AiFunction> functionsCanAddToAssistant) {
    this.functionsCanAddToAssistant = functionsCanAddToAssistant;
  }

  public void updateFunctionsCanAddToAssistant() {
    Predicate<AiFunction> isEnabled = (func) -> {
        func.init();
      return !func.isDisabled() && func.hasPermision();
    };

    Predicate<AiFunction> notAddedTool = (func) -> {
      for (AiFunction existingFunctions : allFunctions) {
        if (existingFunctions.getId().contentEquals(func.getId())) {
          return false;
        }
      }
      return true;
    };

    Predicate<AiFunction> notIvyTool = (func) -> func.getType() != ToolType.IVY;

    functionsCanAddToAssistant = AiFunctionService.getInstance().findAll()
        .stream()
        .filter(
            func -> isEnabled.and(notAddedTool).and(notIvyTool).test(func))
        .collect(Collectors.toList());
  }

  public boolean isStartable(AiFunction function) {
    if (CollectionUtils.isEmpty(nonStartables)) {
      return true;
    }
    return !nonStartables.contains(function);
  }

  public void addFunctions() {
    assistant.getAllTools().addAll(functionsToAdd);
    functionsToAdd.clear();
  }

  public List<AiFunction> getFunctionsToAdd() {
    return functionsToAdd;
  }

  public void setFunctionsToAdd(List<AiFunction> functionsToAdd) {
    this.functionsToAdd = functionsToAdd;
  }
}
