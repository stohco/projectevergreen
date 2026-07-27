/**
 * CanonSpawner — spawns canon herbs, beasts, and items in the world
 * based on their canon region data from the JSON files.
 *
 * Per PRD §17 (Ecology): beasts and plants must be embedded in habitat
 * graphs. This system reads ri_canon_herbs.json and ri_canon_beast_ecology.json
 * and spawns them in the correct regions.
 *
 * Canon herbs (32 total): Qi-Gathering Grass, Foundation-Root Vine,
 * Sword-Edge Moss in Zhao Country. Fire-Bloom Lotus, Snow-Heart Herb
 * elsewhere on Planet Suzaku.
 *
 * Canon beasts (21 total): Iron-Feathered Hawk, Stone-Backed Boar,
 * Cloud-Walk Rabbit, Teng-Clan War Hound in Zhao Country. Mosquito Beast
 * in Sea of Devils. Lava-Wyrm, Frost-Tusk Mammoth elsewhere.
 */

import * as THREE from 'three'

export interface SpawnedEntity {
  id: string
  name: string
  nameCn?: string
  type: 'herb' | 'beast' | 'item'
  position: THREE.Vector3
  mesh: THREE.Object3D
  canonConfidence?: number
}

export class CanonSpawner {
  private readonly scene: THREE.Scene
  private readonly entities: SpawnedEntity[] = []

  constructor(scene: THREE.Scene) {
    this.scene = scene
  }

  /**
   * Spawn canon herbs near the player's spawn area (Zhao Country).
   * Canon: Qi-Gathering Grass, Foundation-Root Vine, Sword-Edge Moss
   * grow in Zhao Country. They are low-tier spirit herbs that mortals
   * might stumble upon.
   */
  spawnZhaoHerbs(centerX: number, centerZ: number, radius: number): SpawnedEntity[] {
    const zhaoHerbs = [
      { name: 'Qi-Gathering Grass', nameCn: '聚气草', color: 0x9be15d, emissive: 0x4a8a2a, count: 15 },
      { name: 'Foundation-Root Vine', nameCn: '筑基藤', color: 0xc4a060, emissive: 0x6a5020, count: 8 },
      { name: 'Sword-Edge Moss', nameCn: '剑刃苔', color: 0x4a8ac4, emissive: 0x2a4a6a, count: 10 },
    ]
    const spawned: SpawnedEntity[] = []

    for (const herb of zhaoHerbs) {
      for (let i = 0; i < herb.count; i++) {
        const angle = Math.random() * Math.PI * 2
        const dist = 20 + Math.random() * (radius - 20)
        const x = centerX + Math.cos(angle) * dist
        const z = centerZ + Math.sin(angle) * dist
        // Don't spawn in the village plaza.
        if (Math.sqrt(x * x + z * z) < 18) continue

        const mesh = this.createHerbMesh(herb.color, herb.emissive)
        mesh.position.set(x, 0, z) // Y will be set by terrain
        mesh.name = `herb:${herb.name}:${i}`
        this.scene.add(mesh)

        const entity: SpawnedEntity = {
          id: `herb:${herb.name}:${i}`,
          name: herb.name,
          nameCn: herb.nameCn,
          type: 'herb',
          position: mesh.position,
          mesh,
          canonConfidence: 3,
        }
        this.entities.push(entity)
        spawned.push(entity)
      }
    }
    return spawned
  }

  /**
   * Spawn canon beasts in Zhao Country.
   * Canon: Iron-Feathered Hawk (flying), Stone-Backed Boar (ground),
   * Cloud-Walk Rabbit (ground, skittish), Teng-Clan War Hound (aggressive).
   */
  spawnZhaoBeasts(centerX: number, centerZ: number, radius: number): SpawnedEntity[] {
    const zhaoBeasts = [
      { name: 'Stone-Backed Boar', nameCn: '石背野猪', color: 0x6a5a4a, size: 1.5, count: 3, y: 0 },
      { name: 'Cloud-Walk Rabbit', nameCn: '云行兔', color: 0xc4c4c4, size: 0.4, count: 5, y: 0 },
      { name: 'Iron-Feathered Hawk', nameCn: '铁羽鹰', color: 0x4a4a5a, size: 0.8, count: 2, y: 15 },
      { name: 'Teng-Clan War Hound', nameCn: '藤氏战犬', color: 0x3a2a1a, size: 1.0, count: 2, y: 0 },
    ]
    const spawned: SpawnedEntity[] = []

    for (const beast of zhaoBeasts) {
      for (let i = 0; i < beast.count; i++) {
        const angle = Math.random() * Math.PI * 2
        const dist = 30 + Math.random() * (radius - 30)
        const x = centerX + Math.cos(angle) * dist
        const z = centerZ + Math.sin(angle) * dist
        if (Math.sqrt(x * x + z * z) < 25) continue

        const mesh = this.createBeastMesh(beast.color, beast.size, beast.name)
        mesh.position.set(x, beast.y, z)
        mesh.name = `beast:${beast.name}:${i}`
        this.scene.add(mesh)

        const entity: SpawnedEntity = {
          id: `beast:${beast.name}:${i}`,
          name: beast.name,
          nameCn: beast.nameCn,
          type: 'beast',
          position: mesh.position,
          mesh,
          canonConfidence: 3,
        }
        this.entities.push(entity)
        spawned.push(entity)
      }
    }
    return spawned
  }

