import React from 'react';

export default function Navbar({ activeTab, setActiveTab, onOpenHealth, healthData }) {
  const isDbUp = healthData?.database?.status === 'UP';
  const hasAiKey = healthData?.aiProvider?.hasApiKey;

  return (
    <nav className="navbar navbar-expand-lg navbar-dark border-bottom border-secondary-subtle px-3 py-2 sticky-top" style={{ backgroundColor: '#161b22' }}>
      <div className="container-fluid">
        <a className="navbar-brand d-flex align-items-center gap-2 fw-bold" href="#">
          <span className="fs-4">🛡️</span>
          <span>Incident Triage & Root-Cause Assistant</span>
          <span className="badge bg-primary text-uppercase" style={{ fontSize: '0.65rem' }}>AI Powered</span>
        </a>

        <div className="d-flex align-items-center gap-3">
          {/* Health Pills */}
          <button
            type="button"
            className="btn btn-sm btn-outline-secondary d-flex align-items-center gap-2"
            onClick={onOpenHealth}
            title="Click to view full system health diagnostics"
          >
            <span
              className={`rounded-circle d-inline-block ${isDbUp ? 'bg-success' : 'bg-danger'}`}
              style={{ width: '8px', height: '8px' }}
            ></span>
            <span className="small">PostgreSQL: {isDbUp ? 'Connected' : 'Offline'}</span>
            <span className="text-secondary">|</span>
            <span className="small text-info">
              {hasAiKey ? 'Gemini 3.5 Flash' : 'Deterministic Fallback'}
            </span>
          </button>

          {/* Swagger link */}
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noopener noreferrer"
            className="btn btn-sm btn-outline-info d-flex align-items-center gap-1"
          >
            <i className="bi bi-file-earmark-code"></i>
            <span>Swagger API</span>
          </a>
        </div>
      </div>
    </nav>
  );
}
