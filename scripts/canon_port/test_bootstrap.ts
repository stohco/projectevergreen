import { WorldGraph } from '../../src/ergen/graph/WorldGraph';
import {
  bootstrapGraph,
  getSummaryCounts,
  ALL_CHARACTERS,
  MOD_ORIGINAL_ENTITIES,
} from '../../src/ergen/canon/RICanonicalDatabase';

const g = new WorldGraph();
bootstrapGraph(g);
const stats = g.stats();
console.log('Graph stats:', JSON.stringify(stats, null, 2));
console.log('Summary counts:', JSON.stringify(getSummaryCounts(), null, 2));
console.log('Expected: 630 nodes total (158 chars + 80 locs + 178 arts + 214 techs)');
console.log('Mod-original entities:', MOD_ORIGINAL_ENTITIES.length);

// Verify Wang Lin node
const wl = g.node('N01');
console.log(
  'Wang Lin node:',
  wl?.id,
  wl?.displayName,
  wl?.canonStatus,
  'realm:',
  wl?.realm,
  'chapter:',
  wl?.firstAppearanceChapter,
);

// Verify L34 unverified (CRON-69 #8)
const wfv = g.node('L34');
console.log('Wang Family Village (L34):', wfv?.displayName, 'canonStatus:', wfv?.canonStatus);

// Verify N152 Zeng Da Niu (CRON-69 #7)
const zn = g.node('N152');
console.log('N152 (Zeng Da Niu):', zn?.displayName, 'canonStatus:', zn?.canonStatus);
const n152Char = ALL_CHARACTERS.find((c) => c.id === 'N152');
console.log('  N152 affiliation:', n152Char?.affiliation);

// Verify edges
const wlOutEdges = g.outEdges('N01');
console.log('Wang Lin outgoing edges:', wlOutEdges.length);

const edgeTypes: Record<string, number> = {};
for (const e of g.allEdges()) {
  edgeTypes[e.type] = (edgeTypes[e.type] ?? 0) + 1;
}
console.log('Edge type distribution:', JSON.stringify(edgeTypes, null, 2));

// Verify a sample relationship edge
const wlRelationships = wlOutEdges.filter((e) => e.type === 'FAMILIAR_WITH' || e.type === 'HOSTILE_TO' || e.type === 'MASTER_OF' || e.type === 'DISCIPLE_OF');
console.log('Wang Lin relationship edges:', wlRelationships.length);

// Verify the bead node (I01)
const bead = g.node('I01');
console.log('I01 (Heaven-Defying Bead):', bead?.displayName, 'canonStatus:', bead?.canonStatus);

// Verify Sea of Devils (L45)
const sod = g.node('L45');
console.log('L45 (Sea of Devils):', sod?.displayName, sod?.displayNameCn);

// Verify Snow Country (L28)
const snow = g.node('L28');
console.log('L28 (Snow Country):', snow?.displayName, snow?.displayNameCn);

// Verify Jue Ming Valley (L46)
const jmv = g.node('L46');
console.log('L46 (Jue Ming Valley):', jmv?.displayName, jmv?.displayNameCn);

// Total edges
console.log('Total edges:', g.allEdges().length);

// Test name resolution
console.log('\n=== Name resolution tests ===');
const tests = ['Wang Lin', 'wang_lin', '王林', 'Li Muwan', 'Heng Yue Sect', 'Planet Suzaku', 'Heaven-Defying Bead'];
for (const t of tests) {
  const node = g.resolveByName(t);
  console.log(`  "${t}" -> ${node?.id ?? 'NOT FOUND'} (${node?.displayName ?? '?'})`);
}

// Test query methods
console.log('\n=== Query method tests ===');
const wangLinOwned = ALL_CHARACTERS.filter(c => c.id === 'N01')[0];
console.log('Wang Lin relationships:', wangLinOwned.relationships.length);
console.log('  - Li Muwan relation:', wangLinOwned.relationships.find(r => r.target === 'Li Muwan')?.relation);
console.log('  - Situ Nan relation:', wangLinOwned.relationships.find(r => r.target === 'Situ Nan')?.relation);
console.log('  - Teng Huayuan relation:', wangLinOwned.relationships.find(r => r.target === 'Teng Huayuan')?.relation);

// CRON-69 verification summary
console.log('\n=== CRON-69 11-point correction verification ===');
const n84 = ALL_CHARACTERS.find(c => c.id === 'N84');
console.log('#1 Teng Li (N84, NOT Teng Lijun):', n84?.name, '-', n84?.nameCn);
const n17 = ALL_CHARACTERS.find(c => c.id === 'N17');
console.log('#2 Li Muwan (N17) affiliation:', n17?.affiliation);
const n20 = ALL_CHARACTERS.find(c => c.id === 'N20');
console.log('#3 Situ Nan (N20) affiliation:', n20?.affiliation);
// #4, #5, #6, #10 verified above
console.log('#7 Zeng Da Niu (N152):', zn?.displayName, '/', n152Char?.affiliation);
console.log('#8 Wang Family Village (L34) canonStatus:', wfv?.canonStatus);
console.log('#9 Mod-original entities:', MOD_ORIGINAL_ENTITIES.map(e => e.name).join(', '));
console.log('#11 firstAppearanceChapter omitted when unknown (sample):');
const noChapterChars = ALL_CHARACTERS.filter(c => g.node(c.id)?.firstAppearanceChapter === undefined).length;
console.log('   Characters without chapter:', noChapterChars, '/ 158');
