import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '@/api/axiosBaseQuery'

export const casesApi = createApi({
  reducerPath: 'casesApi',
  baseQuery: axiosBaseQuery(),
  tagTypes: ['Case'],
  endpoints: build => ({

    getCases: build.query({
      query: ({ page = 0, size = 10, status, keyword } = {}) => ({
        url: '/api/cases',
        params: { page, size, status, keyword }
      }),
      providesTags: ['Case']
    }),

    getCaseById: build.query({
      query: id => ({ url: `/api/cases/${id}` }),
      providesTags: (_, __, id) => [{ type: 'Case', id }]
    }),

    createCase: build.mutation({
      query: body => ({ url: '/api/cases', method: 'POST', data: body }),
      invalidatesTags: ['Case']
    }),

    updateCase: build.mutation({
      query: ({ id, ...body }) => ({
        url: `/api/cases/${id}`, method: 'PUT', data: body
      }),
      invalidatesTags: ['Case']
    }),

    updateCaseStatus: build.mutation({
      query: ({ id, caseStatus }) => ({
        url: `/api/cases/${id}/status`, method: 'PATCH',
        data: { caseStatus }
      }),
      invalidatesTags: ['Case']
    }),

    deleteCase: build.mutation({
      query: id => ({ url: `/api/cases/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Case']
    }),

    getActiveCaseCount: build.query({
      query: () => ({ url: '/api/cases/count/active' }),
      providesTags: ['Case']
    }),
  })
})

export const {
  useGetCasesQuery, useGetCaseByIdQuery, useCreateCaseMutation,
  useUpdateCaseMutation, useUpdateCaseStatusMutation,
  useDeleteCaseMutation, useGetActiveCaseCountQuery,
} = casesApi