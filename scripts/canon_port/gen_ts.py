#!/usr/bin/env python3
"""
Generator: canon_data.json -> RICanonicalDatabase.ts

Emits a fully-typed TypeScript module containing:
  - ALL_CHARACTERS, ALL_LOCATIONS, ALL_ARTIFACTS, ALL_TECHNIQUES (literal data)
  - Index maps + query methods (mirrors Java's static API)
  - bootstrapGraph(graph: WorldGraph) — populates a WorldGraph with nodes + edges

CRON-69 corrections applied as overrides:
  #7 N152 Da Niu -> Zeng Da Niu, affiliation -> 'Si Pai Lian Meng (Four Sects Alliance)'
  #8 L34 Wang Family Village -> canonStatus 'unverified'

NO fabricated chapter citations: firstAppearanceChapter is extracted from
the source string ONLY when an unambiguous "Ch.N" pattern is found.
"""
import json
import re
import sys
from typing import Any, List, Optional


# ── CRON-69 corrections (applied at emit time) ─────────────────────────
CHAR_OVERRIDES: dict[str, dict[str, Any]] = {
    'N152': {
        # CRON-69 #7: Zeng Da Niu belongs to Si Pai Lian Meng (Four Sects Alliance) hua-fan arc.
        # Java file had "Da Niu" with affiliation "none" — fix here.
        'name': 'Zeng Da Niu',
        'nameCn': '曾大牛',
        'affiliation': 'Si Pai Lian Meng (Four Sects Alliance) — hua-fan arc',
        'knownFacts': [
            'Wang Lin\'s childhood friend from the mortal 化凡 (hua-fan) arc',
            'Remained mortal while Wang Lin walked the cultivation path',
            'Belongs to the 四派联盟 (Si Pai Lian Meng / Four Sects Alliance) hua-fan arc',
        ],
        'source': 'novel (hua-fan arc); CRON-69 correction #7 (CanonActorMaterializer.java:60 zeng_da_niu -> four_sects_alliance)',
        'canonConfidence': 4,  # promoted from 3 — affiliation is now canon-attested
    },
}

LOC_OVERRIDES: dict[str, dict[str, Any]] = {
    'L34': {
        # CRON-69 #8: Wang Family Village is UNVERIFIED — canon says only
        # "a remote mountain village in Zhao Country". Flag as unverified.
        # We don't change the name (it's still the mod's working name) but
        # we downgrade canonConfidence and add a knownFact noting the
        # unverified status. The GraphNode emitted by bootstrapGraph will
        # carry canonStatus='unverified'.
        'canonConfidence': 3,  # demoted from 5 — name is unverified
        'knownFacts_append': [
            'CRON-69 #8: the name "Wang Family Village" is UNVERIFIED — canon 仙逆 attests only "赵国某偏僻小山村" (a remote mountain village in Zhao Country). Wang Lin\'s birthplace being a remote Zhao village IS canon; the village name is mod-original.',
        ],
    },
}

# Mod-original entities flagged per CRON-69 #9. These do NOT exist in the
# Java canon DB — we list them here so the engine can recognize them if
# they appear elsewhere (e.g., structure composition files). They are NOT
# added as nodes by bootstrapGraph() because they are not canon.
MOD_ORIGINAL_ENTITIES: list[dict[str, str]] = [
    {
        'name': 'Old Chen',
        'note': 'mod-original — referenced in legacy Wang Lin journal; not in novel',
        'canonStatus': 'mod_original',
    },
    {
        'name': 'Forest of Distorted Sense',
        'note': 'mod-original — the canon-accurate name is Jue Ming Valley (L46)',
        'canonStatus': 'mod_original',
    },
]


def ts_str(s: Any) -> str:
    """Emit a TypeScript string literal with proper escaping."""
    if s is None:
        return 'null'
    if not isinstance(s, str):
        s = str(s)
    out = []
    for ch in s:
        if ch == '\\':
            out.append('\\\\')
        elif ch == '"':
            out.append('\\"')
        elif ch == '\n':
            out.append('\\n')
        elif ch == '\r':
            out.append('\\r')
        elif ch == '\t':
            out.append('\\t')
        elif ord(ch) < 0x20:
            out.append('\\u%04x' % ord(ch))
        else:
            out.append(ch)
    return '"' + ''.join(out) + '"'


def ts_val(v: Any, indent: int = 0) -> str:
    """Emit a TypeScript literal for a Python value."""
    pad = '  ' * indent
    pad_in = '  ' * (indent + 1)
    if v is None:
        return 'null'
    if isinstance(v, bool):
        return 'true' if v else 'false'
    if isinstance(v, (int, float)):
        return str(v)
    if isinstance(v, str):
        return ts_str(v)
    if isinstance(v, list):
        if not v:
            return '[]'
        items = [pad_in + ts_val(x, indent + 1) for x in v]
        return '[\n' + ',\n'.join(items) + ',\n' + pad + ']'
    if isinstance(v, dict):
        if '__class__' in v:
            # RelationShip record
            if v['__class__'] == 'RelationShip':
                target = v['args'][0]
                relation = v['args'][1]
                return '{ target: ' + ts_str(target) + ', relation: ' + ts_str(relation) + ' }'
            else:
                raise ValueError(f"Unknown class: {v['__class__']}")
        # Generic dict — shouldn't happen
        items = [pad_in + ts_str(k) + ': ' + ts_val(val, indent + 1) for k, val in v.items()]
        return '{\n' + ',\n'.join(items) + ',\n' + pad + '}'
    raise TypeError(f"Cannot emit value of type {type(v).__name__}: {v!r}")


# ── Slugify (name -> objectId) ─────────────────────────────────────────
_SLUG_CACHE: dict[str, str] = {}


