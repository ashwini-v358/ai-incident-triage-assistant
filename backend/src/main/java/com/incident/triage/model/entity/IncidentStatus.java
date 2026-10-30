package com.incident.triage.model.entity;

public enum IncidentStatus {
    OPEN("Open", "Incident detected and pending engineer investigation."),
    INVESTIGATING("Investigating", "An engineer or SRE is actively examining the root cause."),
    MITIGATED("Mitigated", "Temporary workaround or rate-limit applied to restore customer service."),
    RESOLVED("Resolved", "Root cause patched, verified in production, and incident closed.");

    private final String label;
    private final String description;

    IncidentStatus(String label, String description) {
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

