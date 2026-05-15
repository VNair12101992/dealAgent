package com.learn.travel_agent.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

public class DealCompletenessIssues {

    private String dealId;

    private String issueType;

    private String issues;

    // Constructors
    public DealCompletenessIssues() {}

    public DealCompletenessIssues(String dealId, String issueType, String issues) {
        this.dealId = dealId;
        this.issueType = issueType;
        this.issues = issues;
    }

    // Getters and Setters
    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getIssues() {
        return issues;
    }

    public void setIssues(String issues) {
        this.issues = issues;
    }

    @Override
    public String toString() {
        return "DealCompletenessIssues{" +
                "dealId='" + dealId + '\'' +
                ", issueType='" + issueType + '\'' +
                ", issues='" + issues + '\'' +
                '}';
    }
}
