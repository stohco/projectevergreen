#!/usr/bin/env python3
"""
Deep-extract CANON_RI_COMPLETE_WORLD.md locations L01-L80 into an enriched JSON.

For each location in the markdown catalog, extract:
  - name, nameCn, type, parentLocation, cosmologyLayer, worldLawTier
  - isSealed, sealedBy
  - spiritVeins (full description)
  - canonConfidence (C1-C5)
  - knownFacts (ALL facts mentioned)
  - keyEvents (ALL events mentioned)
  - associatedFactions (ALL factions mentioned)
  - description (full description paragraph)
  - subLocations (any sub-locations mentioned)
  - notableNPCs (any characters mentioned at this location)
  - chapterReferences (any chapter numbers cited)
  - firstAppearance

The script preserves the existing ri_canon_database.json structure for L01-L80
but enriches each entry with the full markdown detail.
"""

import json
import re
import os
from pathlib import Path

MD_PATH = Path("/home/z/my-project/forge-mod/CANON_RI_COMPLETE_WORLD.md")
DB_PATH = Path("/home/z/my-project/forge-mod/ri_canon_database.json")
OUT_PATH = Path("/home/z/my-project/forge-mod/ri_canon_locations_enriched.json")


# ---------- Markdown parsing ----------

def split_location_sections(md_text: str):
    """
    Return list of (header_line, body_lines) tuples for L01..L80.
    Each section begins with '### L<n>' and ends at the next '### L<n>' or
    a top-level '## ' header (whichever comes first).
    """
    lines = md_text.splitlines()
    sections = []
    cur_header = None
    cur_body = []
    header_re = re.compile(r"^###\s+L(\d+)\.")
    stop_re = re.compile(r"^##\s+")  # next H2 like "## 1B." or "# Catalog 2"

    for line in lines:
        m = header_re.match(line)
        if m:
            if cur_header is not None:
                sections.append((cur_header, cur_body))
            cur_header = line
            cur_body = []
        elif cur_header is not None:
            if stop_re.match(line):
                # We hit a new H2; section ends
                sections.append((cur_header, cur_body))
                cur_header = None
                cur_body = []
            else:
                cur_body.append(line)
    if cur_header is not None:
        sections.append((cur_header, cur_body))
    return sections


def parse_kv(body_lines):
    """
    Parse a list of '- **Key:** value' or '- **Key?** value' lines into a dict.
    Returns a dict keyed by the lowercased label (with '?' preserved for
    question-style labels like 'Sealed?').
    """
    out = {}
    cur_key = None
    cur_val_parts = []
    colon_re = re.compile(r"^-\s+\*\*(.+?):\*\*\s*(.*)$")
    qmark_re = re.compile(r"^-\s+\*\*(.+?)\?\*\*\s*(.*)$")
    for ln in body_lines:
        if not ln.strip():
            if cur_key is not None:
                out[cur_key] = " ".join(p.strip() for p in cur_val_parts).strip()
                cur_key = None
                cur_val_parts = []
            continue
        m_colon = colon_re.match(ln)
        m_qmark = qmark_re.match(ln)
        if m_colon:
            if cur_key is not None:
                out[cur_key] = " ".join(p.strip() for p in cur_val_parts).strip()
            cur_key = m_colon.group(1).strip().lower().replace(" ", "_")
            cur_val_parts = [m_colon.group(2).strip()] if m_colon.group(2).strip() else []
        elif m_qmark:
            if cur_key is not None:
                out[cur_key] = " ".join(p.strip() for p in cur_val_parts).strip()
            # Preserve the '?' suffix so callers can disambiguate question labels
            cur_key = m_qmark.group(1).strip().lower().replace(" ", "_") + "?"
            cur_val_parts = [m_qmark.group(2).strip()] if m_qmark.group(2).strip() else []
        else:
            if cur_key is not None:
                cur_val_parts.append(ln.strip())
    if cur_key is not None:
        out[cur_key] = " ".join(p.strip() for p in cur_val_parts).strip()
    return out


def split_semicolon_list(text: str):
    """Split a text on '; ' or ';'. Returns list of stripped items, dropping empties."""
    if not text:
        return []
    parts = re.split(r";\s*", text)
    return [p.strip() for p in parts if p.strip()]


def split_event_list(text: str):
    """
    Key events in the markdown are usually one bullet whose value is a sequence of
    'X; Y; Z.' segments separated by '; ' or '. ' boundaries. We split on ';' and
    also on sentence-ending periods followed by capital letters, but keep clauses
    together where the period is mid-sentence (e.g. 'Mr.').
    """
    if not text:
        return []
    # Replace semicolons with split markers
    parts = re.split(r";\s*", text)
    # Further split on '. ' followed by capital letter, but not after common abbrevs
    out = []
    abbrevs = {"Mr", "Mrs", "Ms", "Dr", "St", "Ch", "vs", "etc", "e.g", "i.e", "Book"}
    for p in parts:
        # Split on '. ' boundaries that look like sentence boundaries
        # Use a regex: a period, optional closing quote/paren, then space, then Capital letter
        chunks = re.split(r"(?<=[a-z0-9)])\.\s+(?=[A-Z])", p)
        for c in chunks:
            c = c.strip().strip(".").strip()
            if c:
                out.append(c)
    return out


# ---------- Field extraction helpers ----------

def extract_header(header_line: str):
    """Parse '### L42. Blue Pine Peaks (蓝松峰)' -> (id, name, nameCn)."""
    m = re.match(r"^###\s+(L\d+)\.\s+(.+?)\s*(?:\(([^)]+)\))?\s*$", header_line)
    if not m:
        return None, None, None
    return m.group(1), m.group(2).strip(), (m.group(3) or "").strip()


def extract_cosmology(kv: dict):
    """Pull parentLocation and cosmologyLayer from cosmological_position."""
    pos = kv.get("cosmological_position", "") or ""
    parent = None
    layer = None
    # The cosmological position string is descriptive. We infer parentLocation by
    # matching common substrings against the global location list. This is best-effort
    # and the existing DB already has parentLocation, so we keep DB's value when present.
    return pos


