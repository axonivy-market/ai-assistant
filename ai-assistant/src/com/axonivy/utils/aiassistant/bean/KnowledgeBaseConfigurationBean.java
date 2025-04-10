package com.axonivy.utils.aiassistant.bean;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;

import com.axonivy.utils.aiassistant.constant.AiConstants;
import com.axonivy.utils.aiassistant.core.embedding.EmbeddingDocument;
import com.axonivy.utils.aiassistant.dto.knowledgebase.KnowledgeBase;
import com.axonivy.utils.aiassistant.enums.EmbeddingDocumentStatus;
import com.axonivy.utils.aiassistant.navigation.AiNavigator;
import com.axonivy.utils.aiassistant.service.KnowledgeBaseService;
import com.axonivy.utils.aiassistant.utils.TextSplitter;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class KnowledgeBaseConfigurationBean implements Serializable {

  private static final long serialVersionUID = -3172005997858407268L;

  private static final String MESSAGE_COMPONENT_ID = "knowledge-base-messages";

  private KnowledgeBase knowledgeBase;
  private Map<String, String> uploadedSources;
  private List<String> selectedSources;
  private EmbeddingDocument selectedDocument;
  private List<EmbeddingDocumentStatus> statusTypes;
  private Boolean isIndexActive;

  public KnowledgeBase getKnowledgeBase() {
    return knowledgeBase;
  }

  public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
    this.knowledgeBase = knowledgeBase;
  }

  public Map<String, String> getUploadedSources() {
    return uploadedSources;
  }

  public void setUploadedSources(Map<String, String> uploadedSources) {
    this.uploadedSources = uploadedSources;
  }

  public List<String> getSelectedSources() {
    return selectedSources;
  }

  public void setSelectedSources(List<String> selectedSources) {
    this.selectedSources = selectedSources;
  }

  public EmbeddingDocument getSelectedDocument() {
    return selectedDocument;
  }

  public void setSelectedDocument(EmbeddingDocument selectedDocument) {
    this.selectedDocument = selectedDocument;
  }

  public int getMinDimensions() {
    return AiConstants.DEFAULT_DIMENSIONS;
  }

  public int getMaxDimensions() {
    return AiConstants.MAX_DIMENSIONS;
  }

  public void init(String knowledgeBaseId) {
    knowledgeBase = KnowledgeBaseService.getInstance().load(knowledgeBaseId);

    if (knowledgeBase == null) {
      knowledgeBase = new KnowledgeBase();
      knowledgeBase.setName(Ivy.cms().co(
          "/Dialogs/com/axonivy/utils/aiassistant/management/KnowledgeBaseConfiguration/NewKnowledge"));
      knowledgeBase.setDocuments(new ArrayList<>());
    }

    uploadedSources = new HashMap<>();
    selectedSources = new ArrayList<>();
  }

  public void checkIndexStatus() {
    String result = knowledgeBase.getIndexStatus();
    if (StringUtils.isBlank(result)) {
      isIndexActive = true;
    } else {
      showError(result);
      isIndexActive = false;
    }
  }
  
  public void handleUploadSource(FileUploadEvent event) {
    uploadedSources.put(event.getFile().getFileName(),
        new String(event.getFile().getContent(), StandardCharsets.UTF_8));
  }

  public String getSelectedSourcesInfo() {
    return Ivy.cms().co(
        "/Dialogs/com/axonivy/utils/aiassistant/management/KnowledgeBaseConfiguration/SelectedSourcesInfo",
        Arrays.asList(selectedSources.size()));
  }

  public void generate() {
    if (selectedSources.isEmpty()) {
      return;
    }

    for (String selectedSource : selectedSources) {
      String selectedDocument = uploadedSources.entrySet().stream()
          .filter(source -> selectedSource.contentEquals(source.getKey()))
          .map(Entry::getValue).findFirst().orElseGet(() -> StringUtils.EMPTY);

      if (!selectedDocument.isBlank()) {
        List<String> splittedDocuments = TextSplitter
            .splitDocumentByParagraph(selectedDocument);
        
        for (String splitted : splittedDocuments) {
          EmbeddingDocument newDoc = EmbeddingDocument.create(splitted,
              selectedSource);

          if (knowledgeBase.getDocuments() == null) {
            knowledgeBase.setDocuments(new ArrayList<>());
          }

          knowledgeBase.getDocuments().add(newDoc);
        }
      }
    }
  }

  public void editDocument(String documentId) {
    selectedDocument = knowledgeBase.getDocuments().stream()
        .filter(doc -> doc.getDocumentId().contentEquals(documentId))
        .findFirst().get();
  }

  public void updateEmpbeddings() {
    String embeddingDocumentsResult = knowledgeBase
        .createIndexAndEmbedAllDocuments();

    if (StringUtils.isNotBlank(embeddingDocumentsResult)) {
      showError(embeddingDocumentsResult);
    }

    selectedSources.clear();
    uploadedSources.clear();
  }

  public void save() {
    knowledgeBase.setIsPublic(true);
    KnowledgeBaseService.getInstance().save(knowledgeBase);
  }

  public void updateDocument() {
    if (StringUtils.isBlank(selectedDocument.getId())) {
      knowledgeBase.createEmbeddingDocument(selectedDocument);
    } else {
      selectedDocument.setSourceFile(StringUtils.EMPTY);
      knowledgeBase.updateEmbeddingDocument(selectedDocument);
    }
  }

  public void deleteDocument(EmbeddingDocument doc) {
    knowledgeBase.deleteEmbeddingDocument(doc);
    knowledgeBase.getDocuments().remove(doc);
  }

  public void onCreateNewDocument() {
    selectedDocument = EmbeddingDocument.create(StringUtils.EMPTY,
        StringUtils.EMPTY);
  }

  private void showError(String error) {
    FacesContext.getCurrentInstance().addMessage(MESSAGE_COMPONENT_ID,
        new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
  }

  public void onClose() {
    if (knowledgeBase.getDocuments().stream()
        .filter(doc -> doc.getStatus() == EmbeddingDocumentStatus.DRAFT)
        .findFirst().isPresent()) {
      PrimeFaces.current().executeScript("PF('close-confirm-dialog').show();");
    } else {
      navigateToAiManagement();
    }
  }

  public void navigateToAiManagement() {
    AiNavigator.navigateToAIManagement();
  }

  public List<EmbeddingDocumentStatus> getStatusTypes() {
    if (CollectionUtils.isEmpty(statusTypes)) {
      statusTypes = Arrays.asList(EmbeddingDocumentStatus.values());
    }
    return statusTypes;
  }

  public Boolean getIsIndexActive() {
    if (isIndexActive == null) {
      String result = knowledgeBase.getIndexStatus();
      if (StringUtils.isBlank(result)) {
        isIndexActive = true;
      }
      else {
        showError(result);
        isIndexActive = false;
      }
    }
    return isIndexActive;
  }

  public void setIsIndexActive(Boolean isIndexActive) {
    this.isIndexActive = isIndexActive;
  }
}
