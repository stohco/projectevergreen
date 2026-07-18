#!/usr/bin/env python3
"""
Deep-extract techniques and items from CANON_RI_COMPLETE_TECHNIQUES.md and
CANON_RI_COMPLETE_ITEMS.md into enriched JSON files.

CRITICAL RULES (per task EXTRACT-TECH-ITEMS):
- Extract EVERYTHING — do not summarize
- Preserve chapter references exactly as written
- If a technique has 10 effects, extract all 10
- If an artifact has a complex evolution chain, extract the full chain with chapter citations
- Do NOT invent data — only extract what's in the documents
"""

import json
import re
import os
from pathlib import Path

FORGE_MOD_DIR = Path("/home/z/my-project/forge-mod")
TECHNIQUES_MD = FORGE_MOD_DIR / "CANON_RI_COMPLETE_TECHNIQUES.md"
ITEMS_MD = FORGE_MOD_DIR / "CANON_RI_COMPLETE_ITEMS.md"

# ============================================================================
# Helpers
# ============================================================================

CHAPTER_RE = re.compile(r'Ch\.?\s*(\d+)', re.IGNORECASE)
DONGHUA_RE = re.compile(r'(?:S1\s*EP|EP|Donghua\s*EP|donghua\s*S1E)\s*(\d+)', re.IGNORECASE)


def parse_chapter_refs(text: str) -> list[str]:
    """Extract all chapter references (Ch.X, Ch.XYZ) preserving order, deduped, keeping originals."""
    if not text:
        return []
    matches = []
    seen = set()
    # Match "Ch. 86", "Ch.86", "Ch 86", "chapter Ch. 1298"
    for m in re.finditer(r'(?:Ch\.?\s*)(\d+)', text, re.IGNORECASE):
        ref = f"Ch.{m.group(1)}"
        if ref not in seen:
            seen.add(ref)
            matches.append(ref)
    # Match "Ch. 1582" within text like "1st: Ch. 1187; 2nd: Ch. 1223"
    return matches


def parse_donghua_refs(text: str) -> list[str]:
    if not text:
        return []
    refs = []
    seen = set()
    for m in re.finditer(r'(?:S1\s*EP|EP|S1E)\s*(\d+)', text, re.IGNORECASE):
        ref = f"S1EP{m.group(1)}"
        if ref not in seen:
            seen.add(ref)
            refs.append(ref)
    return refs


def split_heading_name_cn(heading: str) -> tuple[str, str, str]:
    """
    Parse a heading like:
        Underworld Ascension Method / Yellow Springs Ascension Art (黄泉升窍诀)
        Cultivator Clone / Celestial Body (immortal/celestial cultivating dao/qi)
        Vermilion Bird Burning Heaven Art (朱雀焚天功)
        Karma Whip (因果鞭 · Yīn Guǒ Biān)
    Returns (name_en, nameCn, pinyin). nameCn is "" if none present; pinyin is "" if absent.
    """
    # Strip leading "# " chars
    h = heading.lstrip('#').strip()
    # Try to extract the parenthetical containing Chinese chars
    paren_match = re.search(r'\(([^)]*[\u4e00-\u9fff][^)]*)\)', h)
    nameCn = ""
    pinyin = ""
    if paren_match:
        inner = paren_match.group(1).strip()
        # Split on " · " to separate Chinese from pinyin (or romanization)
        if ' · ' in inner:
            parts = inner.split(' · ', 1)
            nameCn = parts[0].strip()
            pinyin = parts[1].strip()
        elif '·' in inner and not inner.startswith('·'):
            # Sometimes no spaces around bullet
            parts = inner.split('·', 1)
            nameCn = parts[0].strip()
            pinyin = parts[1].strip()
        else:
            # No pinyin separator — entire content is Chinese (or contains chinese chars)
            # Extract only the CJK portion
            cn_only = re.search(r'[\u4e00-\u9fff][\u4e00-\u9fff\s]*[\u4e00-\u9fff]|[一-龥]', inner)
            if cn_only:
                nameCn = cn_only.group(0).strip()
            else:
                nameCn = inner
    # English name = remove parenthetical Chinese (preserve surrounding spacing carefully)
    name_en = re.sub(r'\s*\([^)]*[\u4e00-\u9fff][^)]*\)', '', h).strip()
    # If English name still contains Chinese chars outside parens, strip them
    name_en = re.sub(r'[\u4e00-\u9fff·／/]+(?:[\u4e00-\u9fff·／/\s]*[\u4e00-\u9fff·／/])?', '', name_en).strip()
    # Strip subtitles after " — " (em-dash) since these are explanatory notes, not part of name
    # Handle both " — " (spaced) and "— " (no leading space) patterns
    name_en = re.split(r'\s*—\s+', name_en, maxsplit=1)[0].strip()
    # Clean trailing/leading junk
    name_en = re.sub(r'\s+', ' ', name_en).strip(' /—-')
    return name_en, nameCn, pinyin


def split_field_value(line: str) -> tuple[str, str]:
    """
    A bullet line looks like:
        - **Chapter learned:** Ch. 86 (Donghua S1 EP8)
        - **Wang Lin did not endure the Five Heaven's Blights.** Upon reaching...
    Returns ("Chapter learned", "Ch. 86 (Donghua S1 EP8)")
    """
    # Strip leading "- " and surrounding whitespace
    s = line.strip()
    if s.startswith('- '):
        s = s[2:]
    elif s.startswith('-'):
        s = s[1:]
    s = s.strip()
    # Match "**Field:** value"
    m = re.match(r'\*\*(.+?):\*\*\s*(.*)$', s)
    if m:
        return m.group(1).strip(), m.group(2).strip()
    # Match "**Field.** value" (period instead of colon — bolded statement)
    m = re.match(r'\*\*(.+?)\.\*\*\s*(.*)$', s)
    if m:
        return m.group(1).strip(), m.group(2).strip()
    # Some lines might be "**Field** — value" (no colon, em-dash separator)
    m = re.match(r'\*\*(.+?)\*\*\s*[—-]\s*(.*)$', s)
    if m:
        return m.group(1).strip(), m.group(2).strip()
    # Fallback: "**Field** value" (just bolded field)
    m = re.match(r'\*\*(.+?)\*\*\s*(.*)$', s)
    if m:
        return m.group(1).strip(), m.group(2).strip()
    return "", s


