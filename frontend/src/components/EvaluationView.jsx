import React, { useState } from 'react';
import { incidentApi } from '../api/client';

export default function EvaluationView({ onIncidentCreated }) {
  const [evaluation, setEvaluation] = useState(null);
  const [running, setRunning] = useState(false);
  const [error, setError] = useState(null);

  const handleRunEvaluation = async () => {
    setRunning(true);
    setError(null);
    try {
      const result = await incidentApi.runEvaluation();
      setEvaluation(result);
    } catch (err) {
      console.error('Evaluation run failed:', err);
      setError(err.response?.data?.message || err.message || 'Benchmark evaluation failed');
    } finally {
      setRunning(false);
    }
  };

  return (
    <div className="card">
      <div className="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div>
          <h5 className="mb-0 text-white d-flex align-items-center gap-2">
            <i className="bi bi-speedometer2 text-info"></i>
            <span>Evaluation & Measurable Quality Benchmark Suite</span>
          </h5>
          <span className="small text-secondary">
            Runs automated evaluation across 8 ground-truth production incident datasets
          </span>
        </div>
        <button
          className="btn btn-primary d-flex align-items-center gap-2"
          onClick={handleRunEvaluation}
          disabled={running}
        >
          {running ? (
            <>
              <span className="spinner-border spinner-border-sm" role="status"></span>
              <span>Running Benchmarks...</span>
            </>
          ) : (
            <>
              <i className="bi bi-play-circle-fill"></i>
              <span>Execute Benchmark Suite</span>
            </>
          )}
        </button>
      </div>

      <div className="card-body">
        {error && (
          <div className="alert alert-danger py-2 mb-3">
            <i className="bi bi-exclamation-triangle me-2"></i> {error}
          </div>
        )}

        {!evaluation && !running && (
          <div className="text-center py-5 text-secondary">
            <i className="bi bi-cpu fs-1 d-block mb-3 text-info opacity-50"></i>
            <h5>Incident Triage Benchmark Suite Ready</h5>
            <p className="small text-muted mx-auto" style={{ maxWidth: '540px' }}>
              Click <strong>"Execute Benchmark Suite"</strong> above to benchmark the Java log parser,
              rule-based classification engine, and root-cause analysis across real-world incident test cases
              (PostgreSQL pool starvation, database deadlocks, OutOfMemoryError, socket timeouts, JWT auth faults, etc.).
            </p>
          </div>
        )}

        {running && (
          <div className="text-center py-5 text-secondary">
            <div className="spinner-border text-info mb-3" role="status"></div>
            <h5>Evaluating Incident Triage Engine...</h5>
            <p className="small text-muted">Triaging benchmark test cases & computing precision, recall, and latency metrics</p>
          </div>
        )}

        {evaluation && (
          <div>
            {/* Metric Scorecards */}
            <div className="row g-3 mb-4">
              <div className="col-md-3 col-sm-6">
                <div className="card p-3 border-success border-opacity-50 h-100" style={{ backgroundColor: '#0f291e' }}>
                  <div className="text-secondary small text-uppercase fw-semibold">Category Accuracy</div>
                  <div className="fs-2 fw-bold text-success mt-1">{evaluation.categoryAccuracyPercent}%</div>
                  <div className="text-muted small">
                    {evaluation.categoryMatches} / {evaluation.totalSamples} correct classifications
                  </div>
                </div>
              </div>

              <div className="col-md-3 col-sm-6">
                <div className="card p-3 border-info border-opacity-50 h-100" style={{ backgroundColor: '#0c2738' }}>
                  <div className="text-secondary small text-uppercase fw-semibold">Severity Accuracy</div>
                  <div className="fs-2 fw-bold text-info mt-1">{evaluation.severityAccuracyPercent}%</div>
                  <div className="text-muted small">
                    {evaluation.severityMatches} / {evaluation.totalSamples} severity alignments
                  </div>
                </div>
              </div>

              <div className="col-md-3 col-sm-6">
                <div className="card p-3 border-primary border-opacity-50 h-100" style={{ backgroundColor: '#131e3b' }}>
                  <div className="text-secondary small text-uppercase fw-semibold">Root Cause Match</div>
                  <div className="fs-2 fw-bold text-primary mt-1">{evaluation.rootCauseAccuracyPercent}%</div>
                  <div className="text-muted small">
                    {evaluation.rootCauseMatches} / {evaluation.totalSamples} keyword matches
                  </div>
                </div>
              </div>

              <div className="col-md-3 col-sm-6">
                <div className="card p-3 border-warning border-opacity-50 h-100" style={{ backgroundColor: '#2b210c' }}>
                  <div className="text-secondary small text-uppercase fw-semibold">Average Latency</div>
                  <div className="fs-2 fw-bold text-warning mt-1">{evaluation.averageLatencyMs} ms</div>
                  <div className="text-muted small">
                    Active Provider: {evaluation.primaryProvider}
                  </div>
                </div>
              </div>
            </div>

            {/* Benchmark Samples Results Table */}
            <h6 className="text-secondary small text-uppercase fw-semibold mb-2">Detailed Test Case Results</h6>
            <div className="table-responsive">
              <table className="table table-dark table-hover align-middle small mb-0">
                <thead>
                  <tr>
                    <th>Sample ID</th>
                    <th>Scenario</th>
                    <th>Category (Ground Truth vs Predicted)</th>
                    <th>Severity (Expected vs Predicted)</th>
                    <th>Root Cause Alignment</th>
                    <th>Latency</th>
                    <th>Fixes Generated</th>
                  </tr>
                </thead>
                <tbody>
                  {evaluation.sampleResults?.map((r) => (
                    <tr key={r.sampleId}>
                      <td className="font-monospace text-secondary">{r.sampleId}</td>
                      <td>
                        <strong className="text-white">{r.sampleName}</strong>
                      </td>
                      <td>
                        <div className="d-flex align-items-center gap-2">
                          <span className={`badge ${r.categoryMatch ? 'bg-success' : 'bg-danger'}`}>
                            {r.categoryMatch ? '✓ MATCH' : '✗ MISMATCH'}
                          </span>
                          <span className="text-secondary">{r.predictedCategory}</span>
                        </div>
                      </td>
                      <td>
                        <div className="d-flex align-items-center gap-2">
                          <span className={`badge ${r.severityMatch ? 'bg-success' : 'bg-warning text-dark'}`}>
                            {r.severityMatch ? '✓ MATCH' : '✗ DIFF'}
                          </span>
                          <span className="text-secondary">{r.predictedSeverity}</span>
                        </div>
                      </td>
                      <td>
                        <span className={`badge ${r.rootCauseMatch ? 'bg-success bg-opacity-25 text-success' : 'bg-warning bg-opacity-25 text-warning'}`}>
                          {r.rootCauseMatch ? '✓ Keyword Aligned' : 'Partial Match'}
                        </span>
                      </td>
                      <td className="font-monospace text-muted">{r.executionTimeMs} ms</td>
                      <td>
                        <span className="badge bg-secondary">{r.recommendedFixes?.length || 0} recommendations</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

