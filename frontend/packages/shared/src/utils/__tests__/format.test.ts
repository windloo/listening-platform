import { describe, it, expect } from 'vitest'
import { formatDateTime, formatDuration } from '../format'

describe('format', () => {
  it('formatDateTime parses ISO', () => {
    const s = formatDateTime('2026-07-08T10:30:00')
    expect(s).toContain('2026')
  })
  it('formatDateTime null -> "-"', () => { expect(formatDateTime(null)).toBe('-') })
  it('formatDuration 65 -> 1:05', () => { expect(formatDuration(65)).toBe('1:05') })
  it('formatDuration null -> "-"', () => { expect(formatDuration(null as any)).toBe('-') })
})