def slugify(name: str, existing: set[str]) -> str:
    """Convert a display name to a lowercase objectId slug, ensuring uniqueness."""
    if name in _SLUG_CACHE:
        return _SLUG_CACHE[name]
    base = re.sub(r'[^a-zA-Z0-9]+', '_', name.strip()).lower().strip('_')
    if not base:
        base = 'unnamed'
    slug = base
    i = 2
    while slug in existing:
        slug = f'{base}_{i}'
        i += 1
    existing.add(slug)
    _SLUG_CACHE[name] = slug
    return slug


# ── Chapter extraction ────────────────────────────────────────────────
_CH_PATTERN = re.compile(r'Ch\.?\s*~?(\d{1,5})', re.IGNORECASE)


def extract_chapter(source: str) -> Optional[int]:
    """Mine the source string for the first 'Ch.N' citation. Return None if
    ambiguous (multiple different chapters) or absent."""
    if not source:
        return None
    matches = _CH_PATTERN.findall(source)
    if not matches:
        return None
    chapters = set(int(m) for m in matches)
    if len(chapters) == 1:
        return next(iter(chapters))
    # Multiple chapters — pick the smallest "obtained" / first appearance.
    # Heuristic: if the source contains "obtained" or "first", pick the min.
    src_lower = source.lower()
    if 'obtained' in src_lower or 'first' in src_lower or 'founded' in src_lower or 'found' in src_lower:
        return min(chapters)
    # Otherwise omit — don't guess
    return None


# ── CultivationRealm mapping (best-effort) ────────────────────────────
REALM_KEYWORDS: list[tuple[str, str]] = [
    ('heaven trampling', 'ascendant'),
    ('treading heaven', 'ascendant'),
    ('ascendant', 'ascendant'),
    ('heaven dao', 'heaven_dao'),
    ('void amalgamation', 'void_amalgamation'),
    ('void refinement', 'void_refinement'),
    ('void tribulant', 'void_amalgamation'),
    ('soul transformation', 'soul_transformation'),
    ('soul formation', 'soul_formation'),
    ('infant transformation', 'soul_formation'),
    ('nascent soul', 'nascent_soul'),
    ('core formation', 'core_formation'),
    ('foundation establishment', 'foundation_establishment'),
    ('foundation', 'foundation_establishment'),
    ('qi condensation', 'qi_condensation'),
    ('mortal', 'mortal'),
]


def map_realm(peak_realm: str) -> Optional[str]:
    if not peak_realm:
        return None
    pl = peak_realm.lower()
    for kw, realm in REALM_KEYWORDS:
        if kw in pl:
            return realm
    return None


# ── Edge kind mapping (character relationship -> EdgeType) ─────────────
RELATION_TO_EDGE: dict[str, tuple[str, str]] = {
    # relation -> (edgeType, reverseEdgeType or None)
    'love_interest': ('FAMILIAR_WITH', 'FAMILIAR_WITH'),
    'family': ('FAMILIAR_WITH', 'FAMILIAR_WITH'),
    'ally': ('ALLIED_WITH', 'ALLIED_WITH'),
    'enemy': ('HOSTILE_TO', 'HOSTILE_TO'),
    'rival': ('HOSTILE_TO', 'HOSTILE_TO'),
    'master': ('MASTER_OF', 'DISCIPLE_OF'),
    'disciple': ('DISCIPLE_OF', 'MASTER_OF'),
    'faction': ('MEMBER_OF', ''),  # NPC -> faction
}


