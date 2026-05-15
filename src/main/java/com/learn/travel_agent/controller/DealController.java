package com.learn.travel_agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.learn.travel_agent.model.PromptRequest;
import com.learn.travel_agent.service.DealService;
import com.learn.travel_agent.service.PromptService;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class DealController {

    private static final Logger logger = LoggerFactory.getLogger(DealController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

    private final DealService dealService;
    private final PromptService promptService;

    public DealController(DealService dealService, PromptService promptService) {
        this.dealService = dealService;
        this.promptService = promptService;
    }

    @PostMapping(value = "/api/deals/{dealId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getDealById(@PathVariable String dealId, @Valid @RequestBody PromptRequest promptRequest) {
        String template = promptService.loadPromptTemplate();
        String userPrompt = promptRequest.getPrompt();
        String prompt;
        if (userPrompt != null || !userPrompt.isEmpty()) {
            prompt = userPrompt;
        } else
            prompt = template
                    .replace("{dealId}", dealId);
        logger.info("Generated prompt: {}", prompt);

        SseEmitter emitter = new SseEmitter(300_000L);
        String userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
        logger.info("Starting travel plan generation for userId: {}", userId);

        dealService.planTrip(userId, prompt)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.MINUTES)
                .subscribe(
                        event -> {
                            try {
                                logger.debug("Sending event for userId: {}", userId);
                                // Create a simplified event object without Optional fields
                                Map<String, Object> simplifiedEvent = new HashMap<>();
                                simplifiedEvent.put("id", event.id());
                                simplifiedEvent.put("invocationId", event.invocationId());
                                simplifiedEvent.put("author", event.author());
                                simplifiedEvent.put("content", event.content());
                                simplifiedEvent.put("actions", event.actions());
                                simplifiedEvent.put("finishReason", event.finishReason());
                                simplifiedEvent.put("usageMetadata", event.usageMetadata());
                                simplifiedEvent.put("timestamp", event.timestamp());

                                emitter.send(SseEmitter.event()
                                        .name("agent-event")
                                        .data(simplifiedEvent));
                            } catch (IOException e) {
                                logger.error("Error sending SSE event for userId: {}", userId, e);
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            logger.error("Error in deal details for userId: {}", userId, error);
                            emitter.completeWithError(error);
                        },
                        () -> {
                            logger.info("deal details for userId: {}", userId);
                            emitter.complete();
                        }
                );

        return emitter;
    }

}
