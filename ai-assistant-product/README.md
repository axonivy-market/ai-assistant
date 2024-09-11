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

##### AI Step