package com.incident.triage.service;

import com.incident.triage.model.dto.*;
import com.incident.triage.model.entity.IncidentCategory;
import com.incident.triage.model.entity.IncidentSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationService.class);

    private final LogParsingService logParsingService;
    private final RuleClassificationService ruleClassificationService;
    private final GeminiAiService geminiAiService;

    public EvaluationService(LogParsingService logParsingService,
                             RuleClassificationService ruleClassificationService,
                             GeminiAiService geminiAiService) {
        this.logParsingService = logParsingService;
        this.ruleClassificationService = ruleClassificationService;
        this.geminiAiService = geminiAiService;
    }

    public static class BenchmarkCase {
        private final String id;
        private final String name;
        private final IncidentCategory expectedCategory;
        private final IncidentSeverity expectedSeverity;
        private final String expectedRootCauseKeywords;
        private final String rawLog;

        public BenchmarkCase(String id, String name, IncidentCategory expectedCategory,
                             IncidentSeverity expectedSeverity, String expectedRootCauseKeywords, String rawLog) {
            this.id = id;
            this.name = name;
            this.expectedCategory = expectedCategory;
            this.expectedSeverity = expectedSeverity;
            this.expectedRootCauseKeywords = expectedRootCauseKeywords;
            this.rawLog = rawLog;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public IncidentCategory getExpectedCategory() { return expectedCategory; }
        public IncidentSeverity getExpectedSeverity() { return expectedSeverity; }
        public String getExpectedRootCauseKeywords() { return expectedRootCauseKeywords; }
        public String getRawLog() { return rawLog; }
    }

    public List<BenchmarkCase> getBenchmarkCases() {
        List<BenchmarkCase> cases = new ArrayList<>();

        cases.add(new BenchmarkCase(
                "BM-01",
                "PostgreSQL HikariCP Connection Pool Starvation",
                IncidentCategory.DATABASE,
                IncidentSeverity.CRITICAL,
                "connection, pool, hikari, exhaustion, timeout",
                """
                2026-10-30 08:14:22.451 ERROR [http-nio-8080-exec-14] com.zaxxer.hikari.pool.HikariPool : HikariPool-1 - Connection is not available, request timed out after 30005ms.
                org.springframework.dao.CannotAcquireLockException: Could not open JPA EntityManager for transaction; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to acquire JDBC Connection
                    at org.springframework.orm.jpa.JpaTransactionManager.doBegin(JpaTransactionManager.java:449)
                    at org.springframework.transaction.support.AbstractPlatformTransactionManager.getTransaction(AbstractPlatformTransactionManager.java:373)
                    at com.example.orderservice.service.OrderCheckoutService.processPayment(OrderCheckoutService.java:142)
                Caused by: org.postgresql.util.PSQLException: FATAL: remaining connection slots are reserved for non-replication superuser connections
                    at org.postgresql.core.v3.ConnectionFactoryImpl.doAuthentication(ConnectionFactoryImpl.java:643)
                    at org.postgresql.core.v3.ConnectionFactoryImpl.tryConnect(ConnectionFactoryImpl.java:204)
                """
        ));

        cases.add(new BenchmarkCase(
                "BM-02",
                "PostgreSQL Database Row Lock Deadlock",
                IncidentCategory.DATABASE,
                IncidentSeverity.CRITICAL,
                "deadlock, lock, transaction, concurrent",
                """
                2026-10-30 09:21:05.112 ERROR [payment-worker-3] org.hibernate.engine.jdbc.spi.SqlExceptionHelper : ERROR: deadlock detected
                  Detail: Process 14202 waits for ShareLock on transaction 89123; blocked by process 14209.
                org.springframework.dao.CannotAcquireLockException: could not execute statement [ERROR: deadlock detected]
                    at org.hibernate.exception.internal.SQLStateConverter.convert(SQLStateConverter.java:115)
                    at com.example.inventory.service.StockService.reserveStock(StockService.java:88)
                Caused by: org.postgresql.util.PSQLException: ERROR: deadlock detected
                  Detail: Process 14202 waits for ShareLock on transaction 89123; blocked by process 14209.
                    at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2675)
                """
        ));

        cases.add(new BenchmarkCase(
                "BM-03",
                "JVM Heap Memory OutOfMemoryError",
                IncidentCategory.MEMORY_RESOURCE,
                IncidentSeverity.CRITICAL,
                "heap, memory, outofmemory, oom, allocation",
                """
                2026-10-30 10:02:18.892 FATAL [catalina-exec-44] org.apache.catalina.core.StandardWrapperValve : Servlet.service() for servlet [dispatcherServlet] threw exception
                java.lang.OutOfMemoryError: Java heap space
                    at java.base/java.util.Arrays.copyOf(Arrays.java:3516)
                    at java.base/java.io.ByteArrayOutputStream.grow(ByteArrayOutputStream.java:120)
                    at com.example.analytics.service.ReportExporterService.generatePdfReport(ReportExporterService.java:215)
                    at com.example.analytics.controller.ReportController.export(ReportController.java:54)
                """
        ));

        cases.add(new BenchmarkCase(
                "BM-04",
                "Downstream REST Microservice Socket Timeout",
                IncidentCategory.NETWORK,
                IncidentSeverity.HIGH,
                "timeout, socket, read, connect, network",
                """
                2026-10-30 10:45:33.204 ERROR [http-nio-8080-exec-8] com.example.payment.client.StripeGatewayClient : Remote call to payment gateway failed
                org.springframework.web.client.ResourceAccessException: I/O error on POST request for "https://api.stripe.com/v1/charges": Read timed out
                    at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:891)
                    at com.example.payment.client.StripeGatewayClient.charge(StripeGatewayClient.java:78)
                Caused by: java.net.SocketTimeoutException: Read timed out
                    at java.base/sun.nio.ch.NioSocketImpl.timedRead(NioSocketImpl.java:278)
                    at java.base/java.net.SocketInputStream.read(SocketInputStream.java:123)
                """
        ));

        cases.add(new BenchmarkCase(
                "BM-05",
                "JWT Authentication Signature & Token Expiration",
                IncidentCategory.AUTHENTICATION,
                IncidentSeverity.MEDIUM,
                "jwt, expired, token, auth, signature",
                """
                2026-10-30 11:15:00.672 WARN [http-nio-8080-exec-2] com.example.security.JwtAuthenticationFilter : JWT token validation failed
                io.jsonwebtoken.ExpiredJwtException: JWT expired at 2026-10-30T10:15:00Z. Current time: 2026-10-30T11:15:00Z, a difference of 3600000 milliseconds.
                    at io.jsonwebtoken.impl.DefaultJwtParser.parse(DefaultJwtParser.java:420)
                    at com.example.security.JwtTokenProvider.validateToken(JwtTokenProvider.java:65)
                    at com.example.security.JwtAuthenticationFilter.doFilterInternal(JwtAuthenticationFilter.java:45)
                """
        ));

        cases.add(new BenchmarkCase(
                "BM-06",
                "Concurrent Modification Exception in Worker Thread",
                IncidentCategory.CONCURRENCY,
                IncidentSeverity.MEDIUM,
                "concurrent, modification, collection, iterator, race",
                """
                2026-10-30 11:55:40.312 ERROR [batch-processor-1] com.example.batch.UserSyncTask : Failed to complete batch synchronisation cycle
                java.util.ConcurrentModificationException: null
                    at java.base/java.util.ArrayList$Itr.checkForComodification(ArrayList.java:1096)
                    at java.base/java.util.ArrayList$Itr.next(ArrayList.java:1050)
                    at com.example.batch.UserSyncTask.broadcastStatusUpdate(UserSyncTask.java:134)
                    at com.example.batch.UserSyncTask.run(UserSyncTask.java:59)
                """
        ));

        cases.add(new BenchmarkCase(
                "BM-07",
                "NullPointerException in Order Processing",
                IncidentCategory.APPLICATION_LOGIC,
                IncidentSeverity.MEDIUM,
                "null, nullpointer, pointer, dereference, object",
                """
                2026-10-30 12:10:11.901 ERROR [http-nio-8080-exec-19] com.example.order.controller.OrderController : Uncaught exception processing order submission
                java.lang.NullPointerException: Cannot invoke "com.example.order.model.Address.getPostalCode()" because "deliveryAddress" is null
                    at com.example.order.service.ShippingCalculatorService.calculateFee(ShippingCalculatorService.java:52)
                    at com.example.order.service.OrderServiceImpl.createOrder(OrderServiceImpl.java:94)
                    at com.example.order.controller.OrderController.submitOrder(OrderController.java:41)
                """
        ));

        cases.add(new BenchmarkCase(
                "BM-08",
                "PostgreSQL Unique Constraint Violation on Customer Email",
                IncidentCategory.DATABASE,
                IncidentSeverity.MEDIUM,
                "duplicate, unique, constraint, key, violation",
                """
                2026-10-30 12:45:00.123 ERROR [http-nio-8080-exec-5] org.hibernate.engine.jdbc.spi.SqlExceptionHelper : ERROR: duplicate key value violates unique constraint "users_email_key"
                  Detail: Key (email)=(sarah.connor@example.com) already exists.
                org.springframework.dao.DataIntegrityViolationException: could not execute statement [ERROR: duplicate key value violates unique constraint "users_email_key"]
                    at org.hibernate.exception.internal.SQLStateConverter.convert(SQLStateConverter.java:118)
                    at com.example.user.service.UserService.registerNewUser(UserService.java:73)
                Caused by: org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint "users_email_key"
                    at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2675)
                """
        ));

        return cases;
    }

    public EvaluationSummaryResponse runEvaluationSuite() {
        List<BenchmarkCase> cases = getBenchmarkCases();
        List<EvaluationMetricDto> results = new ArrayList<>();

        int total = cases.size();
        int categoryMatches = 0;
        int severityMatches = 0;
        int rootCauseMatches = 0;
        long totalLatency = 0;
        double totalConfidence = 0;
        String primaryProvider = "DETERMINISTIC_FALLBACK";

        log.info("Executing Incident Triage Benchmark Suite on {} test samples...", total);

        for (BenchmarkCase bCase : cases) {
            long startTime = System.currentTimeMillis();

            ParsedLogInfo parsed = logParsingService.parse(bCase.getRawLog());
            RuleClassificationResult ruleResult = ruleClassificationService.classify(bCase.getRawLog(), parsed);
            AiAnalysisResult aiResult = geminiAiService.analyze(bCase.getRawLog(), parsed, ruleResult);

            long latency = System.currentTimeMillis() - startTime;
            totalLatency += latency;

            EvaluationMetricDto metric = new EvaluationMetricDto();
            metric.setSampleId(bCase.getId());
            metric.setSampleName(bCase.getName());
            metric.setExpectedCategory(bCase.getExpectedCategory());
            metric.setPredictedCategory(ruleResult.getCategory());
            boolean catMatch = (ruleResult.getCategory() == bCase.getExpectedCategory());
            metric.setCategoryMatch(catMatch);
            if (catMatch) categoryMatches++;

            metric.setExpectedSeverity(bCase.getExpectedSeverity());
            metric.setPredictedSeverity(ruleResult.getSeverity());
            boolean sevMatch = (ruleResult.getSeverity() == bCase.getExpectedSeverity());
            metric.setSeverityMatch(sevMatch);
            if (sevMatch) severityMatches++;

            metric.setExpectedRootCauseKeywords(bCase.getExpectedRootCauseKeywords());
            boolean rcMatch = checkKeywords(aiResult.getProbableRootCause(), bCase.getExpectedRootCauseKeywords());
            metric.setRootCauseMatch(rcMatch);
            if (rcMatch) rootCauseMatches++;

            metric.setConfidenceScore(aiResult.getConfidenceScore());
            totalConfidence += aiResult.getConfidenceScore();
            metric.setExecutionTimeMs(latency);
            metric.setProvider(aiResult.getProvider());
            metric.setRecommendedFixes(aiResult.getRecommendedFixes());

            primaryProvider = aiResult.getProvider();
            results.add(metric);
        }

        EvaluationSummaryResponse summary = new EvaluationSummaryResponse();
        summary.setTotalSamples(total);
        summary.setCategoryMatches(categoryMatches);
        summary.setCategoryAccuracyPercent(Math.round(((double) categoryMatches / total) * 1000.0) / 10.0);
        summary.setSeverityMatches(severityMatches);
        summary.setSeverityAccuracyPercent(Math.round(((double) severityMatches / total) * 1000.0) / 10.0);
        summary.setRootCauseMatches(rootCauseMatches);
        summary.setRootCauseAccuracyPercent(Math.round(((double) rootCauseMatches / total) * 1000.0) / 10.0);
        summary.setAverageLatencyMs(Math.round(((double) totalLatency / total) * 10.0) / 10.0);
        summary.setAverageConfidenceScore(Math.round((totalConfidence / total) * 100.0) / 100.0);
        summary.setPrimaryProvider(primaryProvider);
        summary.setSampleResults(results);

        log.info("Evaluation Complete: Category Accuracy: {}%, Severity Accuracy: {}%, Avg Latency: {}ms",
                summary.getCategoryAccuracyPercent(), summary.getSeverityAccuracyPercent(), summary.getAverageLatencyMs());

        return summary;
    }

    private boolean checkKeywords(String text, String keywords) {
        if (text == null || keywords == null) return false;
        String lowerText = text.toLowerCase();
        String[] parts = keywords.split(",");
        for (String p : parts) {
            if (lowerText.contains(p.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
