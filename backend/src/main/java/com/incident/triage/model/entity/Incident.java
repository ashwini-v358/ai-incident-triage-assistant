package com.incident.triage.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "incidents", indexes = {
        @Index(name = "idx_incidents_category", columnList = "category"),
        @Index(name = "idx_incidents_severity", columnList = "severity"),
        @Index(name = "idx_incidents_status", columnList = "status"),
        @Index(name = "idx_incidents_created_at", columnList = "created_at")
})
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String serviceName;

    @Column(length = 50)
    private String environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IncidentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status;

    @Column(length = 255)
    private String exceptionType;

    @Column(length = 500)
    private String rootCauseException;

    @Column(length = 255)
    private String culpritClass;

    @Column(length = 100)
    private String culpritMethod;

    private Integer culpritLineNumber;

    @Column(name = "raw_log", columnDefinition = "TEXT", nullable = false)
    private String rawLog;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "probable_root_cause", columnDefinition = "TEXT")
    private String probableRootCause;

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "recommended_fixes", columnDefinition = "TEXT")
    private String recommendedFixes;

    @Column(name = "prevention_strategies", columnDefinition = "TEXT")
    private String preventionStrategies;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "analysis_provider", length = 50)
    private String analysisProvider;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Incident() {
    }

    @PrePersist
    public void onPrePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = IncidentStatus.OPEN;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
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

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
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

    public String getRecommendedFixes() {
        return recommendedFixes;
    }

    public void setRecommendedFixes(String recommendedFixes) {
        this.recommendedFixes = recommendedFixes;
    }

    public String getPreventionStrategies() {
        return preventionStrategies;
    }

    public void setPreventionStrategies(String preventionStrategies) {
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
