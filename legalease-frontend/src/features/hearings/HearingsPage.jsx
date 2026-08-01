import { useState } from 'react'
import {
  useGetAllHearingsQuery,
  useDeleteHearingMutation
} from './hearingsApi'
import HearingLogForm from './HearingLogForm'
import Table from '@/components/common/Table'
import Button from '@/components/common/Button'
import Pagination from '@/components/common/Pagination'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import { useRole } from '@/hooks/useRole'
import { usePDFExport } from '@/hooks/usePDFExport'
import { formatDate } from '@/utils/formatDate'
import { Plus, FileDown, Trash2, Sparkles } from 'lucide-react'
import toast from 'react-hot-toast'

export default function HearingsPage() {
  const { isLawyer } = useRole()
  const { exportPDF, loading: pdfLoading } = usePDFExport()

  const [page,      setPage]      = useState(0)
  const [formOpen,  setFormOpen]  = useState(false)
  const [deleteId,  setDeleteId]  = useState(null)

  const { data, isLoading } = useGetAllHearingsQuery({ page, size: 10 })
  const [deleteHearing, { isLoading: deleting }] = useDeleteHearingMutation()

  async function handleDelete() {
    try {
      await deleteHearing(deleteId).unwrap()
      toast.success('Hearing log removed')
      setDeleteId(null)
    } catch {
      toast.error('Delete failed')
    }
  }

  const columns = [
    {
      key: 'hearingDate',
      label: 'Date',
      render: row => (
        <div className="flex items-center gap-2">
          <div className="w-9 text-center shrink-0">
            <p className="font-mono text-base font-semibold text-accent
                           leading-none">
              {formatDate(row.hearingDate, 'dd')}
            </p>
            <p className="font-body text-xs text-text-muted uppercase">
              {formatDate(row.hearingDate, 'MMM')}
            </p>
          </div>
          <div className="w-px h-7 bg-border" />
          <div>
            <div className="flex items-center gap-1.5">
              <p className="font-semibold text-sm text-ink font-body">
                {row.caseTitle}
              </p>
              {row.aiAssisted && (
                <span className="ai-badge flex items-center gap-1">
                  <Sparkles size={9} /> AI
                </span>
              )}
            </div>
            <p className="text-xs text-text-muted font-body">
              {row.lawyerName}
            </p>
          </div>
        </div>
      )
    },
    {
      key: 'outcome',
      label: 'Outcome',
      render: row => (
        <span className="text-sm text-text-muted font-body line-clamp-2
                          max-w-xs">
          {row.outcome ?? '—'}
        </span>
      )
    },
    {
      key: 'nextDate',
      label: 'Next date',
      render: row => row.nextDate
        ? (
          <span className="font-mono text-sm font-semibold text-accent">
            {formatDate(row.nextDate)}
          </span>
        )
        : <span className="text-text-muted text-sm">—</span>
    },
    {
      key: 'actions',
      label: '',
      render: row => (
        <div className="flex items-center gap-1 justify-end">
          {isLawyer && (
            <>
              <button
                onClick={() => exportPDF('HEARING_LOG', row.id)}
                disabled={pdfLoading}
                className="p-1.5 rounded hover:bg-surface-alt text-text-muted
                            hover:text-accent transition-colors"
                title="Export PDF"
              >
                <FileDown size={14} />
              </button>
              <button
                onClick={() => setDeleteId(row.id)}
                className="p-1.5 rounded hover:bg-danger/10 text-text-muted
                            hover:text-danger transition-colors"
                title="Delete"
              >
                <Trash2 size={14} />
              </button>
            </>
          )}
        </div>
      )
    }
  ]

  return (
    <div className="max-w-6xl space-y-6">

      <div className="flex items-center justify-between">
        <div>
          <h2 className="font-display text-xl font-semibold text-ink">
            Hearings
          </h2>
          <p className="text-sm text-text-muted font-body mt-0.5">
            {data?.totalElements ?? 0} total hearing logs
          </p>
        </div>
        <Button onClick={() => setFormOpen(true)}>
          <Plus size={14} /> Log hearing
        </Button>
      </div>

      <Table
        columns={columns}
        data={data?.content}
        loading={isLoading}
        emptyTitle="No hearings logged yet"
      />

      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 0}
        onPageChange={setPage}
      />

      <HearingLogForm
        open={formOpen}
        onClose={() => setFormOpen(false)}
        caseId={null}  // user selects case inside form on standalone page
      />

      <ConfirmDialog
        open={!!deleteId}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDelete}
        loading={deleting}
        title="Delete hearing log"
        description="This permanently removes this hearing log."
        confirmLabel="Delete"
      />
    </div>
  )
}