// Safe JSON.parse wrapper — AI sometimes returns slightly malformed JSON
export function parseAIResponse(raw) {
  if (!raw || raw === '{}') return {}
  try {
    return JSON.parse(raw)
  } catch {
    // Try extracting the JSON object if there's extra text
    const match = raw.match(/\{[\s\S]*\}/)
    if (match) {
      try { return JSON.parse(match[0]) }
      catch { return {} }
    }
    return {}
  }
}