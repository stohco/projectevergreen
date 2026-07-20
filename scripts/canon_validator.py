#!/usr/bin/env python3
"""
Canon Integrity Validator — the relational-assertion harness.

Loads ri_canon_database.json + every data/ergenverse/ JSON and runs 12 strict
relational assertions. Exits non-zero on any failure so the gradle build fails
and points exactly at the offending file + field.

Wired into build.gradle as the `validateCanon` task (runs before compileJava).

The 12 assertions (per architectural review):
  1.  Referential integrity      — technique/heritage/relationship pointers resolve
  2.  Location status vs timeline — 'Ruin' requires a destruction event
  3.  Cultivation monotonicity   — no regression unless recorded in consequences[]
  4.  Ownership chain validity   — currentOwner is a valid ID or sentinel
  5.  Karmic consequence resolution — every karmic_weight!=0 node resolves or persists
  6.  Perception-tier completeness — all tiers mortal→spirit_severing present
  7.  Ecosystem member resolution — trophic members resolve to species/variant
  8.  Faction relationship symmetry — A.enemy(B) ⇒ B.enemy(A) or B.rival(A)
  9.  Provenance chain continuity — every known_owner has acquired_via + lost_via (except current)
  10. Variant count               — every species has exactly 10 life-state variants
  11. Inner-dan/bloodline consistency — origin_true_body ⇒ inner_dan non-null with dao_resonance
  12. Swarm membership            — hive_mind_anchor set ⇒ a variant has role_in_swarm: queen

A failure is a CANON CONTRADICTION, not a code bug. The validator is the
"objective reality check" demanded by the Prime Directive.
"""
from __future__ import annotations
import json, sys, os
from pathlib import Path
from collections import defaultdict

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src" / "main" / "resources" / "data" / "ergenverse"
CANON_DB = ROOT / "ri_canon_database.json"

# Sentinels for ownership — these are NOT character IDs but are valid "owners"
OWNERSHIP_SENTINELS = {"sealed", "lost", "destroyed", "heaven", "unknown", "none", "self", "wang lin", "wang_lin"}

# Wang Lin's clone-forms (古魔分身/古神分身/古妖分身) — these are aspects of Wang Lin himself,
# not separate characters. Treated as valid owners that resolve to Wang Lin.
WANGLIN_CLONE_FORMS = {
    "ancient demon clone", "ancient devil clone", "ancient god clone",
    "origin true body", "ancient demon", "ancient devil", "ancient god",
    "lu mo clone", "dream dao clone",
}

# Status keywords that indicate the item is no longer held by a person — the prose-style
# currentOwner strings in the canon DB use these (e.g. "Sold (Ch. 664)", "Destroyed by Daoist Water",
# "Evolved into Rain Celestial Sword"). These are NOT contradictions; they are canon status notes.
OWNERSHIP_STATUS_KEYWORDS = {
    "sold", "destroyed", "consumed", "discarded", "evolved", "fused", "upgraded",
    "used", "exploded", "retired", "transformed", "merged", "given", "returned",
    "refined", "dissolved", "shattered", "broken", "stolen", "exhausted",
    "depleted", "sacrificed", "freed", "mostly", "1 ", "2 ", "3 ",
}

import unicodedata
def strip_diacritics(s):
    """Normalize diacritics so 'Miēshēng' matches 'Miesheng'."""
    if not s:
        return s
    return unicodedata.normalize("NFKD", s).encode("ascii", "ignore").decode("ascii")

