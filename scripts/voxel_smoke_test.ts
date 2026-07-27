/**
 * Voxel engine smoke test — runs in Bun (no DOM, no Three renderer).
 *
 * Verifies:
 *  1. Deterministic terrain (same seed → same chunk bytes).
 *  2. Greedy meshing produces a valid BufferGeometry.
 *  3. Triangle count reduction vs naive face culling.
 *  4. Cross-chunk border culling (neighbor chunk reduces faces).
 *
 * Run: cd /home/z/my-project && bunx tsx scripts/voxel_smoke_test.ts
 */

import { VoxelWorld, CANON_SEED } from '../src/ergen/voxel/VoxelWorld';
import { meshChunk } from '../src/ergen/voxel/VoxelChunkMesher';
import { VoxelChunk } from '../src/ergen/voxel/VoxelChunk';
import { getBlockFaceTile, TEXTURE_NAMES } from '../src/ergen/voxel/TextureAtlas';

function hr(label: string): void {
  console.log(`\n── ${label} ──────────────────────────────────`);
}

function main(): void {
  console.log('=== Er Gen Verse — Voxel Engine Smoke Test ===');
  console.log(`CANON_SEED = 0x${CANON_SEED.toString(16)}`);
  console.log(`Atlas tiles registered: ${TEXTURE_NAMES.length}`);

  // ── 1. Determinism ──────────────────────────────────────────────────────
  hr('1. Deterministic terrain');
  const worldA = new VoxelWorld(CANON_SEED);
  const worldB = new VoxelWorld(CANON_SEED);
  const chunkA = worldA.ensureChunk(0, 0);
  const chunkB = worldB.ensureChunk(0, 0);
  let bytesMatch = true;
  for (let i = 0; i < chunkA.data.length; i++) {
    if (chunkA.data[i] !== chunkB.data[i]) {
      bytesMatch = false;
      break;
    }
  }
  console.log(`chunk(0,0) byte-identical across two VoxelWorld instances: ${bytesMatch ? 'YES ✓' : 'NO ✗'}`);

  // ── 2. Chunk stats ──────────────────────────────────────────────────────
  hr('2. Chunk (0,0) stats');
  const solidCount = chunkA.solidCount();
  console.log(`solid voxels: ${solidCount} / ${chunkA.data.length} (${((solidCount / chunkA.data.length) * 100).toFixed(1)}%)`);

  // Sample the terrain — print a heightmap slice.
  console.log('heightmap (surface y at sampled points):');
  for (let z = 0; z < 16; z += 2) {
    let row = '';
    for (let x = 0; x < 16; x += 1) {
      const h = worldA.surfaceHeight(x, z);
      const c = h >= 120 ? '█' : h >= 80 ? '▓' : h >= 66 ? '▒' : h >= 62 ? '░' : '·';
      row += c;
    }
    console.log(`  z=${z.toString().padStart(2)}: ${row}`);
  }
  console.log('  legend: · =water  ░ =beach  ▒ =hills  ▓ =mountain  █ =snow');

  // ── 3. Meshing — single chunk (no neighbors) ────────────────────────────
  hr('3. Greedy meshing — chunk (0,0) with NO neighbor data');
  // Simulate "no neighbors loaded": return AIR for any out-of-bounds border
  // lookup. This forces the mesher to render ALL border faces (worst case).
  const isolatedGetBlock = (wx: number, wy: number, wz: number): number => {
    if (wx < 0 || wx >= 16 || wz < 0 || wz >= 16) return 0; // AIR outside
    if (wy < 0 || wy >= 256) return 0;
    return chunkA.getBlock(wx, wy, wz);
  };
  const resultAlone = meshChunk(chunkA, isolatedGetBlock);
  console.log(`raw exposed faces (no neighbors): ${resultAlone.rawFaceCount}`);
  console.log(`greedy quads:                   ${resultAlone.quadCount}`);
  console.log(`triangles (greedy):             ${resultAlone.triangleCount}`);
  console.log(`naive triangles would be:       ${resultAlone.rawFaceCount * 2}`);
  if (resultAlone.rawFaceCount > 0) {
    const reduction = (1 - resultAlone.triangleCount / (resultAlone.rawFaceCount * 2)) * 100;
    console.log(`triangle reduction:             ${reduction.toFixed(1)}%`);
  }
  if (resultAlone.geometry) {
    const attrs = Object.keys(resultAlone.geometry.attributes);
    console.log(`geometry attributes:            ${attrs.join(', ')}`);
    const posAttr = resultAlone.geometry.attributes.position;
    console.log(`position vertex count:          ${posAttr.count} (non-indexed)`);
  }

  // ── 4. Cross-chunk border culling ───────────────────────────────────────
  hr('4. Cross-chunk border culling (with neighbors loaded)');
  // Now use the real world.getBlock which auto-generates neighbors, so
  // border faces shared with solid neighbor blocks get culled.
  console.log('generating 4 neighbor chunks (1,0), (-1,0), (0,1), (0,-1)...');
  worldA.ensureChunk(1, 0);
  worldA.ensureChunk(-1, 0);
  worldA.ensureChunk(0, 1);
  worldA.ensureChunk(0, -1);
  const resultWithNeighbors = meshChunk(chunkA, (wx, wy, wz) => worldA.getBlock(wx, wy, wz));
  console.log(`raw faces with neighbors:  ${resultWithNeighbors.rawFaceCount}`);
  console.log(`raw faces alone:           ${resultAlone.rawFaceCount}`);
  const borderFaces = resultAlone.rawFaceCount - resultWithNeighbors.rawFaceCount;
  console.log(`border faces culled:       ${borderFaces} (${borderFaces * 2} triangles saved)`);
  console.log(`greedy quads (with nbrs):  ${resultWithNeighbors.quadCount}`);
  console.log(`triangles (with nbrs):     ${resultWithNeighbors.triangleCount}`);

  // ── 5. Texture tile mapping sanity ──────────────────────────────────────
  hr('5. Texture tile mapping');
  console.log(`tile for grass top (blockId=4, face=2): ${getBlockFaceTile(4, 2)} → '${TEXTURE_NAMES[getBlockFaceTile(4, 2)]}'`);
  console.log(`tile for grass side (blockId=4, face=0): ${getBlockFaceTile(4, 0)} → '${TEXTURE_NAMES[getBlockFaceTile(4, 0)]}'`);
  console.log(`tile for grass bottom (blockId=4, face=3): ${getBlockFaceTile(4, 3)} → '${TEXTURE_NAMES[getBlockFaceTile(4, 3)]}'`);

  // ── 6. Multi-chunk meshing budget ───────────────────────────────────────
  hr('6. 3×3 chunk region meshing budget');
  let totalTris = 0;
  let totalQuads = 0;
  let totalRaw = 0;
  const t0 = Date.now();
  for (let cz = -1; cz <= 1; cz++) {
    for (let cx = -1; cx <= 1; cx++) {
      const c = worldA.ensureChunk(cx, cz);
      const r = meshChunk(c, (wx, wy, wz) => worldA.getBlock(wx, wy, wz));
      totalTris += r.triangleCount;
      totalQuads += r.quadCount;
      totalRaw += r.rawFaceCount;
    }
  }
  const dt = Date.now() - t0;
  console.log(`3×3 region: ${totalRaw} raw faces → ${totalQuads} quads → ${totalTris} triangles`);
  console.log(`meshing time: ${dt}ms (${(dt / 9).toFixed(1)}ms/chunk avg)`);
  if (totalRaw > 0) {
    const red = (1 - totalTris / (totalRaw * 2)) * 100;
    console.log(`overall triangle reduction: ${red.toFixed(1)}%`);
  }

  // ── Summary ─────────────────────────────────────────────────────────────
  hr('SUMMARY');
  console.log(`deterministic:        ${bytesMatch ? 'YES' : 'NO'}`);
  console.log(`chunk(0,0) triangles: ${resultWithNeighbors.triangleCount}`);
  console.log(`3×3 region triangles: ${totalTris}`);
  console.log(`greedy reduction:     ${totalRaw > 0 ? ((1 - totalTris / (totalRaw * 2)) * 100).toFixed(1) : '0'}%`);
  console.log(`\nSmoke test ${bytesMatch && resultWithNeighbors.triangleCount > 0 ? 'PASSED ✓' : 'FAILED ✗'}`);
}

main();
