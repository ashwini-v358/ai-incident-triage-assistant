package com.incident.triage.controller;

import com.incident.triage.model.dto.*;
import com.incident.triage.model.entity.IncidentCategory;
import com.incident.triage.model.entity.IncidentSeverity;
import com.incident.triage.model.entity.IncidentStatus;
import com.incident.triage.service.IncidentTriageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incidents", description = "Endpoints for analyzing logs, retrieving triaged incidents, and managing incident status")
public class IncidentController {

    private final IncidentTriageService incidentTriageService;

    public IncidentController(IncidentTriageService incidentTriageService) {
        this.incidentTriageService = incidentTriageService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Analyze raw log text", description = "Parses pasted log text, extracts stack-trace, classifies category/severity, performs Gemini AI root cause analysis, and stores the incident.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Incident analyzed and persisted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or empty log content")
    })
    public ResponseEntity<IncidentResponse> analyzeRawLog(@Valid @RequestBody AnalyzeLogRequest request) {
        IncidentResponse response = incidentTriageService.analyzeAndSaveIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/analyze/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and analyze log file", description = "Uploads a log or text file (.log, .txt, .json), extracts exception & stack-trace, and triages the incident.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "File uploaded and triaged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or empty file uploaded")
    })
    public ResponseEntity<IncidentResponse> uploadLogFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam(value = "environment", required = false) String environment,
            @RequestParam(value = "title", required = false) String title) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String resolvedTitle = (title != null && !title.trim().isEmpty()) ? title : file.getOriginalFilename();

        AnalyzeLogRequest request = new AnalyzeLogRequest(content, serviceName, environment, resolvedTitle);
        IncidentResponse response = incidentTriageService.analyzeAndSaveIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List incidents with filters and pagination", description = "Retrieve list of incidents with optional filtering by category, severity, status, and text search.")
    public ResponseEntity<Page<IncidentResponse>> getIncidents(
            @Parameter(description = "Filter by incident category") @RequestParam(required = false) IncidentCategory category,
            @Parameter(description = "Filter by severity level") @RequestParam(required = false) IncidentSeverity severity,
            @Parameter(description = "Filter by workflow status") @RequestParam(required = false) IncidentStatus status,
            @Parameter(description = "Search term in title, exception, culprit, or root cause") @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IncidentResponse> incidents = incidentTriageService.getIncidents(category, severity, status, search, pageable);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident details by ID", description = "Retrieve comprehensive triage report, stack-trace, probable root-cause, evidence, and recommendations.")
    public ResponseEntity<IncidentResponse> getIncidentById(@PathVariable Long id) {
        return ResponseEntity.ok(incidentTriageService.getIncidentById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update incident workflow status", description = "Transition incident status between OPEN, INVESTIGATING, MITIGATED, and RESOLVED.")
    public ResponseEntity<IncidentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncidentStatusRequest request) {
        return ResponseEntity.ok(incidentTriageService.updateIncidentStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an incident", description = "Permanently removes an incident from PostgreSQL storage.")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        incidentTriageService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard analytics and metric distribution", description = "Returns aggregates of total, open, severity counts, category breakdowns, and AI confidence scores.")
    public ResponseEntity<IncidentStatsDto> getStats() {
        return ResponseEntity.ok(incidentTriageService.getStats());
    }
}

