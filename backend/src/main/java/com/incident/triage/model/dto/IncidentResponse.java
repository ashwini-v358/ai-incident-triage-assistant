package com.incident.triage.model.dto;

import com.incident.triage.model.entity.Incident;
import com.incident.triage.model.entity.IncidentCategory;
import com.incident.triage.model.entity.IncidentSeverity;
import com.incident.triage.model.entity.IncidentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IncidentResponse {

    private Long id;
    private String title;
    private String serviceName;
    private String environment;
    private IncidentCategory category;
    private String categoryDisplayName;
    private IncidentSeverity severity;
    private String severityLabel;
    private IncidentStatus status;
    private String statusLabel;
    private String exceptionType;
    private String rootCauseException;
    private String culpritClass;
    private String culpritMethod;
    private Integer culpritLineNumber;
    private String rawLog;
    private String stackTrace;
    private String probableRootCause;
    private String evidence;
    private List<String> recommendedFixes = new ArrayList<>();
    private List<String> preventionStrategies = new ArrayList<>();
    private Double confidenceScore;
    private String analysisProvider;
    private String resolutionNotes;
    private Instant createdAt;
    private Instant updatedAt;

    public IncidentResponse() {
    }

    public static IncidentResponse fromEntity(Incident entity) {
        if (entity == null) {
            return null;
        }

        IncidentResponse response = new IncidentResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setServiceName(entity.getServiceName());
        response.setEnvironment(entity.getEnvironment());
        response.setCategory(entity.getCategory());
        response.setCategoryDisplayName(entity.getCategory() != null ? entity.getCategory().getDisplayName() : null);
        response.setSeverity(entity.getSeverity());
        response.setSeverityLabel(entity.getSeverity() != null ? entity.getSeverity().getLabel() : null);
        response.setStatus(entity.getStatus());
        response.setStatusLabel(entity.getStatus() != null ? entity.getStatus().getLabel() : null);
        response.setExceptionType(entity.getExceptionType());
        response.setRootCauseException(entity.getRootCauseException());
        response.setCulpritClass(entity.getCulpritClass());
        response.setCulpritMethod(entity.getCulpritMethod());
        response.setCulpritLineNumber(entity.getCulpritLineNumber());
        response.setRawLog(entity.getRawLog());
        response.setStackTrace(entity.getStackTrace());
        response.setProbableRootCause(entity.getProbableRootCause());
        response.setEvidence(entity.getEvidence());
        response.setConfidenceScore(entity.getConfidenceScore());
        response.setAnalysisProvider(entity.getAnalysisProvider());
        response.setResolutionNotes(entity.getResolutionNotes());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        // Parse multiline string into List
        if (entity.getRecommendedFixes() != null && !entity.getRecommendedFixes().trim().isEmpty()) {
            response.setRecommendedFixes(Arrays.stream(entity.getRecommendedFixes().split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        }

        if (entity.getPreventionStrategies() != null && !entity.getPreventionStrategies().trim().isEmpty()) {
            response.setPreventionStrategies(Arrays.stream(entity.getPreventionStrategies().split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        }

        return response;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public IncidentCategory getCategory() {
        return category;
    }

    public void setCategory(IncidentCategory category) {
        this.category = category;
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public String getSeverityLabel() {
        return severityLabel;
    }

    public void setSeverityLabel(String severityLabel) {
        this.severityLabel = severityLabel;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getRootCauseException() {
        return rootCauseException;
    }

    public void setRootCauseException(String rootCauseException) {
        this.rootCauseException = rootCauseException;
    }

    public String getCulpritClass() {
        return culpritClass;
    }

    public void setCulpritClass(String culpritClass) {
        this.culpritClass = culpritClass;
    }

    public String getCulpritMethod() {
        return culpritMethod;
    }

    public void setCulpritMethod(String culpritMethod) {
        this.culpritMethod = culpritMethod;
    }

    public Integer getCulpritLineNumber() {
        return culpritLineNumber;
    }

    public void setCulpritLineNumber(Integer culpritLineNumber) {
        this.culpritLineNumber = culpritLineNumber;
    }

    public String getRawLog() {
        return rawLog;
    }

    public void setRawLog(String rawLog) {
        this.rawLog = rawLog;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
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

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getAnalysisProvider() {
        return analysisProvider;
    }

    public void setAnalysisProvider(String analysisProvider) {
        this.analysisProvider = analysisProvider;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
