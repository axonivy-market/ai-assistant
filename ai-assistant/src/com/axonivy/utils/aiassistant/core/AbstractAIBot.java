package com.axonivy.utils.aiassistant.core;

import java.util.List;
import java.util.Map;

import com.axonivy.utils.aiassistant.core.embedding.IvyElasticSearchEmbeddingStore;
import com.axonivy.utils.aiassistant.enums.ModelType;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.StreamingResponseHandler;

public abstract class AbstractAIBot {
  protected static final int DEFAULT_DIMENSIONS = 300;

  private ModelType modelType;

  private IvyElasticSearchEmbeddingStore embeddingStore;

  public ModelType getModelType() {
    return modelType;
  }

  public void setModelType(ModelType modelType) {
    this.modelType = modelType;
  }

  public IvyElasticSearchEmbeddingStore getEmbeddingStore() {
    return embeddingStore;
  }

  public void setEmbeddingStore(IvyElasticSearchEmbeddingStore embeddingStore) {
    this.embeddingStore = embeddingStore;
  }

  public abstract void initModel();

  public abstract void initStreamingChatModel();

  public abstract void initEmbeddingModel();

  public abstract void initEmbeddingStore(String collectionName);

  public abstract void embed(String collectionName,
      List<TextSegment> textSegments);

  public abstract String chat(Map<String, Object> variables,
      String promptTemplate);

  public abstract String streamChat(Map<String, Object> variables,
      String promptTemplate, StreamingResponseHandler<AiMessage> handler);

  public abstract String retrieveDocumentsAsString(String collectionName,
      String query);

  public abstract String testConnection();

  public abstract String testEmbeddingStoreConnection(String collectionName);
}