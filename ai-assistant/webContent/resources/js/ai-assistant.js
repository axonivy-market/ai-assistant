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

      // Ctrl + Enter (or Cmd + Enter on Mac) is pressed
      if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
        // Add new line and adjust textarea height
        view.addNewLineToTextbox(event.srcElement);
        view.adjustTextareaHeight(event.srcElement);
        return;
      }

      // Enter is pressed
      if (event.key === 'Enter') {
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
              view.renderAiFlowMessage(message.content);
            } else {
              if (message.role == 'AI') {
                streaming = true;
                view.renderMessage(message.content);
                streaming = false;
                view.removeStreamingClassFromMessage();
              } else if (message.role == 'Error') {
                view.renderErrorMessage(message.content);
              } else if (message.role == 'Notification') {
                view.renderSystemMessage(message.content, false);
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
      resumeFlow(ivyUri, view, request, conversationId, assistantId);
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
        if (result.error) {
          view.renderErrorMessage(result.error);
          return;
        }
        if (result.selectedFunctionMessage) {
          view.renderSystemMessage(result.selectedFunctionMessage, true);
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
        if(result.error) {
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

  async function resumeFlow(ivyUri, view, request, conversationId, assistantId) {
    view.disableSendButton();

    // Send AJAX request to the chatbot server
    const uri = ivyUri + conversationId + '/resume';
    const content = JSON.stringify({
      'message': request,
      'aiFlow': JSON.stringify(workingFlow),
      'assistantId': assistantId
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
        view.renderSystemMessage(result.selectedFunctionMessage, true);
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

    if (workingFlow.state == 'done') {
      if (workingFlow?.finalResult?.resultForAI) {
        executeResult(workingFlow.finalResult.resultForAI);
        streaming = true;
        view.renderMessage(workingFlow.finalResult.result);
        streaming = false;
        view.removeStreamingClassFromMessage();
      }

      view.enableSendButton();
      workingFlow = null;
      return;
    }
    

    const workingStep = workingFlow.runSteps[workingFlow.runSteps.length - 1];
    executeResult(workingStep.result.resultForAI);
    view.renderAiFlowMessage(workingStep.result.result);

    view.enableSendButton();

    if (workingFlow.state != 'in_progress') {
      workingFlow = null;
    }
  }

  function executeResult(resultForAI) {
    if (resultForAI.startsWith('<execute>') && resultForAI.endsWith('</execute>')) {
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
  
  this.renderSystemMessage = function(message, useAnimation) {
    streaming = true;
    const icon = `<span class="si si-cog-double-2"></span>`;
    this.renderMessage(icon + message.replace(/<([^>]+)>/g, "<strong>$1</strong>"));
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

  // Helper function for rendering messages
  function renderNewMessageFunc(messageWrapper, isMyMessage) {
    // Clone message template
    const cloneTemplate = originalMessageTemplate.cloneNode(true);
    const message = isMyMessage ? messageWrapper.message : parseMessage(messageWrapper);

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
    if (typeof jsMessageList !== 'undefined') {
      const messageList = $(jsMessageList);
      const streamingMessage = messageList.find('.chat-message-container.streaming');
      if (streamingMessage.length > 0) {
        streamingMessage.removeClass('streaming');
        $(streamingMessage).find('.js-message').get(0).innerHTML = parseFinalMessage(streamingValue);
      } else {
        const messages = messageList.find('.chat-message-container .js-message');
        messages.get(messages.length - 1).innerHTML = parseFinalMessage(streamingValue);
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
    const initialHeight = 5 * parseFloat(getComputedStyle(textbox).fontSize);
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