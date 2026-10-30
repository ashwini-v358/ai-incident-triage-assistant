import React, { useState } from 'react';
import { incidentApi } from '../api/client';

export default function IncidentDetailModal({ incident, onClose, onStatusUpdated }) {
  if (!incident) return null;

  const [newStatus, setNewStatus] = useState(incident.status);
  const [resolutionNotes, setResolutionNotes] = useState(incident.resolutionNotes || '');
  const [updating, setUpdating] = useState(false);
  const [copied, setCopied] = useState(false);
  const [showRawLog, setShowRawLog] = useState(false);

  const getSeverityBadge = (sev) => {
    switch (sev) {
      case 'CRITICAL': return 'badge-critical';
      case 'HIGH': return 'badge-high';
      case 'MEDIUM': return 'badge-medium';
      case 'LOW': return 'badge-low';
      default: return 'bg-secondary';
    }
  };

  const getStatusBadge = (st) => {
    switch (st) {
      case 'OPEN': return 'badge-open';
      case 'INVESTIGATING': return 'badge-investigating';
      case 'MITIGATED': return 'badge-mitigated';
      case 'RESOLVED': return 'badge-resolved';
      default: return 'bg-secondary';
    }
  };

  const handleCopyStackTrace = () => {
    navigator.clipboard.writeText(incident.stackTrace || incident.rawLog);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleUpdateStatus = async () => {
    setUpdating(true);
    try {
      const updated = await incidentApi.updateIncidentStatus(incident.id, {
        status: newStatus,
        resolutionNotes: resolutionNotes.trim(),
      });
      onStatusUpdated(updated);
    } catch (err) {
      alert('Failed to update status: ' + (err.response?.data?.message || err.message));
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.8)' }}>
      <div className="modal-dialog modal-xl modal-dialog-scrollable">
        <div className="modal-content bg-dark text-light border border-secondary shadow-lg">
          {/* Header */}
          <div className="modal-header border-secondary-subtle">
            <div className="d-flex align-items-center gap-2 flex-wrap">
              <span className="fs-5 fw-bold text-white">Incident #{incident.id}: {incident.title}</span>
              <span className={`badge ${getSeverityBadge(incident.severity)}`}>{incident.severityLabel || incident.severity}</span>
              <span className={`badge ${getStatusBadge(incident.status)}`}>{incident.statusLabel || incident.status}</span>
              <span className="badge bg-secondary">{incident.serviceName}</span>
              <span className="badge bg-dark border border-secondary">{incident.environment}</span>
            </div>
            <button type="button" className="btn-close btn-close-white" onClick={onClose}></button>
          </div>

          {/* Body */}
          <div className="modal-body p-4">
            {/* Probable Root Cause Section */}
            <div className="card mb-4 border-info border-opacity-50" style={{ backgroundColor: '#0d1d30' }}>
              <div className="card-header bg-transparent d-flex justify-content-between align-items-center">
                <span className="fw-bold text-info d-flex align-items-center gap-2">
                  <i className="bi bi-lightbulb-fill"></i> Probable Root-Cause Analysis
                </span>
                <div className="d-flex align-items-center gap-2">
                  <span className="badge bg-primary bg-opacity-25 text-info border border-info border-opacity-25 small">
                    Provider: {incident.analysisProvider || 'AI Root-Cause'}
                  </span>
                  {incident.confidenceScore && (
                    <span className="badge bg-success bg-opacity-25 text-success border border-success border-opacity-25 small">
                      {Math.round(incident.confidenceScore * 100)}% Confidence
                    </span>
                  )}
                </div>
              </div>
              <div className="card-body">
                <p className="fs-6 mb-0 text-white leading-relaxed">
                  {incident.probableRootCause}
                </p>
              </div>
            </div>

            {/* Culprit Location & Exception Details */}
            <div className="row g-3 mb-4">
              <div className="col-md-6">
                <div className="card h-100 p-3">
                  <h6 className="text-secondary small text-uppercase fw-semibold mb-2">Culprit Stack Frame</h6>
                  {incident.culpritClass ? (
                    <div>
                      <div className="font-monospace text-warning small text-break">
                        <strong>{incident.culpritClass}</strong>
                      </div>
                      <div className="small text-secondary mt-1">
                        Method: <span className="font-monospace text-info">{incident.culpritMethod || 'N/A'}</span>
                        {incident.culpritLineNumber && (
                          <span className="ms-2">Line: <span className="font-monospace text-light">{incident.culpritLineNumber}</span></span>
                        )}
                      </div>
                    </div>
                  ) : (
                    <div className="text-muted small">No specific class frame identified in application namespace.</div>
                  )}
                </div>
              </div>

              <div className="col-md-6">
                <div className="card h-100 p-3">
                  <h6 className="text-secondary small text-uppercase fw-semibold mb-2">Exception Breakdown</h6>
                  <div className="small">
                    <div>
                      <span className="text-secondary">Primary Type:</span>{' '}
                      <span className="font-monospace text-danger">{incident.exceptionType || 'N/A'}</span>
                    </div>
                    <div className="mt-1">
                      <span className="text-secondary">Root Exception:</span>{' '}
                      <span className="font-monospace text-light small text-break">{incident.rootCauseException || 'N/A'}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Supporting Evidence */}
            {incident.evidence && (
              <div className="card mb-4 p-3">
                <h6 className="text-secondary small text-uppercase fw-semibold mb-2 d-flex align-items-center gap-1">
                  <i className="bi bi-search"></i> Diagnostic Signals & Evidence
                </h6>
                <div className="font-monospace small text-secondary-emphasis" style={{ whiteSpace: 'pre-wrap' }}>
                  {incident.evidence}
                </div>
              </div>
            )}

            {/* Recommended Fixes */}
            {incident.recommendedFixes && incident.recommendedFixes.length > 0 && (
              <div className="card mb-4 p-3 border-success border-opacity-25">
                <h6 className="text-success small text-uppercase fw-semibold mb-3 d-flex align-items-center gap-1">
                  <i className="bi bi-wrench-adjustable"></i> Actionable Recommended Fixes
                </h6>
                <ul className="list-group list-group-flush bg-transparent">
                  {incident.recommendedFixes.map((fix, idx) => (
                    <li key={idx} className="list-group-item bg-transparent text-light border-secondary small d-flex gap-2">
                      <i className="bi bi-check-circle-fill text-success flex-shrink-0 mt-1"></i>
                      <span>{fix}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Prevention Strategies */}
            {incident.preventionStrategies && incident.preventionStrategies.length > 0 && (
              <div className="card mb-4 p-3 border-primary border-opacity-25">
                <h6 className="text-info small text-uppercase fw-semibold mb-3 d-flex align-items-center gap-1">
                  <i className="bi bi-shield-check"></i> Long-Term Resilience & Prevention
                </h6>
                <ul className="list-group list-group-flush bg-transparent">
                  {incident.preventionStrategies.map((prev, idx) => (
                    <li key={idx} className="list-group-item bg-transparent text-light border-secondary small d-flex gap-2">
                      <i className="bi bi-arrow-right-circle text-info flex-shrink-0 mt-1"></i>
                      <span>{prev}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Stack Trace Collapsible */}
            <div className="card mb-4">
              <div className="card-header d-flex justify-content-between align-items-center">
                <button
                  type="button"
                  className="btn btn-sm btn-link text-decoration-none text-light p-0 d-flex align-items-center gap-2"
                  onClick={() => setShowRawLog(!showRawLog)}
                >
                  <i className={`bi bi-chevron-${showRawLog ? 'down' : 'right'}`}></i>
                  <span>{showRawLog ? 'Hide Stack Trace & Raw Logs' : 'View Stack Trace & Raw Logs'}</span>
                </button>
                <button
                  type="button"
                  className="btn btn-sm btn-outline-secondary d-flex align-items-center gap-1"
                  onClick={handleCopyStackTrace}
                >
                  <i className={`bi bi-${copied ? 'check2' : 'clipboard'}`}></i>
                  <span>{copied ? 'Copied!' : 'Copy Trace'}</span>
                </button>
              </div>
              {showRawLog && (
                <div className="card-body p-0">
                  <div className="stack-trace-box m-2">
                    {incident.stackTrace || incident.rawLog}
                  </div>
                </div>
              )}
            </div>

            {/* Workflow Status Update Box */}
            <div className="card p-3 border-secondary">
              <h6 className="text-secondary small text-uppercase fw-semibold mb-3">Update Incident Workflow Status</h6>
              <div className="row g-2 align-items-center">
                <div className="col-md-3">
                  <select
                    className="form-select form-select-sm bg-dark text-light border-secondary"
                    value={newStatus}
                    onChange={(e) => setNewStatus(e.target.value)}
                  >
                    <option value="OPEN">OPEN</option>
                    <option value="INVESTIGATING">INVESTIGATING</option>
                    <option value="MITIGATED">MITIGATED</option>
                    <option value="RESOLVED">RESOLVED</option>
                  </select>
                </div>
                <div className="col-md-7">
                  <input
                    type="text"
                    className="form-control form-control-sm bg-dark text-light border-secondary"
                    placeholder="Add resolution notes or root-cause patch reference..."
                    value={resolutionNotes}
                    onChange={(e) => setResolutionNotes(e.target.value)}
                  />
                </div>
                <div className="col-md-2">
                  <button
                    type="button"
                    className="btn btn-sm btn-success w-100"
                    onClick={handleUpdateStatus}
                    disabled={updating}
                  >
                    {updating ? 'Saving...' : 'Update Status'}
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="modal-footer border-secondary-subtle">
            <span className="small text-secondary me-auto">
              Triaged at {new Date(incident.createdAt).toLocaleString()} · Stored in PostgreSQL
            </span>
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

