package com.incident.triage.controller;

import com.incident.triage.config.GeminiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "System Health", description = "Endpoints for database, AI provider, and system health status")
public class HealthController {

    private final DataSource dataSource;
    private final GeminiConfig geminiConfig;

    public HealthController(DataSource dataSource, GeminiConfig geminiConfig) {
        this.dataSource = dataSource;
        this.geminiConfig = geminiConfig;
    }

    @GetMapping
    @Operation(summary = "Check backend and dependency health", description = "Verifies PostgreSQL connectivity, Gemini AI configuration, and JVM system status.")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());

        // Database status
        Map<String, Object> dbStatus = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(2);
            dbStatus.put("connected", valid);
            dbStatus.put("databaseProduct", conn.getMetaData().getDatabaseProductName());
            dbStatus.put("databaseVersion", conn.getMetaData().getDatabaseProductVersion());
            dbStatus.put("status", valid ? "UP" : "DOWN");
        } catch (Exception e) {
            dbStatus.put("connected", false);
            dbStatus.put("status", "DOWN");
            dbStatus.put("error", e.getMessage());
            health.put("status", "DEGRADED");
        }
        health.put("database", dbStatus);

        // Gemini AI status
        Map<String, Object> aiStatus = new LinkedHashMap<>();
        aiStatus.put("hasApiKey", geminiConfig.hasApiKey());
        aiStatus.put("model", geminiConfig.getModel());
        aiStatus.put("fallbackEnabled", geminiConfig.isFallbackEnabled());
        aiStatus.put("activeMode", geminiConfig.hasApiKey() ? "GEMINI_WITH_FALLBACK" : "DETERMINISTIC_FALLBACK_ONLY");
        health.put("aiProvider", aiStatus);

        // JVM Stats
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());
        jvm.put("totalMemoryMb", runtime.totalMemory() / (1024 * 1024));
        jvm.put("freeMemoryMb", runtime.freeMemory() / (1024 * 1024));
        jvm.put("maxMemoryMb", runtime.maxMemory() / (1024 * 1024));
        jvm.put("availableProcessors", runtime.availableProcessors());
        health.put("jvm", jvm);

        return ResponseEntity.ok(health);
    }
}

