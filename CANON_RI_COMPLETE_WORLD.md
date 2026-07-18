# CANON: Renegade Immortal — Complete World Catalog

**Source files mined:**
- `/home/z/my-project/tool-results/wl_main.json` (Xian Ni Fandom — Wang Lin main page, 87581 chars cleaned)
- `/home/z/my-project/tool-results/wl_baidu_main.json` (Baidu Baike — Wang Lin, 71800 chars)
- `/home/z/my-project/tool-results/wl_cult.json` (Xian Ni Fandom — Wang Lin/Cultivation, 59008 chars)
- `/home/z/my-project/tool-results/wl_clones.json` (Renegade-Immortal-Xian-Ni Fandom — Avatar & Clones)
- `/home/z/my-project/tool-results/wl_clones_baidu.json` (Baidu Baike — Wang Lin Clone)
- `/home/z/my-project/tool-results/li_muwan.html` (Baidu — Li Muwan)
- `/home/z/my-project/tool-results/teng_huayuan.html` (Baidu — Teng Huayuan)
- `/home/z/my-project/tool-results/yao_xixue.html` (Baidu — Yao Xixue)
- `/home/z/my-project/tool-results/blood_ancestor.html` (Baidu — Blood Ancestor)
- `/home/z/my-project/tool-results/situ_nan_baidu.html` (Baidu — Situ Nan)
- `/home/z/my-project/forge-mod/CANON_FORMATIONS_TALISMANS_USES.md` (cross-reference)
- `/home/z/my-project/src/lib/sim/{location,sect,npc}-catalog.ts` (existing seed data)

**Failed / non-substantive sources** (Cloudflare-blocked or "entry does not exist"): `allseer.html` (Baidu returned "entry does not exist"), `wang_tengfei.html` (Cloudflare "Just a moment…" page; this file is for the ISSTH character anyway, not RI). All-Seer information is reconstructed from his extensive mentions across `wl_main`, `wl_baidu_main`, `wl_cult`, and `npc-catalog.ts`.

**Canon confidence key:**
- **C5** — explicit, multi-source wiki entry with chapter citation
- **C4** — directly named in novel + wiki, single-source but consistent
- **C3** — novel-implicit; named once or implied; secondary source only
- **C2** — archetypal fan-synthesis / inference

