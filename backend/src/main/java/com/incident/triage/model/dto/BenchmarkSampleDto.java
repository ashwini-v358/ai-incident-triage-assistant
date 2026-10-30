package com.incident.triage.model.dto;

import com.incident.triage.model.entity.IncidentCategory;
import com.incident.triage.model.entity.IncidentSeverity;

public class BenchmarkSampleDto {

    private String id;
    private String name;
    private IncidentCategory expectedCategory;
    private IncidentSeverity expectedSeverity;
    private String expectedRootCauseKeywords;
    private String rawLog;

    public BenchmarkSampleDto() {
    }

    public BenchmarkSampleDto(String id, String name, IncidentCategory expectedCategory, IncidentSeverity expectedSeverity, String expectedRootCauseKeywords, String rawLog) {
        this.id = id;
        this.name = name;
        this.expectedCategory = expectedCategory;
        this.expectedSeverity = expectedSeverity;
        this.expectedRootCauseKeywords = expectedRootCauseKeywords;
        this.rawLog = rawLog;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IncidentCategory getExpectedCategory() {
        return expectedCategory;
    }

    public void setExpectedCategory(IncidentCategory expectedCategory) {
        this.expectedCategory = expectedCategory;
    }

    public IncidentSeverity getExpectedSeverity() {
        return expectedSeverity;
    }

    public void setExpectedSeverity(IncidentSeverity expectedSeverity) {
        this.expectedSeverity = expectedSeverity;
    }

    public String getExpectedRootCauseKeywords() {
        return expectedRootCauseKeywords;
    }

    public void setExpectedRootCauseKeywords(String expectedRootCauseKeywords) {
        this.expectedRootCauseKeywords = expectedRootCauseKeywords;
    }

    public String getRawLog() {
        return rawLog;
    }

    public void setRawLog(String rawLog) {
        this.rawLog = rawLog;
    }
}

