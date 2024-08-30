package com.axonivy.utils.aiassistant.dto.flow;

import java.util.List;
import java.util.Map;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.StepType;
import com.axonivy.utils.aiassistant.service.AiFunctionService;

public class TriggerFlowStep extends AiStep {

  private static final long serialVersionUID = 7837813829785223781L;

  private String flowId;
  private AiFunction function;
  private String triggerMessage;

  @Override
  public StepType getType() {
    return StepType.TRIGGER_FLOW;
  }

  @Override
  public void run(String message, List<ChatMessage> memory,
      Map<String, String> metadatas, Assistant assistant) {
    setResult(new AiResultDTO());
    function = AiFunctionService.getInstance().findById(flowId);
    if (function == null) {
      getResult().setState(AIState.ERROR);
      getResult().setResultForAI(
          String.format("Error when trigger AI Function %s", flowId));
    }
    getResult().setState(AIState.TRIGGER);
    getResult().setResultForAI(triggerMessage);
    getResult().setResult(triggerMessage);
  }

  public String getFlowId() {
    return flowId;
  }

  public void setFlowId(String flowId) {
    this.flowId = flowId;
  }

  public AiFunction getFunction() {
    return function;
  }

  public void setFunction(AiFunction function) {
    this.function = function;
  }

  public String getTriggerMessage() {
    return triggerMessage;
  }

  public void setTriggerMessage(String triggerMessage) {
    this.triggerMessage = triggerMessage;
  }

}