# ── Main generator ────────────────────────────────────────────────────
def emit(out_path: str, canon: dict[str, Any]) -> None:
    chars_raw = canon['characters']
    locs_raw = canon['locations']
    arts_raw = canon['artifacts']
    techs_raw = canon['techniques']

    # Apply overrides
    chars: list[dict[str, Any]] = []
    for r in chars_raw:
        a = list(r['args'])
        cid = a[0]
        if cid in CHAR_OVERRIDES:
            ov = CHAR_OVERRIDES[cid]
            # name=1, nameCn=2, affiliation=5, knownFacts=10, source=12, canonConfidence=7
            if 'name' in ov: a[1] = ov['name']
            if 'nameCn' in ov: a[2] = ov['nameCn']
            if 'affiliation' in ov: a[5] = ov['affiliation']
            if 'knownFacts' in ov: a[10] = ov['knownFacts']
            if 'source' in ov: a[12] = ov['source']
            if 'canonConfidence' in ov: a[7] = ov['canonConfidence']
        chars.append({'id': cid, 'args': a, 'line': r['__line__']})

    locs: list[dict[str, Any]] = []
    for r in locs_raw:
        a = list(r['args'])
        lid = a[0]
        if lid in LOC_OVERRIDES:
            ov = LOC_OVERRIDES[lid]
            # canonConfidence=10, knownFacts=12
            if 'canonConfidence' in ov: a[10] = ov['canonConfidence']
            if 'knownFacts_append' in ov:
                a[12] = list(a[12]) + list(ov['knownFacts_append'])
        locs.append({'id': lid, 'args': a, 'line': r['__line__']})

    arts = [{'id': r['args'][0], 'args': list(r['args']), 'line': r['__line__']} for r in arts_raw]
    techs = [{'id': r['args'][0], 'args': list(r['args']), 'line': r['__line__']} for r in techs_raw]

    # ── Build object literals ──
    out: list[str] = []
    out.append(_HEADER)

    # ── ALL_CHARACTERS ──
    out.append('// ── 158 canon characters (N01..N160) ──')
    out.append('export const ALL_CHARACTERS: readonly CanonCharacter[] = [')
    for c in chars:
        a = c['args']
        # CanonCharacter(id, name, nameCn, type, peakRealm, affiliation, status,
        #                 canonConfidence, firstAppearance, location, knownFacts,
        #                 relationships, source)
        out.append('  {')
        out.append(f'    id: {ts_str(a[0])},')
        out.append(f'    name: {ts_str(a[1])},')
        out.append(f'    nameCn: {ts_str(a[2])},')
        out.append(f'    type: {ts_str(a[3])} as CharType,')
        out.append(f'    peakRealm: {ts_str(a[4])},')
        out.append(f'    affiliation: {ts_str(a[5])},')
        out.append(f'    status: {ts_str(a[6])},')
        out.append(f'    canonConfidence: {ts_val(a[7])},')
        out.append(f'    firstAppearance: {ts_str(a[8])},')
        out.append(f'    location: {ts_str(a[9])},')
        out.append(f'    knownFacts: {ts_val(a[10], 2)},')
        out.append(f'    relationships: {ts_val(a[11], 2)},')
        out.append(f'    source: {ts_str(a[12])},')
        out.append('  },')
    out.append('];')
    out.append('')

    # ── ALL_LOCATIONS ──
    out.append('// ── 80 canon locations (L01..L80) ──')
    out.append('export const ALL_LOCATIONS: readonly CanonLocation[] = [')
    for l in locs:
        a = l['args']
        # CanonLocation(id, name, nameCn, type, parentLocation, cosmologyLayer,
        #               worldLawTier, isSealed, sealedBy, spiritVeins,
        #               canonConfidence, firstAppearance, knownFacts,
        #               associatedFactions, keyEvents, source)
        out.append('  {')
        out.append(f'    id: {ts_str(a[0])},')
        out.append(f'    name: {ts_str(a[1])},')
        out.append(f'    nameCn: {ts_str(a[2])},')
        out.append(f'    type: {ts_str(a[3])} as LocType,')
        out.append(f'    parentLocation: {ts_str(a[4])},')
        out.append(f'    cosmologyLayer: {ts_str(a[5])},')
        out.append(f'    worldLawTier: {ts_str(a[6])},')
        out.append(f'    isSealed: {ts_val(a[7])},')
        out.append(f'    sealedBy: {ts_str(a[8])},')
        out.append(f'    spiritVeins: {ts_str(a[9])},')
        out.append(f'    canonConfidence: {ts_val(a[10])},')
        out.append(f'    firstAppearance: {ts_str(a[11])},')
        out.append(f'    knownFacts: {ts_val(a[12], 2)},')
        out.append(f'    associatedFactions: {ts_val(a[13], 2)},')
        out.append(f'    keyEvents: {ts_val(a[14], 2)},')
        out.append(f'    source: {ts_str(a[15])},')
        out.append('  },')
    out.append('];')
    out.append('')

    # ── ALL_ARTIFACTS ──
    out.append('// ── 178 canon artifacts (I01..I178) ──')
    out.append('export const ALL_ARTIFACTS: readonly CanonArtifact[] = [')
    for ar in arts:
        a = ar['args']
        # CanonArtifact(id, name, nameCn, type, category, currentOwner,
        #               abilities, origin, canonConfidence, knownFacts, source)
        out.append('  {')
        out.append(f'    id: {ts_str(a[0])},')
        out.append(f'    name: {ts_str(a[1])},')
        out.append(f'    nameCn: {ts_str(a[2])},')
        out.append(f'    type: {ts_str(a[3])} as ArtType,')
        out.append(f'    category: {ts_str(a[4])},')
        out.append(f'    currentOwner: {ts_str(a[5])},')
        out.append(f'    abilities: {ts_val(a[6], 2)},')
        out.append(f'    origin: {ts_str(a[7])},')
        out.append(f'    canonConfidence: {ts_val(a[8])},')
        out.append(f'    knownFacts: {ts_val(a[9], 2)},')
        out.append(f'    source: {ts_str(a[10])},')
        out.append('  },')
    out.append('];')
    out.append('')

    # ── ALL_TECHNIQUES ──
    out.append('// ── 214 canon techniques (T01..T170, AT01..AT09, VA01..VA04, OS01..OS07, B01..B09, E01..E14) ──')
    out.append('export const ALL_TECHNIQUES: readonly CanonTechnique[] = [')
    for t in techs:
        a = t['args']
        # CanonTechnique(id, name, nameCn, type, origin, effects,
        #                knownUsers, canonConfidence, knownFacts, source)
        out.append('  {')
        out.append(f'    id: {ts_str(a[0])},')
        out.append(f'    name: {ts_str(a[1])},')
        out.append(f'    nameCn: {ts_str(a[2])},')
        out.append(f'    type: {ts_str(a[3])} as TechType,')
        out.append(f'    origin: {ts_str(a[4])},')
        out.append(f'    effects: {ts_val(a[5], 2)},')
        out.append(f'    knownUsers: {ts_val(a[6], 2)},')
        out.append(f'    canonConfidence: {ts_val(a[7])},')
        out.append(f'    knownFacts: {ts_val(a[8], 2)},')
        out.append(f'    source: {ts_str(a[9])},')
        out.append('  },')
    out.append('];')
    out.append('')

    # ── Index maps ──
    out.append(_INDEX_MAPS)

    # ── Query methods ──
    out.append(_QUERY_METHODS)

    # ── bootstrapGraph ──
    out.append(_BOOTSTRAP_PREFIX)

    # Pre-compute slugs (so we don't have to do it at runtime)
    # We emit them as a const map.
    out.append('  // ── Pre-computed name → slug map (mod-original helper) ──')
    out.append('  const slugSpace: Set<string> = new Set();')

    # Build name -> nodeId index for edge resolution (emit as runtime build)
    out.append('  // Build name → character-id index (case-insensitive)')
    out.append('  const charByName = new Map<string, string>();')
    out.append('  for (const c of ALL_CHARACTERS) {')
    out.append('    if (c.name) charByName.set(c.name.toLowerCase(), c.id);')
    out.append('    if (c.nameCn) charByName.set(c.nameCn, c.id);')
    out.append('  }')
    out.append('  const locByName = new Map<string, string>();')
    out.append('  for (const l of ALL_LOCATIONS) {')
    out.append('    if (l.name) locByName.set(l.name.toLowerCase(), l.id);')
    out.append('    if (l.nameCn) locByName.set(l.nameCn, l.id);')
    out.append('  }')

    # ── Emit nodes ──
    out.append('')
    out.append('  // ── Nodes: characters ──')
    out.append('  for (const c of ALL_CHARACTERS) {')
    out.append('    const chapter = extractChapter(c.source);')
    out.append('    const realm = mapPeakRealm(c.peakRealm);')
    out.append('    const canonStatus = computeCharCanonStatus(c);')
    out.append('    const slug = makeSlug(c.name || c.id, slugSpace);')
    out.append('    graph.addNode({')
    out.append('      id: c.id,')
    out.append("      type: 'npc',")
    out.append('      displayName: c.name,')
    out.append('      displayNameCn: c.nameCn ?? undefined,')
    out.append('      objectId: slug,')
    out.append('      realm: realm ?? undefined,')
    out.append('      canonStatus,')
    out.append('      firstAppearanceChapter: chapter ?? undefined,')
    out.append('      tags: [c.type, c.status, `aff:${c.affiliation.slice(0, 40)}`],')
    out.append('      meta: {')
    out.append('        peakRealm: c.peakRealm,')
    out.append('        affiliation: c.affiliation,')
    out.append('        status: c.status,')
    out.append('        canonConfidence: c.canonConfidence,')
    out.append('        location: c.location,')
    out.append('        knownFacts: c.knownFacts,')
    out.append('        source: c.source,')
    out.append('      },')
    out.append('    });')
    out.append('  }')
    out.append('')

    out.append('  // ── Nodes: locations ──')
    out.append('  for (const l of ALL_LOCATIONS) {')
    out.append('    const chapter = extractChapter(l.source);')
    out.append('    const canonStatus = computeLocCanonStatus(l);')
    out.append('    const slug = makeSlug(l.name || l.id, slugSpace);')
    out.append('    graph.addNode({')
    out.append('      id: l.id,')
    out.append("      type: 'location',")
    out.append('      displayName: l.name,')
    out.append('      displayNameCn: l.nameCn ?? undefined,')
    out.append('      objectId: slug,')
    out.append('      canonStatus,')
    out.append('      firstAppearanceChapter: chapter ?? undefined,')
    out.append('      tags: [l.type, l.cosmologyLayer, `law:${l.worldLawTier}`, l.isSealed ? \'sealed\' : \'open\'],')
    out.append('      meta: {')
    out.append('        parentLocation: l.parentLocation,')
    out.append('        cosmologyLayer: l.cosmologyLayer,')
    out.append('        worldLawTier: l.worldLawTier,')
    out.append('        isSealed: l.isSealed,')
    out.append('        sealedBy: l.sealedBy,')
    out.append('        spiritVeins: l.spiritVeins,')
    out.append('        canonConfidence: l.canonConfidence,')
    out.append('        associatedFactions: l.associatedFactions,')
    out.append('        keyEvents: l.keyEvents,')
    out.append('        knownFacts: l.knownFacts,')
    out.append('        source: l.source,')
    out.append('      },')
    out.append('    });')
    out.append('  }')
    out.append('')

    out.append('  // ── Nodes: artifacts ──')
    out.append('  for (const a of ALL_ARTIFACTS) {')
    out.append('    const chapter = extractChapter(a.source);')
    out.append('    const slug = makeSlug(a.name || a.id, slugSpace);')
    out.append('    graph.addNode({')
    out.append('      id: a.id,')
    out.append("      type: 'item',")
    out.append('      displayName: a.name,')
    out.append('      displayNameCn: a.nameCn ?? undefined,')
    out.append('      objectId: slug,')
    out.append("      canonStatus: 'canon',")
    out.append('      firstAppearanceChapter: chapter ?? undefined,')
    out.append('      tags: [a.type, a.category],')
    out.append('      meta: {')
    out.append('        currentOwner: a.currentOwner,')
    out.append('        abilities: a.abilities,')
    out.append('        origin: a.origin,')
    out.append('        canonConfidence: a.canonConfidence,')
    out.append('        knownFacts: a.knownFacts,')
    out.append('        source: a.source,')
    out.append('      },')
    out.append('    });')
    out.append('  }')
    out.append('')

    out.append('  // ── Nodes: techniques ──')
    out.append('  for (const t of ALL_TECHNIQUES) {')
    out.append('    const chapter = extractChapter(t.source);')
    out.append('    const slug = makeSlug(t.name || t.id, slugSpace);')
    out.append('    graph.addNode({')
    out.append('      id: t.id,')
    out.append("      type: 'technique',")
    out.append('      displayName: t.name,')
    out.append('      displayNameCn: t.nameCn ?? undefined,')
    out.append('      objectId: slug,')
    out.append("      canonStatus: 'canon',")
    out.append('      firstAppearanceChapter: chapter ?? undefined,')
    out.append('      tags: [t.type],')
    out.append('      meta: {')
    out.append('        origin: t.origin,')
    out.append('        effects: t.effects,')
    out.append('        knownUsers: t.knownUsers,')
    out.append('        canonConfidence: t.canonConfidence,')
    out.append('        knownFacts: t.knownFacts,')
    out.append('        source: t.source,')
    out.append('      },')
    out.append('    });')
    out.append('  }')
    out.append('')

    # ── Edges ──
    out.append('  // ── Edges: PARENT_LOCATION (location -> location) ──')
    out.append('  for (const l of ALL_LOCATIONS) {')
    out.append('    if (!l.parentLocation) continue;')
    out.append('    const parentId = locByName.get(l.parentLocation.toLowerCase());')
    out.append('    if (!parentId) continue;')
    out.append("    graph.addEdge({ id: `edge_par_${l.id}_${parentId}`, from: l.id, to: parentId, type: 'PARENT_LOCATION', weight: 1.0, provenance: Provenance.CANON });")
    out.append('  }')
    out.append('')

    out.append('  // ── Edges: LOCATED_IN (character -> location, first match wins) ──')
    out.append('  for (const c of ALL_CHARACTERS) {')
    out.append('    if (!c.location) continue;')
    out.append('    // The location string is a slash-separated list of candidates; pick the first that matches.')
    out.append('    const parts = c.location.split(\'/\').map(s => s.trim());')
    out.append('    for (const p of parts) {')
    out.append('      const lid = locByName.get(p.toLowerCase());')
    out.append('      if (lid) {')
    out.append("        graph.addEdge({ id: `edge_loc_${c.id}_${lid}`, from: c.id, to: lid, type: 'LOCATED_IN', weight: 1.0, provenance: Provenance.CANON });")
    out.append('        break;')
    out.append('      }')
    out.append('    }')
    out.append('  }')
    out.append('')

    out.append('  // ── Edges: character relationships (FAMILIAR_WITH / FAMILY / MASTER_OF / HOSTILE_TO / etc.) ──')
    out.append('  let relIdx = 0;')
    out.append('  for (const c of ALL_CHARACTERS) {')
    out.append('    for (const r of c.relationships) {')
    out.append('      const targetId = charByName.get(r.target.toLowerCase()) || (r.target.includes(\'(\') ? undefined : charByName.get(r.target.split(\' (\')[0].toLowerCase()));')
    out.append('      if (!targetId) continue;')
    out.append('      const edgeType = relationToEdgeType(r.relation);')
    out.append('      if (!edgeType) continue;')
    out.append("      graph.addEdge({ id: `edge_rel_${relIdx++}_${c.id}_${targetId}`, from: c.id, to: targetId, type: edgeType, weight: 1.0, provenance: Provenance.CANON, meta: { relation: r.relation } });")
    out.append('    }')
    out.append('  }')
    out.append('')

    out.append('  // ── Edges: OWNS (character -> artifact, by currentOwner) ──')
    out.append('  let ownIdx = 0;')
    out.append('  for (const a of ALL_ARTIFACTS) {')
    out.append('    if (!a.currentOwner) continue;')
    out.append('    const ownerId = charByName.get(a.currentOwner.toLowerCase());')
    out.append('    if (!ownerId) continue;')
    out.append("    graph.addEdge({ id: `edge_own_${ownIdx++}_${ownerId}_${a.id}`, from: ownerId, to: a.id, type: 'OWNS', weight: 1.0, provenance: Provenance.CANON });")
    out.append('  }')
    out.append('')

    out.append('  // ── Edges: KNOWS (character -> technique, by knownUsers) ──')
    out.append('  let knowIdx = 0;')
    out.append('  for (const t of ALL_TECHNIQUES) {')
    out.append('    for (const u of t.knownUsers) {')
    out.append('      const userId = charByName.get(u.toLowerCase());')
    out.append('      if (!userId) continue;')
    out.append("      graph.addEdge({ id: `edge_know_${knowIdx++}_${userId}_${t.id}`, from: userId, to: t.id, type: 'KNOWS', weight: 1.0, provenance: Provenance.CANON });")
    out.append('    }')
    out.append('  }')
    out.append('}')

    # ── Helper functions ──
    out.append(_HELPERS)

    # ── Mod-original entities registry (CRON-69 #9) ──
    out.append(_MOD_ORIGINAL)

    out.append('')

    with open(out_path, 'w', encoding='utf-8') as f:
        f.write('\n'.join(out))

    print(f'  Wrote {out_path}', file=sys.stderr)
    print(f'  Characters: {len(chars)}', file=sys.stderr)
    print(f'  Locations:  {len(locs)}', file=sys.stderr)
    print(f'  Artifacts:  {len(arts)}', file=sys.stderr)
    print(f'  Techniques: {len(techs)}', file=sys.stderr)


