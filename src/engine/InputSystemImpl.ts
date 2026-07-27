/**
 * InputSystemImpl — keyboard/mouse input → movement intents.
 *
 * Owns: key state, mouse state, pointer lock, camera yaw/pitch/zoom.
 * Does NOT own: player position, collision, door state.
 *
 * Outputs InputState each frame: { forward, right, jump, sprint, flyUp,
 * flyDown, cameraYaw, cameraPitch, cameraZoom, pointerLocked }.
 *
 * Per ARCHITECTURE_AUDIT.md: this separates input from rendering and
 * simulation. WorldCanvas no longer directly handles key/mouse events.
 */

import type { InputSystem, InputState } from './GameSystems'

export class InputSystemImpl implements InputSystem {
  private readonly keys: Record<string, boolean> = {}
  private _pointerLocked = false
  private _yaw = 0
  private _pitch = -0.15
  private _zoom = 7
  private rightMouseDown = false

  state: InputState = {
    forward: 0, right: 0, jump: false, sprint: false,
    flyUp: false, flyDown: false,
    cameraYaw: 0, cameraPitch: -0.15, cameraZoom: 7,
    pointerLocked: false,
  }

  private readonly canvas: HTMLCanvasElement
  private onToggleLock?: () => void
  private onInteract?: () => void
  private onMine?: () => void
  private onPlace?: () => void
  private onSave?: () => void
  private onLoad?: () => void

  constructor(canvas: HTMLCanvasElement, callbacks?: {
    onToggleLock?: () => void
    onInteract?: () => void
    onMine?: () => void
    onPlace?: () => void
    onSave?: () => void
    onLoad?: () => void
  }) {
    this.canvas = canvas
    this.onToggleLock = callbacks?.onToggleLock
    this.onInteract = callbacks?.onInteract
    this.onMine = callbacks?.onMine
    this.onPlace = callbacks?.onPlace
    this.onSave = callbacks?.onSave
    this.onLoad = callbacks?.onLoad

    window.addEventListener('keydown', this.handleKeyDown)
    window.addEventListener('keyup', this.handleKeyUp)
    window.addEventListener('mousedown', this.handleMouseDown)
    window.addEventListener('mouseup', this.handleMouseUp)
    window.addEventListener('contextmenu', this.handleContextMenu)
    canvas.addEventListener('wheel', this.handleWheel, { passive: false })
    document.addEventListener('pointerlockchange', this.handlePointerLockChange)
    document.addEventListener('mousemove', this.handleMouseMove)
  }

  update(_dt: number): InputState {
    this.state.forward = (this.keys['KeyW'] ? 1 : 0) - (this.keys['KeyS'] ? 1 : 0)
    this.state.right = (this.keys['KeyD'] ? 1 : 0) - (this.keys['KeyA'] ? 1 : 0)
    this.state.jump = !!this.keys['Space']
    this.state.sprint = !!(this.keys['ShiftLeft'] || this.keys['ShiftRight'])
    this.state.flyUp = !!this.keys['Space']
    this.state.flyDown = !!this.keys['KeyC']
    this.state.cameraYaw = this._yaw
    this.state.cameraPitch = this._pitch
    this.state.cameraZoom = this._zoom
    this.state.pointerLocked = this._pointerLocked
    return this.state
  }

  private handleKeyDown = (e: KeyboardEvent) => {
    this.keys[e.code] = true
    if (e.code === 'Space') e.preventDefault()
    if (e.code === 'KeyY') this.onToggleLock?.()
    if (e.code === 'KeyE') this.onInteract?.()
    if (e.code === 'KeyF') this.onToggleLock?.() // F also toggles for now
    if (e.code === 'F5') { e.preventDefault(); this.onSave?.() }
    if (e.code === 'F9') { e.preventDefault(); this.onLoad?.() }
  }

  private handleKeyUp = (e: KeyboardEvent) => {
    this.keys[e.code] = false
  }

  private handleMouseDown = (e: MouseEvent) => {
    if (e.button === 2) {
      this.rightMouseDown = true
      e.preventDefault()
    }
    if (this._pointerLocked) {
      if (e.button === 0) this.onMine?.()
      if (e.button === 2) this.onPlace?.()
    }
  }

  private handleMouseUp = (e: MouseEvent) => {
    if (e.button === 2) this.rightMouseDown = false
  }

  private handleContextMenu = (e: Event) => { e.preventDefault() }

  private handleWheel = (e: WheelEvent) => {
    e.preventDefault()
    this._zoom = Math.max(3, Math.min(20, this._zoom + e.deltaY * 0.01))
  }

  private handlePointerLockChange = () => {
    this._pointerLocked = document.pointerLockElement === this.canvas
  }

  private handleMouseMove = (e: MouseEvent) => {
    if (!this._pointerLocked) return
    const sensitivity = this.rightMouseDown ? 0.005 : 0.003
    this._yaw -= e.movementX * sensitivity
    this._pitch = Math.max(-1.0, Math.min(0.6, this._pitch - e.movementY * sensitivity))
  }

  requestPointerLock(): void {
    this.canvas.requestPointerLock?.()
  }

  exitPointerLock(): void {
    document.exitPointerLock?.()
  }

  dispose(): void {
    window.removeEventListener('keydown', this.handleKeyDown)
    window.removeEventListener('keyup', this.handleKeyUp)
    window.removeEventListener('mousedown', this.handleMouseDown)
    window.removeEventListener('mouseup', this.handleMouseUp)
    window.removeEventListener('contextmenu', this.handleContextMenu)
    this.canvas.removeEventListener('wheel', this.handleWheel)
    document.removeEventListener('pointerlockchange', this.handlePointerLockChange)
    document.removeEventListener('mousemove', this.handleMouseMove)
  }
}
