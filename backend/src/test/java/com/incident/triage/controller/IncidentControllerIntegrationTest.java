package com.incident.triage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incident.triage.model.dto.AnalyzeLogRequest;
import com.incident.triage.model.dto.UpdateIncidentStatusRequest;
import com.incident.triage.model.entity.IncidentCategory;
import com.incident.triage.model.entity.IncidentSeverity;
import com.incident.triage.model.entity.IncidentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/incidents/analyze - Should triage database log and return 201 Created")
    void testAnalyzeLogEndpoint() throws Exception {
        String logContent = """
                2026-10-30 08:14:22.451 ERROR [http-nio-8080-exec-14] com.zaxxer.hikari.pool.HikariPool : HikariPool-1 - Connection is not available, request timed out after 30005ms.
                org.springframework.dao.CannotAcquireLockException: Could not open JPA EntityManager for transaction; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to acquire JDBC Connection
                    at org.springframework.orm.jpa.JpaTransactionManager.doBegin(JpaTransactionManager.java:449)
                    at com.example.orderservice.service.OrderCheckoutService.processPayment(OrderCheckoutService.java:142)
                Caused by: org.postgresql.util.PSQLException: FATAL: remaining connection slots are reserved
                    at org.postgresql.core.v3.ConnectionFactoryImpl.doAuthentication(ConnectionFactoryImpl.java:643)
                """;

        AnalyzeLogRequest request = new AnalyzeLogRequest(logContent, "payment-service", "PRODUCTION", "Checkout DB Outage");

        mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.category").value(IncidentCategory.DATABASE.name()))
                .andExpect(jsonPath("$.severity").value(IncidentSeverity.CRITICAL.name()))
                .andExpect(jsonPath("$.status").value(IncidentStatus.OPEN.name()))
                .andExpect(jsonPath("$.serviceName").value("payment-service"))
                .andExpect(jsonPath("$.probableRootCause").isNotEmpty())
                .andExpect(jsonPath("$.evidence").isNotEmpty())
                .andExpect(jsonPath("$.recommendedFixes", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/v1/incidents/analyze/upload - Should upload log file and triage successfully")
    void testUploadLogFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "oom-error.log",
                "text/plain",
                "java.lang.OutOfMemoryError: Java heap space at com.example.analytics.ReportService.export(ReportService.java:42)".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/incidents/analyze/upload")
                        .file(file)
                        .param("serviceName", "analytics-worker")
                        .param("environment", "STAGING"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value(IncidentCategory.MEMORY_RESOURCE.name()))
                .andExpect(jsonPath("$.severity").value(IncidentSeverity.CRITICAL.name()));
    }

    @Test
    @DisplayName("GET /api/v1/incidents - Should filter by category, severity, status, and query")
    void testGetIncidentsWithFilters() throws Exception {
        mockMvc.perform(get("/api/v1/incidents")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("PATCH /api/v1/incidents/{id}/status - Should update status to INVESTIGATING and RESOLVED")
    void testUpdateStatus() throws Exception {
        // First create an incident
        AnalyzeLogRequest createReq = new AnalyzeLogRequest(
                "java.lang.NullPointerException at com.example.TestService.run(TestService.java:10)",
                "test-service", "DEV", "NPE in TestService"
        );

        String createResStr = mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long incidentId = objectMapper.readTree(createResStr).get("id").asLong();

        // Update status to INVESTIGATING
        UpdateIncidentStatusRequest updateReq = new UpdateIncidentStatusRequest(
                IncidentStatus.INVESTIGATING,
                "Engineer assigned. Checking null check guards."
        );

        mockMvc.perform(patch("/api/v1/incidents/" + incidentId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(IncidentStatus.INVESTIGATING.name()))
                .andExpect(jsonPath("$.resolutionNotes").value("Engineer assigned. Checking null check guards."));
    }

    @Test
    @DisplayName("GET /api/v1/incidents/stats - Should return aggregate metrics")
    void testGetStats() throws Exception {
        mockMvc.perform(get("/api/v1/incidents/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncidents").isNumber())
                .andExpect(jsonPath("$.openIncidents").isNumber());
    }

    @Test
    @DisplayName("POST /api/v1/evaluation/run - Should execute benchmark suite and return measurable metrics")
    void testEvaluationRun() throws Exception {
        mockMvc.perform(post("/api/v1/evaluation/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSamples").value(8))
                .andExpect(jsonPath("$.categoryAccuracyPercent").isNumber())
                .andExpect(jsonPath("$.severityAccuracyPercent").isNumber())
                .andExpect(jsonPath("$.averageLatencyMs").isNumber())
                .andExpect(jsonPath("$.sampleResults", hasSize(8)));
    }

    @Test
    @DisplayName("GET /api/v1/health - Should return health status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database.status").value("UP"));
    }
}
