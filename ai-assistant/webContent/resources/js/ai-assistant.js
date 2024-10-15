// Detecting browser type
const isEdge = window.navigator.userAgent.includes('Edge');
const isFireFox = navigator.userAgent.toLowerCase().includes('firefox');
const isIphone = navigator.userAgent.match(/iPhone|iPod/i);
const IVY = 'IVY';
const RETRIEVAL_QA = 'RETRIEVAL_QA';
const VALIDATE_ERROR = 'ERROR';
var streamingValue = '';

var workingFlow = null;

const ajaxHeader = {
  'Content-Type': 'application/json',
  'X-Requested-By': 'ivy',
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST',
}

// Global variables
let streaming = false;

// Handling browser visibility change
if (typeof document.hidden !== 'undefined') {
  hidden = 'hidden';
  visibilityChange = 'visibilitychange';
} else if (typeof document.msHidden !== 'undefined') {
  hidden = 'msHidden';
  visibilityChange = 'msvisibilitychange';
} else if (typeof document.webkitHidden !== 'undefined') {
  hidden = 'webkitHidden';
  visibilityChange = 'webkitvisibilitychange';
}

// Helper class for constructing RESTful URIs
function IvyUri() {
  this.rest = function () {
    return window.location.origin + contextPath + '/api';
  };
}

