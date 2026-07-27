/**
 * NPCCognition — the perception → action stack for NPCs.
 *
 * Per PRD §16: NPCs should communicate thought through body language.
 * The cognition stack is:
 *   perception → interpretation → motivation → reasoning →
 *   commitment → intent → performance → action
 *
 * Per PRD §16.2, the acting layer communicates through channels:
 *   attention, urgency, confidence, concealment, tension, patience, fatigue
 *
 * Per PRD §16.3, the player should read visually:
 *   - whether the NPC is observing
 *   - whether they are cautious
 *   - whether they are suppressing strength
 *   - whether they are about to flee
 *   - whether they are considering the player
 *   - whether they are interested in a world event
 *
 * This system drives Wang Lin's manifestation NPC and future NPCs.
 */

import * as THREE from 'three'
import type { CultivatorModelHandle, AnimKey } from './CultivatorModel'

// ---- Cognition state -----------------------------------------------------

export type PerceptionState = 'idle' | 'observing_player' | 'observing_beast' | 'observing_event' | 'meditating' | 'sleeping' | 'walking'

export type Motivation = 'curiosity' | 'caution' | 'hostility' | 'friendliness' | 'indifference' | 'meditation' | 'duty'

export type Commitment = 'none' | 'observing' | 'approaching' | 'retreating' | 'meditating' | 'patrolling'

export interface IntentState {
  /** What the NPC is currently focused on. */
  perception: PerceptionState
  /** Why they're doing it. */
  motivation: Motivation
  /** What they've committed to doing. */
  commitment: Commitment
  /** Target position (for walking/approaching). */
  targetPosition: THREE.Vector3 | null
  /** Target entity id (for observing/approaching). */
  targetEntityId: string | null

  // Body language channels (0.0 - 1.0).
  attention: number      // how focused they are
  urgency: number        // how time-critical their action is
  confidence: number     // how sure they are of themselves
  concealment: number    // how much they're hiding their true power
  tension: number        // physical readiness / fight-or-flight
  patience: number       // how long they'll wait before acting
  fatigue: number        // how tired they are (0 = fresh, 1 = exhausted)
}

export interface NPCCognitionConfig {
  /** NPC name. */
  name: string
  /** NPC's cultivation realm (affects confidence, concealment). */
  realm: string
  /** Home position (where they return to). */
  homePosition: THREE.Vector3
  /** Patrol radius (how far they wander from home). */
  patrolRadius: number
  /** How quickly they notice the player (0-1, higher = faster). */
  perceptionRange: number
  /** Base hostility toward the player (0 = friendly, 100 = hostile). */
  baseHostility: number
}

// ---- NPC Cognition controller -------------------------------------------

export class NPCCognition {
  private readonly config: NPCCognitionConfig
  private readonly model: CultivatorModelHandle
  private state: IntentState
  private decisionTimer: number = 0
  private readonly DECISION_INTERVAL = 0.5 // seconds between decisions

  // Wander state.
  private wanderTarget: THREE.Vector3 | null = null
  private wanderTimer: number = 0

  constructor(config: NPCCognitionConfig, model: CultivatorModelHandle) {
    this.config = config
    this.model = model
    this.state = {
      perception: 'idle',
      motivation: 'indifference',
      commitment: 'none',
      targetPosition: null,
      targetEntityId: null,
      attention: 0.3,
      urgency: 0.1,
      confidence: 0.5,
      concealment: 0.7, // Wang Lin suppresses his power (canon)
      tension: 0.2,
      patience: 0.8,
      fatigue: 0.0,
    }
  }

