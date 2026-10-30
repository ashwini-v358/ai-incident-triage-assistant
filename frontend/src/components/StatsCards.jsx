import React from 'react';

export default function StatsCards({ stats }) {
  if (!stats) return null;

  return (
    <div className="row g-3 mb-4">
      {/* Total Incidents */}
      <div className="col-md-3 col-sm-6">
        <div className="card metric-card p-3 border-primary border-opacity-25 h-100">
          <div className="text-secondary small text-uppercase fw-semibold">Total Incidents</div>
          <div className="fs-2 fw-bold text-white mt-1">{stats.totalIncidents}</div>
          <div className="text-muted small mt-1">Stored in PostgreSQL</div>
          <i className="bi bi-journal-text metric-icon text-primary"></i>
        </div>
      </div>

      {/* Open & Active */}
      <div className="col-md-3 col-sm-6">
        <div className="card metric-card p-3 border-danger border-opacity-25 h-100">
          <div className="text-secondary small text-uppercase fw-semibold">Open & Investigating</div>
          <div className="fs-2 fw-bold text-danger mt-1">
            {stats.openIncidents + stats.investigatingIncidents}
          </div>
          <div className="text-muted small mt-1">
            {stats.openIncidents} Open · {stats.investigatingIncidents} In Progress
          </div>
          <i className="bi bi-exclamation-triangle metric-icon text-danger"></i>
        </div>
      </div>

      {/* Critical Outages */}
      <div className="col-md-3 col-sm-6">
        <div className="card metric-card p-3 border-warning border-opacity-25 h-100">
          <div className="text-secondary small text-uppercase fw-semibold">Critical & High Severity</div>
          <div className="fs-2 fw-bold text-warning mt-1">
            {stats.criticalIncidents + stats.highIncidents}
          </div>
          <div className="text-muted small mt-1">
            {stats.criticalIncidents} Critical · {stats.highIncidents} High
          </div>
          <i className="bi bi-lightning-charge metric-icon text-warning"></i>
        </div>
      </div>

      {/* AI Confidence & Resolved */}
      <div className="col-md-3 col-sm-6">
        <div className="card metric-card p-3 border-success border-opacity-25 h-100">
          <div className="text-secondary small text-uppercase fw-semibold">Resolved & Quality</div>
          <div className="fs-2 fw-bold text-success mt-1">
            {stats.resolvedIncidents}
          </div>
          <div className="text-muted small mt-1">
            Avg AI Confidence: {Math.round((stats.averageConfidenceScore || 0.9) * 100)}%
          </div>
          <i className="bi bi-check-circle metric-icon text-success"></i>
        </div>
      </div>
    </div>
  );
}