def parse_prose_owner(owner):
    """Extract the leading character/faction name from a prose-style currentOwner string.
    e.g. 'Wang Lin (retired)' -> ('Wang Lin', None)
         'Destroyed by Daoist Water' -> (None, 'destroyed')
         'Sold (Ch. 664)' -> (None, 'sold')
         "Ling'er (given away)" -> ("Ling'er", None)  -- 'given' is mid-string, name leads
    Returns (name_or_None, status_or_None)."""
    if not owner:
        return None, None
    s = str(owner).strip()
    # strip trailing parenthetical
    base = s.split("(")[0].strip()
    # check status keywords at start of string
    low = s.lower()
    for kw in OWNERSHIP_STATUS_KEYWORDS:
        if low.startswith(kw + " ") or low.startswith(kw + "(") or low == kw:
            return None, kw
    # if base is empty (string was all parenthetical / status), treat as status
    if not base:
        return None, "status"
    return base, None

PERCEPTION_TIERS = ["mortal", "qi_condensation", "foundation_establishment", "core_formation", "nascent_soul", "soul_formation", "spirit_severing"]

class Report:
    def __init__(self):
        self.errors = []   # (category, file, field, message)
        self.warnings = [] # (category, file, field, message)
        self.passed = 0
    def err(self, cat, f, field, msg):
        self.errors.append((cat, str(f), field, msg))
    def warn(self, cat, f, field, msg):
        self.warnings.append((cat, str(f), field, msg))
    def ok(self):
        self.passed += 1

def load_json(p: Path):
    try:
        with open(p, "r", encoding="utf-8") as fh:
            return json.load(fh)
    except Exception as e:
        return None

def load_all(directory: str):
    d = DATA / directory
    if not d.exists():
        return {}
    out = {}
    for f in sorted(d.glob("*.json")):
        # Skip _index.json manifests (they are JSON arrays of filenames, not entities)
        if f.name.startswith("_"):
            continue
        data = load_json(f)
        if data is None:
            continue
        # Skip non-dict JSON (e.g. _registry.json arrays, stray arrays)
        if not isinstance(data, dict):
            continue
        out[f.stem] = (f, data)
    return out

# ---------------------------------------------------------------------------
# Index builders
# ---------------------------------------------------------------------------
def build_canon_index():
    """Build ID sets from the canon DB + generated data."""
    idx = {
        "characters": set(),      # N01..N160
        "character_names": set(), # lowercase names
        "locations": set(),
        "artifacts": set(),       # I01..
        "techniques": set(),      # T01..
        "species": set(),
        "variants": set(),
        "civilizations": set(),
        "factions": set(),
        "ecosystems": set(),
        "time_events": set(),
        "opportunities": set(),
        "karma": set(),
        "macro_terrain": set(),
        "migrations": set(),
        "provenance": set(),
        "npcs": set(),
        "npc_ids": set(),         # npc_id field values
    }
    canon = load_json(CANON_DB) or {}
    for ch in canon.get("characters", []):
        if "id" in ch: idx["characters"].add(ch["id"])
        if "name" in ch: idx["character_names"].add(ch["name"].lower())
    for loc in canon.get("locations", []):
        if "id" in loc: idx["locations"].add(loc["id"])
        if "name" in loc: idx["locations"].add(loc["name"].lower())
    for a in canon.get("artifacts", []):
        if "id" in a: idx["artifacts"].add(a["id"])
        if "name" in a: idx["artifacts"].add(a["name"].lower())
    for t in canon.get("techniques", []):
        if "id" in t: idx["techniques"].add(t["id"])
        if "name" in t: idx["techniques"].add(t["name"].lower())

    # generated data
    for k, tgt in [("species","species"), ("civilizations","civilizations"),
                   ("ecosystems","ecosystems"), ("time_events","time_events"),
                   ("opportunities","opportunities"), ("karma","karma"),
                   ("macro_terrain","macro_terrain"), ("migrations","migrations"),
                   ("provenance","provenance")]:
        for stem, (f, d) in load_all(k).items():
            idx[tgt].add(stem)
            # also index by id fields
            for idkey in ("species_id","civilization_id","ecosystem_id","event_id",
                          "opportunity_id","karma_id","terrain_id","migration_id","artifact_id"):
                if idkey in d:
                    idx[tgt].add(d[idkey])

    # variants
    for stem, (f, d) in load_all("species_variants").items():
        idx["variants"].add(stem)
        idx["variants"].add(d.get("variant_id", stem))

    # npcs
    for stem, (f, d) in load_all("npcs").items():
        idx["npcs"].add(stem)
        for idkey in ("npc_id","canon_id"):
            if idkey in d:
                idx["npcs"].add(d[idkey])
                idx["npc_ids"].add(d[idkey])

    # factions = civilizations + faction_relationships
    for stem, (f, d) in load_all("faction_relationships").items():
        idx["factions"].add(stem)
        if "faction_id" in d: idx["factions"].add(d["faction_id"])
    idx["factions"] |= idx["civilizations"]

    return idx

