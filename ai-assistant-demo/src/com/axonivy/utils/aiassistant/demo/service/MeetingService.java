package com.axonivy.utils.aiassistant.demo.service;

import com.axonivy.utils.aiassistant.demo.dto.Meeting;

public class MeetingService extends BusinessDataService<Meeting> {

  @Override
  public Class<Meeting> getType() {
    return Meeting.class;
  }
}