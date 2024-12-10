package com.axonivy.utils.aiassistant.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import ch.ivyteam.ivy.process.call.SubProcessCallStart;
import ch.ivyteam.ivy.process.call.SubProcessSearchFilter;
import ch.ivyteam.ivy.process.call.SubProcessSearchFilter.SearchScope;
import ch.ivyteam.ivy.security.exec.Sudo;

public class AiFunctionUtils {
    protected static final String RESULT_PREFIX = "<<";
    protected static final String RESULT_POSTFIX = ">>";

    private static final Pattern STANDARD_RESULT_PATTERN = Pattern.compile("<<(.*?)>>");
  /**
   * Check if an Ivy tool is existing in the security context
   * 
   * @param signature signature of the Ivy tool
   * @return
   */
  public static Boolean checkIvyToolInSecurityContext(String signature) {
    return Sudo.get(() -> {
      var filter = SubProcessSearchFilter.create()
          .setSearchScope(SearchScope.SECURITY_CONTEXT)
          .setSignature(signature).toFilter();

      var subProcessStartList = SubProcessCallStart.find(filter);
      return CollectionUtils.isNotEmpty(subProcessStartList);
    });
  }

  public static String extractTextInsideTag(String text) {
    String tagPattern = "<([^>]+)>"; // Regex pattern to match characters inside
                                     // <>
    Pattern pattern = Pattern.compile(tagPattern);
    Matcher matcher = pattern.matcher(text);

    if (matcher.find()) {
      return matcher.group(1); // Return the first captured group
    }
    return StringUtils.EMPTY;
  }

  public static String extractJsonArray(String text) {
    String tagPattern = "\\[([^\\]]+)]"; // Regex pattern to match characters
                                         // inside []
    Pattern pattern = Pattern.compile(tagPattern);
    Matcher matcher = pattern.matcher(text);

    if (matcher.find()) {
      return "[" + matcher.group(1) + "]"; // Return the first captured group
                                           // inside array characters
    }
    return StringUtils.EMPTY;
  }
  
  public static String extractTextInsideDoubleTag(String result) {
      Matcher matcher = STANDARD_RESULT_PATTERN.matcher(result);

      if (matcher.find()) {
          return matcher.group(1); // Extracted data between << and >>
      }

      // If the result has << but doesn't has >>, remove <<
      if (result.startsWith(RESULT_PREFIX) && !result.endsWith(RESULT_POSTFIX)) {
          result = result.replaceFirst(RESULT_PREFIX, "").strip();
      }
      
      return result;
  }
}
