package com.incident.triage.model.entity;

public enum IncidentSeverity {
    CRITICAL("Critical", "Immediate outage, data corruption risk, OOM crash, or system-wide deadlock."),
    HIGH("High", "Core functionality impaired, high failure rate, or database connection pool starvation."),
    MEDIUM("Medium", "Isolated feature failure, non-critical upstream timeout, or transient error."),
    LOW("Low", "Minor recoverable exception, deprecation, or non-blocking application warning.");

    private final String label;
    private final String description;

    IncidentSeverity(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}

