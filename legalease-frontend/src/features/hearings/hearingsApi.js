import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '@/api/axiosBaseQuery'

export const hearingsApi = createApi({
  reducerPath: 'hearingsApi',
  baseQuery: axiosBaseQuery(),
  tagTypes: ['Hearing'],
  endpoints: build => ({

    getHearingsByCase: build.query({
      query: ({ caseId, page = 0, size = 10 }) => ({
        url: `/api/hearings/case/${caseId}`,
        params: { page, size }
      }),
      providesTags: ['Hearing']
    }),

    getAllHearings: build.query({
      query: ({ page = 0, size = 10 } = {}) => ({
        url: '/api/hearings', params: { page, size }
      }),
      providesTags: ['Hearing']
    }),

    getUpcomingHearings: build.query({
      query: () => ({ url: '/api/hearings/upcoming' }),
      providesTags: ['Hearing']
    }),

    logHearing: build.mutation({
      query: body => ({ url: '/api/hearings/log', method: 'POST', data: body }),
      invalidatesTags: ['Hearing']
    }),

    updateHearing: build.mutation({
      query: ({ id, ...body }) => ({
        url: `/api/hearings/${id}`, method: 'PUT', data: body
      }),
      invalidatesTags: ['Hearing']
    }),

    deleteHearing: build.mutation({
      query: id => ({ url: `/api/hearings/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Hearing']
    }),
  })
})

export const {
  useGetHearingsByCaseQuery, useGetAllHearingsQuery,
  useGetUpcomingHearingsQuery, useLogHearingMutation,
  useUpdateHearingMutation, useDeleteHearingMutation,
} = hearingsApi