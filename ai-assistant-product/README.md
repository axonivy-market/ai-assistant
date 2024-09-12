# ai-assistant

YOUR DESCRIPTION GOES HERE: Please just give a short description here without further headings.

<todo>

## Demo

YOUR DEMO DESCRIPTION GOES HERE

<todo>

## Setup

1. Deploy the **ai-assistant** artifact in the same application with **Portal**.
2. Start the engine, login to Portal.
3. On the header of Portal, click on the **AI Assistant** icon to access the **AI Assistant** app.

### User guide

#### AI Management
The AI Management page serves as a central hub for configuring and managing all aspects of your AI system. From here, you can:

- [Configure AI models](#configure-ai-models): Change settings for AI models such as API Key that power your applications.
- [Manage AI assistants](#manage-ai-assistants): Oversee and modify the AI assistants, including change visualization, personality, adjusting their behavior, and  managing the functions they can handle.
- [Manage AI functions](#manage-ai-functions): Control and organize various AI functions, defining the actions your AI can perform and how it interacts with users to provide accurate and efficient results.

##### Configure AI Models

<todo>

##### Manage AI Assistants

<todo>

##### Manage AI Functions

<todo>

### Developer guide

#### AI Flow

##### Empowering Intelligent Task Automation

To enable AI Assistants to handle complex tasks with sophisticated logic, Axon Ivy introduced **AI Flow** - an advanced AI workflow framework designed to streamline how AI processes and operates.

**AI Flow** allows users to:

- Seamlessly interact with the Ivy system, ensuring efficient integration.
- Accurately detect, interpret, and handle user requests.
- Maintain simplicity while offering flexible control and easy extensibility for evolving needs.
- Manage access to AI functions
- This framework is built to empower users to design and manage AI workflows effectively, enabling a smarter, more adaptable AI experience.

##### Real world problem

Imagine you want to develop a feature that allows HR employees to easily find information about staff based on criteria like name, date of birth, branch, or position.

In the past, this would require building a search page with multiple filters (e.g., name, date of birth), where HR employees manually filter out the desired information from a data table.

However, in the AI era, you want a smarter solution. An AI-powered function can help HR employees perform these tasks more efficiently using natural language queries like "list all web developers in Boston" or "find the info of Sandy, who has a birthday this Thursday."

That's where AI Flow comes in. It not only speeds up the search process but also helps with tasks like correcting typos and validating illogical data, such as preventing searches for employees with future birthdays!

##### How it works
AI Flow operates as a workflow framework in the form of JSON. It consists of multiple AI Steps, each of which is linked together based on user-defined configurations.

Basic attributes of an AI Flows:

``` json
{
    "version": "11.4.0",
    "id": "find-employees-flow",
    "name": "Find employees information",
    "type": "FLOW",
    "permissions": ["HR_Employee"],
    "description": "Find employees information",
    "usage": "Use this flow when user want to find information of employees",
    "steps": []
}
```

- **version**: Specifies the version of the AI Flow, which must align with the Ivy version.

- **id**: The unique identifier for the AI Flow.

- **name**: The name of the AI Flow.

- **type**: Always set to "FLOW". The AI Assistant can access various tools like Ivy tools, Knowledge Base tools, and AI Flows. By defining the type as "FLOW," you indicate that this tool is an AI Flow, allowing the AI Assistant to use it correctly.

- **permissions**: Defines the roles or usernames of users authorized to use this AI Flow.

- **description**: A detailed explanation of the AI Flow. The more thorough the description, the better the AI can understand how to use the AI Flow.

- **usage**: Specifies when to use the AI Flow. A clearer explanation ensures the AI can accurately select the appropriate flow to fulfill user requests.

- **steps**: Lists the AI Steps that the AI Flow should execute to handle the user's request. Available step types:
   - **Switch**: decision-making element that guide AI in selecting the appropriate next action based on specific conditions.
   - **Ivy Tool**: directs AI to use specific Ivy tools (Ivy callable) in its decision-making process.
   - **Text**: display or generate text-based content for user interaction.
   - **Re-phrase**: help AI refine user input before executing specific actions or using tools.
   - **Trigger Flow**: initiates a new flow within the AI process, either by passing a specific trigger message or using the result of a previous step. This allows for seamless transitions between different workflows and the ability to pass relevant data between them.

To learn more about the AI Steps, please refer to [AI Step](#ai-step)

##### Solve the real world problem

To solve the real world problem above, please follow these steps:

1. Create an Ivy Callable process which has input parameters are the criteria to find employee

| Name | Type | Decription |
| --- | --- | --- |
| `name` | String | Name of the employee |
| `birthday` | String | Employee's date of birth |
| `branch` | String | Company branch which the employee working at |
| `position` | String | Position of the employee in the company |

And output result must be an AI Result DTO object

| Name | Type | Decription |
| --- | --- | --- |
| `result` | com.axonivy.portal.components.dto.AiResultDTO | Result for the AI Assistant |

2. In the variable file **AiFunctions.json** add an Ivy tool which will interact with the callable process above to query for list of employees

```json
{
    "version": "11.4.0",
    "id": "find-employess",
    "name": "Find employees",
    "type": "IVY",
    "signature": "findEmployees(String,String,String,String)",
    "permissions": [ "HR_employees" ],
    "description": "Find employess by name, date of birth, branch, position.",
    "usage": "This tool is helpful when user want to find employess by name, date of birth, branch, position.",
    "attributes": [
        {
            "name": "name",
            "description": "Name of the employee"
        },
        {
            "name": "birthday",
            "description": "Employee's date of birth"
        },
        {
            "name": "branch",
            "description": "Company branch which the employee working at."
        },
        {
            "name": "position",
            "description": "position of the employee in the company."
        }
    ]
}
```

> [!IMPORTANT]
> Please keep in mind:
> - the name of the attributes must be same as name of parameters of the callable process above.
> - `signature` attribute in the JSON object is the signature of the callable process.

3. In the variable file **AiFunctions.json** add an AI Flow to handle the request to find employees from user.

This is an example of a simple AI Flow with 4 steps:
- Step 0: Rephrase the request of user to make it align with the Ivy tool `find-employees`
- Step 1: Call the Ivy Tool, using the rephrased request at **Step 0** as input
    - If has error or cannot find any employee matched the request, show an error (**Step 2**)
    - If success: Show the result (**Step 3**)
- Step 2: Show an message to user then end the flow.
- Step 3: Show information of found employees in a well-structured format then end the flow.

And this is the AI flow:

```json
{
    "version": "11.4.0",
    "id": "find-employees-flow",
    "name": "Find employees information",
    "type": "FLOW",
    "permissions": [ "HR_Employee" ],
    "description": "Find employees information",
    "usage": "Use this flow when user want to find information of employees",
    "steps": [
        {
            "stepNo": 0,
            "type": "RE_PHRASE",
            "toolId": "find-employees",
            "onRephrase": 1,
            "onSuccess": 1,
            "examples": [
            {
                "before": "list all web developers in Boston",
                "after": "find employees in branch 'Boston'"
            },
            {
                "before": "find the info of Sandy, who has a birthday this Thursday",
                "after": "find employees has firstName = 'Sandy' and dateOfBirth = '12/09/2024'"
            }
            ]
        },
        {
            "stepNo": 1,
            "type": "IVY_TOOL",
            "toolId": "find-employees",
            "onSuccess": 3,
            "onError": 2
        },
    {
        "stepNo": 2,
        "type": "TEXT",
        "text": "Sorry, I cannot find any employee matched your request.",
        "useConversationMemory": false,
        "onSuccess": -1
    },
    {
        "stepNo": 3,
        "type": "TEXT",
        "useAI": true,
        "customInstruction": "AI found employees, please read and show them to user with a well-structured format.",
        "onSuccess": -1
    }
    ]
}
```

4. Add the `find-employees-flow` to the toolkit of your AI Assistant and use it.

Below is an example how the conversation look like when user using the `find-employees-flow`:

**legends:**
$\colorbox{#fedec8}{{\space\space}}$: User message
$\colorbox{#c6edfb}{{\space\space}}$: The message that AI show on the screen
$\colorbox{#d3f1a7}{{\space\space}}$: The message that AI talking to itself and don't show to user

**conversation:**
$\colorbox{#fedec8}{{User: Hello AI}}$
$\colorbox{#c6edfb}{{AI: Hi User}}$
$\colorbox{#fedec8}{{User: I want to find Sandy, she is a web developer work for our Munich office}}$
$\colorbox{#d3f1a7}{{AI: find employee has name ‘Sandy’, position ‘web developer’, and work at branch ‘Munich’}}$
$\colorbox{#d3f1a7}{{AI: <use Ivy Tool find-employees to find employees>}}$
$\colorbox{#d3f1a7}{{AI: [ {“name”: “Sandy Williams“, “branch”: “munich”,”position”: “Web Developer”, “rank”:“Senior”, “email”: “sandyw@localhost.com”}]}}$
$\colorbox{#d3f1a7}{{AI: [ {“name”: “Sandy Brown“, “branch”: “munich”,”position”: “Web Developer”, “rank”:“Junior”, “email”: “sandyb@localhost.com”}]}}$
$\colorbox{#c6edfb}{{AI: I found 2 employees matched your request:}}$
$\colorbox{#c6edfb}{{Sandy Williams: Senior Web developer, email: sandyw@localhost.com,  branch: Munich}}$
$\colorbox{#c6edfb}{{AI: [ {“name”: “Sandy Brown“, “branch”: “munich”,”position”: “Web Developer”, “rank”:“Junior”, “email”: “sandyb@localhost.com”}]}}$
$\colorbox{#d3f1a7}{{AI: <end the flow>}}$

##### AI Step

###### Attributes

- **stepType**: type of step. Valid values:
   - IVY_TOOL: [Ivy tool step](#ivy-tool-step).
   - SWITCH: [Switch step](#switch-step).
   - TEXT: Text step.
   - RE_PHRASE: Rephrase step.
   - TRIGGER_FLOW: Trigger flow step.

- **stepNo**: Number of step in the flow.

- **result**: Result of a step (Refer [AI Result DTO](#ai-result-dto) ).

- **onSuccess**: The step that will be execute if this step is run successfully.

- **onError**: The step that will be execute if we got trouble when running this step.

- **useConversationMemory**: Set to true to include all chat messages of the conversation when running the step. Otherwise the AI step only include chat messages of the running AI flow.

- **saveToHistory**: Set to false to exclude the message from conversation history, only save to the memory.

- **customInstruction**: instruction for a specific requirement for AI.

##### Switch step

The **Switch step** is a decision-making element designed to guide AI in selecting the appropriate next action based on specific conditions. It functions by evaluating a list of predefined cases, each representing a potential scenario the AI might encounter. Based on the case that matches the current situation, the AI chooses the corresponding action to take.

In the provided structure, the AI examines the case descriptions within the list and assigns the correct action number to proceed. This allows the AI to dynamically adjust its behavior based on different outcomes or states, ensuring a tailored response for various circumstances.

- **cases**: A list of possible scenarios with corresponding actions.

```json
{
    "stepNo": 3,
    "type": "SWITCH",
    "cases": [
        { "action": 5, "case": "cannot find any tasks" },
        { "action": 4, "case": "found multiple tasks" },
        { "action": 1, "case": "found only one tasks" }
    ]
}
```

##### Ivy tool step

The **Ivy tool step** is a specialized instruction mechanism that directs AI to use specific tools or functions in its decision-making process. This step ensures that the AI interacts with predefined tools (referred to by their toolId) and executes tasks according to the defined conditions and custom instructions. It enables the AI to perform specialized actions and provides flexibility through optional parameters such as success, error handling, and custom instructions.

- **toolId**: Refers to the ID of a tool from the list of available AI functions (as described in the variable AiFunctions), such as "find-tasks" or "find-web-developer".

```json
{
    "stepNo": 1,
    "type": "IVY_TOOL",
    "toolId": "find-tasks",
    "onSuccess": -1
}
```

- **customInstruction**: Provides specific instructions for the AI to follow in executing the tool. For instance, the AI might be directed to "Find employees who have the role 'WEB_DEVELOPER'".

```json
{
    "stepNo": 1,
    "type": "IVY_TOOL",
    "toolId": "find-web-developer",
    "onSuccess": 3,
    "onError": 2,
    "customInstruction": "Find employees has role 'WEB_DEVELOPER'.",
    "saveToHistory": false
}
```

##### Text step

The **Text Step** is a crucial component in AI workflows designed to display or generate text-based content for user interaction. Depending on its configuration, the Text Step can show fixed messages, AI-generated content, results of prior steps, or even hidden messages for internal AI processing. This flexibility enables the AI to communicate effectively with users while guiding decision-making processes.

**Fixed text**

- **text**: The fixed text to be displayed on the UI.

- **showResultOfStep**: Option to display the result of a previous step by referencing its number.

- **onSuccess**: Defines the next step when the user provides input after reading the text.

```json
{
    "stepNo": 2,
    "type": "TEXT",
    "text": "I have rephrased your request as follows. Could you please confirm if it is correct?",
    "showResultOfStep": 0,
    "onSuccess": 3
}
```

**AI generated**

- **useAI**: Set to true to allow AI to generate content dynamically, such as summaries or reports.

- **customInstruction**: A guiding instruction to help the AI generate appropriate text based on context or user inputs

- **onSuccess**: The next step to execute after user interaction.

```json
{
    "stepNo": 1,
    "type": "TEXT",
    "useAI": true,
    "customInstruction": "Use the conversation above to summarize information of the planned project in a structured format. If user didn't provide a name for the project generete the project name based on description of the project. Example: '**Project name:** ProjectA\n**Project description:** description of projectA\n**Technologies:** tech stack\n**Members:** show member information'. Then add a line to ask if need some update",
    "onSuccess": 2
},
```

**Show result of other step**

- **showResultOfStep**: Displays the result from a previously executed step.

```json
{
    "stepNo": 1,
    "type": "IVY_TOOL",
    "toolId": "find-employees",
    "onSuccess": 3,
    "onError": 2,
    "customInstruction" : "Find employees has the tech stack are the technologies above.",
    "useConversationMemory": true,
    "saveToHistory": false
},
{
    "stepNo": 3,
    "type": "TEXT",
    "text": "I found these suitable employees matched the requirement. Do you want to choose some of them for the project?",
    "showResultOfStep" : 1,
    "useConversationMemory": false,
    "onSuccess": 4
}
```

**Hidden**

- **isHidden**: Set to true to hide the text from the user but allow the AI to read and process it. This is useful when generating content that the AI needs for further processing without displaying it on the UI.

```json
{
    "stepNo": 13,
    "type": "TEXT",
    "useAI": true,
    "customInstruction": "Summarize the project plan above.",
    "useConversationMemory": true,
    "onSuccess": 14,
    "isHidden": true
},
{
    "stepNo": 14,
    "type": "TRIGGER_FLOW",
    "flowId": "create-project-flow",
    "showResultOfStep": 13,
    "useConversationMemory": false
}
```

##### Re-phrase step

The **Rephrase step** is designed to help AI refine user input before executing specific actions or using tools. This is particularly useful when the user's message is vague, incomplete, or not structured in a way that the AI can immediately process. By rephrasing the input, the AI ensures that the information is more precise, making it easier to use with target tools or functions.

- **toolId**: Refers to the tool the AI will use as the target. AI should use JSON schema of the corresponding tool to rephrase the message. By defining this attribute, you ensures that the rephrased input is compatible with the tool’s requirements.
    - Example:
        - user input “find my sick leave task”
        - You have a tool to find task by name, description, priority,… but user didn’t tell you that which field he want to use. Therefore you should rephrase the message before use it with the ivy tool.
        - → find task with name ‘sick leave’

- **customInstruction**: Provides specific guidelines for how the AI should rephrase the message. This helps the AI handle particular cases such as abstract terms or dates, formatting them into more useful data.

- **onRephrase**: Defines the next step to execute if the AI determines that it needs to rephrase the message.

- **onSuccess**: Specifies the step to run if the AI does not need to rephrase the message.

- **examples**: A list of predefined examples that guide the AI in understanding how to rephrase user messages. Each example consists of:
    - before: The original, unstructured message from the user.
    - after: The rephrased message that the AI would generate for better clarity and actionability

```json
{
    "stepNo": 0,
    "type": "RE_PHRASE",
    "useConversationMemory": true,
    "toolId": "find-processes",
    "onRephrase": 3,
    "onSuccess": 1,
    "customInstruction": "If in the message has an abstract date such as today, tomorrow,..., please format it. Example: today = 31, July 2024",
    "examples": [
        {
            "before": "find leve request process",
            "after": "find process that help creating leave request"
        },
        {
            "before": "find process leave request",
            "after": "find processes that have name 'leave request'"
        },
        {
            "before": "find process 123",
            "after": "find processes that the id is '123' or the name is '123'"
        }
    ]
}
```

##### Trigger flow step

The **Trigger flow step** initiates a new flow within the AI process, either by passing a specific trigger message or using the result of a previous step. This allows for seamless transitions between different workflows and the ability to pass relevant data between them.

- **flowId**: ID of the flow you want to trigger

**Trigger with trigger message**

- **triggerMessage**: The custom message that serves as input to the new flow, guiding the AI on what to do next.

```json
{
    "stepNo": 15,
    "type": "TRIGGER_FLOW",
    "flowId": "choose-member-flow",
    "triggerMessage": "I want to choose members for my project described above"
}
```

**Trigger with result of other step**

- **showResultOfStep**: Passes the result of a specific preceding step as the trigger message for the new flow. This option is useful when you want to pass result of a flow to another, or the message you want to pass to the new step is get from the system. 

```json
{
    "stepNo": 14,
    "type": "TRIGGER_FLOW",
    "flowId": "create-project-flow",
    "showResultOfStep": 13,
    "useConversationMemory": false
}
```

##### AI Result DTO

###### Introduction

The Result DTO ensures that the AI Assistant provides reliable and consistent results by adhering to a standardized structure for all outputs, promoting efficiency and clarity across AI interactions.

- Project: portal-component
- Class: com.axonivy.portal.components.dto.AiResultDTO

**Attributes**

| Name | Type | Decription |
| --- | --- | --- |
| result | String | result to show for user |
| resultForAI | String | result for AI model |
| state | com.axonivy.portal.components.enums.AIState | state of the result (DONE, ERROR) |