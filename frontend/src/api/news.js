import client from './client'

export const newsApi = {
  getLatest: (category) => client.get('/news', { params: { category } }),
}
