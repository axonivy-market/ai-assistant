package com.axonivy.utils.aiassistant.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.core.embedding.IvyElasticSearchEmbeddingStore;
import com.axonivy.utils.aiassistant.core.error.OpenAIErrorResponse;
import com.axonivy.utils.aiassistant.dto.AiModel;
import com.axonivy.utils.aiassistant.enums.AiVariable;
import com.axonivy.utils.aiassistant.enums.ModelType;

import ch.ivyteam.ivy.environment.Ivy;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;

public class OpenAIBot extends AbstractAIBot {

  private String modelName;
  private String embeddingModelName;
  private OpenAiChatModel model;
  private OpenAiStreamingChatModel chatModel;
  private OpenAiEmbeddingModel embeddingModel;
  private String apiKey;

  public OpenAIBot(AiModel aiModel) {
    apiKey = aiModel.getApiKey();
    modelName = aiModel.getModel();
    embeddingModelName = aiModel.getEmbeddingModel();
    init();
  }

  public OpenAIBot(String apiKey, String modelName, String embeddingModelName) {
    this.apiKey = apiKey;
    this.modelName = modelName;
    this.embeddingModelName = embeddingModelName;
    init();
  }

  private void init() {
    setModelType(ModelType.OPEN_AI);
    initModel();
    initStreamingChatModel();
    initEmbeddingModel();
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getEmbeddingModelName() {
    return embeddingModelName;
  }

  public void setEmbeddingModelName(String embeddingModelName) {
    this.embeddingModelName = embeddingModelName;
  }

  public OpenAiChatModel getModel() {
    return model;
  }

  public void setModel(OpenAiChatModel model) {
    this.model = model;
  }

  public OpenAiStreamingChatModel getChatModel() {
    return chatModel;
  }

  public void setChatModel(OpenAiStreamingChatModel chatModel) {
    this.chatModel = chatModel;
  }

  public OpenAiEmbeddingModel getEmbeddingModel() {
    return embeddingModel;
  }

  public void setEmbeddingModel(OpenAiEmbeddingModel embeddingModel) {
    this.embeddingModel = embeddingModel;
  }

  @Override
  public void initModel() {
    setModel(
        OpenAiChatModel.builder().apiKey(apiKey).modelName(modelName).build());
  }

  @Override
  public void initEmbeddingModel() {
    setEmbeddingModel(OpenAiEmbeddingModel.builder().apiKey(apiKey)
        .modelName(embeddingModelName)
        .dimensions(DEFAULT_DIMENSIONS).build());
  }

  @Override
  public void initStreamingChatModel() {
    setChatModel(OpenAiStreamingChatModel.builder().apiKey(apiKey)
        .modelName(modelName).build());
  }

  @Override
  public void initEmbeddingStore(String collectionName) {
    setEmbeddingStore(IvyElasticSearchEmbeddingStore.builder()
        .serverUrl(Ivy.var().get(AiVariable.ELASTIC_SEARCH_URL.key))
        .dimension(DEFAULT_DIMENSIONS).indexName(collectionName).build());
  }

  @Override
  public void embed(String collectionName, List<TextSegment> textSegments) {
    if (StringUtils.isBlank(collectionName)
        || CollectionUtils.isEmpty(textSegments)) {
      return;
    }

    initEmbeddingStore(collectionName);

    // Remove old index before embed
    Ivy.log().info("Remove old index " + collectionName);
    getEmbeddingStore().removeIndex();

    // re-create index
    Ivy.log().info("Recreate index " + collectionName);
    initEmbeddingStore(collectionName);

    Ivy.log().info("Start embed vector store");

    for (var segment : textSegments) {
      getEmbeddingStore().add(getEmbeddingModel().embed(segment).content(),
          segment);
    }
    Ivy.log().info("End embed vector store");
  }

  @Override
  public String chat(Map<String, Object> variables, String promptTemplate) {
    try {
      Ivy.log().error("Input");
      Ivy.log()
          .error(PromptTemplate.from(promptTemplate).apply(variables).text());
      Ivy.log().error("Output");
      String result = getModel().generate(
          PromptTemplate.from(promptTemplate).apply(variables).toUserMessage())
          .content().text();
      Ivy.log().error(result);
      return result;
    } catch (Exception e) {
      OpenAIErrorResponse error = BusinessEntityConverter.jsonValueToEntity(
          e.getCause().getMessage(), OpenAIErrorResponse.class);
      return error.getError().getMessage();
    }
  }

  @Override
  public String streamChat(Map<String, Object> variables, String promptTemplate,
      StreamingResponseHandler<AiMessage> handler) {
    try {
      getChatModel().generate(
          PromptTemplate.from(promptTemplate).apply(variables).toUserMessage(),
          handler);
    } catch (Exception e) {
      OpenAIErrorResponse error = BusinessEntityConverter.jsonValueToEntity(
          e.getCause().getMessage(), OpenAIErrorResponse.class);
      return error.getError().getMessage();
    }
    return "";
  }

  @Override
  public String retrieveDocumentsAsString(String collectionName, String query) {
    initEmbeddingStore(collectionName);

    Embedding queryEmbedding = embeddingModel.embed(query).content();

    EmbeddingSearchResult<TextSegment> relevant = getEmbeddingStore()
        .searchApproximateKnn(EmbeddingSearchRequest.builder().maxResults(4)
            .minScore(0.8).queryEmbedding(queryEmbedding).build());

    String result = "";
    for (var match : relevant.matches()) {
      result = String.join("\n\n", result, match.embedded().text());
    }
    return result;
  }

  @Override
  public String testConnection() {
    try {
      getModel().generate(Arrays.asList(new UserMessage("hello")));
    } catch (Exception e) {
      OpenAIErrorResponse error = BusinessEntityConverter.jsonValueToEntity(
          e.getCause().getMessage(), OpenAIErrorResponse.class);
      return error.getError().getMessage();
    }
    return "";
  }

  @Override
  public String testEmbeddingStoreConnection(String collectionName) {
    try {
      initEmbeddingStore(collectionName);
    } catch (Exception e) {
      return e.getCause().getMessage();
    }
    return "";
  }
}