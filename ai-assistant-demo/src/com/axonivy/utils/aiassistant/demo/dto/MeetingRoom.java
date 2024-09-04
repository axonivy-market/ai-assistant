package com.axonivy.utils.aiassistant.demo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MeetingRoom {
  private String roomName;
  private String roomId;
  private int floor;
  private Boolean isAvailable;
  private List<Meeting> scheduledMeetings;

  // Getters and Setters
  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public Boolean isAvailable() {
    return isAvailable;
  }

  public void setAvailable(Boolean available) {
    isAvailable = available;
  }

  public List<Meeting> getScheduledMeetings() {
    return scheduledMeetings;
  }

  public void setScheduledMeetings(List<Meeting> scheduledMeetings) {
    this.scheduledMeetings = scheduledMeetings;
  }

  public int getFloor() {
    return floor;
  }

  public void setFloor(int floor) {
    this.floor = floor;
  }

  // Method to check if the room is available at a specific time
  public boolean checkAvailability(String dateTime) {
    for (Meeting meeting : scheduledMeetings) {
      if (meeting.getDateTime().equals(dateTime)) {
        return false;
      }
    }
    return true;
  }

  // Method to add a meeting to the schedule
  public boolean scheduleMeeting(Meeting meeting) {
    if (checkAvailability(meeting.getDateTime())) {
      scheduledMeetings.add(meeting);
      return true;
    } else {
      return false;
    }
  }

  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public static final String DATA = "[{\"roomId\":\"room_a\",\"roomName\":\"Room A\",\"floor\":1,\"isAvailable\":true},{\"roomId\":\"room_b\",\"roomName\":\"Room B\",\"floor\":2,\"isAvailable\":true},{\"roomId\":\"room_c\",\"roomName\":\"Room C\",\"floor\":3,\"isAvailable\":true},{\"roomId\":\"room_d\",\"roomName\":\"Room D\",\"floor\":4,\"isAvailable\":true},{\"roomId\":\"room_e\",\"roomName\":\"Room E\",\"floor\":5,\"isAvailable\":true}]";
}
