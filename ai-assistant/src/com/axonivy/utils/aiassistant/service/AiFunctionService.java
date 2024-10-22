package com.axonivy.utils.aiassistant.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.AiVariable;

import ch.ivyteam.ivy.environment.Ivy;

public class AiFunctionService extends JsonConfigurationService<AiFunction> {

  private static AiFunctionService instance;

  public static AiFunctionService getInstance() {
    if (instance == null) {
      instance = new AiFunctionService();
    }
    return instance;
  }

  @Override
  public Class<AiFunction> getType() {
    return AiFunction.class;
  }

  @Override
  public String getConfigKey() {
    return AiVariable.AI_FUNCTIONS.key;
  }

  @Override
  public List<AiFunction> getPublicConfig() {
    String jsonValue = Ivy.var().get(getConfigKey());
    if (StringUtils.isBlank(jsonValue)) {
      return new ArrayList<>();
    }

    return BusinessEntityConverter.jsonValueToEntities(jsonValue, getType());
  }
}