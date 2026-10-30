package com.incident.triage.service;

import com.incident.triage.model.dto.AiAnalysisResult;
import com.incident.triage.model.dto.ParsedLogInfo;
import com.incident.triage.model.dto.RuleClassificationResult;
import com.incident.triage.model.entity.IncidentCategory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FallbackRuleAnalysisService {

    public AiAnalysisResult analyze(String rawLog, ParsedLogInfo parsedLog, RuleClassificationResult ruleResult) {
        IncidentCategory category = ruleResult.getCategory();
        String primaryEx = parsedLog.getPrimaryExceptionType() != null ? parsedLog.getPrimaryExceptionType() : "UnknownException";
        String rootCause = parsedLog.getRootCauseException() != null ? parsedLog.getRootCauseException() : primaryEx;
        String culprit = parsedLog.getCulpritClass() != null
                ? parsedLog.getCulpritClass() + "." + (parsedLog.getCulpritMethod() != null ? parsedLog.getCulpritMethod() : "")
                + (parsedLog.getCulpritLineNumber() != null ? ":" + parsedLog.getCulpritLineNumber() : "")
                : "Application Stack Trace";

        StringBuilder rootCauseBuilder = new StringBuilder();
        StringBuilder evidenceBuilder = new StringBuilder();
        List<String> fixes = new ArrayList<>();
        List<String> prevention = new ArrayList<>();

        evidenceBuilder.append("Deterministic Analysis Evidence:\n");
        evidenceBuilder.append("- Root Exception: ").append(rootCause).append("\n");
        evidenceBuilder.append("- Culprit Location: ").append(culprit).append("\n");
        if (parsedLog.getTimestamp() != null) {
            evidenceBuilder.append("- Incident Timestamp: ").append(parsedLog.getTimestamp()).append("\n");
        }
        if (parsedLog.getThreadName() != null) {
            evidenceBuilder.append("- Executing Thread: [").append(parsedLog.getThreadName()).append("]\n");
        }
        if (!ruleResult.getMatchedRules().isEmpty()) {
            evidenceBuilder.append("- Rule Engine Matches: ").append(String.join(", ", ruleResult.getMatchedRules())).append("\n");
        }

        switch (category) {
            case DATABASE:
                if (rootCause.toLowerCase().contains("hikaripool") || rootCause.toLowerCase().contains("connection is not available")) {
                    rootCauseBuilder.append("HikariCP Connection Pool Exhaustion: All active database connections are checked out and not being returned in time, causing arriving requests to time out waiting for an available connection.");
                    fixes.add("Inspect open transactions and ensure all connections, ResultSets, and EntityManagers are promptly closed or managed in @Transactional blocks.");
                    fixes.add("Increase HikariCP maximum-pool-size (e.g., from 10 to 25) and tune connectionTimeout (default 30000ms).");
                    fixes.add("Identify slow-running SQL queries holding connections open and add missing database indices.");
                    prevention.add("Enable HikariCP leakDetectionThreshold (e.g. 2000ms) to log unclosed connections.");
                    prevention.add("Set spring.jpa.open-in-view=false to prevent holding database connections through the entire web request lifecycle.");
                } else if (rootCause.toLowerCase().contains("deadlock")) {
                    rootCauseBuilder.append("Database Transaction Deadlock: Two or more concurrent transactions acquired row-level locks in reverse order, resulting in PostgreSQL aborting one transaction to break the cycle.");
                    fixes.add("Reorder SQL update statements across services so concurrent transactions acquire locks on entities in a uniform, sorted order (e.g., always sort entity IDs before batch updates).");
                    fixes.add("Implement Spring Retry (@Retryable) on CannotAcquireLockException with exponential backoff.");
                    fixes.add("Shorten transaction boundaries to minimize the time locks are held.");
                    prevention.add("Add database query profiling and monitor pg_stat_activity for long-running transactions.");
                } else if (rootCause.toLowerCase().contains("duplicate key") || rootCause.toLowerCase().contains("unique constraint")) {
                    rootCauseBuilder.append("Unique Constraint Violation: An INSERT or UPDATE statement attempted to insert a record whose unique key already exists in the database.");
                    fixes.add("Verify uniqueness or perform an UPSERT (ON CONFLICT DO UPDATE / DO NOTHING) in SQL or repository.");
                    fixes.add("Synchronize or lock critical write sections when multiple concurrent requests attempt to create the same resource.");
                    prevention.add("Use idempotent client tokens or unique transaction IDs to discard duplicate web requests.");
                } else {
                    rootCauseBuilder.append("Database Query or Driver Failure: An unhandled SQL exception occurred while executing persistence operations against PostgreSQL: ").append(rootCause);
                    fixes.add("Validate SQL syntax, column mapping, and database schema migrations (Liquibase/Flyway).");
                    fixes.add("Verify network reachability and credentials for PostgreSQL at localhost:5432.");
                    prevention.add("Incorporate integration tests that validate all JPA repositories against an embedded or test PostgreSQL instance.");
                }
                break;

            case MEMORY_RESOURCE:
                rootCauseBuilder.append("JVM OutOfMemoryError: Memory allocation failed because the JVM Heap Space or Metaspace has been completely exhausted.");
                fixes.add("Capture an automatic heap dump (-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/dumps) and inspect with Eclipse Memory Analyzer (MAT).");
                fixes.add("Check for memory leaks in static caches, unclosed streams, unbounded collections, or circular references.");
                fixes.add("Adjust JVM heap parameters (-Xms and -Xmx) to match workload requirements in the target container/host.");
                prevention.add("Implement pagination on queries that return large datasets instead of fetching entire tables into memory.");
                prevention.add("Configure Prometheus JMX exporter or Actuator metrics to alert when JVM heap usage exceeds 85%.");
                break;

            case NETWORK:
                rootCauseBuilder.append("Network Communication Timeout or Unreachable Host: The application attempted to initiate a socket connection or HTTP call that failed: ").append(rootCause);
                fixes.add("Verify target host DNS resolution, firewall rules, and network security group settings.");
                fixes.add("Check if downstream service is online, healthy, and accepting traffic.");
                fixes.add("Adjust socket connection and read timeout values (e.g. connectTimeout=5s, readTimeout=10s) to prevent thread hanging.");
                prevention.add("Wrap external HTTP and TCP calls with Resilience4j Circuit Breakers to fail fast when services degrade.");
                prevention.add("Implement exponential backoff retries for transient network drops.");
                break;

            case AUTHENTICATION:
                rootCauseBuilder.append("Authentication Failure or Invalid Authorization Token: A client request arrived with invalid, expired, or malformed credentials: ").append(rootCause);
                fixes.add("Check client token expiration times and verify that the JWT signing key matches the auth authority.");
                fixes.add("Ensure SecurityFilterChain configuration permits public endpoints or handles expired refresh tokens gracefully.");
                prevention.add("Provide structured 401/403 JSON responses with clear error codes rather than leaking unhandled stack traces to clients.");
                prevention.add("Implement token refresh rotation before short-lived access tokens expire.");
                break;

            case CONCURRENCY:
                rootCauseBuilder.append("Concurrent State Mutation or Optimistic Lock Conflict: Multiple execution threads simultaneously modified shared state without synchronization: ").append(rootCause);
                fixes.add("Replace non-thread-safe collections (e.g. ArrayList/HashMap) with ConcurrentHashMap, CopyOnWriteArrayList, or synchronized structures.");
                fixes.add("Catch ObjectOptimisticLockingFailureException and retry the operation with updated entity version.");
                prevention.add("Avoid modifying shared mutable state across asynchronous threads; adopt immutable data transfer objects.");
                break;

            case THIRD_PARTY:
                rootCauseBuilder.append("Upstream Third-Party Service Degradation: An external microservice or HTTP API dependency returned an error response or failed to respond.");
                fixes.add("Inspect downstream partner status pages and verify API rate limits or quota boundaries.");
                fixes.add("Ensure client request payload matches the external API contract.");
                prevention.add("Configure graceful fallback responses and cache stale responses when external APIs are degraded.");
                prevention.add("Add circuit breakers and bulkheads to isolate upstream failures from the core service.");
                break;

            case APPLICATION_LOGIC:
            default:
                if (rootCause.toLowerCase().contains("nullpointerexception")) {
                    rootCauseBuilder.append("Unhandled Null Reference (NullPointerException): An operation was invoked on an object reference that evaluated to null at runtime at ").append(culprit);
                    fixes.add("Inspect " + culprit + " and add explicit null-checks or use Optional<T> guards.");
                    fixes.add("Verify that dependency injection (Spring @Autowired/@Service) or external deserialization properly initialized all required fields.");
                    prevention.add("Use Java Record classes, Lombok @NonNull, or JetBrains/CheckerFramework @NotNull annotations.");
                    prevention.add("Enforce strict unit testing coverage for boundary conditions with null and empty inputs.");
                } else {
                    rootCauseBuilder.append("Application Runtime Fault: ").append(rootCause).append(" occurred during execution at ").append(culprit);
                    fixes.add("Inspect input validation logic and verify edge cases for incoming parameters.");
                    fixes.add("Review stack trace frames around " + culprit + " to trace caller parameters.");
                    prevention.add("Add comprehensive unit and integration tests covering the failing code path.");
                }
                break;
        }

        return new AiAnalysisResult(
                rootCauseBuilder.toString(),
                evidenceBuilder.toString(),
                fixes,
                prevention,
                0.90,
                "DETERMINISTIC_FALLBACK"
        );
    }
}
