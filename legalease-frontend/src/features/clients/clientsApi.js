import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '@/api/axiosBaseQuery'

export const clientsApi = createApi({
  reducerPath: 'clientsApi',
  baseQuery: axiosBaseQuery(),
  tagTypes: ['Client'],
  endpoints: build => ({

    getClientsByCase: build.query({
      query: ({ caseId, page = 0, size = 10 }) => ({
        url: `/api/clients/case/${caseId}`,
        params: { page, size }
      }),
      providesTags: ['Client']
    }),

    getAllClients: build.query({
      query: ({ page = 0, size = 10, keyword } = {}) => ({
        url: '/api/clients',
        params: { page, size, keyword }
      }),
      providesTags: ['Client']
    }),

    getClientById: build.query({
      query: id => ({ url: `/api/clients/${id}` }),
      providesTags: (_, __, id) => [{ type: 'Client', id }]
    }),

    createClient: build.mutation({
      query: body => ({ url: '/api/clients/intake', method: 'POST', data: body }),
      invalidatesTags: ['Client']
    }),

    updateClient: build.mutation({
      query: ({ id, ...body }) => ({
        url: `/api/clients/${id}`, method: 'PUT', data: body
      }),
      invalidatesTags: ['Client']
    }),

    deleteClient: build.mutation({
      query: id => ({ url: `/api/clients/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Client']
    }),
  })
})

export const {
  useGetClientsByCaseQuery, useGetAllClientsQuery,
  useGetClientByIdQuery, useCreateClientMutation,
  useUpdateClientMutation, useDeleteClientMutation,
} = clientsApi