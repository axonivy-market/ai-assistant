package com.axonivy.utils.aiassistant.bean;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

import com.axonivy.portal.components.dto.SecurityMemberDTO;
import com.axonivy.portal.components.util.RoleUtils;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.dto.tool.RetrievalQATool;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.mapper.SecurityMemberDTOMapper;
import com.axonivy.utils.aiassistant.navigation.AiNavigator;
import com.axonivy.utils.aiassistant.service.AiFunctionService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.ISecurityContext;
import ch.ivyteam.ivy.security.IUser;
import ch.ivyteam.ivy.security.exec.Sudo;

public abstract class AbstractFunctionListBean implements Serializable {

  private static final long serialVersionUID = -7426451944596276964L;

  private static final String TOOL_TYPE_CMS_PATTERN = "/Labels/Enums/ToolType/%s";

  protected Assistant assistant;
  private List<String> functionToolTypes;
  protected List<AiFunction> filteredFunctions;
  private AiFunction selectedFunction;
  protected List<AiFunction> nonStartables;
  private List<String> selectedPermissions;
  private boolean isCreation;

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
    AiNavigator.navigateToFunctionConfiguration(getSelectedFunction().getId());
  }

  public boolean isStartable(AiFunction function) {
    if (CollectionUtils.isEmpty(nonStartables)) {
      return true;
    }
    return !nonStartables.contains(function);
  }

  public void onCreateFunction() {
    selectedFunction = new RetrievalQATool();
    isCreation = true;
    selectedPermissions = new ArrayList<>();
  }

  public void onEditFunction(AiFunction function) {
    selectedFunction = function;
    initPermissions(selectedFunction);
    isCreation = false;
  }

  private void initPermissions(AiFunction function) {
    function.setPermissionDTOs(Optional.ofNullable(function)
        .map(AiFunction::getPermissions).orElse(new ArrayList<>()).stream()
        .filter(Objects::nonNull).distinct()
        .map(permission -> findSecurityMemberDtoByName(permission))
        .collect(Collectors.toList()));

    this.selectedPermissions = Optional.ofNullable(function)
        .map(AiFunction::getPermissionDTOs).orElse(new ArrayList<>()).stream()
        .map(SecurityMemberDTO::getName).collect(Collectors.toList());
  }

  private SecurityMemberDTO findSecurityMemberDtoByName(String permission) {
    return permission.startsWith("#")
        ? new SecurityMemberDTO(findUserByUsername(permission))
        : new SecurityMemberDTO(RoleUtils.findRole(permission));
  }

  private IUser findUserByUsername(String username) {
    return Sudo.get(() -> {
      return ISecurityContext.current().users().find(username);
    });
  }

  public String generatePermisisonForDisplay(AiFunction tool) {
    return Optional.ofNullable(tool).map(AiFunction::getPermissions)
        .filter(permisison -> CollectionUtils.isNotEmpty(permisison))
        .isPresent()
            ? String.join(", ",
                RoleUtils.getDisplayNameOfRoles(tool.getPermissions()))
            : StringUtils.EMPTY;
  }

  public List<SecurityMemberDTO> completePermissions(String query) {
    return RoleUtils.findRoles(null, selectedPermissions, query).stream()
        .map(SecurityMemberDTOMapper::mapFromRoleDTO)
        .collect(Collectors.toList());
  }

  public void onSelectPermission(SelectEvent<Object> event) {
    SecurityMemberDTO selectedItem = (SecurityMemberDTO) event.getObject();
    this.selectedPermissions.add(selectedItem.getName());
  }

  public void onUnSelectPermission(UnselectEvent<Object> event) {
    SecurityMemberDTO selectedItem = (SecurityMemberDTO) event.getObject();
    this.selectedPermissions.remove(selectedItem.getName());
  }

  public void save() {
    if (selectedFunction == null) {
      return;
    }
    selectedFunction.setPermissions(selectedPermissions);
    selectedFunction.setIsPublic(true);
    AiFunctionService.getInstance().save(selectedFunction);
  }

  public String formatName(SecurityMemberDTO permission) {
    String responsibleName = EMPTY;
    if (permission != null) {
      if (StringUtils.isBlank(permission.getDisplayName())) {
        responsibleName = permission.getName();
      } else {
        responsibleName = String.format("%s (%s)", permission.getDisplayName(),
            permission.getName());
      }
      return permission.isEnabled() ? responsibleName
          : String.format("%s %s", Ivy.cms().co("/Labels/disabledUserPrefix"),
              responsibleName);
    }
    return responsibleName;
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

  public AiFunction getSelectedFunction() {
    return selectedFunction;
  }

  public void setSelectedFunction(AiFunction selectedFunction) {
    this.selectedFunction = selectedFunction;
  }

  public List<String> getSelectedPermissions() {
    return selectedPermissions;
  }

  public void setSelectedPermissions(List<String> selectedPermissions) {
    this.selectedPermissions = selectedPermissions;
  }

  public boolean isCreation() {
    return isCreation;
  }

  public void setCreation(boolean isCreation) {
    this.isCreation = isCreation;
  }

  public String getFunctionDialogHeader() {
    String headerCms = isCreation
        ? "/Dialogs/com/axonivy/utils/aiassistant/component/AIFunctionList/CreateNewAiFunction"
        : "/Dialogs/com/axonivy/utils/aiassistant/component/AIFunctionList/EditAIFunction";

    return Ivy.cms().co(headerCms);
  }
}
