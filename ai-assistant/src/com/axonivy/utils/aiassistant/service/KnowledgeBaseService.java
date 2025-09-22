package com.axonivy.utils.aiassistant.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.constant.AiConstants;
import com.axonivy.utils.aiassistant.dto.knowledgebase.KnowledgeBase;
import com.axonivy.utils.aiassistant.enums.AiVariable;

import ch.ivyteam.ivy.environment.Ivy;

public class KnowledgeBaseService
    extends JsonConfigurationService<KnowledgeBase> {

  private static KnowledgeBaseService instance;

  public static KnowledgeBaseService getInstance() {
    if (instance == null) {
      instance = new KnowledgeBaseService();
    }
    return instance;
  }

  @Override
  public Class<KnowledgeBase> getType() {
    return KnowledgeBase.class;
  }

  @Override
  public String getConfigKey() {
    return AiVariable.KNOWLEDGE_BASES.key;
  }

  @Override
  public List<KnowledgeBase> getPublicConfig() {
    String jsonValue = Ivy.var().get(getConfigKey());
    if (StringUtils.isBlank(jsonValue)) {
      return new ArrayList<>();
    }

    List<KnowledgeBase> result = BusinessEntityConverter
        .jsonValueToEntities(jsonValue, getType());
    return result.size() > AiConstants.MAX_KNOWLEDGE_BASES
        ? result.subList(0, AiConstants.MAX_KNOWLEDGE_BASES - 1)
        : result;
  }

  public KnowledgeBase load(String id) {
    KnowledgeBase result = super.findById(id);
    try {
      result
          .setDocuments(result.getEmbeddingStore().findAllEmbeddingDocuments());
      if (CollectionUtils.isNotEmpty(result.getDocuments())) {
        result.getDocuments().forEach(doc -> doc.initDocumentId());
      }
    } catch (Exception e) {
      return result;
    }
    return result;
  }
}
