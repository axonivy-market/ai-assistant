package com.axonivy.utils.aiassistant.utils;

import org.apache.commons.collections4.CollectionUtils;

import ch.ivyteam.ivy.process.call.SubProcessCallStart;
import ch.ivyteam.ivy.process.call.SubProcessSearchFilter;
import ch.ivyteam.ivy.process.call.SubProcessSearchFilter.SearchScope;
import ch.ivyteam.ivy.security.exec.Sudo;

public class AiFunctionUtils {

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
      if (CollectionUtils.isEmpty(subProcessStartList)) {
        return false;
      }
      return true;
    });
  }
}
