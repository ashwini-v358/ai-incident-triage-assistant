import React from 'react';

export default function IncidentTable({
  incidents = [],
  pageInfo = {},
  filters,
  onFilterChange,
  onPageChange,
  onViewIncident,
  onDeleteIncident,
  loading,
}) {
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

  const getCategoryBadge = (cat) => {
    switch (cat) {
      case 'DATABASE': return 'badge-database';
      case 'MEMORY_RESOURCE': return 'badge-memory';
      case 'NETWORK': return 'badge-network';
      case 'AUTHENTICATION': return 'badge-auth';
      case 'CONCURRENCY': return 'badge-concurrency';
      case 'APPLICATION_LOGIC': return 'badge-logic';
      default: return 'bg-secondary';
    }
  };

  return (
    <div className="card">
      <div className="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div className="d-flex align-items-center gap-2">
          <i className="bi bi-clock-history text-primary fs-5"></i>
          <span className="fs-6">Incident History & Root-Cause Database</span>
          <span className="badge bg-secondary">{pageInfo.totalElements || 0} Records</span>
        </div>
      </div>

      <div className="card-body">
        {/* Filters Bar */}
        <div className="row g-2 mb-3">
          <div className="col-md-4">
            <div className="input-group input-group-sm">
              <span className="input-group-text bg-dark text-secondary border-secondary">
                <i className="bi bi-search"></i>
              </span>
              <input
                type="text"
                className="form-control bg-dark text-light border-secondary"
                placeholder="Search by title, class, or root-cause keyword..."
                value={filters.search || ''}
                onChange={(e) => onFilterChange({ ...filters, search: e.target.value, page: 0 })}
              />
            </div>
          </div>

          <div className="col-md-2 col-sm-4">
            <select
              className="form-select form-select-sm bg-dark text-light border-secondary"
              value={filters.category || ''}
              onChange={(e) => onFilterChange({ ...filters, category: e.target.value, page: 0 })}
            >
              <option value="">All Categories</option>
              <option value="DATABASE">Database & Persistence</option>
              <option value="MEMORY_RESOURCE">Memory & Resources</option>
              <option value="NETWORK">Network & Connectivity</option>
              <option value="AUTHENTICATION">Auth & Security</option>
              <option value="CONCURRENCY">Concurrency & Threads</option>
              <option value="APPLICATION_LOGIC">Application Logic</option>
              <option value="THIRD_PARTY">Third Party</option>
            </select>
          </div>

          <div className="col-md-2 col-sm-4">
            <select
              className="form-select form-select-sm bg-dark text-light border-secondary"
              value={filters.severity || ''}
              onChange={(e) => onFilterChange({ ...filters, severity: e.target.value, page: 0 })}
            >
              <option value="">All Severities</option>
              <option value="CRITICAL">Critical</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </div>

          <div className="col-md-2 col-sm-4">
            <select
              className="form-select form-select-sm bg-dark text-light border-secondary"
              value={filters.status || ''}
              onChange={(e) => onFilterChange({ ...filters, status: e.target.value, page: 0 })}
            >
              <option value="">All Statuses</option>
              <option value="OPEN">Open</option>
              <option value="INVESTIGATING">Investigating</option>
              <option value="MITIGATED">Mitigated</option>
              <option value="RESOLVED">Resolved</option>
            </select>
          </div>

          <div className="col-md-2 d-flex justify-content-end">
            <button
              className="btn btn-sm btn-outline-secondary w-100"
              onClick={() => onFilterChange({ search: '', category: '', severity: '', status: '', page: 0 })}
            >
              <i className="bi bi-x-circle me-1"></i> Reset Filters
            </button>
          </div>
        </div>

        {/* Incidents Table */}
        <div className="table-responsive">
          <table className="table table-dark table-hover align-middle mb-0">
            <thead>
              <tr>
                <th style={{ width: '80px' }}>ID</th>
                <th>Incident & Service</th>
                <th>Category</th>
                <th>Severity</th>
                <th>Status</th>
                <th>Culprit Class</th>
                <th>Analysis Provider</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="8" className="text-center py-4 text-secondary">
                    <div className="spinner-border spinner-border-sm me-2" role="status"></div>
                    Loading incidents from PostgreSQL...
                  </td>
                </tr>
              ) : incidents.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center py-4 text-secondary">
                    <i className="bi bi-inbox fs-3 d-block mb-1"></i>
                    No incidents found matching current filters.
                  </td>
                </tr>
              ) : (
                incidents.map((inc) => (
                  <tr key={inc.id}>
                    <td>
                      <span className="font-monospace text-secondary">#{inc.id}</span>
                    </td>
                    <td>
                      <div className="fw-semibold text-white text-truncate" style={{ maxWidth: '280px' }}>
                        {inc.title}
                      </div>
                      <div className="small text-secondary">
                        <span className="badge bg-dark border border-secondary me-1">{inc.serviceName}</span>
                        <span>{new Date(inc.createdAt).toLocaleString()}</span>
                      </div>
                    </td>
                    <td>
                      <span className={`badge ${getCategoryBadge(inc.category)}`}>
                        {inc.category}
                      </span>
                    </td>
                    <td>
                      <span className={`badge ${getSeverityBadge(inc.severity)}`}>
                        {inc.severity}
                      </span>
                    </td>
                    <td>
                      <span className={`badge ${getStatusBadge(inc.status)}`}>
                        {inc.status}
                      </span>
                    </td>
                    <td>
                      {inc.culpritClass ? (
                        <div className="font-monospace small text-truncate text-warning" style={{ maxWidth: '200px' }} title={inc.culpritClass}>
                          {inc.culpritClass.split('.').slice(-2).join('.')}
                          {inc.culpritLineNumber && <span className="text-secondary">:{inc.culpritLineNumber}</span>}
                        </div>
                      ) : (
                        <span className="text-muted small">-</span>
                      )}
                    </td>
                    <td>
                      <span className="badge bg-secondary bg-opacity-25 text-light small border border-secondary border-opacity-50">
                        {inc.analysisProvider?.includes('GEMINI') ? '✨ Gemini AI' : '⚙️ Fallback'}
                      </span>
                    </td>
                    <td className="text-end">
                      <div className="btn-group btn-group-sm">
                        <button
                          type="button"
                          className="btn btn-outline-info"
                          onClick={() => onViewIncident(inc)}
                          title="View Root-Cause Report"
                        >
                          <i className="bi bi-eye"></i> View
                        </button>
                        <button
                          type="button"
                          className="btn btn-outline-danger"
                          onClick={() => onDeleteIncident(inc.id)}
                          title="Delete Incident"
                        >
                          <i className="bi bi-trash"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {pageInfo.totalPages > 1 && (
          <div className="d-flex justify-content-between align-items-center mt-3 pt-3 border-top border-secondary-subtle">
            <span className="small text-secondary">
              Page {pageInfo.number + 1} of {pageInfo.totalPages} ({pageInfo.totalElements} incidents)
            </span>
            <div className="btn-group btn-group-sm">
              <button
                className="btn btn-outline-secondary"
                disabled={pageInfo.first}
                onClick={() => onPageChange(pageInfo.number - 1)}
              >
                Previous
              </button>
              <button
                className="btn btn-outline-secondary"
                disabled={pageInfo.last}
                onClick={() => onPageChange(pageInfo.number + 1)}
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
