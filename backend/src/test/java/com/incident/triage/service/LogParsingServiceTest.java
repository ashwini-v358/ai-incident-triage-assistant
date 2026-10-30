package com.incident.triage.service;

import com.incident.triage.model.dto.ParsedLogInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogParsingServiceTest {

    private LogParsingService service;

    @BeforeEach
    void setUp() {
        service = new LogParsingService();
    }

    @Test
    @DisplayName("Should extract timestamp, log level, thread, and root cause from multi-line stack trace")
    void testParseDatabaseLogWithCausedBy() {
        String log = """
                2026-10-30 08:14:22.451 ERROR [http-nio-8080-exec-14] com.zaxxer.hikari.pool.HikariPool : HikariPool-1 - Connection is not available, request timed out after 30005ms.
                org.springframework.dao.CannotAcquireLockException: Could not open JPA EntityManager for transaction; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to acquire JDBC Connection
                    at org.springframework.orm.jpa.JpaTransactionManager.doBegin(JpaTransactionManager.java:449)
                    at com.example.orderservice.service.OrderCheckoutService.processPayment(OrderCheckoutService.java:142)
                Caused by: org.postgresql.util.PSQLException: FATAL: remaining connection slots are reserved
                    at org.postgresql.core.v3.ConnectionFactoryImpl.doAuthentication(ConnectionFactoryImpl.java:643)
                """;

        ParsedLogInfo parsed = service.parse(log);

        assertNotNull(parsed);
        assertEquals("2026-10-30 08:14:22.451", parsed.getTimestamp());
        assertEquals("ERROR", parsed.getLogLevel());
        assertEquals("http-nio-8080-exec-14", parsed.getThreadName());
        assertEquals("org.springframework.dao.CannotAcquireLockException", parsed.getPrimaryExceptionType());
        assertTrue(parsed.getRootCauseException().contains("org.postgresql.util.PSQLException"));
        assertTrue(parsed.getRootCauseException().contains("FATAL: remaining connection slots are reserved"));
        assertEquals("com.example.orderservice.service.OrderCheckoutService", parsed.getCulpritClass());
        assertEquals("processPayment", parsed.getCulpritMethod());
        assertEquals(142, parsed.getCulpritLineNumber());
        assertNotNull(parsed.getStackTrace());
        assertFalse(parsed.getNestedCauses().isEmpty());
    }

    @Test
    @DisplayName("Should extract OutOfMemoryError and heap space location")
    void testParseOutOfMemoryError() {
        String log = """
                2026-10-30 10:02:18.892 FATAL [catalina-exec-44] org.apache.catalina.core.StandardWrapperValve : Servlet.service() threw exception
                java.lang.OutOfMemoryError: Java heap space
                    at java.base/java.util.Arrays.copyOf(Arrays.java:3516)
                    at com.example.analytics.service.ReportExporterService.generatePdfReport(ReportExporterService.java:215)
                """;

        ParsedLogInfo parsed = service.parse(log);

        assertNotNull(parsed);
        assertEquals("FATAL", parsed.getLogLevel());
        assertEquals("java.lang.OutOfMemoryError", parsed.getPrimaryExceptionType());
        assertTrue(parsed.getRootCauseException().contains("Java heap space"));
        assertEquals("com.example.analytics.service.ReportExporterService", parsed.getCulpritClass());
        assertEquals("generatePdfReport", parsed.getCulpritMethod());
        assertEquals(215, parsed.getCulpritLineNumber());
    }

    @Test
    @DisplayName("Should safely handle null or empty logs")
    void testParseEmptyLog() {
        ParsedLogInfo parsed = service.parse("");
        assertNotNull(parsed);
        assertEquals("Empty log provided", parsed.getMainErrorMessage());

        ParsedLogInfo nullParsed = service.parse(null);
        assertNotNull(nullParsed);
        assertEquals("Empty log provided", nullParsed.getMainErrorMessage());
    }
}

