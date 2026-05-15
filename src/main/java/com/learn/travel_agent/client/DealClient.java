package com.learn.travel_agent.client;

import com.learn.travel_agent.model.Deal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "deal-client", url = "http://localhost:8080")
public interface DealClient {

    @GetMapping("/deal/{dealId}")
    public Deal getDealById(@PathVariable String dealId);

    @GetMapping("/deal/submittedBy/{submittedBy}")
    public List<Deal> getDealBySubmittedBy(@PathVariable String submittedBy);
}
