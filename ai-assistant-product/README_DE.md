# ai-assistant

<todo>

## Demo

### Demoprojekt

Im **ai-assistant-demo** Ivy-Projekt haben wir Demo-Assistenten erstellt, die Ihnen helfen können, Ihren eigenen KI-Assistenten effizienter zu verstehen und zu entwickeln.

> [!IMPORTANT]
> Dieses Demoprojekt wird Ivy-Benutzer und -Rollen erstellen und die Hauptvariablen des KI-Assistenten überschreiben. Es wird daher DRINGEND EMPFOHLEN, es auf einer Ivy-Engine im Demomodus auszuführen, um Ihre Daten zu schützen.
>
> Wenn Sie dieses Demo in einer Produktionsumgebung ausführen möchten, stellen Sie bitte sicher, dass Sie alle Ivy-Variablen, die mit dem KI-Assistenten in Verbindung stehen, sichern und nach Abschluss des Demos wiederherstellen.

#### Komplexe Demo

**Anwendungsfall**

Der Benutzer kann ein Softwareprojekt mit dem KI-Assistenten planen. Der KI-Assistent ermöglicht es dem Benutzer, ein Softwareprojekt effizient zu planen, indem er Informationen in ein strukturiertes Format organisiert. Er kann:

- Das Projekt erstellen und in der Datenbank speichern
- Bei der Suche und Auswahl geeigneter Teammitglieder für das Projekt unterstützen
- Dem Benutzer bei der Vorbereitung des Kick-off-Meetings für das Projekt helfen
- Aufgaben erstellen und zuweisen, um Teammitglieder zu dem Meeting einzuladen

**Wie man es verwendet**

1. Führen Sie den Prozess `startComplexDemo` aus, um Testdaten zu erstellen und den ursprünglichen KI-Assistenten durch den **Complex Demo Assistant** zu ersetzen

2. Öffnen Sie im **Portal** die KI-Assistenten-App

3. Jetzt können Sie den Assistenten verwenden, um ein Softwareprojekt zu planen und zu erstellen, indem Sie die Details des Projekts eingeben, z. B. die Anzahl der Mitglieder oder die Technologien.

Beispiel:

`The XYZ Solutions Web Development Project aims to build a state-of-the-art web application that enhances user experience and supports the company's growth objectives. Mainly use basic web technologies such as HTML and CSS, the project will deliver a secure, scalable, and efficient platform that aligns with industry standards and best practices. We need 3 engineer, 1 web designer and 2 tester for it.`

#### Fehlerbehandlungs-Demo

**Anwendungsfall**

Es gibt keine Daten für Besprechungsräume. Daher wird der KI-Assistent immer dann, wenn der Benutzer nach einem Besprechungsraum sucht, einen Fehler anzeigen. Dies ist ein einfaches Beispiel dafür, wie der KI-Assistent Fehler ordnungsgemäß behandeln kann.

**Wie man es verwendet**

1. Führen Sie den Prozess `startErrorHandlingDemo` aus, um Testdaten zu erstellen und den ursprünglichen KI-Assistenten durch den **Error Handling Demo Assistant** zu ersetzen.

2. Öffnen Sie im **Portal** die KI-Assistenten-App.

3. Jetzt können Sie die Demo starten, indem Sie Informationen zu einem Raum mit dem **Error Handling Demo Assistant** anfordern.

Beispiel:

`I want to find meeting room C`

## Einrichtung

1. Deployen Sie das **ai-assistant** Artefakt in derselben Anwendung wie **Portal**.

2. Starten Sie die Engine und melden Sie sich im Portal an.

3. Klicken Sie in der Kopfzeile des Portals auf das **KI-Assistent** Symbol, um auf die **AI Assistant** App zuzugreifen.

### Benutzerhandbuch

#### KI-Management

Die Seite KI-Management dient als zentrale Anlaufstelle für die Konfiguration und Verwaltung aller Aspekte Ihres KI-Systems. Von hier aus können Sie:

