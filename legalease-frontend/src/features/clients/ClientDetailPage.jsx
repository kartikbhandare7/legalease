import { useParams, useNavigate } from 'react-router-dom'
import {
  useGetClientByIdQuery,
  useUpdateClientMutation,
  useDeleteClientMutation
} from './clientsApi'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Badge from '@/components/common/Badge'
import Button from '@/components/common/Button'
import Input from '@/components/common/Input'
import Spinner from '@/components/common/Spinner'
import Modal from '@/components/common/Modal'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import { useRole } from '@/hooks/useRole'
import { usePDFExport } from '@/hooks/usePDFExport'
import { formatDate } from '@/utils/formatDate'
import {
  ArrowLeft, Pencil, FileDown,
  Trash2, Sparkles, Phone, Mail, User
} from 'lucide-react'
import toast from 'react-hot-toast'

const schema = z.object({
  clientName:     z.string().min(1, 'Name is required'),
  phone:          z.string().optional(),
  email:          z.string().email().optional().or(z.literal('')),
  opposingParty:  z.string().optional(),
  caseBackground: z.string().optional(),
})

export default function ClientDetailPage() {
  const { clientId } = useParams()
  const navigate     = useNavigate()
  const { isLawyer } = useRole()
  const { exportPDF, loading: pdfLoading } = usePDFExport()

  const [editOpen,   setEditOpen]   = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)

  const { data: client, isLoading } = useGetClientByIdQuery(clientId)
  const [updateClient, { isLoading: updating }] = useUpdateClientMutation()
  const [deleteClient, { isLoading: deleting }] = useDeleteClientMutation()

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(schema)
  })

  function openEdit() {
    reset({
      clientName:     client.clientName,
      phone:          client.phone          ?? '',
      email:          client.email          ?? '',
      opposingParty:  client.opposingParty  ?? '',
      caseBackground: client.caseBackground ?? '',
    })
    setEditOpen(true)
  }

  async function onUpdate(data) {
    try {
      await updateClient({ id: clientId, ...data }).unwrap()
      toast.success('Client updated')
      setEditOpen(false)
    } catch {
      toast.error('Update failed')
    }
  }

  async function handleDelete() {
    try {
      await deleteClient(clientId).unwrap()
      toast.success('Client removed')
      navigate('/clients')
    } catch {
      toast.error('Delete failed')
    }
  }

  if (isLoading) return <Spinner size={28} className="py-24" />
  if (!client)   return null

  return (
    <div className="max-w-3xl space-y-6">

      {/* Back + actions */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/clients')}
          className="flex items-center gap-1.5 text-sm text-text-muted
                      hover:text-ink font-body transition-colors"
        >
          <ArrowLeft size={14} /> Back to clients
        </button>

        {isLawyer && (
          <div className="flex items-center gap-2">
            <Button size="sm" variant="ghost" onClick={openEdit}>
              <Pencil size={13} /> Edit
            </Button>
            <Button
              size="sm" variant="ghost"
              onClick={() => exportPDF('CLIENT_INTAKE', clientId)}
              loading={pdfLoading}
            >
              <FileDown size={13} /> PDF
            </Button>
            <Button
              size="sm" variant="ghost"
              className="hover:text-danger"
              onClick={() => setDeleteOpen(true)}
            >
              <Trash2 size={13} /> Delete
            </Button>
          </div>
        )}
      </div>

      {/* Client card */}
      <div className="bg-white border border-border rounded-lg p-6">
        <div className="flex items-start gap-4">

          {/* Avatar */}
          <div className="w-14 h-14 rounded-full bg-accent/10 flex items-center
                           justify-center text-accent text-xl font-semibold
                           font-mono shrink-0">
            {client.clientName.charAt(0)}
          </div>

          <div className="flex-1">
            <div className="flex items-center gap-3 mb-1">
              <h2 className="font-display text-xl font-semibold text-ink">
                {client.clientName}
              </h2>
              {client.aiAssisted && (
                <span className="ai-badge flex items-center gap-1">
                  <Sparkles size={9} /> AI assisted
                </span>
              )}
            </div>

            {/* Contact info */}
            <div className="flex flex-wrap gap-4 mt-3">
              {client.phone && (
                <div className="flex items-center gap-1.5 text-sm
                                 text-text-muted font-body">
                  <Phone size={12} />
                  <span>{client.phone}</span>
                </div>
              )}
              {client.email && (
                <div className="flex items-center gap-1.5 text-sm
                                 text-text-muted font-body">
                  <Mail size={12} />
                  <span>{client.email}</span>
                </div>
              )}
              <div className="flex items-center gap-1.5 text-sm
                               text-text-muted font-body">
                <User size={12} />
                <span>Added {formatDate(client.createdAt)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Details grid */}
        <div className="grid grid-cols-2 gap-5 mt-6 pt-5 border-t border-border">
          <div>
            <p className="text-xs font-semibold text-text-muted uppercase
                           tracking-wide font-body mb-1">
              Opposing party
            </p>
            <p className="text-sm font-body text-ink">
              {client.opposingParty ?? '—'}
            </p>
          </div>
          <div>
            <p className="text-xs font-semibold text-text-muted uppercase
                           tracking-wide font-body mb-1">
              Case
            </p>
            <button
              onClick={() => navigate(`/cases/${client.caseId}`)}
              className="text-sm text-accent hover:underline font-body"
            >
              {client.caseTitle}
            </button>
          </div>
        </div>

        {client.caseBackground && (
          <div className="mt-5 pt-5 border-t border-border">
            <p className="text-xs font-semibold text-text-muted uppercase
                           tracking-wide font-body mb-2">
              Case background
            </p>
            <p className="text-sm font-body text-text leading-relaxed">
              {client.caseBackground}
            </p>
          </div>
        )}

        {/* Raw note — shown only if AI was used */}
        {client.aiAssisted && client.rawIntakeNote && (
          <div className="mt-5 pt-5 border-t border-border">
            <p className="text-xs font-semibold text-text-muted uppercase
                           tracking-wide font-body mb-2">
              Original AI input note
            </p>
            <p className="text-sm font-body text-text-muted italic
                           leading-relaxed bg-surface-alt rounded p-3">
              "{client.rawIntakeNote}"
            </p>
          </div>
        )}
      </div>

      {/* Edit modal */}
      <Modal open={editOpen} onClose={() => setEditOpen(false)} title="Edit client">
        <form onSubmit={handleSubmit(onUpdate)} className="space-y-4">
          <Input
            label="Client name"
            error={errors.clientName?.message}
            {...register('clientName')}
          />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Phone" {...register('phone')} />
            <Input label="Email" type="email" {...register('email')} />
          </div>
          <Input
            label="Opposing party"
            {...register('opposingParty')}
          />
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-text-muted
                               uppercase tracking-wide font-body">
              Case background
            </label>
            <textarea
              rows={4}
              className="w-full text-sm font-body text-text bg-white border
                          border-border rounded px-3 py-2.5 resize-none
                          focus:outline-none focus:ring-2 focus:ring-accent/30"
              {...register('caseBackground')}
            />
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-border">
            <Button variant="secondary" type="button"
              onClick={() => setEditOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={updating}>
              Save changes
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        onConfirm={handleDelete}
        loading={deleting}
        title="Remove client"
        description="This permanently removes the client and their intake record."
        confirmLabel="Remove"
      />
    </div>
  )
}