  /**
   * Update the NPC's cognition and drive its model.
   *
   * @param dt - delta time in seconds
   * @param playerPos - the player's current position
   * @param playerVisible - whether the player is in line of sight
   * @param nearbyBeasts - array of nearby beast positions
   */
  update(
    dt: number,
    playerPos: THREE.Vector3,
    playerVisible: boolean,
    nearbyBeasts: THREE.Vector3[] = [],
  ): void {
    this.decisionTimer += dt
    if (this.decisionTimer < this.DECISION_INTERVAL) {
      // Between decisions: just execute current commitment.
      this.executeCommitment(dt, playerPos)
      this.applyBodyLanguage(dt)
      return
    }
    this.decisionTimer = 0

    // ---- 1. PERCEPTION ----
    const distToPlayer = this.model.group.position.distanceTo(playerPos)
    const playerNearby = distToPlayer < this.config.perceptionRange * 10

    if (playerVisible && playerNearby) {
      // Player is visible and nearby.
      this.state.perception = 'observing_player'
      this.state.attention = Math.min(1, this.state.attention + 0.3)
      this.state.targetEntityId = 'player'
      this.state.targetPosition = playerPos.clone()

      // ---- 2. INTERPRETATION ----
      // How does the NPC interpret the player?
      if (this.config.baseHostility > 50) {
        this.state.motivation = 'hostility'
        this.state.tension = Math.min(1, this.state.tension + 0.2)
      } else if (distToPlayer < 5) {
        // Player is very close — cautious.
        this.state.motivation = 'caution'
        this.state.tension = Math.min(1, this.state.tension + 0.1)
      } else {
        // Player is at a safe distance — curious.
        this.state.motivation = 'curiosity'
        this.state.attention = Math.min(1, this.state.attention + 0.1)
      }
    } else if (nearbyBeasts.length > 0) {
      // A beast is nearby.
      this.state.perception = 'observing_beast'
      this.state.attention = Math.min(1, this.state.attention + 0.2)
      this.state.tension = Math.min(1, this.state.tension + 0.1)
      this.state.targetPosition = nearbyBeasts[0].clone()
    } else {
      // Nothing interesting — return to idle.
      this.state.perception = Math.random() < 0.3 ? 'walking' : 'idle'
      this.state.motivation = 'indifference'
      this.state.attention = Math.max(0.2, this.state.attention - 0.1)
      this.state.tension = Math.max(0.1, this.state.tension - 0.1)
      this.state.targetEntityId = null
    }

    // ---- 3. MOTIVATION → COMMITMENT ----
    if (this.state.motivation === 'hostility' && distToPlayer < 10) {
      this.state.commitment = 'approaching'
    } else if (this.state.motivation === 'caution' && distToPlayer < 4) {
      this.state.commitment = 'retreating'
    } else if (this.state.motivation === 'curiosity' && distToPlayer > 8) {
      // Curious but player is far — approach slowly.
      if (Math.random() < 0.3) {
        this.state.commitment = 'approaching'
      } else {
        this.state.commitment = 'observing'
      }
    } else if (this.state.perception === 'walking') {
      this.state.commitment = 'patrolling'
    } else {
      this.state.commitment = 'observing'
    }

    // Occasionally meditate (canon: Wang Lin meditates frequently).
    if (Math.random() < 0.05 && this.state.perception === 'idle') {
      this.state.perception = 'meditating'
      this.state.commitment = 'meditating'
      this.state.motivation = 'meditation'
    }

    // ---- 4. REASONING → adjust body language ----
    // Confidence is based on realm + concealment.
    this.state.confidence = 0.5 + (1 - this.state.concealment) * 0.3
    // Patience decreases with tension.
    this.state.patience = Math.max(0.1, 0.8 - this.state.tension * 0.5)
    // Fatigue increases slowly.
    this.state.fatigue = Math.min(1, this.state.fatigue + dt * 0.001)

    this.executeCommitment(dt, playerPos)
    this.applyBodyLanguage(dt)
  }

  // ---- Execute the current commitment ----

