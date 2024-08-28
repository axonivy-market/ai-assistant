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

  public void handleUpload(FileUploadEvent event) throws IOException {
    UploadedFile file = event.getFile();
    if (file != null && file.getContent() != null
        && file.getContent().length > 0 && file.getFileName() != null) {

      AbstractAIBot bot = BotUtils.getBot();

      String indexName = FilenameUtils.removeExtension(file.getFileName());
      byte[] buffer = new byte[1024];
      ZipInputStream fileStream = new ZipInputStream(file.getInputStream());
      ZipEntry zipEntry = fileStream.getNextEntry();

      List<String> result = new ArrayList<>();
      boolean isTextFiles = false;
      while (zipEntry != null) {
        String fileName = zipEntry.getName();
        if (fileName.endsWith(".html")) {
          // Get data from the XHTML file
          String fileContent = extractFileContent(buffer, fileStream);

          // Convert HTML content to readable content
          // Then filter out empty lines
          result.add(PortalDocService.convertPortalDocument(fileContent));
          result = result.stream().filter(data -> StringUtils.isNotBlank(data))
              .collect(Collectors.toList());
        } else if (fileName.endsWith(".txt")) {
          isTextFiles = true;
          result.add(extractFileContent(buffer, fileStream));
        }
        zipEntry = fileStream.getNextEntry();
      }

      // Embed converted contents
      if (isTextFiles) {
        PortalDocService.createTextIndex(bot, indexName, result);
      } else {
        PortalDocService.createPortalIndex(bot, indexName, result);
      }
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