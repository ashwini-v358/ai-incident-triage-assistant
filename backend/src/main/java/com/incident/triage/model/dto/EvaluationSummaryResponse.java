package com.incident.triage.model.dto;

import java.util.ArrayList;
import java.util.List;

public class EvaluationSummaryResponse {

    private int totalSamples;
    private int categoryMatches;
    private double categoryAccuracyPercent;
    private int severityMatches;
    private double severityAccuracyPercent;
    private int rootCauseMatches;
    private double rootCauseAccuracyPercent;
    private double averageLatencyMs;
    private double averageConfidenceScore;
    private String primaryProvider;
    private List<EvaluationMetricDto> sampleResults = new ArrayList<>();

    public EvaluationSummaryResponse() {
    }

    public int getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(int totalSamples) {
        this.totalSamples = totalSamples;
    }

    public int getCategoryMatches() {
        return categoryMatches;
    }

    public void setCategoryMatches(int categoryMatches) {
        this.categoryMatches = categoryMatches;
    }

    public double getCategoryAccuracyPercent() {
        return categoryAccuracyPercent;
    }

    public void setCategoryAccuracyPercent(double categoryAccuracyPercent) {
        this.categoryAccuracyPercent = categoryAccuracyPercent;
    }

    public int getSeverityMatches() {
        return severityMatches;
    }

    public void setSeverityMatches(int severityMatches) {
        this.severityMatches = severityMatches;
    }

    public double getSeverityAccuracyPercent() {
        return severityAccuracyPercent;
    }

    public void setSeverityAccuracyPercent(double severityAccuracyPercent) {
        this.severityAccuracyPercent = severityAccuracyPercent;
    }

    public int getRootCauseMatches() {
        return rootCauseMatches;
    }

    public void setRootCauseMatches(int rootCauseMatches) {
        this.rootCauseMatches = rootCauseMatches;
    }

    public double getRootCauseAccuracyPercent() {
        return rootCauseAccuracyPercent;
    }

    public void setRootCauseAccuracyPercent(double rootCauseAccuracyPercent) {
        this.rootCauseAccuracyPercent = rootCauseAccuracyPercent;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public void setAverageLatencyMs(double averageLatencyMs) {
        this.averageLatencyMs = averageLatencyMs;
    }

    public double getAverageConfidenceScore() {
        return averageConfidenceScore;
    }

    public void setAverageConfidenceScore(double averageConfidenceScore) {
        this.averageConfidenceScore = averageConfidenceScore;
    }

    public String getPrimaryProvider() {
        return primaryProvider;
    }

    public void setPrimaryProvider(String primaryProvider) {
        this.primaryProvider = primaryProvider;
    }

    public List<EvaluationMetricDto> getSampleResults() {
        return sampleResults;
    }

    public void setSampleResults(List<EvaluationMetricDto> sampleResults) {
        this.sampleResults = sampleResults;
    }
}

