package com.incident.triage.model.dto;

import java.util.Map;

public class IncidentStatsDto {

    private long totalIncidents;
    private long openIncidents;
    private long investigatingIncidents;
    private long mitigatedIncidents;
    private long resolvedIncidents;
    private long criticalIncidents;
    private long highIncidents;
    private long mediumIncidents;
    private long lowIncidents;
    private double averageConfidenceScore;
    private Map<String, Long> categoryDistribution;
    private Map<String, Long> severityDistribution;
    private Map<String, Long> providerDistribution;

    public IncidentStatsDto() {
    }

    public long getTotalIncidents() {
        return totalIncidents;
    }

    public void setTotalIncidents(long totalIncidents) {
        this.totalIncidents = totalIncidents;
    }

    public long getOpenIncidents() {
        return openIncidents;
    }

    public void setOpenIncidents(long openIncidents) {
        this.openIncidents = openIncidents;
    }

    public long getInvestigatingIncidents() {
        return investigatingIncidents;
    }

    public void setInvestigatingIncidents(long investigatingIncidents) {
        this.investigatingIncidents = investigatingIncidents;
    }

    public long getMitigatedIncidents() {
        return mitigatedIncidents;
    }

    public void setMitigatedIncidents(long mitigatedIncidents) {
        this.mitigatedIncidents = mitigatedIncidents;
    }

    public long getResolvedIncidents() {
        return resolvedIncidents;
    }

    public void setResolvedIncidents(long resolvedIncidents) {
        this.resolvedIncidents = resolvedIncidents;
    }

    public long getCriticalIncidents() {
        return criticalIncidents;
    }

    public void setCriticalIncidents(long criticalIncidents) {
        this.criticalIncidents = criticalIncidents;
    }

    public long getHighIncidents() {
        return highIncidents;
    }

    public void setHighIncidents(long highIncidents) {
        this.highIncidents = highIncidents;
    }

    public long getMediumIncidents() {
        return mediumIncidents;
    }

    public void setMediumIncidents(long mediumIncidents) {
        this.mediumIncidents = mediumIncidents;
    }

    public long getLowIncidents() {
        return lowIncidents;
    }

    public void setLowIncidents(long lowIncidents) {
        this.lowIncidents = lowIncidents;
    }

    public double getAverageConfidenceScore() {
        return averageConfidenceScore;
    }

    public void setAverageConfidenceScore(double averageConfidenceScore) {
        this.averageConfidenceScore = averageConfidenceScore;
    }

    public Map<String, Long> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(Map<String, Long> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public Map<String, Long> getSeverityDistribution() {
        return severityDistribution;
    }

    public void setSeverityDistribution(Map<String, Long> severityDistribution) {
        this.severityDistribution = severityDistribution;
    }

    public Map<String, Long> getProviderDistribution() {
        return providerDistribution;
    }

    public void setProviderDistribution(Map<String, Long> providerDistribution) {
        this.providerDistribution = providerDistribution;
    }
}
