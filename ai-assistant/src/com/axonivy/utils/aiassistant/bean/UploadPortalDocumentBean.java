package com.axonivy.utils.aiassistant.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import com.axonivy.utils.aiassistant.core.AbstractAIBot;
import com.axonivy.utils.aiassistant.service.PortalDocService;
import com.axonivy.utils.aiassistant.utils.BotUtils;

@ManagedBean
@ViewScoped
public class UploadPortalDocumentBean {

  private static final String PORTAL_USER_GUIDE = "portal-user-guide";

  public boolean handlePortalDocumentUpload(FileUploadEvent event)
      throws IOException {

    UploadedFile file = event.getFile();
    if (file != null && file.getContent() != null
        && file.getContent().length > 0 && file.getFileName() != null
        && file.getFileName().endsWith(".zip")) {

      AbstractAIBot bot = BotUtils.getBot();

      byte[] buffer = new byte[1024];
      ZipInputStream fileStream = new ZipInputStream(file.getInputStream());
      ZipEntry zipEntry = fileStream.getNextEntry();

      List<String> result = new ArrayList<>();
      while (zipEntry != null) {
        String fileName = zipEntry.getName();
        // Only handle files within "portal-user-guide" folder and ending with ".html"
        if (fileName.startsWith(PORTAL_USER_GUIDE + "/")
            && fileName.endsWith(".html")) {
          // Get data from the XHTML file
          String fileContent = extractFileContent(buffer, fileStream);

          // Convert HTML content to readable content
          // Then filter out empty lines
          result.add(PortalDocService.convertPortalDocument(fileContent));
          result = result.stream().filter(data -> StringUtils.isNotBlank(data))
              .collect(Collectors.toList());
        }
        zipEntry = fileStream.getNextEntry();
      }

      // Embed converted contents
      PortalDocService.createPortalIndex(bot, PORTAL_USER_GUIDE, result);

      return true;
    }
    return false;
  }

  public void handleUpload(FileUploadEvent event) throws IOException {
    UploadedFile file = event.getFile();
    if (file != null && file.getContent() != null
        && file.getContent().length > 0 && file.getFileName() != null) {

      String indexName = FilenameUtils.removeExtension(file.getFileName());

      AbstractAIBot bot = BotUtils.getBot();

      byte[] buffer = new byte[1024];
      ZipInputStream fileStream = new ZipInputStream(file.getInputStream());
      ZipEntry zipEntry = fileStream.getNextEntry();

      List<String> result = new ArrayList<>();
      while (zipEntry != null) {
        String fileName = zipEntry.getName();
        if (fileName.endsWith(".txt")) {
          result.add(extractFileContent(buffer, fileStream));
        }
        zipEntry = fileStream.getNextEntry();
      }

      // Embed converted contents
      PortalDocService.createTextIndex(bot, indexName, result);
    }
  }

  private String extractFileContent(byte[] buffer, ZipInputStream fileStream)
      throws IOException {
    ByteArrayOutputStream docContentStream = new ByteArrayOutputStream();
    int len;
    while ((len = fileStream.read(buffer)) > 0) {
      docContentStream.write(buffer, 0, len);
    }

    String fileContent = new String(docContentStream.toByteArray(),
        StandardCharsets.UTF_8);
    return fileContent;
  }
}