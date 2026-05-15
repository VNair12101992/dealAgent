package com.learn.travel_agent.model;

import jakarta.validation.constraints.NotBlank;

public class PromptRequest {

    @NotBlank(message = "Prompt cannot be empty")
    private String prompt;

    // Getter and Setter
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