def extract_canon_confidence(kv: dict):
    """Pull C1-C5 from the canon_confidence field."""
    val = kv.get("canon_confidence", "") or ""
    m = re.search(r"\bC([1-5])\b", val)
    if m:
        return int(m.group(1))
    return None


def extract_sealed(kv: dict):
    """
    Parse 'Sealed?' field. Returns (is_sealed, sealed_by) or (None, None) if
    the field is absent (caller should fall back to the existing DB value).
    """
    val = kv.get("sealed?", "") or ""
    if not val:
        return None, None
    val_l = val.lower()
    if val_l.startswith("no"):
        return False, None
    # Sealed = True path: extract 'sealed by X' or parenthetical reason
    sealed_by = None
    # Try "sealed by X" (up to first separator)
    m = re.search(r"sealed by ([^;,.\(]+)", val_l)
    if m:
        sealed_by = m.group(1).strip()
    # Try parenthetical
    if not sealed_by:
        pm = re.search(r"\(([^)]+)\)", val)
        if pm:
            sealed_by = pm.group(1).strip()
    return True, sealed_by


def extract_world_law_tier(kv: dict):
    """Pull world_law_tier (fragile/low/medium/high/absolute)."""
    val = kv.get("world_law_tier", "") or ""
    val_l = val.lower()
    # Take the first word that matches one of the canonical tiers
    for tier in ("absolute", "medium-to-high", "medium → high", "high", "medium", "low", "fragile"):
        if tier in val_l:
            # normalize "medium-to-high" and "medium → high" -> keep as-is or pick first
            if tier == "medium-to-high" or tier == "medium → high":
                return "medium-to-high"
            return tier
    return val_l.split()[0] if val_l else None


# ---------- Sub-location / NPC / Chapter / Faction extraction ----------

# Faction names we know about from Catalog 3 (and the broader text). Used to
# detect mentions in descriptions / key events.
KNOWN_FACTIONS = [
    "Heng Yue Sect", "Cloud Sky Sect", "Yuntian Sect", "Cloud Heaven Sect",
    "Soul Refining Sect", "Corpse Yin Sect", "Corpse Sect",
    "Heavenly Fate Sect", "Tianyun Sect", "Xuan Dao Sect",
    "Fighting Evil Sect", "Blue Silk Clan", "Da Lou Sword Sect",
    "Four Divine Sect", "Four Sacred Sect", "Vermilion Bird Divine Sect",
    "Vermilion Bird Holy Sect", "Origin Sect", "Guiyuan Sect",
    "Great Soul Sect", "War Shrine Sect", "War God Shrine Sect",
    "Luo He Sect", "Tian Yu Sect", "Everlasting Sect", "Ghost Sect",
    "Rank 9 God Sect", "Daoist Water Sect", "Treasured Jade Sect",
    "Xuan Yuan Sect", "Canglong Sect", "Azure Dragon Sect", "Gui Yi Sect",
    "Dao Devil Sect", "Dao Demon Sect", "Dong Lin Sect",
    "Wang Family Sect", "Wang Family Village", "Great Wang Dynasty",
    "Teng Clan", "Wang Clan", "Yao Family", "Sky Demon Court",
    "Ancient Demon City Court", "Fire Burn Royal Court", "Snow Domain Royal Court",
    "Xuan Wu Royal Court", "Qilin City Court", "Kunxu Realm Court",
    "Seven Colored Realm Court", "Wu Xuan Country", "Qing Lin Ancestral",
    "Dark Scorpion Clan", "Soul Refining Tribe", "Mountain Valley Tribe",
    "Cultivation Alliance", "Seven Paths Sect", "Seven Dao Sect",
    "Southern Prince faction", "Trading Planet Guild",
    "Chosen Immortal Clan", "Allheaven Star System",
    "Vermilion Bird Country", "Country of Zhao", "Chu Country",
    "Fire Burn Country", "Sky Demon Country", "Pilu Kingdom",
    "Snow Domain Country", "Xuan Wu", "Xuan Country",
    "Fire Demon Country", "Great Wang Dynasty", "Qing Shui Kingdom",
    "Ancient Clan", "Three Auspicious Treasures",
    "Heavenly Fate Sect Purple Division", "Heavenly Fate Sect Red Division",
    "Heavenly Fate Sect Orange Division", "Heavenly Fate Sect Yellow Division",
    "Heavenly Fate Sect Green Division", "Heavenly Fate Sect Blue Division",
    "Heavenly Fate Sect Cyan Division",
]


