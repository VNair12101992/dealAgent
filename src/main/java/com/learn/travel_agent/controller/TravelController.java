package com.learn.travel_agent.controller;

import com.learn.travel_agent.service.PromptService;
import com.learn.travel_agent.service.TravelService;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//@RestController
public class TravelController {

//    private static final Logger logger = LoggerFactory.getLogger(TravelController.class);
//
//    public record TravelRequest(
//            @NotBlank(message = "City is required")
//            String city,
//
//            @NotNull(message = "Number of pax is required")
//            @Min(value = 1, message = "At least 1 person is required")
//            Integer pax,
//
//            @NotNull(message = "Duration is required")
//            @Min(value = 1, message = "Duration must be at least 1 day")
//            Integer duration,
//
//            String preferences
//    ) {
//    }
//
//
//    private final TravelService travelService;
//    private final PromptService promptService;
//
//    public TravelController(TravelService travelService, PromptService promptService) {
//        this.travelService = travelService;
//        this.promptService = promptService;
//    }
//
//    @PostMapping(value = "/api/plan", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter planTrip(@Valid @RequestBody TravelRequest request) throws IOException {
//        logger.info("Received travel plan request for city: {}, pax: {}, duration: {}", request.city(), request.pax(), request.duration());
//
//        String template = promptService.loadPromptTemplate();
//
//        String preferences = request.preferences() != null ? request.preferences() : "General sightseeing";
//        String prompt = template
//                .replace("{city}", request.city())
//                .replace("{duration}", String.valueOf(request.duration()))
//                .replace("{pax}", String.valueOf(request.pax()))
//                .replace("{preferences}", preferences);
//
//        logger.debug("Generated prompt: {}", prompt);
//
//        SseEmitter emitter = new SseEmitter(300_000L);
//        String userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
//        logger.info("Starting travel plan generation for userId: {}", userId);
//
//        travelService.planTrip(userId, prompt)
//                .subscribeOn(Schedulers.io())
//                .timeout(3, TimeUnit.MINUTES)
//                .subscribe(
//                        event -> {
//                            try {
//                                logger.debug("Sending event for userId: {}", userId);
//                                emitter.send(SseEmitter.event().name("agent-event").data(event));
//                            } catch (IOException e) {
//                                logger.error("Error sending SSE event for userId: {}", userId, e);
//                                emitter.completeWithError(e);
//                            }
//                        },
//                        error -> {
//                            logger.error("Error in travel plan generation for userId: {}", userId, error);
//                            emitter.completeWithError(error);
//                        },
//                        () -> {
//                            logger.info("Travel plan generation completed for userId: {}", userId);
//                            emitter.complete();
//                        }
//                );
//
//        return emitter;
//    }
}
