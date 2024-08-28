package com.axonivy.utils.aiassistant.dto;

import java.io.Serializable;

import com.axonivy.utils.aiassistant.core.AbstractAIBot;
import com.axonivy.utils.aiassistant.core.OpenAIBot;
import com.axonivy.utils.aiassistant.enums.ModelType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AiModel implements Serializable {

  private static final long serialVersionUID = -4045896164335698506L;

  private ModelType modelType;
  private String name;
  private String displayName;
  private String model;
  private String embeddingModel;
  private String apiKey;

  public ModelType getModelType() {
    return modelType;
  }

  public void setModelType(ModelType modelType) {
    this.modelType = modelType;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getEmbeddingModel() {
    return embeddingModel;
  }

  public void setEmbeddingModel(String embeddingModel) {
    this.embeddingModel = embeddingModel;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonIgnore
  public AbstractAIBot getAiBot() {
    return switch (modelType) {
    case OPEN_AI -> new OpenAIBot(apiKey, model, embeddingModel);
    default -> null;
    };
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}