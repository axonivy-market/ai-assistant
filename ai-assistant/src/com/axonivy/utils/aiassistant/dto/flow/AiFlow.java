package com.axonivy.utils.aiassistant.dto.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.history.ChatMessage;
import com.axonivy.utils.aiassistant.dto.history.Conversation;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.enums.StepType;
import com.axonivy.utils.aiassistant.enums.ToolType;
import com.axonivy.utils.aiassistant.history.ChatMessageManager;
import com.axonivy.utils.aiassistant.utils.AssistantUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonProperty(value = "startable")
  private boolean startable;

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

    boolean hasDisabledIvyTool = getSteps().stream()
        .filter(step -> step.getType() == StepType.IVY_TOOL)
        .map(step -> (IvyToolStep) step).filter(step -> step.isDisabled())
        .count() > 0;

    setDisabled(!(BooleanUtils.isTrue(this.startable) && !hasDisabledIvyTool));
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
            BooleanUtils.isNotFalse(text.getSaveToHistory()), null,
            step.getType());
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
            BooleanUtils.isNotFalse(ivyStep.getSaveToHistory()),
            ivyStep.getNotificationMessage(), step.getType());
      }

      // run as Conditional step
      case SWITCH -> {
        SwitchStep conditionalStep = (SwitchStep) step;
        conditionalStep.run(request, memoryToRun, metadatas, assistant);
      }

      // run as Re-phrase step
      case RE_PHRASE -> {
        RephraseStep rephraseStep = (RephraseStep) step;
        rephraseStep.run(request, memoryToRun, metadatas, assistant);

        if (Optional.ofNullable(rephraseStep.getResult())
            .map(AiResultDTO::getResult).isPresent()) {
          updateStepResultToMemory(rephraseStep.getResult(), conversation,
              BooleanUtils.isNotFalse(rephraseStep.getSaveToHistory()),
              rephraseStep.getNotificationMessage(), step.getType());
        }
      }

      case TRIGGER_FLOW -> {
        TriggerFlowStep flowStep = (TriggerFlowStep) step;

        // If showResultOfStep is set, get result of that step as input
        if (flowStep.getShowResultOfStep() != null) {
          flowStep.setTriggerMessage(ChatMessage.newAIMessage(
              getResultOfStep(flowStep.getShowResultOfStep()).getResultForAI())
              .getFormattedMessage());
        }

        // If useConversationMemory = true, append set the conversation memory
        // before the trigger message
        if (BooleanUtils.isTrue(flowStep.getUseConversationMemory())) {
          String newTriggerMessage = conversation.getFormattedMemory()
              .concat(System.lineSeparator())
              .concat(ChatMessage.newAIMessage(flowStep.getTriggerMessage())
                  .getFormattedMessage());
          flowStep.setTriggerMessage(newTriggerMessage);
        }
        flowStep.run(request, memoryToRun, metadatas, workingAssistant);
        state = flowStep.getResult().getState();
        finalResult = flowStep.getResult();
        functionToTrigger = flowStep.getFunction();
        return;
      }
      case KNOWLEDGE_BASE -> {
        KnowledgeBaseStep knowledge = (KnowledgeBaseStep) step;
        knowledge.setUserMessage(memoryToRun.getLast().getContent());
        state = AIState.DONE;
        AiResultDTO result = new AiResultDTO();
        result.setResultForAI(knowledge.getToolId());
        finalResult = result;
      }
      default -> throw new IllegalArgumentException(
          "Unexpected value: " + step.getType());
      }
      ;

      updateWorkingStep(step);
      setNotificationMessage(step.getNotificationMessage());

      if (getWorkingStep() == DEFAULT_DONE_STEP) {
        state = state == AIState.ERROR ? AIState.ERROR : AIState.DONE;
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
   * If the step type is KNOWLEDGE_BASE, end the flow
   * 
   * @param step
   */
  private void updateWorkingStep(AiStep step) {
    if (step.getType() == StepType.KNOWLEDGE_BASE) {
      setWorkingStep(DEFAULT_DONE_STEP);
      return;
    }
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
      Conversation conversation, Boolean saveToHistory,
      String notificationMessage, StepType type) {

    if (StringUtils.isNotBlank(notificationMessage)) {
      conversation.getMemory()
          .add(ChatMessage.newSystemMessage(notificationMessage, type.name()));
    }

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

    if (StringUtils.isNotBlank(notificationMessage)) {
      ChatMessage notification = ChatMessage
          .newSystemMessage(notificationMessage, type.name());
      conversation.getHistory().add(notification);
      conversation.getMemory().add(notification);
    }
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

  public boolean isStartable() {
    return startable;
  }

  public void setStartable(boolean startable) {
    this.startable = startable;
  }
}