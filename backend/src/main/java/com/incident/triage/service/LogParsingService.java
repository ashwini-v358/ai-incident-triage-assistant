package com.incident.triage.service;

import com.incident.triage.model.dto.ParsedLogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogParsingService {

    private static final Logger log = LoggerFactory.getLogger(LogParsingService.class);

    // Regex for standard timestamps (ISO-8601, standard log formats)
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile(
            "\\b(\\d{4}[-/]\\d{2}[-/]\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,6})?(?:[+-]\\d{2}:?\\d{2}|Z)?)\\b");

    // Regex for log levels
    private static final Pattern LOG_LEVEL_PATTERN = Pattern.compile(
            "\\b(FATAL|ERROR|SEVERE|CRITICAL|WARN|WARNING|INFO|DEBUG|TRACE)\\b", Pattern.CASE_INSENSITIVE);

    // Regex for thread name in logs: [main] or [http-nio-8080-exec-5]
    private static final Pattern THREAD_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");

    // Regex for Java Exception declaration: com.example.MyException: details or just java.lang.NullPointerException
    private static final Pattern EXCEPTION_HEADER_PATTERN = Pattern.compile(
            "([a-zA-Z0-9_$.]+(?:Exception|Error|Failure|Throwable|Fault))(?::\\s*(.*))?");

    // Regex for "Caused by: com.example.MyException: message"
    private static final Pattern CAUSED_BY_PATTERN = Pattern.compile(
            "Caused by:\\s+([a-zA-Z0-9_$.]+(?:Exception|Error|Failure|Throwable|Fault))(?::\\s*(.*))?");

    // Regex for stack trace element: "at com.example.MyClass.myMethod(MyClass.java:123)"
    private static final Pattern STACK_FRAME_PATTERN = Pattern.compile(
            "^\\s*at\\s+([a-zA-Z0-9_$.]+)\\.([a-zA-Z0-9_$<>]+)\\(([^:]+?)(?::(\\d+))?\\)");

    public ParsedLogInfo parse(String rawLog) {
        ParsedLogInfo info = new ParsedLogInfo();
        if (rawLog == null || rawLog.trim().isEmpty()) {
            info.setMainErrorMessage("Empty log provided");
            return info;
        }

        String[] lines = rawLog.split("\\r?\\n");
        List<String> stackTraceLines = new ArrayList<>();
        List<String> contextLines = new ArrayList<>();
        List<String> causedByList = new ArrayList<>();

        boolean insideStackTrace = false;
        boolean hasFoundAppClass = false;
        String firstExceptionFound = null;
        String firstExceptionMessage = null;
        String deepestCauseException = null;
        String deepestCauseMessage = null;

        String culpritClass = null;
        String culpritMethod = null;
        Integer culpritLine = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                continue;
            }

            // Extract metadata from the first few non-stack lines if not yet extracted
            if (info.getTimestamp() == null) {
                Matcher tsMatcher = TIMESTAMP_PATTERN.matcher(line);
                if (tsMatcher.find()) {
                    info.setTimestamp(tsMatcher.group(1));
                }
            }

            if (info.getLogLevel() == null) {
                Matcher levelMatcher = LOG_LEVEL_PATTERN.matcher(line);
                if (levelMatcher.find()) {
                    info.setLogLevel(levelMatcher.group(1).toUpperCase());
                }
            }

            if (info.getThreadName() == null) {
                Matcher threadMatcher = THREAD_PATTERN.matcher(line);
                if (threadMatcher.find()) {
                    info.setThreadName(threadMatcher.group(1));
                }
            }

            // Check for "Caused by:"
            Matcher causedByMatcher = CAUSED_BY_PATTERN.matcher(line);
            if (causedByMatcher.find()) {
                insideStackTrace = true;
                stackTraceLines.add(line);
                String causeType = causedByMatcher.group(1);
                String causeMsg = causedByMatcher.group(2);
                deepestCauseException = causeType;
                deepestCauseMessage = causeMsg != null ? causeMsg.trim() : "";
                causedByList.add(causeType + (causeMsg != null && !causeMsg.isEmpty() ? ": " + causeMsg : ""));
                continue;
            }

            // Check for "at com.example..."
            Matcher frameMatcher = STACK_FRAME_PATTERN.matcher(line);
            if (frameMatcher.find()) {
                insideStackTrace = true;
                stackTraceLines.add(line);

                String frameClass = frameMatcher.group(1);
                String frameMethod = frameMatcher.group(2);
                String lineStr = frameMatcher.group(4);

                boolean isApp = isApplicationClass(frameClass);
                if (culpritClass == null || (!hasFoundAppClass && isApp)) {
                    culpritClass = frameClass;
                    culpritMethod = frameMethod;
                    if (lineStr != null) {
                        try {
                            culpritLine = Integer.parseInt(lineStr);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    if (isApp) {
                        hasFoundAppClass = true;
                    }
                }
                continue;
            }

            // Check for Primary Exception header line
            Matcher exMatcher = EXCEPTION_HEADER_PATTERN.matcher(trimmed);
            if (exMatcher.find()) {
                insideStackTrace = true;
                stackTraceLines.add(line);
                if (firstExceptionFound == null) {
                    firstExceptionFound = exMatcher.group(1);
                    firstExceptionMessage = exMatcher.group(2);
                }
                continue;
            }

            // If line is neither a stack frame nor an exception header
            if (insideStackTrace) {
                // If line starts with "... 35 more" or tabbed/nested indicator
                if (trimmed.startsWith("...") || trimmed.contains("more")) {
                    stackTraceLines.add(line);
                } else {
                    insideStackTrace = false;
                    contextLines.add(line);
                }
            } else {
                contextLines.add(line);
            }
        }

        // Set primary and root cause
        info.setPrimaryExceptionType(firstExceptionFound);
        if (deepestCauseException != null) {
            info.setRootCauseException(deepestCauseException + (deepestCauseMessage != null && !deepestCauseMessage.isEmpty() ? ": " + deepestCauseMessage : ""));
        } else if (firstExceptionFound != null) {
            info.setRootCauseException(firstExceptionFound + (firstExceptionMessage != null && !firstExceptionMessage.isEmpty() ? ": " + firstExceptionMessage : ""));
        } else {
            // Fallback for non-standard or text error logs
            extractHeuristicError(rawLog, info);
        }

        // Main message
        if (deepestCauseMessage != null && !deepestCauseMessage.isEmpty()) {
            info.setMainErrorMessage(deepestCauseMessage);
        } else if (firstExceptionMessage != null && !firstExceptionMessage.isEmpty()) {
            info.setMainErrorMessage(firstExceptionMessage);
        } else if (!contextLines.isEmpty()) {
            info.setMainErrorMessage(contextLines.get(0));
        } else {
            info.setMainErrorMessage(info.getRootCauseException() != null ? info.getRootCauseException() : "Error identified in log trace");
        }

        info.setCulpritClass(culpritClass);
        info.setCulpritMethod(culpritMethod);
        info.setCulpritLineNumber(culpritLine);
        info.setNestedCauses(causedByList);
        info.setContextLines(contextLines);

        if (!stackTraceLines.isEmpty()) {
            info.setStackTrace(String.join("\n", stackTraceLines));
        }

        log.debug("Parsed log: Primary={}, RootCause={}, Culprit={}.{}:{}",
                info.getPrimaryExceptionType(), info.getRootCauseException(),
                info.getCulpritClass(), info.getCulpritMethod(), info.getCulpritLineNumber());

        return info;
    }

    private boolean isApplicationClass(String className) {
        if (className == null) return false;
        // Skip common JVM, framework, and database driver internals to pinpoint actual user code
        return !className.startsWith("java.") &&
                !className.startsWith("javax.") &&
                !className.startsWith("jakarta.") &&
                !className.startsWith("jdk.") &&
                !className.startsWith("sun.") &&
                !className.startsWith("org.apache.") &&
                !className.startsWith("org.springframework.") &&
                !className.startsWith("org.hibernate.") &&
                !className.startsWith("org.postgresql.") &&
                !className.startsWith("com.zaxxer.hikari.") &&
                !className.startsWith("io.jsonwebtoken.");
    }

    private void extractHeuristicError(String rawLog, ParsedLogInfo info) {
        String lower = rawLog.toLowerCase();
        if (lower.contains("out of memory") || lower.contains("outofmemoryerror")) {
            info.setPrimaryExceptionType("java.lang.OutOfMemoryError");
            info.setRootCauseException("java.lang.OutOfMemoryError: Java heap space or Metaspace exhaustion");
        } else if (lower.contains("connection refused") || lower.contains("connection reset")) {
            info.setPrimaryExceptionType("java.net.ConnectException");
            info.setRootCauseException("java.net.ConnectException: Connection refused by target service");
        } else if (lower.contains("timed out") || lower.contains("sockettimeoutexception")) {
            info.setPrimaryExceptionType("java.net.SocketTimeoutException");
            info.setRootCauseException("java.net.SocketTimeoutException: Read timed out");
        } else if (lower.contains("nullpointerexception") || lower.contains("null pointer")) {
            info.setPrimaryExceptionType("java.lang.NullPointerException");
            info.setRootCauseException("java.lang.NullPointerException: Dereferencing null pointer");
        } else if (lower.contains("deadlock")) {
            info.setPrimaryExceptionType("org.springframework.dao.CannotAcquireLockException");
            info.setRootCauseException("Deadlock detected during concurrent transaction lock acquisition");
        } else {
            info.setPrimaryExceptionType("UnknownException");
            info.setRootCauseException("Incident log trace captured without explicit Java exception name");
        }
    }
}