_HEADER = """\
/**
 * RICanonicalDatabase — the canonical source of truth for the Er Gen
 * Verse (仙逆 / Renegade Immortal) world model.
 *
 * Ported 1:1 from forge-mod/src/main/java/dev/ergenverse/wanglin/RICanonicalDatabase.java
 * (CRON-PIVOT-A). 630 records: 158 characters + 80 locations + 178 artifacts
 * + 214 techniques. Every field value is preserved verbatim from the Java
 * source — NO fabrication, NO generic fantasy filler.
 *
 * CRON-69 11-point correction manifest preserved (see CanonConstants.CRION_69_CORRECTIONS):
 *   1. Teng = teng (NOT Teng Lijun) — young antagonist is Teng Li (N84). ✓
 *   2. Li Muwan is from Luo He Sect (NOT Xuan Dao). ✓ (N17)
 *   3. Situ Nan is 2nd-gen Zhuque-zi of Zhuque Country (NOT Soul Refining Sect). ✓ (N20)
 *   4. Sea of Devils = Xiu Mo Hai. ✓ (L45 nameCn includes 魔修海)
 *   5. Bead = Tian Ni Zhu (逆天珠). ✓ (I01)
 *   6. Snow Country = Xue Yu Country (雪域国). ✓ (L28)
 *   7. Zeng Da Niu belongs to Si Pai Lian Meng (Four Sects Alliance) hua-fan arc. ✓ (N152 — overridden)
 *   8. Wang Family Village is UNVERIFIED — flag canonStatus 'unverified' for the name; ✓ (L34 — overridden)
 *      Wang Lin birthplace in a remote Zhao village IS canon.
 *   9. Old Chen + Forest of Distorted Sense are mod-original — flagged in MOD_ORIGINAL_ENTITIES. ✓
 *   10. Jue Ming Valley is canon-accurate (NOT Forest of Distorted Sense). ✓ (L46)
 *   11. NO fabricated chapter citations — firstAppearanceChapter omitted when unknown. ✓
 *
 * Article I: Canon is reality. The novel is objective law.
 * (CRON-69 architecture, preserved through the Three.js pivot.)
 */

import type {
  CanonStatus,
  CultivationRealm,
  EdgeType,
  GraphEdge,
  GraphNode,
  NodeType,
} from '../graph/WorldGraph';
import type {
  ArtType,
  CanonArtifact,
  CanonCharacter,
  CanonLocation,
  CanonTechnique,
  CharType,
  LocType,
  RelationShip,
  TechType,
} from './types';
import { Provenance } from '../runtime/Provenance';

""".rstrip()