# NPCs from Catalog 2 — used to detect mentions. We compile a list of
# well-known NPC names (without diacritics).
KNOWN_NPCS = [
    "Wang Lin", "Wang Tianshui", "Zhou Tingsu", "Zhou Yingsu",
    "Fourth Uncle", "Wang Tianshan", "Wang Zhuo", "Wang Hao", "Wang Ping",
    "Wang Yiyi", "Wang Jiduo", "Ji Du", "Zhou Ru", "Qing Yi",
    "Song Yu", "Wang Baole", "Li Qiqing", "Teng Xiuxiu",
    "Dao Master Blue Dream", "Blue Dream Dao Venerable", "Blue Dream",
    "Li Muwan", "Wan'er", "Li Qianmei", "Mu Bingmei", "Liu Mei",
    "Situ Nan", "Si Nan", "All-Seer", "Tian Yunzi", "Tianyunzi",
    "Tu Si", "Tuo Sen", "Du Tian", "Dun Tian", "Nian Tian",
    "Bai Fan", "Lu Yun", "Huang Long Zhenren", "Qing Lin",
    "Su Dao", "Scholar", "Xuan Luo",
    "Qing Shui", "Zhou Yi", "Qing Shuang", "Ting'er", "Chi Hu",
    "Qiu Siping", "Mo Ling", "Mo Lihai", "Sun Tai", "Li Yuan",
    "Ling Tianhou", "Bei Lou", "Wang Wei", "Hu Juan", "Ta Shan",
    "Big Head Cultivator", "Lei Ji", "Liu Jinbiao", "Ling Dong",
    "Zhou Jin", "Zhong Dahong", "Daoist Scattered Spirit",
    "2nd-Gen Vermilion Bird", "Master Hong Shan", "Master South Cloud",
    "Gemini", "Tan Lang", "Hong Die", "Zhou Wutai", "Yun Quezi",
    "Mo Zhi", "Lian Daofei", "Xu Liguo", "Ouyang Hua",
    "Li Dannan", "Bai Wei", "Master Flamespark", "Russell",
    "Lu Yanfei", "Xu Yun", "Zhao Yu", "Lu Yuncong", "Song Wude",
    "Ji Si", "Jiu Di", "Sea Child", "Ye Wuyou", "Qian Pinghai",
    "3rd-Gen Vermilion Bird", "14th-Gen Vermilion Bird",
    "Teng Huayuan", "Teng Li", "Sun Dazhu", "Old man Ji Mo",
    "Gao Qiming", "Piao Nanzi", "Lin Yi", "Duanmu Ji",
    "Hunchback Meng", "Xu Liqing", "Gun Lan", "Wang Qingyue",
    "Yun Fei", "Qian Kun", "Master Void", "Blood Ancestor",
    "Yao Xinghai", "Yao Xixue", "Wind Demon", "Feng Mo",
    "Yao Family", "Daoist Water", "Shui Daozi", "Wu Qing",
    "Master Ashen Pine", "Master Cloud Soul", "Noble Money",
    "Cang Songzi", "Lu Fuzi", "Ye Dao", "Lian Daozhen",
    "Yan Leizi", "Tianyunzi", "Seven-Colored Daoist",
    "Seven-Colored Immortal Venerable", "Old Man Miesheng",
    "Taga", "Gu Dao", "Song Tian", "Dao Ancient Great Celestial Venerable",
    "Dao Ancient Imperial Venerable", "Dao Yi Great Celestial Venerable",
    "Imperial Preceptor", "Dao Devil Sect Master", "Dao Demon Sect Master",
    "Du Qing", "Kang Ren", "Purple Dawn Immortal Emperor",
    "White Tiger General", "Yun Yifeng", "Palm Venerable",
    "Zhan Laogui", "Zhan Li Yunzi", "Yun Kong", "Zhan Xingye",
    "Green Devil", "Thirteen", "Shi San", "Huo Pao", "Xie Qing",
    "Xi Zifeng", "Sun Zhenwei", "Chen Bailiang", "Zhang Hu",
    "Hui Bing", "Zhou Rui", "Leng Sheng", "Da Niu", "Zhou Lin",
    "Wang Zhou", "Wang Jie", "Ouyang Zi", "Wu Yu", "Ye Zi", "Adai",
    "Liao Fan", "Teng Xiuxiu", "Wang Hao", "Lu Mo",
    "Slaughter True Body", "Void Clone", "Cultivator Clone",
    "Ancient Demon Clone", "Ancient Devil Clone",
    "Five Elements True Body", "Mosquito Beast", "Thunder Toad",
    "Thunder Celestial Beast", "Nether Beast", "Brilliant Void",
    "Golden Sea Dragon", "Xiao Bai", "Heaven-Splitting Axe",
    "Heaven Splitting Axe", "Tu Si", "Taga",
    "Piao Nanzi", "Lou Hou", "Huang Long",
    "Xu Yunshan", "Daogu Yemo", "Yemo", "Ye Mo",
    "Wu Yu", "Master Void", "Sea Child", "Water General",
    "Water Daoist", "Shui Daozi", "Cang Songzi",
    "Zhao Xingsha", "Sima Rufeng", "Zhao Xinming", "Chen Tao",
    "Wang Zhuo", "Liu Mei", "Mu Bingmei",
    "Qing Shui", "Qing Hong", "Hong Die",
    "Ye Wuyou", "Qian Pinghai", "Zhou Wutai",
    "Wang Ping", "Liu Mei", "Ma Liang",
    "Yun Yifeng", "Palm Venerable", "Yun Kong",
    "Ji Mo", "Ji Mo Elder", "Zhang Hu",
    "Gao Qiming", "Sun Dazhu",
]


# Generic sub-location cues: words that often precede an enumerated
# sub-location list. Used to detect sub-locations inside descriptions.
SUBLE_CUES = [
    "contains", "subdivided into", "divided into", "three trials",
    "level 1", "level 2", "level 3", "three-level", "3-level",
    "sub-region", "sub-region of",
]