  private executeCommitment(dt: number, playerPos: THREE.Vector3): void {
    const pos = this.model.group.position
    const speed = 2 * dt // NPCs walk slowly (2 blocks/sec)

    switch (this.state.commitment) {
      case 'approaching': {
        if (this.state.targetPosition) {
          const dir = this.state.targetPosition.clone().sub(pos)
          dir.y = 0
          if (dir.length() > 1.5) {
            dir.normalize().multiplyScalar(speed)
            pos.add(dir)
            // Face the target.
            this.model.setYaw(Math.atan2(dir.x, dir.z))
            this.model.setAnimation('walk')
          } else {
            this.model.setAnimation('idle')
          }
        }
        break
      }
      case 'retreating': {
        // Move away from the player.
        const dir = pos.clone().sub(playerPos)
        dir.y = 0
        if (dir.length() > 0.01) {
          dir.normalize().multiplyScalar(speed)
          pos.add(dir)
          this.model.setYaw(Math.atan2(dir.x, dir.z))
          this.model.setAnimation('run')
        }
        break
      }
      case 'patrolling': {
        // Wander around home position.
        this.wanderTimer -= dt
        if (this.wanderTimer <= 0 || !this.wanderTarget) {
          // Pick a new wander target.
          const angle = Math.random() * Math.PI * 2
          const dist = Math.random() * this.config.patrolRadius
          this.wanderTarget = this.config.homePosition.clone().add(
            new THREE.Vector3(Math.cos(angle) * dist, 0, Math.sin(angle) * dist),
          )
          this.wanderTimer = 5 + Math.random() * 10 // 5-15 seconds
        }
        const dir = this.wanderTarget.clone().sub(pos)
        dir.y = 0
        if (dir.length() > 0.5) {
          dir.normalize().multiplyScalar(speed * 0.7)
          pos.add(dir)
          this.model.setYaw(Math.atan2(dir.x, dir.z))
          this.model.setAnimation('walk')
        } else {
          this.model.setAnimation('idle')
        }
        break
      }
      case 'meditating': {
        // Stand still and meditate.
        this.model.setAnimation('cast')
        // Face toward the shrine/altar if nearby, otherwise face home.
        const facing = this.config.homePosition.clone().sub(pos)
        if (facing.length() > 0.1) {
          this.model.setYaw(Math.atan2(facing.x, facing.z))
        }
        // Randomly stop meditating.
        if (Math.random() < 0.01) {
          this.state.commitment = 'none'
          this.state.perception = 'idle'
        }
        break
      }
      case 'observing':
      default: {
        // Stand still and observe. Face the target.
        if (this.state.targetPosition) {
          const dir = this.state.targetPosition.clone().sub(pos)
          dir.y = 0
          if (dir.length() > 0.1) {
            // Smoothly turn toward the target.
            const targetYaw = Math.atan2(dir.x, dir.z)
            const currentYaw = this.model.group.rotation.y
            let diff = targetYaw - currentYaw
            while (diff > Math.PI) diff -= Math.PI * 2
            while (diff < -Math.PI) diff += Math.PI * 2
            this.model.setYaw(currentYaw + diff * Math.min(1, dt * 5))
          }
        }
        this.model.setAnimation('idle')
        break
      }
    }

    // Keep NPC on terrain (caller handles terrain height externally).
  }

  // ---- Apply body language to the model ----

  private applyBodyLanguage(dt: number): void {
    // The body language channels drive subtle model adjustments.
    // For now, we use animation choice + aura visibility as proxies.

    // High tension → show aura (qi flaring).
    if (this.state.tension > 0.6) {
      this.model.setAuraVisible(true)
    } else if (this.state.tension < 0.3) {
      this.model.setAuraVisible(false)
    }

    // High concealment → suppress aura even if tense.
    if (this.state.concealment > 0.8) {
      this.model.setAuraVisible(false)
    }

    // High fatigue → slower animations (would adjust animation speed in a
    // full implementation — for now, just ensure idle when very tired).
    if (this.state.fatigue > 0.8 && this.state.commitment === 'patrolling') {
      this.model.setAnimation('idle')
    }
  }

  // ---- Public accessors ----

  getState(): IntentState {
    return { ...this.state }
  }

  getPerception(): PerceptionState {
    return this.state.perception
  }

  getMotivation(): Motivation {
    return this.state.motivation
  }

  getCommitment(): Commitment {
    return this.state.commitment
  }

  /** Returns a human-readable description of what the NPC is doing. */
  getDescription(): string {
    const parts: string[] = []
    parts.push(this.state.perception.replace(/_/g, ' '))
    if (this.state.motivation !== 'indifference') {
      parts.push(`(${this.state.motivation})`)
    }
    if (this.state.commitment !== 'none' && this.state.commitment !== 'observing') {
      parts.push(`→ ${this.state.commitment}`)
    }
    return parts.join(' ')
  }
}
