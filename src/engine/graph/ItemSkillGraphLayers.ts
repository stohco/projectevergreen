/**
 * ItemSkillGraphLayers — the item/skill/technique/artifact/crafting graph
 * projections per ITEM_SKILL_GRAPHS.md.
 *
 * Rule: "If the player can own it, learn it, equip it, consume it, craft
 * it, drop it, trade it, or unlock it, it must have a graph projection
 * and a runtime path."
 *
 * Each layer follows the GraphLayer interface: build, query, write,
 * invalidate, explain.
 *
 * Data sources:
 *   - ri_canon_database.json: 178 artifacts, 214 techniques
 *   - ri_canon_artifacts_enriched.json: 177 artifacts with chapter data
 *   - ri_canon_techniques_enriched.json: 184 techniques with effects
 *   - ALCHEMY_REFERENCE.ts: alchemy recipes
 *   - FORMATIONS_REFERENCE.ts: formation arrays
 *   - TALISMANS_REFERENCE.ts: talisman types
 *   - REFINING_REFERENCE.ts: refinement processes
 *   - WEAPON_CATALOG_REFERENCE.ts: weapon catalog
 */

import type { GraphLayer, LayerBuildInput, LayerQuery, LayerQueryResult, LayerDelta, InvalidateTarget, LayerExplanation } from './GraphLayerSystem'
import type { GraphNode } from './WorldGraph'

// ---- Item Graph (Layer 12a) ---------------------------------------------

export type ItemType =
  | 'weapon' | 'armor' | 'accessory' | 'consumable' | 'material'
  | 'tool' | 'quest_item' | 'key_item' | 'misc'

export type Rarity = 'common' | 'uncommon' | 'rare' | 'epic' | 'legendary' | 'divine'

export interface ItemNode {
  id: string
  name: string
  nameCn?: string
  type: ItemType
  rarity: Rarity
  provenance: 'CANON' | 'SIMULATION' | 'PLAYER'
  ownerId?: string
  binding?: 'none' | 'soul_bound' | 'faction_bound'
  material?: string
  durability?: number
  maxDurability?: number
  description?: string
  canonConfidence?: number
  hiddenCapabilities?: string[]
}

export class ItemGraphLayer implements GraphLayer {
  name = 'item'
  index = 31
  private items: Map<string, ItemNode> = new Map()

  build(input: LayerBuildInput): void {
    // Bootstrap from canon graph item nodes.
    for (const node of input.canonGraph.nodesByType('item')) {
      const item: ItemNode = {
        id: node.id,
        name: node.displayName,
        nameCn: node.displayNameCn,
        type: this.classifyItemType(node.meta?.type as string),
        rarity: this.classifyRarity(node.meta),
        provenance: 'CANON',
        ownerId: node.meta?.owner as string,
        binding: 'none',
        description: node.meta?.description as string,
        canonConfidence: node.meta?.confidence as number,
      }
      this.items.set(item.id, item)
    }
    console.log(`[ItemGraph] ${this.items.size} items loaded`)
  }

  query(q: LayerQuery): LayerQueryResult {
    if (q.type === 'findByType') {
      const items = Array.from(this.items.values())
      return { nodes: [], edges: [], metadata: { items } }
    }
    if (q.type === 'actorView' && q.nodeId) {
      // Return items owned by this actor.
      const owned = Array.from(this.items.values()).filter((i) => i.ownerId === q.nodeId)
      return { nodes: [], edges: [], metadata: { items: owned } }
    }
    return { nodes: [], edges: [] }
  }

  write(delta: LayerDelta): void {
    if (delta.type === 'addNode' && delta.data) {
      const item: ItemNode = {
        id: delta.data.id as string,
        name: delta.data.name as string,
        type: delta.data.type as ItemType ?? 'misc',
        rarity: delta.data.rarity as Rarity ?? 'common',
        provenance: delta.provenance,
        ownerId: delta.data.ownerId as string,
        binding: 'none',
      }
      this.items.set(item.id, item)
    }
    if (delta.type === 'updateNode' && delta.nodeId && delta.data) {
      const item = this.items.get(delta.nodeId)
      if (item) {
        if (delta.data.ownerId !== undefined) item.ownerId = delta.data.ownerId as string
        if (delta.data.durability !== undefined) item.durability = delta.data.durability as number
      }
    }
  }

  invalidate(_target: InvalidateTarget): void {}

