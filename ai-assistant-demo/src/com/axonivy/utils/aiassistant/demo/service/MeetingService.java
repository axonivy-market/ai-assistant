package com.axonivy.utils.aiassistant.demo.service;

import java.util.ArrayList;
import java.util.UUID;

import com.axonivy.utils.aiassistant.demo.dto.Meeting;
import com.axonivy.utils.aiassistant.demo.dto.MeetingRoom;
import com.axonivy.utils.aiassistant.demo.dto.Project;

public class MeetingService extends BusinessDataService<Meeting> {

  @Override
  public Class<Meeting> getType() {
    return Meeting.class;
  }

  public String createProjectMeeting(String title, String agenda,
      String meetingRoomId, String projectId,
      String host, String time) {
    ProjectService projectService = new ProjectService();
    Project targetProject = projectService.findAll().stream()
        .filter(project -> project.getProjectId().contentEquals(projectId))
        .findFirst().get();

    MeetingRoomService meetingRoomService = new MeetingRoomService();
    MeetingRoom targetRoom = meetingRoomService.findAll().stream()
        .filter(room -> room.getRoomId().contentEquals(meetingRoomId))
        .findFirst().get();

    Meeting meeting = new Meeting();
    meeting.setMeetingId(UUID.randomUUID().toString());
    meeting.setDateTime(time);
    meeting.setHostUsername(host);
    meeting.setAgenda(agenda);
    meeting.setTitle(title);
    meeting.setRoomId(targetRoom.getRoomId());

    meeting.setParticipants(new ArrayList<>());
    for (var member : targetProject.getTeamMembers()) {
      meeting.getParticipants().add(member.getUsername());
    }
    save(meeting);
    return meeting.getMeetingId();
  }

  public Meeting findByMeetingId(String id) {
    return findAll().stream()
        .filter(meeting -> meeting.getMeetingId().contentEquals(id)).findFirst()
        .orElse(null);
  }
}