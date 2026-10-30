package com.incident.triage.model.dto;

import java.util.ArrayList;
import java.util.List;

public class AiAnalysisResult {

    private String probableRootCause;
    private String evidence;
    private List<String> recommendedFixes = new ArrayList<>();
    private List<String> preventionStrategies = new ArrayList<>();
    private double confidenceScore;
    private String provider;

    public AiAnalysisResult() {
    }

    public AiAnalysisResult(String probableRootCause, String evidence, List<String> recommendedFixes, List<String> preventionStrategies, double confidenceScore, String provider) {
        this.probableRootCause = probableRootCause;
        this.evidence = evidence;
        this.recommendedFixes = recommendedFixes != null ? recommendedFixes : new ArrayList<>();
        this.preventionStrategies = preventionStrategies != null ? preventionStrategies : new ArrayList<>();
        this.confidenceScore = confidenceScore;
        this.provider = provider;
    }

    public String getProbableRootCause() {
        return probableRootCause;
    }

    public void setProbableRootCause(String probableRootCause) {
        this.probableRootCause = probableRootCause;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public List<String> getRecommendedFixes() {
        return recommendedFixes;
    }

    public void setRecommendedFixes(List<String> recommendedFixes) {
        this.recommendedFixes = recommendedFixes;
    }

    public List<String> getPreventionStrategies() {
        return preventionStrategies;
    }

    public void setPreventionStrategies(List<String> preventionStrategies) {
        this.preventionStrategies = preventionStrategies;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