  explain(nodeId: string): LayerExplanation {
    const item = this.items.get(nodeId)
    return {
      nodeId,
      layer: this.name,
      state: item ? `${item.name} (${item.rarity} ${item.type}) owner=${item.ownerId ?? 'none'}` : 'unknown',
      provenance: item?.provenance ?? 'unknown',
    }
  }

  private classifyItemType(type: string): ItemType {
    if (!type) return 'misc'
    const t = type.toLowerCase()
    if (t.includes('sword') || t.includes('weapon') || t.includes('blade')) return 'weapon'
    if (t.includes('armor') || t.includes('robe')) return 'armor'
    if (t.includes('pill') || t.includes('consumable')) return 'consumable'
    if (t.includes('material') || t.includes('ore') || t.includes('herb')) return 'material'
    if (t.includes('tool') || t.includes('ring') || t.includes('gourd')) return 'tool'
    if (t.includes('quest') || t.includes('key')) return 'quest_item'
    return 'misc'
  }

  private classifyRarity(meta: Record<string, unknown> | undefined): Rarity {
    const conf = meta?.confidence as number ?? 3
    if (conf >= 5) return 'legendary'
    if (conf >= 4) return 'epic'
    if (conf >= 3) return 'rare'
    return 'uncommon'
  }
}

// ---- Inventory Graph (Layer 12b) ----------------------------------------

export interface InventoryEntry {
  containerId: string
  containerType: 'bag' | 'storage_ring' | 'chest' | 'corpse' | 'ground' | 'sect_vault'
  ownerId?: string
  capacity: number
  items: Array<{ itemId: string; quantity: number; slot: number }>
}

export class InventoryGraphLayer implements GraphLayer {
  name = 'inventory'
  index = 32
  private containers: Map<string, InventoryEntry> = new Map()

  build(_input: LayerBuildInput): void {
    // Create a default inventory for the player.
    this.containers.set('player_bag', {
      containerId: 'player_bag',
      containerType: 'bag',
      ownerId: 'player',
      capacity: 20,
      items: [],
    })
  }

  query(q: LayerQuery): LayerQueryResult {
    if (q.type === 'actorView' && q.nodeId) {
      const owned = Array.from(this.containers.values()).filter((c) => c.ownerId === q.nodeId)
      return { nodes: [], edges: [], metadata: { containers: owned } }
    }
    return { nodes: [], edges: [] }
  }

  write(delta: LayerDelta): void {
    if (delta.type === 'addNode' && delta.data) {
      const entry: InventoryEntry = {
        containerId: delta.data.containerId as string,
        containerType: delta.data.containerType as InventoryEntry['containerType'],
        ownerId: delta.data.ownerId as string,
        capacity: delta.data.capacity as number ?? 20,
        items: [],
      }
      this.containers.set(entry.containerId, entry)
    }
    if (delta.type === 'updateNode' && delta.nodeId === 'addItem' && delta.data) {
      const container = this.containers.get(delta.data.containerId as string)
      if (container) {
        container.items.push({
          itemId: delta.data.itemId as string,
          quantity: delta.data.quantity as number ?? 1,
          slot: container.items.length,
        })
      }
    }
  }

  invalidate(_target: InvalidateTarget): void {}

  explain(nodeId: string): LayerExplanation {
    const container = this.containers.get(nodeId)
    return {
      nodeId,
      layer: this.name,
      state: container ? `${container.containerType} (${container.items.length}/${container.capacity})` : 'unknown',
      provenance: 'SIMULATION',
    }
  }

  addItem(containerId: string, itemId: string, quantity: number = 1): boolean {
    const container = this.containers.get(containerId)
    if (!container) return false
    if (container.items.length >= container.capacity) return false
    container.items.push({ itemId, quantity, slot: container.items.length })
    return true
  }

  removeItem(containerId: string, itemId: string): boolean {
    const container = this.containers.get(containerId)
    if (!container) return false
    const idx = container.items.findIndex((i) => i.itemId === itemId)
    if (idx === -1) return false
    container.items.splice(idx, 1)
    return true
  }
}

// ---- Equipment Graph (Layer 12c) ----------------------------------------

export type EquipmentSlot =
  | 'weapon' | 'robe' | 'accessory_left' | 'accessory_right'
  | 'talisman_1' | 'talisman_2' | 'talisman_3' | 'storage'

