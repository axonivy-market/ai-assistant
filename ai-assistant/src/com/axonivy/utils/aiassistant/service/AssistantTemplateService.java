package com.axonivy.utils.aiassistant.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.dto.AssistantTemplate;
import com.axonivy.utils.aiassistant.enums.AiVariable;

import ch.ivyteam.ivy.environment.Ivy;

public class AssistantTemplateService
    extends JsonConfigurationService<AssistantTemplate> {

  private static AssistantTemplateService instance;

  public static AssistantTemplateService getInstance() {
    if (instance == null) {
      instance = new AssistantTemplateService();
    }
    return instance;
  }

  @Override
  public Class<AssistantTemplate> getType() {
    return AssistantTemplate.class;
  }

  @Override
  public String getConfigKey() {
    return AiVariable.AI_ASSISTANT_TEMPLATES.key;
  }

  @Override
  public List<AssistantTemplate> getPublicConfig() {
    String jsonValue = Ivy.var().get(getConfigKey());
    if (StringUtils.isBlank(jsonValue)) {
      return new ArrayList<>();
    }

    return BusinessEntityConverter.jsonValueToEntities(jsonValue, getType());
  }
}
