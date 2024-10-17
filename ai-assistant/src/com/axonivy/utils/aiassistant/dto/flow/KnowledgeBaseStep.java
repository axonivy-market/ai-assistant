package com.axonivy.utils.aiassistant.dto.flow;

import java.util.List;
import java.util.Map;

import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.tool.RetrievalQATool;
import com.axonivy.utils.aiassistant.enums.StepType;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class KnowledgeBaseStep extends AiStep {

  private static final long serialVersionUID = 4661096230639927440L;

  private String toolId;
  private String userMessage;

  @JsonIgnore
  private RetrievalQATool tool;

  @Override
  public StepType getType() {
    return StepType.KNOWLEDGE_BASE;
  }

  @Override
  public void run(String message, List<ChatMessage> memory,
      Map<String, String> metadatas, Assistant assistant) {
    
  }

  @JsonIgnore
  public RetrievalQATool getTool() {
    return tool;
  }

  @JsonIgnore
  public void setTool(RetrievalQATool tool) {
    this.tool = tool;
  }

  public String getToolId() {
    return toolId;
  }

  public void setToolId(String toolId) {
    this.toolId = toolId;
  }

  public String getUserMessage() {
    return userMessage;
  }

  public void setUserMessage(String userMessage) {
    this.userMessage = userMessage;
  }

}
