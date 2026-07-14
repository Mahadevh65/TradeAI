import client from './client'

export const analyticsApi = {
  growth: (days = 180) => client.get('/analytics/growth', { params: { days } }),
  sectorAllocation: () => client.get('/analytics/sector-allocation'),
  monthlyReturns: (months = 12) => client.get('/analytics/monthly-returns', { params: { months } }),
  dividendIncome: () => client.get('/analytics/dividend-income'),
}
