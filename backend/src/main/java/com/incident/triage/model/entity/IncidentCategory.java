package com.incident.triage.model.entity;

public enum IncidentCategory {
    DATABASE("Database & Persistence", "Issues with database connectivity, transaction deadlocks, slow queries, or SQL integrity constraints."),
    MEMORY_RESOURCE("Memory & System Resources", "JVM OutOfMemoryError, heap exhaustion, metaspace leak, or file descriptor limits."),
    NETWORK("Network & Connectivity", "Socket timeouts, connection drops, unreachable hosts, or DNS resolution failures."),
    AUTHENTICATION("Authentication & Security", "Expired tokens, bad credentials, unauthorized access, or certificate errors."),
    CONCURRENCY("Concurrency & Threading", "Thread pool exhaustion, race conditions, or lock acquisition timeouts."),
    THIRD_PARTY("Third-Party & Upstream APIs", "Downstream microservice failures, REST/gRPC client timeouts, or 5xx upstream responses."),
    APPLICATION_LOGIC("Application Runtime Logic", "Unhandled NullPointerExceptions, illegal arguments, state discrepancies, or serialization bugs."),
    UNKNOWN("Unclassified Incident", "General or unclassified application error requiring manual inspection.");

    private final String displayName;
    private final String description;

    IncidentCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