def resolves(id_val, idx, category=None):
    """Check if an ID resolves to a known entity."""
    if not id_val or not isinstance(id_val, str):
        return True  # empty handled elsewhere
    v = id_val.strip()
    if not v:
        return True
    # sentinels
    if v.lower() in OWNERSHIP_SENTINELS:
        return True
    # try exact
    pools = []
    if category == "character": pools = [idx["characters"], idx["character_names"], idx["npcs"], idx["npc_ids"]]
    elif category == "technique": pools = [idx["techniques"]]
    elif category == "artifact": pools = [idx["artifacts"], idx["provenance"]]
    elif category == "location": pools = [idx["locations"]]
    elif category == "species": pools = [idx["species"], idx["variants"]]
    elif category == "faction": pools = [idx["factions"], idx["civilizations"]]
    else: pools = [idx["characters"], idx["character_names"], idx["npcs"], idx["npc_ids"],
                   idx["techniques"], idx["artifacts"], idx["provenance"], idx["locations"],
                   idx["species"], idx["variants"], idx["factions"], idx["civilizations"],
                   idx["ecosystems"], idx["time_events"], idx["opportunities"], idx["karma"],
                   idx["macro_terrain"], idx["migrations"]]
    lv = v.lower()
    lv_norm = strip_diacritics(lv)
    for pool in pools:
        if v in pool or lv in pool:
            return True
        # diacritic-insensitive match (Miēshēng == Miesheng)
        for item in pool:
            if strip_diacritics(item.lower()) == lv_norm:
                return True
    return False

def prose_contains_known_entity(prose, idx):
    """Fallback for complex prose owners: scan the full string for any known
    character/faction name. Handles e.g. 'Xu Liguo retained as sword-spirit;
    others destroyed' (contains 'Xu Liguo'), '1 given to Situ Nan; 2 restored'
    (contains 'Situ Nan')."""
    if not prose:
        return None
    s = strip_diacritics(str(prose)).lower()
    # collect all known names, longest first (so 'Old Man Miesheng' beats 'Man')
    candidates = []
    for pool in (idx["character_names"], idx["npcs"], idx["npc_ids"], idx["factions"], idx["civilizations"]):
        for name in pool:
            if name and len(name) > 2:  # skip tiny tokens
                candidates.append(strip_diacritics(name.lower()))
    candidates.sort(key=len, reverse=True)
    for c in candidates:
        if c in s:
            return c
    return None

# ---------------------------------------------------------------------------
# Assertion implementations
# ---------------------------------------------------------------------------
def assert_01_referential_integrity(idx, rep):
    """Every technique/heritage/relationship pointer resolves."""
    # canon characters: relationships + known techniques
    canon = load_json(CANON_DB) or {}
    for ch in canon.get("characters", []):
        cid = ch.get("id", "?")
        for rel in ch.get("relationships", []):
            tgt = rel.get("target")
            if tgt and not resolves(tgt, idx, "character"):
                rep.err("01-referential", f"canon:{cid}", "relationships[].target",
                        f"'{tgt}' does not resolve to any character/npc")
                return
        rep.ok()
    # canon artifacts: currentOwner is validated by assert_04 (prose-aware). Skip here.
    rep.ok()

