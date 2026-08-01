import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '@/api/axiosBaseQuery'

export const aiApi = createApi({
  reducerPath: 'aiApi',
  baseQuery: axiosBaseQuery(),
  endpoints: build => ({
    parseAI: build.mutation({
      query: body => ({
        url: '/api/ai/parse', method: 'POST', data: body
      })
    })
  })
})

export const { useParseAIMutation } = aiApi