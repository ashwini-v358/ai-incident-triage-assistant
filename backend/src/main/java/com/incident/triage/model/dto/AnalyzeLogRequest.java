package com.incident.triage.model.dto;

import jakarta.validation.constraints.NotBlank;

public class AnalyzeLogRequest {

    @NotBlank(message = "Raw log content is required")
    private String rawLog;

    private String serviceName;
    private String environment;
    private String title;

    public AnalyzeLogRequest() {
    }

    public AnalyzeLogRequest(String rawLog, String serviceName, String environment, String title) {
        this.rawLog = rawLog;
        this.serviceName = serviceName;
        this.environment = environment;
        this.title = title;
    }

    public String getRawLog() {
        return rawLog;
    }

    public void setRawLog(String rawLog) {
        this.rawLog = rawLog;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