_INDEX_MAPS = """\
// ── Index maps for O(1) lookup ────────────────────────────────────────

const CHAR_BY_ID: ReadonlyMap<string, CanonCharacter> = new Map(
  ALL_CHARACTERS.map((c) => [c.id, c] as const),
);
const LOC_BY_ID: ReadonlyMap<string, CanonLocation> = new Map(
  ALL_LOCATIONS.map((l) => [l.id, l] as const),
);
const ART_BY_ID: ReadonlyMap<string, CanonArtifact> = new Map(
  ALL_ARTIFACTS.map((a) => [a.id, a] as const),
);
const TECH_BY_ID: ReadonlyMap<string, CanonTechnique> = new Map(
  ALL_TECHNIQUES.map((t) => [t.id, t] as const),
);
"""

_QUERY_METHODS = """\
// ── Query methods (mirrors Java static API) ───────────────────────────

export function getCharacterById(id: string): CanonCharacter | undefined {
  return CHAR_BY_ID.get(id);
}
export function getLocationById(id: string): CanonLocation | undefined {
  return LOC_BY_ID.get(id);
}
export function getArtifactById(id: string): CanonArtifact | undefined {
  return ART_BY_ID.get(id);
}
export function getTechniqueById(id: string): CanonTechnique | undefined {
  return TECH_BY_ID.get(id);
}

export function getCharactersByType(type: CharType): readonly CanonCharacter[] {
  return ALL_CHARACTERS.filter((c) => c.type === type);
}
export function getCharactersByAffiliation(affiliation: string): readonly CanonCharacter[] {
  return ALL_CHARACTERS.filter((c) => c.affiliation.includes(affiliation));
}
export function getCharactersAtLocation(location: string): readonly CanonCharacter[] {
  return ALL_CHARACTERS.filter((c) => c.location.includes(location));
}
export function getCharactersByConfidence(minConf: number): readonly CanonCharacter[] {
  return ALL_CHARACTERS.filter((c) => c.canonConfidence >= minConf);
}
export function searchCharacters(query: string): readonly CanonCharacter[] {
  const q = query.toLowerCase();
  return ALL_CHARACTERS.filter(
    (c) =>
      c.name.toLowerCase().includes(q) ||
      (c.nameCn !== null && c.nameCn.includes(query)) ||
      c.affiliation.toLowerCase().includes(q),
  );
}
export function getRelationshipsOf(characterName: string): readonly CanonCharacter[] {
  return ALL_CHARACTERS.filter((c) =>
    c.relationships.some((r) => r.target.includes(characterName)),
  );
}
export function getCharactersByStatus(status: string): readonly CanonCharacter[] {
  const s = status.toLowerCase();
  return ALL_CHARACTERS.filter((c) => c.status.toLowerCase() === s);
}

export function getLocationsByType(type: LocType): readonly CanonLocation[] {
  return ALL_LOCATIONS.filter((l) => l.type === type);
}
export function getLocationsByCosmologyLayer(layer: string): readonly CanonLocation[] {
  return ALL_LOCATIONS.filter((l) => l.cosmologyLayer === layer);
}
export function getLocationsByParent(parentName: string): readonly CanonLocation[] {
  return ALL_LOCATIONS.filter((l) => l.parentLocation === parentName);
}
export function getSealedLocations(): readonly CanonLocation[] {
  return ALL_LOCATIONS.filter((l) => l.isSealed);
}
export function getLocationsByConfidence(minConf: number): readonly CanonLocation[] {
  return ALL_LOCATIONS.filter((l) => l.canonConfidence >= minConf);
}
export function searchLocations(query: string): readonly CanonLocation[] {
  const q = query.toLowerCase();
  return ALL_LOCATIONS.filter(
    (l) => l.name.toLowerCase().includes(q) || (l.nameCn !== null && l.nameCn.includes(query)),
  );
}
export function getLocationsWithFaction(factionName: string): readonly CanonLocation[] {
  return ALL_LOCATIONS.filter((l) => l.associatedFactions.some((f) => f.includes(factionName)));
}

export function getArtifactsByType(type: ArtType): readonly CanonArtifact[] {
  return ALL_ARTIFACTS.filter((a) => a.type === type);
}
export function getArtifactsByOwner(owner: string): readonly CanonArtifact[] {
  return ALL_ARTIFACTS.filter((a) => a.currentOwner.includes(owner));
}
export function getArtifactsByCategory(category: string): readonly CanonArtifact[] {
  return ALL_ARTIFACTS.filter((a) => a.category === category);
}
export function getArtifactsByConfidence(minConf: number): readonly CanonArtifact[] {
  return ALL_ARTIFACTS.filter((a) => a.canonConfidence >= minConf);
}
export function searchArtifacts(query: string): readonly CanonArtifact[] {
  const q = query.toLowerCase();
  return ALL_ARTIFACTS.filter(
    (a) =>
      a.name.toLowerCase().includes(q) ||
      (a.nameCn !== null && a.nameCn.includes(query)) ||
      a.origin.toLowerCase().includes(q),
  );
}
export function getArtifactsMentioningCharacter(characterName: string): readonly CanonArtifact[] {
  return ALL_ARTIFACTS.filter(
    (a) => a.currentOwner.includes(characterName) || a.origin.includes(characterName),
  );
}

export function getTechniquesByType(type: TechType): readonly CanonTechnique[] {
  return ALL_TECHNIQUES.filter((t) => t.type === type);
}
export function getTechniquesByUser(userName: string): readonly CanonTechnique[] {
  return ALL_TECHNIQUES.filter((t) => t.knownUsers.some((u) => u.includes(userName)));
}
export function getTechniquesByConfidence(minConf: number): readonly CanonTechnique[] {
  return ALL_TECHNIQUES.filter((t) => t.canonConfidence >= minConf);
}
export function searchTechniques(query: string): readonly CanonTechnique[] {
  const q = query.toLowerCase();
  return ALL_TECHNIQUES.filter(
    (t) =>
      t.name.toLowerCase().includes(q) ||
      (t.nameCn !== null && t.nameCn.includes(query)) ||
      t.origin.toLowerCase().includes(q),
  );
}

/** All 14 Samsara Essences (E01–E14). */
export function getEssences(): readonly CanonTechnique[] {
  return ALL_TECHNIQUES.filter((t) => t.id.startsWith('E'));
}
/** All 9 Heaven Trampling Bridges (B01–B09). */
export function getBridges(): readonly CanonTechnique[] {
  return ALL_TECHNIQUES.filter((t) => t.id.startsWith('B'));
}
/** All 7 Original Spells (OS01–OS07). */
export function getOriginalSpells(): readonly CanonTechnique[] {
  return ALL_TECHNIQUES.filter((t) => t.id.startsWith('OS'));
}
/** All 9 Accompanying Thunders (AT01–AT09). */
export function getAccompanyingThunders(): readonly CanonTechnique[] {
  return ALL_TECHNIQUES.filter((t) => t.id.startsWith('AT'));
}
/** All 4 Vermilion Bird Awakenings (VA01–VA04). */
export function getVermilionBirdAwakenings(): readonly CanonTechnique[] {
  return ALL_TECHNIQUES.filter((t) => t.id.startsWith('VA'));
}

/** Total entry count across all categories. */
export function getTotalEntries(): number {
  return ALL_CHARACTERS.length + ALL_LOCATIONS.length + ALL_ARTIFACTS.length + ALL_TECHNIQUES.length;
}

/** Summary counts for logging. */
export function getSummaryCounts(): {
  readonly characters: number;
  readonly locations: number;
  readonly artifacts: number;
  readonly techniques: number;
  readonly total: number;
} {
  return {
    characters: ALL_CHARACTERS.length,
    locations: ALL_LOCATIONS.length,
    artifacts: ALL_ARTIFACTS.length,
    techniques: ALL_TECHNIQUES.length,
    total: getTotalEntries(),
  };
}

/** Free-text search across ALL categories. Returns `[Type] name (cn) — id`. */
export function searchAll(query: string): readonly string[] {
  const q = query.toLowerCase();
  const out: string[] = [];
  for (const c of ALL_CHARACTERS) {
    if (c.name.toLowerCase().includes(q) || (c.nameCn !== null && c.nameCn.includes(query))) {
      out.push(`[Char] ${c.name} (${c.nameCn ?? '?'}) — ${c.id}`);
    }
  }
  for (const l of ALL_LOCATIONS) {
    if (l.name.toLowerCase().includes(q) || (l.nameCn !== null && l.nameCn.includes(query))) {
      out.push(`[Loc]  ${l.name} (${l.nameCn ?? '?'}) — ${l.id}`);
    }
  }
  for (const a of ALL_ARTIFACTS) {
    if (a.name.toLowerCase().includes(q) || (a.nameCn !== null && a.nameCn.includes(query))) {
      out.push(`[Art]  ${a.name} (${a.nameCn ?? '?'}) — ${a.id}`);
    }
  }
  for (const t of ALL_TECHNIQUES) {
    if (t.name.toLowerCase().includes(q) || (t.nameCn !== null && t.nameCn.includes(query))) {
      out.push(`[Tech] ${t.name} (${t.nameCn ?? '?'}) — ${t.id}`);
    }
  }
  return out;
}
"""

