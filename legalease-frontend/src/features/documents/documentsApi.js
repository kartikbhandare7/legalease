import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '@/api/axiosBaseQuery'

export const documentsApi = createApi({
  reducerPath: 'documentsApi',
  baseQuery: axiosBaseQuery(),
  endpoints: build => ({
    exportPDF: build.mutation({
      query: body => ({
        url: '/api/docs/export-pdf', method: 'POST', data: body
      })
    })
  })
})

export const { useExportPDFMutation } = documentsApi