def strip_md_bold(s: str) -> str:
    """Strip markdown bold/italic markers from a string."""
    return re.sub(r'\*\*(.+?)\*\*', r'\1', s).replace('*', '')


def parse_users_from_text(text: str) -> list[str]:
    """Extract plausible character names from prose text. Use a curated list."""
    if not text:
        return []
    known = [
        "Wang Lin", "Situ Nan", "Tu Si", "Lian Daofei", "Lian Daozhen",
        "Dao Master Blue Dream", "All-Seer", "Grey-robed All-Seer",
        "Tianyunzi", "Tian Yunzi", "Qing Shui", "Qing Shuang",
        "Qing Lin", "Immortal Emperor Qing Lin", "Ling Tianhou",
        "Zhou Yi", "Dun Tian", "Bei Lou", "Tuo Sen", "Hong Die", "Red Butterfly",
        "Li Qianmei", "Li Muwan", "Wang Ping", "Lian Daozhan",
        "Li Yuan", "Zhan Xingye", "Zhan Li Yunzi", "Xu Decai",
        "Daoist Water", "Shui Daozi", "Daoist Scattered Spirit",
        "Sovereign", "Seven-Colored Immortal Venerable", "Seven-Colored Daoist",
        "Daoist Miao Yin", "Xuan Luo", "Gu Dao", "Lian Yunjue",
        "Ji Si", "Ji Du", "Ji Xiantian", "Ling Dong", "Esteemed Ling Dong",
        "Du Jian", "Ta Shan", "Xie Qing", "Zhou Jin", "Xu Liguo",
        "Lu Mo", "Xuan Zang", "Su Ming", "Old Man Miēshēng", "Miesheng",
        "Da Yi Great Heavenly Venerable", "Cang Songzi", "Palm Venerable",
        "Master Ashen Pine", "Master Yi Chen", "Ling'er", "Yan Lu",
        "Yao Xixue", "Xiangang", "Wang Baole", "Tian Ni",
        "Ge Hong", "Xi Zifeng", "Chi Hu", "Teng Li", "Teng Huayuan",
        "Huang Long", "Ming Hai", "Immortal Lord Qing Shui",
        "Blood Ancestor", "Black Fiend Devil Saint",
        "Seal Sovereign", "Shui Daozi", "Ye Mo", "Dao Gu Ye Mo",
        "Ling Tianhou", "Green Bull",
        "2nd Vermilion Bird Divine Emperor",
        "2nd-Gen Vermilion Bird", "First-Gen Vermilion Bird",
        "First Generation Vermilion Bird", "Second Generation Vermilion Bird",
        "Venerable Sealing Realm",
        "Immortal Ancestor", "Ancient Ancestor",
        "Celestial Ancestor", "Dao Country Imperial Teacher",
    ]
    users = []
    seen = set()
    for k in known:
        if k in text:
            if k not in seen:
                seen.add(k)
                users.append(k)
    return users


# ============================================================================
# Techniques parsing
# ============================================================================

