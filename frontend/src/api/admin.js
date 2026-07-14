import client from './client'

export const adminApi = {
  listUsers: (page = 0, size = 20) => client.get('/admin/users', { params: { page, size } }),
  updateRoles: (userId, roles) => client.put(`/admin/users/${userId}/roles`, { roles }),
  setActive: (userId, active) => client.put(`/admin/users/${userId}/active`, null, { params: { active } }),
  unlock: (userId) => client.put(`/admin/users/${userId}/unlock`),
  allTrades: (page = 0, size = 20) => client.get('/admin/trades', { params: { page, size } }),
  auditLogs: (action, page = 0, size = 50) => client.get('/admin/audit-logs', { params: { action, page, size } }),
  dashboardStats: () => client.get('/admin/dashboard-stats'),
  marketConfig: () => client.get('/admin/market-config'),
  updateMarketConfig: (marketName, payload) => client.put(`/admin/market-config/${marketName}`, payload),
}
