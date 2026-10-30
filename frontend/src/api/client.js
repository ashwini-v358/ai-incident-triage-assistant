import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

export const incidentApi = {
  // Analyze pasted log
  analyzeLog: async (payload) => {
    const res = await api.post('/incidents/analyze', payload);
    return res.data;
  },

  // Upload log file
  uploadLogFile: async (formData) => {
    const res = await api.post('/incidents/analyze/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data;
  },

  // Get incident list with pagination & filters
  getIncidents: async (params = {}) => {
    const res = await api.get('/incidents', { params });
    return res.data;
  },

  // Get incident by ID
  getIncidentById: async (id) => {
    const res = await api.get(`/incidents/${id}`);
    return res.data;
  },

  // Update status and resolution notes
  updateIncidentStatus: async (id, statusData) => {
    const res = await api.patch(`/incidents/${id}/status`, statusData);
    return res.data;
  },

  // Delete incident
  deleteIncident: async (id) => {
    const res = await api.delete(`/incidents/${id}`);
    return res.data;
  },

  // Get metrics and distribution
  getStats: async () => {
    const res = await api.get('/incidents/stats');
    return res.data;
  },

  // Run benchmark evaluation
  runEvaluation: async () => {
    const res = await api.post('/evaluation/run');
    return res.data;
  },

  // Get sample benchmark cases
  getBenchmarkSamples: async () => {
    const res = await api.get('/evaluation/samples');
    return res.data;
  },

  // System health
  getHealth: async () => {
    const res = await api.get('/health');
    return res.data;
  },
};

export default api;
