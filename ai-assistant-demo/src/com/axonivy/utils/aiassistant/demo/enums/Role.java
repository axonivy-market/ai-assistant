package com.axonivy.utils.aiassistant.demo.enums;

public enum Role {
  SOFTWARE_ENGINEER("Software Engineer"), WEB_DESIGNER("Web Designer"),
  TESTER("Tester"), PROJECT_MANAGER("Project Manager");

  private Role(String roleName) {
    this.setRoleName(roleName);
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  private String roleName;
}
