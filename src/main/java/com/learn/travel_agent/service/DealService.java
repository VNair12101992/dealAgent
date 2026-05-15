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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
     */
    public Flowable<Event> planTrip(String userId, String userInput) {
        Session session = runner.sessionService()
                .createSession(runner.appName(), userId)
                .blockingGet();

        System.out.println("--- Examining Session Properties ---");
        System.out.printf("ID (`id`): %s%n", session.id());
        System.out.printf("Application Name (`appName`): %s%n", session.appName());
        System.out.printf("User ID (`userId`): %s%n", session.userId());
        System.out.printf("State (`state`): %s%n", session.state());
        System.out.println("------------------------------------");

        // 2. Convert the raw string input into the Content format expected by the LLM.
        Content userMsg = Content.fromParts(Part.fromText(userInput));

        return runner.runAsync(userId, session.id(), userMsg)
                .retryWhen(errors -> errors.flatMap(error -> {
                    if (error instanceof ClientException && 
                        (error.getMessage().contains("429") || error.getMessage().contains("resource_exhausted"))) {
                        return Flowable.timer(10, TimeUnit.SECONDS);
                    }
                    return Flowable.error(error);
                }).take(5)); // Limit to 5 retry attempts
    }
}
