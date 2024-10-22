package com.axonivy.utils.aiassistant.bean;

import static com.axonivy.utils.aiassistant.enums.SessionAttribute.SELECTED_ASSISTANT_ID;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.Suggestion;
import com.axonivy.utils.aiassistant.dto.history.Conversation;
import com.axonivy.utils.aiassistant.enums.AiVariable;
import com.axonivy.utils.aiassistant.history.ChatMessageManager;
import com.axonivy.utils.aiassistant.navigation.AiNavigator;
import com.axonivy.utils.aiassistant.service.AssistantService;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class AssistantBean implements Serializable {

  private static final long serialVersionUID = 1683098437048122830L;

  private Assistant assistant;
  private String assistantId;
  private List<Assistant> availableAssistants;
  private String conversationId;
  private List<Suggestion> suggestions;

  @PostConstruct
  public void initBean() {
    availableAssistants =
        AssistantService.getInstance().findAvailableAssistantForUser(Ivy.session().getSessionUserName());
    if (assistant == null) {
      String selectedAssistantId = (String) Ivy.session().getAttribute(SELECTED_ASSISTANT_ID.name());
      if (selectedAssistantId != null) {
        assistantId = selectedAssistantId;
        assistant = availableAssistants.stream().filter(assistant -> selectedAssistantId.equals(assistant.getId()))
            .findFirst().orElse(null);
      }
      if (assistant == null) {
        assistant = availableAssistants.get(0);
      }
      assistant.initModel();
      assistant.initToolkit();
    }

    this.conversationId = Conversation.generateConversationId(Ivy.session().getSessionUserName(), assistant.getId());

    this.suggestions =
        BusinessEntityConverter.jsonValueToEntities(Ivy.var().get(AiVariable.SUGGESTIONS.key), Suggestion.class);
  }

  public Assistant getAssistant() {
    return assistant;
  }

  public void setAssistant(Assistant assistant) {
    this.assistant = assistant;
  }

  public List<Suggestion> getSuggestions() {
    return suggestions;
  }

  public void setSuggestions(List<Suggestion> suggestions) {
    this.suggestions = suggestions;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public void clearHistory() throws IOException {
    ChatMessageManager.clearConversation(assistant.getId(), conversationId);
    ChatMessageManager.loadConversation(assistant.getId(), conversationId);
  }

  public void navigateToAIManagement() {
    AiNavigator.navigateToAIManagement();
  }

  public boolean canChangeAssistant() {
    return CollectionUtils.isNotEmpty(availableAssistants) && availableAssistants.size() > 1;
  }

  public void chooseAssistant(String id) {
    assistantId = id;
    assistant = availableAssistants.stream().filter(a -> a.getId().contentEquals(assistantId)).findFirst().orElse(null);

    if (assistant != null) {
      assistant.initModel();
      assistant.initToolkit();
      initBean();
      Ivy.session().setAttribute(SELECTED_ASSISTANT_ID.name(), assistantId);
    }
  }

  public List<Assistant> getAvailableAssistants() {
    return availableAssistants;
  }

  public void setAvailableAssistants(List<Assistant> availableAssistants) {
    this.availableAssistants = availableAssistants;
  }

  public String getAssistantId() {
    return assistantId;
  }

  public void setAssistantId(String assistantId) {
    this.assistantId = assistantId;
  }

  public String getDefaultLogoName() {
    return Assistant.DEFAULT_AVATAR_URI;
  }
}
