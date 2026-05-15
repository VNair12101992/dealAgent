package com.learn.travel_agent.service;

import com.google.adk.tools.Annotations;
import com.learn.travel_agent.client.DealClient;
import com.learn.travel_agent.client.DealCompletenessCheck;
import com.learn.travel_agent.model.Deal;
import com.learn.travel_agent.model.DealCompletenessIssues;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DealToolService {

    private static DealClient dealClient;
    private static DealCompletenessCheck dealCompletenessCheck;

    public DealToolService(DealClient dealClient, DealCompletenessCheck dealCompletenessCheck) {
        DealToolService.dealClient = dealClient;
        DealToolService.dealCompletenessCheck = dealCompletenessCheck;
    }

    public static Deal fetchDeal(String dealId) {
        return dealClient.getDealById(dealId);
    }

    public static List<DealCompletenessIssues> checkDeal(String dealId) {
        return dealCompletenessCheck.getDealCompletenessIssues(dealId);
    }

}
