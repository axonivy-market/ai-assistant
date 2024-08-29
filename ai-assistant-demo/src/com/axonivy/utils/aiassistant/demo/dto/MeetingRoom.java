package com.axonivy.utils.aiassistant.demo.dto;

import java.util.List;

public class MeetingRoom {
  private String roomName;
  private int floor;
  private boolean isAvailable;
  private List<Meeting> scheduledMeetings;

  // Getters and Setters
  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public boolean isAvailable() {
    return isAvailable;
  }

  public void setAvailable(boolean available) {
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

  public static final String DATA = "[{\"roomName\":\"Room A\",\"floor\":1,\"isAvailable\":true},{\"roomName\":\"Room B\",\"floor\":2,\"isAvailable\":true},{\"roomName\":\"Room C\",\"floor\":3,\"isAvailable\":true},{\"roomName\":\"Room D\",\"floor\":4,\"isAvailable\":true},{\"roomName\":\"Room E\",\"floor\":5,\"isAvailable\":true}]";
}
