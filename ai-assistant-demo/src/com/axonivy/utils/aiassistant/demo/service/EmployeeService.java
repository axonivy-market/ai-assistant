package com.axonivy.utils.aiassistant.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.portal.components.publicapi.AiAssistantAPI;
import com.axonivy.utils.aiassistant.demo.dto.Employee;
import com.axonivy.utils.aiassistant.demo.enums.Ranking;
import com.axonivy.utils.aiassistant.demo.enums.Role;

public class EmployeeService extends BusinessDataService<Employee> {

  @Override
  public Class<Employee> getType() {
    return Employee.class;
  }

  public void createData() {
    deleteAll();
    List<Employee> data = BusinessEntityConverter
        .jsonValueToEntities(Employee.DATA, Employee.class);
    for (Employee employee : data) {
      save(employee);
    }
  }

  public List<Employee> findByCriteria(List<String> names, List<Role> roles,
      List<Ranking> ranks, List<String> techStack) {
    List<Employee> data = findAll();

    if (CollectionUtils.isNotEmpty(names)) {
      List<Employee> filteredByName = new ArrayList<>();
      for (String name : names) {
        filteredByName.addAll(data.stream()
            .filter(employee -> (employee.getUsername().toLowerCase()
                .contains(name.toLowerCase())
                || employee.getDisplayName().toLowerCase()
                    .contains(name.toLowerCase())))
            .collect(Collectors.toList()));
      }

      data = filteredByName.stream().distinct().collect(Collectors.toList());
    }

    if (CollectionUtils.isNotEmpty(ranks)) {
      List<Employee> filteredByRank = new ArrayList<>();
      for (Ranking rank : ranks) {
        filteredByRank
            .addAll(data.stream().filter(employee -> employee.getRank() == rank)
                .collect(Collectors.toList()));
      }

      data = filteredByRank.stream().distinct().collect(Collectors.toList());
    }

    if (CollectionUtils.isNotEmpty(roles)) {
      List<Employee> filteredByRoles = new ArrayList<>();
      for (Role role : roles) {
        filteredByRoles
            .addAll(data.stream().filter(employee -> employee.getRole() == role)
                .collect(Collectors.toList()));
      }

      data = filteredByRoles.stream().distinct().collect(Collectors.toList());
    }

    if (CollectionUtils.isNotEmpty(techStack)) {
      for (String tech : techStack) {
        data = data
            .stream().filter(employee -> employee.getTechStackString()
                .toLowerCase().contains(tech.toLowerCase()))
            .collect(Collectors.toList());
      }
    }

    return data;
  }

  public AiResultDTO findByAI(String names, String roles, String ranks,
      String techStack) {
    List<String> nameList = new ArrayList<>();
    if (StringUtils.isNotBlank(names)) {
      nameList = Arrays.asList(names.split(","));
    }

    List<Role> roleList = new ArrayList<>();
    if (StringUtils.isNotBlank(roles)) {
      List<String> rolesStr = Arrays.asList(roles.split(","));
      for (String childRole : rolesStr) {
        Role role = Role.valueOf(childRole.strip());

        if (role == null) {
          return AiAssistantAPI.generateErrorAiResult(
              String.format("Role %s is not valid", childRole.strip()));
        }
        roleList.add(role);
      }
    }

    List<Ranking> rankList = new ArrayList<>();
    if (StringUtils.isNotBlank(ranks)) {
      List<String> ranksStr = Arrays.asList(ranks.split(","));
      for (String childRank : ranksStr) {
        Ranking rank = Ranking.valueOf(childRank.strip());

        if (rank == null) {
          return AiAssistantAPI.generateErrorAiResult(
              String.format("Rank %s is not valid", childRank.strip()));
        }
        rankList.add(rank);
      }
    }

    List<String> techs = new ArrayList<>();
    if (StringUtils.isNotBlank(techStack)) {
      techs = Arrays.asList(techStack.split(","));
    }

    List<Employee> data = findByCriteria(nameList, roleList, rankList, techs);
    String dataJson = BusinessEntityConverter.entityToJsonValue(data);
    AiResultDTO result = new AiResultDTO();
    result.setResult(dataJson);
    result.setResultForAI(dataJson);
    result.setState(AIState.DONE);
    return result;
  }
}
