package com.learn.travel_agent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

public class DealTeamMember {

    private String dealId;

    private String hrId;

    private String role;

    private Deal deal;

    private Employee employee;

    // Constructors
    public DealTeamMember() {
    }

    public DealTeamMember(String dealId, String hrId, String role) {
        this.dealId = dealId;
        this.hrId = hrId;
        this.role = role;
    }

    // Getters and Setters
    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public String getHrId() {
        return hrId;
    }

    public void setHrId(String hrId) {
        this.hrId = hrId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Deal getDeal() {
        return deal;
    }

    public void setDeal(Deal deal) {
        this.deal = deal;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public String toString() {
        return "DealTeamMember{" +
                "dealId='" + dealId + '\'' +
                ", hrId='" + hrId + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    // Composite Primary Key Class
    public static class DealTeamMemberId implements Serializable {

        private String dealId;
        private String hrId;

        // Default constructor
        public DealTeamMemberId() {
        }

        public DealTeamMemberId(String dealId, String hrId) {
            this.dealId = dealId;
            this.hrId = hrId;
        }

        // Getters and Setters
        public String getDealId() {
            return dealId;
        }

        public void setDealId(String dealId) {
            this.dealId = dealId;
        }

        public String getHrId() {
            return hrId;
        }

        public void setHrId(String hrId) {
            this.hrId = hrId;
        }

        // equals and hashCode
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DealTeamMemberId that = (DealTeamMemberId) o;
            return Objects.equals(dealId, that.dealId) &&
                    Objects.equals(hrId, that.hrId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dealId, hrId);
        }
    }
}
