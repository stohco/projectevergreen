#!/usr/bin/env python3
"""
CRON-116 part 2: Fix remaining 'adopted son' canon errors for Wang Ping.

The CRON-116 verification script found 5 more files with the 'adopted son'
canon error for Wang Ping. This script fixes them all by replacing
'adopted son' → 'biological son' (and similar phrasings) in the Wang Ping
context.

Files fixed:
  - CanonRelationshipSeeder.java line 40 (Javadoc)
  - CanonRelationshipSeeder.java line 118 (correction comment — rephrase
    to avoid the literal phrase)
  - WangLinHabits.java line 346
  - WangLinTraits.java lines 212, 217, 218
  - WangLinPersonality.java line 276
  - WangLinSpeechPatterns.java line 186

Canon basis: Wang Ping is Wang Lin's BIOLOGICAL son by Mu Bingmei/Liu Mei
(her 9th avatar), conceived in the Suzaku Tomb. NOT adopted. The
animation/donghua 'blood-soul' framing is 魔改 (non-canon for the novel).
Verified via Baidu Baike (https://baike.baidu.com/item/王平/62563845) +
Fandom wiki + newhanfu + Toutiao + 163.
"""
from __future__ import annotations

import sys
from pathlib import Path

FORGE_MOD = Path("/home/z/my-project/forge-mod")

# (file, old_string, new_string) tuples
REPLACEMENTS = [
    # CanonRelationshipSeeder.java line 40 — Javadoc
    (
        "src/main/java/dev/ergenverse/simulation/action/CanonRelationshipSeeder.java",
        " *   <li>Wang Ping &amp; Wang Lin: father-son. Wang Ping is Wang Lin's adopted son.\n"
        " *       Unconditional trust, deep protectiveness.</li>",
        " *   <li>Wang Ping &amp; Wang Lin: father-son. Wang Ping is Wang Lin's biological son\n"
        " *       by Mu Bingmei/Liu Mei (CRON-116 canon correction). Unconditional trust, deep protectiveness.</li>",
    ),
    # CanonRelationshipSeeder.java line 118 — correction comment (rephrase to avoid literal phrase)
    (
        "src/main/java/dev/ergenverse/simulation/action/CanonRelationshipSeeder.java",
        '        // conceived in the 朱雀墓 (Suzaku Tomb) — NOT adopted. The prior\n'
        '        // "adopted son" framing was the animation/donghua 魔改, not novel canon.\n',
        '        // conceived in the 朱雀墓 (Suzaku Tomb) — NOT adopted. The prior\n'
        '        // framing (used in the animation/donghua 魔改, not the novel) was\n'
        '        // corrected in CRON-116 to "biological son".\n',
    ),
    # WangLinHabits.java line 346
    (
        "src/main/java/dev/ergenverse/wanglin/ai/WangLinHabits.java",
        'attested instance is the mortal lifetime raising his adopted son Wang Ping in ',
        'attested instance is the mortal lifetime raising his biological son Wang Ping (rebuilt from sword qi) in ',
    ),
    # WangLinTraits.java line 212
    (
        "src/main/java/dev/ergenverse/wanglin/ai/WangLinTraits.java",
        "     * PATERNAL_LOVE — Wang Lin's love for his children. Wang Ping (mortal\n"
        "     * adopted son, raised intentionally as mortal, E50) and Wang Yiyi",
        "     * PATERNAL_LOVE — Wang Lin's love for his children. Wang Ping (biological\n"
        "     * son by Mu Bingmei/Liu Mei, rebuilt from sword qi, raised as mortal, E50) and Wang Yiyi",
    ),
    # WangLinTraits.java line 217
    (
        "src/main/java/dev/ergenverse/wanglin/ai/WangLinTraits.java",
        '"Paternal Love — For Wang Ping (Adopted Son) and Wang Yiyi (Daughter)",',
        '"Paternal Love — For Wang Ping (Biological Son) and Wang Yiyi (Daughter)",',
    ),
    # WangLinTraits.java line 218
    (
        "src/main/java/dev/ergenverse/wanglin/ai/WangLinTraits.java",
        '"Wang Lin lived a mortal lifetime raising his adopted son Wang Ping in a "\n'
        '                    + "desolate village (E50, Ch. 701). Wang Ping was raised intentionally as a "',
        '"Wang Lin lived a mortal lifetime raising his biological son Wang Ping (rebuilt "\n'
        '                    + "from sword qi after Liu Mei 怨婴 refinement) in a "\n'
        '                    + "desolate village (E50, Ch. 701). Wang Ping was raised intentionally as a "',
    ),
    # WangLinPersonality.java line 276
    (
        "src/main/java/dev/ergenverse/wanglin/ai/WangLinPersonality.java",
        '"Lived a mortal lifetime raising his adopted son Wang Ping in a desolate village.",',
        '"Lived a mortal lifetime raising his biological son Wang Ping (rebuilt from sword qi) in a desolate village.",',
    ),
    # WangLinSpeechPatterns.java line 186
    (
        "src/main/java/dev/ergenverse/wanglin/ai/WangLinSpeechPatterns.java",
        '"With Li Muwan (his wife), Wang Ping (his adopted son), Wang Yiyi (his daughter), "',
        '"With Li Muwan (his wife), Wang Ping (his biological son), Wang Yiyi (his daughter), "',
    ),
]


def main() -> int:
    print("CRON-116 part 2: Fix remaining 'adopted son' canon errors for Wang Ping")
    print(f"Canon: biological son of Wang Lin + Mu Bingmei/Liu Mei (9th avatar)")
    print(f"Verified via Baidu Baike: https://baike.baidu.com/item/王平/62563845")
    print()

    total_replaced = 0
    total_failed = 0

    for rel, old, new in REPLACEMENTS:
        path = FORGE_MOD / rel
        if not path.exists():
            print(f"  MISSING: {rel}")
            total_failed += 1
            continue
        text = path.read_text(encoding="utf-8")
        if old not in text:
            print(f"  FAIL: {rel} — old string not found")
            total_failed += 1
            continue
        new_text = text.replace(old, new, 1)
        if new_text == text:
            print(f"  FAIL: {rel} — replacement produced no change")
            total_failed += 1
            continue
        path.write_text(new_text, encoding="utf-8")
        total_replaced += 1
        print(f"  OK: {rel}")

    print()
    print(f"TOTAL: {total_replaced} replacements, {total_failed} failures")

    # Verify no remaining 'adopted son' near Wang Ping
    print()
    print("Verification — remaining 'adopted son' near Wang Ping:")
    remaining = 0
    java_dir = FORGE_MOD / "src/main/java"
    for path in java_dir.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        lower = text.lower()
        idx = 0
        while True:
            idx = lower.find("adopted son", idx)
            if idx < 0:
                break
            # Check if Wang Ping is nearby (within 500 chars)
            block = text[max(0, idx-500):idx+500]
            if "wang ping" in block.lower() or "wang_ping" in block.lower():
                # Exclude correction comments (mention 魔改 or "non-canon" or "corrected")
                if "魔改" in block or "non-canon" in block.lower() or "corrected in cron" in block.lower():
                    idx += 1
                    continue
                print(f"  REMAINING: {path.relative_to(FORGE_MOD)}")
                remaining += 1
            idx += 1
    if remaining == 0:
        print("  None — all 'adopted son' references for Wang Ping corrected.")
    else:
        print(f"  TOTAL REMAINING: {remaining}")

    return 0 if remaining == 0 and total_failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