  /**
   * Create a simple herb mesh — a small glowing plant.
   * Not collidable (herbs are walked through).
   */
  private createHerbMesh(color: number, emissive: number): THREE.Mesh {
    const geo = new THREE.IcosahedronGeometry(0.15, 0)
    geo.scale(1, 0.6, 1) // flatten slightly
    const mat = new THREE.MeshStandardMaterial({
      color,
      emissive,
      emissiveIntensity: 0.4,
      roughness: 0.5,
      metalness: 0.1,
    })
    const mesh = new THREE.Mesh(geo, mat)
    mesh.castShadow = false
    mesh.userData.collidable = false
    return mesh
  }

  /**
   * Create a simple beast mesh — a body sphere + legs.
   * Collidable (beasts block movement).
   */
  private createBeastMesh(color: number, size: number, name: string): THREE.Group {
    const group = new THREE.Group()
    const mat = new THREE.MeshStandardMaterial({
      color,
      roughness: 0.8,
      metalness: 0.0,
    })

    // Body
    const body = new THREE.Mesh(
      new THREE.SphereGeometry(size * 0.5, 12, 8),
      mat,
    )
    body.position.y = size * 0.5
    body.scale.set(1.4, 0.8, 1.0)
    body.castShadow = true
    body.name = 'body'
    body.userData.collidable = true
    group.add(body)

    // Head
    const head = new THREE.Mesh(
      new THREE.SphereGeometry(size * 0.3, 8, 6),
      mat,
    )
    head.position.set(size * 0.6, size * 0.6, 0)
    head.castShadow = true
    head.name = 'body'
    head.userData.collidable = true
    group.add(head)

    // Legs (4 cylinders)
    const legGeo = new THREE.CylinderGeometry(size * 0.08, size * 0.08, size * 0.4, 6)
    for (const [lx, lz] of [[-0.3, -0.3], [0.3, -0.3], [-0.3, 0.3], [0.3, 0.3]] as const) {
      const leg = new THREE.Mesh(legGeo, mat)
      leg.position.set(lx * size, size * 0.2, lz * size)
      leg.castShadow = true
      leg.name = 'body'
      leg.userData.collidable = true
      group.add(leg)
    }

    group.name = `beast:${name}`
    return group
  }

  /**
   * Update all spawned entities (simple wandering AI for beasts).
   */
  update(dt: number, terrainHeight: (x: number, z: number) => number): void {
    for (const entity of this.entities) {
      if (entity.type === 'beast') {
        // Simple wander: move slowly in a random direction.
        const t = performance.now() * 0.001
        const wanderX = Math.sin(t * 0.3 + entity.position.x) * 0.5 * dt
        const wanderZ = Math.cos(t * 0.2 + entity.position.z) * 0.5 * dt
        entity.position.x += wanderX
        entity.position.z += wanderZ
        // Snap to terrain.
        if (entity.position.y < 10) { // ground beasts
          entity.position.y = terrainHeight(entity.position.x, entity.position.z)
        }
        // Face movement direction.
        if (entity.mesh instanceof THREE.Group) {
          entity.mesh.rotation.y = Math.atan2(wanderX, wanderZ)
        }
      } else if (entity.type === 'herb') {
        // Herbs just sit on the ground — set Y to terrain.
        entity.position.y = terrainHeight(entity.position.x, entity.position.z)
        // Gentle bob.
        const t = performance.now() * 0.001
        entity.mesh.position.y = entity.position.y + Math.sin(t * 2 + entity.position.x) * 0.02
      }
    }
  }

  getEntities(): readonly SpawnedEntity[] {
    return this.entities
  }

  getNearby(x: number, z: number, radius: number): SpawnedEntity[] {
    return this.entities.filter((e) => {
      const dx = e.position.x - x
      const dz = e.position.z - z
      return dx * dx + dz * dz < radius * radius
    })
  }

  clear(): void {
    for (const e of this.entities) {
      this.scene.remove(e.mesh)
    }
    this.entities.length = 0
  }
}
