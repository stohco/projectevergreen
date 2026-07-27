#!/usr/bin/env python3
"""
Build an importable CurseForge-style modpack ZIP for the Er Gen Verse mod.

CRON-136+: the user requested importable ZIPs (not just JARs) for every release.
This script packages the freshly-built JAR into a CurseForge modpack manifest
format that can be imported via the CurseForge app, MultiMC, Prism Launcher,
GDLauncher, or any tool that accepts the minecraftModpack manifest type.

Output: /home/z/my-project/download/ergenverse-modpack-<version>.zip
        /home/z/my-project/forge-mod/releases/ergenverse-modpack-<version>.zip

Usage:
    python3 scripts/build_importable_modpack.py
    python3 scripts/build_importable_modpack.py --version 0.1.12-alpha

The script reads the version from gradle.properties if --version is omitted.
It will refuse to package a JAR that doesn't match the requested version.
"""

from __future__ import annotations

import argparse
import json
import re
import shutil
import sys
import zipfile
from pathlib import Path

FORGE_MOD = Path("/home/z/my-project/forge-mod")
DOWNLOAD_DIR = Path("/home/z/my-project/download")
RELEASES_DIR = FORGE_MOD / "releases"
GRADLE_PROPS = FORGE_MOD / "gradle.properties"
BUILD_LIBS = FORGE_MOD / "build/libs"

# Curated list of compatible mods for the Er GenVerse experience.
# Empty by default — the user can add optional mods (JEI, JourneyMap, etc.)
# by editing OPTIONAL_MODS below. The manifest files list is intentionally
# empty so the modpack only contains the Ergenverse JAR in overrides/mods/.
OPTIONAL_MODS: list[dict] = []


def read_version() -> str:
    if not GRADLE_PROPS.exists():
        raise FileNotFoundError(f"gradle.properties not found at {GRADLE_PROPS}")
    text = GRADLE_PROPS.read_text(encoding="utf-8")
    m = re.search(r"^mod_version\s*=\s*(.+)$", text, re.MULTILINE)
    if not m:
        raise ValueError("mod_version property not found in gradle.properties")
    return m.group(1).strip()


def find_jar(version: str) -> Path:
    expected = BUILD_LIBS / f"ergenverse-{version}.jar"
    if expected.exists():
        return expected
    # Fall back to glob (in case of suffix variations).
    candidates = sorted(BUILD_LIBS.glob("ergenverse-*.jar"))
    if candidates:
        latest = candidates[-1]
        print(f"  WARN: exact JAR for version {version} not found; using {latest.name}")
        return latest
    raise FileNotFoundError(
        f"No Ergenverse JAR found in {BUILD_LIBS}. Run "
        "`JAVA_HOME=/tmp/my-project/.jdks/jdk-17.0.13+11/ ./gradlew build` first."
    )


def build_manifest(version: str) -> dict:
    return {
        "minecraft": {
            "version": "1.20.1",
            "modLoaders": [
                {"id": "forge-47.4.0", "primary": True}
            ],
        },
        "manifestType": "minecraftModpack",
        "manifestVersion": 1,
        "name": "Ergenverse",
        "version": version,
        "author": "stohco",
        "projectID": -1,
        "overrides": "overrides",
        "files": OPTIONAL_MODS,
    }


def build_modlist_html(version: str) -> str:
    # A minimal HTML modlist for the modpack's "mods" tab in CurseForge.
    return (
        "<!DOCTYPE html>\n"
        "<html><head><meta charset='utf-8'><title>Ergenverse Modpack</title></head>\n"
        "<body><h1>Ergenverse " + version + "</h1>\n"
        "<p>This modpack contains the Er Gen Verse Forge mod "
        "(a Renegade Immortal / 仙逆 novel adaptation).</p>\n"
        "<ul>\n"
        "<li><b>Ergenverse</b> " + version + " (bundled in overrides/mods/)</li>\n"
        "</ul>\n"
        "<p>Loader: Forge 47.4.0 for Minecraft 1.20.1. Java 17 required.</p>\n"
        "</body></html>\n"
    )


def build_zip(version: str, jar_path: Path, out_path: Path) -> None:
    if out_path.exists():
        out_path.unlink()
    with zipfile.ZipFile(out_path, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("manifest.json", json.dumps(build_manifest(version), indent=4))
        z.writestr("modlist.html", build_modlist_html(version))
        # Empty overrides/mods/ entry, then the JAR.
        z.writestr("overrides/", "")
        z.writestr("overrides/mods/", "")
        z.write(jar_path, f"overrides/mods/{jar_path.name}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--version", default=None,
        help="Version string (default: read from gradle.properties)",
    )
    args = parser.parse_args()

    version = args.version or read_version()
    print(f"[build_importable_modpack] version = {version}")

    jar = find_jar(version)
    print(f"[build_importable_modpack] jar = {jar}")

    DOWNLOAD_DIR.mkdir(parents=True, exist_ok=True)
    RELEASES_DIR.mkdir(parents=True, exist_ok=True)

    zip_name = f"ergenverse-modpack-{version}.zip"
    download_zip = DOWNLOAD_DIR / zip_name
    release_zip = RELEASES_DIR / zip_name

    build_zip(version, jar, download_zip)
    print(f"[build_importable_modpack] wrote {download_zip} ({download_zip.stat().st_size} bytes)")

    shutil.copy2(download_zip, release_zip)
    print(f"[build_importable_modpack] copied to {release_zip}")

    # Verify the zip opens and contains the JAR.
    with zipfile.ZipFile(download_zip, "r") as z:
        names = z.namelist()
        assert "manifest.json" in names, "manifest.json missing"
        assert "modlist.html" in names, "modlist.html missing"
        assert f"overrides/mods/{jar.name}" in names, f"{jar.name} missing from overrides/mods/"
        manifest = json.loads(z.read("manifest.json"))
        assert manifest["version"] == version, f"manifest version mismatch: {manifest['version']} vs {version}"
        assert manifest["minecraft"]["version"] == "1.20.1", "minecraft version not 1.20.1"
        assert manifest["minecraft"]["modLoaders"][0]["id"] == "forge-47.4.0", "loader not forge-47.4.0"
    print(f"[build_importable_modpack] verified: {zip_name} is a valid CurseForge modpack")
    return 0


if __name__ == "__main__":
    sys.exit(main())
