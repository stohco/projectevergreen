'use client'

import { useEffect, useRef, useState } from 'react'
import * as THREE from 'three'
import { createSky, type SkyHandle } from '@/engine/render/SkySystem'
import { createPostFX, type PostFXHandle } from '@/engine/render/PostProcessing'
import { createCultivatorModel, type CultivatorModelHandle } from '@/engine/entities/CultivatorModel'
import { createPlayer, type PlayerHandle } from '@/engine/entities/PlayerEntity'
import { createSmoothTerrain, createSpiritPines, createGrassTufts, createRocks, createSpiritFlowers, terrainHeight } from '@/engine/world/SmoothTerrain'
import { compileSettlement } from '@/engine/world/compiler/SettlementCompiler'
import { WANG_FAMILY_VILLAGE } from '@/engine/canon/settlements/WangFamilyVillage'
import { WorldGraph } from '@/engine/graph/WorldGraph'
import { GraphQueryService } from '@/engine/graph/GraphQueryService'
import { bootstrapGraphFromCanon } from '@/engine/graph/CanonGraphLoader'

/**
 * WorldCanvas — the Er Gen Verse Three.js mount.
 *
 * ARCHITECTURE (per formal spec):
 *   Canon (WangFamilyVillage.ts) → Semantic (CanonTypes) → Template (TemplateLibrary)
 *   → Compiler (SettlementCompiler) → Three.js Presentation Layer → Player
 *
 * The player is NOT Wang Lin. The player is a first-class actor (Traveler /
 * 行道者 in ivory robes). Wang Lin is a manifestation NPC (jade green robes,
 * Foundation realm) — his real self is on the Immortal Astral Continent.
 *
 * Visual style: smooth low-poly (like No Mortal Space), NOT blocky Minecraft.
 * Terrain is a smooth heightmap mesh. Buildings are compiled from semantic
 * data using the template library. Trees are instanced meshes.
 */
