package com.axonivy.utils.aiassistant.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.enums.AiVariable;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.IUser;

public class AssistantService extends JsonConfigurationService<Assistant> {

  private static AssistantService instance;

  public static AssistantService getInstance() {
    if (instance == null) {
      instance = new AssistantService();
    }
    return instance;
  }

  @Override
  public Class<Assistant> getType() {
    return Assistant.class;
  }

  @Override
  public String getConfigKey() {
    return AiVariable.AI_ASSISTANT.key;
  }

  @Override
  public List<Assistant> getPublicConfig() {
    String jsonValue = Ivy.var().get(getConfigKey());
    if (StringUtils.isBlank(jsonValue)) {
      return new ArrayList<>();
    }

    return BusinessEntityConverter.jsonValueToEntities(jsonValue, getType());
  }

  @Override
  public Assistant findById(String id) {
    if (StringUtils.isBlank(id)) {
      return null;
    }

    Assistant assistant = super.findById(id);
    assistant.initToolkit();
    assistant.initModel();
    return assistant;
  }

  public List<Assistant> findAvailableAssistantForUser(String username) {
    IUser user = Ivy.security().users().find(username);
    if (user == null) {
      return new ArrayList<>();
    }

    List<Assistant> result = new ArrayList<>();
    List<Assistant> allAssistant = findAll();

    for (var role : user.getAllRoles()) {
      for (var assistant : allAssistant) {
        if (CollectionUtils.isNotEmpty(assistant.getPermissions())
            && assistant.getPermissions().contains(role.getMemberName())
            && !result.contains(assistant)) {
          result.add(assistant);
        }
      }
    }

    return result;
  }

  public List<Assistant> findAssistantByFunctionId(String functionId) {
    List<Assistant> result = new ArrayList<>();

    findAll().forEach(assistant -> {
      if (assistant.getTools().stream()
          .filter(tool -> tool.contentEquals(functionId)).count() > 0) {
        result.add(assistant);
      }
    });

    return result;
  }
}