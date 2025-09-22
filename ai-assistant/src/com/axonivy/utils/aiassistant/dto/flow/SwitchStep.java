package com.axonivy.utils.aiassistant.dto.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.constant.AiConstants;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.StepType;
import com.axonivy.utils.aiassistant.prompts.AiFlowPromptTemplates;
import com.axonivy.utils.aiassistant.utils.StringProcessingUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SwitchStep extends AiStep {

  private static final long serialVersionUID = 2841031346680656698L;

  private Integer showResultOfStep;

  @JsonProperty("cases")
  private List<AICondition> conditions;

  @Override
  public StepType getType() {
    return StepType.SWITCH;
  }

  public Integer getShowResultOfStep() {
    return showResultOfStep;
  }

  public void setShowResultOfStep(Integer showResultOfStep) {
    this.showResultOfStep = showResultOfStep;
  }

  public List<AICondition> getConditions() {
    return conditions;
  }

  public void setConditions(List<AICondition> conditions) {
    this.conditions = conditions;
  }

  @Override
  public void run(String message, List<ChatMessage> memory,
      Map<String, String> metadatas, Assistant assistant) {
    Map<String, Object> params = new HashMap<>();
    params.put(AiConstants.MEMORY, AiFunction.getFormattedMemory(memory));
    params.put("conditions", generateConditionsString());

    String resultFromAI = assistant.getAiModel().getAiBot().chat(params,
        AiFlowPromptTemplates.FULFILL_CONDITIONAL_STEP);
    setOnSuccess(NumberUtils
        .toInt(StringProcessingUtils.standardizeResult(resultFromAI), -1));
  }

  private String generateConditionsString() {
    if (CollectionUtils.isEmpty(conditions)) {
      return StringUtils.EMPTY;
    }
    return BusinessEntityConverter.entityToJsonValue(conditions);
  }
}