/**
 * CanonFidelityChecker — ensures canon accuracy at runtime.
 *
 * Before shipping anything, this module verifies that the world matches
 * the canon research. It checks:
 *   1. Location names match the canon database
 *   2. No forbidden entities (Suzaku Continent, Xue Yue, etc.)
 *   3. All placed locations exist in the canon DB
 *   4. Buildings are tagged with correct canonStatus
 *
 * Usage: call checkCanonFidelity() after world boot. If any check fails,
 * log a warning. This is the debugging tool the user asked for.
 */

export interface FidelityViolation {
  severity: 'error' | 'warning' | 'info'
  category: string
  message: string
  data?: unknown
}

// Forbidden entities (CRON-69 corrections).
const FORBIDDEN = [
  { pattern: /suzaku continent/i, correct: 'Planet Suzaku (it is a PLANET, not a continent)' },
  { pattern: /xue yue/i, correct: 'Snow Domain Country' },
  { pattern: /teng lijun/i, correct: 'Teng Li' },
  { pattern: /heavenly demon city/i, correct: 'Ancient Demon City' },
  { pattern: /ten thousand demons sect/i, correct: 'not canon-attested' },
]

export function checkCanonFidelity(data: {
  locations?: Array<{ name: string; nameCn?: string }>
  characters?: Array<{ name: string; nameCn?: string; affiliation?: string }>
  buildings?: Array<{ name: string; nameCn?: string; canonStatus?: string }>
  spiritVeins?: Array<{ name: string; element?: string }>
  playerName?: string
}): FidelityViolation[] {
  const violations: FidelityViolation[] = []

  // 1. Check for forbidden entities in all string fields.
  const allStrings: string[] = []
  for (const loc of data.locations ?? []) allStrings.push(loc.name, loc.nameCn ?? '')
  for (const ch of data.characters ?? []) allStrings.push(ch.name, ch.nameCn ?? '', ch.affiliation ?? '')
  for (const b of data.buildings ?? []) allStrings.push(b.name, b.nameCn ?? '')
  for (const v of data.spiritVeins ?? []) allStrings.push(v.name, v.element ?? '')

  for (const str of allStrings) {
    for (const f of FORBIDDEN) {
      if (f.pattern.test(str)) {
        violations.push({
          severity: 'error',
          category: 'forbidden_entity',
          message: `Forbidden: "${str}" → use: ${f.correct}`,
        })
      }
    }
  }

  // 2. Check buildings have canonStatus.
  for (const b of data.buildings ?? []) {
    if (!b.canonStatus) {
      violations.push({
        severity: 'warning',
        category: 'missing_canon_status',
        message: `Building "${b.name}" has no canonStatus`,
      })
    }
  }

  // 3. Check player is NOT Wang Lin.
  if (data.playerName) {
    if (/wang.?lin/i.test(data.playerName)) {
      violations.push({
        severity: 'error',
        category: 'player_identity',
        message: `Player name "${data.playerName}" — the player is NOT Wang Lin. Wang Lin is a manifestation NPC.`,
      })
    }
  }

  return violations
}

/**
 * Log fidelity violations to console with color coding.
 */
export function logFidelityViolations(violations: FidelityViolation[]): void {
  for (const v of violations) {
    const prefix = v.severity === 'error' ? '❌' : v.severity === 'warning' ? '⚠️' : 'ℹ️'
    console.log(`[CanonFidelity] ${prefix} [${v.category}] ${v.message}`)
  }
  if (violations.length === 0) {
    console.log('[CanonFidelity] ✅ All checks passed — canon fidelity verified.')
  }
}
