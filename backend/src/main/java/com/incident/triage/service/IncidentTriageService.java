package com.incident.triage.service;

import com.incident.triage.model.dto.*;
import com.incident.triage.model.entity.*;
import com.incident.triage.repository.IncidentRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class IncidentTriageService {

    private static final Logger log = LoggerFactory.getLogger(IncidentTriageService.class);

    private final LogParsingService logParsingService;
    private final RuleClassificationService ruleClassificationService;
    private final GeminiAiService geminiAiService;
    private final IncidentRepository incidentRepository;

    public IncidentTriageService(LogParsingService logParsingService,
                                 RuleClassificationService ruleClassificationService,
                                 GeminiAiService geminiAiService,
                                 IncidentRepository incidentRepository) {
        this.logParsingService = logParsingService;
        this.ruleClassificationService = ruleClassificationService;
        this.geminiAiService = geminiAiService;
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public IncidentResponse analyzeAndSaveIncident(AnalyzeLogRequest request) {
        String rawLog = request.getRawLog();
        if (rawLog == null || rawLog.trim().isEmpty()) {
            throw new IllegalArgumentException("Raw log cannot be empty");
        }

        log.info("Starting incident triage analysis for service: {}, env: {}",
                request.getServiceName(), request.getEnvironment());

        // 1. Parse log and extract Java exception & stack-trace
        ParsedLogInfo parsedLog = logParsingService.parse(rawLog);

        // 2. Classify category and severity using Rule Engine
        RuleClassificationResult ruleResult = ruleClassificationService.classify(rawLog, parsedLog);

        // 3. Probable root cause analysis via Gemini AI with deterministic fallback
        AiAnalysisResult aiResult = geminiAiService.analyze(rawLog, parsedLog, ruleResult);

        // 4. Determine title
        String title = request.getTitle();
        if (title == null || title.trim().isEmpty()) {
            if (parsedLog.getPrimaryExceptionType() != null) {
                title = parsedLog.getPrimaryExceptionType();
                if (parsedLog.getCulpritClass() != null) {
                    title += " in " + getSimpleClassName(parsedLog.getCulpritClass());
                }
            } else if (parsedLog.getMainErrorMessage() != null) {
                title = parsedLog.getMainErrorMessage();
                if (title.length() > 80) {
                    title = title.substring(0, 77) + "...";
                }
            } else {
                title = ruleResult.getCategory().getDisplayName() + " Incident";
            }
        }

        // 5. Construct and persist Incident entity
        Incident incident = new Incident();
        incident.setTitle(title);
        incident.setServiceName(request.getServiceName() != null ? request.getServiceName() : "default-service");
        incident.setEnvironment(request.getEnvironment() != null ? request.getEnvironment() : "PRODUCTION");
        incident.setCategory(ruleResult.getCategory());
        incident.setSeverity(ruleResult.getSeverity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setRawLog(rawLog);
        incident.setStackTrace(parsedLog.getStackTrace());
        incident.setExceptionType(parsedLog.getPrimaryExceptionType());
        incident.setRootCauseException(parsedLog.getRootCauseException());
        incident.setCulpritClass(parsedLog.getCulpritClass());
        incident.setCulpritMethod(parsedLog.getCulpritMethod());
        incident.setCulpritLineNumber(parsedLog.getCulpritLineNumber());

        if (aiResult != null) {
            incident.setProbableRootCause(aiResult.getProbableRootCause());
            incident.setEvidence(aiResult.getEvidence());
            incident.setRecommendedFixes(String.join("\n", aiResult.getRecommendedFixes()));
            incident.setPreventionStrategies(String.join("\n", aiResult.getPreventionStrategies()));
            incident.setConfidenceScore(aiResult.getConfidenceScore());
            incident.setAnalysisProvider(aiResult.getProvider());
        }

        Incident saved = incidentRepository.save(incident);
        log.info("Incident triaged and stored in database successfully with ID: {}", saved.getId());

        return IncidentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> getIncidents(IncidentCategory category,
                                              IncidentSeverity severity,
                                              IncidentStatus status,
                                              String search,
                                              Pageable pageable) {
        Specification<Incident> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titlePred = cb.like(cb.lower(root.get("title")), pattern);
                Predicate servicePred = cb.like(cb.lower(root.get("serviceName")), pattern);
                Predicate exPred = cb.like(cb.lower(root.get("exceptionType")), pattern);
                Predicate causePred = cb.like(cb.lower(root.get("probableRootCause")), pattern);
                predicates.add(cb.or(titlePred, servicePred, exPred, causePred));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return incidentRepository.findAll(spec, pageable).map(IncidentResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incident not found with ID: " + id));
        return IncidentResponse.fromEntity(incident);
    }

    @Transactional
    public IncidentResponse updateIncidentStatus(Long id, UpdateIncidentStatusRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incident not found with ID: " + id));

        incident.setStatus(request.getStatus());
        if (request.getResolutionNotes() != null) {
            incident.setResolutionNotes(request.getResolutionNotes());
        }

        Incident updated = incidentRepository.save(incident);
        log.info("Updated incident {} status to {}", id, request.getStatus());
        return IncidentResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteIncident(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new NoSuchElementException("Incident not found with ID: " + id);
        }
        incidentRepository.deleteById(id);
        log.info("Deleted incident with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public IncidentStatsDto getStats() {
        IncidentStatsDto stats = new IncidentStatsDto();
        stats.setTotalIncidents(incidentRepository.count());
        stats.setOpenIncidents(incidentRepository.countByStatus(IncidentStatus.OPEN));
        stats.setInvestigatingIncidents(incidentRepository.countByStatus(IncidentStatus.INVESTIGATING));
        stats.setMitigatedIncidents(incidentRepository.countByStatus(IncidentStatus.MITIGATED));
        stats.setResolvedIncidents(incidentRepository.countByStatus(IncidentStatus.RESOLVED));

        stats.setCriticalIncidents(incidentRepository.countBySeverity(IncidentSeverity.CRITICAL));
        stats.setHighIncidents(incidentRepository.countBySeverity(IncidentSeverity.HIGH));
        stats.setMediumIncidents(incidentRepository.countBySeverity(IncidentSeverity.MEDIUM));
        stats.setLowIncidents(incidentRepository.countBySeverity(IncidentSeverity.LOW));

        Double avgConfidence = incidentRepository.getAverageConfidenceScore();
        stats.setAverageConfidenceScore(avgConfidence != null ? Math.round(avgConfidence * 100.0) / 100.0 : 0.0);

        // Category distribution
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        for (Object[] row : incidentRepository.countGroupByCategory()) {
            if (row[0] != null) {
                categoryMap.put(row[0].toString(), (Long) row[1]);
            }
        }
        stats.setCategoryDistribution(categoryMap);

        // Severity distribution
        Map<String, Long> severityMap = new LinkedHashMap<>();
        for (Object[] row : incidentRepository.countGroupBySeverity()) {
            if (row[0] != null) {
                severityMap.put(row[0].toString(), (Long) row[1]);
            }
        }
        stats.setSeverityDistribution(severityMap);

        // Provider distribution
        Map<String, Long> providerMap = new LinkedHashMap<>();
        for (Object[] row : incidentRepository.countGroupByAnalysisProvider()) {
            if (row[0] != null) {
                providerMap.put(row[0].toString(), (Long) row[1]);
            }
        }
        stats.setProviderDistribution(providerMap);

        return stats;
    }

    private String getSimpleClassName(String fqcn) {
        if (fqcn == null) return "";
        int lastDot = fqcn.lastIndexOf('.');
        return lastDot >= 0 ? fqcn.substring(lastDot + 1) : fqcn;
    }
}
