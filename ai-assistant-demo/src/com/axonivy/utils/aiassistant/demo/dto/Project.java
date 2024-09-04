package com.axonivy.utils.aiassistant.demo.dto;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Project {
  private String projectId;
  private String projectName;
  private String description;
  private Date startDate;
  private Date endDate;
  private List<Employee> teamMembers;
  private List<String> techStack;

  public void initProject(String projectName, String description,
      Date startDate,
      List<Employee> teamMembers,
      List<String> techStack) {
    this.projectName = projectName;
    this.description = description;
    this.startDate = startDate;
    this.teamMembers = teamMembers;
    this.techStack = techStack;
    this.projectId = UUID.randomUUID().toString();
  }

  // Getters and Setters
  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public List<Employee> getTeamMembers() {
    return teamMembers;
  }

  public void setTeamMembers(List<Employee> teamMembers) {
    this.teamMembers = teamMembers;
  }

  public List<String> getTechStack() {
    return techStack;
  }

  public void setTechStack(List<String> techStack) {
    this.techStack = techStack;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }
}
