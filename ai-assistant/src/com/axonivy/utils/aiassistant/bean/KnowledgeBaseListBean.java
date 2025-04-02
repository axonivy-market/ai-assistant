package com.axonivy.utils.aiassistant.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.aiassistant.constant.AiConstants;
import com.axonivy.utils.aiassistant.dto.AiModel;
import com.axonivy.utils.aiassistant.dto.knowledgebase.KnowledgeBase;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.navigation.AiNavigator;
import com.axonivy.utils.aiassistant.service.AiFunctionService;
import com.axonivy.utils.aiassistant.service.AiModelService;
import com.axonivy.utils.aiassistant.service.KnowledgeBaseService;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class KnowledgeBaseListBean implements Serializable {

  private static final long serialVersionUID = 6797999548881499812L;

  private static final String MESSAGE_COMPONENT_ID = "ai-management-tab-view:knowledge-base-list:knowledge-base-list-form:knowledge-base-list-messages";

  private List<KnowledgeBase> knowledgeBases;
  private KnowledgeBase selectedKnowledgeBase;
  private String knowledgeBaseDetailsDialogHeader;
  private boolean isCreation;
  private List<AiModel> models;

  public void init() {
    knowledgeBases = KnowledgeBaseService.getInstance().getPublicConfig();
    knowledgeBases.forEach(knowledgeBase -> knowledgeBase.initModel());
    setModels(AiModelService.getInstance().findAll());

    checkAndShowReachedMaxAmountMessage();
  }

  /**
   * Check and show warning message if the maximum amount of knowledge bases is
   * reached.
   * 
   */
  private void checkAndShowReachedMaxAmountMessage() {
    if (isReachedMaxAmount()) {
      String message = Ivy.cms().co(
          "/Dialogs/com/axonivy/utils/aiassistant/validation/MaximumKnowledgeBasesReachedMessage");
      FacesContext.getCurrentInstance().addMessage(MESSAGE_COMPONENT_ID,
          new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
    }
  }

  public void navigateToKnowledgeBaseConfiguration(String knowledgeBaseId) {
    AiNavigator.navigateToKnowledgeBaseConfiguration(knowledgeBaseId);
  }

  public void createNewKnowledgeBase() {
    selectedKnowledgeBase = new KnowledgeBase();
    selectedKnowledgeBase.setIsPublic(true);
    knowledgeBaseDetailsDialogHeader = Ivy.cms().co(
        "/Dialogs/com/axonivy/utils/aiassistant/component/KnowledgeBaseList/CreateNewBase");
    isCreation = true;
  }

  public void editKnowledgeBase(KnowledgeBase knowledgeBase) {
    selectedKnowledgeBase = knowledgeBase;
    selectedKnowledgeBase.setIsPublic(true);
    knowledgeBaseDetailsDialogHeader = Ivy.cms().co(
        "/Dialogs/com/axonivy/utils/aiassistant/component/KnowledgeBaseList/EditKnowledge");
    isCreation = false;
  }

  public void onDeleteKnowledgeBase(KnowledgeBase knowledgeBase) {
    selectedKnowledgeBase = knowledgeBase;
    selectedKnowledgeBase.setIsPublic(true);
    isCreation = false;
  }

  public void deleteKnowledgeBase() {
    selectedKnowledgeBase.deleteIndex();
    KnowledgeBaseService.getInstance().delete(selectedKnowledgeBase.getId());
    knowledgeBases = KnowledgeBaseService.getInstance().findAll();
    knowledgeBases.forEach(knowledgeBase -> knowledgeBase.initModel());
  }

  public void onCreateKnowledgeBase() {
    KnowledgeBaseService.getInstance().save(selectedKnowledgeBase);
    knowledgeBases = KnowledgeBaseService.getInstance().getPublicConfig();
    knowledgeBases.forEach(knowledgeBase -> knowledgeBase.initModel());
    selectedKnowledgeBase.createIndex();
    checkAndShowReachedMaxAmountMessage();
  }

  public void updateKnowledgeBase() {
    KnowledgeBaseService.getInstance().save(selectedKnowledgeBase);
    knowledgeBases = KnowledgeBaseService.getInstance().getPublicConfig();
    knowledgeBases.forEach(knowledgeBase -> knowledgeBase.initModel());
  }

  public void onSelectModel() {
    selectedKnowledgeBase.setAiModel(AiModelService.getInstance()
        .findByName(selectedKnowledgeBase.getAiModelName()));
    selectedKnowledgeBase.setEmbeddingModelName(
        selectedKnowledgeBase.getAiModel().getEmbeddingModel());
  }

  public boolean isIndexActive() {
    return StringUtils
        .isBlank(selectedKnowledgeBase.getEmbeddingStore().getIndexStatus());
  }

  public String getRemoveMessage() {
    if (Optional.ofNullable(selectedKnowledgeBase).map(KnowledgeBase::getId)
        .isEmpty()) {
      return StringUtils.EMPTY;
    }

    List<AiFunction> functions = AiFunctionService.getInstance()
        .findAiFunctionByKnowledgeBaseId(selectedKnowledgeBase.getId());

    if (CollectionUtils.isEmpty(functions)) {
      return Ivy.cms().co(
          "/Dialogs/com/axonivy/utils/aiassistant/component/KnowledgeBaseList/RemoveKnowledgeBaseMessage");
    }

    String result = String.join(AiConstants.COMMA,
        functions.stream().map(AiFunction::getName).toList());

    return Ivy.cms().co(
        "/Dialogs/com/axonivy/utils/aiassistant/component/KnowledgeBaseList/RemoveKnowledgeBaseUsingByMessage",
        Arrays.asList(result));
  }

  public List<AiModel> getModels() {
    return models;
  }

  public void setModels(List<AiModel> models) {
    this.models = models;
  }

  public String getKnowledgeBaseDetailsDialogHeader() {
    return knowledgeBaseDetailsDialogHeader;
  }

  public List<KnowledgeBase> getKnowledgeBases() {
    return knowledgeBases;
  }

  public void setKnowledgeBases(List<KnowledgeBase> knowledgeBases) {
    this.knowledgeBases = knowledgeBases;
  }

  public KnowledgeBase getSelectedKnowledgeBase() {
    return selectedKnowledgeBase;
  }

  public void setSelectedKnowledgeBase(KnowledgeBase selectedKnowledgeBase) {
    this.selectedKnowledgeBase = selectedKnowledgeBase;
  }

  public boolean isCreation() {
    return isCreation;
  }

  public void setCreation(boolean isCreation) {
    this.isCreation = isCreation;
  }

  public boolean isReachedMaxAmount() {
    return Optional.ofNullable(knowledgeBases)
        .orElseGet(() -> new ArrayList<>())
        .size() >= AiConstants.MAX_KNOWLEDGE_BASES;
  }
}
