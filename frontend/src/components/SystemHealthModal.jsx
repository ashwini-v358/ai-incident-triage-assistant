import React from 'react';

export default function SystemHealthModal({ healthData, onClose, onRefresh }) {
  if (!healthData) return null;

  const db = healthData.database || {};
  const ai = healthData.aiProvider || {};
  const jvm = healthData.jvm || {};

  return (
    <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.8)' }}>
      <div className="modal-dialog modal-lg">
        <div className="modal-content bg-dark text-light border border-secondary shadow">
          <div className="modal-header border-secondary-subtle">
            <h5 className="modal-title d-flex align-items-center gap-2">
              <i className="bi bi-activity text-info"></i>
              <span>System & Infrastructure Diagnostics</span>
            </h5>
            <button type="button" className="btn-close btn-close-white" onClick={onClose}></button>
          </div>

          <div className="modal-body">
            {/* Overall status */}
            <div className="alert alert-dark border-secondary d-flex justify-content-between align-items-center mb-4">
              <div>
                <span className="text-secondary small d-block">Overall System Status:</span>
                <span className="fs-5 fw-bold text-success d-flex align-items-center gap-2 mt-1">
                  <span className="spinner-grow spinner-grow-sm text-success" role="status"></span>
                  SYSTEM HEALTHY ({healthData.status})
                </span>
              </div>
              <button className="btn btn-sm btn-outline-info" onClick={onRefresh}>
                <i className="bi bi-arrow-clockwise me-1"></i> Refresh
              </button>
            </div>

            <div className="row g-3">
              {/* PostgreSQL Database */}
              <div className="col-md-6">
                <div className="card p-3 h-100">
                  <h6 className="text-secondary small text-uppercase fw-semibold mb-3 d-flex align-items-center gap-2">
                    <i className="bi bi-database text-primary"></i> PostgreSQL Database
                  </h6>
                  <ul className="list-unstyled small mb-0">
                    <li className="mb-2 d-flex justify-content-between">
                      <span className="text-secondary">Connection State:</span>
                      <span className={`badge ${db.status === 'UP' ? 'bg-success' : 'bg-danger'}`}>
                        {db.status === 'UP' ? 'CONNECTED (UP)' : 'DISCONNECTED'}
                      </span>
                    </li>
                    <li className="mb-2 d-flex justify-content-between">
                      <span className="text-secondary">Database:</span>
                      <span className="font-monospace text-white">incident_triage</span>
                    </li>
                    <li className="mb-2 d-flex justify-content-between">
                      <span className="text-secondary">Host / Port:</span>
                      <span className="font-monospace text-white">localhost:5432</span>
                    </li>
                    <li className="mb-2 d-flex justify-content-between">
                      <span className="text-secondary">Engine Product:</span>
                      <span className="text-light">{db.databaseProduct || 'PostgreSQL'}</span>
                    </li>
                    <li className="d-flex justify-content-between">
                      <span className="text-secondary">Version:</span>
                      <span className="font-monospace text-muted">{db.databaseVersion || '18.x'}</span>
                    </li>
                  </ul>
                </div>
              </div>

              {/* AI Engine & Fallback */}
              <div className="col-md-6">
                <div className="card p-3 h-100">
                  <h6 className="text-secondary small text-uppercase fw-semibold mb-3 d-flex align-items-center gap-2">
                    <i className="bi bi-robot text-warning"></i> AI & Fallback Engine
                  </h6>
                  <ul className="list-unstyled small mb-0">
                    <li className="mb-2 d-flex justify-content-between">
                      <span className="text-secondary">API Key Configured:</span>
                      <span className={`badge ${ai.hasApiKey ? 'bg-success' : 'bg-warning text-dark'}`}>
                        {ai.hasApiKey ? 'YES' : 'NOT DETECTED'}
                      </span>
                    </li>
                    <li className="mb-2 d-flex justify-content-between">
                      <span className="text-secondary">Gemini Target Model:</span>
                      <span className="font-monospace text-info">{ai.model || 'gemini-3.5-flash-lite'}</span>
                    </li>
                    <li className="mb-2 d-flex justify-content-between">
                      <span className="text-secondary">Deterministic Fallback:</span>
                      <span className="badge bg-success">ACTIVE & ENABLED</span>
                    </li>
                    <li className="d-flex justify-content-between">
                      <span className="text-secondary">Operational Mode:</span>
                      <span className="badge bg-secondary">{ai.activeMode}</span>
                    </li>
                  </ul>
                </div>
              </div>

              {/* JVM & Host Runtime */}
              <div className="col-12">
                <div className="card p-3">
                  <h6 className="text-secondary small text-uppercase fw-semibold mb-3 d-flex align-items-center gap-2">
                    <i className="bi bi-cpu text-info"></i> Java Virtual Machine (JVM 21)
                  </h6>
                  <div className="row g-2 small">
                    <div className="col-sm-3">
                      <span className="text-secondary d-block">Uptime:</span>
                      <strong className="text-white">{Math.round((jvm.uptimeMs || 0) / 1000)} seconds</strong>
                    </div>
                    <div className="col-sm-3">
                      <span className="text-secondary d-block">Total Memory:</span>
                      <strong className="text-white">{jvm.totalMemoryMb || 0} MB</strong>
                    </div>
                    <div className="col-sm-3">
                      <span className="text-secondary d-block">Free Memory:</span>
                      <strong className="text-success">{jvm.freeMemoryMb || 0} MB</strong>
                    </div>
                    <div className="col-sm-3">
                      <span className="text-secondary d-block">CPU Cores:</span>
                      <strong className="text-white">{jvm.availableProcessors || 0} Cores</strong>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="modal-footer border-secondary-subtle">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
