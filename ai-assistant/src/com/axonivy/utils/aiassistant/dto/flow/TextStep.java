package com.axonivy.utils.aiassistant.dto.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.utils.aiassistant.core.AbstractAIBot;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.StepType;
import com.axonivy.utils.aiassistant.prompts.AiFlowPromptTemplates;

public class TextStep extends AiStep {

  private static final long serialVersionUID = 2717808782004150609L;

  private String text;
  private Integer showResultOfStep;
  private Boolean useAI;

  @Override
  public StepType getType() {
    return StepType.TEXT;
  }

  @Override
  public void run(String message, List<ChatMessage> memory,
      Map<String, String> metadatas, Assistant assistant) {
  }

  public void run(AiResultDTO resultToDisplay, Map<String, String> metadatas,
      List<ChatMessage> memory, Assistant assistant) {
    if (BooleanUtils.isTrue(useAI)) {
      getResultUsingAI(resultToDisplay, metadatas, memory,
          assistant.getAiModel().getAiBot());
    } else {
      getResult(resultToDisplay);
    }

  }

  private void getResult(AiResultDTO resultToDisplay) {
    setResult(new AiResultDTO());
    if (!BooleanUtils.isTrue(getIsHidden())) {
      getResult().setResult(this.getText().concat(System.lineSeparator())
          .concat(System.lineSeparator())
          .concat(Optional.ofNullable(resultToDisplay)
              .map(AiResultDTO::getResult).orElse("")));
    }
    getResult().setResultForAI(this.getText().concat(System.lineSeparator())
        .concat(Optional.ofNullable(resultToDisplay).map(AiResultDTO::getResult)
            .orElse("")));
    getResult().setState(AIState.DONE);
  }

  private void getResultUsingAI(AiResultDTO resultToDisplay,
      Map<String, String> metadatas, List<ChatMessage> memory,
      AbstractAIBot bot) {
    setResult(new AiResultDTO());

    Map<String, Object> params = new HashMap<>();
    params.put("metadata", getFormattedMetadatas(metadatas));
    params.put("memory", AiFunction.getFormattedMemory(memory));
    params.put("customInstruction",
        Optional.ofNullable(getCustomInstruction()).orElse(""));

    String extractedText = extractTextInsideTag(
        bot.chat(params, AiFlowPromptTemplates.TEXT_STEP_USE_AI))
        .concat(System.lineSeparator()).concat(System.lineSeparator())
        .concat(Optional.ofNullable(resultToDisplay).map(AiResultDTO::getResult)
            .orElse(""));

    if (StringUtils.isNotBlank(extractedText)) {
      if (!BooleanUtils.isTrue(getIsHidden())) {
        getResult().setResult(extractedText);
      }
      getResult().setResultForAI(extractedText);
      getResult().setState(AIState.DONE);
    }
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Integer getShowResultOfStep() {
    return showResultOfStep;
  }

  public void setShowResultOfStep(Integer showResultOfStep) {
    this.showResultOfStep = showResultOfStep;
  }

  public Boolean getUseAI() {
    return useAI;
  }

  public void setUseAI(Boolean useAI) {
    this.useAI = useAI;
  }
}