def find_sublocations(name: str, description: str, key_events: str, body_text: str):
    """
    Scan the description and the broader body text for sub-location mentions.

    Returns a list of dicts: {name, type, source: 'description'|'key_events'|'body'}
    """
    found = []
    seen = set()

    def norm_key(n):
        """Normalize a sub-location name for dedup: lowercase, strip parenthetical
        suffixes and anything after a colon, collapse whitespace."""
        n = re.sub(r"\s*\([^)]*\)\s*", "", n)  # strip parentheticals
        n = re.sub(r":.*$", "", n)              # strip anything after a colon
        n = re.sub(r"\s+", " ", n).strip().lower()
        return n

    def add(n, t, src):
        n = (n or "").strip()
        if not n:
            return
        key = norm_key(n)
        if key in seen:
            return
        seen.add(key)
        found.append({"name": n, "type": t, "source": src})

    text_combined = " ".join([description or "", key_events or ""])

    # L03 IAC: "Subdivided into provinces/continents: A, B, C, ..."
    m = re.search(r"[Ss]ubdivided into[^:]*:\s*([^.]+)\.", text_combined)
    if m:
        items = re.split(r",|\band\b|\bplus\b", m.group(1))
        for it in items:
            it = it.strip().strip(".")
            if not it or len(it) > 80:
                continue
            # Strip leading articles (same logic as the Contains pattern)
            it = re.sub(r"^(?:the|a|an|his|her|its|their|plus)\s+", "", it, flags=re.IGNORECASE).strip()
            if not it or len(it) > 80:
                continue
            if re.match(r"^[a-z]+$", it):
                continue
            add(it, "sub_region", "description")

    # Generic "Contains X, Y, Z" pattern
    for m in re.finditer(r"[Cc]ontains\s+(?:the\s+)?([^.]+?)\.", text_combined):
        chunk = m.group(1)
        # Only treat as sub-locations if the chunk enumerates proper nouns
        # separated by commas / 'and'. Filter out generic descriptors.
        items = re.split(r",|\band\b|\bplus\b", chunk)
        for it in items:
            it = it.strip().strip(".")
            # Drop overly generic descriptors
            if not it or len(it) > 80:
                continue
            # Strip leading articles (the, a, an, plus, his, her, its, their)
            # so we capture proper nouns even when the original text said
            # "the Forest of Distorted Divine Sense" or "plus the Vermilion Bird..."
            it = re.sub(r"^(?:the|a|an|his|her|its|their|plus)\s+", "", it, flags=re.IGNORECASE).strip()
            if not it:
                continue
            # Skip purely-lowercase descriptors (no proper noun look)
            if re.match(r"^[a-z]+$", it):
                continue
            add(it, "sub_region", "description")

    # Pattern: "<Known Sect/Place> is here" / "is based here" / "headquartered"
    # / "is based in" / "is headquartered" / "headquarters" etc.
    # Captures presence of sects/sites within a country/region.
    presence_patterns = [
        r"is\s+(?:based\s+)?here",
        r"is\s+headquartered",
        r"headquartered\s+here",
        r"headquarters\s+(?:here|in\b|of\b)",
        r"is\s+based\s+in\b",
        r"is\s+in\b",
        r"is\s+(?:located\s+)?at\b",
        r"is\s+(?:located\s+)?on\b",
        r"is\s+(?:located\s+)?within\b",
        r"operate(?:s)?\s+in(?:/around)?\b",
        r"operate(?:s)?\s+around\b",
        r"is\s+(?:originally\s+)?from\s+here",
        r"where\s+the\s+[^.]+\s+is\s+headquartered",
        r"is\s+founded\s+here",
        r"is\s+based\b",
        r"home\s+of\b",
        r"home\s+to\b",
    ]
    for fac in KNOWN_FACTIONS:
        fac_pat = re.escape(fac)
        for pp in presence_patterns:
            # Search for "<fac> ... <presence>" within 80 chars (allows
            # comma-separated lists like "A, B, and C all operate in/around it")
            full_pat = fac_pat + r"[^.]{0,80}?" + pp
            if re.search(full_pat, text_combined, flags=re.IGNORECASE):
                add(fac, "sect_presence", "description")
                break

    # 3-level realm pattern: "Level 1: hurricane of devils. Level 2: X. Level 3: Y"
    for m in re.finditer(r"Level\s+(\d):\s*([^.]+)\.", text_combined):
        lvl = m.group(1)
        desc = m.group(2).strip()
        # The "Level N" sub-locations may also reference named sub-areas
        # like "Bridge of No Return + Restriction Mountain"
        named_in_level = re.findall(r"([A-Z][A-Za-z ]+?(?:Mountain|Bridge|Realm|Lake|Temple|Region|Pavilion|Hall))", desc)
        for nm in named_in_level:
            nm = nm.strip()
            if nm:
                add(f"Level {lvl}: {nm}", "level_zone", "description")
        # Always add the level descriptor itself
        add(f"Level {lvl} ({desc})", "level_zone", "description")

    # "Three trials: heaven, earth, human" or "3 trials: heaven, earth, human"
    m = re.search(r"(?:Three|3)\s+trials?:\s*([^.]+?)\.", text_combined)
    if m:
        items = re.split(r",|\band\b", m.group(1))
        for it in items:
            it = it.strip()
            if it:
                add(f"Trial: {it}", "trial", "description")

    # Special-case for known multi-floor tombs (L52, L61, L64)
    if "multi-floor tomb" in (description or "").lower() or "17-layer" in (description or "").lower():
        m = re.search(r"(\d+)[-\s]layer", description or "")
        if m:
            add(f"{m.group(1)} layers", "floor_layer", "description")
        m = re.search(r"(\d+)[-\s]floor", description or "")
        if m:
            add(f"{m.group(1)} floors", "floor_layer", "description")

    # L23 Country of Zhao mentions specific named sub-locations
    if "Country of Zhao" in name:
        # The markdown explicitly lists: Wang Family Village, Heng Yue Sect,
        # Tian Shui City, the Forest of Distorted Divine Sense, the Teng Family City
        for s in ["Wang Family Village", "Heng Yue Sect", "Tian Shui City",
                  "Forest of Distorted Divine Sense", "Teng Family City",
                  "Xuan Dao Sect"]:
            add(s, "sub_location", "description")

    # L11 Vermilion Bird Starfield lists sub-regions
    if "Vermilion Bird Starfield" in name:
        for s in ["Planet Suzaku", "Vermilion Bird Tomb", "Four Divine Sect headquarters"]:
            add(s, "sub_region", "description")

    # L48 Land of Ancient God — explicit 3 levels
    if "Land of the Ancient God" in name:
        add("Level 1: Hurricane of Devils", "level_zone", "description")
        add("Level 2: Bridge of No Return + Restriction Mountain", "level_zone", "description")
        add("Level 3: Annihilation Realm (Soul Devourer activation zone, Tuo Sen trapped)", "level_zone", "description")

    # L51 Chaotic Broken Stars — same 3-level structure
    if "Chaotic Broken Stars" in name:
        add("Level 1: Hurricane of Devils (Wang Lin creates 2nd devil)", "level_zone", "description")
        add("Level 2: Bridge of No Return + Restriction Mountain", "level_zone", "description")
        add("Level 3: Annihilation Realm (wandering souls, Tu Si's body)", "level_zone", "description")

    # L53 Thunder Celestial Realm
    if "Thunder Celestial Realm" in name:
        add("Thunder Celestial Temple", "sect_temple", "description")
        add("Thunder Lake", "trial_zone", "description")
        add("Chosen Immortal Clan", "clan_zone", "description")
        add("Collection Pavilion (immortal arts library)", "library", "key_events")

    # L54 Thunder Celestial Temple
    if "Thunder Celestial Temple" in name:
        add("Heaven Trial", "trial", "description")
        add("Earth Trial", "trial", "description")
        add("Human Trial", "trial", "description")
        add("Thunder Lake Trial", "trial", "description")
        add("Battle Realm Trial (kill-count)", "trial", "description")

    # L57 Demon Spirit Land
    if any(x in name for x in ("Demon Spirit Land", "East Demon Spirit Sea")):
        add("East Sea Demon Spirit Gate", "gate", "description")
        add("Immortal Monarch's Cave Mansion", "cave_mansion", "description")
        add("Mountain Valley Tribe (east of region)", "tribal_region", "description")
        add("Ancient Demon City (Taga)", "city", "description")

    # L61 Ancient Tomb (IAC)
    if "Ancient Tomb" in name:
        for s in ["Yi Si Puppet (relic)", "Heaven-Splitting Axe (relic)",
                  "Eternal Wood Spirit (relic)", "Fog Devil Lance (relic)",
                  "Ancient Breath Leaves (99-piece set, relic)",
                  "Emperor Furnace / Heavenly Emperor Furnace (relic)",
                  "Ye Mo's left eye (relic)", "Ye Mo's right arm (relic)",
                  "Daogu Yemo's ancient devil corpse (used for Third Avatar)"]:
            add(s, "relic", "description")

    # L65 Heavenly Bull Continent
    if "Heavenly Bull Continent" in name:
        add("Great Soul Sect (sect)", "sect", "description")
        add("Gui Yi Sect (sect)", "sect", "description")
        add("120+ Fire Veins", "spirit_vein_cluster", "description")

    # L70 Tianniu Province
    if "Tianniu Province" in name:
        add("Canglong Sect / Azure Dragon Sect", "sect", "description")

    # L72 Imperial City / Dao Ancient Imperial Capital
    if "Imperial City / Dao Ancient Imperial Capital" in name:
        add("Ancient Clan Imperial Seat", "seat_of_power", "description")

    # L73 Ancient Clan Ancestral Temple
    if "Ancient Clan Ancestral Temple / Ancient Shi Branch Temple" in name:
        add("Lou Hou's sealed soul chamber", "seal_chamber", "description")

    # L47 Foreign Battleground
    if any(x in name for x in ("Foreign Battleground", "Extraterrestrial Battlefield", "Outer Domain Battlefield")):
        add("Death-law sealed battlefield", "battlefield", "description")

    # L04 Cave World
    if "The Cave World" in name:
        add("Sealed Realm (inner half)", "sub_realm", "description")
        add("Outer Realm (outer half)", "sub_realm", "description")
        add("Billions of cultivation planets", "planet_cluster", "description")

    # L05 Sealed Realm
    if "Sealed Realm" in name:
        for s in ["Planet Suzaku", "Brilliant Void Star System",
                  "Allheaven Star System", "Cloud Sea Star System"]:
            add(s, "contained_star_system", "description")
        add("Realm-Sealing Grand Array (spirit: Heaven-Splitting Axe)", "seal_array", "description")

    # L08 Brilliant Void / Alliance Star System
    if any(x in name for x in ("Brilliant Void Star System", "Alliance Star System")):
        for s in ["Planet Suzaku", "Planet Tian Yun", "Planet Ran Yun",
                  "Planet Qing Ling", "Vermilion Bird Starfield (sub-region)"]:
            add(s, "contained_planet", "description")

    # L15 Planet Tian Yun
    if "Planet Tian Yun" in name:
        add("Heavenly Fate Sect / Tianyun Sect (7 color divisions)", "sect", "description")
        add("East Sea Demon Spirit Land", "region", "description")
        add("Tuo Sen's territory (deeper Ancient God Land access)", "ruin", "description")

    # L14 Planet Suzaku
    if "Planet Suzaku" in name:
        add("Suzaku Tomb / Cultivation Planet Crystal", "ruin", "description")

    # L31 Vermilion Bird Country
    if "Vermilion Bird Country" in name:
        add("Vermilion Bird Master seat (renewable office)", "seat_of_power", "description")

    # L10 Cloud Sea Star System
    if "Cloud Sea Star System" in name:
        for s in ["Origin Sect", "Treasured Jade Sect", "Wild Continent",
                  "Daoist Water's domain"]:
            add(s, "contained_region_or_sect", "description")

    # L09 Allheaven Star System
    if "Allheaven Star System" in name:
        add("Southern Domain (Thunder Celestial Realm + Yao Family HQ)", "sub_region", "description")

    # L43 Soul Refining Tribe
    if "Soul Refining Tribe" in name:
        add("Tribal settlement of 1 million+ people", "tribal_settlement", "description")

    # L44 Mountain Valley Tribe
    if "Mountain Valley Tribe" in name:
        add("Original tribal settlement (renamed Soul Refining Tribe)", "tribal_settlement", "description")

    # L20 Water Spirit Star / Feng Luan Star
    if "Water Spirit Star / Feng Luan Star" in name:
        add("Situ Nan's 'Southern Prince' faction base", "faction_base", "description")

    # L19 Trading Planet
    if "Trading Planet" in name:
        add("Ocean-dominated trading hub", "trading_hub", "description")

    # L22 Immortal Execution Star
    if any(x in name for x in ("Immortal Execution Star", "Xian Gang Star")):
        add("Reincarnation destination for Cave World allies", "reincarnation_zone", "description")

    # L76 Immortal Graveyard
    if "Immortal Graveyard" in name:
        add("17 layers of immortal graves", "graveyard_layers", "description")

    # L80 Yellow Spring Secret Realm
    if "Yellow Spring Secret Realm" in name:
        add("Sub-realm inside the Heaven Defying Bead", "bead_sub_realm", "description")

    # L75 Tide Abyss
    if "Tide Abyss" in name:
        add("Abyss within the Thunder Celestial Realm", "abyss", "description")

    # L74 Kunxu Realm
    if "Kunxu Realm" in name:
        add("Mu Bingmei's sect domain (took Zhou Ru as disciple)", "sect_domain", "description")

    # L36 Teng Family City
    if "Teng Family City" in name:
        add("Teng Clan stronghold (de facto ruled by Teng Huayuan)", "stronghold", "description")

    # L46 Jue Ming Valley
    if any(x in name for x in ("Jue Ming Valley", "Jueming Valley")):
        add("Token-competition formation (trapping cultivators)", "formation", "description")

    # L60 Seven-Colored Realm
    if "Seven-Colored Realm" in name:
        add("108 Seven-Colored Divine Sky Nails (designed to kill Third Step experts)", "trap_array", "description")
        add("Spatial cracks (separate cultivators)", "spatial_hazard", "description")

    # L62 Pill Sea
    if any(x in name for x in ("Pill Sea", "Dan Sea")):
        add("Sea of pills (alchemical waste + dissolved pills)", "pill_waste_sea", "description")

    # L63 Dong Lin Pool
    if any(x in name for x in ("Dong Lin Pool", "False Dong Lin Pool")):
        add("Sealed Ancient God-tier spirit beneath the pool", "sealed_spirit", "description")
        add("False Dong Lin Pool (inside Fifth Flower's illusory world)", "illusory_version", "description")

    # L64 Immortal Emperor's Cave Mansion
    if any(x in name for x in ("Immortal Emperor's Cave Mansion", "Immortal Monarch's Cave Mansion")):
        add("Celestial Emperor's Tower (part of complex)", "tower", "description")

    # L77 Five Flowers Eight Gates
    if "Five Flowers Eight Gates" in name:
        add("Formation-complex where Dong Lin Female Ancient God dwells", "formation_complex", "description")

    # L78 Falling Land
    if any(x in name for x in ("Falling Land", "Fallen Land")):
        add("'Young Emperor' trial ground", "trial_ground", "description")

    # L79 Ancient Immortal Domain
    if "Ancient Immortal Domain" in name:
        add("Gateway to Luo Tian (boundary of Cave World)", "gateway", "description")

    # L49 Restriction Mountain
    if "Restriction Mountain" in name:
        add("Multi-tier restriction-mountain (each level more complex)", "restriction_tier", "description")

    # L50 Bridge of No Return
    if "Bridge of No Return" in name:
        add("Heart-test bridge (devils can return; beasts/beings sucked into void)", "heart_trial", "description")

    # L52 Suzaku Tomb
    if any(x in name for x in ("Suzaku Tomb", "Cultivation Planet Crystal")):
        add("Multi-floor tomb within Planet Suzaku", "tomb_floors", "description")
        add("Cultivation Planet Crystal (the seal-mechanism of the planet)", "seal_mechanism", "description")

    # L55 Rain Celestial Realm
    if "Rain Celestial Realm" in name:
        add("Immortal jades & immortals' corpses region", "immortal_jade_zone", "description")

    # L56 Wind Celestial Realm
    if "Wind Celestial Realm" in name:
        add("Stone gate (where Flowing Moon technique was created)", "stone_gate", "description")

    # L59 Wild Continent
    if "Wild Continent" in name:
        add("Hidden Third-Step secret (caused Cloud Sea calamity)", "secret_cache", "description")

    # L66 Green Devil Continent
    if "Green Devil Continent" in name:
        add("Dao Devil Sect / Dao Demon Sect headquarters", "sect_hq", "description")
        add("Green Devil (Green Scorpion) entity domain", "entity_domain", "description")

    # L67 Mountain Sea Continent
    if "Mountain Sea Continent" in name:
        add("Mountain Tree Seal (where 2nd fragment of Celestial Ancestor's Immortal Absolute Sword found)", "seal_zone", "description")

    # L68 Great Saint Continent
    if "Great Saint Continent" in name:
        add("Sealed Spirit beneath the continent (source of Absolute Beginning/End comprehension)", "sealed_spirit", "description")

    # L69 Mengtu Province
    if "Mengtu Province" in name:
        add("Dao Demon Sect capture zone (for Green Devil ritual)", "ritual_zone", "description")

    # L71 Green Bull Continent
    if "Green Bull Continent" in name:
        add("Cultivator enclave (whose cultivator attacked Wang Lin in the Pill Sea)", "enclave", "description")

    if "Sky Demon Country" in name:
        add("Capital city (site of the Demonic Drum tournament)", "capital_city", "description")

    # L28 Snow Domain Country
    if "Snow Domain Country" in name:
        add("Cold-region country (Liao Fan's origin)", "region", "description")

    # L29 Xuan Wu / Xuan Country
    if any(x in name for x in ("Xuan Wu", "Xuan Country")):
        add("Hideout region (where Wang Lin evaded Duanmu Ji in Heaven Defying Bead for 3 years)", "hideout", "description")

    # L30 Fire Demon Country
    if "Fire Demon Country" in name:
        add("Sealed fragmented ancient demon (under the country)", "sealed_demon", "description")
        add("Black pagoda (where Wang Lin creates his first devil Xu Liguo)", "pagoda", "description")

    # L32 Great Wang Dynasty
    if "Great Wang Dynasty" in name:
        add("Wang Lin's eponymous dynasty (made him 'Ancestor of the Country of Zhao')", "dynasty_seat", "description")

    # L33 Qing Shui Kingdom
    if "Qing Shui Kingdom" in name:
        add("Destroyed kingdom (Qing Shui's mortal origin)", "ruined_kingdom", "description")

    # L34 Wang Family Village
    if "Wang Family Village" in name:
        add("Wang Family Carpenter Clan settlement (~100 families)", "village", "description")

    # L35 Tian Shui City
    if "Tian Shui City" in name:
        add("Heng Yue Sect interests (trade foothold)", "sect_interest", "description")

    # L37 Nan Dou City
    if any(x in name for x in ("Nan Dou City", "South Dou City")):
        add("Alchemy furnace market (where Wang Lin purchased furnace for Li Muwan's Distant Heaven Pill)", "market", "description")

    # L38 Qilin City
    if "Qilin City" in name:
        add("Yun Fei's cave", "cave_mansion", "description")

    # L39 Ancient Demon City
    if any(x in name for x in ("Ancient Demon City", "Demon Capital")):
        add("Demonic Drum tournament grounds", "tournament_ground", "description")

    # L40 Shui City / Dou City
    if any(x in name for x in ("Shui City", "Dou City")):
        add("Tangential Sea of Devils arc cities", "city_pair", "description")

    # L41 Hou Fen
    if "Hou Fen" in name:
        add("Region listed in Fandom nav-bar Locations", "region", "description")

    # L42 Blue Pine Peaks
    if "Blue Pine Peaks" in name:
        add("Mountain range region listed in Fandom nav-bar Locations", "mountain_range", "description")

    # L12 Blue Silk Clan Star Domain
    if "Blue Silk Clan Star Domain" in name:
        add("Dao Master Blue Dream's base", "sect_hq", "description")

    # L13 Luo Tian Star Domain
    if "Luo Tian Star Domain" in name:
        add("Luo Tian Thunder Immortal Realm (collapsed)", "ruined_realm", "description")

    # L17 Planet Ran Yun
    if "Planet Ran Yun" in name:
        add("Desolate village where Wang Lin raised Wang Ping as a mortal", "village", "description")

    # L18 Earth Planet
    if "Earth Planet" in name:
        add("Brilliant Golden Fruit site (fed to Mosquito Beast and Thunder Toad)", "resource_zone", "description")

    # L21 Planet Five Elements
    if "Planet Five Elements" in name:
        add("Five Elements cultivation materials source", "resource_zone", "description")

    # L16 Planet Qing Lin
    if any(x in name for x in ("Planet Qing Lin", "Qing Ling")):
        add("Autumn Orchid Valley (where Wang Lin buried Xie Qing)", "valley", "description")

    # L07 Primordial Divine Realm
    if "Primordial Divine Realm" in name:
        add("Tianyunzi's true-body hideout", "hideout", "description")

    # L06 Outer Realm
    if "Outer Realm" in name:
        add("Dao Devil Sect partial base", "sect_partial_base", "description")

    # L01 Root Dao
    if "The Root Dao" in name:
        add("Substrate of all laws (Five Elements, Karma, Reincarnation, Life-Death, True-False, Absolute Beginning/End, Restriction, Slaughter)", "law_substrate", "description")

    # L02 Luo Tian Star System
    if any(x in name for x in ("Luo Tian Star System", "Luo Tian")):
        add("Immortal Astral Continent (floating in Luo Tian's void)", "continent", "description")

    # L58 Brilliant Void
    if "Brilliant Void" in name:
        add("Original name of the Alliance Star System's interior void", "void_region", "description")

    return found


