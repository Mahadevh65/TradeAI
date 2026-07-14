import client from './client'

export const aiApi = {
  ask: (payload) => client.post('/ai/ask', payload),
}
