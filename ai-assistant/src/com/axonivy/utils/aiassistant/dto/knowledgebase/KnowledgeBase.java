package com.axonivy.utils.aiassistant.dto.knowledgebase;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.aiassistant.core.embedding.EmbeddingDocument;
import com.axonivy.utils.aiassistant.core.embedding.IvyOpenSearchEmbeddingStore;
import com.axonivy.utils.aiassistant.dto.AbstractConfiguration;
import com.axonivy.utils.aiassistant.dto.AiModel;
import com.axonivy.utils.aiassistant.enums.AiVariable;
import com.axonivy.utils.aiassistant.service.AiModelService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import ch.ivyteam.ivy.environment.Ivy;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialEmbeddingModel;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class KnowledgeBase extends AbstractConfiguration implements Serializable {

  private static final long serialVersionUID = -2792737117450136385L;

  private String name;
  private String description;
  private String embeddingModelName;
  private String aiModelName;
  private int numberOfDimensions;

  @JsonIgnore
  private AiModel aiModel;

  @JsonIgnore
  private IvyOpenSearchEmbeddingStore embeddingStore;

  @JsonIgnore
  private OpenAiOfficialEmbeddingModel embeddingModel;

  @JsonIgnore
  private List<EmbeddingDocument> documents;

  @JsonIgnore
  public List<EmbeddingDocument> getDocuments() {
    return documents;
  }

  @JsonIgnore
  public void setDocuments(List<EmbeddingDocument> documents) {
    this.documents = documents;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getEmbeddingModelName() {
    return embeddingModelName;
  }

  public void setEmbeddingModelName(String embeddingModelName) {
    this.embeddingModelName = embeddingModelName;
  }

  public String getAiModelName() {
    return aiModelName;
  }

  public void setAiModelName(String aiModelName) {
    this.aiModelName = aiModelName;
  }

  @JsonIgnore
  public AiModel getAiModel() {
    return aiModel;
  }

  @JsonIgnore
  public void setAiModel(AiModel aiModel) {
    this.aiModel = aiModel;
  }

  public int getNumberOfDimensions() {
    return numberOfDimensions;
  }

  public void setNumberOfDimensions(int numberOfDimensions) {
    this.numberOfDimensions = numberOfDimensions;
  }

  public void initModel() {
    if (StringUtils.isBlank(aiModelName) || aiModel != null) {
      return;
    }
    aiModel = AiModelService.getInstance().findByName(aiModelName);
  }

  public IvyOpenSearchEmbeddingStore getEmbeddingStore() {
    if (embeddingStore == null) {
      embeddingStore = IvyOpenSearchEmbeddingStore.builder()
          .serverUrl(Ivy.var().get(AiVariable.OPEN_SEARCH_VECTOR_STORE_URL.key))
          .indexName(this.getId()).build();
    }
    return embeddingStore;
  }

  public String getIndexStatus() {
    if (aiModel == null) {
      initModel();
    }

    return getEmbeddingStore().getIndexStatus();
  }

  public String createIndex() {
    return getEmbeddingStore().createIndexIfNotExist(numberOfDimensions);
  }

  public String createIndexAndEmbedAllDocuments() {

    String indexStatus = getIndexStatus();
    if (StringUtils.isNotBlank(indexStatus)) {
      return indexStatus;
    }

    // Delete embedded documents
    String deleteIndexResult = getEmbeddingStore().deleteAllDocuments();
    if (StringUtils.isNotBlank(deleteIndexResult)) {
      return deleteIndexResult;
    }

    Ivy.log().info("Start embed vector store");
    getDocuments().forEach(doc -> {
          doc.setDimensions(numberOfDimensions);
          doc.setVector(
              getEmbeddingModel().embed(doc.getText()).content().vector());
        });

    String embedDocumentsResult = getEmbeddingStore()
        .bulkAddNewDocuments(getDocuments());

    Ivy.log().info("End embed vector store");
    return embedDocumentsResult;
  }

  private void prepareToEmbedDocument(EmbeddingDocument doc) {
    initModel();
    doc.setDimensions(numberOfDimensions);
    doc.setVector(getEmbeddingModel().embed(doc.getText()).content().vector());
  }

  public void createEmbeddingDocument(EmbeddingDocument doc) {
    prepareToEmbedDocument(doc);
    doc.setCreatedDate(new Date());

    try {
      getEmbeddingStore().createEmbeddingDocument(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void updateEmbeddingDocument(EmbeddingDocument doc) {
    prepareToEmbedDocument(doc);
    doc.setLastModifiedDate(new Date());

    try {
      getEmbeddingStore().updateEmbeddingDocumentById(doc.getId(), doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String deleteIndex() {
    initModel();
    Ivy.log().info("Remove index " + this.getId());
    try {
      getEmbeddingStore().removeIndex();
    } catch (IOException e) {
      return Ivy.cms().co(
          "/Dialogs/com/axonivy/utils/aiassistant/validation/ErrorOccurredWhenDeleteVectorStoreMessage");
    }
    return StringUtils.EMPTY;
  }

  public void deleteEmbeddingDocument(EmbeddingDocument doc) {
    if (StringUtils.isNotBlank(Optional.ofNullable(doc)
        .map(EmbeddingDocument::getId).orElseGet(() -> null))) {
      try {
        getEmbeddingStore().deleteDocumentById(doc.getId());
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private OpenAiOfficialEmbeddingModel getEmbeddingModel() {
    if (embeddingModel == null) {
      embeddingModel = (OpenAiOfficialEmbeddingModel.builder()
          .apiKey(aiModel.getApiKey()).modelName(embeddingModelName)
          .dimensions(numberOfDimensions)
          .build());
    }
    return embeddingModel;
  }
}