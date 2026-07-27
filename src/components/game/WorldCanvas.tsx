'use client'

import { useEffect, useRef, useState } from 'react'
import * as THREE from 'three'
import { ChunkManager } from '@/engine/voxels/ChunkManager'
import { createSky, type SkyHandle } from '@/engine/render/SkySystem'
import { createPostFX, type PostFXHandle } from '@/engine/render/PostProcessing'
import { createCultivatorModel, type CultivatorModelHandle } from '@/engine/entities/CultivatorModel'
import { createPlayer, type PlayerHandle } from '@/engine/entities/PlayerEntity'
import { BlockId } from '@/engine/voxels/BlockRegistry'
import { raycastVoxels } from '@/engine/voxels/VoxelRaycaster'
import { WorldGraph } from '@/engine/graph/WorldGraph'
import { GraphQueryService } from '@/engine/graph/GraphQueryService'
import { bootstrapGraphFromCanon } from '@/engine/graph/CanonGraphLoader'

/**
 * WorldCanvas — the Er Gen Verse Three.js mount.
 *
 * CRITICAL: The player is NOT Wang Lin. The player is a first-class actor
 * (ivory-robed traveler, Qi Condensation). Wang Lin exists in the world as
 * a manifestation NPC (jade-green robes, Foundation realm) — his real self
 * is on the Immortal Astral Continent (仙罡大陆). The player encounters
 * Wang Lin's manifestation.
 *
 * Camera: third-person, behind the player avatar.
 * Controls: WASD move, mouse look, SPACE jump/fly up, SHIFT sprint,
 *           F toggle flight (御剑飞行, costs qi).
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
    qi: 100,
    maxQi: 100,
    realm: 'Qi Condensation',
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
    let player: PlayerHandle
    let wanglinNpc: CultivatorModelHandle
    let graph: WorldGraph
    let graphQuery: GraphQueryService
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
      renderer.toneMappingExposure = 1.2
      renderer.shadowMap.enabled = true
      renderer.shadowMap.type = THREE.PCFSoftShadowMap
      renderer.outputColorSpace = THREE.SRGBColorSpace
      container.appendChild(renderer.domElement)

      scene = new THREE.Scene()
      // Soft atmospheric fog — not too dense, lets terrain colors through.
      scene.fog = new THREE.FogExp2(0xc8d6e8, 0.0008)

      camera = new THREE.PerspectiveCamera(
        65,
        container.clientWidth / container.clientHeight,
        0.1,
        4000,
      )
      camera.position.set(4, 73, 14)
      camera.lookAt(4, 66, 8)

      // Sky.
      sky = createSky(scene)

      // Voxel world.
      chunkMgr = new ChunkManager(scene)
      chunkMgr.updateCamera(camera.position.x, camera.position.z)

      // Post-FX — subtle, not crushing.
      postFX = createPostFX(renderer, scene, camera, container.clientWidth, container.clientHeight, {
        ssao: false,
        bloom: true,
        bloomStrength: 0.3,
        chromaticAberration: false,
        vignette: true,
        grain: false,
        colorGrade: true,
      })

      // ---- PLAYER (the user, NOT Wang Lin) ----
      // Ivory white robes, Qi Condensation, starts near the village plaza.
      // Start at y=70 (well above the y=64 platform) so gravity settles
      // the player onto the surface, not inside it.
      player = createPlayer({
        name: 'Traveler',
        nameCn: '行道者',
        spiritRoot: 'wood',
        startPosition: [4, 70, 8],
      })
      player.setAnimation('idle')
      scene.add(player.group)

      // ---- WANG LIN (manifestation NPC) ----
      // Jade green robes, Foundation realm. Stands in the village plaza.
      // His real self is on the Immortal Astral Continent (仙罡大陆).
      wanglinNpc = createCultivatorModel('foundation', false)
      wanglinNpc.group.position.set(0, 64, 0)
      wanglinNpc.group.scale.setScalar(1.2)
      wanglinNpc.setAnimation('idle')
      wanglinNpc.setAuraVisible(false)
      scene.add(wanglinNpc.group)

      // Soft ambient + hemisphere light (no harsh specular).
      const ambient = new THREE.AmbientLight(0xbcd6ff, 0.4)
      scene.add(ambient)

      const hemi = new THREE.HemisphereLight(0xbcd6ff, 0x4a3520, 0.6)
      scene.add(hemi)

      // Spirit-vein glow point light near the cliff of 天逆珠.
      const veinLight = new THREE.PointLight(0x9be15d, 1.5, 40, 1.8)
      veinLight.position.set(-80, 68, -120)
      scene.add(veinLight)

      // ---- WorldGraph + GraphQueryService ----
      graph = new WorldGraph()
      graphQuery = new GraphQueryService(graph)
      // Bootstrap asynchronously (fetches /ri_canon_database.json).
      bootstrapGraphFromCanon(graph).then((count) => {
        console.log('[WorldCanvas] graph bootstrapped:', count, 'nodes')
        ;(globalThis as { __ergenGraph?: unknown }).__ergenGraph = graph
        ;(globalThis as { __ergenGraphQuery?: unknown }).__ergenGraphQuery = graphQuery
      }).catch((e) => {
        console.error('[WorldCanvas] graph bootstrap failed', e)
      })

      setStatus('live')

      // ---- Input: third-person camera + player movement ----
      const onKeyDown = (e: KeyboardEvent) => {
        keys[e.code] = true
        if (e.code === 'Space') e.preventDefault()
        // Toggle flight.
        if (e.code === 'KeyF' && !player.state.isFlying) {
          if (player.consumeQi(10)) {
            player.setFlying(true)
            console.log('[Player] sword-flight engaged (御剑飞行)')
          }
        } else if (e.code === 'KeyF' && player.state.isFlying) {
          player.setFlying(false)
        }
        // Toggle meditation.
        if (e.code === 'KeyQ') {
          player.setMeditating(!player.state.isMeditating)
        }
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
        const yaw = (camera.userData.yaw ?? 0) - e.movementX * 0.003
        const pitch = (camera.userData.pitch ?? -0.2) - e.movementY * 0.003
        camera.userData.yaw = yaw
        camera.userData.pitch = Math.max(-1.2, Math.min(0.8, pitch))
      }
      window.addEventListener('keydown', onKeyDown)
      window.addEventListener('keyup', onKeyUp)
      renderer.domElement.addEventListener('click', onClick)
      document.addEventListener('pointerlockchange', onPointerLockChange)
      document.addEventListener('mousemove', onMouseMove)

      camera.userData.yaw = 0
      camera.userData.pitch = -0.2

      // ---- Main loop ----
      const clock = new THREE.Clock()
      let fpsAccum = 0
      let fpsFrames = 0
      let fpsTimer = 0
      let hudTimer = 0
      let wanglinYaw = 0

      const animate = () => {
        frameId = requestAnimationFrame(animate)
        const dt = Math.min(clock.getDelta(), 0.1)

        // ---- Player movement ----
        const speed = (player.state.isFlying ? 40 : (keys['ShiftLeft'] ? 12 : 6)) * dt
        const yaw = camera.userData.yaw ?? 0
        const fwd = new THREE.Vector3(Math.sin(yaw), 0, Math.cos(yaw))
        const right = new THREE.Vector3(fwd.z, 0, -fwd.x)

        let moved = false
        if (keys['KeyW']) { player.state.position.addScaledVector(fwd, speed); moved = true }
        if (keys['KeyS']) { player.state.position.addScaledVector(fwd, -speed); moved = true }
        if (keys['KeyA']) { player.state.position.addScaledVector(right, -speed); moved = true }
        if (keys['KeyD']) { player.state.position.addScaledVector(right, speed); moved = true }

        if (player.state.isFlying) {
          if (keys['Space']) { player.state.position.y += speed; moved = true }
          if (keys['KeyC']) { player.state.position.y -= speed; moved = true }
          // Flight costs qi.
          if (!player.consumeQi(2.0 * dt)) {
            player.setFlying(false) // out of qi, stop flying
          }
        } else {
          // Ground movement: simple gravity + surface snap.
          // Check the block at the player's feet level.
          const feetBlock = chunkMgr.getBlock(
            Math.floor(player.state.position.x),
            Math.floor(player.state.position.y),
            Math.floor(player.state.position.z),
          )
          const belowFeet = chunkMgr.getBlock(
            Math.floor(player.state.position.x),
            Math.floor(player.state.position.y) - 1,
            Math.floor(player.state.position.z),
          )
          if (feetBlock !== BlockId.AIR) {
            // Inside a solid block — push up.
            player.state.position.y = Math.floor(player.state.position.y) + 1
          } else if (belowFeet === BlockId.AIR) {
            // Falling.
            player.state.position.y -= 20 * dt
          } else {
            // Standing on surface — snap to integer.
            player.state.position.y = Math.floor(player.state.position.y) + 0.0
          }
          if (keys['Space']) {
            // Jump.
            player.state.position.y += 1.5
          }
        }

        // Player yaw follows camera yaw.
        player.setYaw(yaw + Math.PI) // face away from camera (third-person)

        // Player animation.
        if (player.state.isMeditating) {
          player.setAnimation('cast')
        } else if (player.state.isFlying) {
          player.setAnimation('fly')
        } else if (moved) {
          player.setAnimation(keys['ShiftLeft'] ? 'run' : 'walk')
        } else {
          player.setAnimation('idle')
        }

        player.update(dt)

        // ---- Wang Lin NPC idle (slowly rotates, meditates occasionally) ----
        wanglinYaw += dt * 0.2
        wanglinNpc.setYaw(wanglinYaw)
        wanglinNpc.update(dt)

        // ---- Third-person camera ----
        // Camera follows behind and above the player.
        const camDistance = 6
        const camHeight = 3
        const pitch = camera.userData.pitch ?? -0.2
        const camOffset = new THREE.Vector3(
          -Math.sin(yaw) * Math.cos(pitch) * camDistance,
          -Math.sin(pitch) * camDistance + camHeight,
          -Math.cos(yaw) * Math.cos(pitch) * camDistance,
        )
        const targetCamPos = player.state.position.clone().add(camOffset)
        camera.position.lerp(targetCamPos, 0.15) // smooth follow
        camera.lookAt(player.state.position.x, player.state.position.y + 1.5, player.state.position.z)

        // ---- Update systems ----
        chunkMgr.updateCamera(camera.position.x, camera.position.z)
        chunkMgr.update(dt)
        sky.update(dt)

        postFX.update(dt)
        postFX.composer.render()

        // ---- FPS counter + HUD ----
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
              x: Math.floor(player.state.position.x),
              y: Math.floor(player.state.position.y),
              z: Math.floor(player.state.position.z),
              biome: chunkMgr.getChunkAt(player.state.position.x, player.state.position.z)?.biomeTag ?? 'unknown',
              qi: Math.floor(player.state.qi),
              maxQi: player.state.maxQi,
              realm: player.state.realm.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase()),
            })
          }
        }
      }

      // ---- Village structures: spawn after chunks load ----
      const villageSpawnedRef = { done: false }
      const trySpawnVillage = () => {
        if (villageSpawnedRef.done) return
        const centerChunk = chunkMgr.getChunkAt(0, 0)
        if (!centerChunk) return
        villageSpawnedRef.done = true
        // Meditation platform: 5x5 jade brick at y=64.
        for (let dx = -2; dx <= 2; dx++) {
          for (let dz = -2; dz <= 2; dz++) {
            chunkMgr.setBlock(dx, 64, dz, BlockId.JADE_BRICKS)
          }
        }
        // Four stone lanterns at the platform corners.
        const lanternPositions: Array<[number, number]> = [[-6, -6], [6, -6], [-6, 6], [6, 6]]
        for (const [lx, lz] of lanternPositions) {
          chunkMgr.setBlock(lx, 64, lz, BlockId.COBBLESTONE)
          chunkMgr.setBlock(lx, 65, lz, BlockId.COBBLESTONE)
          chunkMgr.setBlock(lx, 66, lz, BlockId.JADE_BRICKS)
          chunkMgr.setBlock(lx, 67, lz, BlockId.JADE_BRICKS)
          chunkMgr.setBlock(lx, 68, lz, BlockId.BLUE_TILE)
        }
        // Spirit-pine windbreak to the north.
        for (let dx = -8; dx <= 8; dx += 2) {
          chunkMgr.setBlock(dx, 64, -10, BlockId.PINE_WOOD)
          chunkMgr.setBlock(dx, 65, -10, BlockId.PINE_WOOD)
          chunkMgr.setBlock(dx, 66, -10, BlockId.PINE_WOOD)
          chunkMgr.setBlock(dx, 67, -10, BlockId.PINE_LEAVES)
          chunkMgr.setBlock(dx, 68, -10, BlockId.PINE_LEAVES)
        }
      }
      const villageInterval = setInterval(trySpawnVillage, 200)

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

      // ---- Bridge to HUD ----
      // CRITICAL: player is NOT Wang Lin. Player name is "Traveler" (行道者).
      // Wang Lin is an NPC.
      const bridge = {
        getState() {
          return {
            player: {
              name: player.state.name,
              nameCn: player.state.nameCn,
              realm: player.state.realm.replace(/_/g, ' '),
              realmCn: '凝气期',
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
              biome: chunkMgr.getChunkAt(player.state.position.x, player.state.position.z)?.biomeTag ?? 'plains',
              nearbyActors: [
                { name: 'Wang Lin (Manifestation)', nameCn: '王林 (化身)', realm: 'Foundation Establishment', hostility: 0, distance: Math.floor(player.state.position.distanceTo(wanglinNpc.group.position)) },
              ],
              nearbyThreats: [],
              spiritVeinNear: player.state.position.distanceTo(new THREE.Vector3(-80, 0, -120)) < 100,
            },
            debug: {
              fps: hud.fps,
              frameTime: 16.6,
              chunks: chunkMgr.loadedCount(),
              tris: chunkMgr.triangleCount(),
              pos: [player.state.position.x, player.state.position.y, player.state.position.z],
              yaw: (camera.userData.yaw ?? 0),
              pitch: (camera.userData.pitch ?? 0),
              biome: chunkMgr.getChunkAt(player.state.position.x, player.state.position.z)?.biomeTag ?? 'unknown',
              mem: 0,
              entities: 2, // player + Wang Lin
              drawCalls: 0,
            },
          }
        },
        saveWorld(_key: string) { /* Future: serialize WorldDeltaStore */ },
        loadWorld(_key: string) { /* Future: deserialize WorldDeltaStore */ },
        onTick(_cb: () => void) { return () => {} },
      }
      ;(globalThis as { __ergenBridge?: unknown }).__ergenBridge = bridge

      // Block placement / removal on mouse buttons (player writes through facade).
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
          // Player breaks block (PLAYER provenance).
          chunkMgr.setBlock(hit.block.x, hit.block.y, hit.block.z, BlockId.AIR)
        } else if (e.button === 2) {
          // Player places block (PLAYER provenance).
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
        clearInterval(villageInterval)
        resizeObserver?.disconnect()
        window.removeEventListener('keydown', onKeyDown)
        window.removeEventListener('keyup', onKeyUp)
        renderer.domElement.removeEventListener('click', onClick)
        renderer.domElement.removeEventListener('mousedown', onMouseDown)
        document.removeEventListener('pointerlockchange', onPointerLockChange)
        document.removeEventListener('mousemove', onMouseMove)
        sky.dispose()
        player.dispose()
        wanglinNpc.dispose()
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
        <div>QI <span className="text-emerald-300">{hud.qi}/{hud.maxQi}</span></div>
        <div>RLM <span className="text-emerald-300">{hud.realm}</span></div>
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
        <div>SPACE jump/fly · SHIFT sprint</div>
        <div>F sword-flight (御剑飞行, 2qi/s)</div>
        <div>Q meditate (3x qi regen)</div>
        <div>L-CLICK break · R-CLICK place</div>
      </div>
    </div>
  )
}