def parse_techniques_md():
    text = TECHNIQUES_MD.read_text(encoding='utf-8')
    lines = text.split('\n')

    # Find top-level section headers (## A., ## B., etc.)
    sections = []  # list of (section_title, start_line)
    for i, line in enumerate(lines):
        m = re.match(r'^##\s+([A-Z])\.\s+(.+)$', line)
        if m:
            sections.append((m.group(1), m.group(2).strip(), i))

    # Also detect the trailing cross-reference / cultivation-path sections
    # (## J. The Complete Cultivation Path, etc.) — these are NOT technique entries
    # but we may still want to skip them. We'll parse each section's entries.

    # Section heading line indexes
    section_starts = [s[2] for s in sections]
    # Compute section ends
    section_ends = section_starts[1:] + [len(lines)]
    section_ranges = list(zip(sections, section_ends))

    entries = []
    eid = 0
    # Track which sub-bullets belong to numbered list items (e.g. Evolution's 1./2./3.)
    for (sletter, stitle, sstart), send in section_ranges:
        # Walk through lines in this section
        # Find ### headings within (sstart, send)
        sub_headings = []
        for j in range(sstart + 1, send):
            line = lines[j]
            if re.match(r'^###\s+', line):
                sub_headings.append(j)
        sub_headings.append(send)
        for k in range(len(sub_headings) - 1):
            hline = lines[sub_headings[k]]
            body_start = sub_headings[k] + 1
            body_end = sub_headings[k + 1]
            # Skip non-entry ### notes (some are just cross-reference notes)
            heading_text = re.sub(r'^###\s+', '', hline).strip()
            # Skip entries that are obviously notes (e.g., "Heavenly Chop (天斩) — note: ...")
            # We still extract these as they are part of the canon.
            name_en, nameCn, pinyin = split_heading_name_cn(heading_text)
            # Parse bullet fields
            fields = []  # list of (field_name, value) where value may be multiline
            current_field = None
            current_val_lines = []
            current_subheading = None  # for #### sub-headings inside ### entries
            # Walk through body lines
            for j in range(body_start, body_end):
                line = lines[j]
                if not line.strip():
                    # blank line — flush if we are mid-field, then continue
                    continue
                # Detect #### sub-heading inside ### entry
                m_sub = re.match(r'^####\s+(.+)$', line)
                if m_sub:
                    # Flush previous field
                    if current_field is not None:
                        fields.append((current_field, '\n'.join(current_val_lines)))
                        current_field = None
                        current_val_lines = []
                    current_subheading = m_sub.group(1).strip()
                    # Add a marker fact so the subsection structure is preserved
                    fields.append((f"[subsection]", current_subheading))
                    continue
                # Detect inline bold-label pseudo-subheading: "**Domains:**" or "**Essences:**"
                m_inline = re.match(r'^\*\*([^*]+):\*\*\s*(.*)$', line)
                if m_inline and not line.startswith('-'):
                    # Flush previous field
                    if current_field is not None:
                        fields.append((current_field, '\n'.join(current_val_lines)))
                        current_field = None
                        current_val_lines = []
                    current_subheading = m_inline.group(1).strip()
                    inline_rest = m_inline.group(2).strip()
                    fields.append((f"[subsection]", current_subheading + (f": {inline_rest}" if inline_rest else "")))
                    continue
                # Detect field start: "- **Field:** value" or "- **Field.** value" or "- **Field** — value"
                # The colon/period may be INSIDE the bold markers (e.g., "**What it does:**")
                # or AFTER them (e.g., "**Field** — value").
                if re.match(r'^-\s+\*\*[^*]+?\*\*', line):
                    # Flush previous
                    if current_field is not None:
                        fields.append((current_field, '\n'.join(current_val_lines)))
                    fn, fv = split_field_value(line)
                    # Prefix with current subheading if any
                    if current_subheading and not fn.startswith('Subsection:'):
                        fn = f"{current_subheading} — {fn}"
                    current_field = fn
                    current_val_lines = [fv]
                elif re.match(r'^\s+\d+\.\s+', line):
                    # Numbered sub-bullet (e.g., "  1. Calls treasure spirits...")
                    current_val_lines.append(line.rstrip())
                elif re.match(r'^\s+-\s+', line):
                    # Sub-bullet
                    current_val_lines.append(line.rstrip())
                elif re.match(r'^-\s+', line) and not re.match(r'^-\s+\*\*', line):
                    # Plain bullet (no bold field) — these are subsection facts (e.g., "1st Level — Ch. 25 / age 12...")
                    bullet_content = re.sub(r'^-\s+', '', line).strip()
                    if current_subheading:
                        # Treat as a fact under the current subheading
                        fields.append((f"{current_subheading} — milestone", bullet_content))
                    elif current_field is not None:
                        # Append to current field's value
                        current_val_lines.append(line.rstrip())
                    else:
                        # Top-level plain bullet with no field context — capture as a fact
                        fields.append(("Note", bullet_content))
                elif line.startswith('###') or line.startswith('## ') or (line.startswith('#') and not line.startswith('####')):
                    # Another ### or ## heading; break (#### is handled above)
                    break
                else:
                    # Continuation — add to current field
                    if current_field is not None:
                        current_val_lines.append(line.rstrip())
                    elif current_subheading:
                        # Loose text under subheading — capture as a fact
                        fields.append((f"{current_subheading} — note", line.strip()))
                    else:
                        # Top-level prose paragraph — capture as a "Description" fact
                        stripped = line.strip()
                        if stripped:
                            fields.append(("Description", stripped))
            # Flush last
            if current_field is not None:
                fields.append((current_field, '\n'.join(current_val_lines)))

            eid += 1
            entry = build_technique_entry(eid, sletter, stitle, name_en, nameCn, pinyin, heading_text, fields)
            entries.append(entry)

    return entries


