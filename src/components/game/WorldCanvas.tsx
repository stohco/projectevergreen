'use client'

import { useEffect, useRef, useState } from 'react'
import * as THREE from 'three'
import { ChunkManager } from '@/engine/voxels/ChunkManager'
import { createSky, type SkyHandle } from '@/engine/render/SkySystem'
import { createPostFX, type PostFXHandle } from '@/engine/render/PostProcessing'
import { createCultivatorModel, type CultivatorModelHandle } from '@/engine/entities/CultivatorModel'
import { BlockId } from '@/engine/voxels/BlockRegistry'
import { raycastVoxels } from '@/engine/voxels/VoxelRaycaster'

/**
 * WorldCanvas — the Er Gen Verse Three.js mount.
 *
 * Composes the AAA voxel world (ChunkManager + DeterministicTerrainGenerator),
 * the procedural sky dome (Rayleigh + Mie scattering + 28-mansion stars), the
 * volumetric cloud layer (raymarched fBm), a cultivator entity (Wang Lin at
 * the spawn village), and the post-FX stack (SSAO + bloom + god rays + CA +
 * vignette + grain + ACES color grade).
 *
 * Camera is fly-mode (WASD + mouse look + space/shift up/down).
 */
export default function WorldCanvas() {
  const containerRef = useRef<HTMLDivElement>(null)
  const [status, setStatus] = useState<string>('booting')
  const [hud, setHud] = useState({
    fps: 0,
    chunks: 0,
    tris: 0,
    x: 0,
    y: 0,
    z: 0,
    biome: 'plains',
  })

  useEffect(() => {
    const container = containerRef.current
    if (!container) return

    let renderer: THREE.WebGLRenderer
    let scene: THREE.Scene
    let camera: THREE.PerspectiveCamera
    let chunkMgr: ChunkManager
    let sky: SkyHandle
    let postFX: PostFXHandle
    let cultivator: CultivatorModelHandle
    let frameId = 0
    let resizeObserver: ResizeObserver | null = null
    let pointerLocked = false

    const keys: Record<string, boolean> = {}

    try {
      renderer = new THREE.WebGLRenderer({
        antialias: true,
        powerPreference: 'high-performance',
        alpha: false,
      })
      renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
      renderer.setSize(container.clientWidth, container.clientHeight)
      renderer.toneMapping = THREE.ACESFilmicToneMapping
      renderer.toneMappingExposure = 1.0
      renderer.shadowMap.enabled = true
      renderer.shadowMap.type = THREE.PCFSoftShadowMap
      renderer.outputColorSpace = THREE.SRGBColorSpace
      container.appendChild(renderer.domElement)

      scene = new THREE.Scene()
      scene.fog = new THREE.FogExp2(0xbcd6ff, 0.0012)

      camera = new THREE.PerspectiveCamera(
        70,
        container.clientWidth / container.clientHeight,
        0.1,
        4000,
      )
      camera.position.set(30, 75, 30)
      camera.lookAt(0, 60, 0)

      // Sky.
      sky = createSky(scene)

      // Voxel world.
      chunkMgr = new ChunkManager(scene)
      chunkMgr.updateCamera(camera.position.x, camera.position.z)

      // Post-FX.
      postFX = createPostFX(renderer, scene, camera, container.clientWidth, container.clientHeight, {
        ssao: false,
        bloom: false,
        chromaticAberration: false,
        vignette: false,
        grain: false,
        colorGrade: false,
      })

      // Wang Lin cultivator at spawn village.
      cultivator = createCultivatorModel('foundation', false)
      cultivator.group.position.set(0, 58, 4)
      cultivator.setAnimation('idle')
      cultivator.setAuraVisible(true)
      scene.add(cultivator.group)

      // Spirit-vein glow point light near spawn.
      const veinLight = new THREE.PointLight(0x9be15d, 2.0, 80, 1.6)
      veinLight.position.set(-80, 65, -120)
      scene.add(veinLight)

      setStatus('live')

      // ---- Input: fly-mode camera ----
      const onKeyDown = (e: KeyboardEvent) => {
        keys[e.code] = true
        if (e.code === 'Space') e.preventDefault()
      }
      const onKeyUp = (e: KeyboardEvent) => {
        keys[e.code] = false
      }
      const onClick = () => {
        if (!pointerLocked) {
          renderer.domElement.requestPointerLock?.()
        }
      }
      const onPointerLockChange = () => {
        pointerLocked = document.pointerLockElement === renderer.domElement
      }
      const onMouseMove = (e: MouseEvent) => {
        if (!pointerLocked) return
        // Simple yaw/pitch on the camera.
        const yaw = (camera.userData.yaw ?? 0) - e.movementX * 0.0025
        const pitch = (camera.userData.pitch ?? -0.3) - e.movementY * 0.0025
        camera.userData.yaw = yaw
        camera.userData.pitch = Math.max(-1.4, Math.min(1.4, pitch))
        // Apply to camera.
        const dir = new THREE.Vector3(
          Math.sin(yaw) * Math.cos(pitch),
          Math.sin(pitch),
          Math.cos(yaw) * Math.cos(pitch),
        )
        camera.lookAt(camera.position.clone().add(dir))
      }
      window.addEventListener('keydown', onKeyDown)
      window.addEventListener('keyup', onKeyUp)
      renderer.domElement.addEventListener('click', onClick)
      document.addEventListener('pointerlockchange', onPointerLockChange)
      document.addEventListener('mousemove', onMouseMove)

      // Initial yaw/pitch.
      camera.userData.yaw = 0
      camera.userData.pitch = -0.3
      const initDir = new THREE.Vector3(0, -0.3, -1).normalize()
      camera.lookAt(camera.position.clone().add(initDir))

      // ---- Main loop ----
      const clock = new THREE.Clock()
      let fpsAccum = 0
      let fpsFrames = 0
      let fpsTimer = 0
      let hudTimer = 0
      let cultivatorYaw = 0
      let cultivatorSwapped = false

      const animate = () => {
        frameId = requestAnimationFrame(animate)
        const dt = Math.min(clock.getDelta(), 0.1)

        // Camera movement (fly-mode).
        const speed = (keys['ShiftLeft'] || keys['ShiftRight'] ? 60 : 25) * dt
        const fwd = new THREE.Vector3()
        camera.getWorldDirection(fwd)
        fwd.y = 0
        fwd.normalize()
        const right = new THREE.Vector3().crossVectors(fwd, new THREE.Vector3(0, 1, 0)).normalize()
        if (keys['KeyW']) camera.position.addScaledVector(fwd, speed)
        if (keys['KeyS']) camera.position.addScaledVector(fwd, -speed)
        if (keys['KeyA']) camera.position.addScaledVector(right, -speed)
        if (keys['KeyD']) camera.position.addScaledVector(right, speed)
        if (keys['Space']) camera.position.y += speed
        if (keys['KeyC']) camera.position.y -= speed

        // Reapply lookAt after position change (yaw/pitch already set).
        const yaw = camera.userData.yaw ?? 0
        const pitch = camera.userData.pitch ?? -0.3
        const dir = new THREE.Vector3(
          Math.sin(yaw) * Math.cos(pitch),
          Math.sin(pitch),
          Math.cos(yaw) * Math.cos(pitch),
        )
        camera.lookAt(camera.position.clone().add(dir))

        // Update systems.
        chunkMgr.updateCamera(camera.position.x, camera.position.z)
        chunkMgr.update(dt)
        sky.update(dt)
        cultivator.update(dt)

        // Idle animation cycling for demo.
        cultivatorYaw += dt * 0.3
        if (!cultivatorSwapped) {
          cultivator.setYaw(cultivatorYaw)
          if (cultivatorYaw > Math.PI * 2) {
            cultivator.setAnimation('cast')
            setTimeout(() => cultivator.setAnimation('idle'), 2000)
            cultivatorSwapped = true
          }
        }

        postFX.update(dt)
        postFX.composer.render()

        // FPS counter.
        fpsAccum += dt
        fpsFrames++
        fpsTimer += dt
        if (fpsTimer > 0.5) {
          const fps = Math.round(fpsFrames / fpsAccum)
          fpsTimer = 0
          fpsAccum = 0
          fpsFrames = 0
          hudTimer += 0.5
          if (hudTimer > 0.25) {
            hudTimer = 0
            setHud({
              fps,
              chunks: chunkMgr.loadedCount(),
              tris: chunkMgr.triangleCount(),
              x: Math.floor(camera.position.x),
              y: Math.floor(camera.position.y),
              z: Math.floor(camera.position.z),
              biome: (() => {
                const chunk = chunkMgr.getChunkAt(camera.position.x, camera.position.z)
                return chunk?.biomeTag ?? 'unknown'
              })(),
            })
          }
        }
      }
      animate()

      resizeObserver = new ResizeObserver(() => {
        const w = container.clientWidth
        const h = container.clientHeight
        camera.aspect = w / h
        camera.updateProjectionMatrix()
        renderer.setSize(w, h)
        postFX.setSize(w, h)
      })
      resizeObserver.observe(container)

      // Bridge to HUD: register globalThis.__ergenBridge so the HUD's
      // useEngine hook can pull live state from the running engine.
      const bridge = {
        getState() {
          return {
            player: {
              name: '王林 Wang Lin',
              realm: 'Foundation Establishment',
              realmCn: '筑基期',
              qi: 480,
              maxQi: 500,
              health: 100,
              maxHealth: 100,
              position: [camera.position.x, camera.position.y, camera.position.z] as [number, number, number],
              hostility: 0,
              faction: 'independent',
            },
            world: {
              time: sky.getTimeOfDay(),
              weather: 'clear' as const,
              biome: chunkMgr.getChunkAt(camera.position.x, camera.position.z)?.biomeTag ?? 'plains',
              nearbyActors: [],
              nearbyThreats: [],
              spiritVeinNear: false,
            },
            debug: {
              fps: hud.fps,
              frameTime: 16.6,
              chunks: chunkMgr.loadedCount(),
              tris: chunkMgr.triangleCount(),
              pos: [camera.position.x, camera.position.y, camera.position.z],
              yaw: (camera.userData.yaw ?? 0),
              pitch: (camera.userData.pitch ?? 0),
              biome: chunkMgr.getChunkAt(camera.position.x, camera.position.z)?.biomeTag ?? 'unknown',
              mem: 0,
              entities: 1,
              drawCalls: 0,
            },
          }
        },
        saveWorld(_key: string) {
          // Future: serialize WorldDeltaStore.
        },
        loadWorld(_key: string) {
          // Future: deserialize WorldDeltaStore.
        },
        onTick(_cb: () => void) {
          return () => {}
        },
      }
      ;(globalThis as { __ergenBridge?: unknown }).__ergenBridge = bridge

      // Block placement / removal on mouse buttons (uses raycaster).
      const onMouseDown = (e: MouseEvent) => {
        if (!pointerLocked) return
        const origin = camera.position.clone()
        const dir = new THREE.Vector3()
        camera.getWorldDirection(dir)
        const hit = raycastVoxels(
          { x: origin.x, y: origin.y, z: origin.z },
          { x: dir.x, y: dir.y, z: dir.z },
          32,
          (wx, wy, wz) => chunkMgr.getBlock(wx, wy, wz),
        )
        if (!hit) return
        if (e.button === 0) {
          // Remove block.
          chunkMgr.setBlock(hit.block.x, hit.block.y, hit.block.z, BlockId.AIR)
        } else if (e.button === 2) {
          // Place block adjacent (on the face normal).
          const px = hit.block.x + hit.normal.x
          const py = hit.block.y + hit.normal.y
          const pz = hit.block.z + hit.normal.z
          chunkMgr.setBlock(px, py, pz, BlockId.STONE_BRICKS)
        }
      }
      renderer.domElement.addEventListener('mousedown', onMouseDown)
      renderer.domElement.addEventListener('contextmenu', (e) => e.preventDefault())

      return () => {
        cancelAnimationFrame(frameId)
        resizeObserver?.disconnect()
        window.removeEventListener('keydown', onKeyDown)
        window.removeEventListener('keyup', onKeyUp)
        renderer.domElement.removeEventListener('click', onClick)
        renderer.domElement.removeEventListener('mousedown', onMouseDown)
        document.removeEventListener('pointerlockchange', onPointerLockChange)
        document.removeEventListener('mousemove', onMouseMove)
        sky.dispose()
        cultivator.dispose()
        postFX.dispose()
        renderer.dispose()
        renderer.domElement.remove()
      }
    } catch (e) {
      console.error('[WorldCanvas] boot failed', e)
      setStatus('error')
    }

    return () => {
      cancelAnimationFrame(frameId)
      resizeObserver?.disconnect()
    }
  }, [])

  return (
    <div className="relative h-full w-full bg-black">
      <div ref={containerRef} className="absolute inset-0" />
      {/* Top-left status */}
      <div className="pointer-events-none absolute left-4 top-4 z-10 select-none font-mono text-[11px] uppercase tracking-[0.3em] text-amber-200/80">
        Er Gen Verse · {status}
      </div>
      {/* Top-right HUD */}
      <div className="pointer-events-none absolute right-4 top-4 z-10 select-none rounded-md border border-amber-500/30 bg-black/50 px-3 py-2 font-mono text-[10px] text-amber-100/90 backdrop-blur-sm">
        <div>FPS <span className="text-emerald-300">{hud.fps}</span></div>
        <div>CHK <span className="text-emerald-300">{hud.chunks}</span></div>
        <div>TRI <span className="text-emerald-300">{(hud.tris / 1000).toFixed(1)}k</span></div>
        <div>POS <span className="text-emerald-300">{hud.x},{hud.y},{hud.z}</span></div>
        <div>BIO <span className="text-emerald-300">{hud.biome}</span></div>
      </div>
      {/* Bottom-center lore */}
      <div className="pointer-events-none absolute bottom-4 left-1/2 z-10 -translate-x-1/2 select-none text-center font-serif text-amber-100/60">
        <p className="text-sm italic">天地不仁，以万物为刍狗</p>
        <p className="mt-1 text-[10px] uppercase tracking-[0.3em] text-amber-200/40">
          Heaven is impartial; all things are straw dogs
        </p>
      </div>
      {/* Controls hint */}
      <div className="pointer-events-none absolute bottom-4 right-4 z-10 select-none rounded-md border border-amber-500/30 bg-black/50 px-3 py-2 font-mono text-[10px] text-amber-100/70 backdrop-blur-sm">
        <div>WASD move · MOUSE look</div>
        <div>SPACE up · C down · SHIFT fast</div>
        <div>L-CLICK break · R-CLICK place</div>
      </div>
    </div>
  )
}
