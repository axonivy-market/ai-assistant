package com.axonivy.utils.aiassistant.navigation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.axonivy.portal.components.generic.navigation.BaseNavigator;
import com.axonivy.portal.components.publicapi.PortalNavigatorInFrameAPI;
import com.axonivy.portal.components.util.ProcessStartUtils;
import com.axonivy.utils.aiassistant.exception.AiChatException;

public class AiNavigator extends BaseNavigator {

  private static final String AI_MANAGEMENT_FRIENDLY_REQUEST_PATH = "Start Processes/AiStart/AiManagement.ivp";
  private static final String ASSISTANT_CONFIGURATION_FRIENDLY_REQUEST_PATH = "Start Processes/AiStart/AssistantConfiguration.ivp";
  private static final String FUNCTION_CONFIGURATION_FRIENDLY_REQUEST_PATH = "Start Processes/AiStart/FunctionConfiguration.ivp";
  private static final String AI_MODEL_CONFIGURATION_FRIENDLY_REQUEST_PATH = "Start Processes/AiStart/AiModelConfiguration.ivp";
  private static final String ASSISTANT_DASHBOARD_FRIENDLY_REQUEST_PATH = "Start Processes/AiStart/AssistantDashboard.ivp";

  public static void navigateToAIManagement() {
    try {
      ProcessStartUtils.redirect(PortalNavigatorInFrameAPI
          .findAbsoluteUrlByProcessStartFriendlyRequestPath(
              AI_MANAGEMENT_FRIENDLY_REQUEST_PATH));
    } catch (IOException e) {
      throw new AiChatException(e);
    }
  }

  public static void navigateToAssistantConfiguration(String assistantId) {
    try {
      Map<String, String> params = new HashMap<>();
      params.put("assistantId", assistantId);
      ProcessStartUtils.redirect(buildAbsoluteUrl(
          ASSISTANT_CONFIGURATION_FRIENDLY_REQUEST_PATH, params));
    } catch (IOException e) {
      throw new AiChatException(e);
    }
  }

  public static void navigateToFunctionConfiguration(String functionId) {
    try {
      Map<String, String> params = new HashMap<>();
      params.put("functionId", functionId);
      ProcessStartUtils.redirect(buildAbsoluteUrl(
          FUNCTION_CONFIGURATION_FRIENDLY_REQUEST_PATH, params));
    } catch (IOException e) {
      throw new AiChatException(e);
    }
  }

  public static void navigateToAIModelConfiguration(String modelId) {
    try {
      Map<String, String> params = new HashMap<>();
      params.put("modelId", modelId);
      ProcessStartUtils.redirect(buildAbsoluteUrl(
          AI_MODEL_CONFIGURATION_FRIENDLY_REQUEST_PATH, params));
    } catch (IOException e) {
      throw new AiChatException(e);
    }
  }

  public static void navigateToAssistantDashboard() {
    try {
      ProcessStartUtils.redirect(PortalNavigatorInFrameAPI
          .findAbsoluteUrlByProcessStartFriendlyRequestPath(
              ASSISTANT_DASHBOARD_FRIENDLY_REQUEST_PATH));
    } catch (IOException e) {
      throw new AiChatException(e);
    }
  }
}