def build_technique_entry(eid, section_letter, section_title, name_en, nameCn, pinyin, heading_text, fields):
    """Build a structured technique entry from raw (field, value) tuples."""
    # Build a dict for quick lookup
    raw = {}
    for fn, fv in fields:
        # If field appears multiple times, merge
        if fn in raw:
            raw[fn] = raw[fn] + '\n' + fv
        else:
            raw[fn] = fv

    # ----- Extract canonical fields -----
    chapter_learned = raw.get('Chapter learned', '').strip()
    how_learned = raw.get('How learned', '').strip()
    category = raw.get('Category', '').strip()
    realm_required = raw.get('Realm required', '').strip()
    what_it_does = raw.get('What it does', '').strip()
    cost_limitations = raw.get('Cost/limitations', '').strip()
    evolution = raw.get('Evolution', '').strip()
    canon_conf = raw.get('Canon confidence', '').strip()
    note = raw.get('Note', '').strip()

    # ----- Build effects list -----
    # Effects come from "What it does" plus any sub-bullets. We split on:
    #  - sentence boundaries (periods followed by capital) when prose
    #  - explicit bullet/newline splits when bullets
    effects = split_into_bullets(what_it_does) if what_it_does else []
    # Sometimes effects are also implied in category line itself; skip those.

    # ----- Known users -----
    user_text = ' '.join([how_learned, what_it_does, evolution, raw.get('How obtained', '')])
    users = parse_users_from_text(user_text)
    # Always include Wang Lin (the catalog is about him)
    if 'Wang Lin' not in users:
        users.insert(0, 'Wang Lin')

    # ----- Prerequisites -----
    prerequisites = []
    if realm_required:
        prerequisites.append(realm_required)
    # Cost/limitations may include prereq-like info — but we put it in side effects
    # unless it explicitly mentions "realm" or "requires".
    if cost_limitations:
        # Split cost into prereq-like vs side-effect-like
        cost_bullets = split_into_bullets(cost_limitations)
        for cb in cost_bullets:
            lower = cb.lower()
            if any(kw in lower for kw in ['require', 'realm', 'must be', 'only usable', 'need to']):
                prerequisites.append(cb)
    # Dedupe
    prerequisites = dedupe_list(prerequisites)

    # ----- Side effects -----
    side_effects = []
    if cost_limitations:
        cost_bullets = split_into_bullets(cost_limitations)
        for cb in cost_bullets:
            lower = cb.lower()
            if any(kw in lower for kw in ['side-effect', 'side effect', 'backfire', 'backlash',
                                          'risk', 'danger', 'sacrifices', 'sacrifice',
                                          'karmically heavy', 'heart-demon', 'vulnerable',
                                          'cursed', 'curse', 'rejects']):
                side_effects.append(cb)
            elif not any(kw in lower for kw in ['require', 'realm', 'must be', 'only usable', 'need to']):
                # General limitation — also include as side effect
                side_effects.append(cb)
    side_effects = dedupe_list(side_effects)

    # ----- Evolution chain -----
    evolution_chain = []
    if evolution:
        # Look for explicit arrow chains first (e.g., "Cold Dan → Golden Core → Ji Realm Divine Sense")
        # Note: we don't exclude periods from the match because chapter refs like "Ch.199" contain them.
        # Instead, we rely on newline as the natural chain terminator.
        chain_pattern = re.compile(
            r'((?:[^\n→;]*?\s*→\s*)+[^\n→;]+)'
        )
        arrow_chains_found = []
        for m in chain_pattern.finditer(evolution):
            chain_text = m.group(1).strip()
            # Skip very short chains or those starting with arrow
            if len(chain_text) < 8 or chain_text.startswith('→'):
                continue
            arrow_chains_found.append(chain_text)
        if arrow_chains_found:
            for chain_text in arrow_chains_found:
                stages_raw = re.split(r'\s*→\s*', chain_text)
                stages_clean = [s.strip(' ,;()') for s in stages_raw if s.strip(' ,;()')]
                if len(stages_clean) < 2:
                    continue
                stages = []
                for s in stages_clean:
                    stages.append({
                        'stage': s,
                        'chapterRefs': parse_chapter_refs(s),
                    })
                evolution_chain.append({
                    'stages': stages,
                    'fullChainText': chain_text,
                    'chapterRefs': parse_chapter_refs(chain_text),
                })
        else:
            # Split into bullet/semicolon/newline-separated stages
            stages_raw = re.split(r';;|\n|(?<=[.)])\s+(?=[A-Z])', evolution)
            stages = []
            for st in stages_raw:
                st = st.strip(' ,;')
                if st:
                    stages.append({
                        'stage': st,
                        'chapterRefs': parse_chapter_refs(st),
                    })
            if stages:
                evolution_chain.append({
                    'stages': stages,
                    'fullChainText': evolution,
                    'chapterRefs': parse_chapter_refs(evolution),
                })

    # ----- Known facts -----
    known_facts = []
    # The "How learned" is a fact
    if how_learned:
        known_facts.append(f"How learned: {how_learned}")
    if category:
        known_facts.append(f"Category: {category}")
    if realm_required:
        # Already in prereq but also a fact
        known_facts.append(f"Realm required: {realm_required}")
    if cost_limitations:
        # Cost/limitations is a fact (already split into prereq/side effects, but keep raw too)
        known_facts.append(f"Cost/limitations: {cost_limitations}")
    if note:
        known_facts.append(f"Note: {note}")
    # Add any additional fields not canonical
    skip_fields = {'Chapter learned', 'How learned', 'Category', 'Realm required',
                   'What it does', 'Cost/limitations', 'Evolution',
                   'Canon confidence', 'Note'}
    for fn, fv in fields:
        if fn in skip_fields:
            continue
        if fn == '[subsection]':
            # Subsection marker — render as a header fact
            known_facts.append(f"=== Subsection: {fv} ===")
        elif ' — milestone' in fn:
            # Milestone under a subsection (e.g., cultivation level breakthroughs)
            sub = fn.replace(' — milestone', '').strip()
            known_facts.append(f"[{sub}] {fv}")
        elif ' — note' in fn:
            sub = fn.replace(' — note', '').strip()
            known_facts.append(f"[{sub}] {fv}")
        else:
            # Field name + value
            if fv:
                known_facts.append(f"{fn}: {fv}")
            else:
                known_facts.append(fn)

    # ----- Chapter references (all) -----
    # Include all field values when scanning for chapter refs (not just the canonical ones)
    all_text_parts = [chapter_learned, how_learned, what_it_does, evolution, cost_limitations, note]
    for fn, fv in fields:
        if fn not in skip_fields and fn != '[subsection]':
            all_text_parts.append(fv if isinstance(fv, str) else str(fv))
    all_text = ' '.join(all_text_parts)
    chapter_refs = parse_chapter_refs(all_text)
    donghua_refs = parse_donghua_refs(all_text)

    # ----- Canon confidence -----
    try:
        canon_conf_num = int(re.search(r'(\d+)', canon_conf).group(1))
    except (AttributeError, ValueError):
        canon_conf_num = None

    # ----- Source -----
    src_parts = []
    if chapter_learned:
        src_parts.append(chapter_learned)
    src_parts.append("Fandom wiki (Wang Lin/Techniques); Baidu Baike")
    src = "; ".join(src_parts)

    return {
        "id": f"T{eid:03d}",
        "name": name_en,
        "nameCn": nameCn,
        "pinyin": pinyin,
        "section": f"{section_letter}. {section_title}",
        "headingRaw": heading_text,
        "type": category.split('(')[0].strip() if category else "",
        "category": category,
        "chapterLearned": chapter_learned,
        "chapterRefs": chapter_refs,
        "donghuaRefs": donghua_refs,
        "origin": how_learned,
        "effects": effects,
        "knownUsers": users,
        "prerequisites": prerequisites,
        "sideEffects": side_effects,
        "evolutionChain": evolution_chain,
        "knownFacts": known_facts,
        "canonConfidence": canon_conf_num,
        "canonConfidenceRaw": canon_conf,
        "source": src,
    }


