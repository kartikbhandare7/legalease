import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '@/api/axiosBaseQuery'

export const dashboardApi = createApi({
  reducerPath: 'dashboardApi',
  baseQuery: axiosBaseQuery(),
  endpoints: build => ({
    getDashboard: build.query({
      query: () => ({ url: '/api/dashboard' })
    })
  })
})

export const { useGetDashboardQuery } = dashboardApi