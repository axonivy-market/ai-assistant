package com.axonivy.utils.aiassistant.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.dto.SecurityMemberDTO;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.DefaultEthicalRule;
import com.axonivy.utils.aiassistant.service.AiFunctionService;
import com.axonivy.utils.aiassistant.service.AiModelService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Assistant extends AbstractConfiguration implements Serializable {

  private static final long serialVersionUID = 7885313923903511903L;

  private static final String DEFAULT_INFO = "You are a professional, helpful, and kind assistant for a business management process called Axon Ivy. You have the duty to answer questions from customers.";
  public static final String DEFAULT_AVATAR_URI = "/Logo/DefaultLogo";
  private static final String CHOOSE_FUNCTION_PATTERN = "ID: %s\r\n   -Type: %s\r\n   - Usage: %s";

  private String templateId;
  private String aiModelName;
  private String avatarLocation;
  private String avatarType;
  private String avatarContent;
  private String name;

  private String contactWebsite;
  private String contactEmail;
  private List<String> tools;
  private List<String> permissions;

  // Currently, we use default info for assistants. Later we should implement
  // code of conduct to check the infor to make sure that the assistant do bad
  // things such as racist,...
  @SuppressWarnings("unused")
  private String info;

  private List<String> ethicalRules;

  @JsonIgnore
  private List<AiFunction> toolkit;

  // toolkit including all disabled tools
  @JsonIgnore
  private List<AiFunction> allTools;

  @JsonIgnore
  private List<AiFunction> filteredTools;

  @JsonIgnore
  private AiModel aiModel;
  @JsonIgnore
  private List<SecurityMemberDTO> permissionDTOs;

  public Assistant() {
    setIsPublic(true);
    setEthicalRules(Arrays.asList(DefaultEthicalRule.values()).stream()
        .map(DefaultEthicalRule::getRule).collect(Collectors.toList()));
    setInfo(DEFAULT_INFO);
    setAvatarLocation(DEFAULT_AVATAR_URI);
  }

  @JsonIgnore
  public List<AiFunction> getToolkit() {
    return toolkit;
  }

  @JsonIgnore
  public void setToolkit(List<AiFunction> toolkit) {
    this.toolkit = toolkit;
  }

  @JsonIgnore
  public AiModel getAiModel() {
    return aiModel;
  }

  @JsonIgnore
  public void setAiModel(AiModel aiModel) {
    this.aiModel = aiModel;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getTools() {
    return tools;
  }

  public void setTools(List<String> tools) {
    this.tools = tools;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getAvatarLocation() {
    return avatarLocation;
  }

  public void setAvatarLocation(String avatarLocation) {
    this.avatarLocation = avatarLocation;
  }

  public String getAvatarType() {
    return avatarType;
  }

  public void setAvatarType(String avatarType) {
    this.avatarType = avatarType;
  }

  public String getAvatarContent() {
    return avatarContent;
  }

  public void setAvatarContent(String avatarContent) {
    this.avatarContent = avatarContent;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }

  @JsonIgnore
  public List<SecurityMemberDTO> getPermissionDTOs() {
    return permissionDTOs;
  }

  @JsonIgnore
  public void setPermissionDTOs(List<SecurityMemberDTO> permissionDTOs) {
    this.permissionDTOs = permissionDTOs;
  }

  public void initToolkit() {
    if (CollectionUtils.isEmpty(tools)) {
      this.toolkit = new ArrayList<>();
      this.tools = new ArrayList<>();
    }

    List<AiFunction> foundTools = AiFunctionService.getInstance()
        .getPublicConfig();

    this.allTools = foundTools.stream()
        .filter(tool -> this.tools.contains(tool.getId()))
        .collect(Collectors.toList());
    for (AiFunction tool : allTools) {
      if (tool != null) {
        tool.init();
      }
    }

    this.toolkit = allTools.stream().filter(tool -> !tool.isDisabled())
        .collect(Collectors.toList());
  }

  public void initModel() {
    if (StringUtils.isBlank(aiModelName)) {
      return;
    }
    aiModel = AiModelService.getInstance().findByName(aiModelName);
  }

  public String getContactWebsite() {
    return contactWebsite;
  }

  public void setContactWebsite(String contactWebsite) {
    this.contactWebsite = contactWebsite;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public String getTemplateId() {
    return templateId;
  }

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  public List<String> getEthicalRules() {
    return ethicalRules;
  }

  public void setEthicalRules(List<String> ethicalRules) {
    this.ethicalRules = ethicalRules;
  }

  public String formatEthicalRules() {
    if (CollectionUtils.isNotEmpty(ethicalRules)) {
      String result = "";
      for (String rule : ethicalRules) {
        result = result.concat("   - ").concat(rule)
            .concat(System.lineSeparator());
      }
      return result;
    }
    return "";
  }

  public String formatFunctionsForChoose() {
    if (CollectionUtils.isEmpty(toolkit)) {
      return "";
    }
    String result = "";
    for (AiFunction function : toolkit) {
      result = result
          .concat(String.format(CHOOSE_FUNCTION_PATTERN, function.getId(),
              function.getType().name(), function.getUsage()))
          .concat(System.lineSeparator());
    }
    return result;
  }

  public String getAiModelName() {
    return aiModelName;
  }

  public void setAiModelName(String aiModelName) {
    this.aiModelName = aiModelName;
  }

  public List<AiFunction> getAllTools() {
    return allTools;
  }

  public void setAllTools(List<AiFunction> allTools) {
    this.allTools = allTools;
  }

  public List<AiFunction> getFilteredTools() {
    return filteredTools;
  }

  public void setFilteredTools(List<AiFunction> filteredTools) {
    this.filteredTools = filteredTools;
  }

}