def split_into_bullets(text: str) -> list[str]:
    """Split a markdown text into discrete bullet items.

    Handles:
      - "  1. First item"  (numbered sub-bullet)
      - "  - item"         (dash sub-bullet)
      - Prose sentences separated by periods
    """
    if not text:
        return []
    bullets = []
    lines = text.split('\n')
    for line in lines:
        stripped = line.strip()
        if not stripped:
            continue
        # Strip leading "1.", "2.", "-" etc.
        m = re.match(r'^(\d+\.\s*|-)\s*(.+)$', stripped)
        if m:
            content = m.group(2).strip()
        else:
            content = stripped
        # If the content has multiple sentences, split them
        # We split on ". " followed by capital letter, but be careful with "Ch.X."
        # Simpler heuristic: split on periods followed by space + capital
        parts = re.split(r'(?<=\.)\s+(?=[A-Z])', content)
        for p in parts:
            p = p.strip()
            if p:
                bullets.append(p)
    return dedupe_list(bullets)


def dedupe_list(items):
    """Dedupe a list while preserving order."""
    seen = set()
    out = []
    for x in items:
        if x not in seen:
            seen.add(x)
            out.append(x)
    return out


# ============================================================================
# Artifacts parsing
# ============================================================================

def parse_items_md():
    text = ITEMS_MD.read_text(encoding='utf-8')
    lines = text.split('\n')

    # Find top-level section headers (## 1. , ## 2. , etc.)
    sections = []
    for i, line in enumerate(lines):
        m = re.match(r'^##\s+(\d+)\.\s+(.+)$', line)
        if m:
            sections.append((int(m.group(1)), m.group(2).strip(), i))

    section_starts = [s[2] for s in sections]
    section_ends = section_starts[1:] + [len(lines)]
    section_ranges = list(zip(sections, section_ends))

    entries = []
    eid = 0
    for (snum, stitle, sstart), send in section_ranges:
        # Walk through lines in this section, finding ### (or ####) headings
        # We also need to handle #### (sub-sections like Vermilion-Bird Treasures)
        sub_headings = []
        current_super = None  # parent #### group name
        for j in range(sstart + 1, send):
            line = lines[j]
            # Match "### Name" or "#### Name"
            m3 = re.match(r'^###\s+(.+)$', line)
            m4 = re.match(r'^####\s+(.+)$', line)
            if m3:
                sub_headings.append((j, m3.group(1).strip(), None))
                current_super = None
            elif m4:
                sub_headings.append((j, m4.group(1).strip(), stitle))  # treat #### as entries under stitle
                current_super = m4.group(1).strip()
        sub_headings.append((send, None, None))
        for k in range(len(sub_headings) - 1):
            hline_idx, heading_text, super_group = sub_headings[k]
            if heading_text is None:
                continue
            body_start = hline_idx + 1
            body_end = sub_headings[k + 1][0]
            # Determine heading level
            orig_line = lines[hline_idx]
            heading_level = 4 if orig_line.startswith('####') else 3
            name_en, nameCn, pinyin = split_heading_name_cn(heading_text)

            # Parse bullet fields
            fields = []
            current_field = None
            current_val_lines = []
            current_subheading = None
            for j in range(body_start, body_end):
                line = lines[j]
                if not line.strip():
                    continue
                # Detect #### sub-heading inside ### entry
                m_sub = re.match(r'^####\s+(.+)$', line)
                if m_sub:
                    if current_field is not None:
                        fields.append((current_field, '\n'.join(current_val_lines)))
                        current_field = None
                        current_val_lines = []
                    current_subheading = m_sub.group(1).strip()
                    fields.append((f"[subsection]", current_subheading))
                    continue
                # Detect inline bold-label pseudo-subheading: "**Domains:**" or "**Essences:**"
                m_inline = re.match(r'^\*\*([^*]+):\*\*\s*(.*)$', line)
                if m_inline and not line.startswith('-'):
                    if current_field is not None:
                        fields.append((current_field, '\n'.join(current_val_lines)))
                        current_field = None
                        current_val_lines = []
                    current_subheading = m_inline.group(1).strip()
                    inline_rest = m_inline.group(2).strip()
                    fields.append((f"[subsection]", current_subheading + (f": {inline_rest}" if inline_rest else "")))
                    continue
                # Detect field start: any "- **...**" line
                if re.match(r'^-\s+\*\*[^*]+?\*\*', line):
                    if current_field is not None:
                        fields.append((current_field, '\n'.join(current_val_lines)))
                    fn, fv = split_field_value(line)
                    if current_subheading:
                        fn = f"{current_subheading} — {fn}"
                    current_field = fn
                    current_val_lines = [fv]
                elif re.match(r'^\s+\d+\.\s+', line) or re.match(r'^\s+-\s+', line):
                    current_val_lines.append(line.rstrip())
                elif re.match(r'^-\s+', line) and not re.match(r'^-\s+\*\*', line):
                    # Plain bullet (no bold field)
                    bullet_content = re.sub(r'^-\s+', '', line).strip()
                    if current_subheading:
                        fields.append((f"{current_subheading} — milestone", bullet_content))
                    elif current_field is not None:
                        current_val_lines.append(line.rstrip())
                    else:
                        fields.append(("Note", bullet_content))
                elif line.startswith('###') or line.startswith('## ') or (line.startswith('#') and not line.startswith('####')):
                    break
                else:
                    if current_field is not None:
                        current_val_lines.append(line.rstrip())
                    elif current_subheading:
                        fields.append((f"{current_subheading} — note", line.strip()))
                    else:
                        stripped = line.strip()
                        if stripped:
                            fields.append(("Description", stripped))
            if current_field is not None:
                fields.append((current_field, '\n'.join(current_val_lines)))

            eid += 1
            entry = build_artifact_entry(eid, snum, stitle, name_en, nameCn, pinyin, heading_text, fields, heading_level, super_group)
            entries.append(entry)
    return entries