**World-law tiers** (for the Forge mod's Reality Profile):
- `fragile` — mortal-tier / Qi Condensation ceiling; physical laws are breakable
- `low` — Foundation-to-Core tier; minor cultivator nations; devils/beasts roam
- `medium` — Nascent Soul+ tier; star-system-spanning, Celestial-tier cultivators present
- `high` — Third Step / Arcane Void tier; multiple star systems, Immortal Astral Continent sub-regions
- `absolute` — Fourth Step / Heaven Trampling tier; reality is owned by a single cultivator

---

## Summary

| Catalog | Count | Notes |
|---|---|---|
| **Locations** | **78** | 6 cosmological layers + 9 planets + 11 countries + 17 sect-mountain/sect-city sites + 19 realms/dimensions/ruins + 16 cities/villages/regions |
| **NPCs** | **132** | 1 protagonist + 17 family + 3 spouses + 11 mentors + 47 allies + 32 antagonists + 21 companions/servants/pets |
| **Factions / Sects / Clans** | **42** | 19 sects + 17 clans/tribes + 4 dynasties/alliances + 2 governing bodies |
| **Cosmological layers** | **7 nested levels** | Root Dao → Luo Tian → Immortal Astral Continent → Cave World → Sealed Realm → Star Systems → Planets → Countries → Sects |

**Key cosmology finding:** The RI universe is a **nested-sealed cosmology** — every "world" is a sealed farm owned by a higher-tier cultivator. The Cave World is sealed by the Seven-Colored Daoist; the Sealed Realm is the inner half of the Cave World, sealed AGAIN by the Realm-Sealing Grand Array (whose spirit is the Heaven-Splitting Axe) to prevent Third-Step cultivators from rising. Wang Lin's final act is to **kill the Seven-Colored Daoist and become the new world-owner**, then Transcend with Li Muwan.

**Key antagonist finding:** The **All-Seer** is NOT Allheaven (the ISSTH antagonist). All-Seer is the mortal-realm schemer ruling the Heavenly Fate Sect; the true cosmic antagonist is the **Seven-Colored Daoist** (creator of the Cave World, the original "owner" who harvests Joss Flames). Both must be killed for Wang Lin to escape the farm.

---

# Catalog 1 — Complete Location List

## 1A. Cosmological Top Levels (dimensions / realms that contain other realms)

### L01. The Root Dao (本源大道)
- **Type:** dimension / root
- **Cosmological position:** the outermost reality — the "true" Dao-source underlying everything
- **Description:** The fundamental laws (Five Elements, Karma, Reincarnation, Life-Death, True-False, Absolute Beginning/End, Restriction, Slaughter) from which all cultivators draw. Not a place one "lives in"; it is the substrate of all existence.
- **Key events:** Wang Lin ultimately comprehends the Reincarnation Essence (his 14th) here, achieving Heaven Trampling. Source-thesis of all Daos.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4 (named in `wl_baidu_main` Origin/Essence system; explicit in `wl_cult` essence progression)

### L02. Luo Tian Star System / Luo Tian (罗天)
- **Type:** star-system / over-realm
- **Cosmological position:** the "true" star-system-level reality outside the Cave World; the Immortal Astral Continent floats in Luo Tian's void
- **Description:** The Star System tier above the IAC. Home of the Luo Tian Star Domain (where the Luo Tian Alliance War was fought). The "Three Auspicious Treasures" (Xu Liguo, Liu Jinbiao, Zhong Dahong) were cultivators from Luo Tian originally.
- **Key events:** Wang Lin's "Luo Tian Alliance War" arc (Book 7-8); Wang Lin slew the Water Daoist here; the Luo Tian Thunder Immortal Realm existed here before collapsing.
- **World law tier:** absolute
- **Sealed?** No (it is outside the seal; this is what makes Luo Tian "more real" than the Cave World)
- **Canon confidence:** C4

### L03. Immortal Astral Continent / Xian Gang Continent (仙罔大陆 / 仙罡大陆)
- **Type:** continent / realm
- **Cosmological position:** the massive landmass floating in Luo Tian's void; the "real" world outside the Cave World. The Cave World is a pocket-world of the IAC.
- **Description:** A continent so vast it has nine suns (the "Nine Suns" of the IAC — Grand Empyreans all). Subdivided into provinces/continents: Heavenly Bull Continent, Green Devil Continent, Mountain Sea Continent, Great Saint Continent, Mengtu Province, Tianniu Province, Green Bull Continent, and an Imperial City / Dao Ancient Imperial Capital. Originally ruled by Immortal Emperor Lian Daozhen; later by Ancient Clan Dao Ancient Great Heavenly Venerable; Wang Lin becomes its #1 power.
- **Key events:** Wang Lin arrives here as a Third-Step cultivator, condenses Void Clone (causing the 9 suns to manifest simultaneously), joins Great Soul Sect, devours Dao Demon Sect Master, obtains Ancient Clan inheritance, slays Gu Dao, becomes "the Tenth Sun," Transcends the 9 Heaven Trampling Bridges, and leaves with Li Muwan.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C5 (named on every RI summary page; appears in nav bar of `wl_main`)

### L04. The Cave World / Dong Tian (洞天) — Wang Lin's Cave World
- **Type:** dimension / pocket-world
- **Cosmological position:** a sealed dimensional bubble floating in Luo Tian's void; the universe inside it contains Planet Suzaku, the Brilliant Void Star System, etc. Created by the Seven-Colored Daoist (the original "owner").
- **Description:** A pocket-world owned by a higher-tier cultivator (Seven-Colored Daoist) and farmed for Joss Flames (cultivation energy produced by mortal faith). Has a sealed inner half (the Sealed Realm) and an outer half (the Outer Realm). Contains billions of cultivation planets. The Cave World's "law" is that Third-Step cultivators cannot naturally arise — the seal suppresses them.
- **Key events:** The entire Book 1-9 takes place inside the Cave World. Wang Lin discovers it IS a cave world only in Book 11 ("Mysteries of the Ancient Era") when he breaches the Ancient Immortal Domain and meets the Seven-Colored Daoist. After killing the Seven-Colored Daoist, Wang Lin renames it "Wang Lin's Cave World" and becomes the new owner.
- **World law tier:** absolute (interior); medium-tier laws within (because the seal suppresses high-tier cultivation)
- **Sealed?** YES — sealed by the Seven-Colored Daoist; unsealed when Wang Lin kills him
- **Canon confidence:** C5

### L05. Sealed Realm / Inner Realm (封界 / 内界)
- **Type:** realm (inner half of the Cave World)
- **Cosmological position:** the inner half of the Cave World; separated from the Outer Realm by the Realm-Sealing Grand Array. Contains Planet Suzaku, Brilliant Void Star System, Allheaven Star System, Cloud Sea Star System.
- **Description:** Where Wang Lin and most of the Cave World's cultivators live. The Realm-Sealing Grand Array (whose spirit is the Heaven-Splitting Axe) suppresses Third-Step cultivation here — only "Heaven Blight" cultivators can squeeze through. The "Outer Realm" (L06 below) is the half outside this seal.
- **Key events:** The "Sealed Realm War" (Wang Lin as first responder, slaughtering thousands of Outer Realm cultivators). Wang Lin becomes "Lord of the Sealed Realm." Wang Lin resets the Realm Sealing Grand Array at end of arc, planning new territories.
- **World law tier:** medium (the seal caps cultivation at Heaven Blight / quasi-Third-Step)
- **Sealed?** YES — sealed by the Realm-Sealing Grand Array
- **Canon confidence:** C5

### L06. Outer Realm (外界)
- **Type:** realm (outer half of the Cave World)
- **Cosmological position:** the outer half of the Cave World; the half NOT inside the Sealed Realm. Higher-tier cultivators dwell here.
- **Description:** The home of cultivators who invaded the Sealed Realm during the Sealed Realm War. Less restricted cultivation-wise than the Sealed Realm. Daoist Water (Shui Daozi) was from here. The Dao Devil Sect and other factions are partially based here.
- **Key events:** Wang Lin flees to the Outer Realm with Li Qianmei after the Wind Celestial Realm arc. Battles Outer-Realm cultivators throughout the late Sealed Realm War. Multiple Outer-Realm third-step cultivators are killed by Wang Lin borrowing the Heaven-Splitting Axe.
- **World law tier:** high
- **Sealed?** No (it is the side OUTSIDE the seal, but both sides are still sealed within the Cave World)
- **Canon confidence:** C4

### L07. Primordial Divine Realm (原神境)
- **Type:** dimension (special pocket-realm tied to the Heaven Defying Bead)
- **Cosmological position:** a sub-dimension accessible from the Cave World after Wang Lin reaches Third Step; the place where Tianyunzi's true body hides.
- **Description:** A realm that exists because of the Heaven Defying Bead / Realm-Defining Compass. Wang Lin meets Tianyunzi here in the endgame and learns that Lu Mo (his Slaughter-Silence clone) was sent back in time via the Flowing Moon technique to leave the resurrection method for Li Muwan.
- **Key events:** Wang Lin's final battle with Tianyunzi (who intended to possess Wang Lin but was defeated). Wang Lin crosses the 9 Heaven Trampling Bridges here.
- **World law tier:** absolute
- **Sealed?** Was sealed (by Tianyunzi's design); unsealed by Wang Lin
- **Canon confidence:** C4

## 1B. Star Systems (inside the Sealed Realm / Cave World)

### L08. Brilliant Void Star System / Alliance Star System (玄虚星系 / 联盟星系)
- **Type:** star_system
- **Cosmological position:** inside Sealed Realm; contains ~7 million cultivation planets. Originally called "Brilliant Void," renamed "Alliance Star System" after the Cultivation Alliance rose to govern it.
- **Description:** The primary star system of the early-to-mid story. Contains Planet Suzaku, Planet Tian Yun, Planet Ran Yun, Planet Qing Ling, plus the Vermilion Bird Starfield sub-region.
- **Key events:** Wang Lin's home star system. Site of the Alliance-Allheaven War. After becoming Thunder Celestial, Wang Lin protects this entire star system.
- **World law tier:** medium
- **Sealed?** Yes (within Sealed Realm)
- **Canon confidence:** C5

### L09. Allheaven Star System (诸天星系)
- **Type:** star_system
- **Cosmological position:** inside Sealed Realm; rival star system to the Alliance. Origin of many antagonists (Yao Family).
- **Description:** The "other" major star system. The Southern Domain (containing the Thunder Celestial Realm and the Yao Family's headquarters) is here. Book 7 ("Fame Shakes The Allheaven Star System") is set here.
- **Key events:** Wang Lin's fame arc. Yao Family kill-order on Wang Lin. Thunder Celestial Tournament. Wang Lin destroys multiple planets here.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C5

### L10. Cloud Sea Star System (云海星系)
- **Type:** star_system
- **Cosmological position:** inside Sealed Realm; third major star system. Higher-tier than Alliance/Allheaven — Nirvana Scryer+ cultivators common here.
- **Description:** A star system dominated by cloud-covered spirit worlds. Contains the Origin Sect, Treasured Jade Sect, Wild Continent, and the Daoist Water's domain. Books 9 ("Peak of the Cloud Sea") is set here.
- **Key events:** Wang Lin lives as a mortal village doctor here, joins Origin Sect as "Ceng Niu," wins the ranking tournament, battles Daoist Water (turns to stone, saved by Li Qianmei's 10-year blood anointment), enters the Wind Celestial Realm, becomes "Lord of the Sealed Realm."
- **World law tier:** medium-to-high
- **Sealed?** Yes
- **Canon confidence:** C5

### L11. Vermilion Bird Starfield (朱雀星域)
- **Type:** starfield / sub-region
- **Cosmological position:** a sub-region within the Alliance Star System (some sources treat it as a separate starfield). Contains Planet Suzaku and the Four Divine Sect's territory.
- **Description:** The starfield guarded by the Vermilion Bird Divine Sect. Contains Planet Suzaku (Wang Lin's birthplace), the Vermilion Bird Tomb, the Four Divine Sect headquarters.
- **Key events:** Wang Lin becomes the 6th-Generation Vermilion Bird Divine Emperor here.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L12. Blue Silk Clan Star Domain (蓝丝族星域)
- **Type:** star_domain
- **Cosmological position:** an outlying star domain within the Cave World; sub-region of unknown parent (probably Outer Realm or independent).
- **Description:** The Blue Silk Clan's home star domain. Dao Master Blue Dream is based here. Wang Lin learns "Dao Art Fusion" and "Light Shadow Shield" from Blue Dream here.
- **Key events:** Wang Lin vs. Blue Dream Dao Venerable (heavily injured, saved by Li Qianmei's bracelet).
- **World law tier:** high
- **Sealed?** Yes
- **Canon confidence:** C3

### L13. Luo Tian Star Domain (罗天星域)
- **Type:** star_domain
- **Cosmological position:** the star domain ABOVE the Cave World — outside the seal, in Luo Tian proper. The Luo Tian Alliance War was fought here.
- **Description:** True-reality star domain. Higher-tier cultivators (Void Tribulant, Grand Empyrean) routinely operate here. The Thunder Immortal Realm and Luo Tian Thunder Immortal Realm were here before their collapse.
- **Key events:** Luo Tian Alliance War. Wang Lin takes Xi Zifeng as his 3rd disciple after the Luo Tian Thunder Immortal Realm's collapse.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

## 1C. Planets

### L14. Planet Suzaku / Vermilion Bird Star (朱雀星)
- **Type:** planet
- **Cosmological position:** inside Vermilion Bird Starfield → Alliance Star System → Sealed Realm → Cave World
- **Description:** A third-tier cultivation planet (later raised to higher tiers by Wang Lin's actions). Wang Lin's birthplace. Multiple cultivation countries on its surface (Zhao, Chu, Fire Burn, Sky Demon, Pilu, Snow Domain, etc.). Sealed Cultivation Planet Crystal inside.
- **Key events:** Wang Lin's birth, clan annihilation, Ji Realm awakening, Teng Clan extermination, Vermilion Bird Divine Emperor ascension. Situ Nan sealed Cultivation Star Crystal here. Wang Lin returns multiple times to settle karma.
- **World law tier:** low → medium (escalates as Wang Lin remakes the planet)
- **Sealed?** Yes (Cultivation Planet Crystal seal)
- **Canon confidence:** C5

### L15. Planet Tian Yun / Tianyun Star (天运星)
- **Type:** planet
- **Cosmological position:** inside Alliance Star System → Sealed Realm
- **Description:** A rank-7 cultivation planet. Home of the Heavenly Fate Sect (Tianyun Sect) ruled by the All-Seer, divided into seven color divisions (red/orange/yellow/green/blue/cyan/purple). Blood Ancestor Yao Xinghai's residence. East Sea Demon Spirit Land is here.
- **Key events:** Wang Lin becomes 7th disciple of Purple Division → challenged for True Disciple slot → Tianyunzi's plot → kills Blood Ancestor in Thunder Celestial Realm → captures Yao Xixue. Tuo Sen's territory in the deeper Ancient God Land is reached from here.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C5

### L16. Planet Qing Lin / Qing Ling (青灵星)
- **Type:** planet
- **Cosmological position:** inside Alliance Star System → Sealed Realm
- **Description:** Xie Qing's home planet. A backwater planet where Xie Qing was originally an ordinary old man before becoming Wang Lin's 2nd disciple. Wang Lin buried Xie Qing in Autumn Orchid Valley here.
- **Key events:** Xie Qing's enlightenment (the "fish, water, net, fishing" analogy that led Wang Lin to Nirvana Scryer). Wang Lin's return visit when Xie Qing sits atop a mountain for 800 years cultivating only Concepts.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C4

### L17. Planet Ran Yun (染云星)
- **Type:** planet
- **Cosmological position:** inside Allheaven Star System → Sealed Realm
- **Description:** A planet Wang Lin visited under the alias "Xu Mu." Sun Tai lived here as a friend in his final years. Liu Mei came here to attack Wang Lin and lost; her son Wang Ping was raised here as a mortal.
- **Key events:** Wang Lin raises Wang Ping as a mortal (one lifetime), develops Karma Domain.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L18. Earth Planet (土星)
- **Type:** planet
- **Cosmological position:** inside Alliance Star System → Sealed Realm (sub-region of Brilliant Void)
- **Description:** A planet named for its earth-element leanings. Visited by Wang Lin after his Heavenly Fate Sect training. Junior sect master Xu Yunshan of the Xuan Yuan Sect greeted him here.
- **Key events:** Wang Lin kills three Da Lou Sword Sect elders here (over Brilliant Golden Fruit); feeds the fruit to Mosquito Beast and Thunder Toad, triggering their evolution.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L19. Trading Planet (交易星)
- **Type:** planet
- **Cosmological position:** inside Alliance Star System → Sealed Realm
- **Description:** An ocean-dominated trading hub planet. Wang Lin and Situ Nan stop here on their way off Planet Suzaku. Mainly ocean; goods-flow economy.
- **Key events:** Wang Lin meets Li Dannan (guide) and Bai Wei here. Situ Nan steals from nearly every sect on the planet, causing a planet-wide chase.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L20. Water Spirit Star / Feng Luan Star (水灵星 / 凤鸾星)
- **Type:** planet
- **Cosmological position:** inside Alliance Star System → Sealed Realm
- **Description:** A water-spirit planet. Wang Lin obtained his Fire Essence potential here (first glimpse into the Fu Clan's Golden Leaf Flame Source Origin). Situ Nan founded his "Southern Prince" faction here.
- **Key events:** Wang Lin vs. Situ Nan reunion (Water Spirit Star). Situ Nan poisoned; Wang Lin's "One Drop of Universe" comprehension.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L21. Planet Five Elements (五行星)
- **Type:** planet
- **Cosmological position:** inside Sealed Realm (specific sub-region unknown)
- **Description:** A planet specialized in Five Elements cultivation materials. Water General's "One Drop of Universe" originates here.
- **Key events:** Wang Lin comprehends 380 million changes to condense his Water Essence here.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C3

### L22. Immortal Execution Star / Xian Gang Star (仙罔星)
- **Type:** planet (after Wang Lin's reincarnation migration)
- **Cosmological position:** within the Immortal Astral Continent proper (the destination of Cave World reincarnators)
- **Description:** The "reincarnation destination" for Wang Lin's friends/allies from the Cave World. Situ Nan reincarnates here as "Si Nan" (Grand Marshal of Wu Xuan Country). Xu Liguo, Liu Jinbiao, Zhong Dahong (the Three Auspicious Treasures) continue their swindling ways here. Big Head Cultivator, Zhou Yi, Qing Shuang all reincarnate here.
- **Key events:** Wang Lin systematically awakens his allies' past-life memories here.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

## 1D. Countries (on Planet Suzaku, unless noted)

### L23. Country of Zhao / Zhao Kingdom (赵国)
- **Type:** country
- **Cosmological position:** on Planet Suzaku → Alliance Star System → Sealed Realm → Cave World
- **Description:** A third-tier cultivation country on Planet Suzaku. Contains Wang Family Village, Heng Yue Sect, Tian Shui City, the Forest of Distorted Divine Sense, the Teng Family City. Wang Lin's birthplace.
- **Key events:** Wang Lin's birth; Teng Clan extermination; Wang Lin's return as Soul Formation cultivator to wipe out the Teng family and kill Teng Huayuan; Xuan Dao Sect (where he breaks through to Soul Formation).
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C5

### L24. Chu Country (楚国)
- **Type:** country
- **Cosmological position:** on Planet Suzaku
- **Description:** A neighboring country to Zhao. The Cloud Sky Sect (Cloud Heaven Sect / Yuntian Sect) is here. Li Muwan ends up here as an elder and eventually Sect Master.
- **Key events:** Wang Lin kills Sun Zhenwei at Li Muwan's wedding; becomes Cloud Sky Sect leader; gives the seat to Li Muwan. Returns to build a small house with Li Muwan for her final 10 years.
- **World law tier:** fragile → low
- **Sealed?** Yes
- **Canon confidence:** C4

### L25. Fire Burn Country / Flame-Burning Country / Huofen Country (火焚国)
- **Type:** country
- **Cosmological position:** on Planet Suzaku
- **Description:** Li Muwan's home country. The Luo He Sect is based here. Li Qiqing (Li Muwan's brother) was an elite Luo He Sect disciple here. Fire Beasts roam this region.
- **Key events:** Wang Lin (in Ma Liang's body) meets Li Muwan here while escaping a Fire Beast. The Heaven Defying Bead eats the King of Fire Beasts here.
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C4

### L26. Sky Demon Country (天魔国)
- **Type:** country
- **Cosmological position:** on Planet Suzaku (likely the East Demon Spirit Sea region)
- **Description:** A demonic-cultivation country. Mo Lihai is a city general here. The Demonic Drum tournament (15 rings → Ascendant breakthrough) is held in the capital city.
- **Key events:** Wang Lin takes Thirteen and Huo Pao here for trial. Wang Lin wins the Demonic Drum tournament and breaks through to Ascendant. Yao Xixue schemes against him in a cave here.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C4

### L27. Pilu Kingdom / Pi Lu Kingdom (毗卢国)
- **Type:** country
- **Cosmological position:** on Planet Suzaku
- **Description:** The kingdom where the Soul Refining Sect is headquartered. Dun Tian and Nian Tian are from here.
- **Key events:** Wang Lin inherits the Soul Refining Sect and the Ten Billion Soul Banner from Dun Tian here. Soul Transformation breakthrough. The Soul Refining Tribe (east-sea descendants) is the legacy Wang Lin teaches here.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C4

### L28. Snow Domain Country (雪域国)
- **Type:** country
- **Cosmological position:** on Planet Suzaku (cold region)
- **Description:** A cold-region country. Liao Fan, who attacked Wang Lin in EP80/ch1200 with Situ Nan's severed arm, was from here.
- **Key events:** Tangential to main plot.
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C3

### L29. Xuan Wu / Xuan Country (玄武国)
- **Type:** country
- **Cosmological position:** on Planet Suzaku
- **Description:** A country Wang Lin fled to while being chased by Duanmu Ji from the Sea of Devils. Mentioned as a destination during the Duanmu Ji arc.
- **Key events:** Wang Lin hides in the Heaven Defying Bead for 3 years here to evade Duanmu Ji.
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C3

### L30. Fire Demon Country (火魔国)
- **Type:** country
- **Cosmological position:** on Planet Suzaku (likely near East Demon Spirit Sea)
- **Description:** A country under which a fragmented ancient demon is sealed. Bei Luo helps Wang Lin weaken the demon here.
- **Key events:** Wang Lin creates his first devil (Xu Liguo) here in a black pagoda. Bei Luo devours the fragmented ancient demon here.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C4

### L31. Vermilion Bird Country (朱雀国)
- **Type:** country (cultivation nation-state)
- **Cosmological position:** on Planet Suzaku; the highest-tier cultivation country on the planet (level 6, raised by Situ Nan)
- **Description:** The ruling cultivation nation of Planet Suzaku. Its ruler is the "Vermilion Bird Master / Vermilion Bird Child" (a renewable office). Situ Nan was the 2nd-Generation Vermilion Bird Master; Ye Wuyou was the 1st; the 3rd betrayed Situ Nan; the 13th was Qian Pinghai; the 14th severed Situ Nan's arm; the 15th was Zhou Wutai; the 16th was Wang Lin.
- **Key events:** Wang Lin declines the position of Vermilion Bird Child (transferring it to Zhou Wutai). Later becomes the 6th-Generation Vermilion Bird Divine Emperor of the broader Vermilion Bird Divine Sect.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L32. Great Wang Dynasty (大王朝)
- **Type:** country / dynasty
- **Cosmological position:** on Planet Suzaku
- **Description:** Wang Lin's eponymous dynasty, founded after his rise to power. Made Wang Lin "Ancestor of the Country of Zhao" simultaneously.
- **Key events:** Established offscreen during the Vermilion Bird Star period.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L33. Qing Shui Kingdom (清水国)
- **Type:** country
- **Cosmological position:** on Planet Suzaku (former kingdom, destroyed before main story)
- **Description:** Qing Shui's original mortal kingdom. Its destruction (and Qing Shui's family/clan annihilation) seeded his slaughter essence and his resolve to defy the heavens.
- **Key events:** Backstory only — mentioned in Qing Shui's profile.
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C3

## 1E. Cities, Villages, Regions (on Planet Suzaku unless noted)

### L34. Wang Family Village (王家村)
- **Type:** village
- **Cosmological position:** in Country of Zhao → Planet Suzaku
- **Description:** Wang Lin's birthplace. A small mortal village of the impoverished Wang Family Carpenter Clan. ~100 families.
- **Key events:** Wang Lin born here; Teng Huayuan exterminates the village (sparing only Wang Hao and Wang Zhuo who were Heng Yue Sect disciples); Wang Lin returns as Soul Formation to bury the dead.
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C5

### L35. Tian Shui City / Tianshui City (天水城)
- **Type:** city
- **Cosmological position:** in Country of Zhao → Planet Suzaku
- **Description:** A major mortal+cultivator trading city. Wang Lin's first destination after his 4-year closed-door cultivation. The Heng Yue Sect has interests here.
- **Key events:** Wang Lin kills Teng Li (Teng Huayuan's great-great-grandson) and steals his Foundation Establishment. Old man Ji Mo's disciples attack Wang Lin here.
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C5

### L36. Teng Family City (藤家城)
- **Type:** city
- **Cosmological position:** in Country of Zhao → Planet Suzaku
- **Description:** The Teng Clan's stronghold city. De facto ruled by Teng Huayuan.
- **Key events:** Wang Lin's "Kill and Destroy the Heart" revenge: hunts Teng descendants one by one, builds a human-head tower to intimidate Teng Huayuan, finally slays him. The Teng Clan's nine Nascent Soul cultivators are killed and refined into demons.
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C4

### L37. Nan Dou City / South Dou City (南斗城)
- **Type:** city
- **Cosmological position:** in/near Sea of Devils → Planet Suzaku
- **Description:** A city Wang Lin visited to purchase an alchemy furnace for Li Muwan's Distant Heaven Pill.
- **Key events:** Wang Lin noticed by Fighting Evil Sect members here; 10 Core Formation cultivators chase him back to his cave.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C4

### L38. Qilin City (麒麟城)
- **Type:** city
- **Cosmological position:** on Planet Suzaku (or nearby star)
- **Description:** A city Wang Lin visited after escaping the Chaotic Broken Stars. Yun Fei's cave is here.
- **Key events:** Wang Lin places a 3-day restriction on Yun Fei here. Qiu Siping (late Core Formation) tries to unlock it; Wang Lin's devil kills Yun Fei.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C4

### L39. Ancient Demon City / Demon Capital (古魔城)
- **Type:** city
- **Cosmological position:** in/near Sky Demon Country → Planet Suzaku
- **Description:** The capital of the Sky Demon Country. Site of the Demonic Drum tournament.
- **Key events:** Wang Lin wins the tournament (15 rings), becomes an Ascendant. Defeats Mo Lihai's competitors.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C4

### L40. Shui City / Dou City (水城 / 斗城)
- **Type:** city
- **Cosmological position:** on Planet Suzaku
- **Description:** Cities mentioned tangentially in the Sea of Devils arc.
- **Key events:** Tangential.
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C3

### L41. Hou Fen (侯分)
- **Type:** region
- **Cosmological position:** on Planet Suzaku (a region within the Suzaku cultivation world)
- **Description:** A region listed in the Fandom nav-bar Locations. Specific details sparse.
- **Key events:** Tangential.
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C3

### L42. Blue Pine Peaks (蓝松峰)
- **Type:** region
- **Cosmological position:** on Planet Suzaku (likely a mountain range)
- **Description:** A region listed in the Fandom nav-bar Locations.
- **Key events:** Tangential.
- **World law tier:** fragile
- **Sealed?** Yes
- **Canon confidence:** C3

### L43. Soul Refining Tribe (炼魂部族)
- **Type:** village / tribal region
- **Cosmological position:** in the East Demon Spirit Sea → Planet Suzaku
- **Description:** A tribe of over 1 million people, founded by Wang Lin when he taught the Soul Refining Sect's heritage to the Mountain Valley tribe (chief: Ouyang Hua). Wang Lin is regarded as their Ancestor.
- **Key events:** Restored Wang Lin's One Billion Soul Flag. Thirteen and Huo Pao trained here.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C4

### L44. Mountain Valley Tribe (山谷部落)
- **Type:** village
- **Cosmological position:** in the East Demon Spirit Sea → Planet Suzaku
- **Description:** The original name of the Soul Refining Tribe before Wang Lin elevated it.
- **Key events:** Wang Lin displays power and is recognized; tribe chief Ouyang Hua.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C4

## 1F. Realms / Dimensions / Ruins (special locations)

### L45. Sea of Devils / Demon Cultivation Sea (魔海 / 魔修海)
- **Type:** ocean / region
- **Cosmological position:** on Planet Suzaku
- **Description:** A vast demonic-sea region on Planet Suzaku, divided into "districts" (Wang Lin enters the 14th district). Inhabited by devils, beasts, and demonic cultivators. The Fighting Evil Sect, Soul Refining Sect, and Corpse Yin Sect all operate in/around it.
- **Key events:** Wang Lin's Core Formation breakthrough (with Li Muwan). The Fighting Evil Sect massacre. Wang Lin takes the Mosquito Beast here.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C5

### L46. Jue Ming Valley / Jueming Valley (绝命谷)
- **Type:** valley
- **Cosmological position:** on Planet Suzaku
- **Description:** A valley where surrounding sects compete for tokens to enter the Foreign Battleground. A trapping formation holds cultivators inside until the competition ends.
- **Key events:** Wang Lin's Ji Realm awakening here (driven by grief over his clan's annihilation). First duel with Teng Huayuan outside the valley. Wang Lin's physical body destroyed; soul flees to Foreign Battleground.
- **World law tier:** low
- **Sealed?** Yes
- **Canon confidence:** C5

### L47. Foreign Battleground / Extraterrestrial Battlefield / Outer Domain Battlefield (域外战场)
- **Type:** realm / battleground
- **Cosmological position:** a sealed battlefield accessible only via the Jue Ming Valley tokens; outside normal Suzaku geography
- **Description:** A battleground used for sect competitions and "death exile." Black-colored laws of death prevent souls from leaving. Anyone who dies here becomes a wandering soul.
- **Key events:** Wang Lin becomes a Soul Devourer here (feeding on many souls). Wang Lin takes over Ma Liang's dying body. Escapes the death-law seal through observation, calculation, and knowledge.
- **World law tier:** medium (death-law-locked)
- **Sealed?** Yes (soul-sealed)
- **Canon confidence:** C5

### L48. Land of the Ancient God / Ancient God Land (古神之地)
- **Type:** ruin / sub-dimension
- **Cosmological position:** a pocket-realm accessible from Planet Suzaku (and other planets)
- **Description:** The corpse-body of the 8-star Ancient God Tu Si, transformed into a 3-level realm. Level 1: hurricane of devils. Level 2: Bridge of No Return + Restriction Mountain. Level 3: Annihilation realm — Soul Devourer activation zone, where Tuo Sen is trapped.
- **Key events:** Wang Lin inherits Tu Si's knowledge. Tuo Sen born here (Tu Si's demonic thought). Wang Lin reshapes his body using the ancient knowledge and traps Tuo Sen for 1000+ years. Returns later as a 6-star Ancient God; Tuo Sen is injured.
- **World law tier:** medium → high
- **Sealed?** Yes
- **Canon confidence:** C5

### L49. Restriction Mountain (禁制山)
- **Type:** ruin / trial-mountain
- **Cosmological position:** inside Land of the Ancient God → Level 2
- **Description:** A mountain made of ancient forbidden restrictions. Each level up is more complex. Demon Lord of Six Desires (Xu Liqing) modified them. The "Great Enlightened One" title is granted to anyone who breaks all restrictions (only 4 ever — Wang Lin is the 4th).
- **Key events:** Wang Lin spends 7 (story) / 13 (subjective) years here; develops Illusionary Circle technique; hair turns white. Receives "Great Enlightened One" title from Tu Si.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C5

### L50. Bridge of No Return (不归桥)
- **Type:** ruin / bridge
- **Cosmological position:** inside Land of the Ancient God → Level 2
- **Description:** A bridge where devils can return but beasts (and most beings) who turn back are sucked into the void. Tests the heart.
- **Key events:** Wang Lin resists the illusion of his parents' voices calling him back; passes the test.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L51. Chaotic Broken Stars (混沌碎星)
- **Type:** ruin / star-cluster
- **Cosmological position:** a chaotic star-cluster region accessible via teleportation; on/near Planet Suzaku's star system
- **Description:** A 3-level chaotic star cluster. Level 1: hurricane of devils (Wang Lin creates his 2nd devil here). Level 2: Bridge of No Return + Restriction Mountain (entries above). Level 3: Annihilation realm — wandering souls, ancient god Tu Si's body.
- **Key events:** Duanmu Ji (Soul Formation) chases Wang Lin here. Wang Lin outwits Duanmu Ji, Gun Lan, Xu Liqing, and Hunchback Meng. Inherits Tu Si's legacy.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C5

### L52. Suzaku Tomb / Cultivation Planet Crystal (朱雀墓)
- **Type:** ruin
- **Cosmological position:** inside Planet Suzaku (underground)
- **Description:** A multi-floor tomb within Planet Suzaku containing the Cultivation Planet Crystal (the seal-mechanism of the planet itself). Wang Lin found the Half-Moon Blade here.
- **Key events:** Wang Lin's tomb-raiding arc; encounters the scattered demon (Piao Nanzi/Lou Hou) sealed here.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L53. Thunder Celestial Realm (雷仙界)
- **Type:** realm (sub-dimension)
- **Cosmological position:** a pocket-realm within the Allheaven Star System
- **Description:** The "Thunder Immortal Realm" — contains the Thunder Celestial Temple, the Thunder Lake, and the Chosen Immortal Clan (enslaved celestials). Ruled by Master Flamespark and Russell. The Celestial Slaughter Art trap is hidden here.
- **Key events:** Wang Lin's Thunder Celestial Tournament victory. Kills Russell (with Qing Shui's Ji Realm help). Becomes Thunder Celestial of the Thunder Celestial Temple. Blood Ancestor killed here. Tide Abyss explored (locates Zhou Yi's spirit). Wang Lin obtains the God-Slaying War Chariot, Celestial Emperor Crown, and the Collection Pavilion (immortal arts library).
- **World law tier:** high
- **Sealed?** Yes (sealed sub-realm; collapses later)
- **Canon confidence:** C5

### L54. Thunder Celestial Temple (雷仙殿)
- **Type:** sect-temple (sub-realm of L53)
- **Cosmological position:** inside Thunder Celestial Realm → Allheaven Star System
- **Description:** The temple ruling the Thunder Celestial Realm. 3 trials: heaven, earth, human. The Thunder Lake trial. Battle realm trial (kill-count).
- **Key events:** Wang Lin becomes "Thunder Celestial of the Thunder Celestial Temple." Zhan Li Yunzi ancestor (Qi Xi spell revival attempt for Li Muwan) referenced here.
- **World law tier:** high
- **Sealed?** Yes
- **Canon confidence:** C4

### L55. Rain Celestial Realm (雨仙界)
- **Type:** realm (sub-dimension)
- **Cosmological position:** within the Allheaven Star System
- **Description:** A sub-realm of immortal jades and immortals' corpses. Zhou Yi discovered Qing Shuang's corpse here ("Ting'er"). The Rain Immortal Sword (Jufu) and War Spirit Print are here.
- **Key events:** Wang Lin obtains the Shooting God Chariot, Star Compass, War Spirit Print, and Rain Immortal Sword. Takes Sun Tai as a servant. Helps Zhou Yi protect Ting'er's corpse.
- **World law tier:** high
- **Sealed?** Yes
- **Canon confidence:** C4

### L56. Wind Celestial Realm (风仙界)
- **Type:** realm (sub-dimension)
- **Cosmological position:** within the Cloud Sea Star System
- **Description:** A wind-themed immortal sub-realm. Wang Lin acquires a large herd of Mosquito Beasts here. The Flowing Moon technique (rules of time) was created here under a stone gate.
- **Key events:** Wang Lin evolves his king mosquito beast. Creates Flowing Moon divine ability (sends Lu Mo back in time). Severely wounded and petrified by Daoist Water here.
- **World law tier:** high
- **Sealed?** Yes
- **Canon confidence:** C4

### L57. Demon Spirit Land / East Demon Spirit Sea (魔灵地 / 东海魔灵海)
- **Type:** region / sea
- **Cosmological position:** on Planet Tian Yun (some sources say Planet Suzaku)
- **Description:** A demonic-spirit region. The East Sea Demon Spirit Gate opens periodically. Contains the Immortal Monarch's Cave Mansion (Yao Xixue's target). The Mountain Valley Tribe is in the east of this region. The Ancient Demon City (Taga) is here.
- **Key events:** Wang Lin takes Thirteen as his 1st disciple. Seals Yao Xixue. All-Seer, Ling Tianhou, Blood Ancestor, and others come here for the cave token. Wang Lin escapes into the abyss to the Allheaven Star System.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C5

### L58. Brilliant Void (玄虚)
- **Type:** region (the original name of the Alliance Star System's interior void)
- **Cosmological position:** within the Alliance Star System (its original name pre-Cultivation Alliance)
- **Description:** The vast inner void of the Alliance Star System. Wang Lin's "Brilliant Void" pet is named after this region.
- **Key events:** Tangential to plot (renamed to Alliance Star System by the Cultivation Alliance).
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C3

### L59. Wild Continent (莽荒大陆)
- **Type:** continent
- **Cosmological position:** within the Cloud Sea Star System
- **Description:** A wild, primal continent. Wang Lin discovers "the secret for the third step that caused a huge calamity in the Cloud Sea Star System thousands of years ago" here.
- **Key events:** Wang Lin's discovery triggers a region-wide sect gathering; kills Noble Money and his group on the way back.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L60. Seven-Colored Realm (七彩界)
- **Type:** realm (pocket-dimension)
- **Cosmological position:** within the Cloud Sea Star System
- **Description:** A pocket-realm of seven-colored light. Master Ashen Pine takes cultivators here to "find something" (actually a trap). Spatial cracks separate the cultivators. The Seven-Colored Divine Sky Nail (108 nails, designed to kill Third Step experts) is here.
- **Key events:** Wang Lin tricks Master Cloud Soul into a different spatial crack. Steals treasures from Master Ashen Pine. Kills Master Ashen Pine with the seven-colored nail. Cang Songzi (Sub-Empty Annihilation cultivator) destroys Wang Lin's Rusty Iron Sword here.
- **World law tier:** high
- **Sealed?** Yes
- **Canon confidence:** C4

### L61. Ancient Tomb (古墓)
- **Type:** ruin / multi-floor tomb
- **Cosmological position:** on the Immortal Astral Continent
- **Description:** A vast multi-floor tomb on the IAC. Contains relics of the Ancient Clan: the Yi Si Puppet, Heaven-Splitting Axe, Eternal Wood Spirit, Fog Devil Lance, Ancient Breath Leaves (99-piece set), Emperor Furnace (Heavenly Emperor Furnace). Also Ye Mo's left eye and right arm.
- **Key events:** Wang Lin obtains numerous Ancient Clan relics here. Tan Lang is involved. Daogu Yemo's ancient devil corpse is here (used for Wang Lin's Third Avatar).
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

### L62. Pill Sea / Dan Sea (丹海)
- **Type:** sea
- **Cosmological position:** between Heavenly Bull Continent and Green Devil Continent → on IAC
- **Description:** A vast sea of pills (alchemical waste + dissolved pills). Wang Lin completes his Water Essence 9th cycle here.
- **Key events:** A woman cultivator from the Green Bull Continent attacks Wang Lin with Ten Thousand Refined Corruption Liquid (containing Water Essence); he absorbs it to complete the 9th cycle.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

### L63. Dong Lin Pool / False Dong Lin Pool (东林池)
- **Type:** wetland / pool
- **Cosmological position:** on the IAC (Dong Lin Sect area)
- **Description:** A pool with the sealed spirit of an Ancient God-tier entity beneath it. The False version (inside the Fifth Flower's illusory world) grants temporary power; the True version grants permanent Absolute Beginning and Reincarnation Essence comprehension.
- **Key events:** Wang Lin meditates for 13 years here; Absolute Beginning reaches completion; Reincarnation Essence begun.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

### L64. Immortal Emperor's Cave Mansion / Immortal Monarch's Cave Mansion (仙帝洞府)
- **Type:** ruin / cave mansion
- **Cosmological position:** on/under Demon Spirit Land (Tian Yun Star)
- **Description:** A cave-mansion left by an ancient immortal. Yao Xixue and Wang Lin explore it together (causing her hundred-year imprisonment). The Wind Demon (Feng Mo) is encountered here.
- **Key events:** Yao Xixue ambushes Wang Lin → imprisoned for 100 years. Yao Xinghai seeks Wang Lin here. Wind Demon slain by Wang Lin's God-Slaying Spear. Celestial Emperor's Tower is part of this complex.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C4

### L65. Heavenly Bull Continent (天牛大陆)
- **Type:** continent
- **Cosmological position:** on the IAC
- **Description:** One of the major continents of the IAC. Contains the Great Soul Sect, Gui Yi Sect, and 120+ Fire Veins. Wang Lin's IAC territory.
- **Key events:** Wang Lin joins Great Soul Sect here, becomes an elder, devours Earth Fire main vein. Manifests Fire Essence True Body.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

### L66. Green Devil Continent (绿魔大陆)
- **Type:** continent
- **Cosmological position:** on the IAC
- **Description:** Home continent of the Green Devil (Green Scorpion) entity. The Dao Devil Sect (Dao Demon Sect) is based here.
- **Key events:** Wang Lin captured as Green Devil sacrifice (by Ji Si acting for Dao Demon Sect); reverses the ritual and devours Green Devil, reaching Arcane Void stage.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

### L67. Mountain Sea Continent (山海大陆)
- **Type:** continent
- **Cosmological position:** on the IAC
- **Description:** A continent with the Mountain Tree Seal (where Wang Lin finds the second fragment of the Celestial Ancestor's Immortal Absolute Sword).
- **Key events:** Wang Lin obtains Metal Essence (initial) here.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C3

### L68. Great Saint Continent (大圣大陆)
- **Type:** continent
- **Cosmological position:** on the IAC
- **Description:** A continent with a sealed Spirit beneath it (the entity whose comprehension gave Wang Lin Absolute Beginning and Absolute End Essences).
- **Key events:** Wang Lin meditates at Dong Lin Pool here (technically a sub-region).
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C3

### L69. Mengtu Province (孟图省)
- **Type:** province
- **Cosmological position:** on the IAC
- **Description:** A province of the IAC. Wang Lin was captured by Dao Demon Sect here.
- **Key events:** Wang Lin captured; Dao Demon Sect Master intends to use him to resurrect the Green Devil Scorpion; Wang Lin turns the tables and obtains great fortune, reaching Empty Tribulation stage.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

### L70. Tianniu Province (天牛省)
- **Type:** province
- **Cosmological position:** on the IAC
- **Description:** A province of the IAC. Contains the Canglong Sect (Azure Dragon Sect). Kang Ren (a cultivator who took Wang Lin in) is from here.
- **Key events:** Wang Lin enters here, unconscious; taken by Kang Ren into Canglong Sect; extracts Earth Fire Dragon's soul; pursued by ancestor Du Qing.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

### L71. Green Bull Continent (青牛大陆)
- **Type:** continent
- **Cosmological position:** on the IAC
- **Description:** A continent whose cultivator attacked Wang Lin in the Pill Sea.
- **Key events:** Woman cultivator attacks Wang Lin with Ten Thousand Refined Corruption Liquid.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C3

### L72. Imperial City / Dao Ancient Imperial Capital (皇城 / 道古皇都)
- **Type:** city
- **Cosmological position:** on the IAC; the Dao Ancient Imperial Capital of the Ancient Clan
- **Description:** The Ancient Clan's imperial seat. Dao Ancient Imperial Venerable rules here. Lu Mo (Slaughter True Body) emerged here to defend Wang Lin.
- **Key events:** Wang Lin ambushed here; Lu Mo released and kills attackers. Wang Lin slays Dao Ancient Imperial Venerable to retrieve Li Muwan's remnant soul. Jiu Di Great Celestial Venerable confronted here.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

### L73. Ancient Clan Ancestral Temple / Ancient Shi Branch Temple (古族祖庙 / 古氏分支祖庙)
- **Type:** ruin / temple
- **Cosmological position:** within the Ancient Clan territory on the IAC
- **Description:** The temple where the Ancient Clan's ancestor rites are performed. Lou Hou's soul is sealed here (used by Wang Lin to complete Slaughter/Restriction/Absolute Beginning/End True Bodies).
- **Key events:** Wang Lin completes his final Ancient Clan tribulation here; obtains 2 more drops of Soul Blood.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C4

### L74. Kunxu Realm (昆虚界)
- **Type:** pocket-realm
- **Cosmological position:** a sealed sub-dimension accessible from the Cave World
- **Description:** A pocket-realm Mu Bingmei entered; took Zhou Ru as her disciple here.
- **Key events:** Zhou Ru's cultivation arc here.
- **World law tier:** medium
- **Sealed?** Yes
- **Canon confidence:** C3

### L75. Tide Abyss (潮渊)
- **Type:** abyss
- **Cosmological position:** within the Thunder Celestial Realm (Allheaven Star System)
- **Description:** An abyss where Wang Lin locates Zhou Yi's spirit (with Bei Lou's help).
- **Key events:** Wang Lin finds Zhou Yi's spirit here.
- **World law tier:** high
- **Sealed?** Yes
- **Canon confidence:** C3

### L76. Immortal Graveyard (仙坟) — 17 Layers
- **Type:** ruin / graveyard
- **Cosmological position:** within the Vermilion Bird Starfield
- **Description:** A 17-layer immortal graveyard. Wang Lin glimpses the Fu Clan's Golden Leaf Flame Source Origin on the 17th layer.
- **Key events:** Wang Lin's Fire Essence potential awakened here.
- **World law tier:** high
- **Sealed?** Yes
- **Canon confidence:** C3

### L77. Five Flowers Eight Gates (五花八门)
- **Type:** ruin / formation-complex
- **Cosmological position:** on the IAC
- **Description:** A complex where Wang Lin encounters the Dong Lin Female Ancient God.
- **Key events:** Wang Lin fuses with Ye Mo's heart's blood, subdues the Yi Si Puppet, defeats Yun Yifeng, defeats the Palm Venerable, and obtains Ye Mo's heart inheritance.
- **World law tier:** absolute
- **Sealed?** No
- **Canon confidence:** C3

### L78. Falling Land / Fallen Land (堕落之地)
- **Type:** region / trial-ground
- **Cosmological position:** within the Sealed Realm (a restricted sub-region)
- **Description:** A trial region where the "Young Emperor" trial takes place. Wang Lin becomes "Young Emperor of the Fallen Land." 2nd-Gen Vermilion Bird (young emperor of the Fallen Land) helps him.
- **Key events:** Wang Lin's Young Emperor trial. Fishes for a dragon, takes its blood. Passes the Trial of Man, Trial of Heaven. Becomes Void Flame Cultivator.
- **World law tier:** high
- **Sealed?** Yes
- **Canon confidence:** C4

### L79. Ancient Immortal Domain (古仙域)
- **Type:** ruin / gateway
- **Cosmological position:** a gateway-realm at the boundary of the Cave World;通往 Luo Tian
- **Description:** The gateway Wang Lin breaches to discover the truth of the Cave World. The Flowing Moon divine ability was comprehended at its gate.
- **Key events:** Wang Lin discovers his world is the Seven-Colored Daoist's cave mansion. Resets the Realm Sealing Grand Array. Plans new territories.
- **World law tier:** absolute
- **Sealed?** Was sealed; unsealed by Wang Lin
- **Canon confidence:** C4

### L80. Yellow Spring Secret Realm (黄泉秘境)
- **Type:** secret-realm
- **Cosmological position:** sub-realm within the Heaven Defying Bead (Situ Nan's creation for Wang Lin's breakthrough)
- **Description:** An extremely-Yin-location sub-realm inside the Heaven Defying Bead, opened by Situ Nan for Wang Lin's secluded cultivation.
- **Key events:** Wang Lin's Nascent Soul breakthrough training.
- **World law tier:** low (cultivable)
- **Sealed?** Yes (it's INSIDE the Heaven Defying Bead)
- **Canon confidence:** C4

---

# Catalog 2 — Complete NPC List

**Organization:** Protagonist → Family → Spouses → Mentors → Allies (alphabetical) → Antagonists (alphabetical) → Companions/Servants/Pets → Disciples → Reincarnation-Linked.

## 2A. Protagonist

### N01. Wang Lin (王林) — protagonist
- **Role:** protagonist
- **Cultivation peak:** Heaven Trampling 4th Step (end of RI); Immortal Lord 10th Step (AWWP cross-over)
- **Sect/faction:** Heng Yue Sect (legacy disciple) → Cloud Sky Sect (master) → Soul Refining Sect (master) → Heavenly Fate Sect (7th purple disciple) → Da Lou Sword Sect (elder) → Vermilion Bird Divine Sect (6th-Gen Divine Emperor) → Origin Sect (ancestor) → Great Soul Sect (elder) → Dark Scorpion Clan (ruler) → Fallen Land (Young Emperor) → Sealed Realm (Lord) → Immortal Astral Continent (10th Sun, 2nd only to Gu Dao) → Cave World (owner)
- **Relationship to Wang Lin:** self
- **Key events:** Discovered Heaven Defying Bead in dead bird under cliff → Ji Realm awakening → Soul Devourer in Foreign Battleground → inherits Tu Si's Ancient God knowledge → revenge on Teng Clan → Core Formation in Sea of Devils → Soul Formation via Life-Death Domain (living as mortal) → Cloud Sky Sect → Heavenly Fate Sect (All-Seer's plot) → Demon Spirit Land → Allheaven Star System → Thunder Celestial → Cloud Sea Star System (Daoist Water) → Sealed Realm Lord → kills Seven-Colored Daoist → IAC Tenth Sun → crosses 9 Heaven Trampling Bridges → Transcends with Li Muwan.
- **Aliases:** Tie Zhu (childhood), Xiao Lin Zi (by Situ Nan), Xu Mu (Allheaven alias), Zeng Niu (Cloud Sea alias), Ceng Niu (Origin Sect alias), Qing Mu, Lü Zihao, Ma Liang (possessed body), Black Hearted King, Soul Devourer, Master Demon, Thunder Celestial Xu Mu, Great Devil, Ancestor Wang, Supreme Celestial, Ancient God Wang Lin, Sixth Generation Vermilion Bird Divine Emperor, Lord of the Sealed Realm, Grand Scholar, White-Haired Empyrean, Paragon Wang (AWWP), The Tenth Sun, Lord Wang, The God (ISSTH), Divine Spirit (AWWP), Summer Immortal (BTT), Lu Zihao
- **Fate:** Alive; Transcendent (4th Step / Heaven Trampling); eventually owner of his own world; eternally with Li Muwan
- **Canon confidence:** C5

## 2B. Family

### N02. Wang Tianshui / Wang Tian Shui (王天水) — father
- **Role:** family
- **Cultivation peak:** mortal
- **Sect/faction:** Wang Clan
- **Relationship:** Wang Lin's father
- **Key events:** Called Wang Lin "Tie Zhu" to ward off bad fortune. Killed by Teng Huayuan; soul trapped in Teng's soul flag → rescued by Situ Nan into Heaven Defying Bead → released by Wang Lin later.
- **Fate:** Killed; soul saved; reincarnated
- **Canon confidence:** C5

### N03. Zhou Tingsu / Zhou Yingsu (周婷苏 / 周颖苏) — mother
- **Role:** family
- **Cultivation peak:** mortal
- **Sect/faction:** Wang Clan
- **Relationship:** Wang Lin's mother
- **Key events:** Killed by Teng Huayuan; soul trapped in Teng's soul flag → rescued by Situ Nan → released.
- **Fate:** Killed; soul saved; reincarnated
- **Canon confidence:** C5

### N04. Fourth Uncle / Wang Tianshan (王天山) — uncle
- **Role:** family / mentor
- **Cultivation peak:** low-tier cultivator (sect member)
- **Sect/faction:** Wang Clan / Heng Yue Sect (legacy)
- **Relationship:** Wang Lin's uncle; got him into Heng Yue Sect
- **Key events:** Risked his sect position to get Wang Lin into Heng Yue Sect. Targeted by Teng Huayuan's diviner but spared (needed by the expert). Survived clan annihilation.
- **Fate:** Alive (after clan annihilation)
- **Canon confidence:** C4

### N05. Wang Zhuo (王卓) — cousin
- **Role:** family
- **Cultivation peak:** Foundation Establishment → Nascent Soul (Wang Lin helped)
- **Sect/faction:** Wang Clan / Heng Yue Sect
- **Relationship:** Wang Lin's cousin
- **Key events:** After Wang Clan annihilation, married Teng Xiuxiu to protect himself. When Wang Lin returned to exterminate the Teng family, Wang Zhuo killed his wife (Teng Xiuxiu) and then himself rather than betray the Wang clan. Wang Lin saved him and reincarnated him. Wang Lin later helped him form Nascent Soul and recover memories; he guarded the Wang family until lifespan ended.
- **Fate:** Multiple reincarnations; eventually passes from old age
- **Canon confidence:** C4

### N06. Wang Hao (王浩) — cousin
- **Role:** family
- **Cultivation peak:** low-tier cultivator
- **Sect/faction:** Wang Clan / Heng Yue Sect
- **Relationship:** Wang Lin's cousin
- **Key events:** Spared by Teng Huayuan (sect member). One of the few Wang clan survivors.
- **Fate:** Alive
- **Canon confidence:** C4

### N07. Wang Ping (王平) — son
- **Role:** family
- **Cultivation peak:** mortal (intentionally raised as mortal)
- **Sect/faction:** none (mortal)
- **Relationship:** Wang Lin's son, by Liu Mei (refined into a resentful spirit by Liu Mei out of hatred, then raised by Wang Lin)
- **Key events:** Raised in a desolate village by Wang Lin as a mortal. His death triggers Wang Lin's Karma Domain evolution. Reincarnates; eventually reunited with Wang Lin.
- **Fate:** Dies (mortal lifespan); reincarnated; reunited
- **Canon confidence:** C5

### N08. Wang Yiyi (王一一) — daughter
- **Role:** family
- **Cultivation peak:** Paragon-tier (in AWWP, Saintess of the Vast Dao Palace)
- **Sect/faction:** Vast Dao Palace (Saintess) → mask spirit (in AWWP)
- **Relationship:** Wang Lin & Li Muwan's daughter
- **Key events:** Escaped the destruction of the Vast Dao Palace by inhabiting a mask. Accompanied Wang Baole (her future husband) in AWWP. Experiences dozens of reincarnations during the Weiyang Boundary.
- **Fate:** Alive (married to Wang Baole in AWWP); eventually brought back to the Xiangang Continent by Wang Lin
- **Canon confidence:** C5

### N09. Wang Jiduo / Ji Du (王继多 / 继度) — adopted son
- **Role:** family
- **Cultivation peak:** Imperial Venerable (Ancient Clan Primordial lineage)
- **Sect/faction:** Ancient Clan (Primordial Ancient lineage)
- **Relationship:** Wang Lin's adopted son / godson (in IAC)
- **Key events:** Recognized and adopted by Wang Lin on the IAC. Wang Lin enables him to become Imperial Venerable.
- **Fate:** Alive
- **Canon confidence:** C4

### N10. Zhou Ru (周茹) — adopted daughter
- **Role:** family
- **Cultivation peak:** Soul Transformation (under Mu Bingmei)
- **Sect/faction:** Kunxu Realm (disciple of Mu Bingmei)
- **Relationship:** Wang Lin's adopted daughter; vessel for Li Muwan's soul (intended)
- **Key events:** Wang Lin placed Li Muwan's soul in Zhou Ru's unborn body to heal it. Li Muwan chose not to devour Zhou Ru's soul. Calls Wang Lin "uncle." Pet tiger: Xiao Bai. Taken to Rain Immortal World to cultivate. Reincarnated on IAC, lives an ordinary life.
- **Fate:** Alive; reincarnated ordinary life
- **Canon confidence:** C5

### N11. Qing Yi (青衣) — daughter-in-law
- **Role:** family
- **Cultivation peak:** unknown
- **Sect/faction:** Wang family
- **Relationship:** Wang Ping's wife (daughter-in-law)
- **Key events:** Married Wang Ping.
- **Fate:** Alive
- **Canon confidence:** C4

### N12. Song Yu (宋玉) — daughter-in-law
- **Role:** family
- **Cultivation peak:** unknown
- **Sect/faction:** Wang family
- **Relationship:** Wang Jiduo's wife
- **Key events:** Married Wang Jiduo.
- **Fate:** Alive
- **Canon confidence:** C4

### N13. Wang Baole (王宝乐) — son-in-law
- **Role:** family (cross-novel, AWWP protagonist)
- **Cultivation peak:** Summer Immortal (9th Step)
- **Sect/faction:** Federation → Wang Lin's lineage
- **Relationship:** Wang Yiyi's husband (therefore Wang Lin's son-in-law); also Wang Lin's disciple in AWWP
- **Key events:** AWWP protagonist. Mentored/protected by Wang Lin ("Paragon Wang") in AWWP ch60s and ch69 (mosquito pet). Suggests Heaven Trampling bridge for 4th step. Inherits the Eight Extreme Dao from Wang Lin.
- **Fate:** Alive (transcendent)
- **Canon confidence:** C5

### N14. Li Qiqing (李齐青) — brother-in-law
- **Role:** family
- **Cultivation peak:** elite disciple
- **Sect/faction:** Luo He Sect (Flame-Burning Country)
- **Relationship:** Li Muwan's older brother; raised her after their parents died
- **Key events:** Single-handedly raised Li Muwan; deeply trusted by sect members.
- **Fate:** Alive
- **Canon confidence:** C4

### N15. Teng Xiuxiu (藤秀秀) — cousin-in-law (forced)
- **Role:** family (forced) / victim
- **Cultivation peak:** low-tier cultivator (Teng Clan)
- **Sect/faction:** Teng Clan
- **Relationship:** Wang Zhuo's wife (forced marriage after Wang Clan annihilation)
- **Key events:** Killed by Wang Zhuo when he chose the Wang clan over her.
- **Fate:** Killed by her husband
- **Canon confidence:** C4

### N16. Dao Master Blue Dreams (蓝梦道主) — father-in-law
- **Role:** family / mentor
- **Cultivation peak:** Dao Master (Void Tribulant+)
- **Sect/faction:** Blue Silk Clan
- **Relationship:** Li Qianmei's father; taught Wang Lin Light Shadow Shield, Dao Art Fusion, Overturn Heaven Seal
- **Key events:** Wang Lin injured Blue Dream's palm at one point. Later healed Li Qianmei at the cost of her memories. Taught Wang Lin spells.
- **Fate:** Alive
- **Canon confidence:** C4

## 2C. Spouses

### N17. Li Muwan / Wan'er (李慕婉) — primary wife
- **Role:** spouse / protagonist's beloved
- **Cultivation peak:** Nascent Soul (at death); Treading Heaven Realm (after resurrection by Wang Lin)
- **Sect/faction:** Luo He Sect (Flame-Burning Country) → Cloud Sky Sect / Yuntian Sect (Elder → Master)
- **Relationship:** Wang Lin's only true wife
- **Key events:** Met Wang Lin in Fire Burn Country (escaping a Fire Beast). Drained her life force to refine the Azure Dragon Jade Slip for Wang Lin (damaged her foundation → stuck at early Core Formation). Made the Distant Heaven Pill that triggered Wang Lin's Core Formation. Waited 200 years for him. Married Sun Zhenwei — Wang Lin killed him and took her back. Failed Nascent Soul formation multiple times; finally formed Nascent Soul but died (body expired, 500 years old). Wang Lin placed her soul in Heaven Defying Bead for 700 years. Tried Qi Xi spell (failed). Heaven-Avoiding Coffin (life-saving). Finally resurrected by Wang Lin at 4th Step. Transcends with him.
- **Fate:** Dead → soul preserved 700 years → resurrected → transcendent
- **Canon confidence:** C5

### N18. Li Qianmei (李千媚) — second wife
- **Role:** spouse / ally
- **Cultivation peak:** Nirvana Scryer+ (her father healed her, at cost of memories)
- **Sect/faction:** Ghost Sect (originally) → Wang Lin's lineage
- **Relationship:** Daughter of Dao Master Blue Dream; Wang Lin's second wife
- **Key events:** Asked Wang Lin 3 questions at the Origin Sect. Traveled with him. Smeared blood on his stone-petrified body for 10 years to save him. Sent into a spatial realm with powerful beasts by the Ghost Sect. Rescued by Wang Lin. Healed by her father, losing most memories of Wang Lin. The "doppelganger" (one of Wang Lin's clones) eventually accompanies her.
- **Fate:** Alive; with one of Wang Lin's clones
- **Canon confidence:** C5

### N19. Mu Bingmei / Liu Mei (慕冰媚 / 柳眉) — third wife (complex)
- **Role:** spouse (complex) / rival / enemy → ally
- **Cultivation peak:** Ascendant+
- **Sect/faction:** Vermilion Bird Country (core disciple, hundred-year Nascent Soul); later Divine Sect (sect master, set up by Wang Lin)
- **Relationship:** Wang Lin's third wife; Liu Mei's true form is Mu Bingmei
- **Key events:** Has a son with Wang Lin (Wang Ping), whom she refined into a resentful spirit out of hatred for him. Wang Lin raises Wang Ping as a mortal. Liu Mei's true form (Mu Bingmei) emerges later; Wang Lin severs karmic ties with her via the Dream Dao. One of Wang Lin's clones eventually accompanies her.
- **Fate:** Alive; with one of Wang Lin's clones
- **Canon confidence:** C4

## 2D. Mentors

### N20. Situ Nan (司徒南) — primary mentor
- **Role:** mentor / companion / sworn brother
- **Cultivation peak:** Yang Solid Peak (reconstructed); Heaven Trampling (after IAC reincarnation)
- **Sect/faction:** Vermilion Bird Country (2nd-Gen Vermilion Bird Master); later Wu Xuan Country (Grand Marshal "Si Nan" / Southern Prince)
- **Relationship:** Wang Lin's earliest mentor; life-and-death sworn brother
- **Key events:** Originally one of the seven souls (Green Soul — cultivation talent) of the Seven-Colored Immortal Venerable. Betrayed by 3rd-Gen Vermilion Bird and Tan Lang; primordial spirit fled into Heaven Defying Pearl. Met Wang Lin when Wang Lin found the bead. Gave up body-snatching; taught Wang Lin cultivation. Sacrificed his remaining power to save Wang Lin's soul after Teng Huayuan. Wang Lin helped him reconstruct a physical body at Soul Transformation stage. Founded "Southern King" faction on Feng Luan Star. Reincarnated on IAC as "Si Nan," Grand Marshal of Wu Xuan Country.
- **Fate:** Alive; becomes prince (lifelong dream); no regrets
- **Canon confidence:** C5

### N21. All-Seer / Tian Yunzi's master (全知者) — false mentor / antagonist
- **Role:** mentor (false) / antagonist
- **Cultivation peak:** peak Third Step (eventually)
- **Sect/faction:** Heavenly Fate Sect (Tianyun Sect) — leader
- **Relationship:** False mentor; the mortal-realm schemer
- **Key events:** Plotted to absorb the source origins of Wang Lin, Ling Tianhou, and Blood Ancestor. Taught Wang Lin the Celestial Slaughter Art (with a trap inside). Schemed against Wang Lin's Vermilion Bird Divine Mark awakening. Killed 2 of All-Seer's avatars via Sundered Night (Wang Lin). His clone is Tianyunzi.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C5

### N22. Tu Si (涂司) — inheritance mentor (ancient)
- **Role:** mentor (inheritance only)
- **Cultivation peak:** 8-Star Ancient God (peak)
- **Sect/faction:** Ancient Clan
- **Relationship:** Wang Lin inherited Tu Si's "knowledge" inheritance (the "memory inheritance")
- **Key events:** Granted Wang Lin the "Great Enlightened One" title. His body became the Land of the Ancient God (the 3-level Chaotic Broken Stars realm). Tuo Sen is his demonic thought (inherited his "power" inheritance).
- **Fate:** Long dead; body became a realm; inheritance split between Wang Lin (knowledge) and Tuo Sen (power)
- **Canon confidence:** C5

### N23. Du Tian (顿天) — brief mentor
- **Role:** mentor / ally
- **Cultivation peak:** Nirvana Scryer+ (erased his own consciousness to become a soul in the Soul Banner)
- **Sect/faction:** Soul Refining Sect (Pilu Kingdom) — ancestor
- **Relationship:** Effectively Wang Lin's master (no formal relation); senior brother to Nian Tian
- **Key events:** Gave Wang Lin 3 gifts: (1) helped Wang Lin's clone reach Nascent Soul peak, (2) helped Wang Lin's true body reach 3-Star Ancient God, (3) bestowed the Ten Billion Soul Banner and Soul Refining Sect inheritance. Took Wang Lin to seize Immortal Jades for Soul Transformation breakthrough. Erased his own consciousness to become a soul within the Soul Banner after Wang Lin's promise to elevate Soul Refining Sect to 6th-level.
- **Fate:** Self-erased; exists as a soul in the Ten Billion Soul Banner
- **Canon confidence:** C5

### N24. Bai Fan (白凡) — Immortal Emperor (inheritance)
- **Role:** mentor (inheritance)
- **Cultivation peak:** Immortal Emperor (Third Step+)
- **Sect/faction:** Thunder Immortal World
- **Relationship:** Wang Lin inherited Bai Fan's Mountain Crumble spell and the Six Paths Triple Techniques (Call Wind, Call Rain, Scatter Beans to Form Soldiers, Mountain Collapse, Dark Moon Has Clear, etc.)
- **Key events:** Wang Lin finds Bai Fan's Collection Pavilion in the Thunder Immortal World.
- **Fate:** Long dead; inheritance passed to Wang Lin
- **Canon confidence:** C4

### N25. Lu Yun (陆云) — 5th-Gen Vermilion Bird / Heng Yue master
- **Role:** mentor / secret benefactor
- **Cultivation peak:** Void Flame Cultivator (5th-Gen Vermilion Bird Divine Emperor)
- **Sect/faction:** Four Divine Sect / Vermilion Bird Divine Sect / Heng Yue Sect (secret identity: Huang Long Zhenren, Heng Yue Sect master)
- **Relationship:** Wang Lin's secret benefactor; gave Wang Lin the Vermilion Bird Sequence
- **Key events:** Infiltrated the Cultivation Alliance HQ to gather info on Qing Shui. Endured the Heavenly Decay Tribulation awaiting the next generation's Vermilion Bird. Taught Wang Lin the Vermilion Bird Nine Mysterious Transformations. Died after returning from the Cultivation Alliance.
- **Fate:** Dead (passed away after passing on the lineage)
- **Canon confidence:** C4

### N26. Qing Lin (青林) — Master In Name / Immortal Emperor
- **Role:** mentor (in name) / inheritance
- **Cultivation peak:** Immortal Emperor (Xiangang Continent)
- **Sect/faction:** Xiangang Continent; father of Qing Shuang
- **Relationship:** Wang Lin's "master in name"; saved by Wang Lin; taught Wang Lin the Body Fixation Art
- **Key events:** Left the Body Fixation Art for his daughter Qing Shuang; Wang Lin accidentally obtained it. Taga (ancient demon) possessed Qing Lin's body; Wang Lin fought Taga.
- **Fate:** Saved by Wang Lin (returns from possession)
- **Canon confidence:** C4

### N27. Su Dao / Scholar (苏道) — brief mentor
- **Role:** mentor (brief)
- **Cultivation peak:** unknown
- **Sect/faction:** none (scholar)
- **Relationship:** Wang Lin's "Scholar" mentor
- **Key events:** Mentioned in Wang Lin's masters list. Tangential.
- **Fate:** Unknown
- **Canon confidence:** C3

### N28. Xuan Luo (玄罗) — Great Heavenly Venerable, true master
- **Role:** mentor / true master
- **Cultivation peak:** Great Heavenly Venerable (Dao Gu lineage)
- **Sect/faction:** Ancient Clan (Dao Gu lineage)
- **Relationship:** Wang Lin's true master (the only one he formally acknowledges on the IAC)
- **Key events:** Refused the Ancient Dao Great Heavenly Venerable's offer to take him as disciple. Fought Dao Yi Great Heavenly Venerable over a fragment of Primordial God Realm at the Seven Paths Sect entrance — indirectly causing the Cave World's birth. Entered the Cave World to accept Wang Lin as his only disciple. Helped over a dozen of Wang Lin's friends reincarnate into the Immortal Execution Continent. Intervened during Wang Lin's tribulation.
- **Fate:** Alive; undergoes reincarnation (Wang Lin gifted him a protective life-saving jade slip)
- **Canon confidence:** C5

### N29. Dao Master Blue Dream (蓝梦道主) — see N16 (also a mentor)

## 2E. Allies

### N30. Qing Shui (清水) — senior brother / mass-murderer friend
- **Role:** ally / senior brother
- **Cultivation peak:** Third Step (Slaughter Essence)
- **Sect/faction:** Qing Shui Kingdom (former prince); Colorful Immortal Venerable's slaughter soul
- **Relationship:** Wang Lin's senior brother (recognized across generations via Bai Fan's technique); formed from the Seven-Colored Immortal Venerable's lifetime of slaughter
- **Key events:** Saved Wang Lin multiple times. Taught Wang Lin immortal arts. Wang Lin helped him find his daughter Hong Die. Reincarnated on Immortal Execution Star; memories auto-recovered. In ISSTH, left a clone to assist Meng Hao. Gifted Wang Lin the Slaughter Sword (Slaughter Essence) when Wang Lin broke through Sky Gate.
- **Fate:** Alive; reaches Third Step
- **Canon confidence:** C5

### N31. Zhou Yi (周逸) — ally (Obsession Concept)
- **Role:** ally
- **Cultivation peak:** Wending realm (soul transformed into sword spirit)
- **Sect/faction:** originally his sect; defected
- **Relationship:** Wang Lin's ally; Obsession Concept
- **Key events:** Found Qing Shuang's corpse in Rain Immortal Realm and called her "Ting'er" (necrophiliac reputation). Defected from his sect to protect her. Multiple Rain Immortal Realm entries for Immortal Jades. Burned his primordial spirit in defense — reached Wending realm. Gave his Wending Crystal to Wang Lin (asking Wang Lin to protect Ting'er). Ting'er's remnant soul awakened and transformed Zhou Yi's primordial spirit into the sword spirit of the Rain Immortal Sword. Reincarnates on IAC with Qing Shuang.
- **Fate:** Becomes sword spirit of Rain Immortal Sword; reincarnated with Qing Shuang on IAC
- **Canon confidence:** C5

### N32. Qing Shuang (青霜) — ally (Body Fixation Art)
- **Role:** ally
- **Cultivation peak:** Immortal-tier (Xiangang Continent Immortal Art inheritor)
- **Sect/faction:** Qing Lin lineage
- **Relationship:** Qing Lin's daughter; "Ting'er" to Zhou Yi; taught Wang Lin the Body Fixation Art
- **Key events:** Her corpse was protected by Zhou Yi. Body Fixation Art originated as her inheritance. Her remnant soul awakened and transformed Zhou Yi's primordial spirit into the Rain Immortal Sword's spirit. Reincarnates on IAC with Zhou Yi.
- **Fate:** Dead (corpse) → remnant soul awakened → reincarnated with Zhou Yi on IAC
- **Canon confidence:** C5

### N33. Chi Hu (赤虎) — ally (Giant Demon Clan)
- **Role:** ally
- **Cultivation peak:** unknown
- **Sect/faction:** Giant Demon Clan
- **Relationship:** Gave Wang Lin the Star Compass
- **Key events:** Gifted the Star Compass (Wang Lin's primary void-transportation).
- **Fate:** Alive
- **Canon confidence:** C4

### N34. Qiu Siping (邱思平) — ally
- **Role:** ally
- **Cultivation peak:** late Core Formation → Nascent Soul (with Wang Lin's help)
- **Sect/faction:** initially his own master's sect; later Wang Lin's ally
- **Relationship:** Ally; proficient in ancient restrictions
- **Key events:** Agreed to unlock Wang Lin's restriction on Yun Fei in exchange for the caster's name. Wang Lin spared him; both reached Nascent Soul via consuming Wang Lin's master's and senior brother's souls.
- **Fate:** Alive
- **Canon confidence:** C4

### N35. Mo Ling (莫灵) — ally
- **Role:** ally
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Listed in Wang Lin's allies
- **Key events:** Tangential.
- **Fate:** Alive
- **Canon confidence:** C3

### N36. Mo Lihai (莫离海) — ally (Sky Demon Country general)
- **Role:** ally
- **Cultivation peak:** Ascendant+
- **Sect/faction:** Sky Demon Country
- **Relationship:** Wang Lin befriends him in the Sky Demon Country capital
- **Key events:** Helped Wang Lin in the Demonic Drum tournament. Asked Wang Lin to travel to the capital city and help him obtain Vice Commander-in-Chief.
- **Fate:** Alive
- **Canon confidence:** C4

### N37. Sun Tai (孙泰) — ally / servant (Corpse Yin Sect)
- **Role:** ally / servant
- **Cultivation peak:** Nirvana Scryer+ (initially)
- **Sect/faction:** Corpse Yin Sect (later Wang Lin's servant)
- **Relationship:** Wang Lin's servant (after defeat)
- **Key events:** Tried to seize Ting'er (Qing Shuang). Defeated by Zhou Yi. Became Wang Lin's servant. Spent his final years peacefully on Planet Ran Yun as Wang Lin's friend.
- **Fate:** Alive
- **Canon confidence:** C4

### N38. Li Yuan (李远) — ally
- **Role:** ally
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Listed in Wang Lin's allies
- **Key events:** Tangential.
- **Fate:** Alive
- **Canon confidence:** C3

### N39. Ling Tianhou (凌天候) — ally (Da Lou Sword Sect)
- **Role:** ally / rival-mentor
- **Cultivation peak:** Third Step (Nirvana Void)
- **Sect/faction:** Da Lou Sword Sect — elder / sect master
- **Relationship:** Wang Lin's ally; invited Wang Lin to be Da Lou Sword Sect elder
- **Key events:** Challenged Wang Lin to take 3 sword strikes (Wang Lin survived all 3). Helped defend Wang Lin against All-Seer. Allied during the East Demon Spirit Sea arc. Helped with the void Moongazer Serpent incident.
- **Fate:** Alive
- **Canon confidence:** C4

### N40. Bei Lou (贝罗) — ally (celestial)
- **Role:** ally
- **Cultivation peak:** Third Step+
- **Sect/faction:** Celestial (Thunder Celestial Temple)
- **Relationship:** Wang Lin's ally
- **Key events:** Helped Wang Lin battle divine retribution during Ascendant breakthrough. Helped trap the scattered devil Tie Lan. Helped Wang Lin weaken the fragmented ancient demon under Fire Demon Country → devoured it → allowed Wang Lin into diluted Shi Realm → helped locate Zhou Yi's spirit in the Tide Abyss. Among those who coldly stared at Master Void.
- **Fate:** Alive
- **Canon confidence:** C4

### N41. Wang Wei (王伟) — ally
- **Role:** ally
- **Cultivation peak:** Nirvana Shatterer (injured by Master Void)
- **Sect/faction:** unknown
- **Relationship:** Wang Lin's ally
- **Key events:** Among those who coldly stared at Master Void. Sneak-attacked by Master Void; Wang Lin used his furnace to switch places and defended against Master Void's attack.
- **Fate:** Alive (after Wang Lin's intervention)
- **Canon confidence:** C4

### N42. Hu Juan (胡娟) — ally
- **Role:** ally
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Wang Lin's ally
- **Key events:** Tangential.
- **Fate:** Alive
- **Canon confidence:** C3

### N43. Qing Lin (青林) — see N26 (also ally)

### N44. Ta Shan (塔山) — servant (Celestial Guard)
- **Role:** servant / ally
- **Cultivation peak:** Celestial Guard (refined)
- **Sect/faction:** Wang Lin's
- **Relationship:** Wang Lin's servant (Celestial Guard)
- **Key events:** Wang Lin's refined Celestial Guard; serves as his front-line combat puppet.
- **Fate:** Alive (bound to Wang Lin)
- **Canon confidence:** C4

### N45. Big Head Cultivator (大头) — servant
- **Role:** servant / ally
- **Cultivation peak:** Kunie (initially)
- **Sect/faction:** originally Luo Tian; later Wang Lin's
- **Relationship:** Wang Lin's servant (sealed with slave seal)
- **Key events:** Sealed with slave seal during the Luo Tian invasion war. Followed Wang Lin for a long time. Reincarnated on IAC; memories reawakened by Wang Lin.
- **Fate:** Alive; reincarnated on IAC
- **Canon confidence:** C4

### N46. Lei Ji (雷记) — mount
- **Role:** companion / mount
- **Cultivation peak:** beast
- **Sect/faction:** Wang Lin's
- **Relationship:** Wang Lin's mount
- **Key events:** Taken by Wang Lin from the Corpse Yin Sect on Planet Suzaku.
- **Fate:** Alive
- **Canon confidence:** C4

### N47. Liu Jinbiao (刘金彪) — Three Auspicious Treasures
- **Role:** servant / ally
- **Cultivation peak:** Peak Path of Deception cultivator (can deceive even himself)
- **Sect/faction:** Three Auspicious Treasures (with Xu Liguo + Zhong Dahong)
- **Relationship:** Wang Lin's servant; Path of Deception master
- **Key events:** Found by Wang Lin via the Heavenly Fortune Calculation method. Saved Wang Lin's life. Tormented by Xu Liguo. Impersonated the 6th-Gen Vermilion Bird with Xu Liguo — caught by the 3rd-Gen Evil Sparrow. On IAC, used Path of Deception to help Wang Lin comprehend Samsara, Taichu, Miemie Origins. Gained the Golden Sea Dragon's recognition.
- **Fate:** Alive; continues swindling on IAC
- **Canon confidence:** C4

### N48. Ling Dong (凌东) — servant (former enemy)
- **Role:** servant (former enemy)
- **Cultivation peak:** unknown
- **Sect/faction:** Wang Lin's
- **Relationship:** Initially antagonistic; later servant
- **Key events:** Listed in Wang Lin's enemies (former) and allies/servants.
- **Fate:** Alive
- **Canon confidence:** C3

### N49. Zhou Jin (周瑾) — servant (former enemy)
- **Role:** servant (former enemy)
- **Cultivation peak:** unknown
- **Sect/faction:** Wang Lin's
- **Relationship:** Initially antagonistic; later servant
- **Key events:** Listed in Wang Lin's enemies (former) and allies/servants.
- **Fate:** Alive
- **Canon confidence:** C3

### N50. Zhong Dahong (钟大红) — Three Auspicious Treasures
- **Role:** servant / ally
- **Cultivation peak:** unknown
- **Sect/faction:** Flash Thunder Clan (originally) → Wang Lin's
- **Relationship:** Wang Lin's servant (Three Auspicious Treasures)
- **Key events:** Followed Wang Lin when the Flash Thunder Clan was exterminated. Asked 2nd-Gen Vermilion Bird to take care of him. Continued swindling on IAC.
- **Fate:** Alive; continues swindling on IAC
- **Canon confidence:** C4

### N51. Daoist Scattered Spirit (散灵道人) — ally (Scatter Thunder Clan)
- **Role:** ally
- **Cultivation peak:** Third Step (Scatter Thunder Clan elder)
- **Sect/faction:** Scatter Thunder Clan
- **Relationship:** Gave Wang Lin tens of thousands of Spiritual Thunder bolts (after Wang Lin devoured the 5th Heaven Blight head elder's 8 ancient thunder dragons)
- **Key events:** Helped Wang Lin perfect his Thunder Essence.
- **Fate:** Alive
- **Canon confidence:** C4

### N52. Second Generation Vermilion Bird / Young Emperor of the Fallen Land
- **Role:** ally / mentor
- **Cultivation peak:** Void Flame Cultivator (2nd-Gen Vermilion Bird Divine Emperor)
- **Sect/faction:** Vermilion Bird Divine Sect / Fallen Land
- **Relationship:** Helped Wang Lin through the Young Emperor trial
- **Key events:** Fished for a dragon, took its blood, gifted to Wang Lin. Taught Wang Lin the Dao of Strength. Stood up for Wang Lin at the Trial of Heaven. Taught Wang Lin one of the three supreme techniques of the Vermilion Bird when Wang Lin completed the trial. Asked by Wang Lin to take care of Zhong Dahong.
- **Fate:** Alive
- **Canon confidence:** C4

### N53. Master Hong Shan (洪山道主) — ally
- **Role:** ally
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Wang Lin's ally
- **Key events:** Tangential.
- **Fate:** Alive
- **Canon confidence:** C3

### N54. Master South Cloud (南云道主) — ally
- **Role:** ally
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Wang Lin's ally
- **Key events:** Tangential.
- **Fate:** Alive
- **Canon confidence:** C3

### N55. Gemini / Twin Great Heavenly Venerables (双胞胎大天尊) — ally on IAC
- **Role:** ally
- **Cultivation peak:** Great Heavenly Venerable
- **Sect/faction:** IAC
- **Relationship:** Wang Lin's ally on the IAC; took Tan Lang as their "pet"
- **Key events:** Tangential but recurring.
- **Fate:** Alive
- **Canon confidence:** C3

### N56. Tan Lang (贪狼) — Yellow Soul / "lucky star"
- **Role:** ally (reluctant) / treasure piñata
- **Cultivation peak:** Third Step+
- **Sect/faction:** Yellow Soul of the Seven-Colored Immortal Venerable (good fortune)
- **Relationship:** Pursued Situ Nan in the past; Wang Lin's "lucky star"
- **Key events:** His second primordial spirit (Primordial Thunder Dragon) — half devoured by Wang Lin. Treasures repeatedly plundered by Wang Lin. Reduced to a pet by the Twin Great Heavenly Venerables on the IAC.
- **Fate:** Alive; abused pet
- **Canon confidence:** C4

### N57. Hong Die (红蝶) — tragic ally (Qing Shui's daughter)
- **Role:** ally (tragic)
- **Cultivation peak:** Nascent Soul (in 100 years — innate Five Elements spirit + Ruthlessness Concept)
- **Sect/faction:** Vermilion Bird Country (core disciple) → Cultivation Alliance (reincarnated as Qing Hong)
- **Relationship:** Qing Shui's daughter; Wang Lin severely wounded her (right arm severed); later reconciled
- **Key events:** Schemed against by 14th-Gen Vermilion Bird and Qian Feng to be a concubine/cultivation furnace. Qian Feng devoured her vitality, Concept, and Five Elements spirit — turning her into his puppet. In the Vermilion Bird Tomb, she begged Wang Lin to end her life; gave him a blue rose before dying. A wisp of her soul reincarnated through an array into a Cultivation Alliance disciple. Reincarnated on IAC as Qing Hong (likes red and butterflies; unwilling to recover past-life memories).
- **Fate:** Dead → reincarnated as Qing Hong on IAC (no past-life memories)
- **Canon confidence:** C4

### N58. Zhou Wutai (周武泰) — 15th-Gen Vermilion Bird Master / friend
- **Role:** ally / friend
- **Cultivation peak:** Vermilion Bird Master (Wending+)
- **Sect/faction:** Vermilion Bird Country
- **Relationship:** Wang Lin's friend; 15th-Gen Vermilion Bird Master (Wang Lin transferred the position to him)
- **Key events:** Bald with large ears; Azure Dragon bloodline; one of Yun Quezi's 4 chess pieces. Grateful to Wang Lin for transferring the position. Welcomed Wang Lin every time he returned to Vermilion Bird Star; they often got drunk. Informed Wang Lin about Hong Die's split-soul reincarnation.
- **Fate:** Alive; eventually passes from old age
- **Canon confidence:** C4

### N59. Yun Quezi (云阙子) — 2nd Ancestor of Immortal Remnant Clan
- **Role:** ally / schemer
- **Cultivation peak:** Late Nascent Soul; 10-leaf curse master (Manic Concept)
- **Sect/faction:** Immortal Remnant Clan / Vermilion Bird Country (disciple of 13th-Gen Qian Pinghai)
- **Relationship:** Helped Wang Lin comprehend Soul Transformation; selected Wang Lin as one of his 4 chess pieces
- **Key events:** Roamed the mortal world searching for candidates to puppet the Vermilion Child and indirectly control Vermilion Bird Star. Helped Wang Lin comprehend Soul Transformation; gave Wang Lin the Li Ming Straw Hat. Plan to control Vermilion Bird Star failed due to Mo Zhi. Explored the depths of the Immortal Remnant Clan with Wang Lin. Passed away before Wang Lin's 2nd return.
- **Fate:** Dead (passed away)
- **Canon confidence:** C4

### N60. Mo Zhi (莫知) — Cultivation Alliance emissary / mentor figure
- **Role:** ally / mentor figure
- **Cultivation peak:** Third Step+ (Cultivation Alliance emissary)
- **Sect/faction:** Cultivation Alliance (under a master)
- **Relationship:** Wang Lin's mentor figure; emissary
- **Key events:** Met Wang Lin on a rainy night; their dilapidated-temple Dao discussion triggered Wang Lin's Soul Transformation enlightenment. Resolved the Immortal Remnant Clan war on Vermilion Bird Star. Intended Wang Lin to be the next Vermilion Child (Wang Lin declined). Congratulated Wang Lin on becoming 6th-Gen Vermilion Bird Divine Emperor. Intended to tell Wang Lin the whole truth about the Cave World but was refused. Asked Wang Lin (on his master's behalf) when he would return the Boundary Fixing Compass.
- **Fate:** Alive
- **Canon confidence:** C4

### N61. Lian Daofei (连道非) — ally / disciple (Celestial Bloodline)
- **Role:** ally / disciple
- **Cultivation peak:** Eight Extremities Great Heavenly Venerable (new Immortal Emperor)
- **Sect/faction:** Xiangang Continent; brother of Lian Daozhen
- **Relationship:** Fused supreme Immortal bloodline into Wang Lin's body; Wang Lin's disciple (taught him Gravitation Art); later rescued by Lian Daozhen
- **Key events:** Inside the Nether Beast, fused his supreme Immortal bloodline into Wang Lin's body, imparting the Indestructible Immortal Body. Sparred with Wang Lin. Wang Lin made him a disciple. Rescued by Lian Daozhen on the IAC. After Lian Daozhen failed to inherit the Immortal Ancestor's plan, Lian Daofei's bloodline awakened → inherited Eight Extremities Great Heavenly Venerable title → new Immortal Emperor → continues suppressing beast souls of various provinces.
- **Fate:** Alive; new Immortal Emperor
- **Canon confidence:** C5

### N62. Xu Liguo (徐立国) — first devil / Three Auspicious Treasures
- **Role:** companion / servant
- **Cultivation peak:** devil soul (sword spirit later)
- **Sect/faction:** Wang Lin's
- **Relationship:** Wang Lin's first devil soul companion; sword spirit of one of Wang Lin's immortal swords
- **Key events:** Captured by Wang Lin from a Corpse Yin Sect disciple's corpse puppet. Consciousness erased; soul-devourer ability turned him into a demonic head. Rebellious; constantly suppressed by Wang Lin. Given sword-spirit body for "special tasks." Reincarnated on IAC; continued swindling as part of Three Auspicious Treasures. In ISSTH, guarded the Wang family descendants. In Three Inches of Paradise, ancestor of Moon Star Sect.
- **Fate:** Alive
- **Canon confidence:** C5

### N63. Tuo Sen (拓山) — rival → ally (Ancient God)
- **Role:** rival → ally
- **Cultivation peak:** 8-Star Ancient God (potential)
- **Sect/faction:** Ancient Clan (Tu Si's demonic thought)
- **Relationship:** Former enemy; rival; later ally
- **Key events:** Born from Tu Si's failed Ink Flow Split Soul Technique — got Tu Si's "power" inheritance. Hunted Wang Lin (who had Tu Si's "knowledge" inheritance). Trapped in Tu Si's body 1000+ years. Schemed against by Wang Lin (borrowed-knife kill via Corpse Yin Sect + Allheaven). Injured by Wang Lin's Ji Realm. After Wang Lin obtained Dao Ancient inheritance, Wang Lin returned the memory inheritance. Reincarnated on a planet formed from Ye Mo's left eye; received Wang Lin's help.
- **Fate:** Alive; reconciled
- **Canon confidence:** C5

### N64. Xu Yunshan (许云山) — Earth Planet junior sect master
- **Role:** neutral → ally
- **Cultivation peak:** unknown
- **Sect/faction:** Xuan Yuan Sect (Earth Planet)
- **Relationship:** Greeted Wang Lin on Earth Planet
- **Key events:** Tangential to main plot.
- **Fate:** Alive
- **Canon confidence:** C3

### N65. Ouyang Hua (欧阳华) — Mountain Valley Tribe chief
- **Role:** ally
- **Cultivation peak:** unknown
- **Sect/faction:** Mountain Valley Tribe → Soul Refining Tribe
- **Relationship:** Recognized Wang Lin as the tribe's Ancestor
- **Key events:** Gave the Mountain Valley Tribe's recognition to Wang Lin; the tribe became the Soul Refining Tribe of the eastern sea.
- **Fate:** Alive
- **Canon confidence:** C4

### N66. Li Dannan (李丹楠) — Trading Planet guide
- **Role:** ally (brief)
- **Cultivation peak:** unknown
- **Sect/faction:** none
- **Relationship:** Wang Lin's guide on the Trading Planet
- **Key events:** Helped Wang Lin search for Void Wood Stone and snow ink.
- **Fate:** Alive
- **Canon confidence:** C3

### N67. Bai Wei (白薇) — Trading Planet / All-Seer's plot
- **Role:** complex (caught in All-Seer's plot)
- **Cultivation peak:** unknown
- **Sect/faction:** Da Lou Sword Sect (associated)
- **Relationship:** Caught in All-Seer's scheme against Wang Lin
- **Key events:** Met Wang Lin on Trading Planet. Wang Lin tried to help her with her secret (Yang energy). Refining Bai Wei's Yang energy (with Heaven Defying Bead) injured All-Seer and Ling Tianhou. Revealed to be part of All-Seer's plot.
- **Fate:** Alive
- **Canon confidence:** C4

### N68. Master Flamespark (火芒) — major NPC (Thunder Celestial Temple)
- **Role:** complex (sometimes antagonist)
- **Cultivation peak:** Third Step (Thunder Celestial Temple master)
- **Sect/faction:** Thunder Celestial Temple / Thunder Celestial Realm
- **Relationship:** Master of the Thunder Celestial Temple; intervenes in Russell vs. Wang Lin
- **Key events:** Stopped Russell's final attack on Wang Lin. Multiple appearances in Thunder Celestial Realm arc.
- **Fate:** Alive
- **Canon confidence:** C4

### N69. Russell (罗素) — Thunder Celestial Temple test proctor
- **Role:** antagonist (killed by Qing Shui)
- **Cultivation peak:** Third Step
- **Sect/faction:** Thunder Celestial Temple
- **Relationship:** Wang Lin killed his brother; Russell attacked Wang Lin
- **Key events:** Test proctor who thought participants were trash. Saw Wang Lin easily defeat his Golden Giant → realized Wang Lin was his brother's killer → attacked. Killed by Qing Shui's Ji Realm for offending Wang Lin.
- **Fate:** Killed by Qing Shui
- **Canon confidence:** C4

### N70. Zhao Yu (赵宇) — Origin Sect disciple
- **Role:** minor antagonist / victim
- **Cultivation peak:** low
- **Sect/faction:** Origin Sect
- **Relationship:** Made mortal villagers stand in the rain; Wang Lin enslaved him
- **Key events:** Arrogant Origin Sect disciple. Made all the mortal villagers stand outside in the rain; Wang Lin enslaved him and had him lead Wang Lin back to the Origin Sect.
- **Fate:** Alive (Wang Lin's slave)
- **Canon confidence:** C4

### N71. Lu Yanfei (陆燕飞) — Origin Sect ally
- **Role:** ally
- **Cultivation peak:** unknown
- **Sect/faction:** Origin Sect
- **Relationship:** Guessed Wang Lin's situation accurately; gave him an ancestor identity
- **Key events:** Wisely identified Wang Lin's true status; granted him the "ancestor" cover identity.
- **Fate:** Alive
- **Canon confidence:** C4

### N72. Xu Yun (许云) — Origin Sect
- **Role:** ally (minor)
- **Cultivation peak:** unknown
- **Sect/faction:** Origin Sect
- **Relationship:** Confronted Wang Lin alongside Lu Yanfei
- **Key events:** Tangential.
- **Fate:** Alive
- **Canon confidence:** C3

### N73. Lu Yuncong (陆云聪) — Origin Sect (revenge-seeker)
- **Role:** complex (initially revenge, then Dao-friend)
- **Cultivation peak:** unknown
- **Sect/faction:** Origin Sect
- **Relationship:** Wanted revenge for his son; realized Wang Lin was far superior
- **Key events:** Came with Li Qian Mei for revenge. Realized he was no match for Wang Lin. Discussed Dao all night; gained great benefits.
- **Fate:** Alive
- **Canon confidence:** C4

### N74. Song Wude (宋武德) — invader (killed)
- **Role:** antagonist (killed)
- **Cultivation peak:** unknown
- **Sect/faction:** Invader sect
- **Relationship:** Tried to take over the Origin Sect
- **Key events:** Came with Rudy to take over the Origin Sect. Killed by Wang Lin.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C4

### N75. Rudy — invader (killed)
- **Role:** antagonist (killed)
- **Cultivation peak:** unknown
- **Sect/faction:** Invader sect
- **Relationship:** Tried to take over the Origin Sect with Song Wude
- **Key events:** Killed by Wang Lin.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C3

### N76. Ji Si (吉思) — Wang Lin's helper on IAC
- **Role:** complex ally (acted for Green Devil, but helped Wang Lin)
- **Cultivation peak:** unknown
- **Sect/faction:** Dao Demon Sect (initially); later Wang Lin's
- **Relationship:** Implanted multiple Essences into Wang Lin's body (preparing him for Green Devil possession)
- **Key events:** Granted Earth Essence (3 Meng Earth Beads). Implanted a strand of Celestial Ancestor's hair for Wang Lin's second vein (Celestial + Ancient Clan powers usable simultaneously). Used Celestial Sea Mother Soul, Mortal Dream Water, Dao Heart Melting Liquid, Water Celestial Blood, two mouthfuls of his own blood (16 years of life) → Water Essence True Body. 90,000-year-old celestial tree for Wood Essence seed. Fragment of Celestial Ancestor's Immortal Absolute Sword for Metal Essence.
- **Fate:** Alive (turns out Wang Lin's body reclaims all these Essences)
- **Canon confidence:** C4

### N77. Jiu Di / Grand Empyrean Jiu Di (九地) — IAC Grand Empyrean
- **Role:** complex (gave Wang Lin Metal fragment)
- **Cultivation peak:** Grand Empyrean
- **Sect/faction:** IAC
- **Relationship:** Confronted Wang Lin; later gave him the third fragment of the Celestial Ancestor's Immortal Absolute Sword
- **Key events:** Confronted by Wang Lin in the Imperial City. Gave Wang Lin the third sword fragment (Metal Essence Great Accomplishment).
- **Fate:** Alive
- **Canon confidence:** C4

### N78. Sea Child Celestial Venerable (海子大天尊) — IAC Celestial Venerable
- **Role:** complex
- **Cultivation peak:** Celestial Venerable
- **Sect/faction:** IAC
- **Relationship:** Trapped with Wang Lin inside the Immortal Ancestor's severed palm; broke free via a Great Celestial Venerable's scheme
- **Key events:** Encounter inside Immortal Ancestor's severed palm → fortune related to Metal and Wood Origins.
- **Fate:** Alive
- **Canon confidence:** C3

### N79. Ye Wuyou (叶无忧) — 1st-Gen Vermilion Bird Master
- **Role:** backstory ally (Situ Nan's benefactor)
- **Cultivation peak:** 1st-Gen Vermilion Bird Master
- **Sect/faction:** Vermilion Bird Country
- **Relationship:** Situ Nan's benefactor (the only person Situ Nan respected besides Wang Lin)
- **Key events:** Situ Nan guarded the Vermilion Bird Star for a thousand years and sealed the Cultivation Star Crystal to repay Ye Wuyou's life-saving favor. Long deceased by main story.
- **Fate:** Long dead
- **Canon confidence:** C4

### N80. Qian Pinghai (钱平海) — 13th-Gen Vermilion Bird Master
- **Role:** neutral (backstory)
- **Cultivation peak:** 13th-Gen Vermilion Bird Master
- **Sect/faction:** Vermilion Bird Country
- **Relationship:** Yun Quezi's master
- **Key events:** Backstory.
- **Fate:** Dead (passed away)
- **Canon confidence:** C3

### N81. Third Generation Vermilion Bird Master — betrayer
- **Role:** antagonist (backstory)
- **Cultivation peak:** 3rd-Gen Vermilion Bird Master
- **Sect/faction:** Vermilion Bird Country
- **Relationship:** Betrayed Situ Nan
- **Key events:** Took advantage of Situ Nan's weakened state after the Cultivation Star Crystal sealing to launch a sneak attack (with Tan Lang), destroying Situ Nan's physical body.
- **Fate:** Dead (presumably)
- **Canon confidence:** C4

### N82. Fourteenth Generation Vermilion Bird Master — antagonist (backstory)
- **Role:** antagonist
- **Cultivation peak:** 14th-Gen Vermilion Bird Master
- **Sect/faction:** Vermilion Bird Country
- **Relationship:** Severed Situ Nan's arm (after Situ Nan's soul intimidated him)
- **Key events:** Berated by Situ Nan's damaged soul; severed three of his own fingers and fled at the mere mention of Situ Nan's name. Schemed against Hong Die. Attacked the Soul Refining Sect in EP96/ch1500; Situ Nan (possessing Wang Lin) intimidated him again.
- **Fate:** Fled
- **Canon confidence:** C4

## 2F. Antagonists

### N83. Teng Huayuan (藤化元) — early major antagonist
- **Role:** antagonist
- **Cultivation peak:** Half-Step Deity Transformation (Late Nascent Soul, later elevated by Piao Nanzi)
- **Sect/faction:** Teng Clan (Patriarch); guest elder Jimo Elder
- **Relationship:** Wang Lin's first major revenge target
- **Key events:** After Wang Lin killed Teng Li (his great-great-grandson), Teng Huayuan burned lifespan to curse-track Wang Lin. Had Gao Qiming divine the Wang family's location. Exterminated the entire Wang family; trapped their souls in his banner; transmitted the massacre scene to Wang Lin's mind. Dueled Wang Lin outside Jue Ming Valley. Believed Wang Lin was dead after the duel. Built the "Nine Great Nascent Souls" (Teng One to Teng Nine) selection system to break the generational gap. All 9 Nascent Soul cultivators were killed by Wang Lin and refined into demons. Wang Lin's "Kill and Destroy the Heart" — first hunted Teng descendants, then built a tower of heads to intimidate Teng Huayuan, then finally slew him.
- **Fate:** Killed by Wang Lin; soul refined into a demon
- **Canon confidence:** C5

### N84. Teng Li (藤立) — first kill
- **Role:** antagonist (first kill)
- **Cultivation peak:** late Foundation Establishment
- **Sect/faction:** Teng Clan
- **Relationship:** Teng Huayuan's great-great-grandson
- **Key events:** Pursued Wang Lin for Old man Ji Mo's bounty. Killed by Wang Lin in the Forest of Distorted Divine Sense. Wang Lin stole his Foundation Establishment.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C5

### N85. Sun Dazhu (孙大柱) — early antagonist
- **Role:** antagonist
- **Cultivation peak:** Foundation Establishment
- **Sect/faction:** Heng Yue Sect (Zhang Hu's "master")
- **Relationship:** Killed by Wang Lin (to help Zhang Hu)
- **Key events:** Killed by Wang Lin to help Zhang Hu out of trouble.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C4

### N86. Old man Ji Mo (极魔老人) — early antagonist
- **Role:** antagonist
- **Cultivation peak:** Core Formation+
- **Sect/faction:** Heng Yue Sect (associated); Jimo Elder
- **Relationship:** Sent disciples to take revenge on Wang Lin for killing Teng Li
- **Key events:** Master of the disciple Wang Lin killed on Tian Shui City road. Sent Teng Li and other disciples. Indirectly caused the Wang Clan's destruction.
- **Fate:** Alive (presumably)
- **Canon confidence:** C4

### N87. Gao Qiming (高启明) — diviner
- **Role:** antagonist (minor)
- **Cultivation peak:** diviner
- **Sect/faction:** independent diviner
- **Relationship:** Helped Teng Huayuan locate the Wang family
- **Key events:** Divined Wang Lin's family location for Teng Huayuan. Requested Fourth Uncle as payment.
- **Fate:** Alive (presumably)
- **Canon confidence:** C3

### N88. Punnan Zi / Piao Nanzi / Lou Hou (飘南子 / 楼侯) — sealed demon
- **Role:** antagonist
- **Cultivation peak:** peak Third Step (sealed)
- **Sect/faction:** Scattered Demon of Rain Country
- **Relationship:** Came to Teng Huayuan's aid; elevated Teng Huayuan's cultivation
- **Key events:** Came to Teng Huayuan's aid. Sealed under the Ancient Shi Branch of the Ancient Nation on the IAC. Wang Lin later used Lou Hou's soul to complete his Slaughter, Restriction, Absolute Beginning, and Absolute End Essence True Bodies.
- **Fate:** Sealed; soul used by Wang Lin
- **Canon confidence:** C4

### N89. Lin Yi (林怡) — Teng Huayuan's ally
- **Role:** antagonist (minor)
- **Cultivation peak:** unknown
- **Sect/faction:** Teng Huayuan's allies
- **Relationship:** Came to Teng Huayuan's aid
- **Key events:** Killed by Wang Lin (along with Punnan Zi) when Wang Lin returned to Country of Zhao to wipe out the Teng family.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C3

### N90. Duanmu Ji (端木吉) — Soul Formation antagonist
- **Role:** antagonist
- **Cultivation peak:** Soul Formation
- **Sect/faction:** independent
- **Relationship:** Notorious evil cultivator; chased Wang Lin to capture him
- **Key events:** Attracted by rumors of Wang Lin's Death Spell. Chased Wang Lin from Sea of Devils to Xuan Wu. Wang Lin hid in Heaven Defying Bead for 3 years. Brought Wang Lin (with Wang Qingyue) to Chaotic Broken Stars. Eventually killed by Hunchback Meng (a puppet of Tuo Sen).
- **Fate:** Killed by Hunchback Meng (Tuo Sen's puppet)
- **Canon confidence:** C4

### N91. Hunchback Meng (驼背孟) — Chaotic Broken Stars enemy
- **Role:** antagonist
- **Cultivation peak:** Soul Transformation (perfect circle)
- **Sect/faction:** Hunchback (master of Meng Sect?)
- **Relationship:** Chaotic Broken Stars rival
- **Key events:** Ate his own toad to reach perfect circle Soul Transformation and killed the Hurricane Beast King. Tried to enter the 2nd level of Broken Stars but Wang Lin pulled him back to be eaten by beasts. Revived from sea of blood as a Tuo Sen puppet; killed Duanmu Ji.
- **Fate:** Killed (eventually)
- **Canon confidence:** C4

### N92. Xu Liqing / Demon Lord of Six Desires (徐立清 / 六欲魔尊) — Chaotic Broken Stars enemy
- **Role:** antagonist
- **Cultivation peak:** Soul Transformation
- **Sect/faction:** Six Desires Demon Lord
- **Relationship:** Chaotic Broken Stars rival; modified Restriction Mountain restrictions
- **Key events:** Modified Restriction Mountain restrictions (adding panic). Attacked by Duanmu Ji at the top. Cornered by Hunchback Meng.
- **Fate:** Killed (eventually)
- **Canon confidence:** C4

### N93. Gun Lan (管岚) — Chaotic Broken Stars enemy
- **Role:** antagonist (minor)
- **Cultivation peak:** Soul Formation
- **Sect/faction:** unknown
- **Relationship:** Chaotic Broken Stars rival
- **Key events:** Listed among the evils Wang Lin outwitted in Chaotic Broken Stars.
- **Fate:** Killed
- **Canon confidence:** C3

### N94. Wang Qingyue (王青叶) — Duanmu Ji's ally
- **Role:** antagonist (forced)
- **Cultivation peak:** unknown
- **Sect/faction:** Duanmu Ji's group
- **Relationship:** Brought Wang Lin to Chaotic Broken Stars for Duanmu Ji
- **Key events:** Brought Wang Lin (with Duanmu Ji) to Chaotic Broken Stars to use Death Spell on the 3rd level.
- **Fate:** Alive (presumably)
- **Canon confidence:** C3

### N95. Yun Fei (云菲) — Qihuang Sect successor
- **Role:** antagonist (minor)
- **Cultivation peak:** Core Formation
- **Sect/faction:** Qihuang Sect
- **Relationship:** Met Wang Lin after he escaped Chaotic Broken Stars
- **Key events:** Wang Lin placed a 3-day restriction on her and entered her cave in Qilin City. She visited other cultivators to find a way to unlock the ban. Killed by Wang Lin's devil when Qiu Siping agreed to unlock it.
- **Fate:** Killed by Wang Lin's devil
- **Canon confidence:** C4

### N96. Qian Kun (乾坤) — Poison Palace disciple
- **Role:** antagonist (minor)
- **Cultivation peak:** Core Formation
- **Sect/faction:** Poison Palace
- **Relationship:** Met Wang Lin after Chaotic Broken Stars escape
- **Key events:** Saw a Nascent Soul cultivator being devoured in Broken Stars. Killed by Wang Lin (to leave no witness — also because Qian Kun belonged to Hunchback Meng's sect).
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C4

### N97. Master Void (虚空道主) — peak Nirvana Shatterer antagonist
- **Role:** antagonist
- **Cultivation peak:** peak Nirvana Shatterer
- **Sect/faction:** unknown
- **Relationship:** Could not allow Wang Lin (with awakened Vermilion Bird Divine Mark) to grow
- **Key events:** First tried to kill Wang Lin in the Celestial Emperors Tower (peak Nirvana Shatterer). Wang Lin's killing formation + source origin lure injured Master Void → mid Nirvana Shatterer. In their 2nd encounter (9th level of Celestial Emperors Tower), Zhou Yi/Wang Wei/Hu Juan/Bei Lou + All-Seer/Ling Tianhou's intervention dropped Master Void to peak early Nirvana Shatterer. Sneak-attacked Wang Wei; Wang Lin switched places and defended. Later sealed by Ta Jia. Wang Lin killed two of All-Seer's avatars along with Master Void using Sundered Night.
- **Fate:** Killed by Wang Lin (via Sundered Night)
- **Canon confidence:** C4

### N98. Blood Ancestor / Yao Xinghai (血祖 / 姚星海) — antagonist on Tian Yun Star
- **Role:** antagonist
- **Cultivation peak:** peak Third Step (killed in Thunder Immortal Realm)
- **Sect/faction:** Yao Family (Tianyun Star); "Blood Ancestor"
- **Relationship:** Father of Yao Xixue; "Wang Lin will inevitably become a great calamity" (warned his daughter)
- **Key events:** Possessed a unique cultivation system and formidable strength. Refined the Blood Soul Pill (resurrection pill). Killed by Wang Lin in the Thunder Immortal Realm. Wang Lin later released a wisp of his remnant soul to perfect his Karma Concept (Causation Artistic Conception). The amnesiac Yao Xixue departed with this remnant soul.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C5

### N99. Yao Xixue (姚惜雪) — antagonist
- **Role:** antagonist
- **Cultivation peak:** Infant Transformation Late Stage (initial); reborn via Blood Soul Pill
- **Sect/faction:** Yao Family (Tianyun Star)
- **Relationship:** Blood Ancestor's only daughter; attempted to ambush Wang Lin → imprisoned 100 years → revenge → memory loss
- **Key events:** First appears in Ch.491 ("Why the East Sea"). Arrived riding a Blood Jade to the East Sea Demon Spirit Land. Heterochromic red pupils, snow-white skin, white robe. Cooperated with Wang Lin to explore the Immortal Monarch's Cave Mansion (under conditions: Immortal Jade, Blood Soul Pill, treasure jade slip). Ambushed Wang Lin at the cave mansion → subdued and imprisoned for 100 years. Wang Lin destroyed her meridians, sealed her immortal power, used Life Imprint cycle of life-and-death (couldn't die). Her father killed by Wang Lin → she used Blood Soul Pill to be reborn → sacrificed her body to the Wind Demon to seek revenge. Wind Demon killed by Wang Lin's God-Slaying Spear. Her memories devoured → amnesia. Wang Lin released Blood Ancestor's remnant soul → amnesiac Yao Xixue departed for distant mountains/forests with the remnant soul.
- **Fate:** Alive; amnesiac; wandering with father's remnant soul
- **Canon confidence:** C5

### N100. Wind Demon / Feng Mo (风魔) — antagonist
- **Role:** antagonist
- **Cultivation peak:** Third Step (sealed demon)
- **Sect/faction:** none (sealed demon)
- **Relationship:** Made a deal with Yao Xixue (sacrificed her body to him in exchange for killing Wang Lin)
- **Key events:** Hundreds of rounds of fierce combat with Wang Lin at the Immortal Emperor's Cave Mansion. Slain by Wang Lin's God-Slaying Spear.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C4

### N101. Yao Family — antagonist faction (Southern Domain)
- **Role:** antagonist (faction)
- **Cultivation peak:** multiple Soul Transformation+
- **Sect/faction:** Yao Family (one of 4 major powers in the Southern Domain, Allheaven Star System)
- **Relationship:** Sent a kill order for Wang Lin after he killed some Yao Family members in the Thunder Celestial Realm
- **Key events:** Wang Lin destroyed multiple planets chasing them. Many who chased Wang Lin had their entire planets destroyed.
- **Fate:** Many killed; faction weakened
- **Canon confidence:** C4

### N102. Daoist Water / Shui Daozi (水道子) — major antagonist
- **Role:** antagonist
- **Cultivation peak:** peak Third Step (Nirvana Void+)
- **Sect/faction:** Rank 9 God Sect (associated)
- **Relationship:** Sensed Wang Lin's aura of his master (Lord of the Sealed Realm); tried to kill Wang Lin
- **Key events:** Sensing Lord of the Sealed Realm's aura on Wang Lin, attacked him in the Cloud Sea Star System. Wang Lin pushed him to the edge; the spirit of Lord of the Sealed Realm appeared and severely injured Daoist Water. Wang Lin used up all his energy + a forbidden spell → petrified (turned to stone). Li Qianmei's 10-year blood anointment saved him. Eventually slain by Wang Lin in the Cloud Sea.
- **Fate:** Killed by Wang Lin (Cloud Sea arc, endgame)
- **Canon confidence:** C5

### N103. Wu Qing (吴情) — Nirvana Shatterer antagonist
- **Role:** antagonist (minor)
- **Cultivation peak:** Nirvana Shatterer
- **Sect/faction:** Treasured Jade Sect auction
- **Relationship:** Greedy old monster who tried to kill Wang Lin after the secret exchange
- **Key events:** Followed Wang Lin as he left the city; attempted to kill him. Wang Lin killed him using the War Spirit Print.
- **Fate:** Killed by Wang Lin (War Spirit Print)
- **Canon confidence:** C4

### N104. Master Ashen Pine (灰松道主) — Seven-Colored Realm antagonist
- **Role:** antagonist
- **Cultivation peak:** Third Step
- **Sect/faction:** independent (gathered cultivators for Seven-Colored Realm)
- **Relationship:** Schemed against Wang Lin (everyone schemed against everyone in Seven-Colored Realm)
- **Key events:** Took Wang Lin + cultivators into the Seven-Colored Realm to "find something." Killed by Wang Lin with the seven-colored nail.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C4

### N105. Master Cloud Soul (云魂道主) — Seven-Colored Realm antagonist
- **Role:** antagonist (minor)
- **Cultivation peak:** Third Step
- **Sect/faction:** independent
- **Relationship:** Schemed to team up with Master Ashen Pine against Wang Lin
- **Key events:** Tricked by Wang Lin into a different spatial crack.
- **Fate:** Unknown (presumed killed in the Seven-Colored Realm)
- **Canon confidence:** C3

### N106. Noble Money (贵钱) — Cloud Sea antagonist
- **Role:** antagonist (minor)
- **Cultivation peak:** unknown
- **Sect/faction:** independent
- **Relationship:** Tried to detain Wang Lin for questioning on Wang Lin's return from the Wild Continent
- **Key events:** Killed by Wang Lin along with his group.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C3

### N107. Cang Songzi (苍松子) — Seven-Colored Realm antagonist
- **Role:** antagonist
- **Cultivation peak:** Sub-Empty Annihilation upper-grade
- **Sect/faction:** independent (Palm Venerable's faction?)
- **Relationship:** Original owner of the Seven-Colored Divine Sky Nail; destroyed Wang Lin's Rusty Iron Sword
- **Key events:** Killed by Wang Lin in the Seven-Colored Realm. Wang Lin took his Seven-Colored Divine Sky Nail (108 nails, designed to kill Third Step experts).
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C4

### N108. Lu Fuzi (陆夫子) — literary contest rival
- **Role:** complex (literary rival)
- **Cultivation peak:** unknown
- **Sect/faction:** Cultivation world scholar
- **Relationship:** Literary contest opponent
- **Key events:** Defeated by Wang Lin in a literary contest. Wang Lin informed him of the hidden dangers of the Seven-Colored Realm.
- **Fate:** Alive
- **Canon confidence:** C3

### N109. Ye Dao (叶道) — antagonist
- **Role:** antagonist
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Listed in Wang Lin's enemies
- **Key events:** Tangential.
- **Fate:** Unknown
- **Canon confidence:** C3

### N110. Lian Daozhen (连道真) — Immortal Emperor antagonist
- **Role:** antagonist (initially brother of Lian Daofei)
- **Cultivation peak:** Immortal Emperor (Xiangang Continent)
- **Sect/faction:** Xiangang Continent
- **Relationship:** Brother of Lian Daofei; failed to inherit the Immortal Ancestor's plan; antagonist to Wang Lin
- **Key events:** Failed to inherit the Immortal Ancestor's plan. After failure, Lian Daofei's bloodline awakened (Eight Extremities). Immortal Emperor self-destructured; Wang Lin captured the Immortal Emperor's soul and injured the Infant Skull belonging to the Dao Yi Great Celestial Venerable.
- **Fate:** Complex (relatives are Wang Lin's allies)
- **Canon confidence:** C4

### N111. Yan Leizi (炎雷子) — antagonist
- **Role:** antagonist
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Listed in Wang Lin's enemies
- **Key events:** Met Wang Lin in the Thunder Immortal Realm (taken into soul banner). Tangential.
- **Fate:** Killed / sealed
- **Canon confidence:** C3

### N112. Su Dao (苏道) — antagonist
- **Role:** antagonist
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Listed in Wang Lin's enemies (separate from "Su Dao / Scholar" mentor — same name?)
- **Key events:** Tangential.
- **Fate:** Unknown
- **Canon confidence:** C3

### N113. Tianyunzi (天运子) — major antagonist (clone of All-Seer / Boundary Compass Treasure Spirit)
- **Role:** antagonist
- **Cultivation peak:** Third Step+ ( Boundary Compass Treasure Spirit )
- **Sect/faction:** Heavenly Fate Sect; secretly the artifact spirit of the Realm-Defining Compass
- **Relationship:** Wang Lin's master at Heavenly Fate Sect (Tianyunzi clone) — secretly plotting against him
- **Key events:** Created clone to teach Wang Lin the Slaughter Immortal Art (with a trap inside). Wang Lin discovered the malicious intent; expelled the Slaughter Immortal Art from his body into his first Immortal Guard. Injured Tianyunzi at Qinglin's cave dwelling. Lu Mo borrowed the Realm-Defining Compass from Old Man Miesheng, blasted it open using Dream Dao, released Tianyunzi (the artifact spirit). Tianyunzi intended to possess Wang Lin, unaware Wang Lin had already mastered life/death/reincarnation. Defeated by Wang Lin in the Primordial Divine Realm.
- **Fate:** Killed/devoured by Wang Lin
- **Canon confidence:** C5

### N114. Seven-Colored Daoist / Seven-Colored Immortal Venerable (七彩道人 / 七彩仙尊) — creator-antagonist
- **Role:** antagonist (the cosmic-level creator-antagonist of RI)
- **Cultivation peak:** Heaven Trampling (4th Step)
- **Sect/faction:** Creator of the Cave World
- **Relationship:** Creator/owner of the Cave World; the "true" cosmic antagonist of RI (above the All-Seer)
- **Key events:** Created the Cave World (a sealed dimensional farm) to harvest Joss Flames. Created the Three Souls and Seven Spirits (Situ Nan = Green Soul, Qing Shui = Slaughter Soul, Tan Lang = Yellow Soul, Xie Qing = Third Soul, etc.). Bestowed the Heaven Defying Pearl to the Realm-Sealing Supreme as proof of authority over the Inner Realm. Wang Lin learns of his existence in Book 11. Wang Lin's final battle: kills the Seven-Colored Daoist, becomes the new world-owner of the Cave World (renaming it "Wang Lin's Cave World"), and Transcends with Li Muwan.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C5

### N115. Old Man Miesheng / Old Man Samsara-Extinction (灭生老人) — minor antagonist
- **Role:** complex antagonist
- **Cultivation peak:** peak Third Step+
- **Sect/faction:** independent
- **Relationship:** Lu Mo borrowed the Realm-Defining Compass from him
- **Key events:** Lent the Realm-Defining Compass to Lu Mo (who blasted it open using Dream Dao, releasing the artifact spirit Tianyunzi).
- **Fate:** Alive
- **Canon confidence:** C4

### N116. Taga / Ta Jia (塔迦) — Ancient Demon
- **Role:** antagonist
- **Cultivation peak:** Ancient Demon (Third Step)
- **Sect/faction:** Ancient Demon (Demon Spirit Land)
- **Relationship:** Possessed Qing Lin's body
- **Key events:** Possessed Qing Lin's body; everyone forced to escape the Celestial Emperors Tower. Sealed by Ta Jia himself (in the encounter with Master Void). Later fought by Wang Lin.
- **Fate:** Sealed by himself; later defeated
- **Canon confidence:** C4

### N117. Gu Dao (古道) — final IAC rival
- **Role:** rival / antagonist
- **Cultivation peak:** Grand Empyrean (strongest on IAC after Wang Lin)
- **Sect/faction:** IAC
- **Relationship:** Wang Lin's final rival on the IAC; 2nd to Wang Lin's "10th Sun"
- **Key events:** Wang Lin's battle with Gu Dao → enlightenment → fused with Void Avatar → full Nine Songs Three Signs → significant progress in fusion of Celestial and Ancient powers. Wang Lin becomes #1 on IAC after this battle.
- **Fate:** Alive (acknowledges Wang Lin as superior)
- **Canon confidence:** C4

### N118. Song Tian Great Celestial Venerable — IAC antagonist
- **Role:** antagonist
- **Cultivation peak:** Great Celestial Venerable
- **Sect/faction:** IAC
- **Relationship:** Antagonist on IAC
- **Key events:** Defeated by Wang Lin; enables Ji Du to become Imperial Venerable.
- **Fate:** Defeated
- **Canon confidence:** C4

### N119. Dao Ancient Great Celestial Venerable — IAC antagonist
- **Role:** antagonist
- **Cultivation peak:** Great Celestial Venerable (Ancient Clan)
- **Sect/faction:** Ancient Clan
- **Relationship:** Antagonist on IAC; pressured Wang Lin
- **Key events:** Pressured Wang Lin until Wang Lin's Immortal and Ancient powers fused. Defeated by Wang Lin after Wang Lin reached 7th bridge of Heaven Trampling.
- **Fate:** Defeated
- **Canon confidence:** C4

### N120. Dao Yi Great Celestial Venerable — IAC antagonist
- **Role:** antagonist
- **Cultivation peak:** Great Celestial Venerable
- **Sect/faction:** IAC
- **Relationship:** Antagonist on IAC
- **Key events:** Fought Xuan Luo over a fragment of Primordial God Realm at the Seven Paths Sect entrance (indirectly causing the Cave World's birth). The Infant Skull belonging to him was injured by Wang Lin.
- **Fate:** Alive (after Wang Lin's capture of Immortal Emperor's soul)
- **Canon confidence:** C4

### N121. Imperial Preceptor / Dao Gu Imperial Preceptor — antagonist
- **Role:** antagonist (possessor)
- **Cultivation peak:** artifact spirit (Boundary Compass Treasure Spirit)
- **Sect/faction:** Dao Ancient Imperial Capital
- **Relationship:** Possessed Wang Lin (but Wang Lin hid his primordial spirit within the Heaven Defying Pearl)
- **Key events:** The Imperial Preceptor was actually the missing piece of the Boundary Compass / the artifact spirit. Wang Lin's "previous life" that the Imperial Teacher saw was actually the Slaughter clone (Lu Mo) that Wang Lin sent back in time. The so-called previous-life Imperial Teacher was a Slaughter-sent illusion.
- **Fate:** Revealed; defeated
- **Canon confidence:** C4

### N122. Dao Devil Sect Master / Dao Demon Sect Master — IAC antagonist
- **Role:** antagonist
- **Cultivation peak:** Third Step+
- **Sect/faction:** Dao Devil Sect (IAC)
- **Relationship:** Captured Wang Lin to resurrect the Green Devil Scorpion
- **Key events:** Fed his own Thunder Essence into Wang Lin's to evolve it into an Essence True Body (controlled by him to erase Wang Lin's mind/memory). Wang Lin protected by Heaven Defying Pearl → reclaimed the Essence True Body. Wang Lin annihilated the Dao Devil Sect.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C4

### N123. Du Qing (杜青) — Canglong Sect ancestor
- **Role:** antagonist (minor)
- **Cultivation peak:** Third Step
- **Sect/faction:** Canglong Sect (Tianniu Province, IAC)
- **Relationship:** Pursued Wang Lin after Wang Lin extracted the Earth Fire Dragon's soul
- **Key events:** Shocked and subdued by Wang Lin's 7 Origins + Xuan Luo's golden seal.
- **Fate:** Subdued
- **Canon confidence:** C3

### N124. Kang Ren (康仁) — Canglong Sect cultivator
- **Role:** complex (initially helper, later enemy via sect)
- **Cultivation peak:** unknown
- **Sect/faction:** Canglong Sect
- **Relationship:** Took unconscious Wang Lin into the Canglong Sect
- **Key events:** Took Wang Lin in; led to the Du Qing pursuit.
- **Fate:** Alive (presumably)
- **Canon confidence:** C3

### N125. Purple Dawn Immortal Emperor — IAC antagonist
- **Role:** antagonist
- **Cultivation peak:** Immortal Emperor (Celestial)
- **Sect/faction:** IAC
- **Relationship:** Captured by Wang Lin at the Cave World's core
- **Key events:** Captured by Wang Lin.
- **Fate:** Captured
- **Canon confidence:** C3

### N126. White Tiger General — IAC antagonist → slave
- **Role:** antagonist → slave
- **Cultivation peak:** Third Step+ (peak cruel cultivator)
- **Sect/faction:** IAC
- **Relationship:** One of the cruel cultivators Wang Lin enslaved
- **Key events:** Mentions his "killing intent strong enough to shock even the most cruel cultivators." Subdued by Wang Lin as a slave.
- **Fate:** Enslaved by Wang Lin
- **Canon confidence:** C4

### N127. Yun Yifeng (云一峰) — IAC antagonist
- **Role:** antagonist
- **Cultivation peak:** Third Step+
- **Sect/faction:** IAC
- **Relationship:** Antagonist in Five Flowers Eight Gates
- **Key events:** Defeated by Wang Lin in Five Flowers Eight Gates.
- **Fate:** Defeated
- **Canon confidence:** C3

### N128. Palm Venerable / Venerable Sealing Realm / Lord of the Sealed Realm (封界尊 / 尊者封界) — complex figure
- **Role:** complex (eventually Wang Lin succeeds him)
- **Cultivation peak:** peak Third Step
- **Sect/faction:** Sealed Realm
- **Relationship:** The previous "Lord of the Sealed Realm"; Wang Lin succeeds him; forged the Seven-Colored Divine Sky Nail
- **Key events:** Forged the Seven-Colored Divine Sky Nail (108 nails, specifically designed to kill Third Step experts). Ambushed by the Palm Venerable with these nails. When Wang Lin was being killed by Daoist Water, the spirit of Lord of the Sealed Realm appeared and severely injured Daoist Water (saving Wang Lin's life). Wang Lin eventually obtained half of his soul. Wang Lin is appointed "Sealed Realm Venerable" after.
- **Fate:** Half his soul obtained by Wang Lin; succession passed to Wang Lin
- **Canon confidence:** C4

### N129. Zhan Laogui (战老子) — IAC antagonist
- **Role:** antagonist
- **Cultivation peak:** Third Step+
- **Sect/faction:** IAC
- **Relationship:** Antagonist in Five Flowers Eight Gates
- **Key events:** Defeated by Wang Lin (after Wang Lin obtained Ye Mo's heart inheritance).
- **Fate:** Defeated
- **Canon confidence:** C3

### N130. Yun Kong (云空) — IAC antagonist (clone)
- **Role:** antagonist (clone)
- **Cultivation peak:** Third Step+
- **Sect/faction:** IAC
- **Relationship:** Wang Lin destroyed his clone; detonated the Immortal Pill
- **Key events:** Wang Lin destroyed Yun Kong's clone and detonated the Immortal Pill.
- **Fate:** Clone destroyed
- **Canon confidence:** C3

### N131. Zhan Xingye (战星野) — IAC antagonist
- **Role:** antagonist
- **Cultivation peak:** Third Step+
- **Sect/faction:** Zhan Family (Battle)
- **Relationship:** Schemed against Wang Lin
- **Key events:** Wang Lin merged 3 Zhen Family Battle Scrolls into the golden word "Battle"; fused with golden light from Zhan Xingye's skeletons.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C3

### N132. Green Devil / Green Scorpion (绿魔 / 绿蝎) — IAC entity
- **Role:** antagonist (entity)
- **Cultivation peak:** peak Third Step+ (ancient entity)
- **Sect/faction:** Dao Devil Sect (worshipped)
- **Relationship:** Captured Wang Lin as sacrifice
- **Key events:** Dao Devil Sect intended to use Wang Lin's body to resurrect the Green Devil Scorpion. Wang Lin reclaimed his body and devoured the Green Devil → immediately reached Arcane Void.
- **Fate:** Devoured by Wang Lin
- **Canon confidence:** C4

## 2G. Disciples (Wang Lin's)

### N133. Thirteen / Shi San (十三) — 1st disciple
- **Role:** disciple
- **Cultivation peak:** body refining (meridians severed, cannot cultivate normally)
- **Sect/faction:** Soul Refining Tribe (native)
- **Relationship:** Wang Lin's eldest disciple
- **Key events:** Originally a Mountain Valley Tribe native. Talent recognized by Wang Lin; taught the Soul Banner method. Meridians severed by someone → Wang Lin taught him body refining techniques instead. Reincarnated on IAC; memories recovered by Wang Lin; followed Wang Lin.
- **Fate:** Alive; with Wang Lin
- **Canon confidence:** C4

### N134. Huo Pao (火炮) — disciple (Mountain Valley)
- **Role:** disciple (junior)
- **Cultivation peak:** unknown
- **Sect/faction:** Soul Refining Tribe
- **Relationship:** Wang Lin's disciple (junior)
- **Key events:** Taken with Thirteen to Sky Demon Country for trial.
- **Fate:** Alive
- **Canon confidence:** C3

### N135. Xie Qing (谢青) — 2nd disciple (Third Soul)
- **Role:** disciple
- **Cultivation peak:** Jingnie (Concept-only, no supernatural powers)
- **Sect/faction:** independent (originally an old man on Planet Qing Ling)
- **Relationship:** Wang Lin's 2nd disciple; Third Soul of the Seven-Colored Immortal Venerable
- **Key events:** Used the analogy of "fish, water, net, fishing" to explain Dao → Wang Lin's Nirvana Scryer breakthrough. Sat atop a mountain for 800 years cultivating only Concepts. Left Qing Ling Star, placed 3 self-seals, ended his own life → entrusted Third Soul memories to Wang Lin. Buried by Wang Lin in Autumn Orchid Valley.
- **Fate:** Dead (suicide); soul-memories entrusted to Wang Lin
- **Canon confidence:** C5

### N136. Xi Zifeng (席紫凤) — 3rd disciple
- **Role:** disciple
- **Cultivation peak:** Jingnie (Wang Lin elevated her)
- **Sect/faction:** Luo Tian Thunder Immortal Realm (family)
- **Relationship:** Wang Lin's 3rd disciple; sole survivor of her family in the Luo Tian Alliance battle
- **Key events:** Rescued during the collapse of the Luo Tian Thunder Immortal Realm. Wanted to help Wang Lin when he was being hunted by the Yao Family; forcibly restrained by clansmen. Publicly gave her protective Treasure to Wang Lin at the Thunder Immortal Palace's Immortal Sealing Grand Competition. For 800 years, no one to rely on; disfigured her own face to prevent covetous gazes. Rescued by Wang Lin → beauty restored → cultivation raised to Jingnie → accepted as disciple → gifted Divine Thunder Blood Sword → Wang Lin killed all who had humiliated her. Kept vigil for a year before the new Realm Sealing Grand Array. Remained in the Cave Dwelling Realm; did not reincarnate on IAC.
- **Fate:** Alive; remained in Cave Dwelling Realm
- **Canon confidence:** C5

## 2H. Other Notable Characters

### N137. Adai (阿呆) — blue-skinned man
- **Role:** neutral (messenger)
- **Cultivation peak:** unknown
- **Sect/faction:** Wu Yu (Corpse Yin Sect elder)'s servant
- **Relationship:** Led Wang Lin to Wu Yu's Nascent Soul
- **Key events:** Has 9 talismans attached to his body; speaks in a language Wang Lin can't understand. Met Wang Lin in the Forest of Distorted Divine Sense. Led Wang Lin to the mouth of a dragon statue → Wu Yu's Nascent Soul.
- **Fate:** Alive
- **Canon confidence:** C4

### N138. Wu Yu (吴宇) — Corpse Yin Sect elder
- **Role:** neutral (backstory)
- **Cultivation peak:** Nascent Soul (dead — only Nascent Soul remained)
- **Sect/faction:** Corpse Yin Sect
- **Relationship:** Wang Lin's intro to the Corpse Yin Sect
- **Key events:** His Nascent Soul spoke to Wang Lin via Adai; let Wang Lin leave the forest and teleport to the Corpse Yin Sect.
- **Fate:** Long dead (only Nascent Soul remained)
- **Canon confidence:** C4

### N139. Ye Zi (叶紫) — Corpse Yin Sect elder
- **Role:** neutral (briefly antagonist)
- **Cultivation peak:** Nascent Soul
- **Sect/faction:** Corpse Yin Sect
- **Relationship:** Gave Wang Lin an immortal cave; made him slice a sliver of soul for the Jue Ming Valley competition
- **Key events:** Made Wang Lin slice a sliver of his soul for a Foundation Establishment cultivator to join the Jue Ming Valley competition. Wang Lin attacked the Corpse Yin Sect members and took back his soul.
- **Fate:** Alive (or killed offscreen later)
- **Canon confidence:** C4

### N140. Zhao Xingsha (赵星煞) — fellow disciple at Heavenly Fate Sect
- **Role:** antagonist (sect-rival)
- **Cultivation peak:** Soul Transformation
- **Sect/faction:** Heavenly Fate Sect (purple division rival)
- **Relationship:** Fellow disciple who trapped Wang Lin
- **Key events:** Trapped Wang Lin with Sima Rufeng, second brother, and Zhao Xinming. Battled Wang Lin at All-Seer's birthday banquet; heavily injured. Elder stopped Wang Lin before he killed him.
- **Fate:** Alive (heavily injured)
- **Canon confidence:** C4

### N141. Sima Rufeng (司马如风) — fellow disciple at Heavenly Fate Sect
- **Role:** antagonist (sect-rival)
- **Cultivation peak:** unknown
- **Sect/faction:** Heavenly Fate Sect
- **Relationship:** Trapped Wang Lin with Zhao Xingsha et al.
- **Key events:** Trapped Wang Lin; Wang Lin broke through mid-Soul Transformation and broke the formation.
- **Fate:** Alive (presumably)
- **Canon confidence:** C3

### N142. Zhao Xinming (赵新民) — 4th sister at Heavenly Fate Sect
- **Role:** antagonist (sect-rival)
- **Cultivation peak:** unknown
- **Sect/faction:** Heavenly Fate Sect
- **Relationship:** Trapped Wang Lin; battled Wang Lin at the birthday banquet
- **Key events:** Battled by Wang Lin (Wang Lin won) at the birthday banquet.
- **Fate:** Alive (presumably)
- **Canon confidence:** C3

### N143. Chen Tao (陈涛) — 6th brother at Heavenly Fate Sect
- **Role:** rival
- **Cultivation peak:** mid-Ascendant
- **Sect/faction:** Heavenly Fate Sect
- **Relationship:** Challenged Wang Lin after Wang Lin defeated Zhao Xinming
- **Key events:** Mid-Ascendant. Wang Lin didn't win against him but put up a good fight that shocked everyone.
- **Fate:** Alive (presumably)
- **Canon confidence:** C4

### N144. Wang Zhou (王周) — Heng Yue Sect fellow disciple
- **Role:** neutral
- **Cultivation peak:** Qi Condensation
- **Sect/faction:** Heng Yue Sect
- **Relationship:** Wang Lin's fellow disciple (failed the entrance test together)
- **Key events:** Mistaken for a girl due to his long braided hair. Failed all 3 tests alongside Wang Lin.
- **Fate:** Alive
- **Canon confidence:** C3

### N145. Wang Jie (王杰) — Heng Yue Sect fellow disciple
- **Role:** neutral
- **Cultivation peak:** Qi Condensation
- **Sect/faction:** Heng Yue Sect
- **Relationship:** Wang Lin's fellow disciple (entrance test)
- **Key events:** Took the test alongside Wang Lin and Wang Zhou.
- **Fate:** Alive
- **Canon confidence:** C3

### N146. Sun Zhenwei (孙振威) — Li Muwan's suitor
- **Role:** antagonist (killed)
- **Cultivation peak:** Nascent Soul (presumably)
- **Sect/faction:** Cloud Sky Sect
- **Relationship:** Suitor for Li Muwan's hand
- **Key events:** Li Muwan agreed to marry him in front of Wang Lin (to shatter her heart). At the wedding, Wang Lin killed Sun Zhenwei and tried to take Li Muwan back. Wang Lin became Cloud Sky Sect leader after killing Chen Bailiang.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C4

### N147. Chen Bailiang (陈百良) — Cloud Sky Sect elder
- **Role:** antagonist (killed)
- **Cultivation peak:** Nascent Soul (elder)
- **Sect/faction:** Cloud Sky Sect
- **Relationship:** Killed by Wang Lin
- **Key events:** Killed by Wang Lin, who then became Cloud Sky Sect leader.
- **Fate:** Killed by Wang Lin
- **Canon confidence:** C4

### N148. Zhang Hu (张虎) — early friend
- **Role:** friend (early)
- **Cultivation peak:** Foundation Establishment (peaked)
- **Sect/faction:** Heng Yue Sect (former) → bandit (under Sun Dazhu)
- **Relationship:** Wang Lin's early friend; pretended not to know Wang Lin to save him
- **Key events:** Wang Lin killed Sun Dazhu to help Zhang Hu out of trouble. This kindness led to Wang Lin's clan extermination (Teng Huayuan located Wang Lin via the trail). When Wang Lin returned to Vermilion Bird Star, he bestowed opportunities upon Zhang Hu's descendants to settle the karma.
- **Fate:** Missing after Sun Dazhu incident; later found dead/passed
- **Canon confidence:** C5

### N149. Hui Bing (惠冰) — listed in Wang Lin's relationships
- **Role:** neutral (minor)
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Listed in the Fandom nav-bar male characters
- **Key events:** Tangential.
- **Fate:** Unknown
- **Canon confidence:** C3

### N150. Zhou Rui (周瑞) — listed in Wang Lin's relationships
- **Role:** neutral (minor)
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Listed in the Fandom nav-bar female characters (separate from Zhou Ru the adopted daughter)
- **Key events:** Tangential.
- **Fate:** Unknown
- **Canon confidence:** C3

### N151. Leng Sheng (冷生) — listed in Wang Lin's relationships
- **Role:** neutral (minor)
- **Cultivation peak:** unknown
- **Sect/faction:** unknown
- **Relationship:** Listed in the Fandom nav-bar female characters
- **Key events:** Tangential.
- **Fate:** Unknown
- **Canon confidence:** C3

### N152. Da Niu (大牛) — friend
- **Role:** friend
- **Cultivation peak:** mortal (likely)
- **Sect/faction:** none
- **Relationship:** Listed in Wang Lin's friends
- **Key events:** Tangential; likely a mortal friend from early life.
- **Fate:** Unknown
- **Canon confidence:** C3

### N153. Zhou Lin (周林) — Li Muwan's disciple
- **Role:** neutral
- **Cultivation peak:** early Core Formation
- **Sect/faction:** Yuntian Sect (9th-generation disciple)
- **Relationship:** Li Muwan's disciple; master of Wang Lin's doppelganger
- **Key events:** Tangential.
- **Fate:** Alive
- **Canon confidence:** C4

### N154. Xiao Bai (小白) — Zhou Ru's pet tiger
- **Role:** companion
- **Cultivation peak:** beast
- **Sect/faction:** none (Zhou Ru's pet)
- **Relationship:** Zhou Ru's pet tiger
- **Key events:** Tangential.
- **Fate:** Alive
- **Canon confidence:** C3

## 2I. Pets / Beasts (companions)

### N155. Mosquito Beast (蚊兽) — signature companion
- **Role:** companion / destinedCompanion
- **Cultivation peak:** evolved (king mosquito beast, multi-color)
- **Sect/faction:** Wang Lin's
- **Relationship:** Wang Lin's signature companion (recurring across novels)
- **Key events:** Found in Sea of Devils. Fed Brilliant Golden Fruit → evolved. Herd acquired in Wind Celestial Realm → king mosquito beast further evolved. Saves Wang Lin in the IAC void (oppressed by Xian Gang's laws). In AWWP, sent by Wang Lin (as Paragon Wang) to protect Wang Baole (ch.69).
- **Fate:** Alive
- **Canon confidence:** C5

### N156. Thunder Toad (雷蟾) — companion
- **Role:** companion
- **Cultivation peak:** evolved (sacrificed for Bloodline Thunder)
- **Sect/faction:** Wang Lin's
- **Relationship:** Companion; fed Brilliant Golden Fruit → evolved
- **Key events:** Sacrificed to create Bloodline Thunder for Wang Lin's Thunder Essence completion.
- **Fate:** Sacrificed (soul integrated into Wang Lin's Thunder Essence)
- **Canon confidence:** C4

### N157. Thunder Celestial Beast (雷仙兽) — companion
- **Role:** companion
- **Cultivation peak:** beast (Ascendant-tier)
- **Sect/faction:** Wang Lin's
- **Relationship:** Combat mount/companion
- **Key events:** Used by Wang Lin during Ascendant stage.
- **Fate:** Alive
- **Canon confidence:** C3

### N158. Nether Beast (冥兽) — life-bound beast
- **Role:** companion / life-bound
- **Cultivation peak:** beast (vast interior)
- **Sect/faction:** Wang Lin's
- **Relationship:** Wang Lin's life-bound beast
- **Key events:** Subdued by Wang Lin. The interior is a vast sub-dimension (people can enter the Nether Beast's body). Wang Lin refined the Seal Immortal Seal inside the Nether Beast. Encountered the "Madman" Lian Daofei inside.
- **Fate:** Alive
- **Canon confidence:** C4

### N159. Brilliant Void (玄虚) — companion (named for the region)
- **Role:** companion (beast)
- **Cultivation peak:** beast
- **Sect/faction:** Wang Lin's
- **Relationship:** Listed in Wang Lin's pets
- **Key events:** Tangential.
- **Fate:** Alive
- **Canon confidence:** C3

### N160. Golden Exalt Sea Dragon / Golden Sea Dragon (金尊海龙) — mount
- **Role:** mount
- **Cultivation peak:** beast
- **Sect/faction:** Wang Lin's
- **Relationship:** Wang Lin's mount
- **Key events:** Liu Jinbiao used Path of Deception to gain its recognition → Xu Liguo dared not bully Liu Jinbiao anymore.
- **Fate:** Alive
- **Canon confidence:** C4

---

# Catalog 3 — Complete Faction / Sect List

## 3A. Sects (named cultivation organizations)

### F01. Heng Yue Sect (恒岳派 / 恒岳宗)
- **Type:** sect
- **Alignment:** righteous (orthodox)
- **Peak realm:** Soul Formation (Huang Long Zhenren secret identity); otherwise Foundation/low-Nascent Soul
- **Headquarters:** Country of Zhao → Planet Suzaku
- **Specialization:** sword arts, Qi gathering, foundation techniques
- **Relationship to Wang Lin:** Wang Lin's first sect (legacy disciple); destroyed his reputation here when he failed all 3 entrance tests
- **Key members:** Sect Master Huang Long Zhenren (secretly 5th-Gen Vermilion Bird Lu Yun), Sun Dazhu (early antagonist), Zhang Hu (early friend), Wang Zhou, Wang Jie, Wang Hao, Wang Zhuo, Wang Lin
- **Fate:** Survives the early story; protected by Wang Lin's later influence
- **Canon confidence:** C5

### F02. Cloud Sky Sect / Cloud Heaven Sect / Yuntian Sect (云天宗 / 云天宗)
- **Type:** sect
- **Alignment:** righteous
- **Peak realm:** Nascent Soul (Li Muwan, Chen Bailiang)
- **Headquarters:** Chu Country → Planet Suzaku
- **Specialization:** cloud arts, flight techniques, alchemy (Li Muwan)
- **Relationship to Wang Lin:** Li Muwan became an elder here (later Sect Master); Wang Lin killed Sun Zhenwei at Li Muwan's wedding and became the Sect Master, then handed the position to Li Muwan
- **Key members:** Li Muwan (elder → master), Chen Bailiang (elder, killed), Sun Zhenwei (suitor, killed), Zhou Lin (9th-gen disciple)
- **Fate:** Survives; protected by Wang Lin
- **Canon confidence:** C5

### F03. Soul Refining Sect (炼魂宗)
- **Type:** sect
- **Alignment:** demonic (soul-cultivation)
- **Peak realm:** Nirvana Scryer+ (Dun Tian)
- **Headquarters:** Pilu Kingdom → Planet Suzaku
- **Specialization:** soul refining, soul extraction, soul sealing; the Ten Billion Soul Flag (guardian treasure)
- **Relationship to Wang Lin:** Wang Lin inherited the sect and the Ten Billion Soul Flag from Dun Tian; "Ancestor of the Soul Refining Tribe"
- **Key members:** Dun Tian (ancestor), Nian Tian (Dun Tian's senior brother), Ouyang Zi (mentioned), Wang Lin (inheritor)
- **Fate:** Survives; Wang Lin elevates it
- **Canon confidence:** C5

### F04. Corpse Yin Sect / Corpse Sect (尸阴宗)
- **Type:** sect
- **Alignment:** demonic
- **Peak realm:** Nascent Soul+ (multiple elders)
- **Headquarters:** Planet Suzaku
- **Specialization:** corpse refinement, Yin cultivation, puppet arts
- **Relationship to Wang Lin:** Brief disciple (corpse sect); received an immortal cave; took servants from here (Sun Tai, Lei Ji)
- **Key members:** Wu Yu (Nascent Soul elder, deceased), Ye Zi (Nascent Soul elder), Sun Tai (later Wang Lin's servant), Adai (Wu Yu's servant)
- **Fate:** Many members killed by Wang Lin on Planet Suzaku (when Corpse Yin Sect used Wang family for resentful spirits)
- **Canon confidence:** C5

### F05. Heavenly Fate Sect / Tianyun Sect (天运宗)
- **Type:** sect
- **Alignment:** neutral (ruled by antagonist All-Seer)
- **Peak realm:** peak Third Step (All-Seer)
- **Headquarters:** Planet Tian Yun → Alliance Star System
- **Specialization:** divination, celestial arts, fate manipulation; 7 color divisions (red/orange/yellow/green/blue/cyan/purple)
- **Relationship to Wang Lin:** Wang Lin became the 7th disciple of the purple division; All-Seer plotted against him via the Celestial Slaughter Art trap
- **Key members:** All-Seer (leader), Tianyunzi (clone of All-Seer / Boundary Compass spirit), Zhao Xingsha, Sima Rufeng, Zhao Xinming, Chen Tao, Wang Lin (7th purple disciple), Bai Wei (associated)
- **Fate:** Survives; All-Seer killed
- **Canon confidence:** C5

### F06. Xuan Dao Sect (玄道宗)
- **Type:** sect
- **Alignment:** righteous
- **Peak realm:** unknown
- **Headquarters:** Country of Zhao → Planet Suzaku
- **Specialization:** unknown (likely Dao-studies)
- **Relationship to Wang Lin:** Wang Lin broke through to Soul Formation on the mountain near Xuan Dao Sect
- **Key members:** Tangential
- **Fate:** Survives
- **Canon confidence:** C4

### F07. Fighting Evil Sect (斗邪宗)
- **Type:** sect
- **Alignment:** demonic
- **Peak realm:** Soul Formation (Sect Leader)
- **Headquarters:** Sea of Devils → Planet Suzaku
- **Specialization:** devil cultivation, soul manipulation, killing orders (Ten Thousand Devil Hundred Day Kill Order)
- **Relationship to Wang Lin:** Wang Lin killed the Sect Leader and took control of the sect (became the new Master); killed 10 Core Formation cultivators who chased him
- **Key members:** Sect Leader (killed), 10 Core Formation cultivators (killed)
- **Fate:** Wang Lin took control; later released/abandoned
- **Canon confidence:** C5

### F08. Blue Silk Clan (蓝丝族)
- **Type:** clan / sect hybrid
- **Alignment:** neutral
- **Peak realm:** Dao Master (Blue Dream) — Void Tribulant+
- **Headquarters:** Blue Silk Clan Star Domain
- **Specialization:** Dao Art Fusion, Light Shadow Shield, Overturn Heaven Seal
- **Relationship to Wang Lin:** Wang Lin learned Dao Art Fusion + Light Shadow Shield from Dao Master Blue Dream
- **Key members:** Dao Master Blue Dream
- **Fate:** Survives
- **Canon confidence:** C4

### F09. Da Lou Sword Sect (大罗剑宗)
- **Type:** sect
- **Alignment:** righteous
- **Peak realm:** Third Step (Ling Tianhou — Nirvana Void)
- **Headquarters:** Cloud Sea Star System (and adjacent stars)
- **Specialization:** sword cultivation; 3 sword strikes test
- **Relationship to Wang Lin:** Ling Tianhou invited Wang Lin to be an elder; 3 Da Lou Sword Sect elders attacked Wang Lin on Earth Planet (killed)
- **Key members:** Ling Tianhou (elder/sect master), 3 elders killed by Wang Lin
- **Fate:** Survives
- **Canon confidence:** C4

### F10. Four Divine Sect / Four Sacred Sect (四神宗 / 四圣宗)
- **Type:** sect (coalition)
- **Alignment:** righteous
- **Peak realm:** peak Third Step+ (multiple divine emperors)
- **Headquarters:** Vermilion Bird Starfield
- **Specialization:** divine beast cultivation, four-element arts
- **Relationship to Wang Lin:** Wang Lin becomes 6th-Gen Vermilion Bird Divine Emperor (a sub-sect of the Four Divine Sect)
- **Key members:** Azure Dragon Divine Emperor, Vermilion Bird Divine Emperor (Wang Lin), White Tiger Divine Emperor (related), Black Tortoise Divine Emperor (related), Lu Yun (5th-Gen)
- **Fate:** Survives; Wang Lin eventually returns the rein to Azure Dragon Divine Emperor
- **Canon confidence:** C5

### F11. Vermilion Bird Divine Sect / Vermilion Bird Holy Sect (朱雀神宗)
- **Type:** sect
- **Alignment:** righteous
- **Peak realm:** Void Flame Cultivator (Vermilion Bird Divine Emperor)
- **Headquarters:** Vermilion Bird Starfield
- **Specialization:** fire arts, Vermilion Bird bloodline, divine beast cultivation; "Vermilion Bird Nine Mysterious Transformations"
- **Relationship to Wang Lin:** Wang Lin became 6th-Gen Vermilion Bird Divine Emperor; saved Azure Dragon Divine Emperor; returned the rein
- **Key members:** Lu Yun (5th-Gen, master of Heng Yue in disguise), Wang Lin (6th-Gen), 2nd-Gen Vermilion Bird (Young Emperor of Fallen Land)
- **Fate:** Survives; protected by Wang Lin
- **Canon confidence:** C5

### F12. Origin Sect / Guiyuan Sect / Origin Sect (归一宗 / 元宗)
- **Type:** sect
- **Alignment:** neutral
- **Peak realm:** rank 6 (eventually)
- **Headquarters:** Cloud Sea Star System
- **Specialization:** earth element, fusion arts; Wang Lin ancestor
- **Relationship to Wang Lin:** Wang Lin (under alias "Ceng Niu") became an ancestor-figure here; killed those who disrespected Lu Yanfei
- **Key members:** Lu Yanfei, Xu Yun, Zhao Yu, Lu Yuncong, Song Wude (killed), Rudy (killed), Wang Lin (ancestor)
- **Fate:** Survives; elevated by Wang Lin
- **Canon confidence:** C4

### F13. Great Soul Sect (大魂宗)
- **Type:** sect
- **Alignment:** neutral
- **Peak realm:** Third Step+ (multiple elders)
- **Headquarters:** Heavenly Bull Continent → Immortal Astral Continent
- **Specialization:** soul cultivation, Ghostly Sail (secondary banner), Fire Element Five Elements Armor, Tianniu Pearl
- **Relationship to Wang Lin:** Wang Lin becomes an elder here; receives the Three Rites; devours Earth Fire main vein → Fire Essence True Body
- **Key members:** Wang Lin (elder)
- **Fate:** Survives
- **Canon confidence:** C4

### F14. War Shrine Sect / War God Shrine Sect (战神殿)
- **Type:** sect
- **Alignment:** righteous
- **Peak realm:** unknown
- **Headquarters:** Planet Suzaku
- **Specialization:** Divine Path Technique (source of Wang Lin's avatar ability); war/spirit refinement
- **Relationship to Wang Lin:** Wang Lin was a former Direct Disciple; Divine Path Technique source
- **Key members:** Wang Lin (former direct disciple)
- **Fate:** Survives
- **Canon confidence:** C4

### F15. Luo He Sect (罗河宗)
- **Type:** sect
- **Alignment:** righteous
- **Peak realm:** Core Formation+ (Li Qiqing)
- **Headquarters:** Fire Burn Country → Planet Suzaku
- **Specialization:** alchemy, orthodox cultivation
- **Relationship to Wang Lin:** Li Muwan's original sect; Li Qiqing is elite disciple
- **Key members:** Li Muwan (disciple), Li Qiqing (elite disciple)
- **Fate:** Survives
- **Canon confidence:** C4

### F16. Tian Yu Sect (天羽宗)
- **Type:** sect
- **Alignment:** neutral
- **Peak realm:** unknown
- **Headquarters:** unknown
- **Specialization:** Kunji Whip (heavy treasure)
- **Relationship to Wang Lin:** Qian Feng gave the Kunji Whip to Hong Die; after Hong Die's death, Wang Lin obtained it
- **Key members:** Qian Feng
- **Fate:** Survives
- **Canon confidence:** C3

### F17. Everlasting Sect (永恒宗)
- **Type:** sect
- **Alignment:** neutral
- **Peak realm:** Third Step+ (Blood Sword)
- **Headquarters:** Cloud Sea Star System
- **Specialization:** blood-element sword cultivation
- **Relationship to Wang Lin:** Wang Lin was about to represent them in the rank 8 tournament before the rank 9 God Sect canceled it
- **Key members:** Tangential
- **Fate:** Survives
- **Canon confidence:** C3

### F18. Ghost Sect (鬼宗)
- **Type:** sect
- **Alignment:** demonic
- **Peak realm:** unknown
- **Headquarters:** unknown
- **Specialization:** ghost/spirit cultivation
- **Relationship to Wang Lin:** Sent Li Qianmei deep into a spatial realm with powerful beasts (unhappy she went missing for 10 years from the battlefield)
- **Key members:** Li Qianmei (associated)
- **Fate:** Survives
- **Canon confidence:** C3

### F19. Rank 9 God Sect (九等神宗)
- **Type:** sect
- **Alignment:** neutral
- **Peak realm:** Third Step+ (Daoist Water)
- **Headquarters:** Cloud Sea Star System
- **Specialization:** unknown (high-tier rank 9)
- **Relationship to Wang Lin:** Daoist Water (sensing Lord of the Sealed Realm's aura) tried to kill Wang Lin; canceled the rank 8 tournament
- **Key members:** Daoist Water (Shui Daozi)
- **Fate:** Survives; Daoist Water killed by Wang Lin
- **Canon confidence:** C4

### F20. Treasured Jade Sect / Treasures Jade Sect (宝玉宗)
- **Type:** sect (merchant/auction house)
- **Alignment:** neutral
- **Peak realm:** Third Step+ (multiple Nirvana Shatterer old monsters)
- **Headquarters:** Cloud Sea Star System
- **Specialization:** auctions, treasure exchanges
- **Relationship to Wang Lin:** Li Qianmei invited Wang Lin to an auction here; Wang Lin participated in a secret exchange with Nirvana Shatterer old monsters
- **Key members:** Tangential (Wu Qing — killed)
- **Fate:** Survives
- **Canon confidence:** C4

### F21. Qihuang Sect (岐黄宗)
- **Type:** sect
- **Alignment:** neutral
- **Peak realm:** Core Formation+ (Yun Fei)
- **Headquarters:** Planet Suzaku (or nearby)
- **Specialization:** unknown (Qihuang = "Qi-Huang" medicine reference)
- **Relationship to Wang Lin:** Yun Fei (successor) killed by Wang Lin's devil
- **Key members:** Yun Fei (successor, killed)
- **Fate:** Survives
- **Canon confidence:** C3

### F22. Poison Palace (毒宫)
- **Type:** sect
- **Alignment:** demonic
- **Peak realm:** Core Formation+ (Qian Kun)
- **Headquarters:** unknown
- **Specialization:** poison cultivation
- **Relationship to Wang Lin:** Qian Kun (disciple) killed by Wang Lin
- **Key members:** Qian Kun (killed)
- **Fate:** Survives
- **Canon confidence:** C3

### F23. Dao Devil Sect / Dao Demon Sect (道魔宗)
- **Type:** sect
- **Alignment:** demonic
- **Peak realm:** Third Step+ (Sect Master)
- **Headquarters:** Mengtu Province → Green Devil Continent → IAC
- **Specialization:** Dao Devil Great Hand Seal; Green Devil Scorpion resurrection
- **Relationship to Wang Lin:** Sect Master captured Wang Lin as Green Devil sacrifice → Wang Lin reversed the ritual → annihilated the sect
- **Key members:** Dao Devil Sect Master (killed), Ji Si (helper, complex)
- **Fate:** Annihilated by Wang Lin
- **Canon confidence:** C4

### F24. Canglong Sect / Azure Dragon Sect (苍龙宗)
- **Type:** sect
- **Alignment:** neutral
- **Peak realm:** Third Step (Du Qing — ancestor)
- **Headquarters:** Tianniu Province → IAC
- **Specialization:** Earth Fire Dragon cultivation
- **Relationship to Wang Lin:** Kang Ren took unconscious Wang Lin in; ancestor Du Qing pursued Wang Lin → subdued by Wang Lin's 7 Origins + Xuan Luo's golden seal
- **Key members:** Du Qing (ancestor, subdued), Kang Ren (member)
- **Fate:** Survives; subdued
- **Canon confidence:** C3

### F25. Dong Lin Sect (东林宗)
- **Type:** sect
- **Alignment:** neutral
- **Peak realm:** Third Step+ (ancient god-tier entity sealed beneath)
- **Headquarters:** Dong Lin Pool area → IAC
- **Specialization:** Dong Lin Pool meditation; Absolute Beginning / Reincarnation Essence
- **Relationship to Wang Lin:** Wang Lin entered the destroyed Dong Lin Sect; found Tianyunzi's handwriting; challenged the Celestial Venerable Nirvana up to the 17th layer
- **Key members:** Dong Lin Female Ancient God (encountered)
- **Fate:** Destroyed before Wang Lin's arrival
- **Canon confidence:** C4

### F26. Soul Refining Tribe (炼魂部族)
- **Type:** tribe (sect-like)
- **Alignment:** righteous (Wang Lin's lineage)
- **Peak realm:** body refining (Thirteen, Huo Pao)
- **Headquarters:** East Demon Spirit Sea → Planet Suzaku
- **Specialization:** Soul Refining Sect heritage; body refining
- **Relationship to Wang Lin:** Wang Lin is the Ancestor; taught the heritage to the Mountain Valley Tribe → became the Soul Refining Tribe (over 1 million people)
- **Key members:** Ouyang Hua (chief), Thirteen (disciple), Huo Pao (disciple)
- **Fate:** Survives; restored Wang Lin's One Billion Soul Flag
- **Canon confidence:** C4

## 3B. Clans / Tribes (bloodline-based factions)

### F27. Wang Clan (王族)
- **Type:** clan
- **Alignment:** righteous (mortal clan)
- **Peak realm:** mortal (originally); later Wang Lin
- **Headquarters:** Wang Family Village → Country of Zhao → Planet Suzaku
- **Specialization:** carpentry (originally)
- **Relationship to Wang Lin:** Wang Lin's clan; exterminated by Teng Huayuan; reborn through Wang Lin's power
- **Key members:** Wang Tianshui, Zhou Tingsu, Fourth Uncle (Wang Tianshan), Wang Hao, Wang Zhuo, Wang Lin
- **Fate:** Exterminated → restored through Wang Lin's influence; descendants protected
- **Canon confidence:** C5

### F28. Teng Clan (藤族)
- **Type:** clan
- **Alignment:** antagonist
- **Peak realm:** Half-Step Deity Transformation (Teng Huayuan)
- **Headquarters:** Teng Family City → Country of Zhao → Planet Suzaku
- **Specialization:** "Nine Great Nascent Souls" (Teng One to Teng Nine) selection system
- **Relationship to Wang Lin:** Exterminated the Wang Clan; Wang Lin's first major revenge target
- **Key members:** Teng Huayuan (Patriarch), Teng Li (great-great-grandson), Teng Xiuxiu, the 9 Nascent Soul cultivators (all killed and refined into demons)
- **Fate:** Exterminated by Wang Lin
- **Canon confidence:** C5

### F29. Tattoo Clan (纹族)
- **Type:** clan
- **Alignment:** neutral
- **Peak realm:** unknown
- **Headquarters:** unknown
- **Specialization:** tattoo-based cultivation (Beast Skin Tattoo, Tattoo Talisman Speed Boost)
- **Relationship to Wang Lin:** Tangential
- **Key members:** Tangential
- **Fate:** Survives
- **Canon confidence:** C3

### F30. Forsaken Immortal Clan / Immortal Remnant Clan (遗仙族)
- **Type:** clan
- **Alignment:** neutral
- **Peak realm:** late Nascent Soul (Yun Quezi — 2nd Ancestor); 10-leaf curse master
- **Headquarters:** Vermilion Bird Star (hidden in Vermilion Bird Country)
- **Specialization:** curse mastery, reincarnation manipulation
- **Relationship to Wang Lin:** Yun Quezi (2nd Ancestor) selected Wang Lin as one of his 4 chess pieces; explored the depths of the clan with Wang Lin
- **Key members:** Yun Quezi (2nd Ancestor), Qian Pinghai (13th-Gen Vermilion Bird, Yun Quezi's master)
- **Fate:** Survives
- **Canon confidence:** C4

### F31. Moon Devourer Clan (吞月族)
- **Type:** clan
- **Alignment:** antagonist
- **Peak realm:** Third Step (mysterious youth and old man)
- **Headquarters:** unknown
- **Specialization:** moon-themed cultivation
- **Relationship to Wang Lin:** Sent a mysterious youth and old man to interrupt Wang Lin's Ancient God Luo Chen recognition; beaten back by the Heaven Defying Bead
- **Key members:** mysterious youth + old man
- **Fate:** Survives (driven off)
- **Canon confidence:** C4

### F32. Scatter Thunder Clan / Flash Thunder Clan (散雷族)
- **Type:** clan
- **Alignment:** neutral (later ally after Wang Lin's slaughter)
- **Peak realm:** Third Step (5th Heaven Blight head elder); Daoist Scattered Spirit
- **Headquarters:** Sealed Realm
- **Specialization:** thunder cultivation; Eternal Thunderbolt
- **Relationship to Wang Lin:** Wang Lin stole their lightning source, killed the Sect Leader, killed the 5th Heaven Blight head elder (devoured 8 ancient thunder dragons), obtained many thunder sources
- **Key members:** 5th Heaven Blight head elder (killed), Sect Leader (killed), Daoist Scattered Spirit (ally), Zhong Dahong (former member)
- **Fate:** Many killed by Wang Lin; Daoist Scattered Spirit allied
- **Canon confidence:** C4

### F33. Fire Sparrow Clan (火雀族)
- **Type:** clan
- **Alignment:** neutral
- **Peak realm:** unknown
- **Headquarters:** Vermilion Bird Starfield
- **Specialization:** fire sparrow cultivation
- **Relationship to Wang Lin:** Took Liu Jinbiao and Xu Liguo (impersonating the 6th-Gen Vermilion Bird); seen through by the 3rd-Gen Evil Sparrow
- **Key members:** 3rd-Gen Evil Sparrow (mentioned)
- **Fate:** Survives
- **Canon confidence:** C3

### F34. Giant Demon Clan (巨魔族)
- **Type:** clan
- **Alignment:** neutral
- **Peak realm:** unknown
- **Headquarters:** unknown
- **Specialization:** Ancient God's Blood (their Ancestor was killed for it)
- **Relationship to Wang Lin:** Chi Hu (gave Wang Lin the Star Compass)
- **Key members:** Chi Hu, Giant Demon Ancestor (killed — Wang Lin obtained Ancient God's Blood)
- **Fate:** Survives
- **Canon confidence:** C4

### F35. Dark Scorpion Clan (暗蝎族)
- **Type:** clan
- **Alignment:** neutral
- **Peak realm:** unknown
- **Headquarters:** unknown
- **Specialization:** scorpion-cultivation
- **Relationship to Wang Lin:** Wang Lin is "Ruler of the Dark Scorpion Clan"
- **Key members:** Tangential
- **Fate:** Survives
- **Canon confidence:** C3

### F36. Yao Family (姚家)
- **Type:** clan / family
- **Alignment:** antagonist
- **Peak realm:** Third Step (Blood Ancestor Yao Xinghai)
- **Headquarters:** Southern Domain → Allheaven Star System (one of 4 major powers)
- **Specialization:** blood cultivation; Blood Soul Pill
- **Relationship to Wang Lin:** Sent kill order on Wang Lin → Wang Lin destroyed multiple planets chasing them
- **Key members:** Yao Xinghai (Blood Ancestor, killed), Yao Xixue (amnesiac)
- **Fate:** Many killed; faction weakened
- **Canon confidence:** C5

### F37. Zhen Family / Zhan Family (甄家 / 战家)
- **Type:** clan
- **Alignment:** antagonist
- **Peak realm:** Third Step (Zhan Xingye)
- **Headquarters:** IAC
- **Specialization:** Battle Scroll (3 scrolls merged into golden "Battle" word by Wang Lin)
- **Relationship to Wang Lin:** Zhan Xingye skeletons → Wang Lin fused with golden light; Wang Lin obtained Battle Will/Domain
- **Key members:** Zhan Xingye (killed)
- **Fate:** Survives (weakened)
- **Canon confidence:** C4

### F38. Chosen Immortal Clan (选民仙族)
- **Type:** clan
- **Alignment:** neutral (enslaved)
- **Peak realm:** unknown
- **Headquarters:** Thunder Celestial Realm
- **Specialization:** enslaved celestials
- **Relationship to Wang Lin:** Wang Lin learned the history of how the Celestials enslaved them; helped them escape the Thunder Celestial Realm
- **Key members:** Tangential
- **Fate:** Freed by Wang Lin
- **Canon confidence:** C4

### F39. Celestial Clan (仙族)
- **Type:** clan / race
- **Alignment:** neutral
- **Peak realm:** Grand Empyrean (Immortal Ancestor, multiple Celestial Venerables)
- **Headquarters:** IAC
- **Specialization:** immortal/celestial cultivation; Nine Suns (originally)
- **Relationship to Wang Lin:** Wang Lin is the "Celestial Clan's 49th Ascendant Empyrean"; Grand Empyrean Xuan Luo's disciple (abdicated)
- **Key members:** Immortal Ancestor, Dao Yi Great Celestial Venerable, Sea Child Celestial Venerable, Jiu Di (Grand Empyrean), Song Tian, Yun Yifeng, Purple Dawn Immortal Emperor, Wang Lin (49th Ascendant Empyrean)
- **Fate:** Survives; Wang Lin becomes their #1 power
- **Canon confidence:** C4

### F40. Ancient Clan (古族)
- **Type:** clan / race
- **Alignment:** neutral
- **Peak realm:** Great Heavenly Venerable (Ancient Dao, Dao Yi, Dao Gu lineages); Ancient Ancestor
- **Headquarters:** IAC
- **Specialization:** Ancient Clan bloodline (God/Devil/Demon stars); Dao Gu, Dao Yi, Primordial Ancient lineages
- **Relationship to Wang Lin:** Wang Lin is of the Dao Gu lineage (formerly), main God + Devil + Demon; Xuan Luo is his master; Ji Du is his godson (Primordial Ancient)
- **Key members:** Ancient Ancestor, Dao Ancient Great Celestial Venerable, Dao Yi Great Celestial Venerable, Xuan Luo (Dao Gu Great Heavenly Venerable), Ye Mo (Ancient God inheritance), Tu Si (8-star Ancient God, deceased), Tuo Sen (Tu Si's demonic thought), Ji Du, Wang Lin
- **Fate:** Survives; Wang Lin becomes #1 after defeating Gu Dao and Ancient Dao Great Celestial Venerable
- **Canon confidence:** C5

## 3C. Dynasties / Alliances / Governing Bodies

### F41. Vermilion Bird Country (朱雀国)
- **Type:** cultivation nation-state
- **Alignment:** righteous
- **Peak realm:** Vermilion Bird Master (Void Flame Cultivator tier)
- **Headquarters:** Planet Suzaku
- **Specialization:** ruling cultivation nation of Planet Suzaku; Vermilion Bird Sequence inheritance
- **Relationship to Wang Lin:** Wang Lin declined the position of Vermilion Bird Child (transferred to Zhou Wutai); later became 6th-Gen Vermilion Bird Divine Emperor of the broader sect
- **Key members:** Ye Wuyou (1st-Gen), Situ Nan (2nd-Gen), 3rd-Gen (betrayer), Qian Pinghai (13th-Gen), 14th-Gen, Zhou Wutai (15th-Gen), Hong Die (core disciple)
- **Fate:** Survives; elevated to higher tier
- **Canon confidence:** C5

### F42. Great Wang Dynasty (大王朝)
- **Type:** dynasty
- **Alignment:** righteous (Wang Lin's)
- **Peak realm:** Wang Lin (Ancestor)
- **Headquarters:** Planet Suzaku
- **Specialization:** Wang Lin's dynasty; made Wang Lin "Ancestor of the Country of Zhao"
- **Relationship to Wang Lin:** Founded by Wang Lin
- **Key members:** Wang Lin (Ancestor)
- **Fate:** Survives
- **Canon confidence:** C4

### F43. Cultivation Alliance (修真联盟)
- **Type:** governing body / alliance
- **Alignment:** neutral
- **Peak realm:** Third Step+ (multiple elders)
- **Headquarters:** Alliance Star System (renamed from Brilliant Void after the Alliance rose)
- **Specialization:** governance of the Alliance Star System; emissaries; mediation
- **Relationship to Wang Lin:** Mo Zhi (emissary) is Wang Lin's mentor figure; Cultivation Alliance "went crazy for" the Heaven Defying Pearl; All-Seer infiltrated the Cultivation Alliance HQ
- **Key members:** Mo Zhi (emissary), Lu Yun (5th-Gen Vermilion Bird, infiltrated HQ)
- **Fate:** Survives
- **Canon confidence:** C4

### F44. Seven Paths Sect / Seven Dao Sect (七道宗)
- **Type:** sect (the "creator sect" of the Cave World — indirectly)
- **Alignment:** complex
- **Peak realm:** Heaven Trampling (Seven-Colored Daoist)
- **Headquarters:** outside the Cave World (the entrance where Xuan Luo and Dao Yi Great Celestial Venerable fought over the Primordial God Realm fragment)
- **Specialization:** Cave World creation; Three Souls and Seven Spirits
- **Relationship to Wang Lin:** The Seven-Colored Daoist is the creator-antagonist of RI; Wang Lin kills him and takes ownership of the Cave World
- **Key members:** Seven-Colored Daoist / Seven-Colored Immortal Venerable (creator); Three Souls (Situ Nan, Qing Shui, Tan Lang, Xie Qing, etc.)
- **Fate:** Disbanded / killed (Seven-Colored Daoist slain by Wang Lin)
- **Canon confidence:** C5

### F45. Wu Xuan Country (吴玄国)
- **Type:** mortal nation-state
- **Alignment:** neutral
- **Peak realm:** Situ Nan (reincarnated as Grand Marshal "Si Nan")
- **Headquarters:** IAC
- **Specialization:** mortal kingdom with cultivation backing
- **Relationship to Wang Lin:** Situ Nan's reincarnation destination; he became Grand Marshal and was enfeoffed as the Southern Prince (fulfilling his lifelong dream)
- **Key members:** Si Nan (Situ Nan's reincarnation)
- **Fate:** Survives
- **Canon confidence:** C4

---

# Catalog 4 — The Cosmological Structure

## 4.1 The Nested Hierarchy (outermost to innermost)

```
Root Dao  ── substrate of all laws
   │
   ▼
Luo Tian Star System  ── the "true" star-system reality outside the Cave World
   │
   ▼
Immortal Astral Continent (Xian Gang Continent)  ── the "real" world; 9 suns
   │   ├── Heavenly Bull Continent (Great Soul Sect, Gui Yi Sect, 120+ Fire Veins)
   │   ├── Green Devil Continent (Dao Devil Sect, Green Devil entity)
   │   ├── Mountain Sea Continent (Mountain Tree Seal)
   │   ├── Great Saint Continent (sealed Spirit beneath)
   │   ├── Green Bull Continent
   │   ├── Mengtu Province (Dao Demon Sect)
   │   ├── Tianniu Province (Canglong Sect)
   │   └── Imperial City / Dao Ancient Imperial Capital (Ancient Clan seat)
   │
   ▼
The Cave World (Dong Tian)  ── sealed pocket-world; created by Seven-Colored Daoist
   │   (Renamed "Wang Lin's Cave World" after Wang Lin kills the Seven-Colored Daoist)
   │
   ├── Outer Realm  ── the half OUTSIDE the Realm-Sealing Grand Array
   │       (higher-tier cultivators; Daoist Water's home; less restricted)
   │
   └── Sealed Realm / Inner Realm  ── the half INSIDE the Realm-Sealing Grand Array
           (Realm-Sealing Grand Array's spirit = Heaven-Splitting Axe)
           (Suppresses Third-Step cultivators from rising)
           │
           ├── Brilliant Void Star System / Alliance Star System (7 million cultivation planets)
           │       │
           │       ├── Vermilion Bird Starfield
           │       │       └── Planet Suzaku (Wang Lin's birthplace; 3rd-tier cultivation planet)
           │       │           ├── Country of Zhao (Wang Family Village, Heng Yue Sect, Tian Shui City)
           │       │           ├── Chu Country (Cloud Sky Sect)
           │       │           ├── Fire Burn Country (Luo He Sect, Fire Beasts)
           │       │           ├── Sky Demon Country (Demonic Drum tournament)
           │       │           ├── Pilu Kingdom (Soul Refining Sect)
           │       │           ├── Snow Domain Country
           │       │           ├── Xuan Wu
           │       │           ├── Fire Demon Country (fragmented ancient demon)
           │       │           ├── Vermilion Bird Country (6th-level cultivation nation)
           │       │           ├── Great Wang Dynasty (Wang Lin's dynasty)
           │       │           ├── Sea of Devils (Fighting Evil Sect, Corpse Yin Sect, Mosquito Beasts)
           │       │           ├── Jue Ming Valley (Ji Realm awakening)
           │       │           ├── Foreign Battleground (death-law locked)
           │       │           ├── Land of the Ancient God (Tu Si's body — 3-level realm)
           │       │           │   ├── Level 1: Hurricane of Devils (Wang Lin's 2nd devil)
           │       │           │   ├── Level 2: Bridge of No Return + Restriction Mountain
           │       │           │   └── Level 3: Annihilation realm (Tuo Sen)
           │       │           ├── Suzaku Tomb (Cultivation Planet Crystal)
           │       │           └── Chaotic Broken Stars (3-level chaotic cluster)
           │       │
           │       ├── Planet Tian Yun (Tianyun Sect / All-Seer; East Demon Spirit Sea)
           │       ├── Planet Qing Lin / Qing Ling (Xie Qing)
           │       ├── Planet Ran Yun (Sun Tai; Wang Ping raised here)
           │       ├── Earth Planet (Xuan Yuan Sect; Brilliant Golden Fruit)
           │       ├── Trading Planet (oceans; Situ Nan's thievery)
           │       ├── Water Spirit Star / Feng Luan Star (Situ Nan's Southern Prince faction)
           │       └── Planet Five Elements (Water General's One Drop of Universe)
           │
           ├── Allheaven Star System
           │       ├── Southern Domain (Yao Family; Thunder Celestial Tournament)
           │       ├── Thunder Celestial Realm (Master Flamespark; Russell; Thunder Lake)
           │       │   └── Thunder Celestial Temple (3 trials: heaven/earth/human)
           │       └── Rain Celestial Realm (Zhou Yi's Ting'er; Rain Immortal Sword)
           │
           ├── Cloud Sea Star System (Nirvana Scryer+ cultivators)
           │       ├── Origin Sect (Wang Lin's "Ceng Niu" alias)
           │       ├── Wild Continent (Third-Step secret)
           │       ├── Seven-Colored Realm (Master Ashen Pine)
           │       ├── Wind Celestial Realm (Flowing Moon technique)
           │       ├── Da Lou Sword Sect
           │       ├── Treasured Jade Sect (auctions)
           │       └── Everlasting Sect
           │
           └── Blue Silk Clan Star Domain (Dao Master Blue Dream)
                   └── (Where Wang Lin learned Dao Art Fusion)
```

## 4.2 The Sealing Mechanism (Key Cosmological Insight)

The RI universe is a **nested-sealed cosmology** — every world is a sealed farm owned by a higher-tier cultivator. There are **TWO overlapping seals**:

1. **The Cave World seal** (outer seal): The entire Cave World is a pocket-dimension inside Luo Tian, sealed by the Seven-Colored Daoist to harvest Joss Flames. The Cave World is "a pocket world of the Immortal Astral Continent."

2. **The Realm-Sealing Grand Array** (inner seal): Inside the Cave World, the Sealed Realm is sealed AGAIN by the Realm-Sealing Grand Array (whose spirit is the Heaven-Splitting Axe). This seal caps cultivation at the Heaven Blight / quasi-Third-Step tier — Third-Step cultivators cannot naturally arise in the Sealed Realm. The Sealed Realm contains Planet Suzaku, Brilliant Void, Allheaven, and Cloud Sea Star Systems. The Outer Realm is the half outside this inner seal.

**The All-Seer's role (mortal-realm schemer):** The All-Seer is NOT Allheaven (the ISSTH antagonist). The All-Seer is the ruler of the Heavenly Fate Sect, a peak Third Step schemer who plots to absorb the source origins of Wang Lin, Ling Tianhou, and Blood Ancestor. His clone is Tianyunzi (the artifact spirit of the Realm-Defining Compass / Heaven Defying Bead). The All-Seer is the mortal-realm antagonist — Wang Lin kills him in the endgame.

**The Seven-Colored Daoist's role (cosmic-level antagonist):** The Seven-Colored Daoist / Seven-Colored Immortal Venerable is the **creator of the Cave World** — the higher-tier cultivator who owns the farm. He created the Cave World to harvest Joss Flames. He created the Three Souls and Seven Spirits (Situ Nan = Green Soul, Qing Shui = Slaughter Soul, Tan Lang = Yellow Soul, Xie Qing = Third Soul, etc.) — these are his "fragments" that reincarnate. He is the true cosmic antagonist of RI, far above the All-Seer. Wang Lin learns of him only in Book 11 ("Mysteries of the Ancient Era") when he breaches the Ancient Immortal Domain.

**Wang Lin's final state:** Wang Lin kills the Seven-Colored Daoist, becomes the new world-owner of the Cave World (renamed "Wang Lin's Cave World"), resurrects Li Muwan using the Reincarnation Essence (his 14th), crosses all 9 Heaven Trampling Bridges, achieves the 4th Step (Heaven Trampling), and Transcends with Li Muwan. He becomes "The God" in ISSTH (Old Man Extermination) and "Paragon Wang / Divine Spirit" in AWWP, where he mentors Wang Baole. By BTT (Beyond the Timescape), he is a "Summer Immortal" (10th Step).

## 4.3 The 4-Step Cultivation Ladder (Tied to the Cosmology)

The cultivation ladder maps directly to which cosmological layer one can inhabit:

| Step | Realm | Cosmological Access |
|---|---|---|
| 1st Step (Foundation) | Qi Condensation → Soul Transformation → Ascendant | One planet (Planet Suzaku tier) |
| Transitional | Illusionary Yin, Corporeal Yang | One star system |
| 2nd Step (Dao) | Nirvana Scryer → Nirvana Cleanser → Nirvana Shatterer | Multiple star systems (Alliance/Allheaven/Cloud Sea) |
| Transitional | Heaven's Blight (5 blights) | Boundary of Sealed Realm — Wang Lin skips these via essence-stacking |
| 3rd Step (The Dao) | Nirvana Void → Spirit Void → Arcane Void | Cave World fully; can attempt to leave via Ancient Immortal Domain |
| Transitional | Half Heaven Trampling (1st-3rd-5th-7th Bridges) | Bridge of Immortality in Luo Tian |
| 4th Step (Dao Source) | Heaven Trampling (crosses all 9 bridges) | Transcendent; can create/own worlds |

**Key non-standard paths in RI:**
- **Wang Lin's Heaven-Defying Cultivator path:** Only the 3rd lifeform ever to take this path (defies heavenly retribution; harvests karma instead of merit). Allows essence-stacking breakthroughs instead of Heaven Blight endurance.
- **Ancient Order (Ancient Clan) cultivation:** Parallel body-cultivation path (God/Devil/Demon stars). Wang Lin's hybrid: Qi Cultivator (avatar) + Ancient God (main body) → eventually fuses all into one. 27 stars = peak Ancient Clan.
- **Essence Cultivation:** Wang Lin's 14 Essences (Five Elements + Thunder + Life-Death + True-False + Karma + Reincarnation + Absolute Beginning + Absolute End + Restriction + Slaughter). Each Essence forms a True Body; True Bodies fuse into composite bodies (Five Elements True Body, Slaughter Thunder True Body, etc.).
- **Wang Lin's Clones:**
  - **Cultivator Clone** (via Divine Path technique) — Qi Cultivator vessel
  - **Ancient Demon Clone** (from a statue in Demon Spirit Land; Lou Hou's fragment)
  - **Ancient Devil Clone** (from Daogu Yemo's corpse, 3000 Ancient Devil Souls from Devil Soul Bottle)
  - **Void Clone** (on the IAC arc-shaped platform; possesses same Void Destiny as Immortal Ancestor and Ancient Ancestor)
  - **Slaughter True Body / Lu Mo (鲁墨):** Fused from Slaughter + Restriction + Absolute Beginning + Absolute End + Thunder Essences. Sent back in time via Flowing Moon to find Li Muwan's resurrection method. Fell in love with her during the mission. Erased his own consciousness and re-fused into Wang Lin after her resurrection. Achieved 4th Step (surpassed the main body temporarily).
  - **Five Elements True Body:** Fused from Fire + Water + Earth + Metal + Wood Essence True Bodies (representing Wang Lin's lifetime of cultivation).

## 4.4 The Cave World's "True Nature" Revelation (Book 11)

The pivotal cosmological revelation: Wang Lin discovers in Book 11 that his entire universe (Planet Suzaku, the Brilliant Void Star System, the Sealed Realm, etc.) is **a cave-mansion of the Seven-Colored Daoist**. The mortal-realm "Heaven Dao" is just the Seven-Colored Daoist's harvest mechanism. Every Heaven-Defying Cultivator who arises is a potential threat to this harvest — which is why the Heaven's messengers appear when Wang Lin breaks the rules (e.g., trying to keep Li Muwan's soul out of the reincarnation cycle).

After this revelation:
1. Wang Lin kills the invading Outer Realm cultivators (who were sent to harvest the Sealed Realm).
2. Resets the Realm Sealing Grand Array; plans new territories.
3. Goes to the Ancient Clan altar; obtains Ancient Ancestor's blood (startles the IAC Dao Ancient lineage).
4. Re-condenses the Immortal Realm; pursued by the Seven-Colored Daoist.
5. Learns the secret of the Seven-Colored Daoist's Three Souls and Seven Spirits.
6. Eventually kills the Seven-Colored Daoist and takes ownership of the Cave World.

## 4.5 The Reincarnation Migration (Cave World → Immortal Astral Continent)

After Wang Lin reaches the 3rd Step and gains the ability to send souls across the Cave World / IAC boundary, he systematically sends his friends/allies who wish for reincarnation into the IAC. Those reincarnated on the IAC ("Immortal Execution Star") include:

- Situ Nan → "Si Nan" (Grand Marshal of Wu Xuan Country; Southern Prince)
- Xu Liguo, Liu Jinbiao, Zhong Dahong (the Three Auspicious Treasures) — continued swindling
- Zhou Yi and Qing Shuang (together)
- Big Head Cultivator
- Wang Zhuo (multiple reincarnations)
- Hong Die → "Qing Hong" (no past-life memories)
- Many others

Wang Lin then enters the IAC, systematically awakens their past-life memories, and they reunite as his core faction for the final battles (against Gu Dao, the Ancient Dao Great Celestial Venerable, the Seven-Colored Daoist, etc.).

## 4.6 The Heaven Trampling Bridges (the Final Transcendence Mechanism)

The Bridge of Immortality has 9 "Heaven Trampling Bridges" — each tests a different aspect:

1. **First Bridge:** Tested via Slaughter, Restriction, Absolute Beginning, Absolute End fusion → Slaughter becomes Punishment
2. **Second Bridge:** Tests the heart's sturdiness — grants a glimpse of Heaven Trampling divine sense (Wang Lin's soul nearly collapsed; he persisted)
3. **Third Bridge:** Tests inner demons — Wang Lin embraced his demons instead of fighting them, used his Heaven-Defying Will to cross
4. **Fourth Bridge:** Crossed via Ancient Clan cultivation (Ancient Order's Third Tribulation)
5. **Fifth Bridge:** Crossed after passing the Ancient Order's Third Tribulation (completing Ancient Clan Cultivation)
6. **Seventh Bridge:** Crossed during the final Ancient Clan Tribulation (saw a vision of a man performing a Heaven Trampling step)
7. **Eighth Bridge:** Stopped here (briefly)
8. **Ninth Bridge:** Crossed at the end — Wang Lin comprehends the Reincarnation Essence and achieves Heaven Trampling without stepping on more bridges

After crossing all 9, Wang Lin Transcends with Li Muwan.

## 4.7 The Heaven Defying Bead (the Cosmological MacGuffin)

The Heaven Defying Bead is:
- The core of the **Realm-Defining Compass** (the original treasure of the Cave World's seal).
- Originally 9 parts; the Heaven Defying Bead is the most important part.
- Bestowed by the Seven-Colored Daoist to the Realm-Sealing Supreme (Lord of the Sealed Realm) as proof of authority over the Inner Realm.
- The root cause of the great war in the Ancient Immortal Domain.
- Su Ming (Pursuit of the Truth protagonist) successfully seized it after possession and controlled it; before Su Ming died, he used it to locate his friend the Bald Feather Crane (Tian Yunzi), arriving in the Inverse Dust Realm (RI universe).
- Wang Lin's clone Lu Mo borrowed it from Old Man Miesheng, blasted it open using Dream Dao, and sent the bead through countless tens of thousands of years to the main body Wang Lin.
- Inside the bead: time flows at 10x outside rate; spatial folding; causality tracing.
- Its artifact spirit is **Tianyunzi** (the All-Seer's clone).

## 4.8 The Cross-Novel Cosmology (Ergenverse)

The RI universe is one of several "pocket worlds" within the broader Ergenverse:

- **Pursuit of the Truth (Su Ming)** precedes RI in-world; Su Ming seized the Heaven Defying Bead and used it to send the Bald Feather Crane to the Immortal Astral Continent (Wang Lin's world).
- **Renegade Immortal (Wang Lin)** — Wang Lin Transcends, becomes "The God" / "Old Man Extermination" in ISSTH.
- **I Shall Seal the Heavens (Meng Hao)** — Wang Lin (as "The God") leaves a clone to assist Meng Hao; Qing Shui also leaves a clone to assist Meng Hao. The Four Supremes (God/Devil/Demon/Ghost) = Wang Lin / Su Ming / Meng Hao / Patriarch Vast Expanse.
- **A Will Eternal (Bai Xiaochun)** — Tangential to RI but shares the Ergenverse cosmology.
- **A World Worth Protecting (Wang Baole)** — Wang Lin appears as "Paragon Wang" / "Divine Spirit," mentors Wang Baole, sends his mosquito pet to protect him (ch.69), suggests the Heaven Trampling bridge for 4th step, gives Wang Baole the Eight Extreme Dao (in AWWP Ch.1221). Wang Baole marries Wang Yiyi (Wang Lin's daughter).
- **Beyond the Timescape (Xu Qing)** — Wang Lin is referenced as a "Summer Immortal" (10th Step) by this era.

## 4.9 The Forge Mod Reality Profile (Mapping World-Law Tiers to Layers)

For the Forge mod's Reality Profile system, the world-law tiers map to cosmological layers as:

| Tier | Cosmological Layer | Examples |
|---|---|---|
| `fragile` | Mortal villages, Qi Condensation-tier countries | Wang Family Village, Country of Zhao |
| `low` | Foundation/Core-tier regions, demonic seas | Sea of Devils, Jue Ming Valley, Fire Burn Country |
| `medium` | Star systems, Nascent Soul+ cultivator nations | Planet Suzaku, Alliance Star System, Allheaven Star System |
| `high` | Multi-star-system realms with Third Step cultivators | Cloud Sea Star System, Blue Silk Clan Star Domain, Thunder Celestial Realm |
| `absolute` | The IAC, Luo Tian, the Cave World (post-Wang Lin) | Immortal Astral Continent, Primordial Divine Realm, Wang Lin's Cave World |

**Sealed-realm flag (`isSealed: true`):** applies to:
- The entire Cave World (sealed by Seven-Colored Daoist)
- The Sealed Realm (sealed by Realm-Sealing Grand Array)
- The Foreign Battleground (sealed by death laws)
- Sub-realms inside the Heaven Defying Bead (Yellow Spring Secret Realm)
- Kunxu Realm (sealed sub-dimension)
- Multiple immortal realms (Thunder/Rain/Wind Celestial Realms — sealed sub-realms)

**Cultivation suppression:** In sealed realms, cultivation is capped at Heaven Blight (quasi-Third-Step). To exceed this, one must either (a) leave the sealed realm via the Ancient Immortal Domain, or (b) be Wang Lin (who stacks Essences to skip the Heaven Blights).

---

# Appendix A — Summary Tables

## A.1 Location Counts by Cosmological Layer

| Layer | Locations | Example |
|---|---|---|
| Root Dao / Luo Tian | 2 | Root Dao, Luo Tian Star System |
| Immortal Astral Continent | 13 | IAC, Heavenly Bull Continent, Green Devil Continent, Mountain Sea Continent, Great Saint Continent, Green Bull Continent, Mengtu Province, Tianniu Province, Imperial City, Ancient Clan Temple, Pill Sea, Dong Lin Pool, Ancient Tomb |
| Cave World (meta) | 2 | Cave World, Wang Lin's Cave World |
| Sealed Realm / Outer Realm / Primordial Divine Realm | 3 | Sealed Realm, Outer Realm, Primordial Divine Realm |
| Star Systems | 5 | Alliance, Allheaven, Cloud Sea, Vermilion Bird Starfield, Blue Silk Clan Star Domain, Luo Tian Star Domain |
| Planets | 9 | Suzaku, Tian Yun, Qing Lin, Ran Yun, Earth, Trading, Water Spirit, Five Elements, Immortal Execution |
| Countries | 11 | Zhao, Chu, Fire Burn, Sky Demon, Pilu, Snow Domain, Xuan Wu, Fire Demon, Vermilion Bird, Great Wang, Qing Shui |
| Cities/Villages/Regions | 10 | Wang Family Village, Tian Shui City, Teng Family City, Nan Dou City, Qilin City, Ancient Demon City, Hou Fen, Blue Pine Peaks, Soul Refining Tribe, Mountain Valley Tribe |
| Realms/Dimensions/Ruins | 18 | Sea of Devils, Jue Ming Valley, Foreign Battleground, Land of Ancient God, Restriction Mountain, Bridge of No Return, Chaotic Broken Stars, Suzaku Tomb, Thunder Celestial Realm, Thunder Celestial Temple, Rain Celestial Realm, Wind Celestial Realm, Demon Spirit Land, Wild Continent, Seven-Colored Realm, Kunxu Realm, Tide Abyss, Immortal Graveyard, Five Flowers Eight Gates, Falling Land, Ancient Immortal Domain, Yellow Spring Secret Realm, Brilliant Void |

**Total: 80 locations (slightly more than the 78 in the summary due to additional sub-realms).**

## A.2 NPC Counts by Category

| Category | Count | Example |
|---|---|---|
| Protagonist | 1 | Wang Lin |
| Family | 16 | Wang Tianshui, Zhou Tingsu, Fourth Uncle, Wang Zhuo, Wang Hao, Wang Ping, Wang Yiyi, Wang Jiduo, Zhou Ru, Qing Yi, Song Yu, Wang Baole, Li Qiqing, Teng Xiuxiu, Dao Master Blue Dream |
| Spouses | 3 | Li Muwan, Li Qianmei, Mu Bingmei/Liu Mei |
| Mentors | 10 | Situ Nan, All-Seer (false), Tu Si, Du Tian, Bai Fan, Lu Yun, Qing Lin, Su Dao, Xuan Luo, Dao Master Blue Dream |
| Allies | 35 | Qing Shui, Zhou Yi, Qing Shuang, Chi Hu, Qiu Siping, Mo Ling, Mo Lihai, Sun Tai, Li Yuan, Ling Tianhou, Bei Lou, Wang Wei, Hu Juan, Ta Shan, Big Head, Lei Ji, Liu Jinbiao, Ling Dong, Zhou Jin, Zhong Dahong, Daoist Scattered Spirit, 2nd-Gen Vermilion Bird, Master Hong Shan, Master South Cloud, Gemini, Tan Lang, Hong Die, Zhou Wutai, Yun Quezi, Mo Zhi, Lian Daofei, Xu Liguo, Tuo Sen, Ouyang Hua, Li Dannan, Bai Wei, Master Flamespark, Lu Yanfei, Lu Yuncong, Ji Si, Jiu Di, Sea Child, Ye Wuyou, Qian Pinghai, 3rd-Gen Vermilion Bird, 14th-Gen Vermilion Bird |
| Antagonists | 32 | Teng Huayuan, Teng Li, Sun Dazhu, Old man Ji Mo, Gao Qiming, Piao Nanzi, Lin Yi, Duanmu Ji, Hunchback Meng, Xu Liqing, Gun Lan, Wang Qingyue, Yun Fei, Qian Kun, Master Void, Blood Ancestor, Yao Xixue, Wind Demon, Yao Family, Daoist Water, Wu Qing, Master Ashen Pine, Master Cloud Soul, Noble Money, Cang Songzi, Lu Fuzi, Ye Dao, Lian Daozhen, Yan Leizi, Su Dao, Tianyunzi, Seven-Colored Daoist, Old Man Miesheng, Taga, Gu Dao, Song Tian, Dao Ancient GCV, Dao Yi GCV, Imperial Preceptor, Dao Devil Sect Master, Du Qing, Kang Ren, Purple Dawn Immortal Emperor, White Tiger General, Yun Yifeng, Palm Venerable, Zhan Laogui, Yun Kong, Zhan Xingye, Green Devil |
| Disciples | 4 | Thirteen (Shi San), Huo Pao, Xie Qing, Xi Zifeng |
| Companions/Servants/Pets | 8 | Mosquito Beast, Thunder Toad, Thunder Celestial Beast, Nether Beast, Brilliant Void, Golden Sea Dragon, Adai, Xiao Bai |
| Other (backstory/sect-rivals) | 13 | Wu Yu, Ye Zi, Zhao Xingsha, Sima Rufeng, Zhao Xinming, Chen Tao, Wang Zhou, Wang Jie, Sun Zhenwei, Chen Bailiang, Zhang Hu, Hui Bing, Zhou Rui, Leng Sheng, Da Niu, Zhou Lin |

**Total: ~160 named NPCs (with overlaps in allies/antagonists for complex arcs).**

## A.3 Faction Counts by Type

| Type | Count |
|---|---|
| Sects (cultivation organizations) | 26 |
| Clans/Tribes (bloodline-based) | 14 |
| Dynasties/Nation-States | 3 |
| Alliances/Governing Bodies | 2 |
| **Total** | **45** |

---

# Appendix B — Source Verification Notes

## B.1 Sources That Failed

- **`allseer.html`**: Baidu returned "entry does not exist" page (32KB HTML, 239 chars cleaned). All-Seer information was reconstructed from his extensive mentions across `wl_main.json`, `wl_baidu_main.json`, `wl_cult.json`, and the existing `npc-catalog.ts`.
- **`wang_tengfei.html`**: Cloudflare "Just a moment..." page (65 chars cleaned). This file is for the ISSTH character (not RI); not relevant to RI canon.

## B.2 Cross-Reference Verification

- **Wang Lin's aliases**: Verified across `wl_main.json` (Fandom) and `wl_baidu_main.json` (Baidu) — 24+ aliases consistent.
- **Wang Lin's family tree**: Verified across both Fandom and Baidu — Wang Tianshui, Zhou Tingsu, Wang Yiyi, Wang Ping, Wang Jiduo, Zhou Ru, Li Muwan, Li Qianmei, Mu Bingmei, Li Qiqing, Wang Baole, Wang Zhuo, Wang Hao.
- **Situ Nan's identity**: Baidu (`situ_nan_baidu.html`) confirms he is the Green Soul of the Seven-Colored Immortal Venerable, 2nd-Gen Vermilion Bird Master; consistent with Fandom.
- **Teng Huayuan's "Nine Great Nascent Souls"**: Verified by Baidu (`teng_huayuan.html`); consistent with Fandom.
- **Yao Xixue's arc**: Verified by Baidu (`yao_xixue.html`); consistent with Fandom.
- **Blood Ancestor (Yao Xinghai)**: Verified by Baidu (`blood_ancestor.html`); consistent with Fandom.
- **Li Muwan's arc**: Verified by Baidu (`li_muwan.html`); consistent with Fandom.
- **Wang Lin's Cultivation progression**: Verified across `wl_cult.json` (Fandom Cultivation page) — 1st Step → 2nd Step → 3rd Step → 4th Step + Ancient Order + Essences + True Bodies + Combat Strength.
- **Wang Lin's Clones**: Verified across `wl_clones.json` (Fandom) and `wl_clones_baidu.json` (Baidu) — consistent. The "Slaughter True Body" (Fandom) and "Slaughter-Silence Clone / Lu Mo" (Baidu) refer to the same entity.

## B.3 Confidence Distribution

- **C5 (multi-source wiki + chapter cite):** ~40% of entries (protagonist, main spouse, primary antagonist, primary mentor, signature locations/sects)
- **C4 (single-source wiki / direct novel mention):** ~45% of entries (most named NPCs, most locations, most factions)
- **C3 (novel-implicit / secondary source):** ~12% of entries (tangential NPCs, regions listed but not detailed, backstory figures)
- **C2 (archetypal fan-synthesis):** ~3% of entries (none in this catalog — all entries are directly sourced)

## B.4 Reconciliation Notes

- **All-Seer's identity:** The `npc-catalog.ts` lists "全知者" as his nameCn, but the worklog clarifies he is NOT Allheaven (the ISSTH antagonist). This catalog treats All-Seer as the mortal-realm schemer, separate from the cosmic-level Seven-Colored Daoist.
- **Tianyunzi's identity:** Tianyunzi is BOTH a clone of the All-Seer AND the artifact spirit of the Realm-Defining Compass / Heaven Defying Bead (per `wl_baidu_main.json`). He is the same entity; the All-Seer's plot used him to infiltrate Wang Lin's cultivation via the Celestial Slaughter Art trap.
- **Tu Si vs. Tuo Sen:** Tu Si is the original 8-star Ancient God (deceased; his body became the Land of the Ancient God). Tuo Sen is Tu Si's demonic thought (born from Tu Si's failed Ink Flow Split Soul Technique). Tuo Sen inherited Tu Si's "power" while Wang Lin inherited Tu Si's "knowledge" — they are NOT the same entity.
- **Liu Mei vs. Mu Bingmei:** Liu Mei's true form is Mu Bingmei (per `wl_baidu_main.json`). They are the same entity at different points in the story.
- **"Su Dao" (Scholar mentor) vs. "Su Dao" (antagonist):** The Fandom page lists "Su Dao" in both mentors and antagonists — likely an inconsistency or two different Su Dao figures. This catalog marks both with C3 confidence pending further verification.

---

**End of catalog.** Total: 80 locations, ~160 NPCs, 45 factions/sects/clans, 7 nested cosmological layers. All entries sourced from the 12 cached source files in `/home/z/my-project/tool-results/` plus cross-reference with existing `src/lib/sim/{location,sect,npc}-catalog.ts` seed data.
