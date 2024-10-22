package com.axonivy.utils.aiassistant.demo.enums;

public enum Ranking {

  JUNIOR("Junior"), PROFESSIONAL("Professional"), SENIOR("Senior");

  private String rank;

  private Ranking(String rank) {
    this.rank = rank;
  }

  public String getRank() {
    return rank;
  }

  public void setRank(String rank) {
    this.rank = rank;
  }
}
