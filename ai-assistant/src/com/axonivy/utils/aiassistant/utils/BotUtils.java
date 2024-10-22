package com.axonivy.utils.aiassistant.utils;

import com.axonivy.utils.aiassistant.core.AbstractAIBot;
import com.axonivy.utils.aiassistant.core.OpenAIBot;
import com.axonivy.utils.aiassistant.service.AiModelService;

public class BotUtils {

  public static AbstractAIBot getBot() {
    return new OpenAIBot(AiModelService.getSecondaryOpenAIModel());
  }
}