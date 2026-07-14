import client from './client'

export const stockApi = {
  getDetails: (symbol) => client.get(`/stocks/${symbol}`),
  getHistory: (symbol, days = 90) => client.get(`/stocks/${symbol}/history`, { params: { days } }),
  search: (q) => client.get('/stocks/search', { params: { q } }),
  gainers: () => client.get('/stocks/movers/gainers'),
  losers: () => client.get('/stocks/movers/losers'),
}
