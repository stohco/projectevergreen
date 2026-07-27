/**
 * QiField — the spiritual energy field that permeates the Er Gen world.
 *
 * Implements the advection-diffusion PDE:
 *
 *   ∂q/∂t = -∇·(uq) + D∇²q + S(x,t) - R(x,t)
 *
 * Where:
 *   q(x,t)  = scalar qi concentration at position x, time t
 *   u(x,t)  = flow velocity (advection — qi flows from high to low)
 *   D       = diffusion coefficient (qi spreads naturally)
 *   S(x,t)  = source term (spirit veins emit qi)
 *   R(x,t)  = sink term (cultivators absorb qi during cultivation)
 *
 * This is the math of the Er Gen multiverse: qi is a field, not a resource.
 * Spirit veins are sources. Cultivators are sinks. Formations are boundary
 * conditions that amplify or dampen the field. The field drives:
 *   - Cultivation speed (higher local qi = faster regen)
 *   - Beast spawning (beasts gather in qi-rich areas)
 *   - Herb growth (spirit herbs only grow where qi > threshold)
 *   - Formation power (formations tap the local qi field)
 *   - Tribulation events (qi field instability triggers lightning)
 *
 * The field is discretized on a 2D grid (x,z) at terrain height. 3D
 * would be more accurate but 2D is sufficient for gameplay and 10x faster.
 */

// ---- Types ---------------------------------------------------------------

export interface QiCell {
  /** Qi concentration (0.0 = depleted, 1.0 = saturated). */
  q: number
  /** Flow velocity x-component. */
  ux: number
  /** Flow velocity z-component. */
  uz: number
  /** Source rate (qi emitted per second). Spirit veins = high. */
  source: number
  /** Sink rate (qi absorbed per second). Cultivators = high. */
  sink: number
  /** Diffusion coefficient (how fast qi spreads from this cell). */
  diffusion: number
}

export interface SpiritVeinSource {
  /** World X position. */
  x: number
  /** World Z position. */
  z: number
  /** Qi emission rate (0-1 per second). */
  rate: number
  /** Radius of influence (meters). */
  radius: number
  /** Element affinity (affects flow direction). */
  element: 'fire' | 'water' | 'wood' | 'metal' | 'earth' | 'lightning' | 'void'
}

export interface CultivatorSink {
  /** World X position. */
  x: number
  /** World Z position. */
  z: number
  /** Qi absorption rate (0-1 per second). */
  rate: number
  /** Radius of absorption (meters). */
  radius: number
}

export interface FormationBoundary {
  /** Center X. */
  x: number
  /** Center Z. */
  z: number
  /** Inner radius (amplification zone). */
  innerRadius: number
  /** Outer radius (dampening zone). */
  outerRadius: number
  /** Amplification factor inside inner radius (>1 = amplify, <1 = dampen). */
  amplify: number
  /** Dampening factor in outer zone (<1 = dampen). */
  dampen: number
}

// ---- QiField implementation ----------------------------------------------

export class QiField {
  /** Grid resolution (cells per meter). */
  private readonly resolution: number
  /** Grid size in cells. */
  private readonly gridSize: number
  /** World origin offset (grid[0][0] = world (originX, originZ)). */
  private readonly originX: number
  private readonly originZ: number
  /** The qi field grid. */
  private readonly grid: QiCell[][]
  /** Registered spirit vein sources. */
  private readonly sources: SpiritVeinSource[] = []
  /** Registered cultivator sinks. */
  private readonly sinks: CultivatorSink[] = []
  /** Registered formation boundaries. */
  private readonly formations: FormationBoundary[] = []

  constructor(opts: {
    resolution?: number
    worldSize: number
    originX?: number
    originZ?: number
  }) {
    this.resolution = opts.resolution ?? 1 // 1 cell per meter
    this.gridSize = Math.ceil(opts.worldSize * this.resolution)
    this.originX = opts.originX ?? -opts.worldSize / 2
    this.originZ = opts.originZ ?? -opts.worldSize / 2
    this.grid = []
    for (let i = 0; i < this.gridSize; i++) {
      this.grid[i] = []
      for (let j = 0; j < this.gridSize; j++) {
        this.grid[i][j] = {
          q: 0.1, // ambient qi baseline
          ux: 0, uz: 0,
          source: 0, sink: 0,
          diffusion: 0.1,
        }
      }
    }
  }

