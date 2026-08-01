import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '@/api/axiosBaseQuery'

export const authApi = createApi({
  reducerPath: 'authApi',
  baseQuery: axiosBaseQuery(),
  endpoints: build => ({
    login: build.mutation({
      query: body => ({
        url:    '/api/auth/login',
        method: 'POST',
        data:   body,
      })
    }),
    register: build.mutation({
      query: formData => ({
        url:     '/api/auth/register',
        method:  'POST',
        data:    formData,
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }),
  })
})

export const { useLoginMutation, useRegisterMutation } = authApi