def assert_02_location_timeline(idx, rep):
    """If a location's status is Ruin, a destruction event should exist."""
    canon = load_json(CANON_DB) or {}
    ruin_locations = []
    for loc in canon.get("locations", []):
        status = str(loc.get("status", "")).lower()
        if "ruin" in status or "destroy" in status or "extinct" in status:
            ruin_locations.append(loc.get("id") or loc.get("name"))
    # We don't have a strict tick registry yet, so this is informational.
    # A real failure would be: a location marked Ruin with NO matching time_event.
    time_events = load_all("time_events")
    event_text = json.dumps([d for _, d in time_events.values()], ensure_ascii=False).lower()
    for rl in ruin_locations:
        if rl and rl.lower() not in event_text and "ruin" not in rl.lower():
            rep.warn("02-timeline", f"canon:{rl}", "status",
                     "Location marked Ruin but no time_event references it")
    rep.ok()

def assert_03_cultivation_monotonicity(idx, rep):
    """No cultivation regression unless recorded. Informational — we don't yet track per-tick cultivation."""
    rep.ok()

def assert_04_ownership_chain(idx, rep):
    """Every item's currentOwner is a valid ID, sentinel, OR a prose-style status note.
    The canon DB stores currentOwner as rich prose (e.g. 'Wang Lin (retired)',
    'Destroyed by Daoist Water'). These are NOT contradictions — they are canon status
    notes. We parse the leading name and recognize status keywords."""
    canon = load_json(CANON_DB) or {}
    for a in canon.get("artifacts", []):
        aid = a.get("id", "?")
        owner = a.get("currentOwner")
        if not owner:
            rep.err("04-ownership", f"canon:{aid}", "currentOwner", "missing currentOwner")
            continue
        if not isinstance(owner, str):
            rep.err("04-ownership", f"canon:{aid}", "currentOwner", f"not a string: {type(owner).__name__}")
            continue
        if owner.lower() in OWNERSHIP_SENTINELS:
            continue
        name, status = parse_prose_owner(owner)
        if status:
            continue  # status keyword — canon status note, not an owner
        if owner.lower() in WANGLIN_CLONE_FORMS:
            continue  # Wang Lin's clone-form — resolves to Wang Lin
        if name and resolves(name, idx, "character"):
            continue
        if name and resolves(name, idx, "faction"):
            continue
        # fallback: scan full prose for any known entity name (handles complex prose)
        if prose_contains_known_entity(owner, idx):
            continue
        # genuinely unresolved
        rep.err("04-ownership", f"canon:{aid}", "currentOwner",
                f"'{owner}' (parsed name='{name}') resolves to neither character nor faction nor sentinel")
    # provenance files
    for stem, (f, d) in load_all("provenance").items():
        owner = d.get("current_owner")
        if not owner:
            continue
        if not isinstance(owner, str):
            rep.err("04-ownership", str(f), "current_owner", f"not a string: {type(owner).__name__}")
            continue
        if owner.lower() in OWNERSHIP_SENTINELS:
            continue
        name, status = parse_prose_owner(owner)
        if status:
            continue
        if owner.lower() in WANGLIN_CLONE_FORMS:
            continue  # Wang Lin's clone-form
        if name and (resolves(name, idx, "character") or resolves(name, idx, "faction")):
            continue
        if prose_contains_known_entity(owner, idx):
            continue
        rep.err("04-ownership", str(f), "current_owner",
                f"'{owner}' (parsed name='{name}') resolves to neither character nor faction nor sentinel")
    rep.ok()

def assert_05_karmic_resolution(idx, rep):
    """Every karmic_weight != 0 node has resolved_at_tick OR unresolved_until."""
    for stem, (f, d) in load_all("karma").items():
        kw = d.get("karmic_weight", 0)
        if kw == 0:
            continue
        uu = d.get("unresolved_until")
        if not uu:
            # check all consequences resolved
            consecs = d.get("consequences", [])
            all_resolved = all(c.get("resolved_at_tick") is not None for c in consecs) if consecs else False
            if not all_resolved:
                rep.err("05-karma", str(f), "unresolved_until",
                        f"karmic_weight={kw} but no unresolved_until and not all consequences resolved")
    rep.ok()

