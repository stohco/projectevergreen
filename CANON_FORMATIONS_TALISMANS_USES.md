# Formations and Talismans — Complete Canon Usage Catalog

**Source:** Synthesizes prior EXHAUSTIVE-FORMATIONS-TALISMANS worklog (176 formations + 135 talismans originally documented), the 12 search files in `/home/z/my-project/tool-results/research_formation_talisman/`, `wl_items.json` / `wl_techniques.json` (Xian Ni fandom Wang Lin/Items + Wang Lin/Techniques dumps), `src/lib/sim/formation-talisman-catalog.ts` (existing 100-entry catalog), `src/lib/sim/protagonist-arsenals.ts` (1118-line canonical arsenal), and `forge-mod/src/main/java/dev/ergenverse/formation/Formation.java` (Forge implementation scaffolding).

**Novel codes:** RI = Renegade Immortal (Wang Lin) · Ptt = Pursuit of the Truth (Su Ming) · ISSTH = I Shall Seal the Heavens (Meng Hao) · AWE = A Will Eternal (Bai Xiaochun) · AWWP = A World Worth Protecting (Wang Baole) · BTT = Beyond the Timescape (Xu Qing) · cross = universal archetype.

**Grade ladder:** mortal → spirit → earth → heaven → dao → immortal (with `transcendence` for late-ISSTH/EOS tier).

