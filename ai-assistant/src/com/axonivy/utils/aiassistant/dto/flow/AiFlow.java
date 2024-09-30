package com.axonivy.utils.aiassistant.dto.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.history.Conversation;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.StepType;
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
  private static final int DEFAULT_NUMBER_OF_AI_STEPS_NEED_CHECK = 15;

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

    setDisabled(
        getSteps().stream().filter(step -> step.getType() == StepType.IVY_TOOL)
            .map(step -> (IvyToolStep) step).filter(step -> step.isDisabled())
            .count() > 0);
  }

  @JsonIgnore
  public void proceed(String request, Conversation conversation,
      Assistant workingAssistant) throws JsonProcessingException {

    if (endlessLogicLoop()) {
      finalResult = createSomethingWentWrongError();
      this.state = AIState.ERROR;
      return;
    }

    if (!hasPermision()) {
      finalResult = createNoPermisisonError();
      this.state = AIState.ERROR;
      return;
    }

    if (state == AIState.DONE) {
      return;
    }

    assistant = workingAssistant;
    init();
    if (getWorkingStep() == DEFAULT_DONE_STEP) {
      proceedFinishedFlowMessage(conversation);
      state = AIState.DONE;
      if (finalResult != null) {
        conversation.getHistory()
            .add(ChatMessage.newAIFlowMessage(finalResult.getResult()));
        conversation.getMemory()
            .add(ChatMessage.newAIFlowMessage(finalResult.getResultForAI()));
        ChatMessageManager.saveConversation(assistant.getId(), conversation);
      }
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

        // If showResultOfStep is set, get result of that step as input
        if (flowStep.getShowResultOfStep() != null) {
          flowStep.setTriggerMessage(
              getResultOfStep(flowStep.getShowResultOfStep()).getResultForAI());
        }

        // If useConversationMemory = true, append set the conversation memory
        // before the trigger message
        if (flowStep.getUseConversationMemory()) {
          String newTriggerMessage = conversation.getFormattedMemory()
              .concat(System.lineSeparator())
              .concat(flowStep.getTriggerMessage());
          flowStep.setTriggerMessage(newTriggerMessage);
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

  private void proceedFinishedFlowMessage(Conversation conversation) {
    setNotificationMessage(generateFinishedFunctionMessage());
    conversation.getHistory()
        .add(ChatMessage.newAIFlowMessage(getNotificationMessage()));
    conversation.getMemory()
        .add(ChatMessage.newAIFlowMessage(getNotificationMessage()));
    ChatMessageManager.saveConversation(assistant.getId(), conversation);
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

  /**
   * Handle endless loop logic problem. If AI run more than 15 steps without
   * inform user, consider it as an endless logic loop
   * 
   * @return
   */
  @JsonIgnore
  private Boolean endlessLogicLoop() {
    if (CollectionUtils.isEmpty(runSteps)
        || runSteps.size() < DEFAULT_NUMBER_OF_AI_STEPS_NEED_CHECK) {
      return false;
    }

    // Count down from the latest AI step.
    // If AI Flow run 15 steps without inform to user, consider as endless logic
    // loop
    int numberOfNonTextStep = 0;
    for (int i = runSteps.size() - 1; i >= 0; i--) {
      AiStep step = runSteps.get(i);

      // If AI Flow has a step that inform user: non-hidden text step
      // Stop counting
      if (step.getType() == StepType.TEXT
          && BooleanUtils.isNotTrue(step.getIsHidden())) {
        break;
      }

      numberOfNonTextStep++;
    }

    return numberOfNonTextStep >= DEFAULT_NUMBER_OF_AI_STEPS_NEED_CHECK;
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