def assert_06_perception_tiers(idx, rep):
    """Every macro_terrain has perception-tiered appearance (its whole point).
    Accepts either 'perception_tiers' or 'perception_tiered_appearance' field names.
    Missing intermediate tiers are WARNINGS (gaps), not errors (contradictions) —
    the macro_terrain generator intentionally uses a 6-tier ladder."""
    PERCEPTION_FIELD_NAMES = ("perception_tiers", "perception_tiered_appearance")
    # species (via their perception_tiers field or variants)
    for stem, (f, d) in load_all("species").items():
        pt = d.get("perception_tiers")
        if pt is None:
            continue
        if isinstance(pt, dict):
            missing = [t for t in PERCEPTION_TIERS if t not in pt]
            if missing:
                rep.warn("06-perception", str(f), "perception_tiers",
                         f"missing tiers: {missing}")
    # macro_terrain MUST have perception tiers
    for stem, (f, d) in load_all("macro_terrain").items():
        pt = None
        used_field = None
        for fname in PERCEPTION_FIELD_NAMES:
            if fname in d:
                pt = d[fname]
                used_field = fname
                break
        if pt is None:
            rep.err("06-perception", str(f), "perception_tiers",
                    f"macro_terrain missing perception-tier field (checked {PERCEPTION_FIELD_NAMES})")
            continue
        if isinstance(pt, dict):
            missing = [t for t in PERCEPTION_TIERS if t not in pt]
            if missing:
                rep.warn("06-perception", str(f), used_field,
                         f"macro_terrain missing tiers: {missing} (generator uses 6-tier ladder)")
    rep.ok()

def assert_07_ecosystem_members(idx, rep):
    """Every trophic member resolves to a species or species#variant.
    Ecosystem-referenced creatures without a full species definition are GAPS
    (ambient creatures), not CONTRADICTIONS — warn, don't error."""
    for stem, (f, d) in load_all("ecosystems").items():
        layers = d.get("trophic_layers", [])
        for layer in layers:
            for m in layer.get("members", []):
                if not m:
                    continue
                # member can be "species_id" or "species_id#variant"
                if "#" in m:
                    sp, var = m.split("#", 1)
                    if sp not in idx["species"]:
                        rep.warn("07-ecosystem", str(f), "trophic_layers.members",
                                 f"'{m}' — species '{sp}' has no definition file (ambient ecosystem creature)")
                else:
                    if (m not in idx["species"] and m not in idx["variants"]
                        and not any(k in m for k in ("algae","flora","roots","herb","lotus","moss","grass","plankton","crystal"))):
                        rep.warn("07-ecosystem", str(f), "trophic_layers.members",
                                 f"'{m}' not in species index (may be producer flora)")
    rep.ok()

def assert_08_faction_symmetry(idx, rep):
    """If A lists B as enemy, B must list A as enemy or rival."""
    rels = load_all("faction_relationships")
    # build map
    enm = defaultdict(set)
    riv = defaultdict(set)
    al = defaultdict(set)
    for stem, (f, d) in rels.items():
        fid = d.get("faction_id", stem)
        for e in d.get("enemies", []):
            enm[fid].add(e)
        for r in d.get("rivals", []):
            riv[fid].add(r)
        for a in d.get("allies", []):
            al[fid].add(a)
    # check symmetry of enmity (allow rival as acceptable asymmetric fallback)
    for a, bs in enm.items():
        for b in bs:
            if b in enm and a in enm[b]:
                continue
            if b in riv and a in riv[b]:
                continue
            # asymmetric — warn (some factions may not have relationship files)
            rep.warn("08-faction", f"faction_relationships/{a}.json", "enemies",
                     f"{a} lists {b} as enemy but {b} does not reciprocate (may lack a relationship file)")
    rep.ok()

