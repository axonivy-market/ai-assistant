package com.axonivy.utils.aiassistant.dto.tool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.utils.aiassistant.dto.AbstractConfiguration;
import com.axonivy.utils.aiassistant.dto.flow.AiFlow;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ch.ivyteam.ivy.environment.Ivy;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = IvyTool.class, name = "IVY"),
    @Type(value = RetrievalQATool.class, name = "RETRIEVAL_QA"),
    @Type(value = AiFlow.class, name = "FLOW") })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class AiFunction extends AbstractConfiguration
    implements Serializable {
  private static final long serialVersionUID = -4652001849527628818L;

  private String name;
  private String description;
  private String usage;
  private List<String> permissions;
  private boolean isDefault;

  @JsonIgnore
  private boolean isDisabled;

  public AiFunction() {
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

  public List<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }

  public abstract void init();

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public String getUsage() {
    return usage;
  }

  public void setUsage(String usage) {
    this.usage = usage;
  }

  public abstract ToolType getType();

  @JsonIgnore
  protected boolean hasPermision() {
    boolean hasPermission = false;
    for (String permission : getPermissions()) {

      // Check if username of login user equals to the permission.
      if (permission.startsWith("#")) {
        hasPermission = Ivy.session().getSessionUserName()
            .contentEquals(permission.substring(1));
      } else {
        // Check if the permission is existing in the role list of login user.
        hasPermission = Ivy.session().getSessionUser().getAllRoles().stream()
            .anyMatch(role -> role.getName().contentEquals(permission));
      }

      if (hasPermission) {
        break;
      }
    }
    return hasPermission;
  }

  @JsonIgnore
  protected AiResultDTO createNoPermisisonError() {
    AiResultDTO result = new AiResultDTO();
    result
        .setResult("Sorry, you don't have permission to proceed this request.");
    result.setState(AIState.ERROR);
    return result;
  }

  @JsonIgnore
  protected AiResultDTO createSomethingWentWrongError() {
    AiResultDTO result = new AiResultDTO();
    result.setResult(
        "Something went wrong when proceed your request. Please try again later.");
    result.setState(AIState.ERROR);
    return result;
  }

  @JsonIgnore
  public static String getFormattedMemory(List<ChatMessage> memory) {
    if (CollectionUtils.isEmpty(memory)) {
      return "";
    }

    String result = "";
    for (ChatMessage message : memory) {
      if (!message.isNotificationMessage()) {
        result = result.concat(message.getFormattedMessage())
            .concat(System.lineSeparator());
      }
    }

    return result;
  }

  @JsonIgnore
  public static String getFormattedMemoryForValidateMessage(
      List<ChatMessage> memory) {
    if (CollectionUtils.isEmpty(memory)) {
      return "";
    }

    String result = "";
    List<ChatMessage> checkMemory = new ArrayList<>();

    // Only check 5 latest chat messages

    for (ChatMessage message : memory) {
      if (!message.isNotificationMessage()) {
        checkMemory.add(message);
      }

      // Only check 5 latest chat messages
      if (checkMemory.size() > 5) {
        checkMemory.remove(0);
      }
    }

    for (ChatMessage message : checkMemory) {
      result = result.concat(message.getFormattedMessage().strip())
          .concat(System.lineSeparator());
    }

    return result;
  }

  @JsonIgnore
  public String generateSelectedFunctionMessage() {
    return Ivy.cms().co(
        "/Labels/Message/SelectedToolMessage/".concat(this.getType().name()),
        Arrays.asList(this.getName()));
  }

  @JsonIgnore
  public String generateFinishedFunctionMessage() {
    return Ivy.cms().co(
        "/Labels/Message/FinishedToolMessage/".concat(this.getType().name()),
        Arrays.asList(this.getName()));
  }

  public boolean isDisabled() {
    return isDisabled;
  }

  public void setDisabled(boolean isDisabled) {
    this.isDisabled = isDisabled;
  }
}