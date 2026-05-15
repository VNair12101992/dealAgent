package com.learn.travel_agent.model;

import java.io.Serializable;
import java.util.Objects;

public class DealCompletenessIssuesId implements Serializable {

    private String dealId;
    private String issueType;

    public DealCompletenessIssuesId() {}

    public DealCompletenessIssuesId(String dealId, String issueType) {
        this.dealId = dealId;
        this.issueType = issueType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealCompletenessIssuesId that = (DealCompletenessIssuesId) o;
        return Objects.equals(dealId, that.dealId) && Objects.equals(issueType, that.issueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dealId, issueType);
    }

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
}
