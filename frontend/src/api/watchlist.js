import client from './client'

export const watchlistApi = {
  getAll: () => client.get('/watchlists'),
  create: (name) => client.post('/watchlists', { name }),
  remove: (watchlistId) => client.delete(`/watchlists/${watchlistId}`),
  addItem: (watchlistId, symbol) => client.post(`/watchlists/${watchlistId}/items`, { symbol }),
  removeItem: (watchlistId, stockId) => client.delete(`/watchlists/${watchlistId}/items/${stockId}`),
  getItems: (watchlistId, sortBy, direction) =>
    client.get(`/watchlists/${watchlistId}/items`, { params: { sortBy, direction } }),
  createAlert: (payload) => client.post('/watchlists/alerts', payload),
  getAlerts: () => client.get('/watchlists/alerts'),
  deleteAlert: (alertId) => client.delete(`/watchlists/alerts/${alertId}`),
}
