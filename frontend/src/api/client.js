import axios from 'axios'

// const client = axios.create({
//   baseURL: '/api/v1',
//   headers: { 'Content-Type': 'application/json' },
// })
const client = axios.create({
  baseURL: `${import.meta.env.VITE_API_BASE_URL}/api/v1`,
  headers: { 'Content-Type': 'application/json' },
})

let isRefreshing = false
let queue = []

const processQueue = (error, token = null) => {
  queue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token)
  })
  queue = []
}

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('tm_access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Never try to refresh on the auth endpoints themselves
    const isAuthCall = originalRequest.url?.startsWith('/auth/')

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthCall) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          queue.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return client(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem('tm_refresh_token')

      try {
        // const { data } = await axios.post('/api/v1/auth/refresh', { refreshToken })
        const { data } = await axios.post(
          `${import.meta.env.VITE_API_BASE_URL}/api/v1/auth/refresh`,
          { refreshToken }
        )
        const { accessToken, refreshToken: newRefreshToken } = data.data

        localStorage.setItem('tm_access_token', accessToken)
        localStorage.setItem('tm_refresh_token', newRefreshToken)

        processQueue(null, accessToken)
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return client(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        localStorage.removeItem('tm_access_token')
        localStorage.removeItem('tm_refresh_token')
        localStorage.removeItem('tm_user')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default client



// import axios from 'axios'

// const client = axios.create({
//   baseURL: '/api/v1',
//   headers: {
//     'Content-Type': 'application/json',
//   },
// })

// let isRefreshing = false
// let queue = []

// const processQueue = (error, token = null) => {
//   queue.forEach(({ resolve, reject }) => {
//     if (error) {
//       reject(error)
//     } else {
//       resolve(token)
//     }
//   })
//   queue = []
// }

// client.interceptors.request.use((config) => {
//   const token = localStorage.getItem('tm_access_token')

//   if (token) {
//     config.headers.Authorization = `Bearer ${token}`
//   }

//   return config
// })

// client.interceptors.response.use(
//   (response) => response,

//   async (error) => {
//     const originalRequest = error.config

//     const isAuthCall = originalRequest?.url?.startsWith('/auth/')

//     if (
//       error.response?.status === 401 &&
//       !originalRequest._retry &&
//       !isAuthCall
//     ) {
//       if (isRefreshing) {
//         return new Promise((resolve, reject) => {
//           queue.push({ resolve, reject })
//         }).then((token) => {
//           originalRequest.headers.Authorization = `Bearer ${token}`
//           return client(originalRequest)
//         })
//       }

//       originalRequest._retry = true
//       isRefreshing = true

//       const refreshToken = localStorage.getItem('tm_refresh_token')

//       try {
//         const { data } = await axios.post(
//           '/api/v1/auth/refresh',
//           {
//             refreshToken,
//           }
//         )

//         const {
//           accessToken,
//           refreshToken: newRefreshToken,
//         } = data.data

//         localStorage.setItem('tm_access_token', accessToken)
//         localStorage.setItem('tm_refresh_token', newRefreshToken)

//         processQueue(null, accessToken)

//         originalRequest.headers.Authorization = `Bearer ${accessToken}`

//         return client(originalRequest)
//       } catch (refreshError) {
//         processQueue(refreshError)

//         localStorage.removeItem('tm_access_token')
//         localStorage.removeItem('tm_refresh_token')
//         localStorage.removeItem('tm_user')

//         window.location.href = '/login'

//         return Promise.reject(refreshError)
//       } finally {
//         isRefreshing = false
//       }
//     }

//     return Promise.reject(error)
//   }
// )

// export default client