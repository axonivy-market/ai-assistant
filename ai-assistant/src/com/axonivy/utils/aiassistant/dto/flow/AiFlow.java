package com.axonivy.utils.aiassistant.dto.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.history.Conversation;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.history.ChatMessageManager;
import com.axonivy.utils.aiassistant.prompts.AiFlowPromptTemplates;
import com.axonivy.utils.aiassistant.utils.AssistantUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import ch.ivyteam.ivy.environment.Ivy;

public class AiFlow extends AiFunction {
  private static final long serialVersionUID = 7244844189316469927L;

  private static final int DEFAULT_DONE_STEP = -1;

  private List<AiStep> steps;

  private List<AiStep> runSteps;

  private Integer workingStep;

  private AIState state;

  private List<ChatMessage> memory;

  private AiResultDTO finalResult;

  private String notificationMessage;

  @JsonIgnore
  private AiFunction functionToTrigger;

  @JsonIgnore
  private Map<String, String> metadatas;

  @JsonIgnore
  private Assistant assistant;

  @Override
  public void init() {
    if (metadatas == null) {
      metadatas = AssistantUtils.getMetadatas();

    }

    if (getWorkingStep() == null) {
      setWorkingStep(0);
    }

    if (memory == null) {
      memory = new ArrayList<>();
    }
    setNotificationMessage(null);
  }

  @JsonIgnore
  public void proceed(String request, Conversation conversation,
      Assistant workingAssistant) throws JsonProcessingException {
    if (!hasPermision()) {
      finalResult = createNoPermisisonError();
      this.state = AIState.ERROR;
      return;
    }

    assistant = workingAssistant;
    init();
    if (getWorkingStep() == DEFAULT_DONE_STEP) {
      if (finalResult == null) {
        initDefaultFinalResult();
      }
      state = AIState.DONE;
      conversation.getHistory()
          .add(ChatMessage.newAIFlowMessage(finalResult.getResult()));
      conversation.getMemory()
          .add(ChatMessage.newAIFlowMessage(finalResult.getResultForAI()));
      ChatMessageManager.saveConversation(assistant.getId(), conversation);
      return;
    }

    if (StringUtils.isNotBlank(request)) {
      memory.add(ChatMessage.newUserMessage(request));
    }

    // If user cancel the flow or input something meaningless, just cancel the
    // flow.
    if (memory.size() > 1) {
      switch (runCheckMessageStep()) {
      case 0 -> {
        finalResult = createCancelMessage();
        updateStepResultToMemory(finalResult, conversation, true);
        this.state = AIState.DONE;
        return;
      }
      case 1 -> {
        finalResult = createRestartMessage(request);
        state = AIState.ERROR;
        conversation.getHistory().remove(conversation.getHistory().size() - 1);
        conversation.getMemory().remove(conversation.getMemory().size() - 1);
        return;
      }
      default -> {
      }
      }
      ;
    }

    if (runSteps == null) {
      runSteps = new ArrayList<>();
    }
    state = AIState.IN_PROGRESS;

    // Run the working step
    while (workingStep != DEFAULT_DONE_STEP) {
      AiStep step;
      try {
        step = steps.get(workingStep).clone();
      } catch (CloneNotSupportedException e) {
        finalResult = createSomethingWentWrongError();
        this.state = AIState.ERROR;
        return;
      }

      this.runSteps.add(step);
      List<ChatMessage> memoryToRun = BooleanUtils.isTrue(
          step.getUseConversationMemory()) ? conversation.getMemory() : memory;

      switch (step.getType()) {
      // run as text step
      case TEXT -> {
        TextStep text = (TextStep) step;
        text.run(getResultOfStep(text.getShowResultOfStep()), metadatas,
            memoryToRun, assistant);

        updateStepResultToMemory(text.getResult(), conversation,
            BooleanUtils.isNotFalse(text.getSaveToHistory()));
        updateWorkingStep(text);
        if (!BooleanUtils.isTrue(text.getIsHidden())) {
          return;
        }
      }

      // run as Ivy Tool step
      case IVY_TOOL -> {
        IvyToolStep ivyStep = (IvyToolStep) step;
        ivyStep.run(request, memoryToRun, metadatas, assistant);
        updateStepResultToMemory(ivyStep.getResult(), conversation,
            BooleanUtils.isNotFalse(ivyStep.getSaveToHistory()));
      }

      // run as Conditional step
      case SWITCH -> {
        SwitchStep conditionalStep = (SwitchStep) step;
        conditionalStep.run(request, memoryToRun, metadatas, assistant);

        if (Optional.ofNullable(conditionalStep.getResult())
            .map(AiResultDTO::getResult).isPresent()) {
          updateStepResultToMemory(conditionalStep.getResult(), conversation,
              BooleanUtils.isNotFalse(conditionalStep.getSaveToHistory()));
        }
      }

      // run as Re-phrase step
      case RE_PHRASE -> {
        RephraseStep rephraseStep = (RephraseStep) step;
        rephraseStep.run(request, memoryToRun, metadatas, assistant);

        if (Optional.ofNullable(rephraseStep.getResult())
            .map(AiResultDTO::getResult).isPresent()) {
          updateStepResultToMemory(rephraseStep.getResult(), conversation,
              BooleanUtils.isNotFalse(rephraseStep.getSaveToHistory()));
        }
      }

      case TRIGGER_FLOW -> {
        TriggerFlowStep flowStep = (TriggerFlowStep) step;
        if (StringUtils.isBlank(flowStep.getTriggerMessage())) {
          flowStep.setTriggerMessage(
              getResultOfStep(flowStep.getShowResultOfStep()).getResultForAI());
        }
        flowStep.run(request, memoryToRun, metadatas, workingAssistant);
        state = flowStep.getResult().getState();
        finalResult = flowStep.getResult();
        functionToTrigger = flowStep.getFunction();
        return;
      }
      }
      ;

      updateWorkingStep(step);
      setNotificationMessage(step.getNotificationMessage());

      if (getWorkingStep() == DEFAULT_DONE_STEP) {
        state = AIState.DONE;
        finalResult = step.getResult();
      }
      return;
    }
  }