def find_npcs(description: str, key_events: str, body_text: str):
    """Find all NPC names mentioned in the description, key_events, and body."""
    found = []
    seen = set()
    text = " ".join([description or "", key_events or ""])
    for npc in KNOWN_NPCS:
        # Use word-boundary regex; case-sensitive because names are proper nouns
        # but we also want to catch "Wang Lin" in "Wang Lin's"
        pattern = r"\b" + re.escape(npc) + r"\b"
        if re.search(pattern, text):
            if npc.lower() not in seen:
                seen.add(npc.lower())
                found.append(npc)
    return found


def find_factions(description: str, key_events: str, body_text: str):
    """Find all faction names mentioned."""
    found = []
    seen = set()
    text = " ".join([description or "", key_events or ""])
    for fac in KNOWN_FACTIONS:
        pattern = r"\b" + re.escape(fac) + r"\b"
        if re.search(pattern, text):
            if fac.lower() not in seen:
                seen.add(fac.lower())
                found.append(fac)
    return found


def find_chapter_refs(*texts):
    """Find any 'Ch.<n>' / 'ch<n>' / 'EP<n>' / 'Book <n>' / 'chapter <n>' references."""
    found = []
    seen = set()
    pattern = re.compile(
        r"(?i)\b("
        r"Ch\.\s*\d+(?:-\d+)?|"          # Ch.491, Ch.8-9
        r"ch\d+|"                          # ch69
        r"EP\d+|"                          # EP80, EP96
        r"Book\s+\d+(?:-\d+)?|"            # Book 7-8, Book 11
        r"Books\s+\d+|"                    # Books 9
        r"chapter\s+\d+|"                  # chapter 491
        r"Ch\.\"\s*[^.]+\"|"               # Ch.491 ("Why the East Sea") — handled below
        r"AWWP\s+Ch\.\d+"                  # AWWP Ch.1221
        r")"
    )
    for t in texts:
        if not t:
            continue
        for m in pattern.finditer(t):
            ref = m.group(0).strip()
            # Normalize whitespace
            ref = re.sub(r"\s+", " ", ref)
            if ref.lower() not in seen:
                seen.add(ref.lower())
                # Capture surrounding context (30 chars each side)
                start = max(0, m.start() - 30)
                end = min(len(t), m.end() + 30)
                ctx = t[start:end].replace("\n", " ").strip()
                found.append({"ref": ref, "context": ctx})
    return found


