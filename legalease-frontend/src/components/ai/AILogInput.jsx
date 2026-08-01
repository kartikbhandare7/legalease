import { useState } from 'react'
import { Sparkles, ChevronDown, ChevronUp } from 'lucide-react'
import Button from '@/components/common/Button'
import { useParseAIMutation } from '@/features/ai/aiApi'
import { parseAIResponse } from '@/utils/parseAIResponse'
import toast from 'react-hot-toast'

export default function AILogInput({ parseType, onParsed }) {
  const [expanded, setExpanded] = useState(false)
  const [rawText,  setRawText]  = useState('')
  const [parseAI,  { isLoading }] = useParseAIMutation()

  async function handleParse() {
    if (!rawText.trim()) {
      toast.error('Please type a note first')
      return
    }

    try {
      const result = await parseAI({ rawText, parseType }).unwrap()

      if (!result.success) {
        toast.error(result.errorMessage || 'AI parsing failed. Fill manually.')
        return
      }

      const fields = parseAIResponse(result.parsedFields)
      onParsed(fields, rawText)   // parent form receives parsed fields
      toast.success('Form filled by AI — review before saving')
      setExpanded(false)

    } catch {
      toast.error('AI service unavailable. Please fill manually.')
    }
  }

  return (
    <div className="border border-accent/30 rounded bg-accent-soft/30 mb-6">

      {/* Toggle header */}
      <button
        type="button"
        onClick={() => setExpanded(v => !v)}
        className="flex items-center justify-between w-full px-4 py-3
                    text-left group"
      >
        <div className="flex items-center gap-2">
          <Sparkles size={14} className="text-accent" />
          <span className="text-sm font-semibold text-ink font-body">
            Fill with AI Log
          </span>
          <span className="ai-badge">Beta</span>
        </div>
        {expanded
          ? <ChevronUp size={14} className="text-text-muted" />
          : <ChevronDown size={14} className="text-text-muted" />
        }
      </button>

      {/* Expandable body */}
      {expanded && (
        <div className="px-4 pb-4 border-t border-accent/20 pt-3 space-y-3">
          <p className="text-xs text-text-muted font-body">
            Type a rough note — AI will extract the fields and fill the form below.
            Always review before saving.
          </p>

          <textarea
            rows={4}
            value={rawText}
            onChange={e => setRawText(e.target.value)}
            placeholder={
              parseType === 'INTAKE'
                ? 'e.g. John Doe, theft case, opposing Ravi Kumar, filed at Mumbai Sessions Court...'
                : 'e.g. Sharma vs Gupta hearing today, judge postponed to Nov 5, need to submit affidavit...'
            }
            className="w-full text-sm font-body text-text bg-white border
                        border-accent/30 rounded px-3 py-2.5 resize-none
                        placeholder:text-text-muted/50 focus:outline-none
                        focus:ring-2 focus:ring-accent/30 focus:border-accent"
          />

          <div className="flex items-center justify-between">
            <p className="text-xs text-text-muted font-body">
              {rawText.length} characters
            </p>
            <Button
              size="sm"
              onClick={handleParse}
              loading={isLoading}
              disabled={!rawText.trim()}
            >
              <Sparkles size={12} />
              {isLoading ? 'Parsing...' : 'Parse & Fill Form'}
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}