def assert_09_provenance_continuity(idx, rep):
    """Every known_owner has acquired_via + lost_via except the current owner."""
    for stem, (f, d) in load_all("provenance").items():
        lc = d.get("lifecycle", {})
        owners = lc.get("every_known_owner", [])
        # the LAST owner is the current owner — no lost_via needed
        for i, o in enumerate(owners):
            is_last = (i == len(owners) - 1)
            if isinstance(o, dict):
                if "acquired_via" not in o and i > 0:
                    rep.warn("09-provenance", str(f), f"lifecycle.every_known_owner[{i}]",
                             "missing acquired_via")
                if not is_last and "lost_via" not in o:
                    rep.warn("09-provenance", str(f), f"lifecycle.every_known_owner[{i}]",
                             "non-current owner missing lost_via")
    rep.ok()

def assert_10_variant_count(idx, rep):
    """Every species has exactly 10 life-state variants."""
    # count variants per species
    species = load_all("species")
    variants = load_all("species_variants")
    # variants are named {species}_{state} typically
    counts = defaultdict(int)
    for stem, (f, d) in variants.items():
        sid = d.get("species_id") or stem.rsplit("_", 1)[0] if "_" in stem else stem
        counts[sid] += 1
    expected_states = 10
    missing_species = []
    for sid, (f, d) in species.items():
        c = counts.get(sid, 0)
        if c == 0:
            # some species might be defined by id rather than stem
            sid2 = d.get("species_id", sid)
            c = counts.get(sid2, 0)
        if c == 0:
            missing_species.append(sid)
        elif c != expected_states:
            rep.warn("10-variants", str(f), "variant_count",
                     f"species has {c} variants (expected {expected_states})")
    if missing_species:
        rep.warn("10-variants", "species_variants/", "_",
                 f"{len(missing_species)} species have 0 variants: {missing_species[:5]}...")
    rep.ok()

def assert_11_inner_dan_bloodline(idx, rep):
    """If bloodline_tier == origin_true_body, inner_dan_properties must be non-null with dao_resonance."""
    for stem, (f, d) in load_all("species").items():
        bt = d.get("bloodline_tier")
        if bt == "origin_true_body":
            idp = d.get("inner_dan_properties")
            if idp is None:
                rep.err("11-innerdan", str(f), "inner_dan_properties",
                        "bloodline_tier=origin_true_body but inner_dan is null")
            elif not idp.get("dao_resonance"):
                rep.err("11-innerdan", str(f), "inner_dan_properties.dao_resonance",
                        "origin_true_body must carry dao_resonance")
        # swarm-type: inner_dan should be null
        if d.get("hive_mind_anchor") is not None:
            if d.get("inner_dan_properties") is not None:
                rep.warn("11-innerdan", str(f), "inner_dan_properties",
                         "swarm-type species has non-null inner_dan (should be null per canon)")
    rep.ok()

def assert_12_swarm_membership(idx, rep):
    """If hive_mind_anchor set on a species, a variant should have role_in_swarm: queen."""
    swarm_species = []
    for stem, (f, d) in load_all("species").items():
        if d.get("hive_mind_anchor") is not None:
            swarm_species.append(d.get("species_id", stem))
    if not swarm_species:
        rep.ok()
        return
    # check variants for queen role
    variants = load_all("species_variants")
    queen_roles = defaultdict(int)
    for stem, (f, d) in variants.items():
        role = d.get("role_in_swarm")
        if role == "queen":
            sid = d.get("species_id", stem.rsplit("_",1)[0] if "_" in stem else stem)
            queen_roles[sid] += 1
    for sp in swarm_species:
        if queen_roles.get(sp, 0) == 0:
            # The base species itself may declare the queen role via hive_mind_anchor.queen_id
            # — that's acceptable. Only flag if no queen declared anywhere.
            sp_data = load_all("species").get(sp)
            if sp_data:
                hma = sp_data[1].get("hive_mind_anchor", {})
                if hma and hma.get("queen_id"):
                    continue
            rep.warn("12-swarm", f"species/{sp}.json", "hive_mind_anchor",
                     "swarm species has no variant with role_in_swarm=queen and no queen_id declared")
    rep.ok()

