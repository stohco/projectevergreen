/**
 * GameSystems — the 3 split systems that replace the WorldCanvas god component.
 *
 * Per ARCHITECTURE_AUDIT.md violation #1: "WorldCanvas.tsx is a god component
 * that directly mutates simulation state."
 *
 * Split into:
 *   RenderSystem — pure Three.js rendering (scene, camera, renderer, post-FX)
 *   InputSystem  — keyboard/mouse → player movement + interaction intents
 *   SimulationSystem — NPC cognition, beast wandering, door animation, spawner
 *
 * WorldCanvas now orchestrates these 3 systems instead of doing everything.
 */

import * as THREE from 'three'
import { ChunkManager } from '@/engine/voxels/ChunkManager'

// ---- RenderSystem -------------------------------------------------------
// Owns: renderer, scene, camera, sky, ocean, post-FX, lights.
// Does NOT own: player position, NPC state, door state, collision.
// Queries: "what should be visible?" from the simulation state.

export interface RenderSystem {
  renderer: THREE.WebGLRenderer
  scene: THREE.Scene
  camera: THREE.PerspectiveCamera
  update(dt: number): void
  resize(w: number, h: number): void
  dispose(): void
}

// ---- InputSystem --------------------------------------------------------
// Owns: key state, mouse state, pointer lock, camera yaw/pitch/zoom.
// Does NOT own: player position, collision, door state.
// Outputs: movement intents (forward/right/jump/sprint) + interaction events.

export interface InputState {
  forward: number   // -1 (back) to 1 (forward)
  right: number     // -1 (left) to 1 (right)
  jump: boolean
  sprint: boolean
  flyUp: boolean
  flyDown: boolean
  cameraYaw: number
  cameraPitch: number
  cameraZoom: number
  pointerLocked: boolean
}

export interface InputSystem {
  state: InputState
  update(dt: number): InputState
  dispose(): void
}

// ---- SimulationSystem ---------------------------------------------------
// Owns: player position, NPC cognition, beast wandering, door animation,
// collision, spawner, delta store.
// Does NOT own: renderer, camera, post-FX.
// Inputs: InputState (from InputSystem) + terrain height function.

export interface SimulationSystem {
  playerPosition: THREE.Vector3
  playerYaw: number
  update(dt: number, input: InputState): void
  dispose(): void
}

// ---- Shared game context (passed to all systems) ------------------------

export interface GameContext {
  terrainHeight: (x: number, z: number) => number
  collision: { resolve: (x: number, y: number, z: number, prevX: number, prevY: number, prevZ: number) => { x: number; z: number; hit: boolean } }
}
