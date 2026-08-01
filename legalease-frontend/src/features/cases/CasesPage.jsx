import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  useGetCasesQuery,
  useDeleteCaseMutation
} from './casesApi'
import Table from '@/components/common/Table'
import Badge from '@/components/common/Badge'
import Button from '@/components/common/Button'
import Pagination from '@/components/common/Pagination'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import CaseForm from './CaseForm'
import { useRole } from '@/hooks/useRole'
import { usePDFExport } from '@/hooks/usePDFExport'
import { formatDate } from '@/utils/formatDate'
import {
  Plus, Search, Filter, FileDown,
  Pencil, Trash2, Eye
} from 'lucide-react'
import toast from 'react-hot-toast'

const STATUS_FILTERS = ['', 'ACTIVE', 'ON_HOLD', 'CLOSED']

export default function CasesPage() {
  const navigate = useNavigate()
  const { isLawyer } = useRole()
  const { exportPDF, loading: pdfLoading } = usePDFExport()

  // Filters + pagination state
  const [page,    setPage]    = useState(0)
  const [keyword, setKeyword] = useState('')
  const [status,  setStatus]  = useState('')
  const [search,  setSearch]  = useState('')  // controlled input

  // Modal state
  const [formOpen,    setFormOpen]    = useState(false)
  const [editCase,    setEditCase]    = useState(null)
  const [deleteId,    setDeleteId]    = useState(null)

  const { data, isLoading } = useGetCasesQuery({
    page, size: 10, status: status || undefined, keyword: keyword || undefined
  })

  const [deleteCase, { isLoading: deleting }] = useDeleteCaseMutation()

  // Debounced keyword search — fires on Enter
  function handleSearchKey(e) {
    if (e.key === 'Enter') {
      setKeyword(search)
      setPage(0)
    }
  }

  function handleSearchClear() {
    setSearch('')
    setKeyword('')
    setPage(0)
  }

  function openCreate() { setEditCase(null); setFormOpen(true) }
  function openEdit(c)  { setEditCase(c);    setFormOpen(true) }

  async function handleDelete() {
    try {
      await deleteCase(deleteId).unwrap()
      toast.success('Case deleted')
      setDeleteId(null)
    } catch {
      toast.error('Delete failed. Try again.')
    }
  }

  const columns = [
    {
      key: 'caseTitle',
      label: 'Case',
      render: row => (
        <div className="flex items-center gap-2">
          {/* Gold bookmark on each row */}
          <div className="w-0.5 h-5 bg-accent rounded-full shrink-0" />
          <div>
            <p className="font-semibold text-ink text-sm">{row.caseTitle}</p>
            {row.caseNumber && (
              <p className="font-mono text-xs text-text-muted">
                {row.caseNumber}
              </p>
            )}
          </div>
        </div>
      )
    },
    {
      key: 'caseType',
      label: 'Type',
      render: row => <Badge value={row.caseType} />
    },
    {
      key: 'caseStatus',
      label: 'Status',
      render: row => <Badge value={row.caseStatus} />
    },
    {
      key: 'courtName',
      label: 'Court',
      render: row => (
        <span className="text-sm text-text-muted">
          {row.courtName ?? '—'}
        </span>
      )
    },
    {
      key: 'createdAt',
      label: 'Filed',
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
            onClick={() => navigate(`/cases/${row.id}`)}
            className="p-1.5 rounded hover:bg-surface-alt text-text-muted
                        hover:text-ink transition-colors"
            title="View case"
          >
            <Eye size={14} />
          </button>

          {isLawyer && (
            <>
              <button
                onClick={() => openEdit(row)}
                className="p-1.5 rounded hover:bg-surface-alt text-text-muted
                            hover:text-ink transition-colors"
                title="Edit case"
              >
                <Pencil size={14} />
              </button>

              <button
                onClick={() =>
                  exportPDF('CASE_SUMMARY', row.id)
                }
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
                title="Delete case"
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

      {/* Page header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="font-display text-xl font-semibold text-ink">
            Cases
          </h2>
          <p className="text-sm text-text-muted font-body mt-0.5">
            {data?.totalElements ?? 0} total cases
          </p>
        </div>
        {isLawyer && (
          <Button onClick={openCreate}>
            <Plus size={14} /> New case
          </Button>
        )}
      </div>

      {/* Filters row */}
      <div className="flex items-center gap-3 flex-wrap">

        {/* Search */}
        <div className="relative flex-1 min-w-48 max-w-72">
          <Search size={13}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            onKeyDown={handleSearchKey}
            placeholder="Search by title — press Enter"
            className="w-full h-9 pl-8 pr-3 text-sm font-body border
                        border-border rounded bg-white focus:outline-none
                        focus:ring-2 focus:ring-accent/30 focus:border-accent"
          />
          {search && (
            <button
              onClick={handleSearchClear}
              className="absolute right-2.5 top-1/2 -translate-y-1/2
                          text-text-muted hover:text-ink text-xs"
            >
              ✕
            </button>
          )}
        </div>

        {/* Status filter tabs */}
        <div className="flex items-center gap-1 border border-border rounded
                         bg-surface-alt p-0.5">
          <Filter size={12} className="text-text-muted ml-2" />
          {STATUS_FILTERS.map(s => (
            <button
              key={s}
              onClick={() => { setStatus(s); setPage(0) }}
              className={`
                px-3 py-1 text-xs font-semibold font-body rounded
                transition-all duration-150
                ${status === s
                  ? 'bg-ink text-white'
                  : 'text-text-muted hover:text-text'
                }
              `}
            >
              {s === '' ? 'All' : s.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <Table
        columns={columns}
        data={data?.content}
        loading={isLoading}
        emptyTitle="No cases found"
      />

      {/* Pagination */}
      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 0}
        onPageChange={setPage}
      />

      {/* Create / Edit modal */}
      <CaseForm
        open={formOpen}
        onClose={() => setFormOpen(false)}
        editData={editCase}
      />

      {/* Delete confirm */}
      <ConfirmDialog
        open={!!deleteId}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDelete}
        loading={deleting}
        title="Delete case"
        description="This will permanently delete the case and all its hearings and clients. This cannot be undone."
        confirmLabel="Delete case"
      />

    </div>
  )
}