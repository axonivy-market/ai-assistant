package com.axonivy.utils.aiassistant.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AssistantTemplate extends AbstractConfiguration
    implements Serializable {

  private static final long serialVersionUID = 417441354331411674L;

  private String info;
  private String name;
  private String contactWebsite;
  private String contactEmail;
  private List<String> tools;
  private List<String> ethicalRules;
  private String description;

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getTools() {
    return tools;
  }

  public void setTools(List<String> tools) {
    this.tools = tools;
  }

  public String getContactWebsite() {
    return contactWebsite;
  }

  public void setContactWebsite(String contactWebsite) {
    this.contactWebsite = contactWebsite;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public List<String> getEthicalRules() {
    return ethicalRules;
  }

  public void setEthicalRules(List<String> ethicalRules) {
    this.ethicalRules = ethicalRules;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}