- [KI-Modelle konfigurieren](#ki-modelle-konfigurieren): Ändern Sie Einstellungen für KI-Modelle, wie z. B. den API-Schlüssel, der Ihre Anwendungen unterstützt.

- [KI-Assistenten verwalten](#ki-assistenten-verwalten): Überwachen und modifizieren Sie die KI-Assistenten, einschließlich der Änderung der Visualisierung, Persönlichkeit, Anpassung ihres Verhaltens und Verwaltung der Funktionen, die sie ausführen können.

- [KI-Funktionen verwalten](#ki-funktionen-verwalten): Kontrollieren und organisieren Sie verschiedene KI-Funktionen, definieren Sie die Aktionen, die Ihre KI ausführen kann, und wie sie mit Benutzern interagiert, um genaue und effiziente Ergebnisse zu liefern.

##### KI-Modelle konfigurieren

##### KI-Assistenten verwalten

##### KI-Funktionen verwalten

#### Wissensdatenbanken erstellen

Der KI-Assistent ermöglicht es Administratorbenutzern (mit der Rolle `AXONIVY_PORTAL_ADMIN`), Wissensdatenbanken zu erstellen, die von der KI-Funktion des Typs `Wissensdatenbank` verwendet werden können.
Starten Sie einfach den Prozess `Wissensdatenbank für KI-Assistenten erstellen` und folgen Sie den Anweisungen dort, um Wissensdatenbanken zu erstellen, die von Funktionen des Typs `Wissensdatenbank` genutzt werden können.

![Wissensdatenbank erstellen UI](./doc/img/create-knowledge-page-ui.png)

Derzeit können Sie Wissensdatenbanken für zwei Typen erstellen: Portal-Unterstützung und Sonstiges.

##### Wissensdatenbank: Portal-Unterstützung

Der AI-Assistent enthält ein integriertes Tool namens `Portal support`, das Fragen zum Axon Ivy Portal beantworten kann.

Um die Wissensdatenbank für diese Funktion zu erstellen, besuchen Sie bitte die [Portal-Downloadseite](https://market.axonivy.com/portal) auf dem Axon Ivy Market und laden Sie das neueste Dokument herunter, wie im untenstehenden Bild gezeigt.

![Portal-Dokument herunterladen](./doc/img/download-portal-doc.png)

Anschließend laden Sie die heruntergeladene Datei einfach wie in  [Wissensdatenbanken erstellen](#wissensdatenbanken-erstellen) oben beschrieben hoch.

Nach dem Hochladen der ZIP-Datei warten Sie bitte einige Minuten, bis sich das Upload-Fenster schließt. Dies kann einige Minuten dauern, da der AI-Assistent Zeit benötigt, um die Aufgabe zu erledigen.

##### Wissensdatenbank: Sonstiges

Bevor Sie andere Wissensdatenbanken hochladen, beachten Sie bitte:

1. Der Name der hochgeladenen Datei wird die ID eines Indexes im Vektorspeicher sein, daher:

    - Der Name muss strikt dem dash-case-Format folgen, sonst treten Fehler auf, wenn der AI-Assistent die Wissensdatenbank erstellt.

    - Der Name muss einzigartig sein, sonst überschreiben Sie eine bestehende Wissensdatenbank!

2. Alle Dateien, die Sie in die ZIP-Datei einfügen, müssen Textdateien (Typ `.txt`) sein. Der AI-Assistent wird andere Dateitypen beim Einlesen des Inhalts zur Erstellung der Wissensdatenbank überspringen.

3. Von Axon Ivy erstellte Vektorspeicher haben das Präfix `axon-ivy-vector-store`, gefolgt vom Namen der hochgeladenen Datei. Wenn Sie beispielsweise eine Datei mit dem Namen `customer-support.zip` hochladen, lautet die resultierende Vektorspeicher-ID `axon-ivy-vector-store-customer-support`

Nach dem Hochladen der ZIP-Datei warten Sie bitte einige Minuten, bis sich das Upload-Fenster schließt. Dies kann einige Minuten dauern, da der AI-Assistent Zeit benötigt, um die Aufgabe zu erledigen.

### Entwicklerhandbuch

#### AI Flow

##### Intelligente Aufgabenautomatisierung fördern

Um KI-Assistenten in die Lage zu versetzen, komplexe Aufgaben mit ausgeklügelter Logik zu bewältigen, hat Axon Ivy **AI Flow** eingeführt – ein fortschrittliches KI-Workflow-Framework, das darauf ausgelegt ist, die Art und Weise, wie KI-Prozesse ablaufen, zu optimieren.

**AI Flow** ermöglicht es Benutzern:

- Nahtlos mit dem Ivy-System zu interagieren und eine effiziente Integration sicherzustellen.
- Benutzeranfragen präzise zu erkennen, zu interpretieren und zu bearbeiten.
- Einfachheit beizubehalten und gleichzeitig flexible Kontrolle und einfache Erweiterbarkeit für sich ändernde Anforderungen zu bieten.
- Den Zugriff auf KI-Funktionen zu verwalten.
- Dieses Framework wurde entwickelt, um Benutzer dabei zu unterstützen, KI-Workflows effektiv zu entwerfen und zu verwalten und ein intelligenteres, anpassungsfähigeres KI-Erlebnis zu ermöglichen.

##### Reales Weltproblem

Stellen Sie sich vor, Sie möchten eine Funktion entwickeln, die es HR-Mitarbeitern ermöglicht, Informationen über Mitarbeiter anhand von Kriterien wie Name, Geburtsdatum, Niederlassung oder Position leicht zu finden.

Früher hätte dies den Aufbau einer Suchseite mit mehreren Filtern (z. B. Name, Geburtsdatum) erfordert, auf der HR-Mitarbeiter die gewünschten Informationen manuell aus einer Datentabelle herausfiltern mussten.

Im Zeitalter der KI möchten Sie jedoch eine intelligentere Lösung. Eine KI-gestützte Funktion kann HR-Mitarbeitern dabei helfen, diese Aufgaben effizienter mit natürlichen Sprachabfragen wie „Liste alle Webentwickler in Boston auf“ oder „Finde die Informationen zu Sandy, die diesen Donnerstag Geburtstag hat“ zu erledigen.

Hier kommt AI Flow ins Spiel. Es beschleunigt nicht nur den Suchprozess, sondern hilft auch bei Aufgaben wie der Korrektur von Tippfehlern und der Validierung unlogischer Daten, beispielsweise indem es verhindert, dass nach Mitarbeitern mit zukünftigen Geburtstagen gesucht wird!

##### So funktioniert es

AI Flow funktioniert als Workflow-Framework in Form von JSON. Es besteht aus mehreren AI-Schritten, die jeweils auf benutzerdefinierten Konfigurationen basierend miteinander verknüpft sind.

Grundlegende Attribute eines AI Flow:

``` json
{
    "version": "12.0.0",
    "id": "find-employees-flow",
    "name": "Find employees information",
    "type": "FLOW",
    "permissions": ["HR_Employee"],
    "description": "Find employees information",
    "usage": "Use this flow when user want to find information of employees",
    "steps": []
}
```

- **version**: Gibt die Version des AI Flows an, die mit der Ivy-Version übereinstimmen muss.

- **id**: Der eindeutige Bezeichner für den AI Flow.

- **name**: Der Name des AI Flows.

- **type**: Muss immer auf "FLOW" gesetzt sein. Der AI-Assistent kann auf verschiedene Tools wie Ivy-Tools, Wissensdatenbank-Tools und AI Flows zugreifen. Durch die Festlegung des Typs als "FLOW" wird angegeben, dass dieses Tool ein AI Flow ist, sodass der AI-Assistent es korrekt verwenden kann.

- **permissions**: Definiert die Rollen oder Benutzernamen der Nutzer, die berechtigt sind, diesen AI Flow zu verwenden.

- **description**: Eine ausführliche Erklärung des AI Flows. Je detaillierter die Beschreibung ist, desto besser kann die KI verstehen, wie der AI Flow verwendet werden soll.

- **usage**: Gibt an, wann der AI Flow verwendet werden soll. Eine klarere Erklärung stellt sicher, dass die KI den entsprechenden Flow genau auswählen kann, um Benutzeranfragen zu erfüllen.

- **steps**: Listet die AI-Schritte auf, die der AI Flow ausführen soll, um die Anfrage des Benutzers zu bearbeiten. Verfügbare Schrittarten:

    - **Switch**: Entscheidungselement, das der KI hilft, die geeignete nächste Aktion basierend auf bestimmten Bedingungen auszuwählen.

    - **Ivy** Tool: Weist die KI an, bestimmte Ivy-Tools (Ivy callable) im Entscheidungsprozess zu verwenden.

    - **Text**: Zeigt textbasierte Inhalte an oder generiert diese für die Benutzerinteraktion.

    - **Re-phrase**: Hilft der KI, die Benutzereingaben zu verfeinern, bevor spezifische Aktionen ausgeführt oder Tools verwendet werden.

    - **Trigger Flow**: Startet einen neuen Flow innerhalb des KI-Prozesses, entweder durch Übermittlung einer spezifischen Auslöse-Nachricht oder durch Nutzung des Ergebnisses eines vorherigen Schritts. Dies ermöglicht nahtlose Übergänge zwischen verschiedenen Workflows und die Weitergabe relevanter Daten zwischen ihnen.

> [!TIP]
> Um mehr über die AI-Schritte zu erfahren, lesen Sie bitte [AI-Schritt](#ai-schritt).

> [!TIP]
> Um zu erfahren, wie Sie Ihren eigenen AI Flow erstellen können, lesen Sie bitte [AI Flow-Demo](#erstellen-sie-ihren-eigenen-ai-flow).

##### AI-Schritt

###### Attribute

- **stepType**: Art des Schritts. Gültige Werte:
    - IVY_TOOL: [Ivy-Tool-Schritt](#ivy-tool-schritt).
    - SWITCH: [Switch-Schritt](#switch-schritt).
    - TEXT: [Text-Schritt](#text-schritt).
    - RE_PHRASE: [Rephrase-Schritt](#rephrase-schritt).
    - TRIGGER_FLOW: [Trigger-Flow-Schritt](#trigger-flow-schritt).
    - KNOWLEDGE_BASE: [Knowledge-Base-Schritt](#trigger-flow-schritt).

- **stepNo**: Nummer des Schritts im Flow.

- **result**: Ergebnis eines Schritts (siehe AI Result DTO).

- **onSuccess**: Der Schritt, der ausgeführt wird, wenn dieser Schritt erfolgreich ausgeführt wird.

- **onError**: Der Schritt, der ausgeführt wird, wenn bei der Ausführung dieses Schritts ein Problem auftritt.

- **useConversationMemory**: Auf „true“ setzen, um alle Chat-Nachrichten des Gesprächs beim Ausführen des Schritts einzubeziehen. Andernfalls werden nur die Chat-Nachrichten des laufenden AI Flows einbezogen.

- **saveToHistory**: Auf „false“ setzen, um die Nachricht von der Gesprächshistorie auszuschließen, sie wird nur im Speicher gespeichert.

- **customInstruction**: Anweisung für eine spezifische Anforderung an die KI.

##### Switch-Schritt

Der **Switch-Schritt** ist ein Entscheidungselement, das darauf ausgelegt ist, der KI zu helfen, die geeignete nächste Aktion basierend auf spezifischen Bedingungen auszuwählen. Er funktioniert, indem er eine Liste vordefinierter Fälle auswertet, von denen jeder ein potenzielles Szenario darstellt, dem die KI begegnen könnte. Basierend auf dem Fall, der mit der aktuellen Situation übereinstimmt, wählt die KI die entsprechende Aktion aus.

In der bereitgestellten Struktur prüft die KI die Fallbeschreibungen innerhalb der Liste und weist die richtige Aktionsnummer zu, um fortzufahren. Dadurch kann die KI ihr Verhalten dynamisch an verschiedene Ergebnisse oder Zustände anpassen und stellt sicher, dass eine maßgeschneiderte Reaktion für verschiedene Umstände erfolgt.

- **cases**: Eine Liste möglicher Szenarien mit entsprechenden Aktionen.

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

##### Ivy-Tool-Schritt

Der **Ivy-Tool-Schritt** ist ein spezialisiertes Anweisungsmechanismus, der die KI anweist, bestimmte Werkzeuge oder Funktionen in ihrem Entscheidungsprozess zu verwenden. Dieser Schritt stellt sicher, dass die KI mit vordefinierten Tools (bezeichnet durch ihre toolId) interagiert und Aufgaben gemäß den festgelegten Bedingungen und benutzerdefinierten Anweisungen ausführt. Er ermöglicht der KI, spezialisierte Aktionen auszuführen, und bietet Flexibilität durch optionale Parameter wie Erfolg, Fehlerbehandlung und benutzerdefinierte Anweisungen.

- **toolId**: Bezieht sich auf die ID eines Werkzeugs aus der Liste der verfügbaren KI-Funktionen (wie in der Variablen AiFunctions beschrieben), zum Beispiel "find-tasks" oder "find-web-developer".

```json
{
    "stepNo": 1,
    "type": "IVY_TOOL",
    "toolId": "find-tasks",
    "onSuccess": -1
}
```

- **customInstruction**: Bietet spezifische Anweisungen, die die KI bei der Ausführung des Werkzeugs befolgen soll. Zum Beispiel könnte die KI angewiesen werden, "Find employees has role 'WEB_DEVELOPER'.' zu finden".

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

##### Text-Schritt

Der **Text-Schritt** ist eine zentrale Komponente in KI-Workflows, die darauf ausgelegt ist, textbasierte Inhalte für die Benutzerinteraktion anzuzeigen oder zu generieren. Abhängig von seiner Konfiguration kann der Text-Schritt feste Nachrichten, KI-generierte Inhalte, Ergebnisse vorheriger Schritte oder sogar versteckte Nachrichten für die interne KI-Verarbeitung anzeigen. Diese Flexibilität ermöglicht es der KI, effektiv mit den Benutzern zu kommunizieren und gleichzeitig Entscheidungsprozesse zu steuern.

**Fester Text**

- **text**: Der feste Text, der in der Benutzeroberfläche angezeigt werden soll.

- **showResultOfStep**: Option, das Ergebnis eines vorherigen Schritts anzuzeigen, indem auf dessen Nummer verwiesen wird.

- **onSuccess**: Definiert den nächsten Schritt, wenn der Benutzer nach dem Lesen des Textes eine Eingabe macht.

```json
{
    "stepNo": 2,
    "type": "TEXT",
    "text": "I have rephrased your request as follows. Could you please confirm if it is correct?",
    "showResultOfStep": 0,
    "onSuccess": 3
}
```

**KI-generiert**

- **useAI**: Auf „true“ setzen, um der KI zu erlauben, Inhalte dynamisch zu generieren, wie beispielsweise Zusammenfassungen oder Berichte.

- **customInstruction**: Eine Leit-Anweisung, um der KI zu helfen, passenden Text basierend auf dem Kontext oder den Benutzereingaben zu erstellen.

- **onSuccess**: Der nächste Schritt, der nach der Benutzerinteraktion ausgeführt werden soll.

```json
{
    "stepNo": 1,
    "type": "TEXT",
    "useAI": true,
    "customInstruction": "Use the conversation above to summarize information of the planned project in a structured format. If user didn't provide a name for the project generete the project name based on description of the project. Example: '**Project name:** ProjectA\n**Project description:** description of projectA\n**Technologies:** tech stack\n**Members:** show member information'. Then add a line to ask if need some update",
    "onSuccess": 2
},
```

**Ergebnis eines anderen Schritts anzeigen**

- **showResultOfStep**: Zeigt das Ergebnis eines zuvor ausgeführten Schritts an.

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

**Versteckt**

- **isHidden**: Auf „true“ setzen, um den Text vor dem Benutzer zu verbergen, aber der KI zu erlauben, ihn zu lesen und zu verarbeiten. Dies ist nützlich, wenn Inhalte generiert werden, die die KI für die weitere Verarbeitung benötigt, ohne sie in der Benutzeroberfläche anzuzeigen.

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

##### Rephrase-Schritt

Der **Rephrase-Schritt** ist dazu gedacht, der KI zu helfen, Benutzereingaben zu verfeinern, bevor spezifische Aktionen ausgeführt oder Werkzeuge verwendet werden. Dies ist besonders nützlich, wenn die Nachricht des Benutzers unklar, unvollständig oder nicht in einer Weise strukturiert ist, die die KI sofort verarbeiten kann. Durch die Umformulierung der Eingabe stellt die KI sicher, dass die Informationen präziser sind und sich besser für die Verwendung mit Zielwerkzeugen oder -funktionen eignen.

- **toolId**: Bezieht sich auf das Werkzeug, das die KI als Ziel verwenden wird. Die KI sollte das JSON-Schema des entsprechenden Werkzeugs verwenden, um die Nachricht umzuformulieren. Durch die Definition dieses Attributs stellen Sie sicher, dass die umformulierte Eingabe mit den Anforderungen des Werkzeugs kompatibel ist.
    - Beispiel:
        - Benutzereingabe: „find my sick leave task“
        - Sie haben ein Werkzeug, um Aufgaben nach Name, Beschreibung, Priorität usw. zu finden, aber der Benutzer hat nicht angegeben, welches Feld er verwenden möchte. Daher sollten Sie die Nachricht umformulieren, bevor Sie sie mit dem Ivy-Werkzeug verwenden.
        - → „find task with name ‘sick leave’“
- **customInstruction**: Bietet spezifische Richtlinien dafür, wie die KI die Nachricht umformulieren soll. Dies hilft der KI, besondere Fälle wie abstrakte Begriffe oder Datumsangaben zu behandeln und sie in nützlichere Datenformate zu bringen.

- **onRephrase**: Definiert den nächsten Schritt, der ausgeführt wird, wenn die KI feststellt, dass die Nachricht umformuliert werden muss.

- **onSuccess**: Gibt den Schritt an, der ausgeführt werden soll, wenn die KI die Nachricht nicht umformulieren muss.

- **examples**: Eine Liste vordefinierter Beispiele, die der KI helfen, zu verstehen, wie Benutzernachrichten umformuliert werden sollen. Jedes Beispiel besteht aus:
    - **before**: Die ursprüngliche, unstrukturierte Nachricht des Benutzers.
    - **after**: Die umformulierte Nachricht, die die KI erzeugen würde, um mehr Klarheit und Handlungsfähigkeit zu gewährleisten.

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

##### Trigger-Flow-Schritt

Der **Trigger-Flow-Schritt** startet einen neuen Flow innerhalb des KI-Prozesses, entweder durch Übermittlung einer spezifischen Auslöse-Nachricht oder durch Nutzung des Ergebnisses eines vorherigen Schritts. Dies ermöglicht nahtlose Übergänge zwischen verschiedenen Workflows und die Weitergabe relevanter Daten zwischen ihnen.

- **flowId**: ID des Flows, den Sie auslösen möchten.

**Auslösen mit Auslöse-Nachricht**

- **triggerMessage**: Die benutzerdefinierte Nachricht, die als Eingabe für den neuen Flow dient und die KI anleitet, was als Nächstes zu tun ist.

```json
{
    "stepNo": 15,
    "type": "TRIGGER_FLOW",
    "flowId": "choose-member-flow",
    "triggerMessage": "I want to choose members for my project described above"
}
```

**Auslösen mit dem Ergebnis eines anderen Schritts**

- **showResultOfStep**: Übermittelt das Ergebnis eines bestimmten vorhergehenden Schritts als Auslöse-Nachricht für den neuen Flow. Diese Option ist nützlich, wenn Sie das Ergebnis eines Flows an einen anderen weitergeben möchten oder die Nachricht, die Sie an den neuen Schritt übergeben möchten, vom System abgerufen wird.

```json
{
    "stepNo": 14,
    "type": "TRIGGER_FLOW",
    "flowId": "create-project-flow",
    "showResultOfStep": 13,
    "useConversationMemory": false
}
```

##### Knowledge-Base-Schritt

Beim Arbeiten an einem Schritt können Benutzer Fragen haben, die nicht direkt mit dem Arbeitsablauf zusammenhängen. Wenn ein Benutzer beispielsweise eine Aufgabe nicht delegieren kann, möchte er möglicherweise wissen, warum die Delegation nicht möglich ist. Der **Knowledge-Base-Schritt** wurde entwickelt, um Benutzern zu helfen, schnell Antworten auf solche Fragen zu finden.

- **toolId**: Bezieht sich auf das Tool vom Typ `RETRIEVAL_QA`, das die KI als Knowledge Base zur Beantwortung der Fragen verwendet.

```json
{
    "stepNo": 3,
    "type": "KNOWLEDGE_BASE",
    "toolId": "portal-support",
    "onSuccess": -1,
    "onError": -1
}
```

##### KI-Ergebnis-DTO

###### Einführung

Das Ergebnis-DTO stellt sicher, dass der KI-Assistent zuverlässige und konsistente Ergebnisse liefert, indem es eine standardisierte Struktur für alle Ausgaben einhält, was Effizienz und Klarheit bei allen KI-Interaktionen fördert.

- Projekt: portal-component

- Klasse: com.axonivy.portal.components.dto.AiResultDTO

**Attribute**

| Name | Typ | Beschreibung |
| --- | --- | --- |
| result | String | Ergebnis, das dem Benutzer angezeigt wird |
| resultForAI | String | Ergebnis für das KI-Modell |
| state | com.axonivy.portal.components.enums.AIState | Status des Ergebnisses (DONE, ERROR) |

#### Erstellen Sie Ihren eigenen AI Flow

In diesem Abschnitt erklären wir, wie Sie Ihren eigenen AI Flow entwickeln können, wobei das [Reale Weltproblem](#reales-weltproblem) als Anwendungsfall dient.

> [!NOTE]
> In der [Komplexen Demo](#komplexe-demo) haben wir eine Funktion implementiert, um Mitarbeiterinformationen zu finden. Daher wird dringend empfohlen, diese Demo nicht in Verbindung mit dieser Anleitung zu verwenden.

1. Erstellen Sie ein Ivy-Projekt, das vom Projekt `portal-components` abhängt.

2. Erstellen Sie einen Ivy-Callable-Prozess mit Eingabeparametern, die die Kriterien zur Suche nach Mitarbeitern darstellen, mit der Signatur `findEmployeesInfo(String,String,String,String)`.

| Name | Typ | Beschreibung |
| --- | --- | --- |
| `name` | String | Name des Mitarbeiters |
| `birthday` | String | Geburtsdatum des Mitarbeiters |
| `branch` | String | Unternehmensniederlassung, in der der Mitarbeiter arbeitet |
| `position` | String | Position des Mitarbeiters im Unternehmen |

Das Ausgaberesultat des Ivy-Callable-Prozesses muss ein Objekt mit dem Namen result und dem Typ [KI-Ergebnis-DTO](#ki-ergebnis-dto) sein.

| Name | Typ | Beschreibung |
| --- | --- | --- |
| result | com.axonivy.portal.components.dto.AiResultDTO | Ergebnis für den KI-Assistenten |

> [!TIP]
> Sie müssen die Logik zur Mitarbeitersuche selbst implementieren. Zur Orientierung können Sie den Code in der [Komplexe Demo](#komplexe-demo) im **ai-assistant-demo** Ivy-Projekt untersuchen.

3. Fügen Sie in der Variablendatei **AiFunctions.json** ein Ivy-Tool hinzu, das mit dem oben genannten Callable-Prozess interagiert, um eine Liste von Mitarbeitern abzufragen.

```json
{
    "version": "12.0.0",
    "id": "find-employees-info",
    "name": "Find information of employees",
    "type": "IVY",
    "signature": "findEmployeesInfo(String,String,String,String)",
    "permissions": [ "Everybody" ],
    "description": "Find employees by name, date of birth, branch, position.",
    "usage": "This tool is helpful when user want to find employees by name, date of birth, branch, position.",
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

[!IMPORTANT]
> Bitte beachten Sie:
> - Die Namen der Attribute müssen mit den Namen der Parameter des oben genannten Callable-Prozesses übereinstimmen.
> - Das Attribut `signature` im JSON-Objekt ist die Signatur des Callable-Prozesses.

4. Fügen Sie in der Variablendatei **AiFunctions.json** einen AI Flow hinzu, um die Anfrage des Benutzers zur Suche nach Mitarbeitern zu bearbeiten.

Dies ist ein Beispiel für einen einfachen AI Flow mit 4 Schritten:

- Schritt 0: Formulieren Sie die Anfrage des Benutzers um, damit sie mit dem Ivy-Tool `find-employees-info` übereinstimmt.
- Schritt 1: Rufen Sie das Ivy-Tool auf und verwenden Sie die umformulierte Anfrage aus **Schritt 0** als Eingabe.
    - Wenn ein Fehler auftritt oder keine Mitarbeiter gefunden werden, die der Anfrage entsprechen, zeigen Sie eine Fehlermeldung an (**Schritt 2**).
    - Bei Erfolg: Zeigen Sie das Ergebnis an (**Schritt 3**).
Schritt 2: Zeigen Sie dem Benutzer eine Nachricht an und beenden Sie dann den Flow.
Schritt 3: Zeigen Sie die Informationen der gefundenen Mitarbeiter in einem gut strukturierten Format an und beenden Sie dann den Flow.

Und dies ist der AI Flow:

```json
{
    "version": "12.0.0",
    "id": "find-employees-flow",
    "name": "Find employees information",
    "type": "FLOW",
    "permissions": [ "Everybody" ],
    "description": "Find employees information",
    "usage": "Use this flow when user want to find information of employees",
    "steps": [
        {
            "stepNo": 0,
            "type": "RE_PHRASE",
            "toolId": "find-employees-info",
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
            "toolId": "find-employees-info",
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

5. Öffnen Sie die Variablendatei **Assistants.json** und fügen Sie die ID des AI Flows `Find employees information` dem Attribut `tools` Ihres KI-Assistenten hinzu, wie im Beispiel mit dem KI-Assistenten `Alex` unten gezeigt.

```json
[
    {
        "id": "537bc9e684d8481d87e7f50240aaa45e",
        "version": "12.0.0",
        "templateId": "portal-assistant-template",
        "aiModelName": "AiAssistant.AiModels.OpenAI.SecondaryModel",
        "avatarLocation": "/Logo/DefaultLogo",
        "name": "Alex",
        "contactWebsite": "https://support.axonivy.com/hc/en-us",
        "tools": [
            "find-employees-flow"
        ],
        "permissions": [
            "Everybody"
        ],
        "info": "You are a professional, helpful assistant. Your primary duty is to answer customer questions. You should provide clear, accurate, and timely information while ensuring that customers feel supported and valued. Your interactions should always adhere to strict ethical standards.",
        "ethicalRules": [
            "Keep user data confidential by protecting it securely.",
            "Be transparent by clearly stating you're an AI and providing accurate information.",
            "Treat everyone fairly by ensuring equal treatment and avoiding bias.",
            "Commit to honesty, ensuring that the customer’s trust is maintained at all times",
            "Providing equal support regardless of customer's technical expertise or business size",
            "Avoid using high-pressure tactics or making suggestions that could manipulate customer decisions"
        ]
    }
]
```

6. Der KI-Assistent `Alex` hat nun die Funktion, nach Mitarbeiterinformationen zu suchen. Sie können das Chat-Dashboard öffnen und diese neue KI-Funktion ausprobieren.

So könnte ein Beispielgespräch aussehen, wenn der Benutzer den `find-employees-flow` mit dem Assistenten `Alex` verwendet:

**Legenden:**

😄: Nachricht des Benutzers

🐼: Die Nachricht, die die KI auf dem Bildschirm anzeigt

✨: Die Nachricht, die die KI zu sich selbst spricht und dem Benutzer nicht zeigt

**Unterhaltung:**

😄: Hello Alex

🐼: Hi User

😄: I want to find Sandy, she is a web developer work for our Munich office

✨ `find employee has name ‘Sandy’, position ‘web developer’, and work at branch ‘Munich’`

✨: `<use Ivy Tool find-employees to find employees>`

✨: `[ {“name”: “Sandy Williams“, “branch”: “munich”,”position”: “Web Developer”, “rank”:“Senior”, “email”: “sandyw@localhost.com”}]`

✨: `[ {“name”: “Sandy Brown“, “branch”: “munich”,”position”: “Web Developer”, “rank”:“Junior”, “email”: “sandyb@localhost.com”}]`

🐼: I found 2 employees matched your request:

🐼: Sandy Williams: Senior Web developer, email: sandyw@localhost.com,  branch: Munich

🐼: Sandy Brown: Junior Web developer, email: sandyb@localhost.com,  branch: Munich.

✨: `<end the flow>`