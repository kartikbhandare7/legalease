import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  useGetCaseByIdQuery,
  useUpdateCaseStatusMutation,
  useDeleteCaseMutation
} from './casesApi'
import { useGetClientsByCaseQuery } from '@/features/clients/clientsApi'
import { useGetHearingsByCaseQuery } from '@/features/hearings/hearingsApi'
import Badge from '@/components/common/Badge'
import Button from '@/components/common/Button'
import Spinner from '@/components/common/Spinner'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import CaseForm from './CaseForm'
import ClientIntakeForm from '@/features/clients/ClientIntakeForm'
import HearingLogForm from '@/features/hearings/HearingLogForm'
import { useRole } from '@/hooks/useRole'
import { usePDFExport } from '@/hooks/usePDFExport'
import { formatDate } from '@/utils/formatDate'
import {
  ArrowLeft, Pencil, FileDown, Trash2,
  Users, CalendarDays, Sparkles, Plus
} from 'lucide-react'
import toast from 'react-hot-toast'

const STATUS_TRANSITIONS = {
  ACTIVE:  ['ON_HOLD', 'CLOSED'],
  ON_HOLD: ['ACTIVE', 'CLOSED'],
  CLOSED:  [],
}

export default function CaseDetailPage() {
  const { caseId } = useParams()
  const navigate   = useNavigate()
  const { isLawyer } = useRole()
  const { exportPDF, loading: pdfLoading } = usePDFExport()

  const [editOpen,         setEditOpen]         = useState(false)
  const [deleteOpen,       setDeleteOpen]       = useState(false)
  const [clientFormOpen,   setClientFormOpen]   = useState(false)
  const [hearingFormOpen,  setHearingFormOpen]  = useState(false)
  const [activeTab,        setActiveTab]        = useState('clients')

  const { data: legalCase, isLoading } = useGetCaseByIdQuery(caseId)
  const { data: clientsData }  = useGetClientsByCaseQuery({ caseId, size: 5 })
  const { data: hearingsData } = useGetHearingsByCaseQuery({ caseId, size: 5 })

  const [updateStatus] = useUpdateCaseStatusMutation()
  const [deleteCase, { isLoading: deleting }] = useDeleteCaseMutation()

  async function handleStatusChange(newStatus) {
    try {
      await updateStatus({
        id: caseId, caseStatus: newStatus
      }).unwrap()
      toast.success(`Case marked as ${newStatus.replace('_', ' ')}`)
    } catch (err) {
      toast.error(err?.data?.error ?? 'Status update failed')
    }
  }

  async function handleDelete() {
    try {
      await deleteCase(caseId).unwrap()
      toast.success('Case deleted')
      navigate('/cases')
    } catch {
      toast.error('Delete failed')
    }
  }

  if (isLoading) return <Spinner size={28} className="py-24" />
  if (!legalCase) return null

  const transitions = STATUS_TRANSITIONS[legalCase.caseStatus] ?? []

  return (
    <div className="max-w-5xl space-y-6">

      {/* Back + actions bar */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/cases')}
          className="flex items-center gap-1.5 text-sm text-text-muted
                      hover:text-ink font-body transition-colors"
        >
          <ArrowLeft size={14} /> Back to cases
        </button>

        <div className="flex items-center gap-2">
          {/* Status transition buttons */}
          {isLawyer && transitions.map(s => (
            <Button
              key={s}
              size="sm"
              variant="outline"
              onClick={() => handleStatusChange(s)}
            >
              Mark {s.replace('_', ' ')}
            </Button>
          ))}

          {isLawyer && (
            <>
              <Button
                size="sm"
                variant="ghost"
                onClick={() => setEditOpen(true)}
              >
                <Pencil size={13} /> Edit
              </Button>

              <Button
                size="sm"
                variant="ghost"
                onClick={() => exportPDF('CASE_SUMMARY', caseId)}
                loading={pdfLoading}
              >
                <FileDown size={13} /> PDF
              </Button>

              <Button
                size="sm"
                variant="ghost"
                className="hover:text-danger"
                onClick={() => setDeleteOpen(true)}
              >
                <Trash2 size={13} /> Delete
              </Button>
            </>
          )}
        </div>
      </div>

      {/* Case header card */}
      <div className="bg-white border border-border rounded-lg p-6">
        <div className="flex items-start gap-4">

          {/* Gold bookmark */}
          <div className="w-1 h-14 bg-accent rounded-full shrink-0 mt-0.5" />

          <div className="flex-1">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 className="font-display text-xl font-semibold text-ink">
                  {legalCase.caseTitle}
                </h2>
                {legalCase.caseNumber && (
                  <p className="font-mono text-sm text-text-muted mt-0.5">
                    {legalCase.caseNumber}
                  </p>
                )}
              </div>
              <div className="flex items-center gap-2 shrink-0">
                <Badge value={legalCase.caseType} />
                <Badge value={legalCase.caseStatus} />
              </div>
            </div>

            {/* Meta row */}
            <div className="flex flex-wrap gap-x-6 gap-y-1.5 mt-4">
              {legalCase.courtName && (
                <div>
                  <span className="text-xs text-text-muted font-body
                                    uppercase tracking-wide">Court</span>
                  <p className="text-sm font-semibold text-ink font-body">
                    {legalCase.courtName}
                  </p>
                </div>
              )}
              <div>
                <span className="text-xs text-text-muted font-body
                                  uppercase tracking-wide">Lawyer</span>
                <p className="text-sm font-semibold text-ink font-body">
                  {legalCase.lawyerName}
                </p>
              </div>
              <div>
                <span className="text-xs text-text-muted font-body
                                  uppercase tracking-wide">Filed on</span>
                <p className="text-sm font-semibold text-ink font-mono">
                  {formatDate(legalCase.createdAt)}
                </p>
              </div>
            </div>

            {legalCase.notes && (
              <p className="text-sm text-text-muted font-body mt-4
                             border-t border-border pt-4">
                {legalCase.notes}
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Tabs — Clients / Hearings */}
      <div className="flex items-center gap-0 border-b border-border">
        {[
          { key: 'clients',  label: 'Clients',  icon: Users,
            count: clientsData?.totalElements },
          { key: 'hearings', label: 'Hearings', icon: CalendarDays,
            count: hearingsData?.totalElements },
        ].map(tab => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`
              flex items-center gap-2 px-4 py-2.5 text-sm font-semibold
              font-body border-b-2 transition-all duration-150 -mb-px
              ${activeTab === tab.key
                ? 'border-accent text-accent'
                : 'border-transparent text-text-muted hover:text-ink'
              }
            `}
          >
            <tab.icon size={14} />
            {tab.label}
            {tab.count !== undefined && (
              <span className="font-mono text-xs bg-surface-alt
                                text-text-muted px-1.5 py-0.5 rounded">
                {tab.count}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Tab content */}
      {activeTab === 'clients' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-display text-sm font-semibold text-ink">
              Clients on this case
            </h3>
            {isLawyer && (
              <Button size="sm" onClick={() => setClientFormOpen(true)}>
                <Plus size={13} /> Add client
              </Button>
            )}
          </div>

          {clientsData?.content?.length === 0
            ? (
              <div className="text-center py-10 text-sm text-text-muted
                               font-body">
                No clients added yet.
              </div>
            )
            : clientsData?.content?.map(c => (
              <div
                key={c.id}
                onClick={() => navigate(`/clients/${c.id}`)}
                className="bg-white border border-border rounded-lg p-4
                            hover:shadow-sm cursor-pointer transition-shadow
                            flex items-center gap-4"
              >
                <div className="w-9 h-9 rounded-full bg-accent/10 flex
                                 items-center justify-center text-accent
                                 text-sm font-semibold font-mono shrink-0">
                  {c.clientName.charAt(0)}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="font-semibold text-sm text-ink font-body">
                      {c.clientName}
                    </p>
                    {c.aiAssisted && (
                      <span className="ai-badge flex items-center gap-1">
                        <Sparkles size={9} /> AI
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-text-muted font-body mt-0.5">
                    vs {c.opposingParty ?? 'Unknown opposing party'}
                  </p>
                </div>
                <p className="text-xs font-mono text-text-muted shrink-0">
                  {formatDate(c.createdAt)}
                </p>
              </div>
            ))
          }
        </div>
      )}

      {activeTab === 'hearings' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-display text-sm font-semibold text-ink">
              Hearing history
            </h3>
            <Button size="sm" onClick={() => setHearingFormOpen(true)}>
              <Plus size={13} /> Log hearing
            </Button>
          </div>

          {hearingsData?.content?.length === 0
            ? (
              <div className="text-center py-10 text-sm text-text-muted
                               font-body">
                No hearings logged yet.
              </div>
            )
            : hearingsData?.content?.map(h => (
              <div
                key={h.id}
                className="bg-white border border-border rounded-lg p-4"
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="font-mono text-sm font-semibold
                                        text-ink">
                        {formatDate(h.hearingDate)}
                      </span>
                      {h.aiAssisted && (
                        <span className="ai-badge flex items-center gap-1">
                          <Sparkles size={9} /> AI
                        </span>
                      )}
                    </div>
                    <p className="text-sm text-text font-body">
                      {h.outcome ?? 'No outcome recorded'}
                    </p>
                    {h.nextDate && (
                      <p className="text-xs text-text-muted font-body mt-2">
                        Next date:{' '}
                        <span className="font-mono font-semibold text-accent">
                          {formatDate(h.nextDate)}
                        </span>
                      </p>
                    )}
                  </div>

                  {isLawyer && (
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => exportPDF('HEARING_LOG', h.id)}
                    >
                      <FileDown size={13} />
                    </Button>
                  )}
                </div>

                {/* Action items */}
                {h.actionItems && (
                  <div className="mt-3 pt-3 border-t border-border">
                    <p className="text-xs font-semibold text-text-muted
                                   uppercase tracking-wide mb-2">
                      Action items
                    </p>
                    <div className="flex flex-wrap gap-2">
                      {JSON.parse(h.actionItems ?? '[]').map((item, i) => (
                        <span
                          key={i}
                          className="text-xs bg-accent-soft text-ink
                                      font-body px-2 py-1 rounded border
                                      border-accent/20"
                        >
                          {item}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))
          }
        </div>
      )}

      {/* Modals */}
      <CaseForm
        open={editOpen}
        onClose={() => setEditOpen(false)}
        editData={legalCase}
      />

      <ClientIntakeForm
        open={clientFormOpen}
        onClose={() => setClientFormOpen(false)}
        caseId={caseId}
      />

      <HearingLogForm
        open={hearingFormOpen}
        onClose={() => setHearingFormOpen(false)}
        caseId={caseId}
      />

      <ConfirmDialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        onConfirm={handleDelete}
        loading={deleting}
        title="Delete case"
        description="This permanently deletes the case and all associated clients and hearings."
        confirmLabel="Delete"
      />

    </div>
  )
}