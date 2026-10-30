import React, { useState, useEffect } from 'react';
import { incidentApi } from '../api/client';

export default function LogTriageForm({ onIncidentAnalyzed, benchmarkSamples = [] }) {
  const [mode, setMode] = useState('paste'); // 'paste' or 'upload'
  const [rawLog, setRawLog] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [serviceName, setServiceName] = useState('payment-gateway');
  const [environment, setEnvironment] = useState('PRODUCTION');
  const [title, setTitle] = useState('');
  const [selectedSampleId, setSelectedSampleId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Load sample content if user chooses a benchmark sample
  const handleSampleChange = (e) => {
    const sampleId = e.target.value;
    setSelectedSampleId(sampleId);
    if (!sampleId) return;

    const found = benchmarkSamples.find((s) => s.id === sampleId);
    if (found) {
      setMode('paste');
      setRawLog(found.rawLog);
      setTitle(found.name);
      if (found.expectedCategory === 'DATABASE') setServiceName('order-database-service');
      else if (found.expectedCategory === 'MEMORY_RESOURCE') setServiceName('analytics-exporter');
      else if (found.expectedCategory === 'AUTHENTICATION') setServiceName('auth-jwt-service');
      else if (found.expectedCategory === 'NETWORK') setServiceName('payment-client');
      else setServiceName('app-core-service');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (mode === 'paste' && (!rawLog || !rawLog.trim())) {
      setError('Please paste an application log or stack trace.');
      return;
    }

    if (mode === 'upload' && !selectedFile) {
      setError('Please select a log file (.log, .txt, .json) to upload.');
      return;
    }

    setLoading(true);
    try {
      let result;
      if (mode === 'paste') {
        result = await incidentApi.analyzeLog({
          rawLog: rawLog.trim(),
          serviceName: serviceName.trim(),
          environment,
          title: title.trim() || undefined,
        });
      } else {
        const formData = new FormData();
        formData.append('file', selectedFile);
        if (serviceName) formData.append('serviceName', serviceName.trim());
        if (environment) formData.append('environment', environment);
        if (title) formData.append('title', title.trim());

        result = await incidentApi.uploadLogFile(formData);
      }

      onIncidentAnalyzed(result);
      // Reset form
      setRawLog('');
      setSelectedFile(null);
      setTitle('');
      setSelectedSampleId('');
    } catch (err) {
      console.error('Triage error:', err);
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to analyze incident log';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card mb-4">
      <div className="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div className="d-flex align-items-center gap-2">
          <i className="bi bi-robot text-primary fs-5"></i>
          <span className="fs-6">AI Incident Log Ingestion & Root-Cause Triage</span>
        </div>

        {/* Sample selector dropdown */}
        {benchmarkSamples.length > 0 && (
          <div className="d-flex align-items-center gap-2">
            <span className="small text-secondary">Quick Test Sample:</span>
            <select
              className="form-select form-select-sm bg-dark text-light border-secondary"
              style={{ maxWidth: '280px' }}
              value={selectedSampleId}
              onChange={handleSampleChange}
            >
              <option value="">-- Choose Benchmark Log --</option>
              {benchmarkSamples.map((s) => (
                <option key={s.id} value={s.id}>
                  [{s.id}] {s.name}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      <div className="card-body">
        {error && (
          <div className="alert alert-danger d-flex align-items-center justify-content-between py-2 mb-3">
            <div>
              <i className="bi bi-exclamation-octagon me-2"></i>
              <span>{error}</span>
            </div>
            <button className="btn-close btn-close-white" onClick={() => setError(null)}></button>
          </div>
        )}

        {/* Ingestion Mode Switcher */}
        <div className="d-flex gap-2 mb-3">
          <button
            type="button"
            className={`btn btn-sm ${mode === 'paste' ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => setMode('paste')}
          >
            <i className="bi bi-clipboard-data me-1"></i> Paste Raw Log
          </button>
          <button
            type="button"
            className={`btn btn-sm ${mode === 'upload' ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => setMode('upload')}
          >
            <i className="bi bi-cloud-arrow-up me-1"></i> Upload Log File
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {/* Metadata row */}
          <div className="row g-2 mb-3">
            <div className="col-md-4">
              <label className="form-label small text-secondary">Service / Application Name</label>
              <input
                type="text"
                className="form-control form-control-sm bg-dark text-light border-secondary"
                placeholder="e.g. payment-service"
                value={serviceName}
                onChange={(e) => setServiceName(e.target.value)}
              />
            </div>
            <div className="col-md-3">
              <label className="form-label small text-secondary">Target Environment</label>
              <select
                className="form-select form-select-sm bg-dark text-light border-secondary"
                value={environment}
                onChange={(e) => setEnvironment(e.target.value)}
              >
                <option value="PRODUCTION">Production</option>
                <option value="STAGING">Staging</option>
                <option value="DEVELOPMENT">Development</option>
                <option value="QA">QA / Test</option>
              </select>
            </div>
            <div className="col-md-5">
              <label className="form-label small text-secondary">Incident Title (Optional)</label>
              <input
                type="text"
                className="form-control form-control-sm bg-dark text-light border-secondary"
                placeholder="Auto-detected from exception if left blank"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </div>
          </div>

          {/* Paste Mode */}
          {mode === 'paste' && (
            <div className="mb-3">
              <label className="form-label small text-secondary d-flex justify-content-between">
                <span>Application Log & Java Stack Trace</span>
                <span className="text-muted">{rawLog.length} characters</span>
              </label>
              <textarea
                className="form-control bg-dark text-light border-secondary font-monospace small"
                rows="7"
                placeholder="Paste Java stack trace, Spring Boot logs, PostgreSQL error messages, or fatal exceptions here..."
                value={rawLog}
                onChange={(e) => setRawLog(e.target.value)}
              ></textarea>
            </div>
          )}

          {/* Upload Mode */}
          {mode === 'upload' && (
            <div className="mb-3 p-4 border border-secondary border-dashed rounded text-center bg-dark">
              <i className="bi bi-file-earmark-arrow-up fs-1 text-primary mb-2 d-block"></i>
              <p className="text-secondary small mb-2">Select or drag & drop application log file (.log, .txt, .json)</p>
              <input
                type="file"
                className="form-control bg-dark text-light border-secondary w-auto mx-auto"
                accept=".log,.txt,.json"
                onChange={(e) => setSelectedFile(e.target.files[0] || null)}
              />
              {selectedFile && (
                <div className="mt-2 text-success small">
                  <i className="bi bi-check2 me-1"></i> Selected: <strong>{selectedFile.name}</strong> ({(selectedFile.size / 1024).toFixed(1)} KB)
                </div>
              )}
            </div>
          )}

          <div className="d-flex justify-content-end align-items-center gap-3">
            {loading && (
              <span className="text-info small d-flex align-items-center gap-2">
                <span className="spinner-border spinner-border-sm" role="status"></span>
                Parsing log, classifying rules & calling Gemini AI root-cause engine...
              </span>
            )}
            <button
              type="submit"
              className="btn btn-primary d-flex align-items-center gap-2 px-4"
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm" role="status"></span>
                  <span>Analyzing...</span>
                </>
              ) : (
                <>
                  <i className="bi bi-shield-check"></i>
                  <span>Triage & Analyze Incident</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