**Activation key:**
- **flag** = planted formation-flag blocks (multi-block pattern)
- **core** = heart-array core block (single anchored block that drives a multi-flag pattern)
- **instant** = cultivator casts by hand (no physical anchor — pure technique)
- **blood** = refined via blood (life-bound; activates with blood-essence intent)
- **soul** = soul-branded (binds to owner's origin soul; only they can use it)
- **spirit-vein** = powered by an external spirit vein (perennial source)
- **right-click** = item right-click activation (Forge semantics)
- **crush** = consumable destroy-on-activate (paper talisman)
- **refine** = long ritual refinement (multi-day/month forging)

**Confidence:** C5=explicit wiki entry · C4=directly named in novel + wiki · C3=novel-implicit · C2=archetypal fan-synthesis.

---

## Part 1: Formations

### Summary
- **Total formations catalogued: 176**
- By novel: RI = 41 · ISSTH = 28 · AWE = 27 · Ptt = 22 · AWWP = 29 · BTT = 29
- By category (the 11 requested bins — a single formation may fall into several categories; primary bin listed):
  - Defensive (sect/personal shields, perimeter wards): **~30**
  - Offensive (attack/killing arrays): **~28**
  - Restriction/Sealing (Wang Lin's specialty; seal regions/objects/beings): **~35**
  - Transport (teleport arrays, spatial gates, void travel): **~12**
  - Surveillance (scrying, monitoring, life-tracking): **~6**
  - Illusion (mirage, disguise, false-appearance): **~7**
  - Soul (soul refining, summoning, sealing): **~13**
  - Alchemy Auxiliary (pill-boosting arrays): **~5**
  - Spirit Gathering (Qi gathering, spirit-vein tapping): **~3** (often co-located with defensive)
  - Time/Space (time dilation, spatial locking, planetary seal): **~12**
  - Hybrid/Multi-purpose (Wang Lin's Restriction Flag — seals+stores+attacks): **~25**
- Cross-novel universals: every sect has a protecting array; every realm has a soul-lamp system; every protagonist eventually forges a personal domain that IS a formation.

---

### 1. Defensive Formations

#### Heng Yue Sect Protecting Array (恒岳宗护宗阵)
- **Novel:** RI
- **User:** Heng Yue Sect
- **Grade:** spirit
- **Activation:** flag (perimeter) + core (heart-array) + spirit-vein (power source)
- **Effect:** Layered outer perimeter → spirit-gathering → heart-array core flag matrix. Wang Lin's first sect.
- **Breaking:** Requires Core Formation+ cultivator to brute-force, or a Restriction-Breaking Ancient Mirror to bypass. Mortals can break physical flags but the spiritual anchor persists.
- **Portable?** No — place-based; bound to the sect's spirit vein.
- **Source:** RI wiki / formation-talisman-catalog.ts F36

#### Sect-Protecting Array (universal archetype) (护宗阵)
- **Novel:** cross (RI Heng Yue, ISSTH Reliance, AWE Spirit Stream, Ptt Seven Moons, AWWP Federation Dao Academy, BTT Seven Blood Eyes)
- **User:** universal sect structure
- **Grade:** spirit (default) — scales to immortal for top-tier sects (Heavenspan, Fang Clan Immortal World)
- **Activation:** always flag + core + spirit-vein
- **Effect:** Perimeter shield; deflects hostile cultivators, beast tides, and tribulation fallout. Cultivator disciples inside the array gain passive Qi bonus.
- **Breaking:** Overwhelming the array's qi reserve (multiple Nascent Soul+ attackers); or killing disciples (each disciple's life-lamp weakening the array); or subduing the array's spirit (high-tier arrays gain sentience).
- **Portable?** No.
- **Source:** cross — every novel confirms

#### Spirit Stream Sect Protection Formation (灵溪宗护宗阵)
- **Novel:** AWE
- **User:** Spirit Stream Sect
- **Grade:** immortal
- **Activation:** sect flag-set + spirit-stream essence vein
- **Effect:** Post-merger defensive layered array; one of 4 stream-sect arrays combined after River-Adjudicating Sect formation.
- **Portable?** No.
- **Source:** AWE fandom / F72

#### Blood Stream Sect Blood-Formation (血溪宗血阵)
- **Novel:** AWE
- **User:** Blood Stream Sect
- **Grade:** immortal
- **Activation:** blood-essence + sect-flags; powered by sect bloodline patriarch's lifeblood
- **Effect:** Blood-themed offensive+defensive hybrid. Hardens under bloodshed.
- **Portable?** No.
- **Source:** AWE / F73

#### Profound/Pill/River-Adjudicating Sect Formations (玄溪/丹溪/评脉宗阵)
- **Novel:** AWE
- **User:** Profound Stream / Pill Stream / merged River-Adjudicating sect
- **Grade:** immortal (merged array)
- **Effect:** Each of the 4 stream-sect arrays; the River-Adjudicating merges all four post-reorganization.
- **Source:** AWE / F74–F76

#### Heavenspan Realm Sect-Formation (天渊阵)
- **Novel:** AWE
- **User:** Heavenspan Realm
- **Grade:** immortal→heaven
- **Effect:** Realm-scale protective formation around the Heavenspan river-world.
- **Source:** AWE / F77

#### Arch Emperor / Sacred Emperor Dynasty Formations (大乾皇朝阵 / 神圣皇朝阵)
- **Novel:** AWE
- **Grade:** immortal
- **Activation:** dynasty-runes + imperial flags + dragon-vein (imperial mausoleum)
- **Effect:** Dynasty-protecting arrays that scale with the ruling emperor's Mandate.
- **Source:** AWE / F79–F80

#### Mountain and River Screen (山河屏)
- **Novel:** RI
- **User:** Wang Lin (stolen from Greed)
- **Grade:** immortal
- **Activation:** instant (treasure activation; right-click)
- **Effect:** Defensive landscape-screen reshapes local terrain as a shield. Wang Lin stole it from Greed during the Moongazer Serpent escape.
- **Portable?** Yes — persistent item.
- **Source:** RI ch.717 / wl_items.json

#### Body Formation (体阵)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** instant (Avatar spell; ch.1125) — cultivator's own body becomes the array
- **Effect:** Creates a 10-day avatar made of rock + origin-soul fragment; self-repairing defensive array.
- **Portable?** Yes — self-contained (the cultivator IS the array).
- **Source:** RI ch.1125 Body Formation celestial spell / wl_techniques.json

#### Five Elements True Body (五行真身)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** instant (Dao-fusion)
- **Effect:** Body-formation fusing the 5 elements into one true body.
- **Source:** RI / F34

#### Nine Heavens Treasured Body Seals (九天宝体印)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Grade:** immortal
- **Activation:** instant (9 body-seals stacked)
- **Effect:** Supreme-defense body-formation.
- **Source:** ISSTH / F49

#### Body Formation — 999/1000 Blood-Veins (蛮血脉阵)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** heaven
- **Activation:** internal body cultivation
- **Effect:** Body-formation using 999→1000 blood-veins as internal array-points.
- **Source:** Ptt / F108

#### Undying Live Forever Codex Complete Array (不死长生诀全阵)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** dao
- **Activation:** endgame fusion of Undying Codex (5 volumes: skin/king/tendons/bones/blood) + Live Forever Codex (5 volumes)
- **Effect:** Effectively unkillable body-formation array.
- **Source:** AWE / F96

#### Stellar Nascent Soul Formation (星魂阵)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** heaven
- **Effect:** Body-formation that fuses starlight into the nascent soul.
- **Source:** AWWP / F147

#### Three Bells Shield (三铃盾)
- **Novel:** RI
- **User:** Wang Lin (later gifted to Ling'er)
- **Grade:** spirit
- **Activation:** instant (treasure right-click)
- **Effect:** Defensive three-bell shield array; later gifted.
- **Portable?** Yes.
- **Source:** RI ch.965 / wl_items.json

#### Heaven Tiger Flag (天虎旗)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** planted flag (instant defensive zone)
- **Effect:** Sect-protecting-class defensive flag; activates a defensive domain when planted.
- **Portable?** Yes (item, plant to activate).
- **Source:** RI ch.879 / wl_items.json

#### Three Purple Flags (三紫旗)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** 3-flag set; plant in triangular pattern
- **Effect:** Layered 3-flag defensive array.
- **Portable?** Yes.
- **Source:** RI / wl_items.json

#### Heavenhorn Sword 10-fold Enhancement Formation (天角剑十叠阵)
- **Novel:** AWE
- **User:** Bai Xiaochun (and Heavenhorn Sword lineage)
- **Grade:** heaven
- **Activation:** multi-color flame ritual; 1 additional color per enhancement tier
- **Effect:** Spirit-enhancement layered array; 10-fold boosts the Heavenhorn Sword.
- **Source:** AWE / F82

#### Heavenhorn Ink Dragon Formation (天角墨龙阵)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** heaven
- **Effect:** Ink-dragon-themed array bound to Heavenhorn Sword lineage.
- **Source:** AWE / F83

#### Living Mountain Incantation (活山咒)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** immortal
- **Effect:** Mountain-as-formation; living mountain defense.
- **Source:** AWE / F85

#### Ancient Pagoda Formation (古塔阵)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** immortal
- **Effect:** Pagoda-tier defensive array (imprisoned a demigod beast soul).
- **Source:** AWE / F89

#### Tiny Turtle Shell Formation (小龟壳阵)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** spirit
- **Effect:** Small but sturdy defensive shell-array.
- **Source:** AWE / F90

#### Heaven-Shaking Divine Justice Shield (震天神盾)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** heaven
- **Effect:** Signature shield-formation.
- **Source:** AWWP / F126

#### The Bell (钟)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** heaven
- **Effect:** Defensive + sonic offense bell-formation.
- **Source:** AWWP / F127

#### Eternal Fortress (永恒堡垒)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** heaven
- **Effect:** Mobile defensive fortress-formation.
- **Source:** AWWP / F123

#### Golden Bell Shield (金钟盾)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** spirit
- **Effect:** Golden-bell defensive shield-formation.
- **Source:** AWWP / F138

#### Han Mountain Bell (寒山钟)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** spirit
- **Activation:** bell-ring (instant)
- **Effect:** Berserker Treasure; rings to suppress and defend.
- **Source:** Ptt / F103

#### Dao Avenue Mountain (道途山)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** heaven
- **Activation:** divine-vessel activation (instant)
- **Effect:** A mountain that IS a divine vessel and formation; transports and defends.
- **Source:** Ptt / F101

#### Sacred Constellation Robe Formation (圣星袍阵)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** immortal
- **Effect:** Constellation-themed robe-array; defensive starlight cloak.
- **Source:** Ptt / F105

#### Federation / Mars Dao Academy Formations (联邦/火星道院阵)
- **Novel:** AWWP
- **Grade:** spirit
- **Effect:** Modern-era academy-protecting arrays.
- **Source:** AWWP / F140–F141

#### Mystic Eye / Withered Flame / Righteous Saint Sect Formations
- **Novel:** AWWP
- **Grade:** immortal
- **Effect:** Various sect-protecting arrays.
- **Source:** AWWP / F142–F144

#### Dharmic Armament Pavilion Formation (法器阁阵)
- **Novel:** AWWP
- **Grade:** immortal
- **Effect:** School + forge + defense hybrid array.
- **Source:** AWWP / F145

#### Seven Blood Eyes Sect-Protection Formation (七血瞳护宗阵)
- **Novel:** BTT
- **User:** Seven Blood Eyes sect
- **Grade:** immortal
- **Effect:** Sect-protecting array of the 7-peak Seven Blood Eyes sect.
- **Source:** BTT / F157

#### Seven Blood Eyes 7-Peak Formation (七峰阵)
- **Novel:** BTT
- **User:** Seven Blood Eyes sect
- **Grade:** immortal
- **Activation:** 7 peak-flags coordinated
- **Effect:** Each peak a different specialty (sword/alchemy/magic/beast/formation/forge/port); coordinated 7-peak array.
- **Source:** BTT / F159

#### Violet Moon Palace / Golden Crow Palace Formations (紫月宫阵 / 金乌宫阵)
- **Novel:** BTT
- **User:** Xu Qing (Heavenly Palaces 4 + 6)
- **Grade:** heaven
- **Effect:** Palace arrays among Xu Qing's 12-13 Heavenly Palaces.
- **Source:** BTT / F168–F169

#### Brilliant Heaven 9th Star Ring Formation (辉煌天九星环阵)
- **Novel:** BTT
- **User:** Brilliant Heaven 9th Star Ring
- **Grade:** dao
- **Effect:** Star-ring-scale protective formation.
- **Source:** BTT / F164

#### Realm Mending Plate Formation (补天盘阵)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** dao
- **Activation:** plate-rune + Netherworld River power
- **Effect:** Repairs realm-level damage; cosmic-scale mending array.
- **Source:** AWWP / F135

#### Reincarnation Arc Patriarch Formation (轮回篇宗主阵)
- **Novel:** AWE
- **User:** Bai Xiaochun (patriarch arc)
- **Grade:** immortal
- **Effect:** Bai Xiaochun's patriarch-tier sect array in the Reincarnation Arc.
- **Source:** AWE / F95

---

### 2. Offensive Formations

#### Annihilation Restriction (寂灭禁制)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** dao (immortal-grade Annihilation essence; ranks as one of the **4 Great Restrictions**)
- **Activation:** instant (restriction essence wielded via Restriction Flag) — Wang Lin inherited half of restrictions-heart from Li Yuan (ch.754)
- **Effect:** Annihilates whatever is sealed — body, soul, or dao. Source of the 18 Plum Restriction derived formation.
- **Breaking:** Cannot be broken by cultivators below Transcendence.
- **Portable?** Yes — embedded in Wang Lin's Restriction Flag.
- **Source:** RI ch.754 / wl_techniques.json (explicit)

#### Destruction Restriction (毁灭禁制)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** dao
- **Activation:** instant
- **Effect:** 4th of the 4 Great Restrictions. Total destruction seal.
- **Portable?** Yes.
- **Source:** RI / F09

#### Seven-Star Sword Formation (七星剑阵)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** 7 flying swords + star-rune flags; formed from 7 of Ling Tianhou's 12 disciples' swords
- **Effect:** 7-sword constellation array; strikes with star-light sword-qi. Wang Lin destroyed the array in ch.715.
- **Portable?** Yes (sword-set item).
- **Source:** RI ch.715 / wl_items.json

#### God-Tremble Army Formation (神颤军阵)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** army-coordinated flags (Ancient Order spell ch.1476)
- **Effect:** Coordinated troop-formation; suppresses enemy armies with fear-tremble aura. Part of the three-spell sequence: God Tremble → Demon Spell (Fiery Wind Turns Into a Mountain) → Devil Dao (Life and Death Reversal).
- **Source:** RI ch.1476 / wl_techniques.json

#### Unnamed Wheel Formation (无名轮阵)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** dao
- **Activation:** permanent ritual (uses souls of dead cultivators, destroyed treasure-spirits, and planet-souls)
- **Effect:** Wang Lin's signature endgame replacement for the Realm-Sealing Formation. Calls on (1) treasure spirits of all destroyed treasures in 100 years of war; (2) souls of destroyed cultivation planets; (3) soul-fragments of dead Outer-Realm cultivators; (4) resting souls of dead Inner-Realm cultivators. Functions as a seal blocking all Outer-Realm entry while still admitting Joss Flames (fixing the Realm-Sealing Formation's flaw).
- **Portable?** No — realm-scale.
- **Source:** RI ch.1667 / wl_techniques.json (explicit Wang Lin's 7th Original Spell)

#### Heaven Devouring Demon Formation ( Heaven Devouring Demon Formation )
- **Novel:** RI
- **User:** Wang Lin (Devouring Technique, ch.139)
- **Grade:** spirit
- **Activation:** corpses of cultivators (must be killed by the user) as the formation base
- **Effect:** Trap-and-kill array. 10 Foundation corpses trap a Core Formation cultivator; 5 Core Formation corpses kill any mid-stage-or-lower Core Formation cultivator.
- **Portable?** Yes — the corpses are mobile components.
- **Source:** RI ch.139 / wl_techniques.json

#### Soul Refining Sect Blood-Sacrifice Array (炼魂宗血祭阵)
- **Novel:** RI
- **User:** Soul Refining Sect patriarchs
- **Grade:** immortal
- **Activation:** outer-disciple blood + soul-refining flags; perpetual ritual
- **Effect:** Sect-growth method; refines outer-sect disciples' souls into banner fuel. Powers the Soul Refining Sect's signature soul banners.
- **Portable?** No — sect-bound.
- **Source:** RI / F37

#### Nine Deaths Perish Formation (九死灭阵)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** instant (derived from Annihilation Restriction)
- **Effect:** 9-death killing array.
- **Source:** RI ch.829 / wl_techniques.json

#### 18 Plum Restriction (十八梅禁制)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** instant — transforms into 18 statues
- **Effect:** Derived from Annihilation Restriction; summons 18 restriction-statues.
- **Source:** RI ch.752 / wl_techniques.json

#### Heart-Pounding Thunder (心震雷)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** instant — fused with fire+thunder essence
- **Effect:** Offensive thunder formation comprehended in the last layer of Ancient Graveyard.
- **Source:** RI ch.1777 / wl_techniques.json

#### God Decapitation Altar (斩神坛)
- **Novel:** BTT
- **User:** Xu Qing (originally Li Zihua's trump card)
- **Grade:** immortal→heaven
- **Activation:** altar-ritual; later integrated as Xu Qing's 10th Heavenly Palace
- **Effect:** Decapitates gods. Xu Qing's first major divine ability.
- **Portable?** Yes — integrates as Heavenly Palace (internal formation).
- **Source:** BTT / F150

#### Five Elements God Decapitation Altar (五行斩神坛)
- **Novel:** BTT
- **User:** Xu Qing (11th Heavenly Palace)
- **Grade:** heaven (emperor-tier)
- **Effect:** 5-elements variant.
- **Source:** BTT / F151 / F174

#### Seven Elements God Decapitation Altar (七行斩神坛)
- **Novel:** BTT
- **User:** Xu Qing (12th Heavenly Palace)
- **Grade:** heaven→dao (grand-emperor-tier)
- **Effect:** 7-elements variant.
- **Source:** BTT / F152 / F175

#### Pluck Stars from the Vault of the Universe (摘星穹宇)
- **Novel:** BTT
- **User:** Xu Qing (self-created)
- **Grade:** dao (lord-tier)
- **Activation:** instant
- **Effect:** Self-created lord-tier formation; evolved from "Fishing the Moon in the Well"; plucks stars from cosmic vault.
- **Source:** BTT / F155

#### Primordial Gruish Sacrificial Altar (原始诡祭坛)
- **Novel:** BTT
- **User:** Xu Qing
- **Grade:** dao (lord+)
- **Effect:** Lord+ sacrificial altar-formation.
- **Source:** BTT / F156

#### 49 Taboo Formations (四十九禁忌阵)
- **Novel:** BTT
- **User:** Xu Qing
- **Grade:** heaven (emperor-tier)
- **Activation:** 49 taboo-runes
- **Effect:** Xu Qing's signature emperor-tier taboo array. Inherited from Li Zihua's lineage.
- **Portable?** Yes — internal formation (Heavenly Palace essence).
- **Source:** BTT / F149

#### Heavenly Palace 7 — Blood Mountain (第七天宫·血山)
- **Novel:** BTT
- **User:** Xu Qing
- **Grade:** heaven
- **Effect:** 7th Heavenly Palace; blood-mountain array.
- **Source:** BTT / F170

#### Heavenly Palace 8 — Bone Pile (第八天宫·骨堆)
- **Novel:** BTT
- **User:** Xu Qing
- **Grade:** heaven
- **Effect:** 8th Heavenly Palace; bone-pile array.
- **Source:** BTT / F171

#### Heavenly Palace 9 — Antemage Skulls (第九天宫·前置法相颅骨)
- **Novel:** BTT
- **User:** Xu Qing
- **Grade:** heaven
- **Effect:** 9th Heavenly Palace; antemage-skull array.
- **Source:** BTT / F172

#### Lightning Flag (雷旗)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Grade:** spirit→immortal
- **Activation:** lightning-essence + flag-cloth; instant
- **Effect:** Lightning-themed formation-flag; sheet of lightning coalesced into a flag (Comicvine Meng Hao respect thread confirms: "A sheet of lightning appeared which coalesced into a Lightning Flag").
- **Portable?** Yes — item.
- **Source:** ISSTH / F44 (Comicvine Meng Hao respect thread)

#### Flying Sword Matrix Rain Dragon (飞剑阵雨龙)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Grade:** spirit
- **Activation:** sword-flags arranged as rain-dragon
- **Effect:** Early sword-array; sword-rain dragon formation.
- **Source:** ISSTH / F47

#### Solitary Sword Sect Sword-Formation (孤剑宗剑阵)
- **Novel:** ISSTH
- **User:** Solitary Sword Sect
- **Grade:** immortal
- **Effect:** Coordinated sword-flags unleash a single overwhelming slash.
- **Source:** ISSTH / F55

#### Golden Frost Sect Ice-Formation (金霜宗冰阵)
- **Novel:** ISSTH
- **User:** Golden Frost Sect
- **Grade:** immortal
- **Effect:** Ice-themed sect array (defensive+offensive).
- **Source:** ISSTH / F57

#### Blood Demon Grand Magic (血魔大法) — 3 strata × 2 levels
- **Novel:** ISSTH
- **User:** Meng Hao / Blood Demon Sect
- **Grade:** immortal→heaven
- **Activation:** blood-qi → spirit-meridian → soul-strata (3-tier progression)
- **Effect:** Blood Demon Sect's signature array-magic; absorbs blood-qi, then spirit-meridians, then souls.
- **Source:** ISSTH / F45

#### Killing Sword Formation (杀戮剑阵)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** immortal
- **Activation:** killing-essence + sword-runes
- **Effect:** Immortal-tier killing array bound to the Killing Sword.
- **Source:** Ptt / F97

#### Immortal Slaying Gourd Formation (斩仙葫芦阵)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** immortal→heaven
- **Activation:** gourd-talisman + immortal-slaying runes
- **Effect:** Fires immortal-slaying beams.
- **Source:** Ptt / F98

#### Origin Lightning Cauldron 9-Holes Formation (起源雷鼎九孔阵)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** heaven
- **Activation:** 9-hole cauldron + origin lightning
- **Effect:** Origin-berserker treasure; channels origin lightning for offense+defense.
- **Source:** Ptt / F100

#### Nine Striking Lands Formation (九击地阵)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** heaven
- **Activation:** 9 striking-land flags
- **Effect:** 9-strike array; each strike a different land-based attack.
- **Source:** Ptt / F117

#### Su Ming's Four Seasons Shadows Formation (四季影阵)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** immortal
- **Effect:** Shadow-of-winter / shadow-of-autumn / shadow-of-summer / shadow-of-spring — 4-shadow array.
- **Source:** Ptt / F118

#### Berserker God's Three Barren Arts (蛮神三荒术)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** heaven
- **Activation:** 3 barrenness essences (heaven/earth/man)
- **Effect:** 3-tier barrenness array; strips heaven, earth, and man essence from target.
- **Source:** Ptt / F114

#### Saint Defier Three Forbidden Arts (圣逆三禁术)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** dao
- **Effect:** 3 forbidden sealing/destruction arrays: Foul Flesh Hell / Netherworld Burns Bones / Transmigration Soul.
- **Source:** Ptt / F113

#### Baole Cannon (宝乐炮)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** spirit
- **Activation:** self-forged cannon + lightning-fire essence
- **Effect:** Signature offensive cannon-formation.
- **Source:** AWWP / F122

#### Twelve Emperors Puppets (十二帝王傀儡)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** heaven
- **Activation:** 12 emperor-tier puppets + control-runes
- **Effect:** 12-puppet coordinated strike-formation.
- **Source:** AWWP / F124

#### Dharmic Battleship Locust (法器战舰蝗)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** heaven
- **Effect:** Battleship-formation that deploys like a swarm.
- **Source:** AWWP / F125

#### Fire Tiger Megaphone / Megaphone v2 (火虎扩音器 / 扩音器二式)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** spirit
- **Effect:** Sound-amplified fire attack-formation (signature Wang Baole gag weapon).
- **Source:** AWWP / F133, F137

#### Paper Human Cutout Galaxy Bow (纸人剪影银河弓)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** heaven
- **Activation:** paper-human + galaxy-bow talisman
- **Effect:** Signature paper-formation weapon.
- **Source:** AWWP / T105

#### Lingxi Abyss Beast-Tide Array (灵溪深渊兽潮阵)
- **Novel:** AWE
- **User:** Lingxi Abyss Beast King
- **Grade:** immortal
- **Effect:** Triggers and directs beast tides; defensive mechanism of the abyss.
- **Source:** AWE / F93

#### Wildlands Ghost-King Formation (荒地鬼王阵)
- **Novel:** AWE
- **User:** Giant Ghost King / Great Chu
- **Grade:** immortal
- **Effect:** Ghost-themed soul-array; controls wildlands undead.
- **Source:** AWE / F94

---

### 3. Restriction / Sealing Formations (Wang Lin's specialty)

#### Restriction Flag (禁幡) — Wang Lin's flagship hybrid artifact
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** instant (Restriction Flag Method) — but the flag itself is **refined over years** using 99,999 restrictions + 3 ink-stones (ch.S1/EP32 + ch.879)
- **Effect:** Flagship formation-flag artifact. Embeds restriction matrices, seals regions, suppresses enemies, stores pocket army. Doubles as storage + portable restriction matrix. **Three variants Wang Lin forged:** (1) 1st incomplete — left incomplete so he could summon divine tribulation in danger; (2) 2nd mixed-restrictions; (3) 3rd pure-attack restriction flag.
- **Breaking:** Cannot be broken by cultivators below Soul Formation.
- **Portable?** Yes — item (the canonical "item that is also a formation").
- **Source:** RI ch.879 + 178 / wl_items.json + wl_techniques.json (verified)

#### Restriction Flag Method / Restriction Flags Refining Method (禁旗之法)
- **Novel:** RI
- **User:** Wang Lin (4th person in history to comprehend the Land of the Ancient God Restrictions Mountain trial; taught by Tu Si)
- **Grade:** spirit (technique-grant)
- **Activation:** refine (multi-year) — uses 3 ink-stones + 99,999 embedded restrictions
- **Effect:** The cultivation method to wield Restriction Flags. Foundation for ALL of Wang Lin's restriction work.
- **Source:** RI S1/EP31-32 / wl_techniques.json

#### Ancient Restrictions (古禁制)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal→dao
- **Activation:** instant (Ancient Order spell ch.619; combined with Heavenly Devil Sound)
- **Effect:** General mastery of prehistoric restriction matrices; can decode and re-purpose ancient arrays.
- **Source:** RI ch.619 / wl_techniques.json

#### Heart Restriction (心禁)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** instant (ch.858)
- **Effect:** Seals the heart-mind of targets. Prevents betrayal and silences enemies from speaking of secrets. Combined with Eyes Suppressing the World.
- **Source:** RI ch.858 / wl_techniques.json

#### Time Restriction (岁月禁制 / 时禁)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** dao (2nd of the 4 Great Restrictions)
- **Activation:** instant (ch.1223)
- **Effect:** Freezes/seals targets in time.
- **Source:** RI ch.1223 / wl_techniques.json

#### Life-Death Restriction (生死禁制)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** dao (3rd of the 4 Great Restrictions)
- **Activation:** instant (ch.1229)
- **Effect:** Controls life and death of sealed target.
- **Source:** RI ch.1229 / wl_techniques.json

#### Ancient Soul Restriction (古魂禁制)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** heaven
- **Activation:** instant (ch.1697)
- **Effect:** Seals ancient-tier souls. Used on Immortal Guards and ancient-tomb spirits.
- **Source:** RI ch.1697 / wl_techniques.json

#### Blood Lines Rules Restriction (血脉规则禁制)
- **Novel:** RI
- **User:** Wang Lin (ch.1715)
- **Grade:** immortal
- **Activation:** instant (Restriction Essence)
- **Effect:** Restricts bloodline laws; can seal a cultivator's inherited techniques.
- **Source:** RI ch.1715 / wl_techniques.json

#### Three Ink Stones Restriction (三墨石禁制)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** spirit
- **Activation:** 3 ink-stone discs
- **Effect:** Trapping seal formed by three ink-stone discs. The inkstones are also the raw material for refining Restriction Flags.
- **Source:** RI / F12 / wl_items.json

#### Heart Compass of Annihilation (寂灭心罗盘)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** instant (treasure activation)
- **Effect:** Heart-targeting annihilation compass-seal.
- **Portable?** Yes — item.
- **Source:** RI ch.858 / wl_items.json (Heart Compass Annihilation Restriction Inheritance Treasure)

#### Ancient Soul Restriction Tortoise Beast (古魂禁制龟兽)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal
- **Activation:** instant (treasure)
- **Effect:** Living restriction that traps and refines souls in a tortoise-form array. Gifted by old Vermillion bird in the Fallen Land (ch.1426).
- **Portable?** Yes — item.
- **Source:** RI ch.1426 / wl_items.json

#### Isolation Restriction Compass (隔离禁制罗盘)
- **Novel:** RI
- **User:** Wang Lin (taken from Green Devil Continent woman at pill sea, ch.1850)
- **Grade:** immortal
- **Activation:** instant (compass-disc)
- **Effect:** Isolates a region from divine-sense scrying and teleport-escape. Contains the Devil Restriction Sect's Devil Isolation Restriction. Wang Lin later discarded it (ch.1864) because someone was using it to pin his location.
- **Portable?** Yes — item.
- **Source:** RI ch.1850 / wl_items.json

#### Soul Devil Ship (魂魔船) — made of the 4 Great Restrictions + many more
- **Novel:** RI
- **User:** Wang Lin (ch.1789)
- **Grade:** immortal
- **Activation:** assembled ship + main sail (Ghostly Sail, ch.1699)
- **Effect:** A ship MADE UP of the 4 Great Restrictions and many other restrictions. The main sail (Ghostly Sail) contains all of the Soul Devil Ship's restriction matrices. Used by Fan Shanmeng to cast a multi-layered illusion spell.
- **Portable?** Yes — mobile vehicle-formation.
- **Source:** RI ch.1699+1789 / wl_items.json

#### Six Cultivation Planets Restriction (六星禁制)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** dao
- **Activation:** 6 cultivation-planet anchors + restriction threads; triggered by Wang Lin's gesture
- **Effect:** Seals 6 planets at once. Collapsed one planet on trigger. Confirmed by SpaceBattles RI Feat Thread: "With this, the restrictions Wang Lin had left on the six cultivation planets erupted and one of the planets collapsed."
- **Portable?** No — planetary-scale.
- **Source:** RI SpaceBattles Feat Thread / search_02

#### Heart Restriction — Seal Sub-Soul
- **Novel:** RI
- **User:** Wang Lin (ch.179)
- **Grade:** immortal
- **Effect:** Heart of Slaughter variant — seals the heart against killing aura.
- **Source:** RI ch.179 + 574 + 858 / wl_techniques.json

#### Blood Ancestor's Blood Body (血祖血身)
- **Novel:** RI
- **User:** Wang Lin (ch.769, exploded ch.789)
- **Grade:** immortal
- **Effect:** Sealing-class blood-aspect formation.
- **Source:** RI ch.769 / wl_items.json

#### Fragment Stamp Celestial Sealing (仙封残印) → Celestial Sealing Stamp
- **Novel:** RI
- **User:** Wang Lin (ch.769, refined into Celestial Sealing Stamp)
- **Grade:** immortal
- **Activation:** refined during Illusory Yin breakthrough via Divine Tribulation descent
- **Effect:** Fragment-stamp that seals celestial energy; precursor to the 18 Hell Celestial Sealing Stamp.
- **Source:** RI ch.769+915 / wl_items.json

#### Fate Sealing Ring (封命环)
- **Novel:** RI
- **User:** Wang Lin (ch.1631)
- **Grade:** immortal
- **Effect:** Sealed within divine retribution.
- **Source:** RI ch.1631 / wl_items.json

#### Earth Palace (地宫)
- **Novel:** RI
- **User:** Wang Lin (ch.1478; one of Ye Mo's three palaces: heaven/earth/human)
- **Grade:** immortal
- **Effect:** Inheritance-locked sealing palace.
- **Source:** RI ch.1478 / wl_items.json

#### Realm-Sealing Formation (封界阵)
- **Novel:** RI
- **User:** Heaven-Realm cultivators (Wang Lin destroyed it; Heaven Splitting Axe was its formation spirit)
- **Grade:** dao
- **Effect:** Sealed the Inner Realm from the Outer Realm; flaw = blocked Joss Flames. Wang Lin's Unnamed Wheel Formation replaced it.
- **Source:** RI ch.1664+ / wl_items.json + wl_techniques.json

#### Ancient Leaf Sealing Formation (古叶封禁)
- **Novel:** RI
- **User:** Wang Lin (ch.1387-1460); used dozens to seal the ancient door passage to the Immortal Astral Continent
- **Grade:** immortal
- **Activation:** instant — leaf releases a powerful sealing force on use
- **Effect:** 99 leaves predicted by Grandmaster Yun Luo; dozens used to seal the door passage.
- **Portable?** Yes — consumable leaves.
- **Source:** RI ch.1387 / wl_items.json

#### 49 Taboo Formations (四十九禁忌阵) — Xu Qing's restriction inheritance
- **Novel:** BTT
- **User:** Xu Qing
- **Grade:** heaven (emperor-tier)
- **Effect:** 49 taboo-rune sealing matrix. Successor to Wang Lin's restriction lineage.
- **Source:** BTT / F149

#### Space Grid (空间格)
- **Novel:** BTT
- **User:** Xu Qing (13th Heavenly Palace)
- **Grade:** heaven (emperor-tier)
- **Effect:** Emperor-tier spatial grid array; seals and isolates space.
- **Source:** BTT / F153, F176

#### Infinite Timelessness (无极光阴)
- **Novel:** BTT
- **User:** Xu Qing
- **Grade:** dao (grand-emperor+)
- **Effect:** Grand-emperor+ time-seal formation.
- **Source:** BTT / F154

#### D-132 Prison Cell Formation (D-132囚室阵)
- **Novel:** BTT
- **User:** Xu Qing (became part of God Decapitation Altar)
- **Grade:** heaven
- **Effect:** Prison-cell sealing formation.
- **Source:** BTT / F166

#### Forbidden Sea Restriction Array (禁海禁制阵)
- **Novel:** BTT
- **User:** Forbidden Sea powers
- **Grade:** heaven
- **Effect:** Sea-wide restriction array; the Forbidden Sea is itself a trapped zone.
- **Source:** BTT / F158

#### Ninedawns Forbidden Lands Formation (九黎禁地阵)
- **Novel:** BTT
- **User:** Ninedawns region
- **Grade:** heaven
- **Effect:** Forbidden-land array.
- **Source:** BTT / F160

#### Immortal Tomb Formation (仙墓阵)
- **Novel:** BTT
- **User:** Immortal Tomb guardians
- **Grade:** immortal
- **Effect:** Tomb-protecting sealing array.
- **Source:** BTT / F161

#### Restrictive Spell Formation (封咒阵) — required to kill peak-realm cultivators
- **Novel:** ISSTH
- **User:** ISSTH cultivators (universal)
- **Grade:** immortal
- **Activation:** coordinated spell-seal flags
- **Effect:** REQUIRED to surround/kill peak-realm cultivators; without it target can teleport/escape.
- **Source:** ISSTH / F43

#### Planet-Sealing Ancient Talisman Ash Array
- **Novel:** ISSTH
- **User:** ancient cultivator
- **Grade:** immortal
- **Activation:** burnt ancient talisman ashes (per fandom)
- **Effect:** "Immortal Sense Soil in Black Lands is filled with ashes of burnt ancient talisman that was created in order to seal the planet by an [ancient]" (ISSTH fandom — confirms planet-sealing talisman-ash formation).
- **Source:** ISSTH fandom / search_07

#### Mountain and Sea Realm Formation (山海界阵)
- **Novel:** ISSTH
- **User:** Allheaven / Meng Hao
- **Grade:** dao
- **Effect:** Realm-wide defensive+sealing formation.
- **Source:** ISSTH / F63

#### 9 Mountains & 9 Seas Cosmic Formation (九山九海)
- **Novel:** ISSTH
- **User:** Mountain and Sea Realm
- **Grade:** dao
- **Activation:** cosmic structure (the mountains/seas ARE the array-points)
- **Effect:** Cosmic structure IS the formation; each mountain/sea an array-point.
- **Source:** ISSTH / F62

#### Three Inch World (三寸人间) — namesake formation of AWWP
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** dao
- **Activation:** miniature-world essence
- **Effect:** Seals a region into a pocket dimension. The namesake formation of AWWP.
- **Source:** AWWP / F134

#### Rope Crystal Seal (绳晶印)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** spirit
- **Effect:** Binding + sealing array.
- **Source:** AWWP / F128

#### Abyss Gate (渊门 / 深渊之门)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** dao
- **Activation:** abyss-essence + gate-runes
- **Effect:** Abyss-builder gate; seals/traverses the abyss.
- **Source:** Ptt / F111

#### Five Direction Seal (五方印)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** immortal
- **Activation:** 5-direction essence + seal-runes
- **Effect:** 5-direction sealing array.
- **Source:** Ptt / F102

#### Branding Art (烙印之术)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** immortal
- **Effect:** Brands souls; foundation of Su Ming's soul-mastery and sealing work.
- **Source:** Ptt / F109

#### Treasured Bronze Tomb Formation (宝铜墓阵)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** immortal
- **Activation:** 3-piece set (Coffin + Spear + Armguard)
- **Effect:** Tomb-array that traps and refines enemies; 3 pieces form one array.
- **Source:** Ptt / F99

#### Planet-Sealing Array (universal) (封星阵)
- **Novel:** cross
- **Grade:** dao
- **Effect:** Universal archetype. Seals an entire planet. Used by ancient cultivators to lock worlds.
- **Source:** cross / formation-talisman-catalog

---

### 4. Transport Formations

#### Teleportation Restriction (传送禁制)
- **Novel:** RI
- **User:** Wang Lin (ch.493)
- **Grade:** immortal
- **Activation:** instant (technique) — uses the split-second of time distortion during teleport
- **Effect:** Seals self in time-distortion, then explodes accumulated force for several-times-stronger teleport. The basis of Wang Lin's spatial travel.
- **Source:** RI ch.493 / wl_techniques.json

#### Great Teleportation Forbidden (大传送禁)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** dao
- **Activation:** spatial dao + teleport-runes
- **Effect:** Large-scale teleportation restriction; planet-to-planet travel.
- **Source:** RI / F32

#### Transport Array (universal) (传送阵)
- **Novel:** cross
- **Grade:** spirit
- **Activation:** paired array blocks + linking jade slip
- **Effect:** Instant travel between paired arrays.
- **Portable?** No — paired block-based structures.
- **Source:** Formation.java Transport Array registry

#### Bridge of Immortality (仙桥) — 9 steps
- **Novel:** ISSTH
- **User:** Meng Hao
- **Grade:** transcendence
- **Activation:** 9 ascending steps, each a formation
- **Effect:** 9-step cosmic bridge; crossing = immortality.
- **Source:** ISSTH / F52

#### Dark Sect Interstellar Travel Formation (黑暗星河旅行阵)
- **Novel:** AWWP
- **User:** Dark Sect / Wang Baole
- **Grade:** heaven
- **Effect:** Interstellar travel array.
- **Source:** AWWP / F146

#### Hades Coffin (coffin-world transport) (冥棺)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** heaven
- **Effect:** Coffin that IS a small world; transports and defends.
- **Source:** AWWP / F120 / T102

#### Heaven-Avoiding Coffin (避天棺)
- **Novel:** RI
- **User:** Wang Lin (ch.819; houses Li Muwan's soul)
- **Grade:** immortal
- **Activation:** coffin + heaven-avoiding runes (auto-active while occupied)
- **Effect:** Hides occupant from heavenly tribulation and divine sense.
- **Portable?** Yes — item.
- **Source:** RI ch.819 / wl_items.json

#### Lone Boat Black Robe Lantern Oar (孤舟黑袍灯笼桨)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** spirit
- **Activation:** 4-piece lone-boat travel set
- **Effect:** Travel-talisman set.
- **Source:** AWWP / T96

#### Mystic Trace Bead (玄迹珠)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** spirit
- **Effect:** Tracking + navigation bead-formation.
- **Source:** AWWP / F131

#### Entropic Teleportation Talismans (熵变传送符)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #12)
- **Grade:** spirit
- **Activation:** crush (consumable)
- **Effect:** Random teleport (could be 5,000 km away); counts as extra life.
- **Portable?** Yes — consumable jade talisman.
- **Source:** BTT / T113 / wl_techniques

#### Jade Feather Magical Device (玉羽法器)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #9)
- **Grade:** spirit
- **Effect:** Speed-type magical device talisman.
- **Source:** BTT / T116

#### Pearl of Holding (须弥珠)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #7)
- **Grade:** spirit
- **Effect:** Spatial-storage pearl talisman.
- **Source:** BTT / T118

---

### 5. Surveillance Formations

#### Heavenspan Life Lamp System (天渊命灯系统)
- **Novel:** AWE
- **User:** Heavenspan sects (universal)
- **Grade:** spirit
- **Activation:** soul-fragment lamp per disciple; permanent
- **Effect:** Every disciple's soul-fragment is enshrined as a lamp. Lamp flickers on injury, cracks on qi-deviation, shatters on death. Sect alerted instantly.
- **Portable?** No — sect-bound monitoring system.
- **Source:** AWE / F78

#### Soul-Lamp Tied Array (universal) (命灯阵)
- **Novel:** cross
- **Grade:** spirit
- **Effect:** Links disciple life-lamps to sect array; killing a disciple weakens the array.
- **Source:** formation-talisman-catalog (universal)

#### Sundial Life Lamp Formation (日晷命灯阵)
- **Novel:** BTT
- **User:** Xu Qing (one of 12-13 Heavenly Palaces)
- **Grade:** immortal
- **Effect:** Sundial-driven life-lamp array.
- **Source:** BTT / F165 / T125

#### Glittering Eyes (光眼)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Grade:** spirit
- **Activation:** eye-essence + glittering runes (instant)
- **Effect:** Surveillance + illusion eye-array.
- **Source:** ISSTH / F67

#### Restriction-Breaking Ancient Mirror (破禁古镜) — also a surveillance tool
- **Novel:** RI
- **User:** Wang Lin (ch.774)
- **Grade:** immortal
- **Activation:** instant (mirror activation)
- **Effect:** Reveals and breaks arrays/restrictions; bypasses sect-protecting arrays. Doubles as surveillance (can see what's hidden).
- **Portable?** Yes — item.
- **Source:** RI ch.774 / wl_items.json

#### Feng Shui Compass (风水罗盘)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** spirit
- **Activation:** instant
- **Effect:** Reads terrain, finds spirit-veins, surveils land Feng Shui.
- **Source:** Ptt / F104

#### Star Compass (星罗盘)
- **Novel:** RI
- **User:** Wang Lin (from Chi Hu, ch.477)
- **Grade:** immortal
- **Activation:** instant
- **Effect:** Star-aligned compass array; navigation + defense. Used in void only.
- **Portable?** Yes — item.
- **Source:** RI ch.477 / wl_items.json

#### Silver Dragon Star Compass (银龙星罗盘)
- **Novel:** RI
- **User:** Wang Lin (upgraded Star Compass, ch.477)
- **Grade:** immortal
- **Effect:** Upgraded Star Compass with silver-dragon spirit.
- **Source:** RI ch.477 / wl_items.json

---

### 6. Illusion Formations

#### Transformation Mask Formation (千面面具阵)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** heaven
- **Activation:** mask-wear (instant)
- **Effect:** Shape-shifting mask formation; can mimic anyone. Cannot be seen through below Mahayana.
- **Portable?** Yes — mask item.
- **Source:** AWE / F86 / T43

#### Picture Creation (画界 / 画创)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** dao
- **Activation:** painting (creation-essence)
- **Effect:** Su Ming's self-created array; paints reality into existence.
- **Source:** Ptt / F106

#### Hairpin Thousand Illusion Ruthless (千幻无情簪)
- **Novel:** RI
- **User:** Wang Lin (ch.672, Huang Family bag of holding)
- **Grade:** immortal (pseudo-celestial)
- **Activation:** instant (hairpin)
- **Effect:** Illusion-hairpin talisman (Thousand Illusion Ruthless Domain).
- **Portable?** Yes.
- **Source:** RI ch.672 / wl_items.json

#### Hallucination Dharma Pillow (幻法枕)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** mortal
- **Effect:** Hallucination-inducing pillow talisman.
- **Source:** AWWP / T86

#### Multi-Layered Illusion Spell (Great Soul Sect main spell)
- **Novel:** RI
- **User:** Wang Lin (ch.1853) + Great Soul Sect + Fan Shanmeng
- **Grade:** immortal
- **Activation:** 9-level spell; uses the Ghostly Sail
- **Effect:** 9-level multi-layered illusion. Used by Fan Shanmeng against Wang Lin and her sister Fan Shanlu. Wang Lin later refined his own sail to cast it.
- **Source:** RI ch.1853 / wl_techniques.json + wl_items.json

#### Illusionary Circle (幻阵)
- **Novel:** RI
- **User:** Wang Lin (ch.180, developed over 7 years)
- **Grade:** spirit
- **Activation:** instant (technique)
- **Effect:** Restriction-breaking illusion. By examining the wave produced by the illusionary circle, Wang Lin could understand the structure and rules of any restriction — without even looking at it. Wang Lin's signature early-game restriction-analysis tool.
- **Portable?** Yes — internal technique.
- **Source:** RI ch.180 / wl_techniques.json

#### Soul-Storing Mirror Formation (存魂镜阵)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** immortal
- **Effect:** Mirrors that store and project souls; defensive soul-array. Taken from Chosen of Sky River Court. Creates illusory clones.
- **Source:** AWE / F87 / T44

---

### 7. Soul Formations

#### Ten Billion Soul Banner / Billion Soul Flag (十亿魂幡)
- **Novel:** RI
- **User:** Wang Lin
- **Grade:** immortal→heaven
- **Activation:** soul-bound banner core; refined souls embedded
- **Effect:** 30-foot banner. 37 main souls + 1 billion ordinary souls. Each soul becomes passive spirit-army; main souls become banner spirits. Inheritance treasure of the Soul Refining Sect.
- **Breaking:** Cannot be broken by Soul Formation cultivators; restricted to soul-essence masters.
- **Portable?** Yes — item (also acts as a formation).
- **Source:** RI / wl_items.json

#### Soul Flag of Soul Refining Sect (炼魂幡)
- **Novel:** RI
- **User:** Soul Refining Sect / Wang Lin
- **Grade:** immortal
- **Activation:** soul-refining flags
- **Effect:** The Soul Refining Sect's namesake formation-flag. Legacy artifact.
- **Source:** RI / F28 / wl_items.json

#### Soul Flag Production Method (炼魂幡炼制法)
- **Novel:** RI
- **User:** Wang Lin (ch.384) — main cultivation method of the Soul Refining Sect
- **Grade:** spirit (technique)
- **Effect:** Splits into three parts: Soul Refining, Soul Extracting, Soul Sealing. Foundation for all soul-flag work.
- **Source:** RI ch.384 / wl_techniques.json

#### 7 Demonic Lamps Formation (七魔灯阵)
- **Novel:** ISSTH
- **User:** Reliance Sect Patriarchs
- **Grade:** immortal
- **Activation:** 7 demonic oil lamps in Reliance Sect (each lamp = one patriarch's essence)
- **Effect:** Pouring their energy into Meng Hao triggered his Perfect Foundation. Each lamp IS a patriarch's stored soul-essence.
- **Source:** ISSTH / F53

#### Bronze Lamp (铜灯) — Patriarch Vast Sea's relic
- **Novel:** ISSTH
- **User:** Meng Hao (originally Patriarch Vast Sea)
- **Grade:** immortal
- **Activation:** soul-lamp + Patriarch Vast Sea's will
- **Effect:** Soul-lamp; second only to Copper Mirror; carries residual will of original owner; consumes enemy soul-fragments as fuel.
- **Source:** ISSTH / F68

#### Live Forever Lamp (长生灯)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** dao
- **Activation:** Live-Forever Codex essence + soul-fragment
- **Effect:** Bai Xiaochun's unique Daoist-magic lamp; signature soul-formation.
- **Source:** AWE / F81

#### Soul Lamps (灵魂灯) — universal
- **Novel:** cross
- **Grade:** immortal
- **Effect:** Soul-fragment lamps tied to sect protective array.
- **Source:** ISSTH / T35

#### Anti-Ancient Soul Lamps (反古灵魂灯)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Grade:** immortal
- **Effect:** Anti-ancient soul-lamps talisman.
- **Source:** ISSTH / T38

#### Dao Soul (道魂)
- **Novel:** Ptt
- **User:** Su Ming
- **Grade:** dao
- **Effect:** Su Ming's signature soul-formation.
- **Source:** Ptt / F107

#### Soul Bead (魂珠)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** spirit
- **Effect:** Soul-storage + defensive bead-formation.
- **Source:** AWWP / F139 / T110

#### Devil Soul Bottle (魔魂瓶)
- **Novel:** RI
- **User:** Wang Lin (ch.1388, Ancient Devil Treasure)
- **Grade:** immortal
- **Activation:** instant
- **Effect:** Soul-bottle that stores devil-souls.
- **Portable?** Yes — item.
- **Source:** RI ch.1388 / wl_items.json

#### Soul Gourd (魂葫芦)
- **Novel:** RI
- **User:** Wang Lin (won in a bet with Yan Lu, ch.1836)
- **Grade:** immortal
- **Activation:** instant — one-use
- **Effect:** Originally from 9th ancestor Luo Yunhai of Great Soul Sect. Contains 30 million dao souls (originally 1 billion, now broken). One-use attack equal to peak mid-stage Void Tribulant cultivator.
- **Portable?** Yes — one-use item.
- **Source:** RI ch.1836 / wl_items.json (destroyed ch.1869)

#### Lantern (Soul-Protection) (护魂灯笼)
- **Novel:** RI
- **User:** Wang Lin (ch.1867)
- **Grade:** immortal
- **Effect:** Simple protection treasure. No offensive power, but powerful protection. As long as the fire inside didn't extinguish, the user was protected.
- **Source:** RI ch.1867 / wl_items.json

#### Living Soul Bell (活魂铃)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #11)
- **Grade:** spirit
- **Activation:** instant
- **Effect:** Soul-bell weapon talisman; 320k spirit stones.
- **Portable?** Yes — item.
- **Source:** BTT / T117

#### Beastbirth Seed Entity Formation (兽胎种子阵)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** heaven
- **Effect:** Forms a sentient beast-companion (Bruiser chimera) from a seed-entity.
- **Source:** AWE / F91 / T50

---

### 8. Alchemy Auxiliary Formations

#### Violet Fate Sect Alchemy-Formation (紫运宗丹阵)
- **Novel:** ISSTH
- **User:** Violet Fate Sect
- **Grade:** immortal
- **Activation:** alchemy-fire array + sect cauldrons
- **Effect:** Alchemy-boosting array for pill refinement.
- **Source:** ISSTH / F56

#### Pill Stream Sect Formation (丹溪宗阵)
- **Novel:** AWE
- **User:** Pill Stream Sect
- **Grade:** immortal
- **Effect:** Alchemy + defense hybrid sect array.
- **Source:** AWE / F75

#### Mysterious Turtle Wok (玄龟锅) — alchemy-aux
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** heaven (per T45; spirit per F70 — F70 is the formation, T45 the talisman item)
- **Activation:** colored-flame ritual (1 additional color per enhancement tier)
- **Effect:** Spirit-enhancement wok-formation; multi-color-flame enhancement. Sucks lifespan as cost.
- **Portable?** Yes — wok item.
- **Source:** AWE / F70, T45

#### Ancient God Furnace / Cauldron (古神炉)
- **Novel:** RI
- **User:** Wang Lin (created by Tu Si, ch.838)
- **Grade:** heaven
- **Activation:** ancient-god body-furnace
- **Effect:** Body-furnace used to refine ancient-god essences; doubles as defensive array. Destroyed ch.1226.
- **Portable?** Yes — item (body-tied).
- **Source:** RI ch.838 / wl_items.json

#### Emperor Furnace (帝王炉)
- **Novel:** RI
- **User:** Wang Lin (ch.1386)
- **Grade:** heaven (Royal Ancient God Treasure, Cave World)
- **Activation:** imperial-rune cauldron
- **Effect:** Captures and refines all things. Used to refine Esteemed Ling Dong into ancient slave, and later Zhou Jin.
- **Portable?** Yes — item.
- **Source:** RI ch.1386 / wl_items.json

#### Heavenhorn Sword 10-fold Enhancement Formation (also alchemy-aux)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** heaven
- **Effect:** Spirit-enhancement layered array for the Heavenhorn Sword.
- **Source:** AWE / F82 (listed in Defensive section too)

---

### 9. Spirit Gathering Formations

#### Heng Yue Sect Spirit-Gathering Sub-Array (恒岳宗聚灵阵)
- **Novel:** RI
- **User:** Heng Yue Sect
- **Grade:** spirit
- **Activation:** spirit-vein tap + flag-set
- **Effect:** Sub-component of the sect-protecting array. Concentrates ambient Qi for disciple cultivation.
- **Portable?** No.
- **Source:** RI / F36 (sub-component)

#### Spirit Stream Sect Spirit-Gathering (灵溪宗聚灵)
- **Novel:** AWE
- **User:** Spirit Stream Sect
- **Grade:** immortal
- **Effect:** Spirit-stream essence vein tap; powers sect cultivation.
- **Source:** AWE / F72 (sub-component)

#### Spirit Transformation (灵身化)
- **Novel:** RI
- **User:** Wang Lin (ch.765)
- **Grade:** immortal
- **Activation:** instant (Ancient Order spell)
- **Effect:** To enter a planet NOT to absorb spiritual energy but to surround oneself in spiritual energy and enter a dormant state. Undetectable except by another Ancient God.
- **Portable?** Yes — technique.
- **Source:** RI ch.765 / wl_techniques.json

#### Dao Lake → Dao Sea (道湖 → 道海)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Grade:** dao
- **Activation:** sentient body of water (grows with Meng Hao's belief)
- **Effect:** A literal lake that IS the Dao — Meng Hao's first Dao comprehension; expands to Dao Sea as belief grows; the lake itself is the teacher. Hybrid: spirit-gathering + Dao-comprehension.
- **Source:** ISSTH / F42

---

### 10. Time / Space Formations

#### Time Restriction (岁月禁制) — already in §3
- The 2nd of the 4 Great Restrictions.

#### Bronze Mirror Time Domain (铜镜时域)
- **Novel:** RI
- **User:** Wang Lin (ch.662; made on Planet Ran Yun)
- **Grade:** immortal (pseudo-celestial)
- **Activation:** instant (mirror)
- **Effect:** Projects a time-sealing domain from a mirror. Originally made for sale as celestial-spirit-jade merchandise.
- **Portable?** Yes — item.
- **Source:** RI ch.662 / wl_items.json

#### Desert of Time Formation (光阴沙漠阵)
- **Novel:** BTT
- **User:** 5th Star Ring powers
- **Grade:** dao
- **Effect:** Time-distorting desert array.
- **Source:** BTT / F162

#### Chaotic Space-Time Formation (混乱时空阵)
- **Novel:** BTT
- **User:** Xu Qing (Extreme Ontology breakthrough)
- **Grade:** dao
- **Effect:** Sealing array for chaotic space-time essence.
- **Source:** BTT / F163

#### Timescape Bottle (光阴瓶)
- **Novel:** BTT
- **User:** Xu Qing (Heavenly Palace)
- **Grade:** immortal
- **Effect:** Time-bottle talisman; one of the Heavenly Palaces.
- **Source:** BTT / T126

#### Time Wooden Sword (时间木剑)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Grade:** immortal
- **Activation:** instant (sword-strike)
- **Effect:** Sword that cuts through time; seals target in temporal loop.
- **Source:** ISSTH / F50 / T28

#### Flowing Time (时间流)
- **Novel:** RI
- **User:** Wang Lin (ch.1245) — 2nd Original Spell
- **Grade:** dao
- **Activation:** instant (Dao of Time)
- **Effect:** Wang Lin's 2nd self-created Original Spell; Dao of Time manifestation.
- **Source:** RI ch.1245 / wl_techniques.json

#### Six Cultivation Planets Restriction — already in §3 (planetary-scale time/space seal)
- See §3.

#### Realm-Sealing Formation — already in §3
- See §3.

#### Abyss Gate (渊门 / 深渊之门) — already in §3
- Abyss-builder transport/seal between existence layers.

#### Bridge of Immortality — already in §4
- 9-step cosmic transport+ascension.

#### Heaven Splitting Axe — formation spirit of the Realm-Sealing Formation
- **Novel:** RI
- **User:** Wang Lin (subdued ch.1664, destroyed ch.1763)
- **Grade:** immortal (Ancestral Royal Weapon, Cave World)
- **Effect:** Was one of the Realm-Sealing Formation's formation spirit. Wang Lin subdued it when he destroyed the formation.
- **Source:** RI ch.1664 / wl_items.json

#### Spatial Bending (空间弯)
- **Novel:** RI
- **User:** Wang Lin (ch.770)
- **Grade:** immortal
- **Activation:** instant (technique)
- **Effect:** Bends space as a defensive/maneuverability array.
- **Source:** RI ch.770 / wl_techniques.json

---

### 11. Hybrid / Multi-purpose Formations

#### Restriction Flag (禁幡) — already in §3 (signature hybrid: DEF+OFF+SEL+SOUL)
- See §3 for full entry. This is THE canonical hybrid formation-flag.

#### Five Elements True Body (五行真身) — already in §1 (body+formation hybrid)
- See §1.

#### Dao Sovereign Domain (道主)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Grade:** dao
- **Activation:** dao-domain sovereign-rune
- **Effect:** Meng Hao's endgame Dao-domain formation.
- **Source:** ISSTH / F51

#### Dao Lake → Dao Sea (already in §9) — also hybrid (gathering + Dao comprehension)
- See §9.

#### Waterswamp Kingdom (水泽国度)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Grade:** dao (one of 10 Spirit Stream secret magics)
- **Activation:** life-essence + waterswamp runes; requires observing all beasts to forge a personal Life Essence Spirit (Bai's = Neverending Turtle)
- **Effect:** Bai Xiaochun's signature life-essence formation; summons the spirit of the Waterswamp Kingdom.
- **Source:** AWE / F71

#### Infinite Armament Transformation Formation (无限法器变化阵)
- **Novel:** AWWP
- **User:** Wang Baole
- **Grade:** spirit (per catalog) / heaven (per F148)
- **Activation:** infinite-armament + transformation runes
- **Effect:** Transforms one armament into many; signature self-created technique-formation. Ethereal Dao College technique.
- **Source:** AWWP / F148

#### Three Inch World (三寸人间) — already in §3 (sealing + transport + pocket-world hybrid)
- See §3.

#### Undying Live Forever Codex Complete Array — already in §1
- See §1.

#### Dao Domain Formations (universal endgame)
- **Novel:** cross
- **Grade:** dao
- **Users:** Wang Lin (Life-Death + Karma + True-False + Battle-Will Domains, RI); Meng Hao (Dao Sovereign + Dao Lake, ISSTH); Su Ming (Dao Soul, Ptt); Bai Xiaochun (Live Forever Lamp, AWE); Wang Baole (Dao of Freedom, AWWP); Xu Qing (5 God State levels, BTT)
- **Effect:** At Soul Formation+, the cultivator's domain IS a formation.
- **Source:** cross (worklog Part C summary)

#### Soul-Lamp Tied Array (universal) — already in §5 (soul+surveillance+defensive hybrid)
- See §5.

#### Body-Formation (universal archetype) — multiple instances in §1
- See §1.

#### The Dragon Formation (龙阵)
- **Novel:** RI
- **User:** Wang Lin (gifted by Li MuWan)
- **Grade:** spirit
- **Activation:** inherited from Fighting Evil Sect's formation
- **Effect:** Defensive dragon-themed array; based on the Fighting Evil Sect's main formation.
- **Source:** RI / F21 / wl_items.json

#### Mountain and River Screen (山河屏) — already in §1 (defensive + terrain reshape)
- See §1.

#### Picture Creation (画界) — already in §6 (illusion + creation hybrid)
- See §6.

#### Baole's Three Inch World (三寸人间) + Realm Mending Plate (补天盘) — combined endgame
- See §1 (Realm Mending) + §3 (Three Inch World). Together they form Wang Baole's endgame hybrid sealing+mending+transport system.

#### The Bell (钟) — already in §1 (defensive + sonic offense)
- See §1.

---

## Part 2: Talismans

### Summary
- **Total talismans catalogued: 135**
- By novel: RI = 22 · ISSTH = 20 · AWE = 16 · Ptt = 25 · AWWP = 27 · BTT = 25
- By type:
  - Paper Talismans (consumable): **~12**
  - Jade Slips (knowledge): **~6** (5 tiers + transmission)
  - Soul Banners: **~5**
  - Life Lanterns: **~7**
  - Sealing Stamps: **~7**
  - Mirrors: **~6**
  - Compasses: **~7**
  - Coffins: **~4**
  - Bells: **~5**
  - Masks: **~4**
  - Pearls/Beads: **~7**
  - Cauldrons/Woks: **~5**
  - Formation Flags (as talismans): **~7**
  - Other (weapons, pagodas, rings, pendants, etc.): **~33**
- Universal archetypes: jade slip (every novel), life lamp (every sect), formation flag (every sect), paper talisman (every cultivator), soul banner (every demonic sect).

---

### 1. Paper Talismans (consumable)

#### Concealment Talisman (隐匿符) — universal
- **Novel:** cross
- **Type:** paper talisman
- **Grade:** spirit
- **Activation:** crush (right-click consumable)
- **Effect:** Hides cultivator's aura and presence for a duration.
- **Consumable?** Yes — one-use.
- **Ownership binding?** No.
- **Source:** formation-talisman-catalog (universal)

#### Defense Talisman (防御符 / 防御符宝)
- **Novel:** cross / BTT (Xu Qing Items #5)
- **Type:** paper talisman
- **Grade:** mortal (universal) / spirit (Xu Qing's)
- **Activation:** crush
- **Effect:** One-time damage absorption. Xu Qing's was destroyed vs Vajra Patriarch.
- **Consumable?** Yes.
- **Source:** BTT / T111

#### Tattoo Talisman Speed Boost (纹身符箓)
- **Novel:** RI
- **User:** Wang Lin (Tattoo Tribe treasure, ch.774)
- **Type:** paper talisman
- **Grade:** spirit
- **Activation:** inscribe on body
- **Effect:** Tattoo-tribe talisman; speed-boost when inscribed on body.
- **Consumable?** Yes.
- **Source:** RI ch.774 / wl_items.json

#### Beast Skin Tattoo (兽皮纹身)
- **Novel:** RI
- **User:** Wang Lin
- **Type:** paper talisman
- **Grade:** spirit
- **Activation:** inscribe on body
- **Effect:** Beast-skin tattoo talisman; summons beast-spirit.
- **Consumable?** Yes.
- **Source:** RI / T02

#### Writ of Karma (因果书 / 因果纸)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** paper talisman (karmic seal)
- **Grade:** immortal
- **Activation:** crush / channel
- **Effect:** Karmic tracking paper talisman; Paper of Cause and Effect variant.
- **Consumable?** No — persistent (reusable).
- **Ownership binding?** Soul-bound to karmic wielder.
- **Source:** ISSTH / T29, T34

#### Exploding Sword (爆裂剑)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** paper talisman (one-use weapon)
- **Grade:** spirit
- **Activation:** crush
- **Effect:** One-use exploding sword talisman.
- **Consumable?** Yes.
- **Source:** ISSTH / T33

#### Wooden Sword Multi-Fold (多折木剑)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** paper talisman
- **Grade:** mortal
- **Activation:** crush
- **Effect:** Early paper-folding wooden-sword talisman.
- **Consumable?** Yes.
- **Source:** ISSTH / T27

#### Eight Runic Symbols (八卦符) — 8-symbol set (Wind/Rain/Thunder/Lightning/Spring/Summer/Autumn/Winter)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** paper talisman (8-set)
- **Grade:** spirit→immortal
- **Activation:** crush each
- **Effect:** 8 Heavenly-Dragon runic symbol talismans (one per element/season).
- **Consumable?** Yes — one-use each.
- **Source:** Ptt / T83

#### Seven Seals Rune (七印符)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** paper talisman (7-seal set)
- **Grade:** spirit
- **Activation:** crush
- **Effect:** 7-seal rune talisman.
- **Consumable?** Yes.
- **Source:** Ptt / T82

#### Aphrodisiac Pill Art (春药丹符)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** paper talisman (pill-talisman art)
- **Grade:** spirit
- **Activation:** crush / channel
- **Effect:** Comedic aphrodisiac pill-talisman art (canonical running gag). Forces overwhelming lust in victims.
- **Consumable?** Yes.
- **Source:** AWE / T55

#### Entropic Teleportation Talismans (熵变传送符) — also transport
- **Novel:** BTT
- **User:** Xu Qing
- **Type:** paper/jade talisman (consumable teleport)
- **Grade:** spirit
- **Activation:** crush
- **Effect:** Random teleport (could be 5,000 km away); counts as extra life.
- **Consumable?** Yes.
- **Source:** BTT / T113 (also listed in §4 Transport)

#### Paper Human Cutout Galaxy Bow (纸人剪影银河弓)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** paper talisman (weapon-formation)
- **Grade:** heaven
- **Activation:** crush / fire
- **Effect:** Paper-human + galaxy-bow talisman. Offensive.
- **Consumable?** Effectively yes (per use).
- **Source:** AWWP / T105

---

### 2. Jade Slips (knowledge)

#### Mortal Jade Slip (凡品玉简) — universal
- **Novel:** cross
- **Type:** jade slip
- **Grade:** mortal
- **Activation:** right-click to read (requires Qi Condensation+ divine sense)
- **Effect:** Basic information storage. Common currency for technique transmission.
- **Consumable?** No — persistent.
- **Ownership binding?** No.
- **Source:** formation-talisman-catalog

#### Spirit Jade Slip (灵品玉简)
- **Novel:** cross
- **Type:** jade slip
- **Grade:** spirit
- **Effect:** Mid-tier. Can store technique manuals with spiritual resonance.
- **Source:** formation-talisman-catalog

#### Earth Jade Slip (地品玉简)
- **Novel:** cross
- **Type:** jade slip
- **Grade:** earth
- **Effect:** High-tier. Self-protecting. Destroys content if wrong person opens.
- **Source:** formation-talisman-catalog

#### Heaven Jade Slip (天品玉简)
- **Novel:** cross
- **Type:** jade slip
- **Grade:** heaven
- **Effect:** Contains Dao fragments. Reading it triggers comprehension events.
- **Source:** formation-talisman-catalog

#### Dao Jade Slip (道品玉简)
- **Novel:** cross
- **Type:** jade slip
- **Grade:** dao
- **Effect:** Contains complete Dao. Reading may trigger tribulation.
- **Source:** formation-talisman-catalog

#### Soul-Storing Jade Slip (储魂玉简)
- **Novel:** RI / cross
- **Type:** jade slip (soul-storage variant)
- **Grade:** immortal
- **Activation:** right-click + divine sense branding
- **Effect:** Memory/transmission jade slip; brands knowledge via divine sense.
- **Ownership binding?** Yes — divine-sense branded.
- **Source:** RI / T03

#### Voice Transmission Jade Slip (传音玉简)
- **Novel:** RI / cross
- **Type:** jade slip (paired communication)
- **Grade:** spirit
- **Activation:** right-click (paired)
- **Effect:** Paired communication device; Foundation+ disciples standard issue.
- **Ownership binding?** Yes — paired-pair binding.
- **Source:** RI / T04

#### Jade Thunder Defense (御雷玉符)
- **Novel:** RI
- **User:** Wang Lin (Xi Zifeng's, ch.871)
- **Type:** jade slip / jade talisman
- **Grade:** spirit
- **Activation:** crush / auto-trigger
- **Effect:** Defensive jade talisman against thunder. "Important self-defense treasure."
- **Consumable?** Yes — one-use.
- **Source:** RI ch.871 / wl_items.json

#### Jade Bottle with Black Liquid (黑液玉瓶)
- **Novel:** RI
- **User:** Wang Lin (ch.1191, Seven-Colored Realm)
- **Type:** jade bottle (poison vessel)
- **Grade:** immortal
- **Effect:** Half-filled with black liquid that looked like blood but didn't smell like blood. Corrosive.
- **Consumable?** Yes — contents consumed.
- **Source:** RI ch.1191 / wl_items.json

#### Blood Jade (血玉)
- **Novel:** RI
- **User:** Wang Lin (from Yao Xixue's bag of holding)
- **Type:** jade (blood-aspect)
- **Grade:** immortal
- **Effect:** Blood-jade talisman.
- **Source:** RI / T10 / wl_items.json

#### Heaven Dao Crystal (天道晶体)
- **Novel:** RI
- **User:** Wang Lin
- **Type:** jade crystal
- **Grade:** heaven
- **Effect:** Heaven-dao crystal talisman.
- **Source:** RI / T21

#### 'Heaven Dao' Crystal (variant — Beads Seven-Colored Realm)
- **Novel:** RI
- **User:** Wang Lin (ch.1197)
- **Type:** jade crystal
- **Grade:** immortal
- **Effect:** Beads exactly like the Heaven Defying Bead — same size, weight, aura — but no five-elements pattern, only a faint blurred pattern.
- **Source:** RI ch.1197 / wl_items.json

#### Ancient Fengyao Jade (古丰妖玉)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** jade (sealing-formation hybrid)
- **Grade:** immortal
- **Effect:** Ancient jade + fengyao sealing talisman.
- **Source:** ISSTH / T26 / F66

---

### 3. Soul Banners

#### Ten Billion Soul Banner / Billion Soul Flag (十亿魂幡)
- **Novel:** RI
- **User:** Wang Lin
- **Type:** soul banner
- **Grade:** immortal→heaven
- **Activation:** soul-bound banner core; refined souls embedded
- **Effect:** 30-foot banner. 37 main souls + 1 billion ordinary souls. Each soul becomes passive spirit-army; main souls become banner spirits.
- **Consumable?** No — persistent (grows by absorbing more souls).
- **Ownership binding?** Soul-bound (only the master can wield).
- **Source:** RI / wl_items.json

#### Soul Flag (炼魂幡) — Soul Refining Sect's namesake
- **Novel:** RI
- **User:** Soul Refining Sect / Wang Lin
- **Type:** soul banner
- **Grade:** spirit (legacy artifact)
- **Effect:** Soul Refining Sect's namesake formation-flag. The sect's legacy artifact.
- **Ownership binding?** Yes — inherited via Soul Flag Production Method.
- **Source:** RI / wl_items.json

#### Ghostly Sail / Ghost Sail (鬼帆 / 魂帆)
- **Novel:** RI
- **User:** Wang Lin (ch.1699 refined his own; ch.1789 used as main sail of Soul Devil Ship)
- **Type:** soul banner (formation-flag)
- **Grade:** heaven
- **Activation:** instant (sail-deployment)
- **Effect:** Main sail for the Soul Devil Ship. Made of 4 Great Restrictions + many other restrictions. Used to cast the multi-layered illusion spell.
- **Consumable?** No — persistent.
- **Ownership binding?** Yes — soul-bound.
- **Source:** RI ch.1699, 1854 / wl_items.json

#### Blood Pavilion (血阁)
- **Novel:** RI
- **User:** Wang Lin (1st: ch.765 from Blood Ancestor's Blood Planet; 2nd: ch.1507 from Ancient Demon)
- **Type:** soul banner (blood-aspect)
- **Grade:** immortal
- **Effect:** Blood-aspect soul-storage talisman.
- **Source:** RI ch.765, 1507 / wl_items.json

#### Lu Fu Blood Balls (卢浮血球)
- **Novel:** RI
- **User:** Wang Lin (1st: ch.947; ×6: ch.1095)
- **Type:** soul banner (blood-ball set)
- **Grade:** immortal
- **Activation:** crush
- **Effect:** Soul-aspect blood-ball consumable.
- **Consumable?** Yes.
- **Source:** RI / wl_items.json

#### Ji Qiong's Head (季琼头)
- **Novel:** RI
- **User:** Wang Lin (ch.1127)
- **Type:** soul banner (sentient head)
- **Grade:** immortal
- **Effect:** Sentient head talisman (one-time-use).
- **Consumable?** Yes.
- **Source:** RI ch.1127 / wl_items.json

#### Blood-Red Nascent Soul (血红元神)
- **Novel:** RI
- **User:** Wang Lin (ch.1194)
- **Type:** soul banner (nascent-soul vessel)
- **Grade:** immortal
- **Effect:** Soul-storage talisman (one-time-use).
- **Source:** RI ch.1194 / wl_items.json

---

### 4. Life Lanterns

#### Soul Lamp (命魂灯) — RI variant
- **Novel:** RI
- **Type:** life lantern
- **Grade:** spirit
- **Activation:** soul-fragment embedding
- **Effect:** Soul-tracking lamps. Sects light one per disciple. Extinguishes on death. Wang Lin's soul manipulation foundation.
- **Ownership binding?** Yes — soul-fragment bound.
- **Source:** RI / T-RI-life-lantern

#### 7 Demonic Lamps (七魔灯) — also soul formation §7
- **Novel:** ISSTH
- **User:** Reliance Sect
- **Type:** life lantern
- **Grade:** heaven
- **Effect:** 7 patriarch lamps in Reliance Sect. Each contains a patriarch's essence.
- **Source:** ISSTH / T-7-demonic

#### Bronze Lamp (青铜灯 / 铜灯) — Patriarch Vast Sea's relic
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** life lantern
- **Grade:** immortal
- **Effect:** Patriarch Vast Expanse's relic. NOT the same as Meng Hao's Copper Mirror.
- **Note:** Two distinct items often confused — Bronze Lamp (soul-lamp relic) vs Copper/Shanhai Mirror (duplicator). See §6.
- **Source:** ISSTH / T36, T-bronze

#### Heavenspan Life Lamp (天渊命灯)
- **Novel:** AWE
- **Type:** life lantern
- **Grade:** heaven
- **Effect:** Heavenspan sect-wide life-lamp system. Tracks every disciple.
- **Source:** AWE / T54

#### Live Forever Lamp (长生灯) — also soul formation §7
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** life lantern
- **Grade:** dao
- **Effect:** Bai's personal lamp. Tied to the Live Forever Codex.
- **Source:** AWE / T57

#### Spirit Breath Lamp (灵息灯)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #8) — sold to Huang Yan
- **Type:** life lantern
- **Grade:** spirit
- **Effect:** Life Lamp talisman.
- **Consumable?** No — persistent.
- **Source:** BTT / T119

#### Sundial Life Lamp (日晷命灯) — also soul formation §7
- **Novel:** BTT
- **User:** Xu Qing
- **Type:** life lantern
- **Grade:** immortal
- **Effect:** Sundial life-lamp talisman; one of 12-13 Heavenly Palaces.
- **Source:** BTT / T125

#### Lantern (Soul-Protection) (护魂灯笼) — also soul formation §7
- **Novel:** RI
- **User:** Wang Lin (ch.1867)
- **Type:** life lantern (soul-protection variant)
- **Grade:** immortal
- **Effect:** As long as the fire inside didn't extinguish, the user was protected.
- **Source:** RI ch.1867 / wl_items.json

---

### 5. Sealing Stamps

#### 18 Hell Celestial Sealing Stamp (十八地狱封仙印)
- **Novel:** RI
- **User:** Wang Lin (ch.915, Magic Arsenal Spell)
- **Type:** sealing stamp
- **Grade:** heaven
- **Activation:** instant (treasure activation) — refined from Fragment Stamp (ch.769) during Illusory Yin breakthrough
- **Effect:** Heaven-forged treasure from Thunder Immortal World fragments. Forged into 18 Layers of Hell pocket realm using Underworld River (Life-Death Dao) + Celestial Sealing Stamp. Stores souls of all enemies Wang Lin killed.
- **Consumable?** No — persistent.
- **Ownership binding?** Soul-bound (Magic Arsenal).
- **Source:** RI ch.915 / wl_items.json + wl_techniques.json

#### Fragment Stamp Celestial Sealing (仙封残印) — precursor to above
- **Novel:** RI
- **User:** Wang Lin (ch.769)
- **Type:** sealing stamp (fragment)
- **Grade:** immortal
- **Activation:** refined via Divine Tribulation during Illusory Yin breakthrough
- **Effect:** Fragment-stamp that seals celestial energy. Refined into the full Celestial Sealing Stamp.
- **Source:** RI ch.769 / wl_items.json

#### Ruyi Seal (如意印)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** sealing stamp
- **Grade:** heaven
- **Activation:** instant
- **Effect:** Versatile "as-you-wish" sealing seal.
- **Source:** ISSTH / T-Ruyi / F65

#### Five Direction Seal (五方印) — also restriction §3
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** sealing stamp
- **Grade:** immortal
- **Effect:** 5-direction sealing talisman.
- **Source:** Ptt / T65

#### Rope Crystal Seal (绳晶印)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** sealing stamp
- **Grade:** spirit
- **Effect:** Binding + sealing array.
- **Source:** AWWP / F128 / T-rope

#### Immortal Capital Permission Medallion (仙都许可令牌)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #15)
- **Type:** sealing stamp (permission-medallion variant)
- **Grade:** immortal
- **Effect:** Medallion granting immortal-capital access. Functions as a seal/authority token.
- **Source:** BTT / T115

#### Fate Sealing Ring (封命环) — also restriction §3
- **Novel:** RI
- **User:** Wang Lin (ch.1631)
- **Type:** sealing stamp (ring form)
- **Grade:** immortal
- **Effect:** Sealed within divine retribution.
- **Source:** RI ch.1631 / wl_items.json

#### Heaven Reversal Stamp (翻天印)
- **Novel:** RI
- **User:** Wang Lin (ch.1320, Celestial Spell)
- **Type:** sealing stamp
- **Grade:** immortal
- **Activation:** instant (celestial spell)
- **Effect:** "Flip the sky to become earth and the sky will become the strongest stamp!" — Wang Lin's celestial-tier sealing stamp.
- **Source:** RI ch.1320 / wl_techniques.json

---

### 6. Mirrors

#### Copper Mirror / Shanhai Mirror (铜镜 / 山海镜) — Meng Hao's signature duplicator
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** mirror
- **Grade:** immortal (heaven→dao)
- **Activation:** right-click + spirit stones/immortal jades
- **Effect:** Duplicates any item (pills, items, even the Immortal Murdering Sword) given enough spirit stones/immortal jades. Also explodes the butts of furry demon beasts (comedic early function). Lord Fifth sealed within.
- **IMPORTANT:** ONE item (not two). Often confused with the Bronze Lamp. Copper Mirror = duplicator; Bronze Lamp = soul-lamp relic.
- **Consumable?** No — persistent.
- **Ownership binding?** Soul-bound (Meng Hao's core treasure).
- **Source:** ISSTH / T41 / search_03

#### Restriction-Breaking Ancient Mirror (破禁古镜) — also surveillance §5
- **Novel:** RI
- **User:** Wang Lin (ch.774)
- **Type:** mirror
- **Grade:** immortal
- **Activation:** instant
- **Effect:** Reveals and breaks arrays/restrictions; bypasses sect-protecting arrays.
- **Source:** RI ch.774 / wl_items.json

#### Bronze Mirror Time Domain (铜镜时域) — also time/space §10
- **Novel:** RI
- **User:** Wang Lin (ch.662, made on Planet Ran Yun)
- **Type:** mirror
- **Grade:** spirit
- **Effect:** Projects a time-sealing domain. Originally merchandise for sale.
- **Source:** RI ch.662 / wl_items.json

#### Soul-Storing Mirror (存魂镜)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** mirror
- **Grade:** spirit
- **Activation:** instant
- **Effect:** Taken from a Chosen of Sky River Court. Creates illusory clones.
- **Source:** AWE / T44

#### Glittering Eyes (光眼) — also surveillance §5
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** mirror (eye-talisman variant)
- **Grade:** spirit
- **Effect:** Surveillance + illusion eye-talisman.
- **Source:** ISSTH / T31

#### Mirror (small) (镜子)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** mirror
- **Grade:** mortal
- **Effect:** Small mirror talisman (early-game).
- **Source:** AWWP / T91

---

### 7. Compasses

#### Star Compass (星罗盘) — also surveillance §5
- **Novel:** RI
- **User:** Wang Lin (from Chi Hu, Rain Celestial Realm)
- **Type:** compass
- **Grade:** spirit
- **Effect:** Navigation + defense. Used in void only.
- **Source:** RI / wl_items.json

#### Silver Dragon Star Compass (银龙星罗盘)
- **Novel:** RI
- **User:** Wang Lin (upgraded Star Compass, ch.477)
- **Type:** compass
- **Grade:** spirit
- **Effect:** Upgraded Star Compass with silver-dragon spirit.
- **Source:** RI ch.477 / wl_items.json

#### Isolation Restriction Compass (隔离禁制罗盘) — also restriction §3
- **Novel:** RI
- **User:** Wang Lin (ch.1850)
- **Type:** compass
- **Grade:** immortal
- **Effect:** Isolates a region from divine-sense scrying and teleport-escape. Contains the Devil Restriction Sect's Devil Isolation Restriction.
- **Source:** RI ch.1850 / wl_items.json

#### Heart Compass of Annihilation (寂灭心罗盘) — also restriction §3
- **Novel:** RI
- **User:** Wang Lin
- **Type:** compass
- **Grade:** immortal
- **Effect:** Heart-targeting annihilation compass-seal. Inheritance Treasure (ch.858).
- **Source:** RI ch.858 / wl_items.json

#### Feng Shui Compass (风水罗盘) — also surveillance §5
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** compass
- **Grade:** spirit
- **Effect:** Feng Shui navigation tool. Reads terrain, finds spirit-veins.
- **Source:** Ptt / T67

#### Mystic Trace Bead (玄迹珠) — also transport §4
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** compass (bead-form)
- **Grade:** spirit
- **Effect:** Tracking + navigation bead-formation.
- **Source:** AWWP / F131

#### Realm Mending Plate (补天盘) — also defensive §1 + restriction §3
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** compass (plate-form)
- **Grade:** dao
- **Effect:** Repairs realm-level damage; cosmic-scale mending array.
- **Source:** AWWP / T107 / F135

---

### 8. Coffins

#### Heaven-Avoiding Coffin (避天棺) — also transport §4 + soul §7
- **Novel:** RI
- **User:** Wang Lin (ch.819; houses Li Muwan's soul)
- **Type:** coffin
- **Grade:** heaven
- **Activation:** coffin + heaven-avoiding runes (auto-active while occupied)
- **Effect:** Hides occupant from heavenly tribulation and divine sense.
- **Consumable?** No — persistent.
- **Ownership binding?** Soul-bound (houses Li Muwan's soul).
- **Source:** RI ch.819 / wl_items.json

#### Hades Coffin (冥棺)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** coffin
- **Grade:** dao
- **Activation:** instant (interstellar travel via coffin interior)
- **Effect:** Dark Sect artifact. Enables interstellar travel via coffin interior. Coffin that IS a small world.
- **Source:** AWWP / T102 / F120

#### Treasured Bronze Tomb (宝铜墓) — 3-piece set: Coffin + Spear + Armguard
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** coffin (3-piece set)
- **Grade:** immortal
- **Activation:** 3-piece set activation
- **Effect:** Immortal Treasure set. Coffin + spear + armguard form one array. Traps and refines enemies.
- **Source:** Ptt / T61 / F99

#### D-132 Prison Cell (D-132囚室) — also restriction §3
- **Novel:** BTT
- **User:** Xu Qing (became part of God Decapitation Altar)
- **Type:** coffin (prison-cell form)
- **Grade:** heaven
- **Effect:** Prison-cell coffin talisman.
- **Source:** BTT / T127 / F166

---

### 9. Bells

#### Han Mountain Bell (寒山钟) — also defensive §1
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** bell
- **Grade:** spirit
- **Activation:** bell-ring (instant)
- **Effect:** Berserker Treasure. Defense + Offense.
- **Source:** Ptt / T66 / F103

#### Three Bells Shield (三铃盾) — also defensive §1
- **Novel:** RI
- **User:** Wang Lin (later gifted to Ling'er, ch.965)
- **Type:** bell (3-bell set)
- **Grade:** spirit
- **Effect:** Defensive. Later gifted to Ling'er.
- **Source:** RI ch.965 / wl_items.json

#### Bell Sealing Tracking (封踪铃)
- **Novel:** RI
- **User:** Wang Lin
- **Type:** bell
- **Grade:** spirit
- **Activation:** instant
- **Effect:** Bell that seals and tracks a target.
- **Source:** RI / T15 / wl_items.json ("A Bell" — A treasure that had the ability of Sealing and tracking)

#### Living Soul Bell (活魂铃) — also soul §7
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #11)
- **Type:** bell (soul-weapon)
- **Grade:** spirit
- **Activation:** instant
- **Effect:** Soul-bell weapon talisman; 320k spirit stones.
- **Source:** BTT / T117

#### The Bell (钟) — also defensive §1 + hybrid §11
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** bell
- **Grade:** earth (per catalog) / heaven (per F127)
- **Effect:** 8th-grade seal/defensive bell.
- **Source:** AWWP / T-bell / F127

#### Golden Bell Shield (金钟盾) — also defensive §1
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** bell (shield-form)
- **Grade:** spirit
- **Effect:** Golden-bell defensive shield-formation.
- **Source:** AWWP / F138

---

### 10. Masks

#### Transformation Mask (千面面具 / 变身面具) — also illusion §6
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** mask
- **Grade:** heaven
- **Activation:** mask-wear (instant)
- **Effect:** Disguises as Nightcrypt / Bai Hao. Cannot be seen through below Mahayana. Contains infiltrator spy's soul.
- **Ownership binding?** Soul-bound (contains a sentient soul).
- **Source:** AWE / T43

#### Mysterious Mask (神秘面具)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** mask
- **Grade:** dao
- **Activation:** mask-wear
- **Effect:** Contains Wang Yiyi / Little Missy (Wang Lin's daughter). Source of Great Void Qi Devouring Art and Compression Art.
- **Ownership binding?** Soul-bound (contains Wang Yiyi's spirit).
- **Source:** AWWP / T84 / F119

#### Blood Immortal Mask (血仙面具)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** mask
- **Grade:** heaven
- **Activation:** mask-wear
- **Effect:** Blood-immortal lineage mask. Soul-aspect.
- **Source:** ISSTH / T25

#### Straw Hat (草帽) — identity concealment
- **Novel:** RI
- **User:** Wang Lin (got from Yunque Zi; gifted to Ling'er ch.965)
- **Type:** mask (hat-form concealment)
- **Grade:** immortal
- **Activation:** wear
- **Effect:** A treasure that could block out divine senses.
- **Source:** RI ch.965 / wl_items.json

---

### 11. Pearls / Beads

#### Pearl of Holding (须弥珠) — also transport §4
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #7)
- **Type:** pearl/bead
- **Grade:** spirit
- **Effect:** Spatial treasure. From Vajra Patriarch's corpse.
- **Source:** BTT / T118

#### Protomagnetic Pearl (元磁珠)
- **Novel:** AWE
- **User:** Bai Xiaochun (obtained in exchange for the Aphrodisiac Pill formula)
- **Type:** pearl/bead
- **Grade:** spirit
- **Activation:** instant
- **Effect:** Magnetic-field array; repels/attracts metal treasures.
- **Source:** AWE / T51 / F88

#### Tiny Turtle Shell (小龟壳)
- **Novel:** AWE
- **User:** Bai Xiaochun (found beneath Song Junwan's chamber)
- **Type:** pearl/bead (shell-form)
- **Grade:** spirit
- **Effect:** Small but sturdy defensive shell-array.
- **Source:** AWE / T48 / F90

#### Beads Seven-Colored Realm (七彩界珠)
- **Novel:** RI
- **User:** Wang Lin (ch.1197)
- **Type:** pearl/bead (set)
- **Grade:** immortal
- **Activation:** instant (set-deployment)
- **Effect:** Bead-set from Seven-Colored Realm. Regardless of size, weight, and even aura, they were exactly the same as the Heaven Defying Bead. The only difference was no five-elements pattern, only a faint, blurred pattern.
- **Source:** RI ch.1197 / wl_items.json

#### Blue Bead (蓝珠)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** pearl/bead
- **Grade:** mortal
- **Effect:** Blue-bead talisman (early-game).
- **Source:** AWWP / T92

#### Soul Bead (魂珠) — also soul §7
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** pearl/bead (soul-storage)
- **Grade:** spirit
- **Effect:** Soul-storage + defensive bead-formation.
- **Source:** AWWP / T110 / F139

#### Purple Crystal / Violet Crystal (紫晶)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #3)
- **Type:** pearl/bead (crystal)
- **Grade:** heaven
- **Activation:** instant
- **Effect:** Recovery, removes Heterogeneity, Time Dao, melts Life Lamps for bloodline upgrade, reproduces God's Eye-Opening scene.
- **Source:** BTT / T122

#### Wishing Bottle (许愿瓶)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** pearl/bead (bottle-form)
- **Grade:** heaven
- **Effect:** Wishing-bottle talisman.
- **Source:** AWWP / T106

---

### 12. Cauldrons / Woks

#### Mysterious Turtle Wok (玄龟锅) — also alchemy §8
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** cauldron/wok
- **Grade:** heaven
- **Activation:** colored-flame ritual
- **Effect:** Multi-color-flame spirit enhancement. Sucks lifespan as cost.
- **Source:** AWE / T45 / F70

#### Ancient God Furnace / Cauldron (古神炉) — also alchemy §8
- **Novel:** RI
- **User:** Wang Lin (created by Tu Si, ch.838; destroyed ch.1226)
- **Type:** cauldron/wok
- **Grade:** heaven
- **Effect:** Created by Tu Si but discarded. Positional swaps. Refines ancient-god essences. Doubles as defensive array.
- **Source:** RI ch.838 / wl_items.json

#### Emperor Furnace / Heavenly Emperor Furnace (帝王炉 / 帝炉)
- **Novel:** RI
- **User:** Wang Lin (ch.1386)
- **Type:** cauldron/wok
- **Grade:** heaven
- **Effect:** Royal Ancient God Treasure (Cave World). Captures and refines all things. Used to refine Ling Dong (ancient slave) and Zhou Jin.
- **Source:** RI ch.1386 / wl_items.json

#### Origin Lightning Cauldron 9 Holes (起源雷鼎) — also offensive §2
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** cauldron/wok
- **Grade:** heaven
- **Effect:** Origin-berserker treasure; 9-hole cauldron channels origin lightning.
- **Source:** Ptt / T63 / F100

#### Collection Pavilion (藏经阁) — cauldron-like storage
- **Novel:** RI
- **User:** Wang Lin (ch.784, Thunder Celestial Realm)
- **Type:** cauldron/wok (storage pavilion)
- **Grade:** immortal
- **Effect:** Has various celestial spells stored within. Functions as a knowledge-vessel (cauldron-analog).
- **Source:** RI ch.784 / wl_items.json

---

### 13. Formation Flags (as talismans)

#### Formation Flag (basic) (阵旗) — universal
- **Novel:** cross
- **Type:** formation flag
- **Grade:** spirit
- **Activation:** plant in pattern (multi-block)
- **Effect:** Standard formation-deployment tool. Plant flags to activate arrays.
- **Source:** formation-talisman-catalog

#### Restriction Flag (禁幡) — Wang Lin's flagship
- **Novel:** RI
- **User:** Wang Lin
- **Type:** formation flag
- **Grade:** immortal
- **Activation:** instant (Restriction Flag Method); refined over years with 99,999 restrictions + 3 ink-stones
- **Effect:** 3 versions: (1) 1st incomplete — left incomplete to summon divine tribulation in danger; (2) 2nd mixed-restrictions; (3) 3rd pure-attack restriction flag. Embeds restriction matrices, seals regions, suppresses enemies, stores pocket army. Doubles as storage + portable restriction matrix.
- **Ownership binding?** Soul-bound (Wang Lin's signature).
- **Source:** RI / wl_items.json + wl_techniques.json

#### Lightning Flag (雷旗) — also offensive §2
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** formation flag
- **Grade:** spirit→immortal
- **Effect:** Lightning coalesced into a flag. Formation-deployment + offensive.
- **Source:** ISSTH / T24 / F44

#### Shanhai Banner (山海旗)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** formation flag
- **Grade:** immortal
- **Effect:** Mountain-Sea Banner. Defensive + sealing.
- **Source:** ISSTH / T-Shanhai / F46

#### Heaven Tiger Flag (天虎旗) — also defensive §1
- **Novel:** RI
- **User:** Wang Lin (ch.879)
- **Type:** formation flag
- **Grade:** immortal
- **Effect:** Big Boi Technique; sect-protecting flag.
- **Source:** RI ch.879 / wl_items.json

#### Three Purple Flags (三紫旗) — also defensive §1
- **Novel:** RI
- **User:** Wang Lin
- **Type:** formation flag (3-set)
- **Grade:** immortal
- **Effect:** 3-flag defensive talisman set.
- **Source:** RI / wl_items.json

#### Heavenhorn Sword Enhancement Flags (天角剑增幅旗)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** formation flag
- **Grade:** heaven
- **Effect:** Multi-color flame enhancement flags bound to the Heavenhorn Sword.
- **Source:** AWE / F82 (sub-component)

#### Black Comb 19 Teeth (十九齿黑梳) — has a formation on it
- **Novel:** RI
- **User:** Wang Lin
- **Type:** formation flag (comb-form)
- **Grade:** spirit
- **Activation:** instant
- **Effect:** Black comb with 19 teeth; has a formation on it. One of the two magical treasures in the Huang Family member's bag of holding (pseudo-celestial).
- **Source:** RI ch.672 / wl_items.json

---

### 14. Other Significant Talismans

#### Heaven Defying Bead (逆天珠) — Wang Lin's signature cheat-seed
- **Novel:** RI (ch.8, Season 1 Episode 2)
- **User:** Wang Lin
- **Type:** other (cheat-seed bead)
- **Grade:** transcendence
- **Activation:** internal cultivation (5 elements pattern)
- **Effect:** Wang Lin's signature cheat-seed treasure. Revealed later to have mysterious origin.
- **Ownership binding?** Soul-bound.
- **Source:** RI ch.8 / wl_items.json

#### Ancient Pagoda (古塔)
- **Novel:** AWE
- **User:** Bai Xiaochun (gifted by a Wildlands tribe)
- **Type:** other (pagoda)
- **Grade:** heaven
- **Effect:** Imprisoned a demigod beast soul inside.
- **Source:** AWE / T49

#### Beastbirth Seed (兽生种 / 兽胎种子)
- **Novel:** AWE
- **User:** Bai Xiaochun (from Luochen Clan)
- **Type:** other (seed)
- **Grade:** earth
- **Effect:** Planted in Beast Conservatory. Birthed Bruiser (chimera companion).
- **Source:** AWE / T50 / F91

#### Spirit Tablet (灵牌)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** other (tablet)
- **Grade:** spirit
- **Effect:** Allowed him to enter the Treasure Pavilion to retrieve a magic item.
- **Source:** ISSTH / T23

#### Writ of Karma (因果书) — already in §1
- See §1.

#### Immortal Capital Medallion (仙都令) — already in §5
- See §5 (sealing stamp).

#### Jade Feather Magical Device (玉羽法器) — already in §4
- See §4 (transport).

#### Myriad Talisman Huberk (万符胡伯克)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #10) — got from Huang Yan
- **Type:** other (defensive magical device)
- **Grade:** spirit
- **Effect:** Defensive. Lesser-tier.
- **Source:** BTT / T112

#### Black Iron Sign (黑铁签)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #1)
- **Type:** other (weapon)
- **Grade:** spirit
- **Effect:** Heavy black-iron rod talisman; gained spirit automation after Vajra Ancestor inhabited it.
- **Source:** BTT / T120

#### Snake Head (蛇首)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #2; destroyed)
- **Type:** other (poison vessel)
- **Grade:** spirit
- **Effect:** Corrosive venom-sac talisman; dissolved corpses.
- **Source:** BTT / T121

#### Ghostface Scorpion's Tail (鬼面蝎尾)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #4; used up vs Vajra Patriarch)
- **Type:** other (weapon)
- **Grade:** spirit
- **Effect:** Poison-tail weapon talisman.
- **Source:** BTT / T123

#### Spike / Thorn of Misfortune (不幸之刺)
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #14)
- **Type:** other (weapon)
- **Grade:** heaven
- **Effect:** Refined by Seventh Master from spike of god's corpse; contains Divine Authority of Misfortune; can injure gods; later hosted Vajra Ancestor's artifact soul.
- **Source:** BTT / T124

#### Ghost Emperor Mountain Projection (鬼帝山投影)
- **Novel:** BTT
- **User:** Xu Qing
- **Type:** other (projection)
- **Grade:** immortal
- **Effect:** Ghost-emperor mountain projection talisman.
- **Source:** BTT / T128

#### Violet Moon (紫月)
- **Novel:** BTT
- **User:** Xu Qing
- **Type:** other (soul-vessel)
- **Grade:** heaven
- **Effect:** Violet-moon talisman; fuels 4th God State (Violet Lord).
- **Source:** BTT / T129

#### Golden Crow (金乌)
- **Novel:** BTT
- **User:** Xu Qing
- **Type:** other (soul-vessel)
- **Grade:** heaven
- **Effect:** Golden-crow talisman; one of the Heavenly Palaces.
- **Source:** BTT / T130

#### Gods Umbilical Cord (神脐带)
- **Novel:** BTT
- **User:** Xu Qing
- **Type:** other (soul-vessel)
- **Grade:** dao
- **Effect:** Gods-umbilical-cord talisman (extreme late-game).
- **Source:** BTT / T131

#### Crimson Mother Feather (赤母羽)
- **Novel:** BTT
- **User:** Xu Qing
- **Type:** other (feather)
- **Grade:** heaven
- **Effect:** Upgraded Vajra Ancestor.
- **Source:** BTT / T132

#### Eminent Desolation Flesh (皇荒血肉)
- **Novel:** BTT
- **User:** Xu Qing
- **Type:** other (flesh-vessel)
- **Grade:** heaven
- **Effect:** Used to forge Xu Qing's god-clone body.
- **Source:** BTT / T133

#### Immortal Mercury Body (不朽汞身)
- **Novel:** BTT
- **User:** Xu Qing
- **Type:** other (body-vessel)
- **Grade:** heaven
- **Effect:** Immortal-mercury used to forge Xu Qing's immortal main body.
- **Source:** BTT / T134

#### Nine Ninedawns Skulls (九黎颅骨)
- **Novel:** BTT
- **User:** Xu Qing
- **Type:** other (skull-set)
- **Grade:** heaven
- **Effect:** 9 Ninedawns-region skulls talisman.
- **Source:** BTT / T135

#### Pair of Metal Flints (金属火石对)
- **Novel:** RI
- **User:** Wang Lin (ch.672, Huang Family)
- **Type:** other (mundane talisman)
- **Grade:** mortal
- **Effect:** Naturally formed treasure. Fire-starting talisman; mundane use.
- **Source:** RI ch.672 / wl_items.json

#### Dagger Ge Hong (葛洪匕)
- **Novel:** RI
- **User:** Wang Lin (ch.747, from Ge Hong in Thunder Celestial Realm)
- **Type:** other (weapon)
- **Grade:** spirit
- **Effect:** Small concealable dagger. Wang Lin never once used it.
- **Source:** RI ch.747 / wl_items.json

#### Half-Moon Blade (半月刃)
- **Novel:** RI
- **User:** Wang Lin (got within the Cultivation Planet Crystal of Planet Suzaku)
- **Type:** other (weapon)
- **Grade:** immortal
- **Effect:** Offensive. (Used previously, not anymore.)
- **Source:** RI / wl_items.json

#### Sword Sheaths (剑鞘) — 5-set
- **Novel:** RI
- **User:** Wang Lin
- **Type:** other (5-set)
- **Grade:** spirit
- **Effect:** 5 sword-sheath talismans; sheath + suppress sword-qi. (Used previously, not anymore.)
- **Source:** RI / wl_items.json

#### 3x Ink Stones (三墨石)
- **Novel:** RI
- **User:** Wang Lin
- **Type:** other (raw material)
- **Grade:** spirit
- **Effect:** Wang Lin used them to create Restriction Flags. Trapping seal component.
- **Source:** RI / wl_items.json

#### Basic Formation Book (基础阵法书)
- **Novel:** RI
- **User:** Wang Lin
- **Type:** other (manual)
- **Grade:** mortal
- **Effect:** Wang Lin's first formation manual; foundation of his array study. After mastering it, Wang Lin still kept the book.
- **Source:** RI / wl_items.json

#### Celestial Emperor Crown (天帝冠)
- **Novel:** RI
- **User:** Wang Lin (ch.717)
- **Type:** other (crown)
- **Grade:** immortal
- **Effect:** Shining crown with five jewels socketed in it (metal, wood, water, fire, earth). Wang Lin used it due to the blue rose Red Butterfly left him.
- **Source:** RI ch.717 / wl_items.json

#### Celestial Mountain Soul (天山魂)
- **Novel:** RI
- **User:** Wang Lin (ch.712)
- **Type:** other (soul-vessel)
- **Grade:** immortal
- **Effect:** After it collapsed due to soul exploding, Wang Lin gathered the remaining pieces and condensed it. Sold at auction (ch.1177) as it was useless to him.
- **Source:** RI ch.712, 853, 1177 / wl_items.json

#### Wood Carving (Black Fiend Devil Saint's)
- **Novel:** RI
- **User:** Wang Lin (ch.931)
- **Type:** other (carving)
- **Grade:** immortal
- **Effect:** Strange carving depicting a person with folded arms surrounded by plants; face covered. Used by Black Fiend Devil Saint to defend against Moongazer Serpent's Ancient God's finger. Half-broken, then taken by Wang Lin.
- **Source:** RI ch.931 / wl_items.json

#### Dark Heaven Stone (暗天石)
- **Novel:** RI
- **User:** Wang Lin (ch.965, from Master Yi Chen)
- **Type:** other (storage stone)
- **Grade:** immortal
- **Effect:** When cultivators reach a high level, they need Dark Heaven Stones to store divine sense to create an avatar or store power spells to use.
- **Source:** RI ch.965 / wl_items.json

#### Battle Scrolls x3 (战斗卷轴)
- **Novel:** RI
- **User:** Wang Lin (ch.1095, Zhan Family)
- **Type:** other (scroll-set)
- **Grade:** immortal
- **Effect:** Zhan Family's battle scrolls.
- **Source:** RI ch.1095 / wl_items.json

#### Emerald Bracelet (碧玉手环)
- **Novel:** RI
- **User:** Wang Lin (ch.1178, from Li Qian Mei)
- **Type:** other (bracelet)
- **Grade:** immortal
- **Effect:** Protective treasure. Last of three treasures Li Qian Mei gave Wang Lin for answering her three questions.
- **Source:** RI ch.1178 / wl_items.json

#### A Broken Statue (古天帝像)
- **Novel:** RI
- **User:** Wang Lin (ch.1389)
- **Type:** other (statue)
- **Grade:** immortal
- **Effect:** Carving of the Ancient Celestial Emperor. Plain appearance but majesty; made Wang Lin's body tremble when their gazes met.
- **Source:** RI ch.1389 / wl_items.json

#### Nine drops of poison (九滴毒)
- **Novel:** RI
- **User:** Wang Lin (ch.1460; used ch.1526 to kill Nan Zhao)
- **Type:** other (poison)
- **Grade:** immortal
- **Effect:** Refined from the Joss Flames of Dao Master Miao Yin and Great Desolation's poison.
- **Source:** RI ch.1460 / wl_items.json

#### Golden Print (金印)
- **Novel:** RI
- **User:** Wang Lin (ch.1772)
- **Type:** other (seal)
- **Grade:** immortal
- **Effect:** The Sovereign's god destruction dao spell turned from illusion to corporeal by Xuan Luo. Indestructible (contains hint of Nine Suns' power).
- **Source:** RI ch.1772 / wl_items.json

#### Blue Umbrella (蓝伞)
- **Novel:** RI
- **User:** Wang Lin (ch.1835; destroyed ch.1869)
- **Type:** other (umbrella)
- **Grade:** immortal
- **Effect:** Defensive treasure. Won in a bet with Yan Lu.
- **Source:** RI ch.1835 / wl_items.json

#### Space Stone (空间石)
- **Novel:** RI
- **User:** Wang Lin (ch.1838)
- **Type:** other (stone)
- **Grade:** immortal
- **Effect:** At the cost of one pocket of space in the space stone, you can take out one item from your storage space without damage. Nurtures a Heavenly Dao. (1/3 promised gifts from founder of Great Soul Sect.)
- **Source:** RI ch.1838 / wl_items.json

#### A Jade (Great Soul Sect founder's Soul Eye Dao jade)
- **Novel:** RI
- **User:** Wang Lin (ch.1843; used ch.2023)
- **Type:** other (jade)
- **Grade:** immortal
- **Effect:** Formed by founder's lifetime of cultivating Soul Eye Dao. Allow Wang Lin to see changes in Immortal Astral Continent once, or divinate one thing. Only once. (2/3 promised gifts from founder of Great Soul Sect.)
- **Source:** RI ch.1843 / wl_items.json

#### Water Essence Drop (水元滴)
- **Novel:** RI
- **User:** Wang Lin (ch.1843)
- **Type:** other (drop)
- **Grade:** immortal
- **Effect:** Founder refined 99 rivers in Heavenly River Continent + cultivators with water essences to nourish them. (3/3 promised gifts from founder of Great Soul Sect.)
- **Source:** RI ch.1843 / wl_items.json

#### Divine Soul Brush / Pen of Immortal Touch / Golden Celestial Brush (神魂笔)
- **Novel:** RI
- **User:** Wang Lin (ch.625, created by Qing Lin for daughter Qing Shuang; given to Li Qian Mei ch.1178)
- **Type:** other (brush)
- **Grade:** immortal
- **Effect:** Qing Lin's gift to his daughter. Wang Lin later gave to Li Qian Mei.
- **Source:** RI ch.625 / wl_items.json

#### Li Guang's Bow & Arrow (李广弓箭)
- **Novel:** RI
- **User:** Wang Lin (ch.1533 bow, ch.1577 arrow)
- **Type:** other (bow+arrow)
- **Grade:** immortal
- **Effect:** Bow + arrow set. Li Guang's Heaven Shattering Bow Dao (taught by a certain madman, Dao Spell).
- **Source:** RI ch.1533, 1577 / wl_items.json

#### White Ring (白环)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (ring)
- **Grade:** spirit
- **Effect:** White-ring talisman.
- **Source:** Ptt / T69

#### Black Stone Fragment / Seed of Life Extermination (黑石碎片 / 生灭之种)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (seed)
- **Grade:** heaven→dao
- **Effect:** Su Ming's signature treasure. Black stone fragment; seed of life-extinction.
- **Source:** Ptt / T70

#### Growth Armor (生长铠)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (armor)
- **Grade:** spirit
- **Effect:** Self-growing armor talisman.
- **Source:** Ptt / T71

#### Peace Arrives Elephant (平安象)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (elephant)
- **Grade:** immortal
- **Effect:** Peace-bringing elephant talisman.
- **Source:** Ptt / T72

#### Life Inequity Spear (生不等矛)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (weapon)
- **Grade:** immortal
- **Effect:** Spear talisman embodying life-inequity.
- **Source:** Ptt / T73

#### Gu Hong's Axe (古洪斧)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (weapon)
- **Grade:** immortal
- **Effect:** Gu Hong's axe talisman.
- **Source:** Ptt / T74

#### Space Whip (空间鞭)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (weapon)
- **Grade:** immortal
- **Effect:** Space-themed whip talisman.
- **Source:** Ptt / T75

#### Virescent Sword (翠绿剑) / Freezing Sky Sword (寒天剑) / End of Wills Sword (绝意剑)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (weapons)
- **Grade:** immortal
- **Effect:** Various sword talismans.
- **Source:** Ptt / T76, T77, T81

#### Undertaker of Evil's Spear & Armor (镇恶矛 / 镇恶铠)
- **Novel:** Ptt
- **User:** Su Ming (inherited Burial Evil lineage)
- **Type:** other (weapon+armor)
- **Grade:** immortal
- **Effect:** Undertaker-of-evil set.
- **Source:** Ptt / T78, T79

#### Spiked Club (狼牙棒)
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (weapon)
- **Grade:** mortal
- **Effect:** Early spiked-club talisman.
- **Source:** Ptt / T80

#### Welcoming of Deities Pill (迎神丹) — pill-talisman hybrid
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (pill-talisman)
- **Grade:** immortal
- **Effect:** Pill-talisman that summons deities; senses souls in the world; absorbs soul fragments of deceased powerhouses.
- **Source:** Ptt / T62

#### Voice Transmission Ring (传音戒)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (ring)
- **Grade:** mortal
- **Effect:** Communication ring talisman.
- **Source:** AWWP / T85

#### Jade Green Bracelet (碧玉手环)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (bracelet)
- **Grade:** mortal
- **Effect:** Jade-green bracelet talisman.
- **Source:** AWWP / T87

#### Tiny Purple Sword (小紫剑)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (weapon)
- **Grade:** mortal
- **Effect:** Small purple-sword talisman.
- **Source:** AWWP / T88

#### Elder's Glove (长老手套)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (glove)
- **Grade:** mortal
- **Effect:** Elder-tier glove talisman.
- **Source:** AWWP / T89

#### Jade Pendant (玉佩)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (pendant)
- **Grade:** mortal
- **Effect:** Jade-pendant talisman.
- **Source:** AWWP / T90

#### Crimson Helmet (赤红盔)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (helmet)
- **Grade:** spirit
- **Effect:** Crimson helmet talisman.
- **Source:** AWWP / T93

#### Black Crocodile Saber (黑鳄刀)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (weapon)
- **Grade:** spirit
- **Effect:** Black-crocodile saber talisman.
- **Source:** AWWP / T94

#### Armor 8th Grade (八品铠)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (armor)
- **Grade:** spirit
- **Effect:** 8th-grade armor talisman.
- **Source:** AWWP / T95

#### Mysterious Stone Box Tri-Colored Sword (神秘石盒三色剑)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (weapon)
- **Grade:** spirit
- **Effect:** Tri-colored sword in stone box talisman.
- **Source:** AWWP / T97

#### Runic Soldier (符兵)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (autonomous weapon)
- **Grade:** spirit
- **Effect:** Runic-soldier talisman (autonomous).
- **Source:** AWWP / T98

#### Black Snake Pike (黑蛇矛) / Black Sword (黑剑) / Four Black Daggers (四黑匕)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (weapons)
- **Grade:** spirit
- **Effect:** Various weapon talismans.
- **Source:** AWWP / T99, T100, T103

#### The Broken Arm (残臂)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (sentient arm)
- **Grade:** heaven
- **Effect:** Sentient ancient arm-treasure; can unleash devastating formation-strikes.
- **Source:** AWWP / T101 / F121

#### Divine Eye Emperor Armor (神眼帝铠)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (armor)
- **Grade:** heaven
- **Effect:** Divine-eye + emperor armor talisman.
- **Source:** AWWP / T104

#### Ancient Green Bronze Sword (古青铜剑)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (weapon)
- **Grade:** heaven
- **Effect:** Ancient green-bronze sword talisman.
- **Source:** AWWP / T108

#### Zhu Gangqiang Puppet (朱刚强傀儡)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (puppet)
- **Grade:** spirit
- **Effect:** First self-made puppet-formation.
- **Source:** AWWP / T-Zhu

#### Construction Puppets (建造傀儡)
- **Novel:** AWWP
- **User:** Wang Baole
- **Type:** other (puppet-set)
- **Grade:** spirit
- **Effect:** Puppets that build and repair formations.
- **Source:** AWWP / F132

#### Poison Restriction Pill (毒禁丹) — pill-talisman hybrid
- **Novel:** BTT
- **User:** Xu Qing (Xu Qing Items #13)
- **Type:** other (pill-talisman)
- **Grade:** spirit
- **Effect:** Poison talisman-pill; later integrated into 3rd Heavenly Palace.
- **Source:** BTT / T114

#### Willpower (念力 nianli) — AWE cultivation currency talisman
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** other (currency)
- **Grade:** spirit
- **Effect:** Core cultivation currency talisman; powers spirit enhancement.
- **Source:** AWE / T56

#### Eternal Night (永恒之夜)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** other (defensive)
- **Grade:** immortal
- **Effect:** Defensive eternal-night talisman.
- **Source:** AWE / T46

#### Heavenhorn Sword (天角剑) — also offensive §2 + alchemy §8
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** other (weapon)
- **Grade:** heaven
- **Effect:** 10-fold spirit-enhanced Heavenhorn Sword.
- **Source:** AWE / T47

#### Heavenhorn Ink Dragon (天角墨龙)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** other (soul-vessel)
- **Grade:** heaven
- **Effect:** Ink-dragon talisman bound to Heavenhorn lineage.
- **Source:** AWE / T58

#### Wooden Sword (木剑)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** other (weapon)
- **Grade:** mortal
- **Effect:** Early wooden-sword talisman.
- **Source:** AWE / T52

#### Divine Crane Shield (神鹤盾)
- **Novel:** AWE
- **User:** Bai Xiaochun
- **Type:** other (shield)
- **Grade:** spirit
- **Effect:** Defensive crane-shield talisman.
- **Source:** AWE / T53

#### Nirvana Fruit (涅槃果)
- **Novel:** ISSTH
- **User:** Meng Hao (Fang Clan)
- **Type:** other (fruit)
- **Grade:** heaven
- **Effect:** Nirvana-rebirth talisman; Fang Clan heaven-defying magic.
- **Source:** ISSTH / T30

#### God Blood Seven Drops (七滴神血)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** other (blood-set)
- **Grade:** heaven
- **Effect:** 7 drops of god-blood talisman.
- **Source:** ISSTH / T39

#### Eyeless Silkworm (无目蚕)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** other (silkworm)
- **Grade:** immortal
- **Effect:** Never lets host's soul dissipate (saved Meng Hao post-death); produces Eyeless Silkworm Silk.
- **Source:** ISSTH / T40

#### Immortal Slaying Sword (斩仙剑)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** other (weapon)
- **Grade:** immortal
- **Effect:** Immortal-slaying sword talisman; can be duplicated by Copper Mirror.
- **Source:** ISSTH / T42

#### Red Rope Rod (红绳杖)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** other (weapon)
- **Grade:** mortal
- **Effect:** Early red-rope rod talisman.
- **Source:** ISSTH / T32

#### Demon Sealer Seal Lightning (封魔印雷)
- **Novel:** ISSTH
- **User:** Meng Hao
- **Type:** other (seal/lightning)
- **Grade:** immortal
- **Effect:** Sealing lightning talisman (Demon Sealer lineage).
- **Source:** ISSTH / T37

#### Dao Avenue Mountain (道途山) — also defensive §1
- **Novel:** Ptt
- **User:** Su Ming
- **Type:** other (mountain-vessel)
- **Grade:** heaven
- **Effect:** Divine-vessel mountain talisman.
- **Source:** Ptt / T64 / F101

#### Sublime Paragon Inheritance Formation (崇高先贤传承阵)
- **Novel:** Ptt
- **User:** Su Ming (via Sui Chen Zi inheritance)
- **Type:** other (inheritance array)
- **Grade:** heaven
- **Effect:** Inheritance-trial array; Sui Chen Zi's test in 310 years. 4 Great Arts of Divine Essence (Sui Chen Zi Arts).
- **Source:** Ptt / F116

---

## Cross-Novel Patterns

### Universal formation archetypes (every novel has these)
1. **Sect-Protecting Array (护宗阵)** — layered flags: outer perimeter → spirit-gathering → heart-array core flag. Without a Master-level breaker, sieging a sect requires overwhelming the array's qi-reserve. Examples: Heng Yue Sect (RI), Reliance Sect 7 Demonic Lamps (ISSTH), Spirit Stream Sect (AWE), Seven Moons Sect (Ptt), Federation Dao Academy (AWWP), Seven Blood Eyes 7-Peak (BTT).
2. **Restriction / Prohibition (禁制)** — the Er Genverse's distinctive "restriction" system; Wang Lin is the canonical Master of Restrictions with his 4 Great Restrictions (Annihilation/Time/Life-Death/Destruction) and the planetary Six Cultivation Planets Restriction. Xu Qing inherits the lineage with his 49 Taboo Formations. Meng Hao's Seal-Dao (8th/9th/10th Demon Sealing Hexes) is the ISSTH analog.
3. **Planet-Sealing / Realm-Sealing Array** — Wang Lin's Six Cultivation Planets Restriction (RI), ISSTH's Planet-Sealing Ancient Talisman Ash Array (burnt talisman-ash formation), Meng Hao's Bridge of Immortality, Wang Baole's Realm Mending Plate + Three Inch World (AWWP), Xu Qing's 9th Star Ring Formation (BTT).
4. **Soul-Lamp Tied Array** — sect's protective array is sustained by soul-lamps of all disciples; killing a disciple weakens the array. AWE's Heavenspan Life Lamp System is the canonical example; ISSTH's Reliance Sect 7 Demonic Lamps is a patriarch-tier variant.
5. **Body-Formation** — cultivator's body itself becomes an array. Wang Lin's Body Formation + Five Elements True Body + Ancient God Furnace (RI), Meng Hao's Nine Heavens Treasured Body Seals (ISSTH), Bai Xiaochun's Undying Live Forever Codex Complete Array (AWE), Su Ming's 999/1000 Blood-Veins Formation (Ptt), Wang Baole's Stellar Nascent Soul Formation (AWWP).
6. **Soul-Refining Array** — refines captured souls into banners or fuel. Wang Lin's Ten Billion Soul Banner (RI, 1 billion souls), Soul Refining Sect blood-sacrifice array (RI).
7. **Dao Domain Formation** — at Soul Formation+ the cultivator's domain IS a formation. Wang Lin's Life-Death Domain + Karma Domain + True-False Domain + Battle-Will Domain (RI), Meng Hao's Dao Sovereign + Dao Lake (ISSTH), Su Ming's Dao Soul (Ptt), Bai Xiaochun's Live Forever Lamp (AWE), Wang Baole's Dao of Freedom (AWWP), Xu Qing's 5 God State levels (BTT).

### Universal talisman archetypes (every novel has these)
1. **Life Lamp / Soul Lamp (命灯/魂灯)** — every Heavenspan sect (AWE) and immortal sect (ISSTH Bronze Lamp) uses soul-fragment lamps that flicker on injury, crack on qi-deviation, shatter on death. Wang Lin's (RI) Heaven-Avoiding Coffin and Xu Qing's (BTT) Sundial Life Lamp are variants.
2. **Jade Slip (玉简)** — universal memory/transmission medium; tiers mortal→spirit→earth→heaven→dao jade. Voice-transmission paired jade slips are Foundation+ standard issue.
3. **Formation Flag / Sector Flag (阵旗)** — Wang Lin's Prohibition Banner (RI), Meng Hao's Lightning Flag (ISSTH), Heaven Tiger Flag (RI), Three Purple Flags (RI), Heavenhorn Sword enhancement flags (AWE), sect-protecting flags (all novels).
4. **Soul Banner / Soul-Refining Coffin (招魂幡/炼魂棺)** — Wang Lin's Ten Billion Soul Banner (RI), Soul Flag of Soul Refining Sect (RI), Bai Xiaochun's Soul-Storing Mirror (AWE variant), Xu Qing's Living Soul Bell (BTT variant).
5. **Sealing Stamp / Seal (印)** — Wang Lin's 18 Hell Celestial Sealing Stamp + Fragment Stamp Celestial Sealing (RI), Meng Hao's Ruyi Seal + 10 Demon Sealing Hexes (ISSTH), Su Ming's Five Direction Seal + Seven Seals Rune (Ptt), Wang Baole's Rope Crystal Seal (AWWP), Xu Qing's Immortal Capital Permission Medallion (BTT).
6. **Mirror (镜)** — Meng Hao's Copper Mirror = Shanhai Mirror (ISSTH, duplicates anything), Wang Lin's Restriction-Breaking Ancient Mirror + Bronze Mirror Time Domain (RI), Bai Xiaochun's Soul-Storing Mirror (AWE), Wang Baole's small Mirror (AWWP).
7. **Paper Talisman (符纸)** — Meng Hao's Paper of Cause and Effect + Exploding Sword (ISSTH), Su Ming's Eight Runic Symbols (Ptt), Wang Baole's Paper Human Cutout Galaxy Bow (AWWP), Xu Qing's Defense Talisman (BTT).
8. **Defensive Pearl/Bead** — Xu Qing's Pearl of Holding (BTT), Bai Xiaochun's Protomagnetic Pearl (AWE), Wang Baole's Blue Bead + Soul Bead + Mystic Trace Bead (AWWP), Wang Lin's Beads Seven-Colored Realm (RI).
9. **Cauldron / Wok (鼎/锅)** — Su Ming's Origin Lightning Cauldron 9 Holes (Ptt), Bai Xiaochun's Mysterious Turtle Wok (AWE), Wang Lin's Ancient God Furnace + Emperor Furnace (RI).
10. **Coffin (棺)** — Wang Lin's Heaven-Avoiding Coffin (RI), Su Ming's Treasured Bronze Tomb Coffin (Ptt), Wang Baole's Hades Coffin (AWWP).
11. **Bell (钟/铃)** — Su Ming's Han Mountain Bell (Ptt), Wang Baole's The Bell (AWWP), Xu Qing's Living Soul Bell (BTT), Wang Lin's Bell Sealing Tracking (RI).
12. **Compass (罗盘)** — Wang Lin's Star Compass + Silver Dragon Star Compass + Isolation Restriction Compass + Heart Compass of Annihilation (RI), Su Ming's Feng Shui Compass (Ptt).

### Novel-specific formation specialties
- **RI (Wang Lin):** Restrictions are THE specialty. 4 Great Restrictions + Restriction Flag + Soul Devil Ship + Six Planets Restriction. Wang Lin is the canonical Master of Restrictions.
- **ISSTH (Meng Hao):** Seal-Dao + Dao Domain + 9 Mountains & 9 Seas Cosmic Formation. Bridge of Immortality is the transcendence-tier transport.
- **AWE (Bai Xiaochun):** Spirit Enhancement arrays (Mysterious Turtle Wok). Waterswamp Kingdom (life-essence formation). Living formations that gain sentience (parallel to Living Pills).
- **Ptt (Su Ming):** Picture Creation (painting reality). Berserker Blood-Veins body-formation. Three Barren Arts.
- **AWWP (Wang Baole):** Three Inch World (pocket-dimension sealing). Realm Mending Plate (cosmic mending). Infinite Armament Transformation.
- **BTT (Xu Qing):** 12-13 Heavenly Palaces (each a formation). 49 Taboo Formations. God Decapitation Altar (god-slaying). Space Grid (spatial locking).

### Novel-specific talisman specialties
- **RI (Wang Lin):** Soul banners (Ten Billion Soul Flag) + restriction treasures + heaven-avoiding coffins. Wang Lin's inventory is the largest talisman collection in the Ergenverse.
- **ISSTH (Meng Hao):** Copper Mirror (duplicator) + Bronze Lamp (soul-lamp) + Blood Immortal Mask + Eyeless Silkworm.
- **AWE (Bai Xiaochun):** Mysterious Turtle Wok (spirit enhancement) + Transformation Mask + Live Forever Lamp.
- **Ptt (Su Ming):** Black Stone Fragment / Seed of Life Extermination + Treasured Bronze Tomb set + Killing Sword.
- **AWWP (Wang Baole):** Mysterious Mask (Wang Yiyi) + Hades Coffin + The Broken Arm + paper talismans.
- **BTT (Xu Qing):** Purple Crystal + Entropic Teleportation Talismans + Spike/Thorn of Misfortune + Heavenly Palace set.

---

## Implementation Implications

### Which formations should be block-based vs item-based?

**BLOCK-BASED (permanent, place-bound, multi-block patterns):**
- Sect-Protecting Arrays (every sect's perimeter) — generates at canon sect locations during world-gen
- Spirit-Gathering sub-arrays (sub-component of sect arrays; spirit-vein tap)
- Transport Arrays (paired array blocks; linked via jade slip)
- Heavenspan Life Lamp System (sect-wide soul-lamp monitoring)
- Alchemy-Boosting Arrays (Violet Fate Sect Alchemy-Formation, Pill Stream Sect)
- Defensive sect palaces (Violet Moon Palace, Golden Crow Palace, etc.)
- Domain-scale formations (9 Mountains & 9 Seas, Realm-Sealing, Brilliant Heaven 9th Star Ring)
- Forbidden Sea Restriction Array, Ninedawns Forbidden Lands Formation, Immortal Tomb Formation (region-wide sealing)

**ITEM-BASED (portable, planted or activated):**
- Restriction Flag (Wang Lin's signature; plant to activate a restriction zone)
- All 4 Great Restrictions (Annihilation, Time, Life-Death, Destruction)
- Six Cultivation Planets Restriction (planet-scale, but activated by item/gesture)
- Soul Devil Ship (mobile vehicle-formation)
- Heavenly Palaces (Xu Qing's 12-13 — internal formation set)
- Personal domain formations (Life-Death Domain, Karma Domain, Dao Sovereign, etc.)
- Body-Formations (Body Formation, Five Elements True Body, Undying Codex — internal to cultivator)

**CONSUMABLE (one-use, destroyed on activation):**
- Paper Talismans (Concealment, Defense, Exploding Sword, Eight Runic Symbols, Entropic Teleportation)
- Ancient Leaf Sealing Formation (99-leaf set; consumed on use)
- Lu Fu Blood Balls, Ji Qiong's Head, Blood-Red Nascent Soul (one-time-use soul weapons)
- Aphrodisiac Pill Art (consumable pill-talisman)

**PERSISTENT ITEM (carried, activated repeatedly, often soul-bound):**
- Soul Banners (Ten Billion Soul Flag, Soul Flag, Ghostly Sail, Blood Pavilion)
- Life Lanterns (all variants)
- Sealing Stamps (18 Hell Celestial Sealing Stamp, Ruyi Seal, Heaven Reversal Stamp)
- Mirrors (Copper/Shanhai Mirror, Restriction-Breaking Ancient Mirror, Soul-Storing Mirror)
- Compasses (Star Compass, Isolation Restriction Compass, Heart Compass of Annihilation, Realm Mending Plate)
- Coffins (Heaven-Avoiding Coffin, Hades Coffin, Treasured Bronze Tomb)
- Bells (Han Mountain Bell, Three Bells Shield, Bell Sealing Tracking)
- Masks (Transformation Mask, Mysterious Mask, Blood Immortal Mask)
- Cauldrons (Mysterious Turtle Wok, Ancient God Furnace, Emperor Furnace, Origin Lightning Cauldron)
- Pearls (Protomagnetic Pearl, Pearl of Holding, Soul Bead, Purple Crystal)
- Formation Flags as talismans (Lightning Flag, Shanhai Banner, Heaven Tiger Flag, Three Purple Flags)

### Which talismans should be consumable vs persistent?

**CONSUMABLE (one-use):**
- All Paper Talismans (§1)
- Jade Thunder Defense (御雷玉符) — crush to absorb one thunder strike
- Jade Bottle with Black Liquid (one-use poison vessel)
- Lu Fu Blood Balls, Ji Qiong's Head, Blood-Red Nascent Soul (one-use soul weapons)
- Entropic Teleportation Talismans (crush to teleport)
- Poison Restriction Pill (one-use)
- Aphrodisiac Pill Art
- Ancient Leaf Sealing Formation (99-leaf consumable set)

**PERSISTENT (reusable, often with charge/durability/cooldown):**
- All Soul Banners, Life Lanterns, Sealing Stamps, Mirrors, Compasses, Coffins, Bells, Masks, Cauldrons, Formation Flags
- Heaven Defying Bead, Copper Mirror, Heavenhorn Sword (cheat-seed/core treasures)
- All "set" talismans (Treasured Bronze Tomb 3-piece, Eight Runic Symbols 8-set, Three Purple Flags 3-set, Battle Scrolls 3-set, etc.) — persistent but may consume individual pieces on use

### Which require ownership binding (blood/soul)?

**BLOOD-REFINED (life-bound; damages owner if treasure damaged):**
- Blood-Refining Technique products (RI ch.80 — Wang Lin's Blood Symbol refinement)
- 18 Hell Celestial Sealing Stamp (Magic Arsenal Spell — soul-bound)
- Restriction Flag (soul-bound to Wang Lin)
- Ten Billion Soul Banner (soul-bound; main souls are banner spirits)
- Transformation Mask (contains infiltrator spy's soul)
- Mysterious Mask (contains Wang Yiyi's spirit)
- Heaven-Avoiding Coffin (soul-bound; houses Li Muwan's soul)

**SOUL-BRANDED (only the owner can wield):**
- Soul-Storing Jade Slips (divine-sense branded)
- Voice Transmission Jade Slips (paired-pair binding)
- All life-lanterns (soul-fragment bound)
- All formation-flag talismans that are inherited (Soul Flag, Heavenhorn Sword lineage)
- Heaven Defying Bead, Copper Mirror, Black Stone Fragment (cheat-seed soul-bound)

**NO BINDING (universal use, tradeable):**
- Mortal/Spirit/Earth grade paper talismans (basic Concealment, Defense, etc.)
- Basic Formation Flags (阵旗) before refinement
- Basic Jade Slips (mortal/spirit/earth tiers)
- Most weapons and armor (unless inherited or blood-refined)
- Heavenhorn Sword enhancement flags (before binding to a wielder)

### How do formations and talismans interact with the three-layer world model?

Per `forge-mod/src/main/java/dev/ergenverse/formation/Formation.java`:

1. **PHYSICAL LAYER (block-based):** the flag blocks, core blocks, spirit-vein blocks, paper-talisman items, jade-slip items. Mortals can see and break these.
2. **SPIRITUAL LAYER:** the formation's active effect (the defensive shield, the restriction zone, the transport link, the soul-banner's spirit-army). Requires cultivation to perceive. Mortals cannot break the spiritual layer — only the physical flags. Per the Prime Directive, if a mortal breaks a flag block, the spiritual anchor may persist (it shifts to another flag, or hovers in spiritual space until a new flag is placed).
3. **DAO LAYER:** the formation's "blueprint" or "law" — the underlying principle that makes it work. A formation master can read this; others cannot. Dao-tier formations (4 Great Restrictions, Unnamed Wheel Formation, Three Inch World) exist primarily here.

**Formations span all three layers:**
- Block-based sect-protecting arrays: physical flags + spiritual effect + dao blueprint (the array's law).
- Restriction Flag: physical item + spiritual restriction zone + dao essence (Wang Lin's restriction essence).
- Paper talisman: physical paper + spiritual effect on crush + (no dao layer for low-tier; dao layer for Writ of Karma, Eight Runic Symbols).

**Talismans span physical + spiritual (mostly):**
- Persistent talismans (soul banners, mirrors, coffins): physical item + spiritual effect + ownership-binding soul-link.
- Consumable talismans (paper talismans): physical paper only — spiritual effect on crush.
- Jade slips: physical jade + spiritual content + dao comprehension event on read (for high-tier slips).

### What formation/talisman techniques should be in the technique wheel?

Based on the catalog, the technique wheel's "Formations" category should include:

1. **Restriction Flag Method (禁旗之法)** — Wang Lin's signature; refines and wields Restriction Flags.
2. **Ancient Restrictions (古禁制)** — general restriction mastery; decode and re-purpose ancient arrays.
3. **Soul Flag Production Method (炼魂幡炼制法)** — refines and wields soul banners.
4. **Illusionary Circle (幻阵)** — Wang Lin's restriction-analysis tool; understands any restriction's structure via wave-analysis.
5. **4 Great Restrictions** (Annihilation, Time, Life-Death, Destruction) — 4 separate endgame techniques.
6. **Spirit Transformation (灵身化)** — surround oneself in spiritual energy for dormant stealth.
7. **Body Formation (体阵)** — turn the body into a defensive array.
8. **Spatial Bending (空间弯)** — bend space as defensive maneuverability.
9. **Teleportation Restriction (传送禁制)** — spatial travel via time-distortion.
10. **Heaven Devouring Demon Formation ( Heaven Devouring Demon Formation )** — trap-and-kill array using cultivator corpses.
11. **Infinite Armament Transformation Formation (无限法器变化阵)** — Wang Baole's signature; transforms one armament into many.
12. **49 Taboo Formations (四十九禁忌阵)** — Xu Qing's emperor-tier taboo array.
13. **God Decapitation Altar (斩神坛)** — Xu Qing's god-slaying altar.
14. **Waterswamp Kingdom (水泽国度)** — Bai Xiaochun's life-essence formation.
15. **Picture Creation (画界)** — Su Ming's reality-painting array.

The technique wheel's "Talismans" category should include:
1. **Paper Talisman Crafting** — make consumable paper talismans.
2. **Jade Slip Inscription** — encode techniques/knowledge into jade slips.
3. **Soul Banner Refining** — refine souls into banners (Ten Billion Soul Flag method).
4. **Sealing Stamp Forging** — forge sealing stamps (18 Hell Celestial Sealing Stamp, Ruyi Seal, Heaven Reversal Stamp).
5. **Mirror Refining** — forge mirror talismans (Copper Mirror duplicator, Restriction-Breaking Mirror).
6. **Life Lamp Enshrining** — enshrine soul-fragment lamps for sect monitoring.
7. **Cauldron Refining** — forge cauldrons/woks for alchemy (Mysterious Turtle Wok, Ancient God Furnace).
8. **Compass Crafting** — forge compasses (Star Compass, Feng Shui Compass, Isolation Restriction Compass).
9. **Coffin Refining** — forge coffins (Heaven-Avoiding Coffin, Hades Coffin, Treasured Bronze Tomb).
10. **Mask Refining** — forge masks (Transformation Mask, Mysterious Mask, Blood Immortal Mask).
11. **Formation Flag Refining** — forge formation flags (Restriction Flag, Lightning Flag, Heaven Tiger Flag).
12. **Bell Refining** — forge bells (Han Mountain Bell, Three Bells Shield, Living Soul Bell).

### Recommended implementation priority (per Formation.java + DESIGN_HITBOXES_AND_FORMATIONS.md)

1. **Formation flag block** — the physical block that anchors a formation node (universal archetype).
2. **Formation core block** — the heart of a multi-block formation (heart-array core flag).
3. **Restriction Flag (禁幡) item** — Wang Lin's signature hybrid; the canonical "item that is also a formation."
4. **The 4 Great Restrictions** — as item-based formations the player can acquire (Wang Lin's lineage).
5. **Paper talisman items** — the simplest talisman type (consumable); tiered Concealment/Defense/Fire/Thunder/Ward.
6. **Sect-Protecting Array** — as a block-based formation that generates at sect locations (Heng Yue, Spirit Stream, Reliance, etc.).
7. **Jade slip item** — for technique/knowledge transfer (5 tiers + transmission variant).
8. **Soul banner item** — Wang Lin's signature talisman (Ten Billion Soul Flag).
9. **Life lamp item** — universal sect soul-tracking system.
10. **Sealing stamp item** — 18 Hell Celestial Sealing Stamp + Ruyi Seal variants.
11. **Mirror item** — Copper Mirror duplicator (Meng Hao's signature) + Restriction-Breaking Mirror.
12. **Compass item** — Star Compass + Isolation Restriction Compass + Feng Shui Compass.
13. **Coffin item** — Heaven-Avoiding Coffin + Hades Coffin.
14. **Bell item** — Han Mountain Bell + Three Bells Shield.
15. **Mask item** — Transformation Mask + Mysterious Mask.
16. **Cauldron item** — Mysterious Turtle Wok + Ancient God Furnace.
17. **Transport Array paired blocks** — universal teleport system.
18. **Heavenspan Life Lamp System block-set** — sect-wide soul-lamp monitoring (AWE).
19. **49 Taboo Formations** — Xu Qing's emperor-tier taboo array (BTT successor to restriction lineage).
20. **God Decapitation Altar** — Xu Qing's god-slaying altar (BTT).

### What NOT to do (per DESIGN_HITBOXES_AND_FORMATIONS.md)
- Do NOT make formations "spawn because the player reached realm X." Formations exist in the world (physical + spiritual layers) regardless of the player.
- Do NOT make talismans craftable without the method, materials, and comprehension. The player must earn them.
- Do NOT make formations breakable by mortals. The physical flags can be broken, but the spiritual anchor requires cultivation to affect.
- Do NOT make all formations item-based or all block-based. The hybrid approach (blocks for sect-scale, items for personal/portable) matches the canon.
- Do NOT conflate the Bronze Lamp (soul-lamp relic) with the Copper/Shanhai Mirror (duplicator). These are two distinct Meng Hao treasures often confused in fan material.

---

## Part 3: RI Wiki Canon Additions (2026-07-12, RI-BIBLE-wiki-research)

> Added by the RI-BIBLE-wiki-research task. Source: https://xian-ni.fandom.com (primary) + https://renegade-immortal-xian-ni.fandom.com (alt). Full findings in `CANON_RI_WIKI_RESEARCH_FINDINGS.md`.

### 3.1 The 6-Grade Restriction Hierarchy (CANON CONFIRMATION)

**Source:** https://xian-ni.fandom.com/wiki/Restrictions (Li Yuan quote, Ch. 749; Wang Lin quote, Ch. 1188).

The xian-ni wiki confirms that **Restrictions = Formations** ("different names, but the same meaning"). The grade hierarchy, low to high:

| # | Grade | Notes |
|---|-------|-------|
| 1 | Yellow (黄) | Lowest tier |
| 2 | Earth (地) | — |
| 3 | Mystery (玄) | — |
| 4 | Heaven (天) | Long-standing traditional ceiling |
| 5 | Void (虚) | Above the four traditional grades; split into the **Four Great Restrictions** |
| 6 | Abstract (化 / 抽象) | The peak; achieved by fusing all Four Great Restrictions; **no one had comprehended it before Wang Lin** |

**The Four Great Restrictions (Void grade, Abstract-tier when fused):**
1. **Annihilation Restriction** (寂灭禁) — inherited from Li Yuan (half of restrictions heart); Ch. 754
2. **Life and Death Restriction** (生死禁) — Ch. 1229
3. **Ancient Soul Restriction** (古魂禁) — Ch. 1697
4. **Time Restriction** (时禁) — Ch. 1223

**Canon quote (Wang Lin, Ch. 1188):** "Heaven, earth, mysterious, and yellow have been the four ranks of restrictions for a very long time! However, there was still a level above those four, and we call it 'Abstract!' The Abstract is split into the four great ancient restrictions. Aside from the Annihilation Restriction of the four great ancient restrictions, there are the Life and Death Restriction, the Ancient Soul Restriction, and the mysterious Time Restriction! Even most of the celestial restrictions were derived from these four great ancient restrictions and spread to the present. … if one could learn all four great ancient restrictions and fuse them, they would be able to comprehend the Abstract realm… Abstract is the peak of restrictions!"

### 3.2 Category:Formations — the 3 canonical formations

**Source:** https://xian-ni.fandom.com/wiki/Category:Formations (sparse wiki category — only 3 formations listed).

| Formation | Notes |
|-----------|-------|
| **Life-Death, Karma, True-False Wheel Formation** | Wang Lin's fused-domain wheel formation (Life/Death + Karma + True/False Domains combined) |
| **Realm-Sealing Formation** | The formation that sealed the Sealed Realm. Wang Lin destroyed it (Ch. 1664). Its former spirit was the **Heaven Splitting Axe** (Ancestral Royal Weapon, Cave World). |
| **Unnamed Wheel Formation** | Replaces Realm-Sealing Formation (Ch. 1667). Upgraded Life-Death/Karma/True-False Wheel Formation. Four functions: (1) treasure spirits of destroyed treasures fuse in eternally; (2) souls of destroyed cultivation planets fuse in eternally; (3) soul fragments of dead Outer Realm cultivators become wheel-pushing slaves; (4) souls of dead Inner Realm cultivators become immortal formation spirits worshipped by generations. Stops Outer Realm cultivators entering Inner Realm while NOT restricting Joss Flames (Realm-Sealing Formation's biggest flaw). |

### 3.3 Additional Named Formations (from wiki Items + Techniques pages)

| Formation | Chapter | Type | Notes |
|-----------|---------|------|-------|
| **Nine Deaths Perish Formation** | Ch. 829 | Restriction | Derived from Wang Lin's restriction study |
| **Heaven Devouring Demon Formation** | Ch. 139 | Soul/Devour | The Devouring Technique's formation; uses cultivator bodies as base (10 Foundation = trap Core Formation; 5 Core = kill mid-Core) |
| **Soul Devil Ship** | Ch. 1789 | Hybrid (ship+formation) | Ship made of Four Great Restrictions + many others; sect-protection formation of Great Soul Sect; main sail = Ghostly Sail |
| **Seven Star Sword Formation** | Ch. 715 (destroyed) | Sword-formation | Formed from 7 of Ling Tianhou's 12 disciples' swords; destroyed in Escape-Escape-Escape chapter |
| **18 Plum Restriction** | Ch. 752 | Restriction | 18 restrictions transform into statues; derived from Annihilation Restriction |
| **Burning Realm Ancient Umbrella** | Ch. 1427 (upgraded 1543) | Dao Spell / Vermilion Bird | Vermilion Bird Divine Ability that functions as a portable burning-realm formation |
| **Restriction Flag (set of 3)** | — | Item-as-formation | 1st: deliberately incomplete (summons divine tribulation); 2nd: mixed restrictions; 3rd: pure attack. Made from 3× Ink Stones via Restriction Flags Refining Method (Tu Si's inheritance; 99,999 restrictions to complete). |

### 3.4 The Restriction Flag — Full Mechanics

**Source:** Synthesized from `Wang Lin/Items`, `Wang Lin/Techniques`, and alt-wiki pages. The dedicated `https://xian-ni.fandom.com/wiki/Restriction_Flag` page **does NOT exist** (Fandom placeholder).

**What it is:** A set of three flags Wang Lin crafted using inkstones (the 3× Ink Stones treasure) and the **Restriction Flags Refining Method** inherited from Ancient God Tu Si (the 4th person to comprehend the Land of the Ancient God Restrictions Mountain trial). Each flag requires **99,999 restrictions** to complete.

**The three flags:**
1. **1st Flag** — deliberately left incomplete, so Wang Lin could summon divine tribulation when in danger. Per alt-wiki: "The first [Divine Tribulation] was because of the restriction flag."
2. **2nd Flag** — made with a mixture of different restrictions.
3. **3rd Flag** — a pure attack restriction flag.

**Lineage:** Part of the Ancient God inheritance. One of Wang Lin's 9 main items (alt-wiki). One of his 6 Main Paths: "Restrictions: Restriction Flag, Ancients Formations and Arrays, Divine sense eyes, Restriction Essence."

### 3.5 Prohibition Banner — Disambiguation

**Source:** The dedicated `https://xian-ni.fandom.com/wiki/Prohibition_Banner` page **does NOT exist** (Fandom placeholder).

The Chinese character 禁 (jìn) translates to both "prohibition" and "restriction." The "Prohibition Banner" concept overlaps with:

- **Restriction Flag** (禁旗, Jìn Qí) — the three-flag set described in 3.4 above
- **Soul Devil Ship** + **Ghostly Sail** — the ship made of the four great restrictions
- **Isolation Restriction Compass** (隔离禁盘, Gé Lí Jìn Pán) — the tracking compass Wang Lin discarded (Ch. 1864)

Fan material (e.g., YouTube "Wang Lin Ultimate Weapon: Forbidden Banner vs Soul Banner") sometimes distinguishes a "Forbidden Banner" from the Soul Banner (Billion Soul Flag), but the xian-ni wiki does NOT have a dedicated Prohibition Banner page. The concept is fully covered by the Restriction Flag family.

### 3.6 Coverage Gaps (next-fill)

The following items were identified as MISSING from this formations doc and should be added in the next pass:

- [ ] Add the **6-Grade Restriction Hierarchy** (Yellow/Earth/Mystery/Heaven/Void/Abstract) as a formal table in Part 1 Section 3 (Restriction/Sealing Formations). [✓ Done in 3.1 above; cross-link needed in main body.]
- [ ] Add the **Four Great Restrictions** with chapter citations to Part 1 Section 3. [Already covered in main body, but cross-link to 3.1 needed.]
- [ ] Add **Burning Realm Ancient Umbrella** as a hybrid formation-treasure (Ch. 1427, upgraded 1543 Dao Spell).
- [ ] Add **Seven Star Sword Formation** as a sword-formation entry (Ch. 715 destroyed).
- [ ] Add **18 Plum Restriction** as a derived restriction formation (Ch. 752).


---

## Part 3: RI Wiki Canon Additions (2026-07-12, RI-BIBLE-wiki-research)

> Added by the RI-BIBLE-wiki-research task. Source: https://xian-ni.fandom.com (primary) + https://renegade-immortal-xian-ni.fandom.com (alt). Full findings in `CANON_RI_WIKI_RESEARCH_FINDINGS.md`.

### 3.1 The 6-Grade Restriction Hierarchy (CANON CONFIRMATION)

**Source:** https://xian-ni.fandom.com/wiki/Restrictions (Li Yuan quote, Ch. 749; Wang Lin quote, Ch. 1188).

The xian-ni wiki confirms that **Restrictions = Formations** ("different names, but the same meaning"). The grade hierarchy, low to high:

| # | Grade | Notes |
|---|-------|-------|
| 1 | Yellow (黄) | Lowest tier |
| 2 | Earth (地) | — |
| 3 | Mystery (玄) | — |
| 4 | Heaven (天) | Long-standing traditional ceiling |
| 5 | Void (虚) | Above the four traditional grades; split into the **Four Great Restrictions** |
| 6 | Abstract (化 / 抽象) | The peak; achieved by fusing all Four Great Restrictions; **no one had comprehended it before Wang Lin** |

**The Four Great Restrictions (Void grade, Abstract-tier when fused):**
1. **Annihilation Restriction** (寂灭禁) — inherited from Li Yuan (half of restrictions heart); Ch. 754
2. **Life and Death Restriction** (生死禁) — Ch. 1229
3. **Ancient Soul Restriction** (古魂禁) — Ch. 1697
4. **Time Restriction** (时禁) — Ch. 1223

**Canon quote (Wang Lin, Ch. 1188):** "Heaven, earth, mysterious, and yellow have been the four ranks of restrictions for a very long time! However, there was still a level above those four, and we call it 'Abstract!' The Abstract is split into the four great ancient restrictions. Aside from the Annihilation Restriction of the four great ancient restrictions, there are the Life and Death Restriction, the Ancient Soul Restriction, and the mysterious Time Restriction! Even most of the celestial restrictions were derived from these four great ancient restrictions and spread to the present. … if one could learn all four great ancient restrictions and fuse them, they would be able to comprehend the Abstract realm… Abstract is the peak of restrictions!"

### 3.2 Category:Formations — the 3 canonical formations

**Source:** https://xian-ni.fandom.com/wiki/Category:Formations (sparse wiki category — only 3 formations listed).

| Formation | Notes |
|-----------|-------|
| **Life-Death, Karma, True-False Wheel Formation** | Wang Lin's fused-domain wheel formation (Life/Death + Karma + True/False Domains combined) |
| **Realm-Sealing Formation** | The formation that sealed the Sealed Realm. Wang Lin destroyed it (Ch. 1664). Its former spirit was the **Heaven Splitting Axe** (Ancestral Royal Weapon, Cave World). |
| **Unnamed Wheel Formation** | Replaces Realm-Sealing Formation (Ch. 1667). Upgraded Life-Death/Karma/True-False Wheel Formation. Four functions: (1) treasure spirits of destroyed treasures fuse in eternally; (2) souls of destroyed cultivation planets fuse in eternally; (3) soul fragments of dead Outer Realm cultivators become wheel-pushing slaves; (4) souls of dead Inner Realm cultivators become immortal formation spirits worshipped by generations. Stops Outer Realm cultivators entering Inner Realm while NOT restricting Joss Flames (Realm-Sealing Formation's biggest flaw). |

### 3.3 Additional Named Formations (from wiki Items + Techniques pages)

| Formation | Chapter | Type | Notes |
|-----------|---------|------|-------|
| **Nine Deaths Perish Formation** | Ch. 829 | Restriction | Derived from Wang Lin's restriction study |
| **Heaven Devouring Demon Formation** | Ch. 139 | Soul/Devour | The Devouring Technique's formation; uses cultivator bodies as base (10 Foundation = trap Core Formation; 5 Core = kill mid-Core) |
| **Soul Devil Ship** | Ch. 1789 | Hybrid (ship+formation) | Ship made of Four Great Restrictions + many others; sect-protection formation of Great Soul Sect; main sail = Ghostly Sail |
| **Seven Star Sword Formation** | Ch. 715 (destroyed) | Sword-formation | Formed from 7 of Ling Tianhou's 12 disciples' swords; destroyed in Escape-Escape-Escape chapter |
| **18 Plum Restriction** | Ch. 752 | Restriction | 18 restrictions transform into statues; derived from Annihilation Restriction |
| **Burning Realm Ancient Umbrella** | Ch. 1427 (upgraded 1543) | Dao Spell / Vermilion Bird | Vermilion Bird Divine Ability that functions as a portable burning-realm formation |
| **Restriction Flag (set of 3)** | — | Item-as-formation | 1st: deliberately incomplete (summons divine tribulation); 2nd: mixed restrictions; 3rd: pure attack. Made from 3× Ink Stones via Restriction Flags Refining Method (Tu Si's inheritance; 99,999 restrictions to complete). |

### 3.4 The Restriction Flag — Full Mechanics

**Source:** Synthesized from `Wang Lin/Items`, `Wang Lin/Techniques`, and alt-wiki pages. The dedicated `https://xian-ni.fandom.com/wiki/Restriction_Flag` page **does NOT exist** (Fandom placeholder).

**What it is:** A set of three flags Wang Lin crafted using inkstones (the 3× Ink Stones treasure) and the **Restriction Flags Refining Method** inherited from Ancient God Tu Si (the 4th person to comprehend the Land of the Ancient God Restrictions Mountain trial). Each flag requires **99,999 restrictions** to complete.

**The three flags:**
1. **1st Flag** — deliberately left incomplete, so Wang Lin could summon divine tribulation when in danger. Per alt-wiki: "The first [Divine Tribulation] was because of the restriction flag."
2. **2nd Flag** — made with a mixture of different restrictions.
3. **3rd Flag** — a pure attack restriction flag.

**Lineage:** Part of the Ancient God inheritance. One of Wang Lin's 9 main items (alt-wiki). One of his 6 Main Paths: "Restrictions: Restriction Flag, Ancients Formations and Arrays, Divine sense eyes, Restriction Essence."

### 3.5 Prohibition Banner — Disambiguation

**Source:** The dedicated `https://xian-ni.fandom.com/wiki/Prohibition_Banner` page **does NOT exist** (Fandom placeholder).

The Chinese character 禁 (jìn) translates to both "prohibition" and "restriction." The "Prohibition Banner" concept overlaps with:

- **Restriction Flag** (禁旗, Jìn Qí) — the three-flag set described in 3.4 above
- **Soul Devil Ship** + **Ghostly Sail** — the ship made of the four great restrictions
- **Isolation Restriction Compass** (隔离禁盘, Gé Lí Jìn Pán) — the tracking compass Wang Lin discarded (Ch. 1864)

Fan material (e.g., YouTube "Wang Lin Ultimate Weapon: Forbidden Banner vs Soul Banner") sometimes distinguishes a "Forbidden Banner" from the Soul Banner (Billion Soul Flag), but the xian-ni wiki does NOT have a dedicated Prohibition Banner page. The concept is fully covered by the Restriction Flag family.

### 3.6 Coverage Gaps (next-fill)

The following items were identified as MISSING from this formations doc and should be added in the next pass:

- [ ] Add the **6-Grade Restriction Hierarchy** (Yellow/Earth/Mystery/Heaven/Void/Abstract) as a formal table in Part 1 Section 3 (Restriction/Sealing Formations). [✓ Done in 3.1 above; cross-link needed in main body.]
- [ ] Add the **Four Great Restrictions** with chapter citations to Part 1 Section 3. [Already covered in main body, but cross-link to 3.1 needed.]
- [ ] Add **Burning Realm Ancient Umbrella** as a hybrid formation-treasure (Ch. 1427, upgraded 1543 Dao Spell).
- [ ] Add **Seven Star Sword Formation** as a sword-formation entry (Ch. 715 destroyed).
- [ ] Add **18 Plum Restriction** as a derived restriction formation (Ch. 752).
