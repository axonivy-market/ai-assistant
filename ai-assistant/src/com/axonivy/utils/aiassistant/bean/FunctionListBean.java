package com.axonivy.utils.aiassistant.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.aiassistant.constant.AiConstants;
import com.axonivy.utils.aiassistant.dto.Assistant;
import com.axonivy.utils.aiassistant.dto.flow.AiFlow;
import com.axonivy.utils.aiassistant.dto.knowledgebase.KnowledgeBase;
import com.axonivy.utils.aiassistant.dto.tool.AiFunction;
import com.axonivy.utils.aiassistant.dto.tool.RetrievalQATool;
import com.axonivy.utils.aiassistant.service.AiFunctionService;
import com.axonivy.utils.aiassistant.service.AssistantService;
import com.axonivy.utils.aiassistant.service.KnowledgeBaseService;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class FunctionListBean extends AbstractFunctionListBean
    implements Serializable {

  private static final long serialVersionUID = 5915528319848497892L;
  private List<AiFunction> allFunctions;
  private boolean showNonStartableAiFunction;
  private List<KnowledgeBase> knowledgeBases;

  @Override
  public void init(Assistant selectedAssistant) {
    allFunctions = AiFunctionService.getInstance().findAll();
    allFunctions.forEach(function -> function.init());

    sortAllFunctions();
    toggleNonStartable();
  }

  private void sortAllFunctions() {
    List<AiFunction> startables = filterStartableAiFunctions(allFunctions);
    nonStartables = (List<AiFunction>) CollectionUtils
        .subtract(allFunctions,
        startables);

    startables.sort(Comparator.comparing(AiFunction::getName));
    nonStartables.sort(Comparator.comparing(AiFunction::isEnabled)
        .thenComparing(AiFunction::getType)
        .thenComparing(AiFunction::getName));
    startables.addAll(nonStartables);
    allFunctions = startables;
  }

  /**
   * If turn on: show ivy tools and non-startable AI flows
   * 
   */
  public void toggleNonStartable() {
    if (showNonStartableAiFunction) {
      setFilteredFunctions(allFunctions);
    } else {
      filteredFunctions = filterStartableAiFunctions(allFunctions);
    }
  }

  private List<AiFunction> filterStartableAiFunctions(
      List<AiFunction> functions) {
    List<AiFunction> result = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(functions)) {
      for (AiFunction function : functions) {
        switch (function) {
          case AiFlow flow -> {
            flow.init();
            if (!flow.isDisabled()) {
              result.add(function);
            }
          }
          case RetrievalQATool knowledgeBase -> {
            if (!knowledgeBase.isDisabled()) {
              result.add(function);
            }
          }
          default ->  {}
        };
      }
    }
    return result;
  }

  public String getRemoveAiFunctionMessage() {
    if (Optional.ofNullable(getSelectedFunction()).map(AiFunction::getId)
        .isEmpty()) {
      return StringUtils.EMPTY;
    }

    List<Assistant> assistants = AssistantService.getInstance().findAssistantByFunctionId(getSelectedFunction().getId());
    String formattedUsingAssistants = String.join(
        AiConstants.COMMA,
        assistants.stream().map(Assistant::getName).toList());

    return Ivy.cms().co(
        "/Dialogs/com/axonivy/utils/aiassistant/component/AIFunctionList/RemoveFunctionMessage",
        Arrays.asList(formattedUsingAssistants));
  }

  @Override
  public void deleteFunction() {
    for (AiFunction function : allFunctions) {
      if (function.getId().contentEquals(getSelectedFunction().getId())) {
        AiFunctionService.getInstance().delete(function.getId());
        allFunctions.remove(function);
        toggleNonStartable();
        break;
      }
    }
  }

  public List<AiFunction> getAllFunctions() {
    return allFunctions;
  }

  public void setAllFunctions(List<AiFunction> allFunctions) {
    this.allFunctions = allFunctions;
  }

  public boolean isShowNonStartableAiFunction() {
    return showNonStartableAiFunction;
  }

  public void setShowNonStartableAiFunction(
      boolean showNonStartableAiFunction) {
    this.showNonStartableAiFunction = showNonStartableAiFunction;
  }

  public List<KnowledgeBase> getKnowledgeBases() {
    if (knowledgeBases == null) {
      knowledgeBases = KnowledgeBaseService.getInstance().findAll();
    }
    return knowledgeBases;
  }
}