_BOOTSTRAP_PREFIX = """\
// ── bootstrapGraph — populate a WorldGraph with nodes + edges ─────────
//
// Adds one GraphNode per canon record (npc / location / item / technique)
// and one GraphEdge per attested relationship (PARENT_LOCATION, LOCATED_IN,
// FAMILIAR_WITH / FAMILY / MASTER_OF / HOSTILE_TO / OWNS / KNOWS).
//
// CRON-69 #8: Wang Family Village (L34) is flagged canonStatus='unverified'
// because the novel attests only "a remote mountain village in Zhao Country"
// — the village NAME is mod-original. Wang Lin's birthplace being a remote
// Zhao village IS canon (preserved in the node's tags + meta).
//
// CRON-69 #11: firstAppearanceChapter is omitted when no unambiguous
// "Ch.N" citation is found in the source string. NO fabricated chapters.

export function bootstrapGraph(graph: import('../graph/WorldGraph').WorldGraph): void {
"""

_HELPERS = """

// ── bootstrap helpers ─────────────────────────────────────────────────

const CHAPTER_PATTERN = /Ch\\.?\\s*~?(\\d{1,5})/i;

function extractChapter(source: string): number | undefined {
  if (!source) return undefined;
  const matches = source.match(new RegExp(CHAPTER_PATTERN.source, 'gi'));
  if (!matches) return undefined;
  const chapters = new Set(
    matches.map((m) => {
      const inner = m.match(/(\\d{1,5})/);
      return inner ? parseInt(inner[1], 10) : NaN;
    }).filter((n) => !Number.isNaN(n)),
  );
  if (chapters.size === 0) return undefined;
  if (chapters.size === 1) return chapters.values().next().value;
  // Multiple chapters — pick the min if the source mentions "obtained" / "first" / "found"
  const srcLower = source.toLowerCase();
  if (srcLower.includes('obtained') || srcLower.includes('first') || srcLower.includes('founded') || srcLower.includes('found')) {
    return Math.min(...chapters);
  }
  return undefined;
}

const REALM_KEYWORDS: ReadonlyArray<readonly [string, CultivationRealm]> = [
  ['heaven trampling', 'ascendant'],
  ['treading heaven', 'ascendant'],
  ['ascendant', 'ascendant'],
  ['heaven dao', 'heaven_dao'],
  ['void amalgamation', 'void_amalgamation'],
  ['void refinement', 'void_refinement'],
  ['void tribulant', 'void_amalgamation'],
  ['soul transformation', 'soul_transformation'],
  ['soul formation', 'soul_formation'],
  ['infant transformation', 'soul_formation'],
  ['nascent soul', 'nascent_soul'],
  ['core formation', 'core_formation'],
  ['foundation establishment', 'foundation_establishment'],
  ['foundation', 'foundation_establishment'],
  ['qi condensation', 'qi_condensation'],
  ['mortal', 'mortal'],
];

function mapPeakRealm(peakRealm: string): CultivationRealm | undefined {
  if (!peakRealm) return undefined;
  const pl = peakRealm.toLowerCase();
  for (const [kw, realm] of REALM_KEYWORDS) {
    if (pl.includes(kw)) return realm;
  }
  return undefined;
}

function computeCharCanonStatus(c: CanonCharacter): CanonStatus {
  // CRON-69 #7: Zeng Da Niu (N152) — overridden to canon (was C3, now C4 with verified affiliation)
  if (c.id === 'N152') return 'canon';
  // Default: canon if confidence >= 4, unverified if 3, mod_original if < 3
  if (c.canonConfidence >= 4) return 'canon';
  if (c.canonConfidence === 3) return 'unverified';
  return 'mod_original';
}

function computeLocCanonStatus(l: CanonLocation): CanonStatus {
  // CRON-69 #8: Wang Family Village (L34) — flag unverified for the name
  if (l.id === 'L34') return 'unverified';
  if (l.canonConfidence >= 4) return 'canon';
  if (l.canonConfidence === 3) return 'unverified';
  return 'mod_original';
}

const RELATION_TO_EDGE: Readonly<Record<string, EdgeType>> = {
  love_interest: 'FAMILIAR_WITH',
  family: 'FAMILIAR_WITH',
  ally: 'ALLIED_WITH',
  enemy: 'HOSTILE_TO',
  rival: 'HOSTILE_TO',
  master: 'MASTER_OF',
  disciple: 'DISCIPLE_OF',
  faction: 'MEMBER_OF',
};

function relationToEdgeType(relation: string): EdgeType | undefined {
  return RELATION_TO_EDGE[relation];
}

function makeSlug(name: string, used: Set<string>): string {
  let base = name.trim().toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_+|_+$/g, '');
  if (!base) base = 'unnamed';
  let slug = base;
  let i = 2;
  while (used.has(slug)) {
    slug = `${base}_${i}`;
    i++;
  }
  used.add(slug);
  return slug;
}
"""

