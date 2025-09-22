package com.axonivy.utils.aiassistant.core;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.client.opensearch.core.SearchResponse;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.core.embedding.EmbeddingDocument;
import com.axonivy.utils.aiassistant.core.embedding.IvyOpenSearchEmbeddingStore;
import com.axonivy.utils.aiassistant.core.error.OpenAIErrorResponse;
import com.axonivy.utils.aiassistant.dto.AiModel;
import com.axonivy.utils.aiassistant.enums.AiVariable;
import com.axonivy.utils.aiassistant.enums.ModelType;
import com.axonivy.utils.aiassistant.prompts.RagPromptTemplates;

import ch.ivyteam.ivy.environment.Ivy;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;

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
        OpenAiChatModel.builder().apiKey(apiKey).modelName(modelName)
            .temperature(Double.valueOf(0)).build());
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
        .modelName(modelName).temperature(Double.valueOf(0)).build());
  }

  @Override
  public void initEmbeddingStore(String collectionName) {
    setEmbeddingStore(IvyOpenSearchEmbeddingStore.builder()
        .serverUrl(Ivy.var().get(AiVariable.OPEN_SEARCH_VECTOR_STORE_URL.key))
        .indexName(collectionName).build());
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
    try {
      getEmbeddingStore().removeIndex();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // re-create index
    Ivy.log().info("Recreate index " + collectionName);
    initEmbeddingStore(collectionName);

    getEmbeddingStore().createIndexIfNotExist(DEFAULT_DIMENSIONS);

    Ivy.log().info("Start embed vector store");

    List<EmbeddingDocument> embeddings = new ArrayList<>();
    for (TextSegment segment : textSegments) {
      EmbeddingDocument doc = new EmbeddingDocument();

      Map<String, String> newMetadata = new HashMap<>();
      for (Entry<String, Object> entry : segment.metadata().toMap()
          .entrySet()) {
        newMetadata.put(entry.getKey(), entry.getValue().toString());
      }
      doc.setText(segment.text());
      doc.setVector(getEmbeddingModel().embed(segment).content().vector());
      embeddings.add(doc);
    }

    getEmbeddingStore().bulkAddNewDocuments(embeddings);
    Ivy.log().info("End embed vector store");
  }

  @Override
  public String chat(Map<String, Object> variables, String promptTemplate) {
    try {
      return getModel()
          .chat(PromptTemplate.from(promptTemplate).apply(variables).text());
    } catch (Exception e) {
      OpenAIErrorResponse error = BusinessEntityConverter.jsonValueToEntity(
          e.getCause().getMessage(), OpenAIErrorResponse.class);
      return error.getError().getMessage();
    }
  }

  @Override
  public String chat(String message) {
    try {
      return getModel().chat(message);
    } catch (Exception e) {
      OpenAIErrorResponse error = BusinessEntityConverter.jsonValueToEntity(
          e.getCause().getMessage(), OpenAIErrorResponse.class);
      return error.getError().getMessage();
    }
  }

  @Override
  public String streamChat(Map<String, Object> variables, String promptTemplate,
      StreamingChatResponseHandler handler) {
    try {
      getChatModel().chat(
          PromptTemplate.from(promptTemplate).apply(variables).text(),
          handler);
    } catch (Exception e) {
      OpenAIErrorResponse error = BusinessEntityConverter.jsonValueToEntity(
          e.getCause().getMessage(), OpenAIErrorResponse.class);
      return error.getError().getMessage();
    }
    return StringUtils.EMPTY;
  }

  @Override
  public String retrieveDocumentsAsString(String collectionName, String query) {
    initEmbeddingStore(collectionName);

    Embedding queryEmbedding = embeddingModel.embed(query).content();

    List<EmbeddingMatch<TextSegment>> relevant = toEmbeddingMatch(
        getEmbeddingStore().findRelevantDocuments(queryEmbedding, 10, 0.7));

    relevant.sort(Comparator.comparing(EmbeddingMatch::score));

    String formattedRetrievedDocuments = RagPromptTemplates
        .formatRetrievedDocuments(relevant);

    return formattedRetrievedDocuments;
  }

  @Override
  public String testConnection() {
    try {
      getModel().chat(Arrays.asList(new UserMessage("hello")));
    } catch (Exception e) {
      OpenAIErrorResponse error = BusinessEntityConverter.jsonValueToEntity(
          e.getCause().getMessage(), OpenAIErrorResponse.class);
      return error.getError().getMessage();
    }
    return StringUtils.EMPTY;
  }

  @Override
  public String testEmbeddingStoreConnection(String collectionName) {
    try {
      initEmbeddingStore(collectionName);
    } catch (Exception e) {
      return e.getCause().getMessage();
    }
    return getEmbeddingStore().getIndexStatus();
  }

  private List<EmbeddingMatch<TextSegment>> toEmbeddingMatch(
      SearchResponse<EmbeddingDocument> response) {
    return response.hits().hits().stream()
        .map(hit -> Optional.ofNullable(hit.source())
            .map(document -> new EmbeddingMatch<>(hit.score(), hit.id(),
                new Embedding(document.getVector()),
                document.getText() == null ? null
                    : TextSegment.from(document.getText())))
            .orElse(null))
        .collect(toList());
  }
}