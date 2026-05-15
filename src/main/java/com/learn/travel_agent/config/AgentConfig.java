package com.learn.travel_agent.config;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.tools.FunctionTool;
import org.springframework.beans.factory.annotation.Value;
import com.google.genai.types.GenerateContentConfig;
import com.learn.travel_agent.service.TravelToolService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class AgentConfig {

//    @Value("${google.api.key}")
//    private String apiKey;
//
//    private final TravelToolService travelToolService;
//
//    public AgentConfig(TravelToolService travelToolService) {
//        this.travelToolService = travelToolService;
//    }
//
//    @PostConstruct
//    public void setup() {
//        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key-here")) {
//            throw new IllegalStateException("Please provide a valid Google API Key in application.properties or via GOOGLE_API_KEY environment variable.");
//        }
//        System.setProperty("GOOGLE_API_KEY", apiKey);
//    }
//
//
//    @Bean
//    public FunctionTool weatherTool() {
//        return FunctionTool.create(TravelToolService.class, "getWeather");
//    }
//
//    @Bean
//    public FunctionTool timeTool() {
//        return FunctionTool.create(TravelToolService.class, "getCurrentTime");
//    }
//
//    @Bean
//    public BaseAgent rootTravelAgent(FunctionTool weatherTool, FunctionTool timeTool) {
//        // 1. Define sub-agents
//        BaseAgent researcher = LlmAgent.builder()
//                .name("Researcher")
//                .description("Researches destination details including weather and time.")
//                .model("gemini-2.5-flash")
//                .instruction("You are a travel researcher. Your goal is to gather information about the destination specified by the user. " +
//                        "Always use the 'getWeather' and 'getCurrentTime' tools to fetch real-time data for the destination. " +
//                        "Do not ask the user for clarification. If the date is not specified, assume the trip is for the upcoming weekend. " +
//                        "Provide a summary of the weather and time.")
//                .tools(weatherTool, timeTool)
//                .outputKey("research_data")
//                .build();
//
//        BaseAgent planner = LlmAgent.builder()
//                .name("Planner")
//                .description("Creates a detailed itinerary based on research data.")
//                .model("gemini-2.5-flash")
//                .instruction("You are a travel planner. Create a detailed day-by-day itinerary based on the 'research_data' provided by the Researcher. " +
//                        "Incorporate the weather and time information into the plan. " +
//                        "Assume a general tourist profile if no specific preferences are given. " +
//                        "Do not ask for user input. Generate the complete itinerary immediately. " +
//                        "Output the itinerary in a structured JSON format.")
//                .generateContentConfig(GenerateContentConfig.builder()
//                        .responseMimeType("application/json")
//                        .build())
//                .build();
//
//        // 2. Wrapped them in a SequentialAgent
//        return SequentialAgent.builder()
//                .name("TravelChain")
//                .subAgents(researcher, planner)
//                .build();
//    }
}
