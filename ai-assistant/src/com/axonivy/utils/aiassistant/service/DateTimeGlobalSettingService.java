package com.axonivy.utils.aiassistant.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ch.ivyteam.ivy.environment.Ivy;

public class DateTimeGlobalSettingService {
  private static DateTimeGlobalSettingService instance;

  public static DateTimeGlobalSettingService getInstance() {
    if (instance == null) {
      instance = new DateTimeGlobalSettingService();
    }
    return instance;
  }

  public DateFormat getDefaultDateTimeFormatter() {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT,
        Ivy.session().getFormattingLocale());
  }

  public DateFormat getDefaultDateFormatter() {
    return DateFormat.getDateInstance(DateFormat.MEDIUM,
        Ivy.session().getFormattingLocale());
  }

  public String getDefaultDatePattern() {
    return ((SimpleDateFormat) getDefaultDateFormatter()).toPattern();
  }

}
