package com.axonivy.utils.aiassistant.enums;

public enum DefaultEthicalRule {
  PRIVACY("Keep user data confidential by protecting it securely."),
  TRANSPARENCY(
      "Be transparent by clearly stating you're an AI and providing accurate information."),
  FAIR("Treat everyone fairly by ensuring equal treatment and avoiding bias.");

  private String rule;

  private DefaultEthicalRule(String rule) {
    this.setRule(rule);
  }

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }
}