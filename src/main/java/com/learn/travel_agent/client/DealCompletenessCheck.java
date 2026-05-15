package com.learn.travel_agent.client;

import com.learn.travel_agent.model.DealCompletenessIssues;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "deal-completeness-check", url = "http://localhost:8080")
public interface DealCompletenessCheck {

    @GetMapping("/deal/{dealId}/completeness/issues")
    public List<DealCompletenessIssues> getDealCompletenessIssues(@PathVariable String dealId);
}
