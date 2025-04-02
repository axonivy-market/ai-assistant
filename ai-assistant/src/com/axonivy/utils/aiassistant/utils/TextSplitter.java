package com.axonivy.utils.aiassistant.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class TextSplitter {

  /**
   * Regular expression to split by new line characters includes:
   * \n (Line Feed)
   * \r (Carriage Return)
   * \r\n (Windows-style CRLF)
   * Other line break characters recognized by Unicode
   * 
   * \\s to remove unnecessary whitespace characters before and after the new line character.
   */
  private static final String NEW_LINE_REGEX = "\\s*\\R\\s*";

  private static final String WHITE_SPACE_REGEX = "\\s+";

  private static final int PARAGRAPH_MAX_LENGTH = 1000;

  private static List<String> splitByNewLine(String content) {
    if (StringUtils.isBlank(content)) {
      return null;
    }

    return Arrays.asList(content.split(NEW_LINE_REGEX));
  }

  public static List<String> splitDocumentByParagraph(String document) {
    // If the document has length less than the default max length, return it
    // instead
    if (Optional.ofNullable(document).orElse(StringUtils.EMPTY)
        .length() <= PARAGRAPH_MAX_LENGTH) {
      return Arrays.asList(document);
    }

    List<String> result = new ArrayList<>();
    List<String> paragraphs = splitByNewLine(document);
    List<Integer> paragraphLengths = paragraphs.stream().map(String::length)
        .toList();

    StringBuilder contentBuilder = new StringBuilder();

    for (int i = 0; i < paragraphs.size(); i++) {
      String paragraph = paragraphs.get(i);

      // put content to string builder
      // and add a line separator between paragraphs
      contentBuilder.append(paragraph).append(System.lineSeparator())
          .append(System.lineSeparator());

      // Get length of this paragraph and the next paragraph
      // If this is the last paragraph, just get its length
      int upcomingParagraphLength = i == paragraphs.size() - 1
          ? contentBuilder.length()
          : (contentBuilder.length() + paragraphLengths.get(i + 1));

      // If the content hit the max length limit:
      // strip the content from builder,
      // add to the result list,
      // reset the builder
      if (upcomingParagraphLength >= PARAGRAPH_MAX_LENGTH) {
        result.add(contentBuilder.toString().strip());
        contentBuilder.setLength(0);
      }
    }
    return result;
  }

  public static String[] splitByWhitespace(String content) {
    return content.trim().split(WHITE_SPACE_REGEX);
  }
}
