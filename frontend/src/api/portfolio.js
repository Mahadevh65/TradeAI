import client from './client'

export const portfolioApi = {
  buy: (payload) => client.post('/portfolio/buy', payload),
  sell: (payload) => client.post('/portfolio/sell', payload),
  summary: () => client.get('/portfolio/summary'),
  trades: (page = 0, size = 20) => client.get('/portfolio/trades', { params: { page, size } }),
  history: (days = 90) => client.get('/portfolio/history', { params: { days } }),
}