  addSource(source: SpiritVeinSource): void {
    this.sources.push(source)
  }

  addSink(sink: CultivatorSink): void {
    this.sinks.push(sink)
  }

  addFormation(formation: FormationBoundary): void {
    this.formations.push(formation)
  }

  /**
   * Step the qi field forward by dt seconds.
   *
   * ∂q/∂t = -∇·(uq) + D∇²q + S - R
   *
   * Discretized using finite differences on the 2D grid:
   *   advection: -u·∇q (upwind scheme)
   *   diffusion: D·(q[i+1] + q[i-1] + q[j+1] + q[j-1] - 4q) / h²
   *   source: S(x,t)
   *   sink: R(x,t)
   *
   * Stability: dt < h² / (4D) for diffusion. We clamp dt to ensure stability.
   */
  step(dt: number): void {
    // Update sources and sinks.
    this.updateSourcesAndSinks()

    // Clamp dt for stability.
    const h = 1 / this.resolution
    const maxDt = (h * h) / (4 * 0.5) // assume max diffusion = 0.5
    const steps = Math.max(1, Math.ceil(dt / maxDt))
    const subDt = dt / steps

    for (let s = 0; s < steps; s++) {
      this.subStep(subDt, h)
    }
  }

  private subStep(dt: number, h: number): void {
    const n = this.gridSize
    // Create a copy of the current field for reading.
    const prev: number[][] = []
    for (let i = 0; i < n; i++) {
      prev[i] = []
      for (let j = 0; j < n; j++) {
        prev[i][j] = this.grid[i][j].q
      }
    }

    for (let i = 1; i < n - 1; i++) {
      for (let j = 1; j < n - 1; j++) {
        const cell = this.grid[i][j]
        const q = prev[i][j]

        // Advection: -u·∇q (upwind scheme — stable).
        const ux = cell.ux
        const uz = cell.uz
        const dqdx = ux > 0 ? (q - prev[i - 1][j]) / h : (prev[i + 1][j] - q) / h
        const dqdz = uz > 0 ? (q - prev[i][j - 1]) / h : (prev[i][j + 1] - q) / h
        const advection = -(ux * dqdx + uz * dqdz)

        // Diffusion: D·∇²q (Laplacian).
        const laplacian = (prev[i + 1][j] + prev[i - 1][j] + prev[i][j + 1] + prev[i][j - 1] - 4 * q) / (h * h)
        const diffusion = cell.diffusion * laplacian

        // Source - Sink.
        const sourceMinusSink = cell.source - cell.sink

        // Update: ∂q/∂t = advection + diffusion + source - sink
        cell.q = Math.max(0, Math.min(1, q + dt * (advection + diffusion + sourceMinusSink)))
      }
    }

    // Apply formation boundaries (amplification/dampening).
    for (const f of this.formations) {
      const fi = Math.floor((f.x - this.originX) * this.resolution)
      const fj = Math.floor((f.z - this.originZ) * this.resolution)
      const rInner = f.innerRadius * this.resolution
      const rOuter = f.outerRadius * this.resolution
      for (let di = -Math.ceil(rOuter); di <= Math.ceil(rOuter); di++) {
        for (let dj = -Math.ceil(rOuter); dj <= Math.ceil(rOuter); dj++) {
          const ni = fi + di
          const nj = fj + dj
          if (ni < 0 || ni >= n || nj < 0 || nj >= n) continue
          const dist = Math.sqrt(di * di + dj * dj)
          if (dist < rInner) {
            this.grid[ni][nj].q *= f.amplify
          } else if (dist < rOuter) {
            this.grid[ni][nj].q *= f.dampen
          }
        }
      }
    }
  }

