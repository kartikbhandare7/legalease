import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useLogHearingMutation } from './hearingsApi'
import Modal from '@/components/common/Modal'
import Input from '@/components/common/Input'
import Button from '@/components/common/Button'
import AILogInput from '@/components/ai/AILogInput'
import toast from 'react-hot-toast'

const schema = z.object({
  hearingDate: z.string().min(1, 'Hearing date is required'),
  outcome:     z.string().optional(),
  nextDate:    z.string().optional(),
  actionItems: z.string().optional(),
})

export default function HearingLogForm({ open, onClose, caseId }) {
  const [logHearing, { isLoading }] = useLogHearingMutation()

  const { register, handleSubmit, reset, setValue,
          formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      hearingDate: new Date().toISOString().split('T')[0]
    }
  })

  useEffect(() => {
    if (open) {
      reset({
        hearingDate: new Date().toISOString().split('T')[0],
        outcome: '', nextDate: '', actionItems: ''
      })
    }
  }, [open])

  // AI fills hearing fields after parsing raw note
  function handleAIParsed(fields, rawNote) {
    if (fields.hearingDate) setValue('hearingDate', fields.hearingDate)
    if (fields.nextDate)    setValue('nextDate',    fields.nextDate)
    if (fields.outcome)     setValue('outcome',     fields.outcome)

    // actionItems from AI is an array — join to comma-separated string
    // stored as JSON string in backend
    if (fields.actionItems?.length) {
      setValue('actionItems', JSON.stringify(fields.actionItems))
    }

    setValue('rawNote',    rawNote)
    setValue('aiAssisted', true)
  }

  async function onSubmit(data) {
    try {
      await logHearing({
        ...data,
        caseId,
        aiAssisted: data.aiAssisted ?? false,
        rawNote:    data.rawNote    ?? null,
        // Convert empty strings to null for optional dates
        nextDate:   data.nextDate   || null,
      }).unwrap()

      toast.success('Hearing logged')
      onClose()
    } catch (err) {
      toast.error(err?.data?.error ?? 'Failed to log hearing')
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Log hearing" width="max-w-xl">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

        {/* AI Log Input */}
        <AILogInput parseType="HEARING" onParsed={handleAIParsed} />

        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Hearing date"
            type="date"
            error={errors.hearingDate?.message}
            {...register('hearingDate')}
          />
          <Input
            label="Next date"
            type="date"
            error={errors.nextDate?.message}
            {...register('nextDate')}
          />
        </div>

        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-text-muted
                             uppercase tracking-wide font-body">
            Outcome
          </label>
          <textarea
            rows={3}
            placeholder="What happened in court today..."
            className="w-full text-sm font-body text-text bg-white border
                        border-border rounded px-3 py-2.5 resize-none
                        focus:outline-none focus:ring-2 focus:ring-accent/30
                        focus:border-accent placeholder:text-text-muted/50"
            {...register('outcome')}
          />
        </div>

        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-text-muted
                             uppercase tracking-wide font-body">
            Action items
            <span className="text-text-muted/50 normal-case tracking-normal
                              ml-1">(JSON array or leave blank)</span>
          </label>
          <textarea
            rows={2}
            placeholder='["Submit FIR copy", "Notify client of next date"]'
            className="w-full text-sm font-mono text-text bg-white border
                        border-border rounded px-3 py-2.5 resize-none
                        focus:outline-none focus:ring-2 focus:ring-accent/30
                        focus:border-accent placeholder:text-text-muted/50"
            {...register('actionItems')}
          />
        </div>

        <div className="flex justify-end gap-3 pt-2 border-t border-border">
          <Button variant="secondary" type="button" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={isLoading}>
            Log hearing
          </Button>
        </div>

      </form>
    </Modal>
  )
}