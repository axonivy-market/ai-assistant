package com.axonivy.utils.aiassistant.core.embedding;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.aiassistant.enums.EmbeddingDocumentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EmbeddingDocument {

  @JsonProperty("_id")
  private String id;

  private float[] vector;
  private String text;
  private String embeddingModel;
  private int dimensions;
  private Date createdDate;
  private Date lastModifiedDate;

  @JsonIgnore
  private String documentId;

  @JsonIgnore
  private EmbeddingDocumentStatus status;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public float[] getVector() {
    return vector;
  }

  public void setVector(float[] vector) {
    this.vector = vector;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getEmbeddingModel() {
    return embeddingModel;
  }

  public void setEmbeddingModel(String embeddingModel) {
    this.embeddingModel = embeddingModel;
  }

  public int getDimensions() {
    return dimensions;
  }

  public void setDimensions(int dimensions) {
    this.dimensions = dimensions;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public String getDocumentId() {
    return documentId;
  }

  public void setDocumentId(String documentId) {
    this.documentId = documentId;
  }

  public static EmbeddingDocument create(String text) {
    EmbeddingDocument doc = new EmbeddingDocument();
    doc.setDocumentId(
        UUID.randomUUID().toString().replace("-", StringUtils.EMPTY));

    doc.setText(text);
    doc.setCreatedDate(new Date());
    doc.setLastModifiedDate(new Date());
    return doc;
  }

  public void initDocumentId() {
    if (StringUtils.isBlank(documentId)) {
      documentId = UUID.randomUUID().toString().replace("-", StringUtils.EMPTY);
    }
  }

  public EmbeddingDocumentStatus getStatus() {
    return (this.vector == null || this.vector.length == 0)
        ? EmbeddingDocumentStatus.DRAFT
        : EmbeddingDocumentStatus.EMBEDDED;
  }
}