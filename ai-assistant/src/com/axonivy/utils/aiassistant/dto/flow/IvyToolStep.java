package com.axonivy.utils.aiassistant.dto.flow;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.tool.IvyTool;
import com.axonivy.utils.aiassistant.enums.StepType;
import com.axonivy.utils.aiassistant.service.AiFunctionService;
import com.fasterxml.jackson.core.JsonProcessingException;

public class IvyToolStep extends AiStep {

  private static final long serialVersionUID = 3979398095155133272L;

  private String toolId;
  private String systemMessage;

  @Override
  public StepType getType() {
    return StepType.IVY_TOOL;
  }

  public String getToolId() {
    return toolId;
  }

  public void setToolId(String toolId) {
    this.toolId = toolId;
  }

  @Override
  public void run(String message, List<ChatMessage> memory,
      Map<String, String> metadatas, Assistant assistant) {
    if (StringUtils.isNotBlank(systemMessage)) {
      memory.add(ChatMessage.newSystemMessage(systemMessage));
    }
    IvyTool tool = (IvyTool) AiFunctionService.getInstance()
        .findById(this.toolId);
    try {
      tool = tool.fullfilIvyTool(memory,
          assistant.getAiModel().getAiBot(), getFormattedMetadatas(metadatas));
      setResult(tool.getResult());
    } catch (JsonProcessingException e) {
      setResult(createSomethingWentWrongError());
    }
  }

  public String getSystemMessage() {
    return systemMessage;
  }

  public void setSystemMessage(String systemMessage) {
    this.systemMessage = systemMessage;
  }
}