// ChatBot class for handling chat interactions
function Assistant(ivyUri, uri, view, assistantId, conversationId, username) {
  this.uri = uri;
  this.ivyUri = ivyUri;
  this.assistantId = assistantId;
  this.conversationId = conversationId;
  this.username = username;

  // Bind events for UI elements
  this.bindEvents = function () {
    // Bind event for window
    window.addEventListener('resize', (event) => {
      view.init();
    });

    // Bind events for text box interactions
    const textbox = $('.js-chat-send-form .js-chatbot-input-message').get(0);
    if (!textbox) {
      console.error('Textbox is undefined. Make sure to provide a valid element.');
      return;
    }

    textbox.addEventListener('paste', (event) => {
      // Adjust textarea height after paste event
      view.adjustTextareaHeight(event.srcElement);
    });

    textbox.addEventListener('keydown', (event) => {
      const ctrlPressed = event.ctrlKey || event.metaKey;
      // Ctrl + V (or Cmd + V on Mac) is pressed
      if (ctrlPressed && event.key === 'v') {
        return;
      }

      // Ctrl (or Cmd on Mac) + X, Y, Z is pressed
      if (ctrlPressed && ((event.key === 'z') || (event.key === 'y') || (event.key === 'x'))) {
        view.adjustTextareaHeight(event.srcElement);
        return;
      }

      // Enter is pressed without other keys, send the message
      if (event.key === 'Enter' && !(event.ctrlKey || event.altKey || event.shiftKey || event.metaKey)) {
        event.preventDefault();
        this.sendMessage(event.srcElement);
        return;
      }

      // Adjust textarea height for other cases
      view.adjustTextareaHeight(event.srcElement);
    });

    // Bind event for candidate questions
    const candidateQuestionBlocks = $('.js-candidate-question-block');
    if (!candidateQuestionBlocks || candidateQuestionBlocks.length == 0) {
      return;
    }

    for (let i = 0; i < candidateQuestionBlocks.length; i++) {
      candidateQuestionBlocks.get(i).addEventListener('click', (event) => {

        // Create message object and send it
        const inputMessage = $(event.currentTarget).find('.js-candidate-question').text();
        const selectedMessage = { 'message': inputMessage };

        // Render own message
        view.renderMyMessage(selectedMessage);

        // Clear input textbox and initialize
        textbox.value = '';
        view.initInputTextbox(textbox);

        // Scroll to latest message
        view.scrollToLatestMessage();

        request(this.ivyUri, view, inputMessage, this.conversationId);
      });
    }
  };

  // Initialize conversation
  this.init = function () {
    const uri = ivyUri + assistantId + '/' + conversationId + '/initialize';

    fetch(uri, {
      headers: ajaxHeader,
      method: 'GET'
    }).then(response => response.json())
      .then(result => {
        if (result && result.history) {
          result.history.forEach(message => {
            if (message.isAiFlowMessage) {
              view.collapseSystemSteps();
              view.renderAiFlowMessage(message.content);
            } else {
              if (message.role == 'AI') {
                view.collapseSystemSteps();
                streaming = true;
                view.renderMessage(message.content);
                streaming = false;
                view.removeStreamingClassFromMessage();
              } else if (message.role == 'Error') {
                view.collapseSystemSteps();
                view.renderErrorMessage(message.content);
              } else if (message.role == 'System') {
                view.renderSystemMessage(message.content, false, message.type);
              } else {
                view.renderMyMessage({ 'message': message.content });
              }
            }
          });
          streaming = false;
        }
      }).catch(function (err) {
        console.error('Error initilize conversation history:', err);
      });
  }

  // Sending a message
  this.sendMessage = async function (textbox) {
    // Get input message value
    const inputMessage = $(textbox).val().trim();
    if (!inputMessage) {
      return;
    }

    // Create message object and send it
    const message = { 'message': inputMessage };

    // Render own message
    view.renderMyMessage(message);

    // Clear input textbox and initialize
    textbox.value = '';
    view.initInputTextbox(textbox);

    // Scroll to latest message
    view.scrollToLatestMessage();

    request(this.ivyUri, view, inputMessage, this.conversationId);
  };

  this.onClickSendMessage = function (event) {
    // Send message on click then initialize input textbox
    const textbox = $('.js-chat-send-form .js-chatbot-input-message').get(0);
    this.sendMessage(textbox);
  };

  // Function to start a user request
  async function request(ivyUri, view, request, conversationId) {
    if (workingFlow) {
      resumeFlow(ivyUri, view, request, conversationId, assistantId, false);
      return;
    }

    view.disableSendButton();

    // Send AJAX request to the chatbot server
    const uri = ivyUri + conversationId + '/request';
    const content = JSON.stringify({
      'message': request,
      'conversationId': conversationId,
      'assistantId': assistantId
    });

    fetch(uri, {
      headers: ajaxHeader,
      method: 'POST',
      body: content
    }).then(response => response.json())
      .then(result => {

        // Handle error from server
        if (result.error) {
          view.renderErrorMessage(result.error);
          return;
        }

        //Handle request error
        if (result.statusCode && result.statusCode == 500) {
          view.renderErrorMessage(result.errorMessage);
          return;
        }

        if (result.selectedFunctionMessage) {
          view.renderSystemMessage(result.selectedFunctionMessage, true, result.type);
          continueRequest(conversationId, JSON.stringify(result));
          return;
        }
        if (result.workingStep) {
          // Handle AI flow
          handleWorkingFlow(ivyUri, view, result, conversationId);
          return;
        } else {
          // Handle normal conversation
          if (result.status == 'in_progress') {
            streaming = true;
            streamReply(ivyUri, assistantId, result.conversationId, view);
          } else {
            if (result.status == 'done') {
              view.renderMessage(result.token);
            }
            view.enableSendButton();
          }
        }
      }).catch(function (err) {
        console.error('Error sending chat message:', err);
        view.enableSendButton();
      });
  }

  async function continueRequest(conversationId, assistantPayload) {
    // Send AJAX request to the chatbot server
    const uri = ivyUri + conversationId + '/continueRequest';
    const content = assistantPayload;

    fetch(uri, {
      headers: ajaxHeader,
      method: 'POST',
      body: content
    }).then(response => response.json())
      .then(result => {
        if (result.error) {
          view.renderErrorMessage(result.error);
          return;
        }
        if (result.workingStep) {
          // Handle AI flow
          handleWorkingFlow(ivyUri, view, result, conversationId);
          return;
        } else {
          // Handle normal conversation
          if (result.status == 'in_progress') {
            streaming = true;
            view.collapseSystemSteps();
            streamReply(ivyUri, assistantId, result.conversationId, view);
          } else {
            if (result.status == 'done') {
              view.renderMessage(result.token);
            }
            view.enableSendButton();
          }
        }
      }).catch(function (err) {
        console.error('Error sending chat message:', err);
        view.enableSendButton();
      });
  }

  async function streamReply(ivyUri, assistantId, conversationId, view) {
    const uri = ivyUri + assistantId + '/' + encodeURIComponent(conversationId) + '/stream';
    await fetch(uri, {
      headers: ajaxHeader,
      method: 'GET'
    }).then(response => response.json())
      .then(result => {
        if (result.error) {
          view.renderErrorMessage(result.error);
          return;
        }

        if (result.status == 'in_progress') {
          streaming = true;
          if (result.token != null) {
            if (result.token.startsWith(result.conversationId)) {
              streaming = false;
              streamingValue = result.token.replace(result.conversationId, '').trim();
              view.removeStreamingClassFromMessage();
              view.enableSendButton();
              return;
            }
            view.renderMessage(result.token);
          }
          streamReply(ivyUri, assistantId, result.conversationId, view);
        } else {
          streaming = false;
          view.enableSendButton();
        }
      });
  }

  async function resumeFlow(ivyUri, view, request, conversationId, assistantId, isSkipMessage) {
    view.disableSendButton();

    // Send AJAX request to the chatbot server
    const uri = ivyUri + conversationId + '/resume';
    const content = JSON.stringify({
      'message': request,
      'aiFlow': JSON.stringify(workingFlow),
      'assistantId': assistantId,
      'isSkipMessage': isSkipMessage
    });

    fetch(uri, {
      headers: ajaxHeader,
      method: 'POST',
      body: content
    }).then(response => response.json())
      .then(result => {
        handleWorkingFlow(ivyUri, view, result, conversationId);
      });
  }

  function handleWorkingFlow(ivyUri, view, result, conversationId) {
    workingFlow = result;

    // Handle trigger another flow
    if (!workingFlow.state && workingFlow?.selectedFunctionId) {
      view.renderSystemMessage(result.selectedFunctionMessage, true, result.type);
      continueRequest(conversationId, JSON.stringify(workingFlow));
      return;
    }

    // If error occurred, send the request back to the default flow
    if (workingFlow.state == 'error') {
      const message = workingFlow.finalResult.result;
      workingFlow = null;
      request(ivyUri, view, message, conversationId);
      return;
    }

    // If the flow is done, show final result
    if (workingFlow.state == 'done') {
      if (workingFlow?.finalResult?.resultForAI) {
        executeResult(workingFlow.finalResult.resultForAI);
        streaming = true;
        view.renderMessage(workingFlow.finalResult.result);
        streaming = false;
      }
      view.removeStreamingClassFromMessage();
      view.renderSystemMessage(workingFlow.notificationMessage, true);

      view.enableSendButton();
      workingFlow = null;
      return;
    }

    const workingStep = workingFlow.runSteps.length == 0 ? null : workingFlow.runSteps[workingFlow.runSteps.length - 1];

    // Show notification
    if (workingFlow.notificationMessage) {
      const type = workingStep == null ? '' : workingStep.type;
      view.renderSystemMessage(workingFlow.notificationMessage, true, type);
      resumeFlow(ivyUri, view, '', conversationId, assistantId, true);
      return;
    }

    
    
    // If step type switch, continue without render message
    if (workingStep.type == 'SWITCH') {
      resumeFlow(ivyUri, view, '', conversationId, assistantId, true);
      return;
    }
    
    if (workingStep.result) {
      executeResult(workingStep.result.resultForAI);
      if (workingStep.isHidden) {
        resumeFlow(ivyUri, view, '', conversationId, assistantId, true);
        return;
      }
      view.renderAiFlowMessage(workingStep.result.result);

      if (workingFlow.workingStep == -1) {
        resumeFlow(ivyUri, view, '', conversationId, assistantId, true);
        return;
      }

      return;
    }


    view.enableSendButton();

    if (workingFlow.state != 'in_progress') {
      workingFlow = null;
    }
  }

  function executeResult(resultForAI) {
    if (resultForAI && resultForAI.startsWith('<execute>') && resultForAI.endsWith('</execute>')) {
      let link = resultForAI.replace('<execute>', '').replace('</execute>', '');
      parent.redirectToUrlCommand([{ name: 'url', value: link }]);
    }
  }

  this.onClearHistory = function () {
    workingFlow = null;
  }
}