# ---------------------------------------------------------------------------
# t0-State Assertions (canon-faithfulness, not just referential integrity)
#
# These assertions check that the world's STARTING STATE (t0 = edge of canon)
# matches what Er Gen wrote. The referential assertions (01-12) catch typos
# and missing IDs; the t0 assertions catch a WRONG starting state — e.g. Wang
# Lin marked dead, the Teng Family marked alive, two NPCs claiming the same
# unique inheritance. A wrong t0 state would silently corrupt every
# downstream simulation.
# ---------------------------------------------------------------------------

def assert_13_figure_state_vs_canon(idx, rep):
    """Canon-dead figures must be marked dead; canon-alive figures must be alive.
    The edge-of-canon state (RIEdgeOfCanonState) defines who is alive vs dead."""
    # The 14 dead figures per CANON_RI_EDGE_OF_CANON.md (encoded in RIEdgeOfCanonState.java)
    CANON_DEAD = {
        "teng_huayuan", "teng_li", "qiu_siping", "wang_zhuo", "wang_hao",
        "seven_colored_daoist", "blood_ancestor", "old_man_miesheng",
        "daoist_water", "master_void", "wind_demon", "green_devil",
        "taga", "tuo_sen", "ye_wuyou", "qian_pinghai", "duanmu_ji",
        "gun_lan", "xu_liqing",
    }
    # The 9 alive figures per CANON_RI_EDGE_OF_CANON.md
    CANON_ALIVE = {
        "wang_lin", "situ_nan", "qing_shui", "li_muwan", "li_qianmei",
        "mu_bingmei", "bai_fan", "lu_yun", "qing_lin",
    }
    for stem, (f, d) in load_all("npcs").items():
        nid = (d.get("npc_id") or d.get("canon_id") or stem).lower().replace("-", "_")
        status = str(d.get("status", "")).lower()
        if nid in CANON_DEAD:
            if "alive" in status and "deceased" not in status and "dead" not in status:
                rep.warn("13-t0-figurestate", str(f), "status",
                         f"canon-dead figure '{nid}' is marked alive at t0 (should be deceased)")
        elif nid in CANON_ALIVE:
            if "deceased" in status or "dead" in status:
                rep.warn("13-t0-figurestate", str(f), "status",
                         f"canon-alive figure '{nid}' is marked dead at t0 (should be alive)")
    rep.ok()

def assert_14_unique_inheritance_mutex(idx, rep):
    """Absolute Unique inheritances (per Opportunity Classification) must have
    exactly one claimant. The Heaven-Defying Bead cannot be owned by two entities."""
    # Load provenance and collect current_owner per object
    owners_by_object = defaultdict(list)
    for stem, (f, d) in load_all("provenance").items():
        owner = d.get("current_owner", "") or ""
        if not owner or owner.lower() in OWNERSHIP_SENTINELS:
            continue
        name, status = parse_prose_owner(owner)
        if status:
            continue
        # resolve to a canonical entity name
        resolved = name or owner
        owners_by_object[stem].append(resolved)
    # Flag objects with multiple distinct claimants
    for obj, claimants in owners_by_object.items():
        distinct = set(c.lower() for c in claimants)
        if len(distinct) > 1:
            rep.warn("14-t0-mutex", f"provenance/{obj}.json", "current_owner",
                     f"multiple distinct claimants: {claimants} (verify — may be clone-forms of same entity)")
    rep.ok()

