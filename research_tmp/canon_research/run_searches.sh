#!/bin/bash
# Run all 25 canon-verification web searches with retry on 429
set -u
cd /home/z/my-project/forge-mod/research_tmp/canon_research

declare -a QUERIES=(
  "Renegade Immortal Wang Lin restriction flag abilities"
  "Renegade Immortal Wang Lin soul refining technique"
  "Renegade Immortal Wang Lin heaven defying bead powers"
  "Renegade Immortal Wang Lin karma whip"
  "Renegade Immortal Wang Lin nine heaven trampling bridges list"
  "Renegade Immortal Wang Lin Situ Nan inheritance ancient god"
  "Renegade Immortal Wang Lin Qing Lin spell"
  "Renegade Immortal Wang Lin Tu Si ancient devil inheritance"
  "Renegade Immortal Wang Lin pets mosquito beast thunder toad list"
  "Renegade Immortal Wang Lin flying swords list"
  "Renegade Immortal Wang Lin puppets immortal guards"
  "Renegade Immortal Wang Lin storage treasures pouches ring"
  "Renegade Immortal Wang Lin essences 14 domains"
  "Renegade Immortal Wang Lin avatars clones true bodies"
  "Renegade Immortal Wang Lin devil armor ancient demon"
  "Renegade Immortal Wang Lin sword sheath five"
  "Renegade Immortal Wang Lin formation flags restriction"
  "Renegade Immortal Wang Lin talismans seals"
  "Renegade Immortal Wang Lin divine abilities celestial spells"
  "Renegade Immortal Wang Lin movement escape arts"
  "Renegade Immortal cultivation realms list Qi Condensation to Heaven Trampling"
  "Renegade Immortal Wang Lin third step fourth step cultivation"
  "Renegade Immortal Wang Lin joss flame karma"
  "Renegade Immortal Wang Lin samsara dao reincarnation"
  "Renegade Immortal ancient god ancient demon ancient devil ancient clan four lineages"
)

i=1
for q in "${QUERIES[@]}"; do
  idx=$(printf "%02d" $i)
  outfile="search_${idx}.json"
  if [ -s "$outfile" ]; then
    # already done (size>0)
    echo "[skip] $idx already done"
    i=$((i+1))
    continue
  fi
  echo "[run] $idx: $q"
  # Build JSON safely using python to escape
  payload=$(python3 -c "import json,sys; print(json.dumps({'query': sys.argv[1], 'num': 5}))" "$q")
  z-ai function -n web_search -a "$payload" 2>/dev/null > "$outfile"
  rc=$?
  if [ $rc -ne 0 ] || [ ! -s "$outfile" ]; then
    echo "  -> retry in 10s"
    sleep 10
    z-ai function -n web_search -a "$payload" 2>/dev/null > "$outfile"
    rc=$?
    if [ $rc -ne 0 ] || [ ! -s "$outfile" ]; then
      echo "  -> FAILED after retry"
      echo "FAILED: $q" > "$outfile"
    fi
  fi
  # Check for 429 error content
  if grep -q "Too many requests\|429\|error" "$outfile" 2>/dev/null; then
    echo "  -> 429 detected, sleeping 15s"
    sleep 15
  else
    sleep 2  # be nice
  fi
  i=$((i+1))
done
echo "ALL DONE"
