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

    String dealCompletenessCheckInstruction =
            "You are a Deal Completeness Check Agent. " +
                    "Your role is to identify and summarize completeness issues for a given deal. " +
                    "Rules: If a deal ID is provided, always call the dealCompletenessCheckTool to fetch issues. " +
                    "Rules: If no deal ID is provided, politely ask the user to supply one. " +
                    "Rules: If the tool reports deal not found, politely inform the user. " +
                    "Possible issues are limited to three types: DEAL_MEMBER_DESIGNATION (Lead Banker designation below Director), " +
                    "DEAL_BANKER_REGION (Lead Banker region does not match deal region), " +
                    "DEAL_DUPLICATE (deal is duplicate; provide duplicate deal IDs and similarities). " +
                    "A deal may have none, one, or multiple issues. " +
                    "If issues exist, provide a clear, human-readable summary of each. " +
                    "If no issues exist, inform the user the deal is clean and can proceed. " +
                    "Do not ask for any other clarifications beyond the deal ID. " +
                    "Output must be concise, professional, and actionable.";

    private final DealToolService dealToolService;

    public DealAgentConfig(DealToolService dealToolService) {
        this.dealToolService = dealToolService;
    }

    @PostConstruct
    public void setup() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key-here")) {
            throw new IllegalStateException("Please provide a valid Google API Key in" +
                    " application.properties or via GOOGLE_API_KEY environment variable.");
        }
        System.setProperty("GOOGLE_API_KEY", apiKey);
    }


    @Bean
    public FunctionTool dealTool() {
        return FunctionTool.create(DealToolService.class, "fetchDeal");
    }

    @Bean
    public FunctionTool dealCompletenessCheckTool() {
        return FunctionTool.create(DealToolService.class, "checkDeal");
    }

//    @Bean
//    public FunctionTool timeTool() {
//        return FunctionTool.create(TravelToolService.class, "getCurrentTime");
//    }

    @Bean
    public BaseAgent rootTravelAgent(FunctionTool dealTool,FunctionTool dealCompletenessCheckTool) {

        BaseAgent dealAgent = LlmAgent.builder()
                .name("deal_agent")
                .description("Fetches deal information by ID")
                .model("gemini-2.5-flash")
                .instruction("You are a deal agent. Your goal is to gather information about the deal. " +
                        "If a deal ID is provided in the user's request, always use the 'fetchDeal' tool to fetch the deal details. " +
                        "If no deal ID is provided in the request, politely ask the user to provide a deal ID. " +
                        "If the deal tool returns that the deal is not found, politely inform the user that no such deal was found. " +
                        "Do not ask for any other clarification from the user. " +
                        "Provide a clear summary of the deal information when found.")
                .tools(dealTool)
                .outputKey("deal_info")
                .build();

        BaseAgent dealCompletenessAgent = LlmAgent.builder()
                .name("deal_completeness_agent")
                .description("Fetches deal completeness check issues by ID")
                .model("gemini-2.5-flash")
                .instruction(dealCompletenessCheckInstruction)
                .tools(dealCompletenessCheckTool)
                .outputKey("completeness_issues")
                .build();

        AgentTool dealAgentTool = AgentTool.create(dealAgent, true);
        AgentTool dealCompletenessAgentTool = AgentTool.create(dealCompletenessAgent, true);

        return LlmAgent.builder()
                .name("conflict_officer_assistant")
                .model("gemini-2.5-flash")
                .instruction("You are a conflict officer assistant. Your goal is to gather information about the deal and deal completeness check issues." +
                        "If the deal summary is requested, use the 'deal_agent' tool to fetch the deal details." +
                        "If the deal completeness check issues is requested, use the 'deal_completeness_agent' tool to provide the response" +
                        "for fetching the deal completeness check issues.")
                .tools(List.of(dealAgentTool, dealCompletenessAgentTool))
                .description("You are a conflict officer assistant. Your goal is to gather information about the deal and deal completeness check issues.")
                .build();
    }
}
