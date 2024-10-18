package com.axonivy.utils.aiassistant.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.aiassistant.dto.AiModel;
import com.axonivy.utils.aiassistant.enums.ModelType;

import ch.ivyteam.ivy.environment.Ivy;

public class AiModelService {

  private static final String PRIMARY_OPEN_AI_VARIABLE = "AiAssistant.AiModels.OpenAI.PrimaryModel";
  private static final String SECONDARY_OPEN_AI_VARIABLE = "AiAssistant.AiModels.OpenAI.SecondaryModel";
  private static final String MODEL = "Model";
  private static final String EMBEDDING_MODEL = "EmbeddingModel";
  private static final String API_KEY = "ApiKey";
  private static final String DOT = DOT;

  private static AiModelService instance;

  public static AiModelService getInstance() {
    if (instance == null) {
      instance = new AiModelService();
    }
    return instance;
  }

  public List<AiModel> findAll() {
    return Arrays.asList(getPrimaryOpenAIModel(), getSecondaryOpenAIModel());
  }

  private static String getPropertyOfPrimaryOpenAI(String propery) {
    return Ivy.var().get(String.join(DOT, PRIMARY_OPEN_AI_VARIABLE, propery));
  }

  private static String getPropertyOfSecondaryOpenAI(String propery) {
    return Ivy.var().get(String.join(DOT, SECONDARY_OPEN_AI_VARIABLE, propery));
  }

  public static AiModel getPrimaryOpenAIModel() {
    AiModel result = new AiModel();
    result.setName(PRIMARY_OPEN_AI_VARIABLE);
    result.setDisplayName(Ivy.cms().co("/Labels/Model/OpenAI/PrimaryModel"));
    result.setModelType(ModelType.OPEN_AI);
    result.setModel(getPropertyOfPrimaryOpenAI(MODEL));
    result.setEmbeddingModel(getPropertyOfPrimaryOpenAI(EMBEDDING_MODEL));
    result.setApiKey(getPropertyOfPrimaryOpenAI(API_KEY));
    return result;
  }

  public static AiModel getSecondaryOpenAIModel() {
    AiModel result = new AiModel();
    result.setName(SECONDARY_OPEN_AI_VARIABLE);
    result.setDisplayName(Ivy.cms().co("/Labels/Model/OpenAI/SecondaryModel"));
    result.setModelType(ModelType.OPEN_AI);
    result.setModel(getPropertyOfSecondaryOpenAI(MODEL));
    result.setEmbeddingModel(getPropertyOfSecondaryOpenAI(EMBEDDING_MODEL));
    result.setApiKey(getPropertyOfSecondaryOpenAI(API_KEY));
    return result;
  }

  public void saveApiKey(String modelName, String apiKey) {
    Ivy.var().set(String.join(DOT, modelName, API_KEY), apiKey);
  }

  public AiModel findByName(String name) {
    if (StringUtils.isBlank(name)) {
      return null;
    }
    return findAll().stream()
        .filter(model -> model.getName().contentEquals(name)).findFirst().get();
  }

}