  private updateSourcesAndSinks(): void {
    const n = this.gridSize
    // Reset sources and sinks.
    for (let i = 0; i < n; i++) {
      for (let j = 0; j < n; j++) {
        this.grid[i][j].source = 0
        this.grid[i][j].sink = 0
        this.grid[i][j].ux = 0
        this.grid[i][j].uz = 0
      }
    }

    // Apply spirit vein sources.
    for (const s of this.sources) {
      const si = Math.floor((s.x - this.originX) * this.resolution)
      const sj = Math.floor((s.z - this.originZ) * this.resolution)
      const r = s.radius * this.resolution
      for (let di = -Math.ceil(r); di <= Math.ceil(r); di++) {
        for (let dj = -Math.ceil(r); dj <= Math.ceil(r); dj++) {
          const ni = si + di
          const nj = sj + dj
          if (ni < 0 || ni >= n || nj < 0 || nj >= n) continue
          const dist = Math.sqrt(di * di + dj * dj)
          if (dist > r) continue
          const falloff = 1 - dist / r
          this.grid[ni][nj].source += s.rate * falloff * falloff
          // Flow toward the vein center (element attraction).
          this.grid[ni][nj].ux -= (di / Math.max(1, dist)) * s.rate * 0.1
          this.grid[ni][nj].uz -= (dj / Math.max(1, dist)) * s.rate * 0.1
        }
      }
    }

    // Apply cultivator sinks.
    for (const s of this.sinks) {
      const si = Math.floor((s.x - this.originX) * this.resolution)
      const sj = Math.floor((s.z - this.originZ) * this.resolution)
      const r = s.radius * this.resolution
      for (let di = -Math.ceil(r); di <= Math.ceil(r); di++) {
        for (let dj = -Math.ceil(r); dj <= Math.ceil(r); dj++) {
          const ni = si + di
          const nj = sj + dj
          if (ni < 0 || ni >= n || nj < 0 || nj >= n) continue
          const dist = Math.sqrt(di * di + dj * dj)
          if (dist > r) continue
          const falloff = 1 - dist / r
          this.grid[ni][nj].sink += s.rate * falloff * falloff
        }
      }
    }
  }

  /**
   * Sample the qi concentration at a world position.
   * Used by: cultivation speed, herb growth, beast spawning, formation power.
   */
  sampleQi(worldX: number, worldZ: number): number {
    const i = Math.floor((worldX - this.originX) * this.resolution)
    const j = Math.floor((worldZ - this.originZ) * this.resolution)
    if (i < 0 || i >= this.gridSize || j < 0 || j >= this.gridSize) return 0.1
    return this.grid[i][j].q
  }

  /**
   * Sample the qi flow direction at a world position.
   * Used by: particle effects (qi motes follow flow), formation alignment.
   */
  sampleFlow(worldX: number, worldZ: number): { ux: number; uz: number } {
    const i = Math.floor((worldX - this.originX) * this.resolution)
    const j = Math.floor((worldZ - this.originZ) * this.resolution)
    if (i < 0 || i >= this.gridSize || j < 0 || j >= this.gridSize) return { ux: 0, uz: 0 }
    return { ux: this.grid[i][j].ux, uz: this.grid[i][j].uz }
  }

  /**
   * Register the player as a qi sink (cultivators absorb ambient qi).
   * Call this each frame with the player's current position.
   */
  updatePlayerSink(x: number, z: number, absorptionRate: number): void {
    // Remove old player sink (last in list).
    if (this.sinks.length > 0 && this.sinks[this.sinks.length - 1].rate > 0) {
      this.sinks.pop()
    }
    this.sinks.push({ x, z, rate: absorptionRate, radius: 5 })
  }

  /**
   * Get the qi field as a texture for visualization (debug overlay).
   * Returns a Uint8Array of size gridSize×gridSize with qi values 0-255.
   */
  toTextureData(): Uint8Array {
    const data = new Uint8Array(this.gridSize * this.gridSize)
    for (let i = 0; i < this.gridSize; i++) {
      for (let j = 0; j < this.gridSize; j++) {
        data[i * this.gridSize + j] = Math.floor(this.grid[i][j].q * 255)
      }
    }
    return data
  }

  getGridSize(): number {
    return this.gridSize
  }
}