export default function WorldCanvas() {
  const containerRef = useRef<HTMLDivElement>(null)
  const [status, setStatus] = useState<string>('booting')
  const [hud, setHud] = useState({
    fps: 0,
    x: 0, y: 0, z: 0,
    qi: 0, maxQi: 0,
    realm: 'Mortal',
  })

  useEffect(() => {
    const container = containerRef.current
    if (!container) return

    let renderer: THREE.WebGLRenderer
    let scene: THREE.Scene
    let camera: THREE.PerspectiveCamera
    let sky: SkyHandle
    let postFX: PostFXHandle
    let player: PlayerHandle
    let wanglinNpc: CultivatorModelHandle
    let frameId = 0
    let resizeObserver: ResizeObserver | null = null
    let pointerLocked = false
    const keys: Record<string, boolean> = {}

    try {
      // ---- Renderer ----
      renderer = new THREE.WebGLRenderer({ antialias: true, powerPreference: 'high-performance' })
      renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
      renderer.setSize(container.clientWidth, container.clientHeight)
      renderer.toneMapping = THREE.ACESFilmicToneMapping
      renderer.toneMappingExposure = 1.1
      renderer.shadowMap.enabled = true
      renderer.shadowMap.type = THREE.PCFSoftShadowMap
      renderer.outputColorSpace = THREE.SRGBColorSpace
      container.appendChild(renderer.domElement)

      // ---- Scene ----
      scene = new THREE.Scene()
      scene.fog = new THREE.FogExp2(0xc8d6e8, 0.0025)

      // ---- Camera ----
      camera = new THREE.PerspectiveCamera(65, container.clientWidth / container.clientHeight, 0.1, 2000)
      camera.position.set(16, 14, 20)
      camera.lookAt(0, 4, 0)

      // ---- Sky ----
      sky = createSky(scene)

      // ---- Lighting (proper, not flat) ----
      const ambient = new THREE.AmbientLight(0xbcd6ff, 0.35)
      scene.add(ambient)
      const hemi = new THREE.HemisphereLight(0xbcd6ff, 0x4a3520, 0.5)
      scene.add(hemi)
      // Sun — the sky system already adds a directional light, but we add
      // a fill light so shadows aren't too dark.
      const fillLight = new THREE.DirectionalLight(0xfff4d6, 0.8)
      fillLight.position.set(30, 50, 20)
      fillLight.castShadow = true
      fillLight.shadow.mapSize.set(2048, 2048)
      fillLight.shadow.camera.left = -60
      fillLight.shadow.camera.right = 60
      fillLight.shadow.camera.top = 60
      fillLight.shadow.camera.bottom = -60
      fillLight.shadow.camera.near = 0.5
      fillLight.shadow.camera.far = 200
      fillLight.shadow.bias = -0.0003
      scene.add(fillLight)

      // ---- Smooth terrain (NOT blocky voxels) ----
      const terrainSize = 400
      const terrain = createSmoothTerrain(0, 0, terrainSize, 120)
      scene.add(terrain)

      // ---- Spirit pines (instanced, not blocky) ----
      const pines = createSpiritPines(0, 0, 200, 150)
      scene.add(pines)

      // ---- Grass tufts (kills the "flat green void" look) ----
      const grass = createGrassTufts(0, 0, 200, 3000)
      scene.add(grass)

      // ---- Rocks (geological detail) ----
      const rocks = createRocks(0, 0, 200, 40)
      scene.add(rocks)

      // ---- Spirit flowers (qi-infused xianxia atmosphere) ----
      const flowers = createSpiritFlowers(0, 0, 150, 60)
      scene.add(flowers)

      // ---- Wang Family Village (compiled from semantic data) ----
      const villageGroup = compileSettlement(WANG_FAMILY_VILLAGE)
      // The village buildings have Y=0 in semantic data; we need to place
      // them at terrain height. The settlement origin is (0,0,0) so buildings
      // are already at the right XZ. We lift the entire group to terrain Y.
      villageGroup.position.y = terrainHeight(0, 0)
      scene.add(villageGroup)

      // ---- Player (NOT Wang Lin) ----
      // Player name comes from the character creation screen (English, typed).
      // Player starts as a MORTAL: 0 qi, 0 maxQi, brown peasant clothes.
      const playerName = (globalThis as { __ergenPlayerName?: string }).__ergenPlayerName ?? 'Mortal'
      const playerSpiritRoot = ((globalThis as { __ergenPlayerSpiritRoot?: string }).__ergenPlayerSpiritRoot ?? 'wood') as 'metal' | 'wood' | 'water' | 'fire' | 'earth' | 'void'
      const spawnY = terrainHeight(4, 8)
      player = createPlayer({
        name: playerName,
        spiritRoot: playerSpiritRoot,
        startPosition: [4, spawnY, 8],
      })
      player.setAnimation('idle')
      scene.add(player.group)

      // ---- Wang Lin NPC (manifestation) ----
      const wanglinY = terrainHeight(0, 0)
      wanglinNpc = createCultivatorModel('foundation', false)
      wanglinNpc.group.position.set(0, wanglinY, 0)
      wanglinNpc.group.scale.setScalar(1.1)
      wanglinNpc.setAnimation('idle')
      wanglinNpc.setAuraVisible(false)
      scene.add(wanglinNpc.group)

      // ---- WorldGraph (async bootstrap) ----
      const graph = new WorldGraph()
      const graphQuery = new GraphQueryService(graph)
      bootstrapGraphFromCanon(graph).then((n) => {
        console.log('[WorldCanvas] graph bootstrapped:', n, 'nodes')
      }).catch(() => {})

      // ---- Post-FX (subtle) ----
      postFX = createPostFX(renderer, scene, camera, container.clientWidth, container.clientHeight, {
        ssao: false,
        bloom: true,
        bloomStrength: 0.2,
        chromaticAberration: false,
        vignette: true,
        grain: false,
        colorGrade: true,
      })

      setStatus('live')

      // ---- Input ----
      const onKeyDown = (e: KeyboardEvent) => {
        keys[e.code] = true
        if (e.code === 'Space') e.preventDefault()
        if (e.code === 'KeyF') {
          // Sword-flight requires qi. A mortal (maxQi=0) cannot fly.
          if (player.state.isFlying) { player.setFlying(false) }
          else if (player.state.maxQi > 0 && player.consumeQi(10)) { player.setFlying(true) }
        }
        if (e.code === 'KeyQ') {
          // Meditation requires at least some qi capacity.
          if (player.state.maxQi > 0) player.setMeditating(!player.state.isMeditating)
        }
      }
      const onKeyUp = (e: KeyboardEvent) => { keys[e.code] = false }
      const onClick = () => { if (!pointerLocked) renderer.domElement.requestPointerLock?.() }
      const onPointerLockChange = () => { pointerLocked = document.pointerLockElement === renderer.domElement }
      const onMouseMove = (e: MouseEvent) => {
        if (!pointerLocked) return
        camera.userData.yaw = (camera.userData.yaw ?? 0) - e.movementX * 0.003
        camera.userData.pitch = Math.max(-1.0, Math.min(0.6, (camera.userData.pitch ?? -0.15) - e.movementY * 0.003))
      }
      // Scroll-to-zoom: adjust camera distance.
      const onWheel = (e: WheelEvent) => {
        e.preventDefault()
        const zoom = (camera.userData.zoom ?? 7) + e.deltaY * 0.01
        camera.userData.zoom = Math.max(3, Math.min(20, zoom))
      }
      window.addEventListener('keydown', onKeyDown)
      window.addEventListener('keyup', onKeyUp)
      renderer.domElement.addEventListener('click', onClick)
      renderer.domElement.addEventListener('wheel', onWheel, { passive: false })
      document.addEventListener('pointerlockchange', onPointerLockChange)
      document.addEventListener('mousemove', onMouseMove)
      camera.userData.yaw = 0
      camera.userData.pitch = -0.15
      camera.userData.zoom = 7

      // ---- Main loop ----
      const clock = new THREE.Clock()
      let fpsFrames = 0, fpsTimer = 0, hudTimer = 0
      let wanglinYaw = 0

      const animate = () => {
        frameId = requestAnimationFrame(animate)
        const dt = Math.min(clock.getDelta(), 0.1)

        // Player movement.
        const yaw = camera.userData.yaw ?? 0
        const speed = (player.state.isFlying ? 30 : (keys['ShiftLeft'] ? 10 : 5)) * dt
        const fwd = new THREE.Vector3(Math.sin(yaw), 0, Math.cos(yaw))
        const right = new THREE.Vector3(-fwd.z, 0, fwd.x)
        let moved = false
        if (keys['KeyW']) { player.state.position.addScaledVector(fwd, speed); moved = true }
        if (keys['KeyS']) { player.state.position.addScaledVector(fwd, -speed); moved = true }
        if (keys['KeyA']) { player.state.position.addScaledVector(right, -speed); moved = true }
        if (keys['KeyD']) { player.state.position.addScaledVector(right, speed); moved = true }

        // Ground clamp: player stands on terrain surface.
        if (player.state.isFlying) {
          if (keys['Space']) player.state.position.y += speed
          if (keys['KeyC']) player.state.position.y -= speed
          if (!player.consumeQi(2.0 * dt)) player.setFlying(false)
        } else {
          const groundY = terrainHeight(player.state.position.x, player.state.position.z)
          if (player.state.position.y < groundY + 0.5) {
            player.state.position.y = groundY + 0.0 // snap to surface
            player.state.velocity.y = 0
          } else {
            player.state.position.y -= 20 * dt // gravity
          }
          if (keys['Space'] && player.state.position.y <= groundY + 0.1) {
            player.state.velocity.y = 7 // jump
          }
          player.state.position.y += player.state.velocity.y * dt
          player.state.velocity.y -= 25 * dt // gravity
          // Don't fall through terrain.
          const newGround = terrainHeight(player.state.position.x, player.state.position.z)
          if (player.state.position.y < newGround) {
            player.state.position.y = newGround
            player.state.velocity.y = 0
          }
        }

        player.setYaw(yaw + Math.PI)
        if (player.state.isMeditating) player.setAnimation('cast')
        else if (player.state.isFlying) player.setAnimation('fly')
        else if (moved) player.setAnimation(keys['ShiftLeft'] ? 'run' : 'walk')
        else player.setAnimation('idle')
        player.update(dt)

        // Wang Lin NPC idle.
        wanglinYaw += dt * 0.15
        wanglinNpc.setYaw(wanglinYaw)
        wanglinNpc.update(dt)

        // Third-person camera (smooth follow + scroll zoom).
        const pitch = camera.userData.pitch ?? -0.15
        const camDist = camera.userData.zoom ?? 7
        const camHeight = 2
        const camOffset = new THREE.Vector3(
          -Math.sin(yaw) * Math.cos(pitch) * camDist,
          -Math.sin(pitch) * camDist + camHeight,
          -Math.cos(yaw) * Math.cos(pitch) * camDist,
        )
        const targetCamPos = player.state.position.clone().add(camOffset)
        camera.position.lerp(targetCamPos, 0.12)
        camera.lookAt(player.state.position.x, player.state.position.y + 1.2, player.state.position.z)

        // Update systems.
        sky.update(dt)
        postFX.update(dt)
        postFX.composer.render()

        // FPS + HUD.
        fpsFrames++
        fpsTimer += dt
        if (fpsTimer > 0.5) {
          const fps = Math.round(fpsFrames / fpsTimer)
          fpsTimer = 0; fpsFrames = 0; hudTimer += 0.5
          if (hudTimer > 0.2) {
            hudTimer = 0
            setHud({
              fps,
              x: Math.floor(player.state.position.x),
              y: Math.floor(player.state.position.y),
              z: Math.floor(player.state.position.z),
              qi: Math.floor(player.state.qi),
              maxQi: player.state.maxQi,
              realm: 'Mortal',
            })
          }
        }
      }
      animate()

      // ---- Resize ----
      resizeObserver = new ResizeObserver(() => {
        const w = container.clientWidth, h = container.clientHeight
        camera.aspect = w / h
        camera.updateProjectionMatrix()
        renderer.setSize(w, h)
        postFX.setSize(w, h)
      })
      resizeObserver.observe(container)

      // ---- Bridge to HUD ----
      const bridge = {
        getState() {
          return {
            player: {
              name: player.state.name,
              realm: 'Mortal',
              qi: Math.floor(player.state.qi),
              maxQi: player.state.maxQi,
              health: player.state.health,
              maxHealth: player.state.maxHealth,
              position: [player.state.position.x, player.state.position.y, player.state.position.z] as [number, number, number],
              hostility: 0,
              faction: 'independent',
              isFlying: player.state.isFlying,
              isMeditating: player.state.isMeditating,
              spiritRoot: player.state.spiritRoot,
            },
            world: {
              time: sky.getTimeOfDay(),
              weather: 'clear' as const,
              biome: 'plains',
              nearbyActors: [
                { name: 'Wang Lin (Manifestation)', nameCn: '王林 (化身)', realm: 'Foundation Establishment', hostility: 0, distance: Math.floor(player.state.position.distanceTo(wanglinNpc.group.position)) },
              ],
              nearbyThreats: [],
              spiritVeinNear: player.state.position.distanceTo(new THREE.Vector3(-80, 0, -120)) < 100,
            },
            debug: {
              fps: hud.fps,
              frameTime: 16.6,
              chunks: 1, tris: 0,
              pos: [player.state.position.x, player.state.position.y, player.state.position.z],
              yaw: (camera.userData.yaw ?? 0),
              pitch: (camera.userData.pitch ?? 0),
              biome: 'plains',
              mem: 0, entities: 2, drawCalls: 0,
            },
          }
        },
        saveWorld() {}, loadWorld() {}, onTick() { return () => {} },
      }
      ;(globalThis as { __ergenBridge?: unknown }).__ergenBridge = bridge

      return () => {
        cancelAnimationFrame(frameId)
        resizeObserver?.disconnect()
        window.removeEventListener('keydown', onKeyDown)
        window.removeEventListener('keyup', onKeyUp)
        renderer.domElement.removeEventListener('click', onClick)
        renderer.domElement.removeEventListener('wheel', onWheel)
        document.removeEventListener('pointerlockchange', onPointerLockChange)
        document.removeEventListener('mousemove', onMouseMove)
        sky.dispose(); player.dispose(); wanglinNpc.dispose()
        postFX.dispose(); renderer.dispose()
        renderer.domElement.remove()
      }
    } catch (e) {
      console.error('[WorldCanvas] boot failed', e)
      setStatus('error')
    }
    return () => { cancelAnimationFrame(frameId); resizeObserver?.disconnect() }
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
        <div>POS <span className="text-emerald-300">{hud.x},{hud.y},{hud.z}</span></div>
        <div>QI <span className="text-emerald-300">{hud.qi}/{hud.maxQi}</span></div>
        <div>RLM <span className="text-emerald-300">{hud.realm}</span></div>
      </div>
      {/* Bottom-center lore */}
      <div className="pointer-events-none absolute bottom-4 left-1/2 z-10 -translate-x-1/2 select-none text-center font-serif text-amber-100/60">
        <p className="text-sm italic">天地不仁，以万物为刍狗</p>
        <p className="mt-1 text-[10px] uppercase tracking-[0.3em] text-amber-200/40">Heaven is impartial</p>
      </div>
      {/* Controls hint */}
      <div className="pointer-events-none absolute bottom-4 right-4 z-10 select-none rounded-md border border-amber-500/30 bg-black/50 px-3 py-2 font-mono text-[10px] text-amber-100/70 backdrop-blur-sm">
        <div>WASD move · MOUSE look · SCROLL zoom</div>
        <div>SPACE jump · SHIFT sprint</div>
        <div>F sword-flight (needs qi) · Q meditate (needs qi)</div>
      </div>
    </div>
  )
}