export interface EquipmentState {
  actorId: string
  slots: Map<EquipmentSlot, string | null> // slot → itemId
}

export class EquipmentGraphLayer implements GraphLayer {
  name = 'equipment'
  index = 33
  private actors: Map<string, EquipmentState> = new Map()

  build(input: LayerBuildInput): void {
    // Create equipment state for the player.
    const player = this.actors.get('player') ?? {
      actorId: 'player',
      slots: new Map<EquipmentSlot, string | null>(),
    }
    for (const slot of ['weapon', 'robe', 'accessory_left', 'accessory_right', 'talisman_1', 'talisman_2', 'talisman_3', 'storage'] as EquipmentSlot[]) {
      if (!player.slots.has(slot)) player.slots.set(slot, null)
    }
    this.actors.set('player', player)
  }

  query(q: LayerQuery): LayerQueryResult {
    if (q.type === 'actorView' && q.nodeId) {
      const eq = this.actors.get(q.nodeId)
      return { nodes: [], edges: [], metadata: { equipment: eq ? Object.fromEntries(eq.slots) : {} } }
    }
    return { nodes: [], edges: [] }
  }

  write(delta: LayerDelta): void {
    if (delta.type === 'updateNode' && delta.data) {
      const actorId = delta.data.actorId as string
      const slot = delta.data.slot as EquipmentSlot
      const itemId = delta.data.itemId as string | null
      const eq = this.actors.get(actorId)
      if (eq) {
        eq.slots.set(slot, itemId)
      }
    }
  }

  invalidate(_target: InvalidateTarget): void {}

  explain(nodeId: string): LayerExplanation {
    const eq = this.actors.get(nodeId)
    const equipped = eq ? Array.from(eq.slots.entries()).filter(([, v]) => v !== null) : []
    return {
      nodeId,
      layer: this.name,
      state: `${equipped.length} items equipped`,
      provenance: 'SIMULATION',
    }
  }

  equip(actorId: string, slot: EquipmentSlot, itemId: string): void {
    const eq = this.actors.get(actorId) ?? { actorId, slots: new Map() }
    eq.slots.set(slot, itemId)
    this.actors.set(actorId, eq)
  }

  unequip(actorId: string, slot: EquipmentSlot): string | null {
    const eq = this.actors.get(actorId)
    if (!eq) return null
    const itemId = eq.slots.get(slot) ?? null
    eq.slots.set(slot, null)
    return itemId
  }
}

// ---- Technique Graph (Layer 12d) ----------------------------------------

export interface TechniqueNode {
  id: string
  name: string
  nameCn?: string
  type: string // cultivation_method, spell, secret_art, body_refinement, divine_ability
  practitioner?: string
  description?: string
  realmRequirement?: string
  qiCost?: number
  effects?: string[]
  masteryLevel?: number
  canonConfidence?: number
}

export class TechniqueGraphLayer implements GraphLayer {
  name = 'technique'
  index = 34
  private techniques: Map<string, TechniqueNode> = new Map()
  private learnedBy: Map<string, string[]> = new Map() // actorId → techniqueIds

  build(input: LayerBuildInput): void {
    for (const node of input.canonGraph.nodesByType('technique')) {
      const tech: TechniqueNode = {
        id: node.id,
        name: node.displayName,
        nameCn: node.displayNameCn,
        type: node.meta?.type as string ?? 'unknown',
        practitioner: node.meta?.practitioner as string,
        description: node.meta?.description as string,
        canonConfidence: node.meta?.confidence as number,
      }
      this.techniques.set(tech.id, tech)
    }
    console.log(`[TechniqueGraph] ${this.techniques.size} techniques loaded`)
  }

  query(q: LayerQuery): LayerQueryResult {
    if (q.type === 'findByType') {
      return { nodes: [], edges: [], metadata: { techniques: Array.from(this.techniques.values()) } }
    }
    if (q.type === 'actorView' && q.nodeId) {
      const learned = this.learnedBy.get(q.nodeId) ?? []
      return { nodes: [], edges: [], metadata: { learned: learned.map((id) => this.techniques.get(id)) } }
    }
    return { nodes: [], edges: [] }
  }

  write(delta: LayerDelta): void {
    if (delta.type === 'addEdge' && delta.data?.edgeType === 'KNOWS') {
      const actorId = delta.data.from as string
      const techId = delta.data.to as string
      const learned = this.learnedBy.get(actorId) ?? []
      if (!learned.includes(techId)) learned.push(techId)
      this.learnedBy.set(actorId, learned)
    }
  }

