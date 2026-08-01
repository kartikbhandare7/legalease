import { useState } from 'react'
import {
  useGetPendingLawyersQuery,
  useUpdateApprovalMutation
} from './adminApi'
import Spinner from '@/components/common/Spinner'
import Button from '@/components/common/Button'
import Pagination from '@/components/common/Pagination'
import Modal from '@/components/common/Modal'
import { formatDate } from '@/utils/formatDate'
import { ShieldCheck, ShieldX, FileText, Clock } from 'lucide-react'
import toast from 'react-hot-toast'

export default function PendingLawyersPage() {
  const [page,           setPage]           = useState(0)
  const [rejectModal,    setRejectModal]    = useState(null)
  const [rejectReason,   setRejectReason]   = useState('')

  const { data, isLoading } = useGetPendingLawyersQuery({ page, size: 10 })
  const [updateApproval, { isLoading: updating }] = useUpdateApprovalMutation()

  async function handleApprove(userId) {
    try {
      await updateApproval({ userId, status: 'ACTIVE' }).unwrap()
      toast.success('Lawyer approved — account is now active')
    } catch {
      toast.error('Approval failed')
    }
  }

  async function handleReject() {
    try {
      await updateApproval({
        userId:          rejectModal,
        status:          'REJECTED',
        rejectionReason: rejectReason
      }).unwrap()
      toast.success('Account rejected')
      setRejectModal(null)
      setRejectReason('')
    } catch {
      toast.error('Rejection failed')
    }
  }

  if (isLoading) return <Spinner size={28} className="py-24" />

  return (
    <div className="max-w-5xl space-y-6">

      <div className="border-b border-border pb-5">
        <div className="flex items-center gap-2 mb-1">
          <Clock size={16} className="text-accent" />
          <h2 className="font-display text-xl font-semibold text-ink">
            Pending approvals
          </h2>
        </div>
        <p className="text-sm text-text-muted font-body">
          Review enrollment certificates and approve or reject lawyer accounts
        </p>
      </div>

      {data?.content?.length === 0
        ? (
          <div className="text-center py-16">
            <ShieldCheck size={36} className="text-success mx-auto mb-3" />
            <p className="font-display text-base font-semibold text-ink">
              All caught up
            </p>
            <p className="text-sm text-text-muted font-body mt-1">
              No pending lawyer accounts to review
            </p>
          </div>
        )
        : (
          <div className="space-y-4">
            {data?.content?.map(lawyer => (
              <div key={lawyer.id}
                className="bg-white border border-border rounded-lg p-5">

                <div className="flex items-start justify-between gap-4">
                  {/* Lawyer info */}
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-full bg-accent/10 flex
                                     items-center justify-center text-accent
                                     font-semibold font-mono shrink-0">
                      {lawyer.fullName.charAt(0)}
                    </div>
                    <div>
                      <p className="font-semibold text-ink font-body">
                        {lawyer.fullName}
                      </p>
                      <p className="text-sm text-text-muted font-body">
                        {lawyer.email}
                      </p>
                      <p className="text-xs text-text-muted font-body mt-1">
                        Registered {formatDate(lawyer.createdAt)}
                      </p>
                    </div>
                  </div>

                  {/* Action buttons */}
                  <div className="flex items-center gap-2 shrink-0">
                    <Button
                      size="sm"
                      variant="secondary"
                      onClick={() => setRejectModal(lawyer.id)}
                    >
                      <ShieldX size={13} /> Reject
                    </Button>
                    <Button
                      size="sm"
                      onClick={() => handleApprove(lawyer.id)}
                      loading={updating}
                    >
                      <ShieldCheck size={13} /> Approve
                    </Button>
                  </div>
                </div>

                {/* Bar council info */}
                <div className="mt-4 pt-4 border-t border-border
                                 grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-xs font-semibold text-text-muted
                                   uppercase tracking-wide font-body mb-1">
                      Bar Council number
                    </p>
                    <p className="font-mono text-sm text-ink">
                      {lawyer.barCouncilNumber ?? '—'}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs font-semibold text-text-muted
                                   uppercase tracking-wide font-body mb-1">
                      Certificate
                    </p>
                    {lawyer.certificatePath
                      ? (
                        <a
                          href={`${import.meta.env.VITE_API_BASE_URL}/uploads/${
                            lawyer.certificatePath.split('/').pop()
                          }`}
                          target="_blank"
                          rel="noreferrer"
                          className="flex items-center gap-1.5 text-sm
                                      text-accent hover:underline font-body"
                        >
                          <FileText size={13} /> View certificate
                        </a>
                      )
                      : <span className="text-sm text-text-muted">
                          Not uploaded
                        </span>
                    }
                  </div>
                </div>
              </div>
            ))}
          </div>
        )
      }

      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 0}
        onPageChange={setPage}
      />

      {/* Reject reason modal */}
      <Modal
        open={!!rejectModal}
        onClose={() => { setRejectModal(null); setRejectReason('') }}
        title="Reject account"
        width="max-w-md"
      >
        <div className="space-y-4">
          <p className="text-sm text-text-muted font-body">
            Optionally provide a reason — this helps the lawyer understand
            what went wrong.
          </p>
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-text-muted
                               uppercase tracking-wide font-body">
              Reason (optional)
            </label>
            <textarea
              rows={3}
              value={rejectReason}
              onChange={e => setRejectReason(e.target.value)}
              placeholder="e.g. Certificate appears invalid or unclear..."
              className="w-full text-sm font-body text-text bg-white border
                          border-border rounded px-3 py-2.5 resize-none
                          focus:outline-none focus:ring-2 focus:ring-accent/30"
            />
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-border">
            <Button
              variant="secondary"
              onClick={() => { setRejectModal(null); setRejectReason('') }}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              onClick={handleReject}
              loading={updating}
            >
              <ShieldX size={13} /> Reject account
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}