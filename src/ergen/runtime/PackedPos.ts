/** PackedPos — bit-packed world position (CRON-69 point 1) */
export const CHUNK_SIZE = 16;
export const CHUNK_HEIGHT = 256;

export function packChunkKey(cx: number, cz: number): number {
  return (cx << 16) | (cz & 0xffff);
}

export function unpackChunkKey(key: number): [number, number] {
  return [key >> 16, (key << 16) >> 16];
}

export function worldToChunk(wx: number, wz: number): [number, number] {
  return [Math.floor(wx / CHUNK_SIZE), Math.floor(wz / CHUNK_SIZE)];
}

export function worldToLocal(wx: number, wy: number, wz: number): [number, number, number] {
  return [
    ((wx % CHUNK_SIZE) + CHUNK_SIZE) % CHUNK_SIZE,
    wy,
    ((wz % CHUNK_SIZE) + CHUNK_SIZE) % CHUNK_SIZE,
  ];
}
