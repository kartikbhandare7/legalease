import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import {
  useCreateCaseMutation,
  useUpdateCaseMutation
} from './casesApi'
import Modal from '@/components/common/Modal'
import Input from '@/components/common/Input'
import Button from '@/components/common/Button'
import toast from 'react-hot-toast'

const schema = z.object({
  caseTitle:   z.string().min(1, 'Case title is required'),
  caseType:    z.string().min(1, 'Case type is required'),
  caseNumber:  z.string().optional(),
  courtName:   z.string().optional(),
  notes:       z.string().optional(),
})

const CASE_TYPES = [
  'CRIMINAL', 'CIVIL', 'FAMILY',
  'PROPERTY', 'CORPORATE', 'LABOUR', 'OTHER'
]

export default function CaseForm({ open, onClose, editData }) {
  const isEdit = !!editData

  const [createCase, { isLoading: creating }] = useCreateCaseMutation()
  const [updateCase, { isLoading: updating }] = useUpdateCaseMutation()
  const isLoading = creating || updating

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(schema)
  })

  // Prefill form when editing
  useEffect(() => {
    if (open) {
      reset(isEdit ? {
        caseTitle:  editData.caseTitle,
        caseType:   editData.caseType,
        caseNumber: editData.caseNumber ?? '',
        courtName:  editData.courtName  ?? '',
        notes:      editData.notes      ?? '',
      } : {
        caseTitle: '', caseType: 'CRIMINAL',
        caseNumber: '', courtName: '', notes: ''
      })
    }
  }, [open, editData])

  async function onSubmit(data) {
    try {
      if (isEdit) {
        await updateCase({ id: editData.id, ...data }).unwrap()
        toast.success('Case updated')
      } else {
        await createCase(data).unwrap()
        toast.success('Case created')
      }
      onClose()
    } catch (err) {
      toast.error(err?.data?.error ?? 'Something went wrong')
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={isEdit ? 'Edit case' : 'New case'}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

        <Input
          label="Case title"
          placeholder="e.g. Sharma vs State of Maharashtra"
          error={errors.caseTitle?.message}
          {...register('caseTitle')}
        />

        {/* Case type select */}
        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-text-muted
                             uppercase tracking-wide font-body">
            Case type
          </label>
          <select
            className="w-full h-10 px-3 text-sm font-body text-text
                        bg-white border border-border rounded
                        focus:outline-none focus:ring-2 focus:ring-accent/30
                        focus:border-accent"
            {...register('caseType')}
          >
            {CASE_TYPES.map(t => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
          {errors.caseType && (
            <p className="text-xs text-danger">{errors.caseType.message}</p>
          )}
        </div>

        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Case number"
            placeholder="e.g. CRI/001/2024"
            error={errors.caseNumber?.message}
            {...register('caseNumber')}
          />
          <Input
            label="Court name"
            placeholder="e.g. Mumbai High Court"
            error={errors.courtName?.message}
            {...register('courtName')}
          />
        </div>

        {/* Notes textarea */}
        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-text-muted
                             uppercase tracking-wide font-body">
            Notes <span className="text-text-muted/50 normal-case
                                    tracking-normal">(optional)</span>
          </label>
          <textarea
            rows={3}
            placeholder="Any initial notes about this case..."
            className="w-full text-sm font-body text-text bg-white border
                        border-border rounded px-3 py-2.5 resize-none
                        focus:outline-none focus:ring-2 focus:ring-accent/30
                        focus:border-accent placeholder:text-text-muted/50"
            {...register('notes')}
          />
        </div>

        {/* Form actions */}
        <div className="flex justify-end gap-3 pt-2 border-t border-border">
          <Button variant="secondary" type="button" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={isLoading}>
            {isEdit ? 'Save changes' : 'Create case'}
          </Button>
        </div>

      </form>
    </Modal>
  )
}