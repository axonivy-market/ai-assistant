package com.axonivy.utils.aiassistant.demo.dto;

import com.axonivy.utils.aiassistant.demo.enums.ReportType;

public class ReportTicket {
  private String ticketId;
  private String reporterUsername;
  private String content;
  private ReportType type;

  public String getTicketId() {
    return ticketId;
  }
  public void setTicketId(String ticketId) {
    this.ticketId = ticketId;
  }

  public String getReporterUsername() {
    return reporterUsername;
  }

  public void setReporterUsername(String reporterUsername) {
    this.reporterUsername = reporterUsername;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public ReportType getType() {
    return type;
  }

  public void setType(ReportType type) {
    this.type = type;
  }
}
