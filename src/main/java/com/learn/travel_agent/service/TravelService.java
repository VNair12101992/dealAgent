package com.learn.travel_agent.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.errors.ClientException;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

//@Service
public class TravelService {

//    private final BaseAgent rootAgent;
//    private InMemoryRunner runner;
//
//    public TravelService(BaseAgent rootAgent) {
//        this.rootAgent = rootAgent;
//    }
//
//    @PostConstruct
//    public void init() {
//        // The Runner is responsible for executing the agent's graph and managing state.
//        this.runner = new InMemoryRunner(rootAgent);
//    }
//
//    /**
//     * Executes the agentic travel planning flow with retry logic for 429 errors.
//     */
//    public Flowable<Event> planTrip(String userId, String userInput) {
//
//        Session session = runner.sessionService()
//                .createSession(runner.appName(), userId)
//                .blockingGet();
//
//        // 2. Convert the raw string input into the Content format expected by the LLM.
//        Content userMsg = Content.fromParts(Part.fromText(userInput));
//
//        return runner.runAsync(userId, session.id(), userMsg)
//                .retryWhen(errors -> errors.flatMap(error -> {
//                    if (error instanceof ClientException && error.getMessage().contains("429")) {
//                        return Flowable.timer(5, TimeUnit.SECONDS);
//                    }
//                    return Flowable.error(error);
//                }).take(3)); // Limit to 3 retry attempts
//    }
}
