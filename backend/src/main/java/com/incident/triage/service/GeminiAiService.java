package com.incident.triage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incident.triage.config.GeminiConfig;
import com.incident.triage.model.dto.AiAnalysisResult;
import com.incident.triage.model.dto.ParsedLogInfo;
import com.incident.triage.model.dto.RuleClassificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    private final GeminiConfig geminiConfig;
    private final FallbackRuleAnalysisService fallbackService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public GeminiAiService(GeminiConfig geminiConfig,
                            FallbackRuleAnalysisService fallbackService,
                            ObjectMapper objectMapper) {
        this.geminiConfig = geminiConfig;
        this.fallbackService = fallbackService;
        this.objectMapper = objectMapper;

        // Custom RestTemplate with explicit connect & read timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.min(geminiConfig.getTimeoutMs(), 10000));
        factory.setReadTimeout(geminiConfig.getTimeoutMs());
        this.restTemplate = new RestTemplate(factory);
    }

    public AiAnalysisResult analyze(String rawLog, ParsedLogInfo parsedLog, RuleClassificationResult ruleResult) {
        if (!geminiConfig.hasApiKey()) {
            log.info("Gemini API key not configured or empty. Using deterministic expert fallback analysis.");
            return fallbackService.analyze(rawLog, parsedLog, ruleResult);
        }

        try {
            log.info("Calling Google Gemini AI ({}) for incident root-cause analysis...", geminiConfig.getModel());
            AiAnalysisResult result = callGeminiApi(rawLog, parsedLog, ruleResult);
            if (result != null) {
                return result;
            }
        } catch (Exception ex) {
            log.warn("Gemini AI API call encountered an error: {}. Triggering fallback analysis.", ex.getMessage());
        }

        if (geminiConfig.isFallbackEnabled()) {
            log.info("Activating deterministic expert system fallback for incident analysis.");
            return fallbackService.analyze(rawLog, parsedLog, ruleResult);
        } else {
            throw new RuntimeException("Gemini AI analysis failed and fallback is disabled.");
        }
    }

    private AiAnalysisResult callGeminiApi(String rawLog, ParsedLogInfo parsedLog, RuleClassificationResult ruleResult) throws Exception {
        String url = String.format("%s/v1beta/models/%s:generateContent?key=%s",
                geminiConfig.getBaseUrl(), geminiConfig.getModel(), geminiConfig.getApiKey());

        String prompt = buildPrompt(rawLog, parsedLog, ruleResult);

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> contentPart = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        parts.add(Collections.singletonMap("text", prompt));
        contentPart.put("parts", parts);
        contents.add(contentPart);
        requestBody.put("contents", contents);

        // Generation config requesting structured output
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("responseMimeType", "application/json");
        requestBody.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return parseGeminiResponse(response.getBody());
        }

        return null;
    }

    private String buildPrompt(String rawLog, ParsedLogInfo parsedLog, RuleClassificationResult ruleResult) {
        // Sanitize raw log length if huge
        String truncatedLog = rawLog.length() > 6000 ? rawLog.substring(0, 6000) + "\n...[TRUNCATED]" : rawLog;

        return "You are an expert SRE and Senior Site Reliability Engineer performing root cause analysis for an application incident.\n" +
                "Analyze the following incident log, parsed exception details, and rule-engine classifications.\n\n" +
                "Detected Rule Category: " + ruleResult.getCategory() + "\n" +
                "Detected Rule Severity: " + ruleResult.getSeverity() + "\n" +
                "Parsed Exception Type: " + parsedLog.getPrimaryExceptionType() + "\n" +
                "Parsed Root Cause: " + parsedLog.getRootCauseException() + "\n" +
                "Culprit Class: " + parsedLog.getCulpritClass() + "\n" +
                "Culprit Method: " + parsedLog.getCulpritMethod() + "\n" +
                "Culprit Line Number: " + parsedLog.getCulpritLineNumber() + "\n\n" +
                "INCIDENT RAW LOG TRACE:\n" +
                "```\n" + truncatedLog + "\n```\n\n" +
                "Respond strictly with a JSON object adhering to this schema:\n" +
                "{\n" +
                "  \"probableRootCause\": \"Clear, concise 2-4 sentence explanation of the technical root cause\",\n" +
                "  \"evidence\": \"Specific log lines, error codes, and thread/stack trace signals confirming this hypothesis\",\n" +
                "  \"recommendedFixes\": [\"Specific immediate action 1\", \"Action 2\", \"Action 3\"],\n" +
                "  \"preventionStrategies\": [\"Long-term prevention 1\", \"Long-term prevention 2\"],\n" +
                "  \"confidenceScore\": 0.95\n" +
                "}";
    }

    private AiAnalysisResult parseGeminiResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    String text = parts.get(0).path("text").asText();
                    if (text != null && !text.isEmpty()) {
                        // In case text has markdown json fences
                        text = text.trim();
                        if (text.startsWith("```json")) {
                            text = text.substring(7);
                        }
                        if (text.startsWith("```")) {
                            text = text.substring(3);
                        }
                        if (text.endsWith("```")) {
                            text = text.substring(0, text.length() - 3);
                        }
                        text = text.trim();

                        JsonNode jsonResult = objectMapper.readTree(text);
                        String probableRootCause = jsonResult.path("probableRootCause").asText();
                        String evidence = jsonResult.path("evidence").asText();
                        double confidence = jsonResult.path("confidenceScore").asDouble(0.92);

                        List<String> fixes = new ArrayList<>();
                        JsonNode fixesNode = jsonResult.path("recommendedFixes");
                        if (fixesNode.isArray()) {
                            for (JsonNode f : fixesNode) {
                                fixes.add(f.asText());
                            }
                        }

                        List<String> prevention = new ArrayList<>();
                        JsonNode prevNode = jsonResult.path("preventionStrategies");
                        if (prevNode.isArray()) {
                            for (JsonNode p : prevNode) {
                                prevention.add(p.asText());
                            }
                        }

                        return new AiAnalysisResult(
                                probableRootCause,
                                evidence,
                                fixes,
                                prevention,
                                confidence,
                                "GEMINI_" + geminiConfig.getModel().toUpperCase().replace("-", "_")
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response JSON: {}", e.getMessage());
        }
        return null;
    }
}
