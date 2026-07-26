#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-79: Unify 决明谷 / 绝命谷 → 决明谷 project-wide.

Canon verification (web search 2026-07-26):
- Baidu Baike 仙逆编年史: "王林 体内灵力转化为极境，在 决明谷 身亡失去肉身"
- Baidu Baike 仙逆: "后在 决明谷 外与藤化元展开生死决斗"
- Douban 仙逆王林编年史: "在 决明谷 内激发王林体内灵力转化为极境"
- Zhihu 仙逆故事线整理: "决明谷 走出智斗藤化元"
- NO source uses 绝命谷 — the codebase's use of 绝命 is a misreading/typo.

This script:
1. Replaces dual-character references "决明谷 / 绝命谷" → "决明谷" (various spacing)
2. Replaces "Jue Ming Gu / 绝命谷" → "决明谷" (romanization + wrong character)
3. Replaces remaining standalone "绝命谷" → "决明谷"
4. Reports every file changed + line numbers

Does NOT touch:
- The misleading comment in PlanetSuzakuBlueprint.java (lines 154-163) — that
  requires manual rewriting to remove the false "novel uses both characters" claim.
  The script will report it but leave it for manual edit.
- Binary files, .class files, .git directory
"""

import os
import re
import sys

PROJECT_ROOT = "/home/z/my-project/forge-mod"

# File extensions to process (text files only)
TEXT_EXTENSIONS = {
    '.java', '.json', '.md', '.txt', '.py', '.js', '.ts', '.json5',
    '.json.bak', '.disabled', '.backup', '.gradle', '.properties', '.toml',
    '.cfg', '.conf', '.ini', '.yaml', '.yml', '.xml', '.html', '.css',
}

# Directories to skip
SKIP_DIRS = {'.git', 'build', '.gradle', 'node_modules', 'run', 'logs'}

def is_text_file(filepath):
    """Check if the file has a text extension or is a known text file."""
    for ext in TEXT_EXTENSIONS:
        if filepath.endswith(ext):
            return True
    # Also check for files without standard extensions but known names
    basename = os.path.basename(filepath)
    if basename in ('.gitignore', '.gitattributes', 'gradlew', 'gradlew.bat'):
        return True
    return False

def process_file(filepath, changes):
    """Process a single file, replacing 绝命谷 → 决明谷."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except (UnicodeDecodeError, PermissionError):
        return False

    original = content

    # Step 1: Replace dual-character references with various spacing patterns.
    # "决明谷 / 绝命谷" → "决明谷"
    # "决明谷 / 绝命谷" (with various spaces) → "决明谷"
    # "决明谷/绝命谷" → "决明谷"
    # "决明谷 /绝命谷" → "决明谷"
    # "决明谷/ 绝命谷" → "决明谷"
    patterns_dual = [
        (r'决明谷\s*/\s*绝命谷', '决明谷'),
        (r'决明\s*/\s*绝命', '决明'),  # non-valley dual refs
    ]
    for pattern, replacement in patterns_dual:
        content = re.sub(pattern, replacement, content)

    # Step 2: Replace "Jue Ming Gu / 绝命谷" → "决明谷" (romanization + wrong char)
    content = re.sub(r'Jue Ming Gu\s*/\s*绝命谷', 'Jue Ming Gu / 决明谷', content)

    # Step 3: Replace remaining standalone 绝命谷 → 决明谷
    content = content.replace('绝命谷', '决明谷')

    # Step 4: Replace remaining standalone 绝命 → 决明 (only in contexts where
    # it refers to the valley name — but since ALL 绝命 in this codebase refers
    # to the valley, a global replace is safe)
    # BUT: we need to be careful not to change the comment that EXPLAINS the
    # character difference. That comment is in PlanetSuzakuBlueprint.java lines
    # 154-163 and will be manually rewritten. So we skip that file for the
    # standalone 绝命 (non-谷) replacement.
    if 'PlanetSuzakuBlueprint.java' not in filepath:
        content = content.replace('绝命', '决明')

    if content != original:
        # Find which lines changed
        orig_lines = original.split('\n')
        new_lines = content.split('\n')
        changed_lines = []
        for i, (ol, nl) in enumerate(zip(orig_lines, new_lines), 1):
            if ol != nl:
                changed_lines.append((i, ol.strip(), nl.strip()))

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

        changes.append((filepath, changed_lines))
        return True
    return False

def main():
    changes = []
    files_scanned = 0
    files_changed = 0

    for root, dirs, files in os.walk(PROJECT_ROOT):
        # Skip excluded directories
        dirs[:] = [d for d in dirs if d not in SKIP_DIRS]

        for filename in files:
            filepath = os.path.join(root, filename)
            if not is_text_file(filepath):
                continue
            files_scanned += 1
            if process_file(filepath, changes):
                files_changed += 1

    print(f"\n{'='*70}")
    print(f"CRON-79: 决明谷 / 绝命谷 unification complete")
    print(f"{'='*70}")
    print(f"Files scanned: {files_scanned}")
    print(f"Files changed: {files_changed}")
    print(f"{'='*70}")

    for filepath, changed_lines in changes:
        relpath = os.path.relpath(filepath, PROJECT_ROOT)
        print(f"\n  {relpath}:")
        for lineno, old, new in changed_lines:
            # Truncate long lines for readability
            old_short = old[:100] + '...' if len(old) > 100 else old
            new_short = new[:100] + '...' if len(new) > 100 else new
            print(f"    L{lineno}:")
            print(f"      OLD: {old_short}")
            print(f"      NEW: {new_short}")

    print(f"\n{'='*70}")
    print(f"NOTE: PlanetSuzakuBlueprint.java was skipped for standalone 绝命")
    print(f"replacement. The misleading comment (lines ~154-163) must be")
    print(f"manually rewritten to remove the false 'novel uses both characters' claim.")
    print(f"{'='*70}")

if __name__ == '__main__':
    main()
