package com.incident.triage.model.dto;

import com.incident.triage.model.entity.IncidentCategory;
import com.incident.triage.model.entity.IncidentSeverity;

import java.util.ArrayList;
import java.util.List;

public class RuleClassificationResult {

    private IncidentCategory category;
    private IncidentSeverity severity;
    private List<String> matchedRules = new ArrayList<>();
    private double ruleConfidence;
    private String ruleRationale;

    public RuleClassificationResult() {
    }

    public RuleClassificationResult(IncidentCategory category, IncidentSeverity severity, List<String> matchedRules, double ruleConfidence, String ruleRationale) {
        this.category = category;
        this.severity = severity;
        this.matchedRules = matchedRules != null ? matchedRules : new ArrayList<>();
        this.ruleConfidence = ruleConfidence;
        this.ruleRationale = ruleRationale;
    }

    public IncidentCategory getCategory() {
        return category;
    }

    public void setCategory(IncidentCategory category) {
        this.category = category;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public List<String> getMatchedRules() {
        return matchedRules;
    }

    public void setMatchedRules(List<String> matchedRules) {
        this.matchedRules = matchedRules;
    }

    public double getRuleConfidence() {
        return ruleConfidence;
    }

    public void setRuleConfidence(double ruleConfidence) {
        this.ruleConfidence = ruleConfidence;
    }

    public String getRuleRationale() {
        return ruleRationale;
    }

    public void setRuleRationale(String ruleRationale) {
        this.ruleRationale = ruleRationale;
    }
}