// View class for rendering chat messages
function ViewAI(uri) {
  this.init = function () {
    $('.container.frame').addClass('chatbot-layout-main');
    // Set the iframe height to content height
    const contentHeight = window.parent.document.getElementById('iFrame').clientHeight;
    $('.js-chatbot-panel').height(contentHeight);

    $('.js-chatbot-panel').parents('#content').css('padding', '0');
    $('.js-chatbot-panel').parents('body').css('overflow', 'hidden');

    // Initialize textbox element
    this.initInputTextbox($('.js-chat-send-form .js-chatbot-input-message').get(0));
  };

  // Initializing message components
  function setValueForMessageComponent() {
    originalMessageTemplate = document.getElementsByClassName('js-message-template')[0];
    jsMessageList = document.getElementsByClassName('js-chatbot-message-list')[0];
  }

  // Rendering received messages
  this.renderMessage = function (message) {
    // Set value for message components
    setValueForMessageComponent();
    // Render the message
    if (streaming) {
      updateMessageFunc(message);
    } else {
      renderNewMessageFunc(message, false);
    }
    this.scrollToLatestMessage();
  };

  // Rendering message from AI flow
  this.renderAiFlowMessage = function (message) {
    if (!message) {
      return;
    }

    view.collapseSystemSteps();

    var messages = message.split('\r\n\r\n');
    messages.forEach(line => {
      if (line.trim() != '') {
        streaming = true;
        this.renderMessage(line);
        streaming = false;
        this.removeStreamingClassFromMessage();
      }
    });

  }

  // Rendering user's own messages
  this.renderMyMessage = function (message) {
    // Set value for message components
    setValueForMessageComponent();
    // Render own message
    renderNewMessageFunc(message, true);
  };

  this.renderErrorMessage = function (message) {
    var messages = message.split('\r\n\r\n');
    messages.forEach(line => {
      if (line.trim() != '') {
        streaming = true;
        this.renderMessage(line);
        streaming = false;
      }
    });

    var messageList = document.getElementsByClassName('js-chatbot-message-list')[0];
    var streamingMessage = $(messageList).find('.chat-message-container.streaming');
    streamingMessage.addClass('error-response');
    streamingMessage.find('.chat-message').addClass('error');
    this.removeStreamingClassFromMessage();

  }

  this.renderSystemMessage = function (message, useAnimation, type) {
    if (!message || message.trim() === '') {
      return;
	  }

    message = message.replace(/<([^>]+)>/g, "$1");

    let icon = '';
    let header = '';

    switch(type) {
      case 'FLOW':
        icon = 'si si-lg si-cog-play';
        header = 'Use AI Flow';
        break;
      case 'IVY_TOOL':
        icon = 'si si-lg si-cog-double-2';
        header = 'Processing Ivy tool';
        break;
      case 'RE_PHRASE':
        icon = 'si si-lg si-messages-bubble-check';
        header = 'Rephrase message';
        break;
      case 'TRIGGER_FLOW':
        icon = 'si si-lg si-cog-play';
        header = 'Trigger AI Flow';
        break;
      case 'RETRIEVAL_QA':
        icon = 'si si-common-file-search';
        header = 'Use knowledge base';
        break;
      default:
        icon = 'si si-lg si-cog-double-2';
        header = 'Proceeded';
    }

    streaming = true;

    const iconPart = `<span class="${icon}"></span>`;
    const messagePart = `<div class="ml-3 flex flex-column"><span class="font-bold w-fit">${header}</span><p class="w-fit">${message}</p></div>`;

    this.renderMessage(`<div class="run-step w-full flex flex-row align-items-center ${type}"><div class="icon-container">${iconPart}</div>${messagePart}</div>`);
    streaming = false;

    var messageList = document.getElementsByClassName('js-chatbot-message-list')[0];
    var streamingMessage = $(messageList).find('.chat-message-container.streaming');
    streamingMessage.addClass('system-response');
    var chatMessage = streamingMessage.find('.chat-message');
    chatMessage.addClass('system');

    if (useAnimation) {
      chatMessage.hide();
      chatMessage.fadeToggle('slow');
    }

    this.removeStreamingClassFromMessage(true);
  }

  this.collapseSystemSteps = function () {
    const numberOfSystemResponse = $('.chatbot-message-list > .chat-message-container.system-response').length;
    
    if (numberOfSystemResponse !== 0) {
      const parentContainer = $('.chat-message-container.system-response').last().parent();
      
      // Append button and collapsible panel for system responses
      parentContainer.append(`
        <div class='ui-g-9'>
          <button type='button' class='system-response-expand-button chat-message'>
            Answered by ${numberOfSystemResponse} step(s)
            <i class='si si-arrow-down-1 si-sm ml-2'></i>
          </button>
        </div>
      `);
      
      parentContainer.append("<div class='system-response-container ui-g-9 hidden'></div>");

      // Move all system responses into the collapsible panel
      $('.chatbot-message-list > .chat-message-container.system-response')
        .appendTo($('.system-response-container').last());

      // Handle button click to collapse/expand the panel
      const expandButton = $('.system-response-expand-button').last();
      expandButton.click(function () {
        const responseContainer = expandButton.parent().next().first();
        responseContainer.toggleClass("hidden");
        expandButton.find("i")
          .toggleClass("si-arrow-down-1")
          .toggleClass("si-arrow-up-1");
      });

      parentContainer.find('.system-response-container').each(function() {
        let level = 0;
        let runSteps = $(this).find('.run-step');
        for (var i = 0; i < runSteps.length; i++) {
          var step = runSteps.get(i);
          $(step).addClass(`level-${level}`);
          $(step).find('.icon-container').addClass('border-circle border-1 border-solid flex flex-row align-items-center justify-content-center');

          if ($(step).hasClass('FLOW')) {
            level ++;
            $(step).addClass('has-child');
          }
          
          if (i != runSteps.length - 1) {
            $(step).addClass('draw-line');
          }
        }
      });
    }
  }

  // Helper function for rendering messages
  function renderNewMessageFunc(messageWrapper, isMyMessage) {
    // Clone message template
    const cloneTemplate = originalMessageTemplate.cloneNode(true);
    let message = '';
    if (isMyMessage) {
      message = messageWrapper.message.replaceAll('\r\n', '<br/>').replaceAll('\n', '<br/>');
    } else {
      message = parseMessage(messageWrapper);
    }

    // Set message content
    cloneTemplate.getElementsByClassName('js-message')[0].innerHTML = message;
    $(cloneTemplate).removeClass('u-hidden').removeClass('js-message-template');

    // Set width to iframe inside the chat message
    if (cloneTemplate.querySelector('iframe') != null) {
      $(cloneTemplate).find('.chatbot-meta .message-block').get(0).style.width = '100%';
      $(cloneTemplate).find('.chatbot-meta .message-block .js-message').get(0).style.width = '100%';
      setTimeout(function () {
        $(cloneTemplate).find('iframe').width('100%');
      }, 50);
    }

    if (isMyMessage) {
      // Add class for own message
      $(cloneTemplate).addClass('my-message');
    } else {
      $(cloneTemplate).find('.js-message p').addClass('typed-out');
    }

    // Append message to the list
    appendMessageToList(cloneTemplate);
    return cloneTemplate;
  }

  // Helper function for update message
  function updateMessageFunc(messageWrapper) {
    streamingValue = streamingValue.concat(messageWrapper);

    // Clone message template
    const cloneTemplate = originalMessageTemplate.cloneNode(true);
    const message = parseMessage(streamingValue);

    // Set message content
    cloneTemplate.getElementsByClassName('js-message')[0].innerHTML = message;
    $(cloneTemplate).removeClass('u-hidden').removeClass('js-message-template');

    // Set width to iframe inside the chat message
    if (cloneTemplate.querySelector('iframe') != null) {
      $(cloneTemplate).find('.chatbot-meta .message-block').get(0).style.width = '100%';
      $(cloneTemplate).find('.chatbot-meta .message-block .js-message').get(0).style.width = '100%';
      setTimeout(function () {
        $(cloneTemplate).find('iframe').width('100%');
      }, 50);
    }

    // Update the streaming message
    updateStreamingMessage(cloneTemplate);
    return cloneTemplate;
  }

  // Helper function for appending messages to the message list
  function appendMessageToList(cloneTemplate) {
    const $messageList = $(jsMessageList);
    $messageList.append(cloneTemplate);

    if ($(cloneTemplate).hasClass('my-message')) {
      // If own message, scroll to the latest message
      const $chatMessages = $('.js-chatbot-message-list').find('.chat-message');
      setTimeout(function () {
        $messageList.animate({
          scrollTop: $messageList.scrollTop() + $chatMessages.last().offset().top
        }, 0);
      }, 0);
    }
  }

  // Function to update streaming message
  function updateStreamingMessage(cloneTemplate) {
    const messageList = $(jsMessageList);
    const streamingMessage = messageList.find('.chat-message-container.streaming');

    // Create streaming message if not exist
    if (streamingMessage.length == 0) {
      $(cloneTemplate).addClass('streaming');
      messageList.append(cloneTemplate);
      return;
    }

    // Update existing streaming message
    streamingMessage.get(0).innerHTML = cloneTemplate.innerHTML;
  }

  // Function to remove the 'streaming' class from a message
  // after the streaming process is done.
  this.removeStreamingClassFromMessage = function (isDisableChat) {
    if (streamingValue == '') {
      return;
    }

    let converted = isIFrame(streamingValue) ? convertIFrame(streamingValue) : marked.parse(streamingValue);

    if (typeof jsMessageList !== 'undefined') {
      const messageList = $(jsMessageList);
      const streamingMessage = messageList.find('.chat-message-container.streaming').not('.my-message');
      if (streamingMessage.length > 0) {
        streamingMessage.removeClass('streaming');
        $(streamingMessage).find('.js-message').get(0).innerHTML = converted;
      } else {
        const messages = messageList.find('.chat-message-container').not('.my-message').find('.js-message');
        messages.get(messages.length - 1).innerHTML = converted;
      }

      if (!isDisableChat) {
        this.enableSendButton();
      }

      streamingValue = '';
    }
  }

  // Add new line to the textbox
  this.addNewLineToTextbox = function (textbox) {
    const position = textbox.selectionEnd;
    // Insert new line and adjust height
    textbox.value = textbox.value.substring(0, position) + '\n' + textbox.value.substring(position);
    textbox.selectionEnd = position + 1;
    textbox.selectionStart = position + 1;
    textbox.scrollTop = textbox.scrollHeight;
  };

  // Scroll to the latest message
  this.scrollToLatestMessage = function () {
    const $messageList = $('.js-message-list');
    // Scroll to the bottom of the message list with animation
    $messageList.animate({
      scrollTop: $messageList[0].scrollHeight
    }, 0);
  };

  // Adjust textarea height based on content
  this.adjustTextareaHeight = function (textbox) {
    setTimeout(function () {
      initTextbox(textbox);
      const scrollHeight = textbox.scrollHeight;
      const maxHeight = 200;

      if (scrollHeight > maxHeight) {
        // If content exceeds max height, set max height and show scrollbar
        textbox.style.maxHeight = maxHeight + 'px';
        textbox.style.height = maxHeight + 'px';
        textbox.style.overflow = 'auto';
      } else {
        // Set height based on content and hide scrollbar
        textbox.style.height = scrollHeight + 'px';
        textbox.style.overflow = 'hidden';
      }
    }, 0);
  };

  // Initialize input textbox height
  this.initInputTextbox = function (textbox) {
    initTextbox(textbox);
  };

  function initTextbox(textbox) {
    // Initial height of the textbox equals to 5 times font size
    const initialHeight = 2 * parseFloat(getComputedStyle(textbox).fontSize);
    const paddingTop = parseFloat(getComputedStyle(textbox).paddingTop);
    const paddingBottom = parseFloat(getComputedStyle(textbox).paddingBottom);
    textbox.style.height = initialHeight + paddingTop + paddingBottom + 'px';
  }

  // Disable the send button, input text
  // and show typing dots.
  this.disableSendButton = function () {
    var sendButton = $('.js-chat-send-form').find('.ui-button');
    sendButton.addClass('ui-state-disabled');
    sendButton.blur();

    var sendTextbox = $('.js-chat-send-form').find('.js-chatbot-input-message');
    sendTextbox.addClass('ui-state-disabled');
    sendTextbox.blur();
    createTypingDots('js-chat-send-form');
  }

  // Enable the send button, input text
  // and show typing dots.
  this.enableSendButton = function () {
    $('.js-chat-send-form').find('.ui-button').removeClass('ui-state-disabled');
    $('.js-chat-send-form').find('.js-chatbot-input-message').removeClass('ui-state-disabled').focus();
    removeTypingDots('js-chat-send-form');
    this.scrollToLatestMessage();
  }
}

$(document).ready(function() {
  var parent = $(window.parent);
  if (parent.length > 0) {
    if (parent.get(0).Portal) {
      $('body').addClass('in-portal');
    }
  }
});