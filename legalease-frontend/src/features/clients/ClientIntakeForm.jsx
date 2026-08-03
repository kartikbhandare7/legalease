import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useCreateClientMutation } from './clientsApi'
import Modal from '@/components/common/Modal'
import Input from '@/components/common/Input'
import Button from '@/components/common/Button'
import AILogInput from '@/components/ai/AILogInput'
import toast from 'react-hot-toast'

const schema = z.object({
  clientName:      z.string().min(1, 'Client name is required'),
  phone:           z.string().optional(),
  email:           z.string().email().optional().or(z.literal('')),
  opposingParty:   z.string().optional(),
  caseBackground:  z.string().optional(),
})

export default function ClientIntakeForm({ open, onClose, caseId }) {
  const [createClient, { isLoading }] = useCreateClientMutation()

  const { register, handleSubmit, reset, setValue,
          formState: { errors } } = useForm({
    resolver: zodResolver(schema)
  })

  useEffect(() => {
    if (open) reset()
  }, [open])

  // Called by AILogInput after successful parse
  // AI fields map directly to form field names
  function handleAIParsed(fields, rawNote) {
    if (fields.clientName)     setValue('clientName',     fields.clientName)
    if (fields.opposingParty)  setValue('opposingParty',  fields.opposingParty)
    if (fields.caseBackground) setValue('caseBackground', fields.caseBackground)
    if (fields.phone)          setValue('phone',          fields.phone)
    if (fields.email)          setValue('email',          fields.email)

    // Store raw note so backend saves audit trail
    setValue('rawIntakeNote', rawNote)
    setValue('aiAssisted', true)
  }

  async function onSubmit(data) {
    try {
      await createClient({
        ...data,
        caseId,
        aiAssisted:    data.aiAssisted    ?? false,
        rawIntakeNote: data.rawIntakeNote ?? null,
      }).unwrap()

      toast.success('Client added to case')
      onClose()
    } catch (err) {
  console.error('Client creation failed:', err)  // add this line
  toast.error(err?.data?.error ?? err?.data?.message ?? 'Failed to add client')
}
  }

  return (
    <Modal open={open} onClose={onClose} title="Add client" width="max-w-xl">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

        {/* AI Log Input — sits above the form */}
        <AILogInput parseType="INTAKE" onParsed={handleAIParsed} />

        <Input
          label="Client name"
          placeholder="Full name of client"
          error={errors.clientName?.message}
          {...register('clientName')}
        />

        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Phone"
            placeholder="9876543210"
            error={errors.phone?.message}
            {...register('phone')}
          />
          <Input
            label="Email"
            type="email"
            placeholder="client@example.com"
            error={errors.email?.message}
            {...register('email')}
          />
        </div>

        <Input
          label="Opposing party"
          placeholder="Name of opposing party"
          error={errors.opposingParty?.message}
          {...register('opposingParty')}
        />

        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-text-muted
                             uppercase tracking-wide font-body">
            Case background
          </label>
          <textarea
            rows={4}
            placeholder="Brief background of the case..."
            className="w-full text-sm font-body text-text bg-white border
                        border-border rounded px-3 py-2.5 resize-none
                        focus:outline-none focus:ring-2 focus:ring-accent/30
                        focus:border-accent placeholder:text-text-muted/50"
            {...register('caseBackground')}
          />
        </div>

        <div className="flex justify-end gap-3 pt-2 border-t border-border">
          <Button variant="secondary" type="button" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={isLoading}>
            Add client
          </Button>
        </div>

      </form>
    </Modal>
  )
}