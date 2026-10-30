package com.incident.triage.model.dto;

import com.incident.triage.model.entity.IncidentCategory;
import com.incident.triage.model.entity.IncidentSeverity;

import java.util.List;

public class EvaluationMetricDto {

    private String sampleId;
    private String sampleName;
    private IncidentCategory expectedCategory;
    private IncidentCategory predictedCategory;
    private boolean categoryMatch;
    private IncidentSeverity expectedSeverity;
    private IncidentSeverity predictedSeverity;
    private boolean severityMatch;
    private String expectedRootCauseKeywords;
    private boolean rootCauseMatch;
    private double confidenceScore;
    private long executionTimeMs;
    private String provider;
    private List<String> recommendedFixes;

    public EvaluationMetricDto() {
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public IncidentCategory getExpectedCategory() {
        return expectedCategory;
    }

    public void setExpectedCategory(IncidentCategory expectedCategory) {
        this.expectedCategory = expectedCategory;
    }

    public IncidentCategory getPredictedCategory() {
        return predictedCategory;
    }

    public void setPredictedCategory(IncidentCategory predictedCategory) {
        this.predictedCategory = predictedCategory;
    }

    public boolean isCategoryMatch() {
        return categoryMatch;
    }

    public void setCategoryMatch(boolean categoryMatch) {
        this.categoryMatch = categoryMatch;
    }

    public IncidentSeverity getExpectedSeverity() {
        return expectedSeverity;
    }

    public void setExpectedSeverity(IncidentSeverity expectedSeverity) {
        this.expectedSeverity = expectedSeverity;
    }

    public IncidentSeverity getPredictedSeverity() {
        return predictedSeverity;
    }

    public void setPredictedSeverity(IncidentSeverity predictedSeverity) {
        this.predictedSeverity = predictedSeverity;
    }

    public boolean isSeverityMatch() {
        return severityMatch;
    }

    public void setSeverityMatch(boolean severityMatch) {
        this.severityMatch = severityMatch;
    }

    public String getExpectedRootCauseKeywords() {
        return expectedRootCauseKeywords;
    }

    public void setExpectedRootCauseKeywords(String expectedRootCauseKeywords) {
        this.expectedRootCauseKeywords = expectedRootCauseKeywords;
    }

    public boolean isRootCauseMatch() {
        return rootCauseMatch;
    }

    public void setRootCauseMatch(boolean rootCauseMatch) {
        this.rootCauseMatch = rootCauseMatch;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<String> getRecommendedFixes() {
        return recommendedFixes;
    }

    public void setRecommendedFixes(List<String> recommendedFixes) {
        this.recommendedFixes = recommendedFixes;
    }
}

