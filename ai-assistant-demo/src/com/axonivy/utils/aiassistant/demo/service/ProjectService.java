package com.axonivy.utils.aiassistant.demo.service;

import com.axonivy.utils.aiassistant.demo.dto.Project;

public class ProjectService extends BusinessDataService<Project> {

  @Override
  public Class<Project> getType() {
    return Project.class;
  }
}