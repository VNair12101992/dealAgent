package com.learn.travel_agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class PromptService {

    private static final Logger logger = LoggerFactory.getLogger(PromptService.class);
    private static final String GCS_PATH = "gs://spring-prompt-templates/prompts/travel_plan_prompt.txt";
    private static final String CLASSPATH_PATH = "prompts/deal_prompt.txt";

//    private final ResourceLoader resourceLoader;
//
//    public PromptService(ResourceLoader resourceLoader) {
//        this.resourceLoader = resourceLoader;
//    }

    public String loadPromptTemplate() {
        // Try loading from GCS dynamically
//        try {
//           // Resource gcsResource = resourceLoader.getResource(GCS_PATH);
//            // We check for existence. If credentials are missing, getInputStream() or exists()
//            // might throw an exception, which we catch below.
//            if (gcsResource.exists() && gcsResource.isReadable()) {
//                logger.info("Loading prompt template from GCS: {}", GCS_PATH);
//                return StreamUtils.copyToString(gcsResource.getInputStream(), StandardCharsets.UTF_8);
//            }
//        } catch (Exception e) {
//            logger.warn("GCS is unavailable or credentials missing. Falling back to classpath. Reason: {}", e.getMessage());
//        }

        // Fallback to Classpath
        try {
            logger.info("Loading prompt template from classpath: {}", CLASSPATH_PATH);
            Resource resource = new ClassPathResource(CLASSPATH_PATH);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Could not load prompt template from GCS or classpath", e);
            throw new RuntimeException("Critical error: Prompt template missing from all sources", e);
        }
    }
}