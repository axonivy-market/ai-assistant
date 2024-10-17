package com.axonivy.utils.aiassistant.dto.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.dto.tool.IvyTool;
import com.axonivy.utils.aiassistant.enums.StepType;
import com.axonivy.utils.aiassistant.prompts.AiFlowPromptTemplates;
import com.axonivy.utils.aiassistant.service.AiFunctionService;
public class RephraseStep extends AiStep {

  private static final long serialVersionUID = -4106563714989416129L;

  private static final String EXAMPLE_FORMAT = "before: %s ; after: %s";
  private String toolId;
  private Integer onRephrase;
  private List<RephraseExample> examples;

  @Override
  public StepType getType() {
    return StepType.RE_PHRASE;
  }

  @Override
  public void run(String message, List<ChatMessage> memory,
      Map<String, String> metadatas, Assistant assistant) {
    IvyTool tool = (IvyTool) AiFunctionService.getInstance()
        .findById(getToolId());

    String dataFulfillRequest = tool.buildDataFulfillRequest();
    if (StringUtils.isBlank(dataFulfillRequest)) {
      dataFulfillRequest = BusinessEntityConverter.entityToJsonValue(tool);
    }

    Map<String, Object> params = new HashMap<>();
    params.put("info", assistant.getInfo());
    params.put("metadata", getFormattedMetadatas(metadatas));
    params.put("memory", AiFunction.getFormattedMemory(memory));
    params.put("tool", dataFulfillRequest);
    params.put("examples", formatExamples());
    params.put("customInstruction",
        Optional.ofNullable(getCustomInstruction()).orElse(StringUtils.EMPTY));

    String resultFromAI = assistant.getAiModel().getAiBot().chat(params,
        AiFlowPromptTemplates.RE_PHRASE_STEP);
    String extractedRephrasedText = extractTextInsideTag(resultFromAI);

    AiResultDTO resultDto = new AiResultDTO();
    if (StringUtils.isNotBlank(extractedRephrasedText)) {
      setOnSuccess(onRephrase);
      resultDto.setResult(extractedRephrasedText);
      resultDto.setResultForAI(extractedRephrasedText);

    } else {
      resultDto.setResult(memory.getLast().getContent());
      resultDto.setResultForAI(memory.getLast().getContent());
    }
    setResult(resultDto);
    resultDto.setState(AIState.DONE);
    setNotificationMessage(resultDto.getResult());
  }

  public String getToolId() {
    return toolId;
  }

  public void setToolId(String toolId) {
    this.toolId = toolId;
  }

  public Integer getOnRephrase() {
    return onRephrase;
  }

  public void setOnRephrase(Integer onRephrase) {
    this.onRephrase = onRephrase;
  }

  public List<RephraseExample> getExamples() {
    return examples;
  }

  public void setExamples(List<RephraseExample> examples) {
    this.examples = examples;
  }

  public String formatExamples() {
    if (CollectionUtils.isNotEmpty(examples)) {
      String result = StringUtils.EMPTY;
      for (RephraseExample example : examples) {
        result = result.concat(String.format(EXAMPLE_FORMAT,
            example.getBefore(), example.getAfter()))
            .concat(System.lineSeparator());
      }
      return result;
    }
    return StringUtils.EMPTY;
  }
}