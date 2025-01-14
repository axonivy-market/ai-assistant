package com.axonivy.utils.aiassistant.demo.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.axonivy.utils.aiassistant.demo.constant.AiConstants;
import com.axonivy.utils.aiassistant.demo.dto.Employee;
import com.axonivy.utils.aiassistant.demo.dto.Project;

public class ProjectService extends BusinessDataService<Project> {

  @Override
  public Class<Project> getType() {
    return Project.class;
  }

  public String createProject(String projectName, String projectDescription,
      String members, String technologies) {
    EmployeeService employeeService = new EmployeeService();
    List<Employee> memberDTOs = employeeService
        .findByCriteria(
            Arrays.asList(members.split(AiConstants.COMMA)), null,
            null, null);

    Project result = new Project();
    result.initProject(projectName, projectDescription, new Date(),
        memberDTOs,
        Arrays.asList(technologies.split(AiConstants.COMMA)));
    save(result);
    return result.getProjectId();
  }

  public Project findByProjectId(String projectId) {
    return findAll().stream()
        .filter(project -> project.getProjectId().contentEquals(projectId))
        .findFirst().orElse(null);
  }
}