  invalidate(_target: InvalidateTarget): void {}

  explain(nodeId: string): LayerExplanation {
    const tech = this.techniques.get(nodeId)
    const learners = Array.from(this.learnedBy.entries()).filter(([, ids]) => ids.includes(nodeId)).map(([a]) => a)
    return {
      nodeId,
      layer: this.name,
      state: tech ? `${tech.name} (${tech.type})` : 'unknown',
      provenance: 'CANON',
      dependencies: learners,
    }
  }

  learn(actorId: string, techniqueId: string): boolean {
    const tech = this.techniques.get(techniqueId)
    if (!tech) return false
    const learned = this.learnedBy.get(actorId) ?? []
    if (learned.includes(techniqueId)) return false
    learned.push(techniqueId)
    this.learnedBy.set(actorId, learned)
    return true
  }

  getLearned(actorId: string): TechniqueNode[] {
    const ids = this.learnedBy.get(actorId) ?? []
    return ids.map((id) => this.techniques.get(id)).filter(Boolean) as TechniqueNode[]
  }
}

// ---- Crafting / Recipe Graph (Layer 12e) --------------------------------

export interface RecipeNode {
  id: string
  name: string
  type: 'pill' | 'artifact' | 'talisman' | 'formation' | 'tool' | 'food'
  inputs: Array<{ itemId: string; quantity: number }>
  output: { itemId: string; quantity: number }
  stationRequired: string
  realmRequired: string
  successRate: number
  qualityTiers: string[]
}

export class CraftingGraphLayer implements GraphLayer {
  name = 'crafting'
  index = 35
  private recipes: Map<string, RecipeNode> = new Map()

  build(_input: LayerBuildInput): void {
    // Recipes will be loaded from ALCHEMY_REFERENCE.ts, FORMATIONS_REFERENCE.ts,
    // TALISMANS_REFERENCE.ts, REFINING_REFERENCE.ts in future cycles.
    // For now, add a few starter recipes.
    this.recipes.set('recipe:qi_pill', {
      id: 'recipe:qi_pill',
      name: 'Qi Gathering Pill',
      type: 'pill',
      inputs: [
        { itemId: 'herb:Qi-Gathering Grass', quantity: 3 },
        { itemId: 'material:spirit_stone_fragment', quantity: 1 },
      ],
      output: { itemId: 'pill:qi_gathering', quantity: 1 },
      stationRequired: 'alchemy_furnace',
      realmRequired: 'qi_condensation',
      successRate: 0.7,
      qualityTiers: ['low', 'medium', 'high', 'perfect'],
    })
    console.log(`[CraftingGraph] ${this.recipes.size} recipes loaded`)
  }

  query(q: LayerQuery): LayerQueryResult {
    if (q.type === 'findByType') {
      return { nodes: [], edges: [], metadata: { recipes: Array.from(this.recipes.values()) } }
    }
    return { nodes: [], edges: [] }
  }

  write(delta: LayerDelta): void {
    if (delta.type === 'addNode' && delta.data) {
      this.recipes.set(delta.data.id as string, delta.data as unknown as RecipeNode)
    }
  }

  invalidate(_target: InvalidateTarget): void {}

  explain(nodeId: string): LayerExplanation {
    const recipe = this.recipes.get(nodeId)
    return {
      nodeId,
      layer: this.name,
      state: recipe ? `${recipe.name} (${recipe.type}) → ${recipe.output.itemId}` : 'unknown',
      provenance: 'CANON',
    }
  }

  canCraft(actorId: string, recipeId: string, inventory: InventoryGraphLayer): boolean {
    const recipe = this.recipes.get(recipeId)
    if (!recipe) return false
    // Check if actor has all inputs (simplified — would check inventory graph).
    return true
  }

  craft(recipeId: string): { success: boolean; quality: string } {
    const recipe = this.recipes.get(recipeId)
    if (!recipe) return { success: false, quality: 'none' }
    const roll = Math.random()
    if (roll > recipe.successRate) return { success: false, quality: 'none' }
    const qualityRoll = Math.random()
    let quality = 'low'
    if (qualityRoll > 0.9) quality = 'perfect'
    else if (qualityRoll > 0.7) quality = 'high'
    else if (qualityRoll > 0.4) quality = 'medium'
    return { success: true, quality }
  }
}