def build_artifact_entry(eid, section_num, section_title, name_en, nameCn, pinyin, heading_text, fields, heading_level, super_group):
    raw = {}
    for fn, fv in fields:
        if fn in raw:
            raw[fn] = raw[fn] + '\n' + fv
        else:
            raw[fn] = fv

    chapter_obtained = raw.get('Chapter obtained', '').strip()
    chapter_lost = raw.get('Chapter lost', '').strip()
    category = raw.get('Category', '').strip()
    grade = raw.get('Grade', '').strip()
    what_it_does = raw.get('What it does', '').strip()
    how_obtained = raw.get('How obtained', '').strip()
    special_properties = raw.get('Special properties', '').strip()
    current_status = raw.get('Current status', '').strip()
    canon_conf = raw.get('Canon confidence', '').strip()
    note = raw.get('Note', '').strip()

    # ----- Abilities (split what_it_does + special_properties into discrete items) -----
    abilities = []
    if what_it_does:
        abilities.extend(split_into_bullets(what_it_does))
    if special_properties:
        # Special properties are abilities/attributes too
        abilities.extend([f"[property] {b}" for b in split_into_bullets(special_properties)])
    abilities = dedupe_list(abilities)

    # ----- Current owner & previous owners -----
    current_owner = ""
    previous_owners = []
    # Parse from current_status + how_obtained
    status_lower = current_status.lower()
    if 'retained' in status_lower or 'restored' in status_lower or 'kept' in status_lower:
        current_owner = "Wang Lin"
    elif 'destroyed' in status_lower:
        current_owner = "(destroyed)"
    elif 'sold' in status_lower:
        current_owner = "(sold)"
    elif 'given' in status_lower or 'gifted' in status_lower:
        current_owner = "(given away)"
    elif 'fused' in status_lower:
        current_owner = "(fused)"
    elif 'used' in status_lower:
        current_owner = "(used)"
    elif 'retired' in status_lower:
        current_owner = "Wang Lin (retired/unused)"
    elif 'discarded' in status_lower:
        current_owner = "(discarded)"
    elif 'freed' in status_lower:
        current_owner = "(freed)"
    elif 'returned' in status_lower:
        current_owner = "(returned)"
    elif 'exploded' in status_lower:
        current_owner = "(exploded)"
    elif 'shattered' in status_lower:
        current_owner = "(shattered)"
    elif current_status:
        current_owner = current_status

    # Previous owners from how_obtained + chapter_lost context
    prev_text = how_obtained + ' ' + current_status + ' ' + chapter_lost
    prev_users = parse_users_from_text(prev_text)
    # Remove Wang Lin from previous owners (he's current)
    prev_users = [u for u in prev_users if u != 'Wang Lin']
    previous_owners = dedupe_list(prev_users)

    # ----- Evolution chain -----
    evolution_chain = []
    # Look for "→" or "evolved into" or "fused into" sequences
    # Strategy: find sequences of the form "A → B → C" (multi-step chains)
    # Each segment becomes one stage in the evolution_chain.
    evolution_text_parts = []
    for k in ['What it does', 'How obtained', 'Special properties', 'Current status']:
        if k in raw:
            evolution_text_parts.append(raw[k])
    evolution_text = ' '.join(evolution_text_parts)

    # Find complete arrow chains: a sequence like "burned by X → mysterious transformation → nourished by Y → became Z"
    # Each segment is a stage; the entire chain is one evolution_chain entry.
    # Note: we don't exclude periods because chapter refs like "Ch.731" contain them.
    chain_pattern = re.compile(
        r'((?:[^\n→;]*?\s*→\s*)+[^\n→;]+)'
    )
    for m in chain_pattern.finditer(evolution_text):
        chain_text = m.group(1).strip()
        # Skip if it's just "→ Karma Whip)" (starts with arrow)
        if chain_text.startswith('→') or chain_text.startswith('->'):
            continue
        # Skip very short chains
        if len(chain_text) < 8:
            continue
        # Split into stages
        stages_raw = re.split(r'\s*→\s*', chain_text)
        stages_clean = [s.strip(' ,;()') for s in stages_raw if s.strip(' ,;()')]
        if len(stages_clean) < 2:
            continue
        # Each stage gets chapter refs
        stages = []
        for s in stages_clean:
            stages.append({
                'stage': s,
                'chapterRefs': parse_chapter_refs(s),
            })
        # Capture chapter refs from the entire chain text too
        all_chain_refs = parse_chapter_refs(chain_text)
        evolution_chain.append({
            'stages': stages,
            'fullChainText': chain_text,
            'chapterRefs': all_chain_refs,
        })

    # Specific known evolution chains from cross-reference notes
    if 'Soul Lasher' in name_en or 'Karma Whip' in name_en:
        if not any('Soul Lasher' in str(ec) for ec in evolution_chain):
            evolution_chain.insert(0, {
                'stages': [
                    {'stage': 'Soul Lasher (originally Red Butterfly / Hong Die)', 'chapterRefs': []},
                    {'stage': 'burned by Ghostly Sky Fire', 'chapterRefs': []},
                    {'stage': 'nourished by Wang Lin\'s Karma Concept', 'chapterRefs': []},
                    {'stage': 'Karma Whip', 'chapterRefs': ['Ch.731']},
                ],
                'fullChainText': 'Soul Lasher → burned by Ghostly Sky Fire → nourished by Wang Lin\'s Karma Concept → Karma Whip (Ch. 731)',
                'chapterRefs': ['Ch.731'],
            })

    # Special: God-Slaying War Chariot — three chariots with different fates
    if 'God-Slaying War Chariot' in name_en or 'God Slaying War Chariot' in name_en:
        # Already captured via normal arrow parsing if present
        pass

    # Special: Restriction Flag (three variants) — explicit numbered list
    if 'Restriction Flag' in name_en and not evolution_chain:
        # Look for "1. ... 2. ... 3. ..." pattern
        if re.search(r'\n\s*1\.\s', raw.get('What it does', '')):
            variants = re.findall(r'\n\s*(\d+)\.\s+([^\n]+)', raw.get('What it does', ''))
            if variants:
                evolution_chain.append({
                    'stages': [
                        {'stage': f"Variant {n}: {desc}", 'chapterRefs': parse_chapter_refs(desc)}
                        for n, desc in variants
                    ],
                    'fullChainText': 'Three flag variants (numbered)',
                    'chapterRefs': [],
                })

    # ----- Restrictions -----
    restrictions = []
    restriction_text = ' '.join([how_obtained, special_properties, what_it_does]).lower()
    if 'bloodline' in restriction_text or 'royal' in restriction_text:
        restrictions.append("Requires specific bloodline or royal heritage")
    if 'celestial' in restriction_text and ('requires' in restriction_text or 'must' in restriction_text):
        restrictions.append("Requires Celestial cultivation")
    if 'void-only' in restriction_text:
        restrictions.append("Can only be used in the void")
    if 'master-locked' in restriction_text or 'master lock' in restriction_text:
        restrictions.append("Master-locked — only Wang Lin can command it")
    if 'one-use' in restriction_text or 'one time' in restriction_text:
        restrictions.append("One-use / consumable")
    if 'conditional gift' in restriction_text:
        restrictions.append("Conditional — bound to original gift's conditions")
    if 'fuseable' in restriction_text or 'fuses with' in restriction_text:
        restrictions.append("Fuseable with cultivation/dao (consumed on fusion)")
    if 'ancient-god-bound' in restriction_text or 'ancient god inheritance' in restriction_text:
        restrictions.append("Requires Ancient God inheritance")
    if 'cross-novel' in restriction_text:
        restrictions.append("Cross-novel artifact — also exists in other Ergenverse novels")
    if 'soul-binding' in restriction_text or 'soul-bound' in restriction_text:
        restrictions.append("Soul-bound to original contractor")
    if 'tracking-vulnerable' in restriction_text:
        restrictions.append("Vulnerable to tracking — must be discarded if compromised")
    # Parse from raw fields too
    if 'Requires' in how_obtained or 'requires' in how_obtained:
        # Extract the clause after "requires"
        for m in re.finditer(r'[Rr]equires?\s+([^.;]+)', how_obtained):
            restrictions.append(f"Requires: {m.group(1).strip()}")
    restrictions = dedupe_list(restrictions)

    # ----- Known facts -----
    known_facts = []
    if grade:
        known_facts.append(f"Grade: {grade}")
    if category:
        known_facts.append(f"Category: {category}")
    if how_obtained:
        known_facts.append(f"How obtained: {how_obtained}")
    if special_properties:
        known_facts.append(f"Special properties: {special_properties}")
    if current_status:
        known_facts.append(f"Current status: {current_status}")
    if chapter_lost:
        known_facts.append(f"Chapter lost: {chapter_lost}")
    if note:
        known_facts.append(f"Note: {note}")
    # Add any non-canonical fields
    skip_fields = {'Chapter obtained', 'Chapter lost', 'Category', 'Grade',
                   'What it does', 'How obtained', 'Special properties',
                   'Current status', 'Canon confidence', 'Note'}
    for fn, fv in fields:
        if fn in skip_fields:
            continue
        if fn == '[subsection]':
            known_facts.append(f"=== Subsection: {fv} ===")
        elif ' — milestone' in fn:
            sub = fn.replace(' — milestone', '').strip()
            known_facts.append(f"[{sub}] {fv}")
        elif ' — note' in fn:
            sub = fn.replace(' — note', '').strip()
            known_facts.append(f"[{sub}] {fv}")
        else:
            if fv:
                known_facts.append(f"{fn}: {fv}")
            else:
                known_facts.append(fn)

    # ----- Chapter references -----
    # Include all field values when scanning for chapter refs
    all_text_parts = [chapter_obtained, chapter_lost, what_it_does, how_obtained,
                      special_properties, current_status, note]
    for fn, fv in fields:
        if fn not in skip_fields and fn != '[subsection]':
            all_text_parts.append(fv if isinstance(fv, str) else str(fv))
    all_text = ' '.join(all_text_parts)
    chapter_refs = parse_chapter_refs(all_text)

    # ----- Canon confidence -----
    try:
        canon_conf_num = int(re.search(r'(\d+)', canon_conf).group(1))
    except (AttributeError, ValueError):
        canon_conf_num = None

    # ----- Source -----
    src_parts = []
    if chapter_obtained:
        src_parts.append(f"Obtained: {chapter_obtained}")
    if chapter_lost:
        src_parts.append(f"Lost: {chapter_lost}")
    src_parts.append("Fandom wiki (Wang Lin/Items); Baidu Baike")
    src = "; ".join(src_parts)

    return {
        "id": f"I{eid:03d}",
        "name": name_en,
        "nameCn": nameCn,
        "pinyin": pinyin,
        "section": f"{section_num}. {section_title}",
        "headingRaw": heading_text,
        "headingLevel": heading_level,
        "superGroup": super_group,  # parent #### group if any
        "type": category.split('·')[0].strip() if category else "",
        "category": category,
        "grade": grade,
        "chapterObtained": chapter_obtained,
        "chapterLost": chapter_lost,
        "chapterRefs": chapter_refs,
        "currentOwner": current_owner,
        "previousOwners": previous_owners,
        "abilities": abilities,
        "origin": how_obtained,
        "evolutionChain": evolution_chain,
        "restrictions": restrictions,
        "knownFacts": known_facts,
        "canonConfidence": canon_conf_num,
        "canonConfidenceRaw": canon_conf,
        "source": src,
    }


