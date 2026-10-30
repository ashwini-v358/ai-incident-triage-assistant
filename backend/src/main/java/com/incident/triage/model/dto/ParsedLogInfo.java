package com.incident.triage.model.dto;

import java.util.ArrayList;
import java.util.List;

public class ParsedLogInfo {

    private String timestamp;
    private String logLevel;
    private String threadName;
    private String loggerName;
    private String mainErrorMessage;
    private String rootCauseException;
    private String primaryExceptionType;
    private String culpritClass;
    private String culpritMethod;
    private Integer culpritLineNumber;
    private String stackTrace;
    private List<String> nestedCauses = new ArrayList<>();
    private List<String> contextLines = new ArrayList<>();

    public ParsedLogInfo() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getMainErrorMessage() {
        return mainErrorMessage;
    }

    public void setMainErrorMessage(String mainErrorMessage) {
        this.mainErrorMessage = mainErrorMessage;
    }

    public String getRootCauseException() {
        return rootCauseException;
    }

    public void setRootCauseException(String rootCauseException) {
        this.rootCauseException = rootCauseException;
    }

    public String getPrimaryExceptionType() {
        return primaryExceptionType;
    }

    public void setPrimaryExceptionType(String primaryExceptionType) {
        this.primaryExceptionType = primaryExceptionType;
    }

    public String getCulpritClass() {
        return culpritClass;
    }

    public void setCulpritClass(String culpritClass) {
        this.culpritClass = culpritClass;
    }

    public String getCulpritMethod() {
        return culpritMethod;
    }

    public void setCulpritMethod(String culpritMethod) {
        this.culpritMethod = culpritMethod;
    }

    public Integer getCulpritLineNumber() {
        return culpritLineNumber;
    }

    public void setCulpritLineNumber(Integer culpritLineNumber) {
        this.culpritLineNumber = culpritLineNumber;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public List<String> getNestedCauses() {
        return nestedCauses;
    }

    public void setNestedCauses(List<String> nestedCauses) {
        this.nestedCauses = nestedCauses;
    }

    public List<String> getContextLines() {
        return contextLines;
    }

    public void setContextLines(List<String> contextLines) {
        this.contextLines = contextLines;
    }
}

