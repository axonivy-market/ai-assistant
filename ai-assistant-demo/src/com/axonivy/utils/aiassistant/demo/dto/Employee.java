package com.axonivy.utils.aiassistant.demo.dto;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.axonivy.utils.aiassistant.demo.enums.Ranking;
import com.axonivy.utils.aiassistant.demo.enums.Role;

public class Employee {
  private String username;
  private String displayName;
  private Role role;
  private Ranking rank;
  private List<String> techStack;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Ranking getRank() {
    return rank;
  }

  public void setRank(Ranking rank) {
    this.rank = rank;
  }

  public List<String> getTechStack() {
    return techStack;
  }

  public void setTechStack(List<String> techStack) {
    this.techStack = techStack;
  }

  public String getTechStackString() {
    if (CollectionUtils.isEmpty(techStack)) {
      return "";
    }
    return String.join(",", this.techStack);
  }

  /**
   * Data for demo
   * SOFTWARE_ENGINEER: SENIOR (10) JUNIOR (15) PROFESSIONAL (10)
   * TESTER: SENIOR (4) JUNIOR (5) PROFESSIONAL (1)
   * WEB_DESIGNER: SENIOR (2) JUNIOR (2) PROFESSIONAL (1)
   * 
   * Technologies: 
   * Spring JSF (JavaServer Faces)
   * HTML
   * CSS
   * JQuery
   * Jax-rs (Java API for RESTful Web Services)
   * Thymeleaf
   * Hibernate
   * Servlets
   * JSP (JavaServer Pages)
   */
  public static final String DATA = "[{\"username\":\"user_001\",\"displayName\":\"Casey Davis\",\"role\":\"TESTER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Jax-rs\",\"Thymeleaf\"]},{\"username\":\"user_002\",\"displayName\":\"Morgan Smith\",\"role\":\"TESTER\",\"rank\":\"SENIOR\",\"techStack\":[\"Spring\",\"Hibernate\",\"CSS\",\"JSF\"]},{\"username\":\"user_003\",\"displayName\":\"Chris Miller\",\"role\":\"TESTER\",\"rank\":\"SENIOR\",\"techStack\":[\"JQuery\",\"Servlets\"]},{\"username\":\"user_004\",\"displayName\":\"Morgan Williams\",\"role\":\"TESTER\",\"rank\":\"JUNIOR\",\"techStack\":[\"CSS\",\"Servlets\"]},{\"username\":\"user_005\",\"displayName\":\"Alex Garcia\",\"role\":\"TESTER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"Jax-rs\",\"JSF\",\"Spring\",\"Hibernate\",\"Servlets\"]},{\"username\":\"user_006\",\"displayName\":\"Morgan Johnson\",\"role\":\"TESTER\",\"rank\":\"SENIOR\",\"techStack\":[\"HTML\",\"Jax-rs\",\"JSF\",\"Thymeleaf\",\"Spring\"]},{\"username\":\"user_007\",\"displayName\":\"Alex Smith\",\"role\":\"TESTER\",\"rank\":\"JUNIOR\",\"techStack\":[\"JSP\",\"JQuery\",\"CSS\"]},{\"username\":\"user_008\",\"displayName\":\"Jamie Williams\",\"role\":\"TESTER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"JSP\",\"CSS\",\"Thymeleaf\",\"Hibernate\"]},{\"username\":\"user_009\",\"displayName\":\"Jamie Jones\",\"role\":\"TESTER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Hibernate\",\"Spring\",\"JSP\",\"Jax-rs\"]},{\"username\":\"user_010\",\"displayName\":\"Jordan Jones\",\"role\":\"TESTER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"JQuery\",\"HTML\",\"JSP\",\"JSF\",\"Jax-rs\"]},{\"username\":\"user_011\",\"displayName\":\"Jamie Brown\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"SENIOR\",\"techStack\":[\"CSS\",\"Thymeleaf\",\"JSP\",\"Servlets\",\"Jax-rs\",\"JQuery\"]},{\"username\":\"user_012\",\"displayName\":\"Chris Miller\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"SENIOR\",\"techStack\":[\"Jax-rs\",\"Hibernate\",\"JSP\",\"HTML\",\"JQuery\"]},{\"username\":\"user_013\",\"displayName\":\"Taylor Brown\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"SENIOR\",\"techStack\":[\"CSS\",\"Servlets\",\"Thymeleaf\",\"Hibernate\"]},{\"username\":\"user_014\",\"displayName\":\"Alex Johnson\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"SENIOR\",\"techStack\":[\"Jax-rs\",\"JQuery\",\"JSP\"]},{\"username\":\"user_015\",\"displayName\":\"Morgan Miller\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"SENIOR\",\"techStack\":[\"CSS\",\"HTML\",\"JQuery\",\"Hibernate\"]},{\"username\":\"user_016\",\"displayName\":\"Alex Williams\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"SENIOR\",\"techStack\":[\"JSP\",\"Servlets\",\"Hibernate\",\"Spring\",\"HTML\"]},{\"username\":\"user_017\",\"displayName\":\"Chris Miller\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"SENIOR\",\"techStack\":[\"CSS\",\"JQuery\",\"JSP\",\"Jax-rs\",\"Thymeleaf\"]},{\"username\":\"user_018\",\"displayName\":\"Jane Garcia\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"SENIOR\",\"techStack\":[\"Thymeleaf\",\"JSF\",\"Jax-rs\",\"HTML\"]},{\"username\":\"user_019\",\"displayName\":\"Chris Martinez\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"SENIOR\",\"techStack\":[\"JQuery\",\"Thymeleaf\",\"Spring\"]},{\"username\":\"user_020\",\"displayName\":\"Taylor Rodriguez\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"SENIOR\",\"techStack\":[\"Jax-rs\",\"JSF\",\"HTML\",\"JQuery\",\"Servlets\",\"Thymeleaf\"]},{\"username\":\"user_021\",\"displayName\":\"Drew Williams\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"HTML\",\"Servlets\",\"Thymeleaf\",\"JSF\"]},{\"username\":\"user_022\",\"displayName\":\"Jordan Rodriguez\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"JSF\",\"Spring\",\"Jax-rs\",\"JSP\",\"HTML\"]},{\"username\":\"user_023\",\"displayName\":\"Drew Garcia\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Hibernate\",\"JSF\",\"Spring\",\"CSS\",\"Servlets\"]},{\"username\":\"user_024\",\"displayName\":\"Jamie Davis\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Servlets\",\"HTML\",\"JSF\",\"Jax-rs\"]},{\"username\":\"user_025\",\"displayName\":\"Morgan Davis\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Thymeleaf\",\"CSS\"]},{\"username\":\"user_026\",\"displayName\":\"Chris Davis\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Servlets\",\"Spring\",\"HTML\"]},{\"username\":\"user_027\",\"displayName\":\"Casey Williams\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"JSP\",\"Hibernate\"]},{\"username\":\"user_028\",\"displayName\":\"Jane Jones\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Jax-rs\",\"Spring\",\"JQuery\",\"JSF\"]},{\"username\":\"user_029\",\"displayName\":\"Chris Williams\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Hibernate\",\"JQuery\"]},{\"username\":\"user_030\",\"displayName\":\"Alex Rodriguez\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"HTML\",\"Spring\"]},{\"username\":\"user_031\",\"displayName\":\"Drew Williams\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Thymeleaf\",\"Jax-rs\",\"JQuery\",\"HTML\",\"Spring\"]},{\"username\":\"user_032\",\"displayName\":\"Jordan Garcia\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Jax-rs\",\"Spring\",\"HTML\",\"CSS\"]},{\"username\":\"user_033\",\"displayName\":\"Jamie Rodriguez\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Servlets\",\"Jax-rs\",\"HTML\",\"Spring\"]},{\"username\":\"user_034\",\"displayName\":\"Chris Williams\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Servlets\",\"Thymeleaf\",\"HTML\",\"JQuery\"]},{\"username\":\"user_035\",\"displayName\":\"Jamie Rodriguez\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"JUNIOR\",\"techStack\":[\"HTML\",\"Jax-rs\",\"JSF\"]},{\"username\":\"user_036\",\"displayName\":\"Alex Jones\",\"role\":\"WEB_DESIGNER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Servlets\",\"Thymeleaf\",\"JSF\",\"Hibernate\",\"Jax-rs\"]},{\"username\":\"user_037\",\"displayName\":\"Jordan Davis\",\"role\":\"WEB_DESIGNER\",\"rank\":\"JUNIOR\",\"techStack\":[\"Jax-rs\",\"JQuery\"]},{\"username\":\"user_038\",\"displayName\":\"Jane Rodriguez\",\"role\":\"WEB_DESIGNER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"JQuery\",\"Spring\"]},{\"username\":\"user_039\",\"displayName\":\"Alex Williams\",\"role\":\"WEB_DESIGNER\",\"rank\":\"SENIOR\",\"techStack\":[\"JSF\",\"Jax-rs\",\"Spring\"]},{\"username\":\"user_040\",\"displayName\":\"Casey Brown\",\"role\":\"WEB_DESIGNER\",\"rank\":\"SENIOR\",\"techStack\":[\"Servlets\",\"CSS\",\"JSF\"]},{\"username\":\"user_041\",\"displayName\":\"Jane Davis\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"HTML\",\"Jax-rs\",\"CSS\",\"Spring\"]},{\"username\":\"user_042\",\"displayName\":\"Casey Jones\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"HTML\",\"Jax-rs\",\"JQuery\",\"Spring\",\"JSP\"]},{\"username\":\"user_043\",\"displayName\":\"Jamie Smith\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"JSP\",\"HTML\",\"CSS\",\"Servlets\"]},{\"username\":\"user_044\",\"displayName\":\"Casey Garcia\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"JSF\",\"HTML\",\"JSP\"]},{\"username\":\"user_045\",\"displayName\":\"Chris Miller\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"Spring\",\"Jax-rs\",\"JSP\",\"Servlets\"]},{\"username\":\"user_046\",\"displayName\":\"Jamie Williams\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"Servlets\",\"CSS\",\"HTML\",\"Hibernate\",\"JSF\"]},{\"username\":\"user_047\",\"displayName\":\"Morgan Miller\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"Spring\",\"Jax-rs\",\"Thymeleaf\",\"JSP\"]},{\"username\":\"user_048\",\"displayName\":\"Jordan Miller\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"CSS\",\"Hibernate\",\"Jax-rs\"]},{\"username\":\"user_049\",\"displayName\":\"Alex Brown\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"JSF\",\"Servlets\",\"CSS\",\"JQuery\",\"Thymeleaf\"]},{\"username\":\"user_050\",\"displayName\":\"Drew Johnson\",\"role\":\"SOFTWARE_ENGINEER\",\"rank\":\"PROFESSIONAL\",\"techStack\":[\"JQuery\",\"Hibernate\",\"JSP\",\"HTML\",\"JSF\"]}]";
}
