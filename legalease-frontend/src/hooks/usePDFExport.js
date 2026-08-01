import { useState } from 'react'
import api from '@/api/axiosInstance'
import toast from 'react-hot-toast'

export function usePDFExport() {
  const [loading, setLoading] = useState(false)

  async function exportPDF(documentType, documentId) {
    setLoading(true)
    try {
      const res = await api.post(
        '/api/docs/export-pdf',
        { documentType, documentId },
        { responseType: 'blob' }
      )

      // Create download link from blob
      const url  = URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href  = url
      link.setAttribute('download',
        `${documentType.toLowerCase()}_${documentId}.pdf`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      URL.revokeObjectURL(url)

      toast.success('PDF downloaded successfully')
    } catch {
      toast.error('PDF export failed. Try again.')
    } finally {
      setLoading(false)
    }
  }

  return { exportPDF, loading }
}