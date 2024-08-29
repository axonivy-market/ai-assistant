package com.axonivy.utils.aiassistant.demo.dto;

import java.util.List;

public class Meeting {
  private String dateTime;
  private String agenda;
  private List<String> participants;
  private String roomName;

  // Constructor
  public Meeting(String dateTime, String agenda, List<String> participants) {
    this.dateTime = dateTime;
    this.agenda = agenda;
    this.participants = participants;
  }

  // Getters and Setters
  public String getDateTime() {
    return dateTime;
  }

  public void setDateTime(String dateTime) {
    this.dateTime = dateTime;
  }

  public String getAgenda() {
    return agenda;
  }

  public void setAgenda(String agenda) {
    this.agenda = agenda;
  }

  public List<String> getParticipants() {
    return participants;
  }

  public void setParticipants(List<String> participants) {
    this.participants = participants;
  }

  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }
}
