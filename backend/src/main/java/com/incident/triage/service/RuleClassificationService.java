package com.incident.triage.service;

import com.incident.triage.model.dto.ParsedLogInfo;
import com.incident.triage.model.dto.RuleClassificationResult;
import com.incident.triage.model.entity.IncidentCategory;
import com.incident.triage.model.entity.IncidentSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RuleClassificationService {

    private static final Logger log = LoggerFactory.getLogger(RuleClassificationService.class);

    public RuleClassificationResult classify(String rawLog, ParsedLogInfo parsedLog) {
        List<String> matchedRules = new ArrayList<>();
        String combined = ((rawLog != null ? rawLog : "") + " " +
                (parsedLog.getRootCauseException() != null ? parsedLog.getRootCauseException() : "") + " " +
                (parsedLog.getPrimaryExceptionType() != null ? parsedLog.getPrimaryExceptionType() : "") + " " +
                (parsedLog.getMainErrorMessage() != null ? parsedLog.getMainErrorMessage() : "")).toLowerCase();

        IncidentCategory category = determineCategory(combined, parsedLog, matchedRules);
        IncidentSeverity severity = determineSeverity(combined, category, matchedRules);

        double confidence = calculateConfidence(matchedRules);
        String rationale = "Incident classified as " + category + " [" + severity + "] based on "
                + matchedRules.size() + " matched pattern rules: " + String.join("; ", matchedRules);

        log.debug("Rule classification: Category={}, Severity={}, Rules={}", category, severity, matchedRules);
        return new RuleClassificationResult(category, severity, matchedRules, confidence, rationale);
    }

    private IncidentCategory determineCategory(String text, ParsedLogInfo parsed, List<String> rules) {
        // 1. Memory and System Resource limits (High precedence)
        if (text.contains("outofmemoryerror") ||
                text.contains("java heap space") ||
                text.contains("metaspace") ||
                text.contains("gc overhead limit exceeded") ||
                text.contains("too many open files") ||
                text.contains("unable to create new native thread")) {
            rules.add("RULE_MEMORY_RESOURCE_EXHAUSTION: Detected JVM OutOfMemoryError, Heap/Metaspace exhaustion, or thread/FD limit");
            return IncidentCategory.MEMORY_RESOURCE;
        }

        // 2. Database & Persistence issues
        if (text.contains("sqlexception") ||
                text.contains("psqlexception") ||
                text.contains("hikaripool") ||
                text.contains("connection is not available, request timed out") ||
                text.contains("deadlock detected") ||
                text.contains("lockacquisitionexception") ||
                text.contains("dataintegrityviolationexception") ||
                text.contains("duplicate key value violates unique constraint") ||
                text.contains("table or view does not exist") ||
                text.contains("cannotgetjdbcconnectionexception") ||
                text.contains("could not extract resultset") ||
                text.contains("foreign key constraint") ||
                text.contains("org.hibernate.exception")) {
            rules.add("RULE_DATABASE_PERSISTENCE: Detected JDBC/PostgreSQL/HikariCP exception, constraint violation, or lock contention");
            return IncidentCategory.DATABASE;
        }

        // 3. Authentication & Security
        if (text.contains("badcredentialsexception") ||
                text.contains("accessdeniedexception") ||
                text.contains("expiredjwtexception") ||
                text.contains("signatureexception") ||
                text.contains("malformedjwtexception") ||
                text.contains("jwt") ||
                text.contains("unauthorized") ||
                text.contains("401 unauthorized") ||
                text.contains("403 forbidden") ||
                text.contains("forbidden") ||
                text.contains("insufficientauthenticationexception") ||
                text.contains("authentication failed")) {
            rules.add("RULE_AUTH_SECURITY: Detected authentication failure, invalid credentials, or unauthorized access violation");
            return IncidentCategory.AUTHENTICATION;
        }

        // 4. Concurrency & Threading
        if (text.contains("concurrentmodificationexception") ||
                text.contains("optimisticlockingfailureexception") ||
                text.contains("staleobjectstateexception") ||
                text.contains("thread starvation") ||
                text.contains("race condition")) {
            rules.add("RULE_CONCURRENCY_THREADING: Detected ConcurrentModificationException or Optimistic Locking failure");
            return IncidentCategory.CONCURRENCY;
        }

        // 5. Network & Connectivity
        if (text.contains("sockettimeoutexception") ||
                text.contains("connectexception") ||
                text.contains("unknownhostexception") ||
                text.contains("httphostconnectexception") ||
                text.contains("connection refused") ||
                text.contains("connection reset") ||
                text.contains("no route to host") ||
                text.contains("broken pipe") ||
                text.contains("sslhandshakeexception")) {
            rules.add("RULE_NETWORK_CONNECTIVITY: Detected SocketTimeoutException, ConnectException, or connection breakdown");
            return IncidentCategory.NETWORK;
        }

        // 6. Third-Party & External API
        if (text.contains("feignexception") ||
                text.contains("restclientexception") ||
                text.contains("webclientresponseexception") ||
                text.contains("502 bad gateway") ||
                text.contains("503 service unavailable") ||
                text.contains("504 gateway timeout") ||
                text.contains("resourceaccessexception") ||
                text.contains("external service returned") ||
                text.contains("downstream api error")) {
            rules.add("RULE_THIRD_PARTY_UPSTREAM: Detected microservice, Feign client, or upstream HTTP gateway failure");
            return IncidentCategory.THIRD_PARTY;
        }

        // 7. Application Runtime Logic
        if (text.contains("nullpointerexception") ||
                text.contains("illegalargumentexception") ||
                text.contains("illegalstateexception") ||
                text.contains("indexoutofboundsexception") ||
                text.contains("classcastexception") ||
                text.contains("numberformatexception") ||
                text.contains("nosuchmethoderror") ||
                text.contains("unsupportedoperationexception") ||
                text.contains("jsonparseexception") ||
                text.contains("cannot deserialize")) {
            rules.add("RULE_APPLICATION_LOGIC: Detected standard JVM runtime fault (NullPointer, IllegalState, ClassCast, or format error)");
            return IncidentCategory.APPLICATION_LOGIC;
        }

        rules.add("RULE_UNCLASSIFIED: No standard deterministic pattern matched; falling back to heuristic evaluation");
        return IncidentCategory.UNKNOWN;
    }

    private IncidentSeverity determineSeverity(String text, IncidentCategory category, List<String> rules) {
        // Critical conditions: OOM, system deadlock, crash, fatal
        if (category == IncidentCategory.MEMORY_RESOURCE ||
                text.contains("fatal") ||
                text.contains("deadlock detected") ||
                text.contains("crash") ||
                text.contains("system halt") ||
                text.contains("data corruption") ||
                text.contains("service unavailable")) {
            rules.add("SEVERITY_RULE_CRITICAL: OOM, Deadlock, or system-stopping fatal error detected");
            return IncidentSeverity.CRITICAL;
        }

        // High conditions: DB connection pool exhaustion, Auth breaches, Network drops
        if (category == IncidentCategory.DATABASE && text.contains("hikaripool") ||
                category == IncidentCategory.DATABASE && text.contains("connection is not available") ||
                text.contains("500 internal server error") ||
                text.contains("connection refused") ||
                text.contains("connection timeout") ||
                text.contains("cannotgetjdbcconnectionexception") ||
                category == IncidentCategory.NETWORK) {
            rules.add("SEVERITY_RULE_HIGH: Database connectivity outage or widespread network/service disruption");
            return IncidentSeverity.HIGH;
        }

        // Medium conditions: Application logic NPE, standard Auth failure, 4xx, concurrency optimistic lock
        if (category == IncidentCategory.APPLICATION_LOGIC ||
                category == IncidentCategory.AUTHENTICATION ||
                category == IncidentCategory.CONCURRENCY ||
                category == IncidentCategory.THIRD_PARTY ||
                category == IncidentCategory.DATABASE) {
            rules.add("SEVERITY_RULE_MEDIUM: Request-level runtime or operational failure affecting specific user interactions");
            return IncidentSeverity.MEDIUM;
        }

        rules.add("SEVERITY_RULE_LOW: Non-critical warning or transient recoverable event");
        return IncidentSeverity.LOW;
    }

    private double calculateConfidence(List<String> rules) {
        if (rules.isEmpty()) return 0.50;
        int count = rules.size();
        if (count >= 3) return 0.95;
        if (count == 2) return 0.88;
        return 0.80;
    }
}
