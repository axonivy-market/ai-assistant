package com.axonivy.utils.aiassistant.enums;

import ch.ivyteam.ivy.environment.Ivy;

public enum UploadDocumentType {
  PORTAL_SUPPORT, OTHERS;

  public String getLabel() {
    return Ivy.cms()
        .co(String.format("/Labels/Enums/UploadDocumentType/%s", this.name()));
  }
}
