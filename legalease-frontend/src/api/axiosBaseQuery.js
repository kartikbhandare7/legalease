import api from './axiosInstance'

export const axiosBaseQuery = () =>
  async ({ url, method = 'GET', data, params, headers }) => {
    try {
      const result = await api({ url, method, data, params, headers })
      return { data: result.data }
    } catch (err) {
      return {
        error: {
          status: err.response?.status,
          data:   err.response?.data ?? err.message,
        }
      }
    }
  }