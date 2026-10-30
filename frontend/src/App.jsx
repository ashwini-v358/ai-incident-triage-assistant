import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import StatsCards from './components/StatsCards';
import LogTriageForm from './components/LogTriageForm';
import IncidentTable from './components/IncidentTable';
import IncidentDetailModal from './components/IncidentDetailModal';
import EvaluationView from './components/EvaluationView';
import SystemHealthModal from './components/SystemHealthModal';
import { incidentApi } from './api/client';

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard'); // 'dashboard', 'history', 'evaluation'
  const [stats, setStats] = useState(null);
  const [healthData, setHealthData] = useState(null);
  const [benchmarkSamples, setBenchmarkSamples] = useState([]);
  const [incidents, setIncidents] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 0, totalElements: 0, first: true, last: true });
  const [filters, setFilters] = useState({ search: '', category: '', severity: '', status: '', page: 0, size: 10 });
  const [selectedIncident, setSelectedIncident] = useState(null);
  const [showHealthModal, setShowHealthModal] = useState(false);
  const [loadingIncidents, setLoadingIncidents] = useState(false);

  // Fetch Stats
  const loadStats = useCallback(async () => {
    try {
      const data = await incidentApi.getStats();
      setStats(data);
    } catch (err) {
      console.warn('Failed to load stats:', err);
    }
  }, []);

  // Fetch Health
  const loadHealth = useCallback(async () => {
    try {
      const data = await incidentApi.getHealth();
      setHealthData(data);
    } catch (err) {
      console.warn('Failed to load health:', err);
    }
  }, []);

  // Fetch Benchmark Samples
  const loadSamples = useCallback(async () => {
    try {
      const data = await incidentApi.getBenchmarkSamples();
      setBenchmarkSamples(data);
    } catch (err) {
      console.warn('Failed to load benchmark samples:', err);
    }
  }, []);

  // Fetch Incidents
  const loadIncidents = useCallback(async () => {
    setLoadingIncidents(true);
    try {
      const params = {
        page: filters.page,
        size: filters.size,
        search: filters.search || undefined,
        category: filters.category || undefined,
        severity: filters.severity || undefined,
        status: filters.status || undefined,
        sortBy: 'createdAt',
        direction: 'desc',
      };
      const res = await incidentApi.getIncidents(params);
      setIncidents(res.content || []);
      setPageInfo({
        number: res.number,
        totalPages: res.totalPages,
        totalElements: res.totalElements,
        first: res.first,
        last: res.last,
      });
    } catch (err) {
      console.error('Failed to load incidents:', err);
    } finally {
      setLoadingIncidents(false);
    }
  }, [filters]);

  useEffect(() => {
    loadHealth();
    loadStats();
    loadSamples();
  }, [loadHealth, loadStats, loadSamples]);

  useEffect(() => {
    loadIncidents();
  }, [loadIncidents]);

  // When a new incident is analyzed and saved
  const handleIncidentAnalyzed = (newIncident) => {
    setSelectedIncident(newIncident);
    loadStats();
    loadIncidents();
  };

  // When status is updated
  const handleStatusUpdated = (updatedIncident) => {
    setSelectedIncident(updatedIncident);
    loadStats();
    loadIncidents();
  };

  // When an incident is deleted
  const handleDeleteIncident = async (id) => {
    if (!window.confirm(`Are you sure you want to delete incident #${id}?`)) return;
    try {
      await incidentApi.deleteIncident(id);
      if (selectedIncident?.id === id) {
        setSelectedIncident(null);
      }
      loadStats();
      loadIncidents();
    } catch (err) {
      alert('Failed to delete incident: ' + err.message);
    }
  };

  return (
    <div className="min-vh-100 d-flex flex-column">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onOpenHealth={() => setShowHealthModal(true)}
        healthData={healthData}
      />

      <div className="container-fluid py-4 px-4 flex-grow-1">
        {/* Metric Overview Cards */}
        <StatsCards stats={stats} />

        {/* Tab Navigation */}
        <ul className="nav nav-tabs mb-4">
          <li className="nav-item">
            <button
              className={`nav-link ${activeTab === 'dashboard' ? 'active' : ''}`}
              onClick={() => setActiveTab('dashboard')}
            >
              <i className="bi bi-shield-shaded me-2"></i>
              Active Triage & Dashboard
            </button>
          </li>
          <li className="nav-item">
            <button
              className={`nav-link ${activeTab === 'history' ? 'active' : ''}`}
              onClick={() => setActiveTab('history')}
            >
              <i className="bi bi-database me-2"></i>
              Incident History & Filters ({pageInfo.totalElements})
            </button>
          </li>
          <li className="nav-item">
            <button
              className={`nav-link ${activeTab === 'evaluation' ? 'active' : ''}`}
              onClick={() => setActiveTab('evaluation')}
            >
              <i className="bi bi-speedometer2 me-2"></i>
              Quality Evaluation & Benchmarks
            </button>
          </li>
        </ul>

        {/* Tab Content */}
        {activeTab === 'dashboard' && (
          <div>
            <LogTriageForm
              onIncidentAnalyzed={handleIncidentAnalyzed}
              benchmarkSamples={benchmarkSamples}
            />

            <IncidentTable
              incidents={incidents}
              pageInfo={pageInfo}
              filters={filters}
              onFilterChange={setFilters}
              onPageChange={(newPage) => setFilters({ ...filters, page: newPage })}
              onViewIncident={setSelectedIncident}
              onDeleteIncident={handleDeleteIncident}
              loading={loadingIncidents}
            />
          </div>
        )}

        {activeTab === 'history' && (
          <IncidentTable
            incidents={incidents}
            pageInfo={pageInfo}
            filters={filters}
            onFilterChange={setFilters}
            onPageChange={(newPage) => setFilters({ ...filters, page: newPage })}
            onViewIncident={setSelectedIncident}
            onDeleteIncident={handleDeleteIncident}
            loading={loadingIncidents}
          />
        )}

        {activeTab === 'evaluation' && (
          <EvaluationView
            onIncidentCreated={handleIncidentAnalyzed}
          />
        )}
      </div>

      {/* Incident Detail Modal */}
      {selectedIncident && (
        <IncidentDetailModal
          incident={selectedIncident}
          onClose={() => setSelectedIncident(null)}
          onStatusUpdated={handleStatusUpdated}
        />
      )}

      {/* System Health Diagnostics Modal */}
      {showHealthModal && (
        <SystemHealthModal
          healthData={healthData}
          onClose={() => setShowHealthModal(false)}
          onRefresh={loadHealth}
        />
      )}

      {/* Footer */}
      <footer className="border-top border-secondary-subtle py-3 px-4 text-center text-secondary small" style={{ backgroundColor: '#161b22' }}>
        <span>AI-Powered Incident Triage & Root-Cause Assistant · Spring Boot 3.5 & PostgreSQL 18 & Google Gemini API · Temurin Java 21</span>
      </footer>
    </div>
  );
}