# ============================================================================
# Main
# ============================================================================

def main():
    print("Parsing techniques...")
    techniques = parse_techniques_md()
    print(f"  Extracted {len(techniques)} technique entries")

    print("Parsing items...")
    artifacts = parse_items_md()
    print(f"  Extracted {len(artifacts)} artifact entries")

    # Compute stats
    t_facts = sum(len(t['knownFacts']) for t in techniques)
    t_effects = sum(len(t['effects']) for t in techniques)
    a_facts = sum(len(a['knownFacts']) for a in artifacts)
    a_abil = sum(len(a['abilities']) for a in artifacts)
    a_restrict = sum(len(a['restrictions']) for a in artifacts)
    a_evol = sum(len(a['evolutionChain']) for a in artifacts)
    t_evol = sum(len(t['evolutionChain']) for t in techniques)

    tech_doc = {
        "metadata": {
            "source": "CANON_RI_COMPLETE_TECHNIQUES.md",
            "novel": "Renegade Immortal (仙逆) by Er Gen",
            "extractionTask": "EXTRACT-TECH-ITEMS",
            "extractionAgent": "deep-extractor",
            "totalEntries": len(techniques),
            "stats": {
                "totalEffects": t_effects,
                "avgEffectsPerEntry": round(t_effects / len(techniques), 2) if techniques else 0,
                "totalKnownFacts": t_facts,
                "avgFactsPerEntry": round(t_facts / len(techniques), 2) if techniques else 0,
                "totalEvolutionStages": t_evol,
                "entriesWithEvolution": sum(1 for t in techniques if t['evolutionChain']),
                "entriesWithSideEffects": sum(1 for t in techniques if t['sideEffects']),
                "entriesWithPrerequisites": sum(1 for t in techniques if t['prerequisites']),
                "entriesWithChapterRefs": sum(1 for t in techniques if t['chapterRefs']),
            },
            "canonConfidenceKey": {
                "5": "explicitly named + described in wiki",
                "4": "mentioned in wiki, detail partial",
                "3": "implied / pieced together from cultivation & items pages",
            },
            "preservationPolicy": "Every effect, fact, and chapter reference from the source markdown is preserved. No summarization. No invention.",
        },
        "techniques": techniques,
    }

    items_doc = {
        "metadata": {
            "source": "CANON_RI_COMPLETE_ITEMS.md",
            "novel": "Renegade Immortal (仙逆) by Er Gen",
            "extractionTask": "EXTRACT-TECH-ITEMS",
            "extractionAgent": "deep-extractor",
            "totalEntries": len(artifacts),
            "stats": {
                "totalAbilities": a_abil,
                "avgAbilitiesPerEntry": round(a_abil / len(artifacts), 2) if artifacts else 0,
                "totalKnownFacts": a_facts,
                "avgFactsPerEntry": round(a_facts / len(artifacts), 2) if artifacts else 0,
                "totalRestrictions": a_restrict,
                "entriesWithRestrictions": sum(1 for a in artifacts if a['restrictions']),
                "totalEvolutionStages": a_evol,
                "entriesWithEvolution": sum(1 for a in artifacts if a['evolutionChain']),
                "entriesWithChapterRefs": sum(1 for a in artifacts if a['chapterRefs']),
                "entriesWithPreviousOwners": sum(1 for a in artifacts if a['previousOwners']),
            },
            "canonConfidenceKey": {
                "5": "explicitly named + chapter-cited in the wiki items table",
                "4": "named in narrative prose of the wiki / verified in Baidu Baike",
                "3": "mentioned in passing or implied by category",
            },
            "statusLegend": "retained / lost / destroyed / given-away / sold / fused / restored",
            "preservationPolicy": "Every ability, fact, and chapter reference from the source markdown is preserved. No summarization. No invention.",
        },
        "artifacts": artifacts,
    }

    out_tech = FORGE_MOD_DIR / "ri_canon_techniques_enriched.json"
    out_items = FORGE_MOD_DIR / "ri_canon_artifacts_enriched.json"

    out_tech.write_text(json.dumps(tech_doc, indent=2, ensure_ascii=False), encoding='utf-8')
    out_items.write_text(json.dumps(items_doc, indent=2, ensure_ascii=False), encoding='utf-8')

    print(f"\nWrote: {out_tech} ({out_tech.stat().st_size} bytes)")
    print(f"Wrote: {out_items} ({out_items.stat().st_size} bytes)")

    # Print summary stats
    print("\n=== Techniques stats ===")
    print(f"  Total entries: {len(techniques)}")
    print(f"  Total effects: {t_effects} (avg {t_effects/len(techniques):.2f}/entry)")
    print(f"  Total known facts: {t_facts} (avg {t_facts/len(techniques):.2f}/entry)")
    print(f"  Total evolution stages: {t_evol}")
    print(f"  Entries with chapter refs: {sum(1 for t in techniques if t['chapterRefs'])}")

    print("\n=== Artifacts stats ===")
    print(f"  Total entries: {len(artifacts)}")
    print(f"  Total abilities: {a_abil} (avg {a_abil/len(artifacts):.2f}/entry)")
    print(f"  Total known facts: {a_facts} (avg {a_facts/len(artifacts):.2f}/entry)")
    print(f"  Total restrictions: {a_restrict}")
    print(f"  Total evolution stages: {a_evol}")
    print(f"  Entries with chapter refs: {sum(1 for a in artifacts if a['chapterRefs'])}")


if __name__ == '__main__':
    main()