# ---------- Main extraction ----------

def main():
    md_text = MD_PATH.read_text(encoding="utf-8")
    db = json.loads(DB_PATH.read_text(encoding="utf-8"))

    # Build a lookup of existing DB entries by ID
    existing_by_id = {loc["id"]: loc for loc in db.get("locations", [])}

    # Split markdown into L-sections
    sections = split_location_sections(md_text)

    enriched = []
    skipped = []

    for header, body in sections:
        loc_id, name, nameCn = extract_header(header)
        if loc_id is None:
            continue
        # We only care about L01..L80
        m = re.match(r"^L(\d+)$", loc_id)
        if not m:
            continue
        num = int(m.group(1))
        if num < 1 or num > 80:
            continue

        kv = parse_kv(body)
        existing = existing_by_id.get(loc_id, {})

        # Description
        description = kv.get("description", "") or ""
        key_events_text = kv.get("key_events", "") or ""
        cosmological_position = kv.get("cosmological_position", "") or ""

        # Type
        loc_type = (kv.get("type", "") or "").strip().lower() or existing.get("type", "")

        # World law tier
        world_law_tier = extract_world_law_tier(kv) or existing.get("worldLawTier")

        # Sealed
        is_sealed_extracted, sealed_by_extracted = extract_sealed(kv)
        # Fall back to existing DB value if markdown didn't have a Sealed? field
        if is_sealed_extracted is None:
            is_sealed = existing.get("isSealed", None)
        else:
            is_sealed = is_sealed_extracted
        # Prefer existing DB's sealedBy when present and richer; otherwise use extraction
        sealed_by = existing.get("sealedBy") or sealed_by_extracted

        # Canon confidence
        canon_conf = extract_canon_confidence(kv)
        if canon_conf is None:
            canon_conf = existing.get("canonConfidence")

        # First appearance — preserve DB's value if present
        first_appearance = existing.get("firstAppearance", None)

        # Spirit veins
        spirit_veins = existing.get("spiritVeins", "") or ""

        # Parent location — preserve DB's value
        parent_location = existing.get("parentLocation", None)

        # Cosmology layer — preserve DB's value
        cosmology_layer = existing.get("cosmologyLayer", None)

        # Build knownFacts from the markdown's description + cosmological_position +
        # key_events sentences (broken into individual facts). Preserve existing
        # DB facts as well, deduped.
        known_facts = []
        seen_facts = set()

        def normalize_fact(s):
            """Normalize a fact for deduplication: lowercase, collapse whitespace,
            normalize quote chars (' and \" -> '), strip trailing punctuation."""
            s = (s or "").strip().strip(".").strip()
            s = s.replace("\u201c", "'").replace("\u201d", "'").replace('"', "'")
            s = s.replace("\u2018", "'").replace("\u2019", "'")
            s = re.sub(r"\s+", " ", s).lower()
            return s

        def add_fact(s):
            s = (s or "").strip().strip(".").strip()
            if not s:
                return
            key = normalize_fact(s)
            if not key:
                return
            if key in seen_facts:
                return
            seen_facts.add(key)
            known_facts.append(s)

        # Existing DB facts (kept verbatim)
        for f in existing.get("knownFacts", []) or []:
            add_fact(f)

        # New facts from cosmological_position (split into individual sentences;
        # do NOT also add the full text as a single fact to avoid duplication)
        if cosmological_position:
            for sent in re.split(r"(?<=[a-z0-9)])\.\s+(?=[A-Z])", cosmological_position):
                add_fact(sent)

        # New facts from description (split on sentence boundaries)
        if description:
            for sent in re.split(r"(?<=[a-z0-9)])\.\s+(?=[A-Z])", description):
                add_fact(sent)

        # Key events as facts (each event clause becomes a fact AND a key event)
        key_events_list = []
        if key_events_text:
            # The key_events field is a single bullet with semicolon-separated clauses
            key_events_list = split_event_list(key_events_text)
            for ev in key_events_list:
                add_fact("Event: " + ev)
        # Also preserve DB's existing keyEvents
        for ev in existing.get("keyEvents", []) or []:
            if ev not in key_events_list:
                key_events_list.append(ev)
            add_fact("Event: " + ev)

        # Sub-locations
        sub_locations = find_sublocations(name, description, key_events_text, body)

        # Notable NPCs
        notable_npcs = find_npcs(description, key_events_text, body)

        # Associated factions (merge DB + extraction)
        associated_factions = []
        seen_fac = set()
        for f in existing.get("associatedFactions", []) or []:
            if f.lower() not in seen_fac:
                seen_fac.add(f.lower())
                associated_factions.append(f)
        for f in find_factions(description, key_events_text, body):
            if f.lower() not in seen_fac:
                seen_fac.add(f.lower())
                associated_factions.append(f)

        # Chapter references
        chapter_refs = find_chapter_refs(description, key_events_text, cosmological_position,
                                          "\n".join(body))

        # Build the enriched location object
        loc_out = {
            "id": loc_id,
            "name": name,
            "nameCn": nameCn or existing.get("nameCn", ""),
            "type": loc_type,
            "parentLocation": parent_location,
            "cosmologyLayer": cosmology_layer,
            "worldLawTier": world_law_tier,
            "isSealed": is_sealed,
            "sealedBy": sealed_by,
            "spiritVeins": spirit_veins,
            "canonConfidence": canon_conf,
            "firstAppearance": first_appearance,
            "description": description,
            "cosmologicalPosition": cosmological_position,
            "knownFacts": known_facts,
            "keyEvents": key_events_list,
            "associatedFactions": associated_factions,
            "subLocations": sub_locations,
            "notableNPCs": notable_npcs,
            "chapterReferences": chapter_refs,
            "source": existing.get("source", ""),
            # Keep the raw markdown body for full traceability
            "rawMarkdownBody": "\n".join(body).strip(),
        }
        enriched.append(loc_out)

    # Sanity check: warn if any L01..L80 missing
    have_ids = {loc["id"] for loc in enriched}
    for i in range(1, 81):
        loc_id = f"L{i:02d}"
        if loc_id not in have_ids:
            skipped.append(loc_id)

    out_obj = {
        "metadata": {
            "source": "Renegade Immortal (仙逆) by Er Gen",
            "extractionScript": "_extract_scripts/extract_world.py",
            "inputMarkdown": "CANON_RI_COMPLETE_WORLD.md (3,034 lines)",
            "inputDatabase": "ri_canon_database.json (locations L01-L80)",
            "extractionMethod": "Markdown section parsing + existing DB merge",
            "totalLocations": len(enriched),
            "missingLocations": skipped,
            "fieldsExtracted": [
                "id", "name", "nameCn", "type", "parentLocation",
                "cosmologyLayer", "worldLawTier", "isSealed", "sealedBy",
                "spiritVeins", "canonConfidence", "firstAppearance",
                "description", "cosmologicalPosition",
                "knownFacts", "keyEvents", "associatedFactions",
                "subLocations", "notableNPCs", "chapterReferences",
                "source", "rawMarkdownBody"
            ],
        },
        "locations": enriched,
    }

    OUT_PATH.write_text(json.dumps(out_obj, ensure_ascii=False, indent=2),
                         encoding="utf-8")
    print(f"Wrote {OUT_PATH}")
    print(f"  Total locations: {len(enriched)}")
    print(f"  Missing: {skipped}")
    total_facts = sum(len(loc["knownFacts"]) for loc in enriched)
    total_events = sum(len(loc["keyEvents"]) for loc in enriched)
    total_subs = sum(len(loc["subLocations"]) for loc in enriched)
    total_npcs = sum(len(loc["notableNPCs"]) for loc in enriched)
    total_facs = sum(len(loc["associatedFactions"]) for loc in enriched)
    total_refs = sum(len(loc["chapterReferences"]) for loc in enriched)
    print(f"  Total knownFacts: {total_facts}")
    print(f"  Total keyEvents: {total_events}")
    print(f"  Total subLocations: {total_subs}")
    print(f"  Total notableNPCs: {total_npcs}")
    print(f"  Total associatedFactions: {total_facs}")
    print(f"  Total chapterReferences: {total_refs}")


if __name__ == "__main__":
    main()
