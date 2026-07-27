/**
 * Wang Family Village — authored canon settlement.
 *
 * Canon: 仙逆 attests Wang Lin was born in 赵国某偏僻小山村 (a remote
 * mountain village in Zhao Country). The name "Wang Family Village" /
 * 王家村 is UNVERIFIED (CRON-69 correction #8) — canon says only "a
 * remote mountain village." We use "Remote Village in Zhao Country" as
 * the display name and flag the settlement canonStatus as 'unverified'
 * for the name but 'canon' for the birthplace.
 *
 * The buildings are mod-original reconstructions (REASONABLE_RECONSTRUCTION,
 * conf 3). The novel attests Wang Lin lived in a poor household with his
 * parents, had a bedroom, and found the Heaven-Defying Bead (天逆珠) under
 * a cliff as a child. We author:
 *   - Wang Lin's childhood home (poor wooden hut, 2 rooms)
 *   - An elder's home (slightly larger, with a meditation room)
 *   - A village well (communal water source)
 *   - A storage shed
 *   - A spirit-pine grove (attested flora of Zhao Country)
 *   - A cliff to the south-west (where the bead was found)
 *
 * NO invented chapter citations. NO fabricated lore.
 */

import type { CanonSettlement } from '../semantic/CanonTypes'

export const WANG_FAMILY_VILLAGE: CanonSettlement = {
  id: 'settlement:zhao_remote_village',
  name: 'Remote Village in Zhao Country',
  nameCn: '赵国偏僻山村',
  parentLocationId: 'L:zhao_country',
  regionId: 'L:zhao_country',
  position: [0, 0, 0], // world origin — player spawns here
  radius: 40,
  canonStatus: 'unverified', // village name is unverified; birthplace is canon
  buildings: [
    // ---- Wang Lin's childhood home ----
    // A poor wooden hut: one bedroom + one kitchen/courtyard.
    {
      id: 'building:wanglin_home',
      name: "Wang Lin's Childhood Home",
      nameCn: '王林故居',
      ownerId: 'N01', // Wang Lin's canon id
      purpose: 'home',
      shellTheme: 'poor_village_wood',
      position: [-6, 0, 4],
      rotation: 0,
      size: [6, 4, 5], // width, height, depth
      rooms: [
        {
          id: 'room:wanglin_bedroom',
          name: 'Bedroom',
          function: 'bedroom',
          ownerId: 'N01',
          position: [0, 0, 0],
          size: [3, 4, 5],
          furniture: [
            {
              id: 'furn:wanglin_bed',
              kind: 'BED',
              tags: ['poor', 'straw'],
              position: [-1, 0, -1.5],
              rotation: 0,
            },
            {
              id: 'furn:wanglin_mat',
              kind: 'MEDITATION_MAT',
              tags: ['woven_straw'],
              position: [0.8, 0, 1.2],
              rotation: 90,
            },
            {
              id: 'furn:wanglin_chest',
              kind: 'CHEST',
              tags: ['wooden', 'personal'],
              position: [-1.2, 0, 1.5],
              rotation: 0,
            },
          ],
          anchors: [
            { id: 'anchor:wanglin_bed', role: 'bed', localPos: [-1, 0, -1.5] },
            { id: 'anchor:wanglin_meditation', role: 'meditation', localPos: [0.8, 0, 1.2] },
            { id: 'anchor:wanglin_storage', role: 'storage', localPos: [-1.2, 0, 1.5] },
            { id: 'anchor:wanglin_door', role: 'door', localPos: [1.4, 0, 0] },
          ],
        },
      ],
    },

    // ---- Village elder's home ----
    // Slightly larger, with a meditation room.
    {
      id: 'building:elder_home',
      name: "Village Elder's Home",
      nameCn: '长老居所',
      ownerId: 'N02', // mod-original elder NPC
      purpose: 'home',
      shellTheme: 'poor_village_wood',
      position: [8, 0, -4],
      rotation: -15,
      size: [7, 4, 6],
      rooms: [
        {
          id: 'room:elder_meditation',
          name: 'Meditation Room',
          function: 'meditation',
          ownerId: 'N02',
          position: [0, 0, 0],
          size: [4, 4, 6],
          furniture: [
            {
              id: 'furn:elder_mat',
              kind: 'MEDITATION_MAT',
              tags: ['woven_straw', 'well_worn'],
              position: [0, 0, 0],
              rotation: 0,
            },
            {
              id: 'furn:elder_altar',
              kind: 'ALTAR',
              tags: ['wooden', 'ancestral'],
              position: [0, 0, -2.5],
              rotation: 0,
            },
            {
              id: 'furn:elder_incense',
              kind: 'INCENSE_BURNER',
              tags: ['bronze', 'lit'],
              position: [-1.5, 0.8, -2.5],
              rotation: 0,
            },
          ],
          anchors: [
            { id: 'anchor:elder_meditation', role: 'meditation', localPos: [0, 0, 0] },
            { id: 'anchor:elder_altar', role: 'altar', localPos: [0, 0, -2.5] },
            { id: 'anchor:elder_door', role: 'door', localPos: [2, 0, 0] },
          ],
        },
      ],
    },

    // ---- Village well ----
    // Communal water source — a stone well with a wooden frame.
    {
      id: 'building:village_well',
      name: 'Village Well',
      nameCn: '村井',
      purpose: 'well',
      shellTheme: 'stone_well',
      position: [2, 0, 0],
      rotation: 0,
      size: [2, 3, 2],
      rooms: [],
    },

    // ---- Storage shed ----
    {
      id: 'building:storage_shed',
      name: 'Storage Shed',
      nameCn: '杂物棚',
      purpose: 'storage',
      shellTheme: 'poor_village_wood',
      position: [-8, 0, -6],
      rotation: 25,
      size: [4, 3, 4],
      rooms: [
        {
          id: 'room:shed_storage',
          name: 'Storage',
          function: 'storage',
          position: [0, 0, 0],
          size: [4, 3, 4],
          furniture: [
            {
              id: 'furn:shed_chest',
              kind: 'CHEST',
              tags: ['wooden', 'communal'],
              position: [-1, 0, -1],
              rotation: 0,
            },
            {
              id: 'furn:shed_table',
              kind: 'TABLE',
              tags: ['wooden', 'work'],
              position: [1, 0, 1],
              rotation: 0,
            },
          ],
          anchors: [
            { id: 'anchor:shed_storage', role: 'storage', localPos: [-1, 0, -1] },
            { id: 'anchor:shed_door', role: 'door', localPos: [2, 0, 0] },
          ],
        },
      ],
    },

    // ---- Village gate (south entrance) ----
    {
      id: 'building:village_gate',
      name: 'Village Gate',
      nameCn: '村门',
      purpose: 'gate',
      shellTheme: 'wooden_gate',
      position: [0, 0, 12],
      rotation: 0,
      size: [5, 4, 1],
      rooms: [],
    },
  ],
  roads: [
    {
      id: 'road:main_path',
      points: [
        [0, 0, 12], // gate
        [0, 0, 6], // plaza
        [2, 0, 0], // well
        [-6, 0, 4], // Wang Lin's home
      ],
      width: 1.5,
      material: 'DIRT',
    },
    {
      id: 'road:elder_path',
      points: [
        [2, 0, 0], // well
        [8, 0, -4], // elder's home
      ],
      width: 1.2,
      material: 'DIRT',
    },
  ],
  spiritVeins: [
    {
      id: 'vein:village_minor',
      name: 'Minor Spirit Vein',
      position: [-80, 0, -120], // the cliff area (mod-original)
      quality: 3,
      element: 'wood',
    },
  ],
}