  /**
   * Get result of the step defined by the field "showResultOfStep"
   * 
   * @param text the textStep
   * @return
   */
  private AiResultDTO getResultOfStep(Integer stepToShow) {
    if (stepToShow == null || stepToShow < 0) {
      return null;
    }

    List<AiStep> filteredSteps = runSteps.stream()
        .filter(s -> s.getStepNo() == stepToShow).toList();
    AiStep targetStep = filteredSteps.get(filteredSteps.size() - 1);
    return Optional.ofNullable(targetStep).map(AiStep::getResult).orElse(null);
  }

  /**
   * Update working step based on state of the step after run Done: set
   * 'onSucess' to the working step Error: set 'onError' to the working step
   * 
   * @param step
   */
  private void updateWorkingStep(AiStep step) {
    setWorkingStep(Optional.ofNullable(step).map(AiStep::getResult)
        .map(AiResultDTO::getState).orElse(AIState.DONE) == AIState.ERROR
            ? step.getOnError()
            : step.getOnSuccess());
  }

  /**
   * Handle save result of a step to memory of flow and the conversation
   * 
   * @param result
   * @param conversation
   * @param saveToHistory
   */
  private void updateStepResultToMemory(AiResultDTO result,
      Conversation conversation, Boolean saveToHistory) {
    ChatMessage messageForAi = ChatMessage
        .newAIFlowMessage(result.getResultForAI());
    ChatMessage message = ChatMessage.newAIFlowMessage(result.getResult());

    memory.add(messageForAi);

    // IF has error when run tool, only store in memory, don't store to the
    // history
    if (result.getState() != AIState.ERROR && saveToHistory) {
      conversation.getHistory().add(message);
    }
    conversation.getMemory().add(messageForAi);
    ChatMessageManager.saveConversation(assistant.getId(), conversation);
  }

  public boolean isInProgress() {
    return this.getState() == AIState.IN_PROGRESS;
  }

  @Override
  public ToolType getType() {
    return ToolType.FLOW;
  }

  public List<AiStep> getSteps() {
    return steps;
  }

  public void setSteps(List<AiStep> steps) {
    this.steps = steps;
  }

  public AIState getState() {
    return state;
  }

  public AiResultDTO getFinalResult() {
    return finalResult;
  }

  public void setFinalResult(AiResultDTO finalResult) {
    this.finalResult = finalResult;
  }

  public List<ChatMessage> getMemory() {
    return memory;
  }

  public void setMemory(List<ChatMessage> memory) {
    this.memory = memory;
  }

  public Integer getWorkingStep() {
    return workingStep;
  }

  public void setWorkingStep(Integer workingStep) {
    this.workingStep = workingStep;
  }

  public List<AiStep> getRunSteps() {
    return runSteps;
  }

  public void setRunSteps(List<AiStep> runSteps) {
    this.runSteps = runSteps;
  }

  private Integer runCheckMessageStep() {
    if ((memory.stream().filter(ChatMessage::isUserMessage).count() == 0)
        || !memory.getLast().isUserMessage()) {
      return 2;
    }
    Map<String, Object> params = new HashMap<>();
    params.put("memory",
        AiFunction.getFormattedMemoryForValidateMessage(memory));

    String resultFromAI = assistant.getAiModel().getAiBot().chat(params,
        AiFlowPromptTemplates.CHECK_USER_MESSAGE_STEP);

    return NumberUtils.toInt(AiStep.extractTextInsideTag(resultFromAI), 0);
  }

  @JsonIgnore
  protected AiResultDTO createCancelMessage() {
    AiResultDTO result = new AiResultDTO();
    result.setResult(Ivy.cms().co("/Labels/AI/Error/CanceledFlow"));
    result.setResultForAI(result.getResult());
    result.setState(AIState.DONE);
    return result;
  }

  @JsonIgnore
  protected AiResultDTO createRestartMessage(String request) {
    AiResultDTO result = new AiResultDTO();
    result.setResult(request);
    result.setResultForAI(request);
    result.setState(AIState.DONE);
    return result;
  }

  private void initDefaultFinalResult() {
    finalResult = new AiResultDTO();
    finalResult.setState(AIState.DONE);
    finalResult
        .setResult("Please let me know if you have any further requests.");
    finalResult
        .setResultForAI("Please let me know if you have any further requests.");
  }

  public Map<String, String> getMetadatas() {
    return metadatas;
  }

  public void setMetadatas(Map<String, String> metadatas) {
    this.metadatas = metadatas;
  }

  @JsonIgnore
  public Assistant getAssistant() {
    return assistant;
  }

  @JsonIgnore
  public void setAssistant(Assistant assistant) {
    this.assistant = assistant;
  }

  public AiFunction getFunctionToTrigger() {
    return functionToTrigger;
  }

  public void setFunctionToTrigger(AiFunction functionToTrigger) {
    this.functionToTrigger = functionToTrigger;
  }

  public String getNotificationMessage() {
    return notificationMessage;
  }

  public void setNotificationMessage(String notificationMessage) {
    this.notificationMessage = notificationMessage;
  }
}