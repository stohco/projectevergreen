'use client'

import { useWorldStateQuery, qiPercent, qiCritical, getRealm } from './WorldStateQuery'

/**
 * QiBar — the bottom-center cultivation qi bar.
 *
 * 400px wide × 12px tall. Fills with a jade→gold gradient that shifts toward
 * gold at higher realms. Pulses when regenerating. Flashes red + shakes when
 * qi drops below 5%.
 *
 * The shake is triggered purely by CSS — the `qi-bar-shake` class is applied
 * when `critical` is true, and the keyframe runs once (no `infinite`).
 * Re-trigger on false→true transitions happens automatically because the
 * class is removed and re-added.
 */
export default function QiBar() {
  const player = useWorldStateQuery((s) => s.player)
  const pct = qiPercent(player)
  const critical = qiCritical(player)
  const realm = getRealm(player.realmId)
  // High realms (Core Formation +) shift the gradient toward gold.
  const highRealm = realm.order >= 2

  const fillClass = [
    'qi-bar-fill',
    player.qiRegenerating && !critical ? 'regen' : '',
    critical ? 'critical' : '',
    highRealm ? 'high-realm' : '',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <div
      className={critical ? 'qi-bar-shake' : ''}
      style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4, width: 400 }}
    >
      <div
        className="qi-bar-track"
        style={{ width: 400, height: 12, borderRadius: 2 }}
        role="progressbar"
        aria-label="Cultivation qi"
        aria-valuemin={0}
        aria-valuemax={player.maxQi}
        aria-valuenow={Math.round(player.qi)}
      >
        <div
          className={fillClass}
          style={{ width: `${Math.max(0, Math.min(100, pct))}%`, borderRadius: 2 }}
        />
      </div>
      <div
        className="flex w-full items-center justify-between px-1"
        style={{ fontFamily: 'var(--font-mono)', fontSize: 11, letterSpacing: '0.04em' }}
      >
        <span
          className="uppercase tracking-[0.3em]"
          style={{ color: 'var(--gold-300)', opacity: 0.85 }}
        >
          元神 · Qi
        </span>
        <span
          style={{
            color: critical ? 'var(--vermillion-bright)' : 'var(--ivory)',
            textShadow: critical ? '0 0 8px rgba(231,76,60,0.6)' : '0 0 6px rgba(244,236,216,0.25)',
          }}
        >
          {Math.round(player.qi)} <span style={{ opacity: 0.5 }}>/</span>{' '}
          {Math.round(player.maxQi)} qi
          {critical && (
            <span
              className="ml-2 font-cal"
              style={{ color: 'var(--vermillion-bright)', letterSpacing: '0.1em' }}
            >
              气竭
            </span>
          )}
        </span>
      </div>
    </div>
  )
}
