import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '@/api/axiosBaseQuery'

export const adminApi = createApi({
  reducerPath: 'adminApi',
  baseQuery: axiosBaseQuery(),
  tagTypes: ['AdminUser'],
  endpoints: build => ({

    getAdminDashboard: build.query({
      query: () => ({ url: '/api/admin/dashboard' }),
    }),

    getPendingLawyers: build.query({
      query: ({ page = 0, size = 10 } = {}) => ({
        url: '/api/admin/pending-lawyers', params: { page, size }
      }),
      providesTags: ['AdminUser']
    }),

    getAllUsers: build.query({
      query: ({ page = 0, size = 10, role } = {}) => ({
        url: '/api/admin/users', params: { page, size, role }
      }),
      providesTags: ['AdminUser']
    }),

    updateApproval: build.mutation({
      query: ({ userId, ...body }) => ({
        url: `/api/admin/users/${userId}/approval`,
        method: 'PATCH', data: body
      }),
      invalidatesTags: ['AdminUser']
    }),

    deleteUser: build.mutation({
      query: userId => ({
        url: `/api/admin/users/${userId}`, method: 'DELETE'
      }),
      invalidatesTags: ['AdminUser']
    }),
  })
})

export const {
  useGetAdminDashboardQuery, useGetPendingLawyersQuery,
  useGetAllUsersQuery, useUpdateApprovalMutation, useDeleteUserMutation,
} = adminApi