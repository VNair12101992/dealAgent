# Google ADK Integration Guide for Agentic AI Projects

This guide provides comprehensive instructions for incorporating Google ADK (Agent Development Kit) into Spring Boot applications to build agentic AI systems.

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Project Setup](#project-setup)
4. [Dependencies](#dependencies)
5. [Configuration](#configuration)
6. [Creating Tool Services](#creating-tool-services)
7. [Configuring Agents](#configuring-agents)
8. [Implementing Service Layer](#implementing-service-layer)
9. [Creating REST Controllers](#creating-rest-controllers)
10. [Best Practices](#best-practices)
11. [Common Issues and Solutions](#common-issues-and-solutions)

---

## Overview

Google ADK is a framework for building agentic AI applications that can:
- Define multiple AI agents with specific roles
- Expose custom tools/functions to agents
- Chain agents together for complex workflows
- Manage conversation sessions and state
- Handle asynchronous streaming responses

This project demonstrates a Deal Management system with AI agents that can:
- Fetch deal details
- Check deal completeness issues
- Coordinate between specialized agents

---

## Prerequisites

- Java 21 or higher
- Spring Boot 3.5.9 or higher
- Google API Key (for Gemini models)
- Maven 3.6+ or Gradle

---

## Project Setup

### 1. Create Spring Boot Project

```bash
spring init --dependencies=web,actuator,openfeign,validation travel-agent
cd travel-agent
```

### 2. Project Structure

```
src/main/java/com/learn/travel_agent/
├── config/
│   └── DealAgentConfig.java       # Agent configuration
├── controller/
│   └── DealController.java        # REST endpoints
├── service/
│   ├── DealService.java           # Agent execution service
│   └── DealToolService.java       # Tool implementations
├── client/
│   ├── DealClient.java            # Feign client for external APIs
│   └── DealCompletenessCheck.java
└── model/
    ├── Deal.java
    └── DealCompletenessIssues.java
```

---

## Dependencies

Add the following dependencies to your `pom.xml`:

```xml
<properties>
    <java.version>21</java.version>
    <google-adk.version>1.2.0</google-adk.version>
</properties>

<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Cloud OpenFeign -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <!-- Google ADK Core -->
    <dependency>
        <groupId>com.google.adk</groupId>
        <artifactId>google-adk</artifactId>
        <version>${google-adk.version}</version>
    </dependency>

    <!-- Google ADK Dev (optional, for web UI) -->
    <dependency>
        <groupId>com.google.adk</groupId>
        <artifactId>google-adk-dev</artifactId>
        <version>${google-adk.version}</version>
    </dependency>

    <!-- RxJava3 for reactive streams -->
    <dependency>
        <groupId>io.reactivex.rxjava3</groupId>
        <artifactId>rxjava</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

---

## Configuration

### 1. Application Properties

Configure in `src/main/resources/application.properties`:

```properties
spring.application.name=travel-agent
server.port=9090

# Google API Key
google.api.key=${GOOGLE_API_KEY:your-api-key-here}

# Enable virtual threads (optional)
spring.threads.virtual.enabled=true

# Retry configuration for API calls
resilience4j.retry.instances.geminiRetry.max-attempts=3
resilience4j.retry.instances.geminiRetry.wait-duration=2s
resilience4j.retry.instances.geminiRetry.enable-exponential-backoff=true
resilience4j.retry.instances.geminiRetry.exponential-backoff-multiplier=2
```

### 2. Enable Feign Clients

Add to your main application class:

```java
@SpringBootApplication
@EnableFeignClients
public class TravelAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelAgentApplication.class, args);
    }
}
```

---

## Creating Tool Services

Tool services expose methods that AI agents can call as functions.

### 1. Define Tool Service

```java
package com.learn.travel_agent.service;

import com.google.adk.tools.Annotations;
import com.learn.travel_agent.client.DealClient;
import com.learn.travel_agent.client.DealCompletenessCheck;
import com.learn.travel_agent.model.Deal;
import com.learn.travel_agent.model.DealCompletenessIssues;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DealToolService {

    private final DealClient dealClient;
    private final DealCompletenessCheck dealCompletenessCheck;

    public DealToolService(DealClient dealClient, DealCompletenessCheck dealCompletenessCheck) {
        this.dealClient = dealClient;
        this.dealCompletenessCheck = dealCompletenessCheck;
    }

    /**
     * Tool method to fetch deal details.
     * The @Annotations.Schema annotation provides metadata for the AI model.
     */
    public Deal deal(@Annotations.Schema(name="dealId",
            description = "ID of the deal, which will be used to fetch the deal details. " +
                    "This ID is unique and is used to identify the deal.")
                            String dealId) {
        return dealClient.getDealById(dealId);
    }

    /**
     * Tool method to fetch deal completeness check issues.
     */
    public List<DealCompletenessIssues> check(@Annotations.Schema(name="dealId",
            description = "ID of the deal, which will be used to fetch the deal completeness check issues. " +
                    "This ID is unique and is used to identify the deal.")
                            String dealId) {
        return dealCompletenessCheck.getDealCompletenessIssues(dealId);
    }
}
```

### 2. Important Notes for Tool Methods

- **Method Naming**: Use simple, descriptive names. Avoid complex naming that might violate Google GenAI API function name requirements (must start with letter/underscore, contain only alphanumeric, underscores, dots, colons, or dashes, max 128 characters).

- **Annotations**: Use `@Annotations.Schema` on parameters to provide descriptions to the AI model about what each parameter represents.

- **Return Types**: Return simple POJOs or collections. The ADK will handle serialization.

- **Instance vs Static Methods**: 
  - For Spring-managed services with dependencies, use instance methods
  - For simple utilities, static methods can be used with class-based `FunctionTool.create()`

---

## Configuring Agents

### 1. Create Agent Configuration Class

```java
package com.learn.travel_agent.config;

import com.google.adk.agents.*;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.tools.AgentTool;
import com.google.adk.tools.FunctionTool;
import com.learn.travel_agent.service.DealToolService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DealAgentConfig {

    @Value("${google.api.key}")
    private String apiKey;

    @Bean
    public InMemorySessionService sessionService() {
        return new InMemorySessionService();
    }

    private final DealToolService dealToolService;

    public DealAgentConfig(DealToolService dealToolService) {
        this.dealToolService = dealToolService;
    }

    @PostConstruct
    public void setup() {
        // Validate API key
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key-here")) {
            throw new IllegalStateException("Please provide a valid Google API Key in " +
                    "application.properties or via GOOGLE_API_KEY environment variable.");
        }
        System.setProperty("GOOGLE_API_KEY", apiKey);
    }

    /**
     * Create FunctionTool for deal fetching
     * IMPORTANT: Use instance-based create() for Spring-managed services
     */
    @Bean
    public FunctionTool dealTool() {
        return FunctionTool.create(dealToolService, "deal");
    }

    /**
     * Create FunctionTool for deal completeness check
     */
    @Bean
    public FunctionTool dealCompletenessCheckTool() {
        return FunctionTool.create(dealToolService, "check");
    }

    /**
     * Define the root agent that coordinates sub-agents
     */
    @Bean
    public BaseAgent rootTravelAgent(FunctionTool dealTool, 
                                     FunctionTool dealCompletenessCheckTool) {

        // Sub-agent 1: Deal Agent - Fetches deal information
        BaseAgent dealAgent = LlmAgent.builder()
                .name("Deal Agent")
                .description("Fetches the info related to Deal")
                .model("gemini-2.5-flash")
                .instruction("You are a deal agent. Your goal is to gather information about the deal. " +
                        "If a deal ID is provided in the user's request, always use the 'dealTool' to fetch the deal details. " +
                        "When asked to provide summary then only provide the summarization. " +
                        "If no deal ID is provided in the request, politely ask the user to provide a deal ID. " +
                        "If the deal tool returns that the deal is not found, politely inform the user that no such deal was found. " +
                        "Do not ask for any other clarification from the user. " +
                        "Provide a clear summary of the deal information when found.")
                .tools(dealTool)
                .outputKey("deal_tool_agent")
                .build();

        // Sub-agent 2: Deal Completeness Agent - Checks for completeness issues
        BaseAgent dealCompletenessAgent = LlmAgent.builder()
                .name("Deal Completeness Agent")
                .description("Fetches the info related to Deal Completeness Check issues")
                .model("gemini-2.5-flash")
                .instruction("You are a Deal Completeness Check Agent. " +
                        "Your role is to identify and summarize completeness issues for a given deal. " +
                        "Rules: If a deal ID is provided, always call the dealCompletenessCheckTool to fetch issues. " +
                        "Rules: If no deal ID is provided, politely ask the user to supply one. " +
                        "Rules: If the tool reports deal not found, politely inform the user. " +
                        "Possible issues are limited to three types: DEAL_MEMBER_DESIGNATION, " +
                        "DEAL_BANKER_REGION, DEAL_DUPLICATE. " +
                        "A deal may have none, one, or multiple issues. " +
                        "If issues exist, provide a clear, human-readable summary of each. " +
                        "If no issues exist, inform the user the deal is clean and can proceed.")
                .tools(dealCompletenessCheckTool)
                .outputKey("deal_completeness_check_toolagent")
                .build();

        // Wrap sub-agents as AgentTools
        AgentTool dealAgentTool = AgentTool.create(dealAgent, true);
        AgentTool dealCompletenessAgentTool = AgentTool.create(dealCompletenessAgent, true);

        // Root agent: Coordinates between sub-agents
        return LlmAgent.builder()
                .name("Conflict Officer Assistant")
                .model("gemini-2.5-flash")
                .instruction("You are a conflict officer assistant. Your goal is to gather information about the deal and deal completeness check issues." +
                        "If the deal summary is requested then only call 'dealAgentTool' to fetch the deal details." +
                        "If the deal completeness check issues is requested then only call 'dealCompletenessAgentTool' tool to provide the response" +
                        "for fetching the deal completeness check issues.")
                .tools(List.of(dealAgentTool, dealCompletenessAgentTool))
                .description("You are a conflict officer assistant. Your goal is to gather information about the deal and deal completeness check issues.")
                .build();
    }
}
```

### 2. Agent Types

- **LlmAgent**: An agent powered by an LLM that can use tools
- **SequentialAgent**: Chains multiple agents in sequence
- **AgentTool**: Wraps an agent to be used as a tool by another agent

### 3. Agent Builder Pattern

```java
LlmAgent.builder()
    .name("Agent Name")           // Required: Agent identifier
    .description("Description")  // Required: What the agent does
    .model("gemini-2.5-flash")    // Required: LLM model to use
    .instruction("Instructions")  // Required: Agent's role and behavior
    .tools(tool1, tool2)          // Optional: Tools the agent can use
    .outputKey("key")             // Optional: Key for output in agent graph
    .build()
```

---

## Implementing Service Layer

The service layer handles agent execution and session management.

```java
package com.learn.travel_agent.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.sessions.Session;
import com.google.genai.errors.ClientException;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class DealService {

    private final BaseAgent rootAgent;
    private InMemoryRunner runner;

    public DealService(BaseAgent rootAgent) {
        this.rootAgent = rootAgent;
    }

    @PostConstruct
    public void init() {
        // The Runner is responsible for executing the agent's graph and managing state.
        this.runner = new InMemoryRunner(rootAgent, "travel-agent");
    }

    /**
     * Executes the agentic deal service flow with retry logic for 429 errors.
     * 
     * @param userId Unique identifier for the user/session
     * @param userInput User's natural language input
     * @return Flowable of events from the agent execution
     */
    public Flowable<Event> planTrip(String userId, String userInput) {
        // 1. Create or retrieve a session
        Session session = runner.sessionService()
                .createSession(runner.appName(), userId)
                .blockingGet();

        // Debug: Log session properties
        System.out.println("--- Examining Session Properties ---");
        System.out.printf("ID (`id`): %s%n", session.id());
        System.out.printf("Application Name (`appName`): %s%n", session.appName());
        System.out.printf("User ID (`userId`): %s%n", session.userId());
        System.out.printf("State (`state`): %s%n", session.state());
        System.out.println("------------------------------------");

        // 2. Convert the raw string input into the Content format expected by the LLM.
        Content userMsg = Content.fromParts(Part.fromText(userInput));

        // 3. Run the agent asynchronously with retry logic
        return runner.runAsync(userId, session.id(), userMsg)
                .retryWhen(errors -> errors.flatMap(error -> {
                    // Retry on rate limit (429) or resource exhausted errors
                    if (error instanceof ClientException && 
                        (error.getMessage().contains("429") || 
                         error.getMessage().contains("resource_exhausted"))) {
                        return Flowable.timer(5, TimeUnit.SECONDS);
                    }
                    return Flowable.error(error);
                }).take(3)); // Limit to 3 retry attempts
    }
}
```

### Key Components

- **InMemoryRunner**: Executes the agent graph and manages state
- **Session**: Represents a conversation with state management
- **Content**: Wraps user input for LLM consumption
- **Flowable<Event>**: Reactive stream of events from agent execution

---

## Creating REST Controllers

Expose the agent functionality via REST endpoints with Server-Sent Events (SSE) for streaming responses.

```java
package com.learn.travel_agent.controller;

import com.learn.travel_agent.model.PromptRequest;
import com.learn.travel_agent.service.DealService;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class DealController {

    private static final Logger logger = LoggerFactory.getLogger(DealController.class);

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping(value = "/api/deals/{dealId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getDealById(@PathVariable String dealId, 
                                   @Valid @RequestBody PromptRequest promptRequest) {
        
        // Generate unique user ID for this request
        String userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
        logger.info("Starting deal analysis for userId: {}, dealId: {}", userId, dealId);

        // Create SSE emitter with 5-minute timeout
        SseEmitter emitter = new SseEmitter(300_000L);

        // Get user prompt or use default template
        String userPrompt = promptRequest.getPrompt();
        String prompt = (userPrompt != null && !userPrompt.isEmpty()) 
                ? userPrompt 
                : "Please analyze deal with ID: " + dealId;

        // Execute agent and stream events
        dealService.planTrip(userId, prompt)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.MINUTES)
                .subscribe(
                        event -> {
                            try {
                                logger.debug("Sending event for userId: {}", userId);
                                emitter.send(SseEmitter.event().name("agent-event").data(event));
                            } catch (IOException e) {
                                logger.error("Error sending SSE event for userId: {}", userId, e);
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            logger.error("Error in deal analysis for userId: {}", userId, error);
                            emitter.completeWithError(error);
                        },
                        () -> {
                            logger.info("Deal analysis completed for userId: {}", userId);
                            emitter.complete();
                        }
                );

        return emitter;
    }
}
```

### Request Model

```java
package com.learn.travel_agent.model;

import jakarta.validation.constraints.NotBlank;

public class PromptRequest {

    @NotBlank(message = "Prompt cannot be blank")
    private String prompt;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
```

---

## Best Practices

### 1. Agent Design

- **Single Responsibility**: Each agent should have one clear purpose
- **Clear Instructions**: Provide detailed instructions for agent behavior
- **Tool Selection**: Only give agents access to tools they need
- **Output Keys**: Use descriptive output keys for agent graph coordination

### 2. Tool Design

- **Simple Methods**: Keep tool methods focused and simple
- **Descriptive Parameters**: Use `@Annotations.Schema` for all parameters
- **Error Handling**: Handle errors gracefully and return meaningful messages
- **Validation**: Validate inputs before making external API calls

### 3. Session Management

- **Unique User IDs**: Generate unique IDs for each user/session
- **Session Reuse**: Reuse sessions for multi-turn conversations
- **State Management**: Leverage session state for context retention

### 4. Error Handling

- **Retry Logic**: Implement retry for transient errors (429, resource_exhausted)
- **Timeouts**: Set appropriate timeouts for agent execution
- **Logging**: Log errors with sufficient context for debugging

### 5. Performance

- **Async Execution**: Use reactive streams for non-blocking execution
- **Virtual Threads**: Enable virtual threads for better concurrency
- **Connection Pooling**: Configure connection pooling for external APIs

---

## Common Issues and Solutions

### Issue 1: Invalid Function Name Error

**Error**: `Invalid function name. Must start with a letter or an underscore...`

**Cause**: The ADK generates function names that may violate Google GenAI API naming rules.

**Solution**:
- Use simple, short method names in tool services
- Avoid complex naming patterns
- Method names should start with a letter, contain only alphanumeric characters, underscores, dots, colons, or dashes
- Maximum length: 128 characters

**Example**:
```java
// Good
public Deal deal(String dealId) { ... }

// Avoid
public Deal getDealByIdUsingComplexNaming(String dealId) { ... }
```

### Issue 2: Static Method Not Found

**Error**: `Static method X not found in class Y`

**Cause**: Using class-based `FunctionTool.create()` with instance methods.

**Solution**:
- For Spring-managed services with dependencies, use instance-based creation:
```java
// Correct for Spring services
@Bean
public FunctionTool dealTool() {
    return FunctionTool.create(dealToolService, "deal");
}

// Only use class-based for static methods
@Bean
public FunctionTool staticTool() {
    return FunctionTool.create(StaticToolService.class, "staticMethod");
}
```

### Issue 3: Resource Exhausted / 429 Errors

**Error**: `resource_exhausted: The third-party model provider is experiencing issues`

**Cause**: API rate limiting or service unavailability.

**Solution**:
- Implement retry logic with exponential backoff
- Use resilience4j for circuit breaking
- Add appropriate delays between retries

```java
.retryWhen(errors -> errors.flatMap(error -> {
    if (error instanceof ClientException && 
        (error.getMessage().contains("429") || 
         error.getMessage().contains("resource_exhausted"))) {
        return Flowable.timer(5, TimeUnit.SECONDS);
    }
    return Flowable.error(error);
}).take(3))
```

### Issue 4: Session Creation Errors

**Error**: `Cannot resolve method 'createSession(String, String, ConcurrentMap<String, Object>)'`

**Cause**: Incorrect method signature for session creation.

**Solution**:
- Use the correct signature without initial state parameter:
```java
Session session = runner.sessionService()
        .createSession(runner.appName(), userId)
        .blockingGet();
```

### Issue 5: Bean Creation Failures

**Error**: `UnsatisfiedDependencyException` during bean creation

**Cause**: Circular dependencies or missing beans.

**Solution**:
- Ensure all required dependencies are properly defined as beans
- Check for circular dependencies and refactor if needed
- Use `@Lazy` annotation if circular dependency is unavoidable

---

## Advanced Patterns

### 1. Sequential Agent Chains

Chain multiple agents to execute in sequence:

```java
@Bean
public BaseAgent sequentialAgent() {
    BaseAgent agent1 = LlmAgent.builder()
        .name("Agent1")
        .instruction("...")
        .build();
    
    BaseAgent agent2 = LlmAgent.builder()
        .name("Agent2")
        .instruction("...")
        .build();
    
    return SequentialAgent.builder()
        .name("Chain")
        .subAgents(agent1, agent2)
        .build();
}
```

### 2. JSON Output

Configure agents to output structured JSON:

```java
import com.google.genai.types.GenerateContentConfig;

BaseAgent agent = LlmAgent.builder()
    .name("JSON Agent")
    .instruction("...")
    .generateContentConfig(GenerateContentConfig.builder()
        .responseMimeType("application/json")
        .build())
    .build();
```

### 3. Custom Session State

Manage custom state in sessions:

```java
Session session = runner.sessionService()
    .createSession(runner.appName(), userId)
    .blockingGet();

// Update session state
session.state().put("customKey", customValue);
```

---

## Testing

### Unit Testing Tool Services

```java
@SpringBootTest
class DealToolServiceTest {

    @Autowired
    private DealToolService dealToolService;

    @MockBean
    private DealClient dealClient;

    @Test
    void testDealTool() {
        when(dealClient.getDealById("123")).thenReturn(mockDeal);
        Deal result = dealToolService.deal("123");
        assertNotNull(result);
    }
}
```

### Integration Testing Agents

```java
@SpringBootTest
@AutoConfigureMockMvc
class AgentIntegrationTest {

    @Autowired
    private DealService dealService;

    @Test
    void testAgentExecution() {
        Flowable<Event> events = dealService.planTrip("test-user", "Test input");
        List<Event> eventList = events.toList().blockingGet();
        assertFalse(eventList.isEmpty());
    }
}
```

---

## Monitoring and Debugging

### 1. Enable Logging

Configure logging in `application.properties`:

```properties
logging.level.com.google.adk=DEBUG
logging.level.com.google.genai=DEBUG
```

### 2. Session Inspection

Log session properties for debugging:

```java
System.out.println("--- Session Properties ---");
System.out.printf("ID: %s%n", session.id());
System.out.printf("AppName: %s%n", session.appName());
System.out.printf("UserID: %s%n", session.userId());
System.out.printf("State: %s%n", session.state());
```

### 3. Event Tracking

Track events during agent execution:

```java
dealService.planTrip(userId, prompt)
    .subscribe(
        event -> logger.info("Event: {}", event),
        error -> logger.error("Error: {}", error),
        () -> logger.info("Completed")
    );
```

---

## Resources

- [Google ADK Documentation](https://adk.dev/)
- [Google GenAI API Reference](https://ai.google.dev/docs)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [RxJava Documentation](https://reactivex.io/RxJava/)

---

## License

This guide is provided as-is for educational purposes.
