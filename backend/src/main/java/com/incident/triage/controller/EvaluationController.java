package com.incident.triage.controller;

import com.incident.triage.model.dto.BenchmarkSampleDto;
import com.incident.triage.model.dto.EvaluationSummaryResponse;
import com.incident.triage.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/evaluation")
@Tag(name = "Evaluation", description = "Benchmark and quality evaluation suite across real-world incident test samples")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/run")
    @Operation(summary = "Execute automated evaluation benchmark suite", description = "Runs triage across pre-configured ground-truth incident logs and computes accuracy, precision, and latency metrics.")
    public ResponseEntity<EvaluationSummaryResponse> runEvaluation() {
        EvaluationSummaryResponse summary = evaluationService.runEvaluationSuite();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/samples")
    @Operation(summary = "List benchmark test samples", description = "Returns available pre-configured realistic test log samples with ground-truth labels for manual testing.")
    public ResponseEntity<List<BenchmarkSampleDto>> getBenchmarkSamples() {
        List<BenchmarkSampleDto> samples = evaluationService.getBenchmarkCases().stream()
                .map(c -> new BenchmarkSampleDto(
                        c.getId(),
                        c.getName(),
                        c.getExpectedCategory(),
                        c.getExpectedSeverity(),
                        c.getExpectedRootCauseKeywords(),
                        c.getRawLog()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(samples);
    }
}
