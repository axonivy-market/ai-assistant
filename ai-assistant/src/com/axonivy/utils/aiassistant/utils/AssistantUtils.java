package com.axonivy.utils.aiassistant.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import com.axonivy.portal.components.service.impl.ProcessService;
import com.axonivy.utils.aiassistant.constant.AiConstants;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.enums.IvyToolResultType;
import com.axonivy.utils.aiassistant.service.DateTimeGlobalSettingService;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.cm.ContentObject;
import ch.ivyteam.ivy.cm.ContentObjectValue;
import ch.ivyteam.ivy.cm.exec.ContentManagement;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.workflow.start.IWebStartable;
import dev.langchain4j.model.input.PromptTemplate;

public class AssistantUtils {
  public static final String IMAGE_DIRECTORY = "com/axonivy/utils/aiassistant/Assistant";
  public static final String DEFAULT_LOCALE_TAG = "en";

  public static String generateLinkToIvyProcess(String link,
      Map<String, String> params) {
    try {
      IWebStartable process = initWebStartable(link);
      return IvyToolResultType.IFRAME
          .format(process.getLink().queryParams(params).getRelative());
    } catch (Exception e) {
      return IvyToolResultType.ERROR
          .format(Ivy.cms().co("/Labels/AI/Error/ErrorWhenProceedRequest"));
    }
  }

  private static IWebStartable initWebStartable(String processPath) {
    if (StringUtils.isBlank(processPath)) {
      return null;
    }

    return ProcessService.getInstance().findAllProcesses().stream()
        .filter(process -> process.getId().contentEquals(processPath))
        .findFirst().orElse(null);
  }

  public static String generateErrorResult(String error) {
    if (StringUtils.isBlank(error)) {
      return StringUtils.EMPTY;
    }
    return IvyToolResultType.ERROR.format(error);
  }

  public static String applyAssistantInfoToPromptTemplate(Assistant assistant,
      String promptTemplate) {
    if (assistant == null) {
      return StringUtils.EMPTY;
    }
    Map<String, Object> params = new HashMap<>();
    params.put(AiConstants.INFO, assistant.getInfo());
    return PromptTemplate.from(promptTemplate).apply(params).text();
  }

  public static Pair<String, String> handleImageUpload(FileUploadEvent event) {
    UploadedFile file = event.getFile();
    if (file != null && file.getContent() != null
        && file.getContent().length > 0 && file.getFileName() != null) {
      // save the image
      String fileName = UUID.randomUUID().toString();
      String fileExtension = FilenameUtils.getExtension(file.getFileName());

      ContentObject imageCMSObject = getApplicationCMS().child()
          .folder(IMAGE_DIRECTORY).child().file(fileName, fileExtension);

      if (imageCMSObject != null) {
        readObjectValueOfDefaultLocale(imageCMSObject).write()
            .bytes(file.getContent());
        return Pair.of(imageCMSObject.uri(), fileExtension);
      }
    }
    return Pair.of(StringUtils.EMPTY, StringUtils.EMPTY);
  }

  private static ContentObjectValue readObjectValueOfDefaultLocale(
      ContentObject contentObject) {
    if (contentObject == null) {
      return null;
    }
    return contentObject.value().get(DEFAULT_LOCALE_TAG);
  }

  private static ContentObject getApplicationCMS() {
    return ContentManagement.cms(IApplication.current()).root();
  }

  public static void removeImage(String imageUrl, String imageType) {
    ContentObject imageObject = getApplicationCMS().child().file(imageUrl,
        imageType);
    if (imageObject != null) {
      imageObject.delete();
    }
  }

  public static Map<String, String> getMetadatas() {
    Map<String, String> metadatas = new HashMap<>();
    metadatas.put("date time pattern",
        DateTimeGlobalSettingService.getInstance().getDefaultDatePattern());
    metadatas.put("today", DateTimeGlobalSettingService.getInstance()
        .getDefaultDateFormatter().format(new Date()));
    metadatas.put("username", Ivy.session().getSessionUserName());
    return metadatas;
  }

  public static String getDefaultLanguage() {
    Locale locale = Ivy.session().getContentLocale();
    if (locale == null) {
      return Locale.ENGLISH.getDisplayLanguage();
    }

    return StringUtils.defaultIfBlank(locale.getDisplayLanguage(),
        locale.toLanguageTag());
  }
}