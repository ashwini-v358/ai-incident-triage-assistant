package com.incident.triage.service;

import com.incident.triage.model.dto.ParsedLogInfo;
import com.incident.triage.model.dto.RuleClassificationResult;
import com.incident.triage.model.entity.IncidentCategory;
import com.incident.triage.model.entity.IncidentSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleClassificationServiceTest {

    private RuleClassificationService service;
    private LogParsingService parsingService;

    @BeforeEach
    void setUp() {
        service = new RuleClassificationService();
        parsingService = new LogParsingService();
    }

    @Test
    @DisplayName("Should classify PostgreSQL HikariCP timeout as DATABASE category and HIGH severity")
    void testClassifyHikariPoolTimeout() {
        String log = "HikariPool-1 - Connection is not available, request timed out after 30005ms. org.postgresql.util.PSQLException: FATAL: remaining connection slots are reserved";
        ParsedLogInfo parsed = parsingService.parse(log);

        RuleClassificationResult result = service.classify(log, parsed);

        assertEquals(IncidentCategory.DATABASE, result.getCategory());
        assertEquals(IncidentSeverity.CRITICAL, result.getSeverity());
        assertFalse(result.getMatchedRules().isEmpty());
        assertTrue(result.getRuleConfidence() >= 0.80);
    }

    @Test
    @DisplayName("Should classify JVM OutOfMemoryError as MEMORY_RESOURCE and CRITICAL severity")
    void testClassifyOOM() {
        String log = "java.lang.OutOfMemoryError: Java heap space. Metaspace exhaustion. System fatal.";
        ParsedLogInfo parsed = parsingService.parse(log);

        RuleClassificationResult result = service.classify(log, parsed);

        assertEquals(IncidentCategory.MEMORY_RESOURCE, result.getCategory());
        assertEquals(IncidentSeverity.CRITICAL, result.getSeverity());
    }

    @Test
    @DisplayName("Should classify ExpiredJwtException as AUTHENTICATION category")
    void testClassifyJwtExpiration() {
        String log = "io.jsonwebtoken.ExpiredJwtException: JWT expired at 2026-10-30T10:15:00Z. 401 Unauthorized";
        ParsedLogInfo parsed = parsingService.parse(log);

        RuleClassificationResult result = service.classify(log, parsed);

        assertEquals(IncidentCategory.AUTHENTICATION, result.getCategory());
    }

    @Test
    @DisplayName("Should classify SocketTimeoutException as NETWORK category")
    void testClassifySocketTimeout() {
        String log = "org.springframework.web.client.ResourceAccessException: I/O error: java.net.SocketTimeoutException: Read timed out";
        ParsedLogInfo parsed = parsingService.parse(log);

        RuleClassificationResult result = service.classify(log, parsed);

        assertEquals(IncidentCategory.NETWORK, result.getCategory());
    }

    @Test
    @DisplayName("Should classify NullPointerException as APPLICATION_LOGIC category")
    void testClassifyNullPointer() {
        String log = "java.lang.NullPointerException: Cannot invoke 'Order.getId()' because order is null";
        ParsedLogInfo parsed = parsingService.parse(log);

        RuleClassificationResult result = service.classify(log, parsed);

        assertEquals(IncidentCategory.APPLICATION_LOGIC, result.getCategory());
    }
}