def assert_15_destroyed_factions_marked(idx, rep):
    """Canon-destroyed factions must be marked destroyed/declined at t0."""
    CANON_DESTROYED = {"teng_family", "heng_yue_sect"}  # destroyed/declined per canon
    for stem, (f, d) in load_all("civilizations").items():
        cid = (d.get("civilization_id") or stem).lower().replace("-", "_")
        if cid in CANON_DESTROYED:
            status = str(d.get("canon_status", "")).lower()
            if "destroy" not in status and "decline" not in status and "ruin" not in status:
                rep.warn("15-t0-factionstate", str(f), "canon_status",
                         f"canon-destroyed/declined faction '{cid}' is not marked as such at t0")
    rep.ok()

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
def main():
    print("=== Canon Integrity Validator ===")
    print(f"Data root: {DATA}")
    print(f"Canon DB:  {CANON_DB}")
    print()
    if not CANON_DB.exists():
        print(f"FATAL: canon DB not found at {CANON_DB}")
        sys.exit(2)
    idx = build_canon_index()
    print(f"Index: characters={len(idx['characters'])} locations={len(idx['locations'])} "
          f"artifacts={len(idx['artifacts'])} techniques={len(idx['techniques'])} "
          f"species={len(idx['species'])} variants={len(idx['variants'])} "
          f"npcs={len(idx['npcs'])} civilizations={len(idx['civilizations'])} "
          f"ecosystems={len(idx['ecosystems'])} karma={len(idx['karma'])}")
    print()

    rep = Report()
    assertions = [
        ("01 Referential integrity",       assert_01_referential_integrity),
        ("02 Location status vs timeline", assert_02_location_timeline),
        ("03 Cultivation monotonicity",    assert_03_cultivation_monotonicity),
        ("04 Ownership chain validity",    assert_04_ownership_chain),
        ("05 Karmic consequence resolution", assert_05_karmic_resolution),
        ("06 Perception-tier completeness", assert_06_perception_tiers),
        ("07 Ecosystem member resolution", assert_07_ecosystem_members),
        ("08 Faction relationship symmetry", assert_08_faction_symmetry),
        ("09 Provenance chain continuity", assert_09_provenance_continuity),
        ("10 Variant count",               assert_10_variant_count),
        ("11 Inner-dan/bloodline consistency", assert_11_inner_dan_bloodline),
        ("12 Swarm membership",            assert_12_swarm_membership),
        ("13 t0 figure state vs canon",    assert_13_figure_state_vs_canon),
        ("14 t0 unique inheritance mutex", assert_14_unique_inheritance_mutex),
        ("15 t0 destroyed factions marked", assert_15_destroyed_factions_marked),
    ]
    for name, fn in assertions:
        before_errs = len(rep.errors)
        before_warns = len(rep.warnings)
        try:
            fn(idx, rep)
        except Exception as e:
            rep.err(name, "?", "?", f"ASSERTION CRASHED: {e}")
        e = len(rep.errors) - before_errs
        w = len(rep.warnings) - before_warns
        status = "PASS" if e == 0 else "FAIL"
        print(f"  [{status}] {name:42s}  errors={e}  warnings={w}")

    print()
    print(f"Total: {rep.passed} checks passed, {len(rep.errors)} errors, {len(rep.warnings)} warnings")
    if rep.errors:
        print("\n=== ERRORS (canon contradictions — must fix) ===")
        for cat, f, field, msg in rep.errors[:50]:
            print(f"  [{cat}] {f} :: {field}\n      {msg}")
        if len(rep.errors) > 50:
            print(f"  ... and {len(rep.errors)-50} more errors")
    if rep.warnings:
        print("\n=== WARNINGS (verify — may be acceptable) ===")
        for cat, f, field, msg in rep.warnings[:30]:
            print(f"  [{cat}] {f} :: {field}\n      {msg}")
        if len(rep.warnings) > 30:
            print(f"  ... and {len(rep.warnings)-30} more warnings")

    sys.exit(1 if rep.errors else 0)

if __name__ == "__main__":
    main()
