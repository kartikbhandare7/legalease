import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  useGetAllClientsQuery,
  useDeleteClientMutation
} from './clientsApi'
import Table from '@/components/common/Table'
import Pagination from '@/components/common/Pagination'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import { useRole } from '@/hooks/useRole'
import { usePDFExport } from '@/hooks/usePDFExport'
import { formatDate } from '@/utils/formatDate'
import {
  Search, Eye, Trash2,
  FileDown, Sparkles, Users
} from 'lucide-react'
import toast from 'react-hot-toast'

export default function ClientsPage() {
  const navigate = useNavigate()
  const { isLawyer } = useRole()
  const { exportPDF, loading: pdfLoading } = usePDFExport()

  const [page,     setPage]     = useState(0)
  const [search,   setSearch]   = useState('')
  const [keyword,  setKeyword]  = useState('')
  const [deleteId, setDeleteId] = useState(null)

  const { data, isLoading } = useGetAllClientsQuery({
    page, size: 10, keyword: keyword || undefined
  })

  const [deleteClient, { isLoading: deleting }] = useDeleteClientMutation()

  function handleSearchKey(e) {
    if (e.key === 'Enter') { setKeyword(search); setPage(0) }
  }

  async function handleDelete() {
    try {
      await deleteClient(deleteId).unwrap()
      toast.success('Client removed')
      setDeleteId(null)
    } catch {
      toast.error('Delete failed. Try again.')
    }
  }

  const columns = [
    {
      key: 'clientName',
      label: 'Client',
      render: row => (
        <div className="flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-full bg-accent/10 flex items-center
                           justify-center text-accent text-xs font-semibold
                           font-mono shrink-0">
            {row.clientName.charAt(0)}
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <p className="font-semibold text-ink text-sm">
                {row.clientName}
              </p>
              {row.aiAssisted && (
                <span className="ai-badge flex items-center gap-1">
                  <Sparkles size={9} /> AI
                </span>
              )}
            </div>
            <p className="text-xs text-text-muted font-body">
              {row.email ?? row.phone ?? '—'}
            </p>
          </div>
        </div>
      )
    },
    {
      key: 'opposingParty',
      label: 'Opposing party',
      render: row => (
        <span className="text-sm text-text-muted font-body">
          {row.opposingParty ?? '—'}
        </span>
      )
    },
    {
      key: 'caseTitle',
      label: 'Case',
      render: row => (
        <button
          onClick={e => { e.stopPropagation(); navigate(`/cases/${row.caseId}`) }}
          className="text-sm text-accent hover:underline font-body text-left"
        >
          {row.caseTitle}
        </button>
      )
    },
    {
      key: 'createdAt',
      label: 'Added',
      render: row => (
        <span className="font-mono text-xs text-text-muted">
          {formatDate(row.createdAt)}
        </span>
      )
    },
    {
      key: 'actions',
      label: '',
      render: row => (
        <div className="flex items-center gap-1 justify-end">
          <button
            onClick={() => navigate(`/clients/${row.id}`)}
            className="p-1.5 rounded hover:bg-surface-alt text-text-muted
                        hover:text-ink transition-colors"
            title="View client"
          >
            <Eye size={14} />
          </button>

          {isLawyer && (
            <>
              <button
                onClick={() => exportPDF('CLIENT_INTAKE', row.id)}
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
                title="Delete client"
              >
                <Trash2 size={14} />
              </button>
            </>
          )}
        </div>
      )
    },
  ]

  return (
    <div className="max-w-6xl space-y-6">

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="font-display text-xl font-semibold text-ink">
            Clients
          </h2>
          <p className="text-sm text-text-muted font-body mt-0.5">
            {data?.totalElements ?? 0} total clients across all cases
          </p>
        </div>
      </div>

      {/* Search */}
      <div className="relative max-w-72">
        <Search size={13}
          className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
        <input
          value={search}
          onChange={e => setSearch(e.target.value)}
          onKeyDown={handleSearchKey}
          placeholder="Search by name — press Enter"
          className="w-full h-9 pl-8 pr-3 text-sm font-body border
                      border-border rounded bg-white focus:outline-none
                      focus:ring-2 focus:ring-accent/30 focus:border-accent"
        />
        {search && (
          <button
            onClick={() => { setSearch(''); setKeyword(''); setPage(0) }}
            className="absolute right-2.5 top-1/2 -translate-y-1/2
                        text-text-muted hover:text-ink text-xs"
          >✕</button>
        )}
      </div>

      <Table
        columns={columns}
        data={data?.content}
        loading={isLoading}
        emptyTitle="No clients found"
      />

      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 0}
        onPageChange={setPage}
      />

      <ConfirmDialog
        open={!!deleteId}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDelete}
        loading={deleting}
        title="Remove client"
        description="This will permanently remove the client and their intake record."
        confirmLabel="Remove client"
      />

    </div>
  )
}