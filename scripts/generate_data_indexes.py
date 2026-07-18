#!/usr/bin/env python3
"""
Generate _index.json manifests for each data/ergenverse/ subsystem directory.

The WorldStateDataLoader (Java) reads /data/ergenverse/<subsystem>/_index.json
to discover which JSON files to load from the classpath at runtime (Java
classpath resources don't support directory listing portably).

Each _index.json is a JSON array of filenames (relative to the directory),
excluding files starting with '_' (those are registries/comments, not entities).

Idempotent — re-running overwrites the index files.
"""
from __future__ import annotations
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src" / "main" / "resources" / "data" / "ergenverse"

SUBSYSTEMS = [
    "species", "species_variants", "ecosystems", "ecosystem_integration",
    "migrations", "macro_terrain", "provenance", "civilizations", "npcs",
    "faction_relationships", "opportunities", "time_events", "item_properties",
    "karma",
]

def main():
    print("=== Generating _index.json manifests ===")
    total_files = 0
    total_indexed = 0
    for sub in SUBSYSTEMS:
        d = DATA / sub
        if not d.exists():
            print(f"  {sub:24s} SKIP (dir missing)")
            continue
        files = sorted(f.name for f in d.glob("*.json") if not f.name.startswith("_"))
        index_path = d / "_index.json"
        with open(index_path, "w", encoding="utf-8") as fh:
            json.dump(files, fh, ensure_ascii=False, indent=2)
            fh.write("\n")
        total_files += len(files)
        total_indexed += 1
        print(f"  {sub:24s} {len(files):4d} files indexed")
    print(f"\nTotal: {total_files} files across {total_indexed} subsystems.")

if __name__ == "__main__":
    main()
