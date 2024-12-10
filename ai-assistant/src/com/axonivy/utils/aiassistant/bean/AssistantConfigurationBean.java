package com.axonivy.utils.aiassistant.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

import com.axonivy.portal.components.dto.SecurityMemberDTO;
import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.portal.components.util.FacesMessageUtils;
import com.axonivy.portal.components.util.RoleUtils;
import com.axonivy.utils.aiassistant.dto.AiModel;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.AssistantTemplate;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.AiVariable;
import com.axonivy.utils.aiassistant.enums.DefaultEthicalRule;
import com.axonivy.utils.aiassistant.history.ChatMessageManager;
import com.axonivy.utils.aiassistant.jsonversion.AssistantJsonVersion;
import com.axonivy.utils.aiassistant.mapper.SecurityMemberDTOMapper;
import com.axonivy.utils.aiassistant.navigation.AiNavigator;
import com.axonivy.utils.aiassistant.service.AiModelService;
import com.axonivy.utils.aiassistant.service.AssistantService;
import com.axonivy.utils.aiassistant.service.AssistantTemplateService;
import com.axonivy.utils.aiassistant.utils.AssistantUtils;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.ISecurityContext;
import ch.ivyteam.ivy.security.IUser;
import ch.ivyteam.ivy.security.exec.Sudo;

@ManagedBean
@ViewScoped
public class AssistantConfigurationBean implements Serializable {

  private static final long serialVersionUID = -363630076717966691L;

  private List<Assistant> assistants;
  private Assistant selectedAssistant;
  private String pageTitle;
  private List<AiModel> models;
  private List<String> selectedPermissions;
  private List<AssistantTemplate> assistantTemplates;

  public void init(String assistantId) {
    selectedPermissions = new ArrayList<>();
    assistants = AssistantService.getInstance().getPublicConfig();

    pageTitle = Ivy.cms().co(
        "/Dialogs/com/axonivy/utils/aiassistant/management/AssistantConfiguration/CreateAssistant");
    models = AiModelService.getInstance().findAll();

    if (CollectionUtils.isEmpty(assistantTemplates)) {
      assistantTemplates = AssistantTemplateService.getInstance()
          .getPublicConfig();
    }

    if (CollectionUtils.isEmpty(assistants)) {
      assistants = new ArrayList<>();
      selectedAssistant = new Assistant();
      return;
    }

    if (StringUtils.isBlank(assistantId)) {
      selectedAssistant = new Assistant();
    } else {
      selectedAssistant = assistants.stream()
          .filter(a -> a.getId().contentEquals(assistantId)).findFirst()
          .orElse(new Assistant());

      if (StringUtils.isNotBlank(selectedAssistant.getAiModelName())) {
        selectedAssistant.setAiModel(AiModelService.getInstance()
            .findByName(selectedAssistant.getAiModelName()));
      }

      if (CollectionUtils.isNotEmpty(selectedAssistant.getPermissions())) {
        selectedAssistant.setPermissionDTOs(selectedAssistant.getPermissions()
            .stream().map(p -> findSecurityMemberDtoByName(p))
            .collect(Collectors.toList()));
      }

      selectedAssistant.initToolkit();
      pageTitle = selectedAssistant.getName();
    }
  }

  public List<Assistant> getAssistants() {
    return assistants;
  }

  public void setAssistants(List<Assistant> assistants) {
    this.assistants = assistants;
  }

  public Assistant getSelectedAssistant() {
    return selectedAssistant;
  }

  public void setSelectedAssistant(Assistant selectedAssistant) {
    this.selectedAssistant = selectedAssistant;
  }

  public void handleAvatarUpload(FileUploadEvent event) {
    removeImage();
    Pair<String, String> imageInfo = AssistantUtils.handleImageUpload(event);
    selectedAssistant.setAvatarLocation(imageInfo.getLeft());
    selectedAssistant.setAvatarType(imageInfo.getRight());
  }

  public void removeImage() {
    if (StringUtils.isNoneBlank(selectedAssistant.getAvatarLocation())) {
      AssistantUtils.removeImage(selectedAssistant.getAvatarLocation(),
          selectedAssistant.getAvatarType());
      selectedAssistant.setAvatarLocation(null);
      selectedAssistant.setAvatarType(null);
    }
  }