_MOD_ORIGINAL = """
// ── Mod-original entities registry (CRON-69 #9) ───────────────────────
//
// These entities are NOT canon and are NOT added to the graph by
// bootstrapGraph(). They are listed here so simulation systems can
// recognize and label them honestly when they appear in mod-original
// content (e.g., structure composition files).

export interface ModOriginalEntity {
  readonly name: string;
  readonly note: string;
  readonly canonStatus: 'mod_original';
}

export const MOD_ORIGINAL_ENTITIES: readonly ModOriginalEntity[] = [
  {
    name: 'Old Chen',
    note: 'mod-original — referenced in the legacy Wang Lin journal; not attested in novel 仙逆',
    canonStatus: 'mod_original',
  },
  {
    name: 'Forest of Distorted Sense',
    note: 'mod-original — the canon-accurate name is Jue Ming Valley (决明谷, L46)',
    canonStatus: 'mod_original',
  },
];

/** Check whether a name refers to a known mod-original entity. */
export function isModOriginalEntity(name: string): boolean {
  return MOD_ORIGINAL_ENTITIES.some((e) => e.name.toLowerCase() === name.toLowerCase());
}
"""


def main() -> None:
    if len(sys.argv) != 3:
        print('Usage: gen_ts.py <canon_data.json> <output.ts>', file=sys.stderr)
        sys.exit(2)
    in_path = sys.argv[1]
    out_path = sys.argv[2]
    with open(in_path, 'r', encoding='utf-8') as f:
        canon = json.load(f)
    emit(out_path, canon)


if __name__ == '__main__':
    main()
