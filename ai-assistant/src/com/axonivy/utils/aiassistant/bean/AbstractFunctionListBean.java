package com.axonivy.utils.aiassistant.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.util.RoleUtils;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.navigation.AiNavigator;

import ch.ivyteam.ivy.environment.Ivy;

public abstract class AbstractFunctionListBean implements Serializable {

  private static final long serialVersionUID = -7426451944596276964L;

  private static final String TOOL_TYPE_CMS_PATTERN = "/Labels/Enums/ToolType/%s";

  protected Assistant assistant;
  private List<String> functionToolTypes;
  protected List<AiFunction> filteredFunctions;
  protected AiFunction selectedFunction;
  protected List<AiFunction> nonStartables;

  public abstract void init(Assistant selectedAssistant);

  public abstract void deleteFunction();

  public List<String> getFunctionToolTypes() {
    if (functionToolTypes == null) {
      functionToolTypes = Arrays.asList(ToolType.values()).stream()
          .map(type -> getToolType(type.name())).toList();
    }
    return functionToolTypes;
  }

  public String getToolType(String typeName) {
    return Ivy.cms().co(String.format(TOOL_TYPE_CMS_PATTERN, typeName));
  }

  public void navigateToFunctionDetails(String functionId) {
    AiNavigator.navigateToFunctionConfiguration(functionId);
  }

  public void navigateToSelectedFunctionDetails() {
    AiNavigator.navigateToFunctionConfiguration(selectedFunction.getId());
  }

  public boolean isStartable(AiFunction function) {
    if (CollectionUtils.isEmpty(nonStartables)) {
      return true;
    }
    return !nonStartables.contains(function);
  }

  public String generatePermisisonForDisplay(AiFunction tool) {
    return Optional.ofNullable(tool).map(AiFunction::getPermissions)
        .filter(permisison -> CollectionUtils.isNotEmpty(permisison))
        .isPresent()
            ? String.join(", ",
                RoleUtils.getDisplayNameOfRoles(tool.getPermissions()))
            : StringUtils.EMPTY;
  }

  public List<AiFunction> getFilteredFunctions() {
    return filteredFunctions;
  }

  public void setFilteredFunctions(List<AiFunction> filteredFunctions) {
    this.filteredFunctions = filteredFunctions;
  }

  public Assistant getAssistant() {
    return assistant;
  }
}
