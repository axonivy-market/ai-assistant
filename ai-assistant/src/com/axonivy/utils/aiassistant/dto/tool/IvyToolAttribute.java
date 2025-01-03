package com.axonivy.utils.aiassistant.dto.tool;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class IvyToolAttribute implements Serializable {

  private static final long serialVersionUID = -2291917200620175108L;

  private String name;
  private String value;
  private String description;
  private String isImportant;

  public IvyToolAttribute() {
  }

  public IvyToolAttribute(String name, String value, String description) {
    this.name = name;
    this.value = value;
    this.description = description;
  }

  public IvyToolAttribute(String name, String description) {
    this.name = name;
    this.description = description;
    this.value = StringUtils.EMPTY;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getIsImportant() {
    return isImportant;
  }

  public void setIsImportant(String isImportant) {
    this.isImportant = isImportant;
  }
}