import client from './client'

export const authApi = {
  register: (payload) => client.post('/auth/register', payload),
  login: (payload) => client.post('/auth/login', payload),
  verifyEmail: (token) => client.get(`/auth/verify-email`, { params: { token } }),
  forgotPassword: (email) => client.post('/auth/forgot-password', { email }),
  resetPassword: (payload) => client.post('/auth/reset-password', payload),
}