  public void save() {
    selectedAssistant.setVersion(AssistantJsonVersion.LATEST);
    pageTitle = selectedAssistant.getName();
    updatePermissionsBeforeSave();
    if (!assistants.contains(selectedAssistant)) {
      assistants.add(selectedAssistant);
    }

    selectedAssistant.setTools(
        Optional.ofNullable(selectedAssistant).map(Assistant::getAllTools)
            .orElse(new ArrayList<>()).stream().map(AiFunction::getId)
            .toList());

    AssistantService.getInstance().saveAllPublicConfig(assistants);

    FacesContext.getCurrentInstance().addMessage(null, FacesMessageUtils
        .sanitizedMessage(FacesMessage.SEVERITY_INFO, Ivy.cms().co(
            "/Dialogs/com/axonivy/utils/aiassistant/management/AssistantConfiguration/AssistantSavedMessage",
            Arrays.asList(selectedAssistant.getName())), StringUtils.EMPTY));
    AiNavigator.navigateToAIManagement();
  }

  private void updatePermissionsBeforeSave() {
    List<SecurityMemberDTO> responsibles = this.selectedAssistant
        .getPermissionDTOs();
    List<String> permissions = new ArrayList<>();

    if (CollectionUtils.isNotEmpty(responsibles)) {
      Collection<SecurityMemberDTO> distinctPermissionDTOs = responsibles
          .stream()
          .collect(Collectors.toMap(SecurityMemberDTO::getMemberName,
              responsible -> responsible,
              (responsible1, responsible2) -> responsible1))
          .values();
      responsibles.clear();
      responsibles.addAll(distinctPermissionDTOs);

      permissions = responsibles.stream().map(SecurityMemberDTO::getMemberName)
          .collect(Collectors.toList());
    }
    this.selectedAssistant.setPermissions(permissions);
  }

  public void delete() {
    assistants.remove(selectedAssistant);
    Ivy.var().set(AiVariable.AI_ASSISTANT.key,
        BusinessEntityConverter.entityToJsonValue(assistants));
    ChatMessageManager.clearAllConversations();
  }

  public void onSelectModel() {
    selectedAssistant.setAiModel(AiModelService.getInstance()
        .findByName(selectedAssistant.getAiModelName()));
  }

  public void navigateBack() {
    AiNavigator.navigateToAIManagement();
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

  public boolean isPersisted() {
    return assistants.contains(selectedAssistant);
  }

  public String getPageTitle() {
    return pageTitle;
  }

  public List<AiModel> getModels() {
    return models;
  }

  public void setModels(List<AiModel> models) {
    this.models = models;
  }

  public List<String> getSelectedPermissions() {
    return selectedPermissions;
  }

  public void setSelectedPermissions(List<String> selectedPermissions) {
    this.selectedPermissions = selectedPermissions;
  }

  public List<AssistantTemplate> getAssistantTemplates() {
    return assistantTemplates;
  }

  public void createAssistantFromTemplate(AssistantTemplate template) {
    selectedAssistant.setTemplateId(template.getId());
    selectedAssistant.setInfo(template.getInfo());
    selectedAssistant.setTools(template.getTools());
    selectedAssistant.initToolkit();

    selectedAssistant.setName(template.getName());

    if (StringUtils.isBlank(selectedAssistant.getContactEmail())) {
      selectedAssistant.setContactEmail(template.getContactEmail());
    }

    if (StringUtils.isBlank(selectedAssistant.getContactWebsite())) {
      selectedAssistant.setContactWebsite(template.getContactWebsite());
    }

    selectedAssistant
        .setEthicalRules(Arrays.asList(DefaultEthicalRule.values()).stream()
            .map(DefaultEthicalRule::getRule).collect(Collectors.toList()));
    selectedAssistant.getEthicalRules().addAll(template.getEthicalRules());

    if (CollectionUtils.isNotEmpty(selectedPermissions)) {
      selectedPermissions.clear();
    }
  }
}