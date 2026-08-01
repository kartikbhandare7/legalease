import { format, parseISO, isValid } from 'date-fns'

export function formatDate(dateStr, pattern = 'dd MMM yyyy') {
  if (!dateStr) return '—'
  try {
    const date = typeof dateStr === 'string' ? parseISO(dateStr) : dateStr
    return isValid(date) ? format(date, pattern) : '—'
  } catch {
    return '—'
  }
}