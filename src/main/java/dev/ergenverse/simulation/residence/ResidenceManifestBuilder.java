package dev.ergenverse.simulation.residence;

import java.util.ArrayList;
import java.util.List;

/**
 * ResidenceManifestBuilder — derives a {@link ResidenceManifest} from a
 * {@link ResidentProfile}.
 *
 * <p>This is the user's 2026-07-26 authorship directive, operationalized:
 *
 * <blockquote>
 * You don't author buildings. You author people. […] Everything exists because
 * someone needed it. […] The house is literally generated from the resident.
 * </blockquote>
 *
 * <p>The builder is a pure function: profile in, manifest out. It reads the
 * resident's needs, personality, occupation, habits, fears, and history, and
 * produces the rooms + objects + memories that constitute their home. The
 * logic is <b>need-driven</b>: each {@link NeedCategory} maps to one or more
 * rooms with specific objects. Personality and habits modulate the objects
 * (e.g., a cautious resident's bedroom has a weapon near the bed; a
 * concealment-first resident has a hidden stash).
 *
 * <h2>The mapping — needs to rooms</h2>
 *
 * <pre>
 *   BASIC_SHELTER    → ENTRY + BEDROOM
 *   KITCHEN          → KITCHEN
 *   STORAGE          → STORAGE (+ COURTYARD if scale demands)
 *   CULTIVATION_SPACE→ CULTIVATION_CHAMBER (or MEDITATION_ROOM if sparse personality)
 *   ALCHEMY          → ALCHEMY_ROOM
 *   HERB_GARDEN      → HERB_GARDEN (in courtyard)
 *   COMBAT_TRAINING  → TRAINING_YARD
 *   LIBRARY          → LIBRARY
 *   DEFENSE          → reinforced ENTRY (objects, not a separate room)
 *   OBSERVATION      → OBSERVATION_POST (rooftop)
 *   CONCEALMENT      → HIDDEN_STASH
 *   ANIMAL_HOUSING   → ANIMAL_PEN (in courtyard)
 *   SOCIAL_SPACE     → RECEPTION
 *   MOURNING         → MEMORIAL_NOOK
 *   WORKSHOP         → WORKSHOP
 * </pre>
 *
 * <p>Personality modulates the objects. For example:
 * <ul>
 *   <li>"cautious" → weapon near the bed, escape route noted in memories</li>
 *   <li>"concealment_first" → hidden stash room, false-bottom chest in storage</li>
 *   <li>"observant" → observation post on roof, sightlines noted</li>
 *   <li>"sparse" / "minimalist" → fewer decorative objects, meditation room over cultivation chamber</li>
 *   <li>"grieving" → memorial nook with portrait and incense</li>
 * </ul>
 *
 * <p>Occupation modulates the workshop and storage contents. A farmer has hoe,
 * sickle, grain sacks. A hunter has bow, arrows, drying rack. An alchemist has
 * furnace, herb shelves, mortar. A hidden cultivator has a flying sword
 * (concealed), jade slips (hidden), a meditation mat.
 *
 * <h2>Why this is NOT a schematic</h2>
 *
 * <p>The manifest has no coordinates, no block types, no dimensions. It is pure
 * semantics. A future BlockPlacementEngine will read the manifest and decide:
 * "a KITCHEN needs a furnace block, a water cauldron, a chest for grain, a
 * crafting table for food prep." But THAT decision is separate from authorship.
 * The authorship is: "Old Chen needs a kitchen because he cooks, and his
 * kitchen contains a mortar because he grinds herbs for tea." The block
 * placement is a rendering concern; the manifest is an authorship concern.
 *
 * <p>USER-DIRECTED. The 2026-07-26 review explicitly named this layer and the
 * derivation direction (life → residence, not residence → life).
 */
public final class ResidenceManifestBuilder {

    private ResidenceManifestBuilder() {}

    /**
     * Build a residence manifest from a resident profile.
     *
     * @param profile the resident whose home is being authored
     * @return the manifest describing what the home contains and why
     */
    public static ResidenceManifest build(ResidentProfile profile) {
        List<RoomSpec> rooms = new ArrayList<>();
        List<String> reasoning = new ArrayList<>();

        // ── BASIC_SHELTER: everyone gets an entry and a bedroom ──
        if (profile.hasNeed(NeedCategory.BASIC_SHELTER)) {
            rooms.add(buildBedroom(profile));
            rooms.add(buildEntry(profile));
            reasoning.add("BASIC_SHELTER → ENTRY + BEDROOM");
        }

        // ── KITCHEN: everyone who cooks ──
        if (profile.hasNeed(NeedCategory.KITCHEN)) {
            rooms.add(buildKitchen(profile));
            reasoning.add("KITCHEN → KITCHEN (hearth, water, food prep)");
        }

        // ── STORAGE: scale depends on occupation ──
        if (profile.hasNeed(NeedCategory.STORAGE)) {
            rooms.add(buildStorage(profile));
            reasoning.add("STORAGE → STORAGE (scale from occupation)");
        }

        // ── CULTIVATION_SPACE: cultivators only; sparse personality → meditation room ──
        if (profile.hasNeed(NeedCategory.CULTIVATION_SPACE)) {
            if (profile.hasTrait("sparse") || profile.hasTrait("minimalist")) {
                rooms.add(buildMeditationRoom(profile));
                reasoning.add("CULTIVATION_SPACE → MEDITATION_ROOM (sparse personality)");
            } else {
                rooms.add(buildCultivationChamber(profile));
                reasoning.add("CULTIVATION_SPACE → CULTIVATION_CHAMBER");
            }
        }

        // ── ALCHEMY: pill refiners only ──
        if (profile.hasNeed(NeedCategory.ALCHEMY)) {
            rooms.add(buildAlchemyRoom(profile));
            reasoning.add("ALCHEMY → ALCHEMY_ROOM (furnace, herb shelves)");
        }

        // ── HERB_GARDEN: herbalists and some farmers ──
        if (profile.hasNeed(NeedCategory.HERB_GARDEN)) {
            rooms.add(buildHerbGarden(profile));
            reasoning.add("HERB_GARDEN → HERB_GARDEN (in courtyard)");
        }

        // ── COMBAT_TRAINING: martial cultivators ──
        if (profile.hasNeed(NeedCategory.COMBAT_TRAINING)) {
            rooms.add(buildTrainingYard(profile));
            reasoning.add("COMBAT_TRAINING → TRAINING_YARD");
        }

        // ── LIBRARY: scholars and knowledge-seekers ──
        if (profile.hasNeed(NeedCategory.LIBRARY)) {
            rooms.add(buildLibrary(profile));
            reasoning.add("LIBRARY → LIBRARY (jade slips, manuals)");
        }

        // ── OBSERVATION: the cautious and the concealment-first ──
        if (profile.hasNeed(NeedCategory.OBSERVATION)) {
            rooms.add(buildObservationPost(profile));
            reasoning.add("OBSERVATION → OBSERVATION_POST (rooftop, sightlines)");
        }

        // ── CONCEALMENT: hidden cultivators ──
        if (profile.hasNeed(NeedCategory.CONCEALMENT)) {
            rooms.add(buildHiddenStash(profile));
            reasoning.add("CONCEALMENT → HIDDEN_STASH (false floor, warded)");
        }

        // ── ANIMAL_HOUSING: farmers, elders with dogs ──
        if (profile.hasNeed(NeedCategory.ANIMAL_HOUSING)) {
            rooms.add(buildAnimalPen(profile));
            reasoning.add("ANIMAL_HOUSING → ANIMAL_PEN (in courtyard)");
        }

        // ── SOCIAL_SPACE: those with social ties ──
        if (profile.hasNeed(NeedCategory.SOCIAL_SPACE)) {
            rooms.add(buildReception(profile));
            reasoning.add("SOCIAL_SPACE → RECEPTION (tea table, seating)");
        }

        // ── MOURNING: those who have lost ──
        if (profile.hasNeed(NeedCategory.MOURNING)) {
            rooms.add(buildMemorialNook(profile));
            reasoning.add("MOURNING → MEMORIAL_NOOK (shrine, portrait)");
        }

        // ── WORKSHOP: occupation-specific ──
        if (profile.hasNeed(NeedCategory.WORKSHOP)) {
            rooms.add(buildWorkshop(profile));
            reasoning.add("WORKSHOP → WORKSHOP (occupation-specific tools)");
        }

        // ── COURTYARD: if the residence has outdoor needs (garden, animals, training) ──
        if (profile.hasNeed(NeedCategory.HERB_GARDEN)
                || profile.hasNeed(NeedCategory.ANIMAL_HOUSING)
                || profile.hasNeed(NeedCategory.COMBAT_TRAINING)) {
            rooms.add(buildCourtyard(profile));
            reasoning.add("COURTYARD (outdoor needs present)");
        }

        // ── DEFENSE: modulates the entry, doesn't add a room ──
        if (profile.hasNeed(NeedCategory.DEFENSE)) {
            rooms.add(buildDefensiveFeatures(profile));
            reasoning.add("DEFENSE → reinforced entry + escape route (objects, not a room)");
        }

        String reasoningText = "Manifest derived from " + profile.displayName()
                + " (" + profile.occupation() + ", " + profile.cultivationStyle() + "). "
                + String.join("; ", reasoning)
                + ". Total: " + rooms.size() + " rooms.";

        return new ResidenceManifest(
                profile.residentId(),
                profile.settlementId(),
                profile.displayName() + "'s Home",
                rooms,
                profile.history(),
                reasoningText
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Room builders — each maps a need to a room with objects + memories
    // ═══════════════════════════════════════════════════════════════════

    private static RoomSpec buildEntry(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Wooden Door", "entry and barrier", "inferred", ""));
        if (p.hasTrait("cautious")) {
            objects.add(new ObjectSpec("Door Bar", "reinforces door at night", "inferred",
                    p.hasTrait("cautious") ? "Wang Lin checks this before sleep" : ""));
        }
        return new RoomSpec(RoomPurpose.ENTRY, "Entry", "Basic shelter — a door and threshold",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildBedroom(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Wooden Bed", "sleep", "inferred", ""));
        objects.add(new ObjectSpec("Clothing Chest", "personal items", "inferred", ""));
        if (p.hasTrait("cautious") || p.hasNeed(NeedCategory.DEFENSE)) {
            objects.add(new ObjectSpec("Weapon Near Bed", "self-defense within arm's reach",
                    "inferred", "a hidden cultivator sleeps within reach of a weapon"));
        }
        List<ResidenceMemory> memories = new ArrayList<>(p.history().stream()
                .filter(m -> m.location().contains("bedroom") || m.location().contains("bed"))
                .toList());
        return new RoomSpec(RoomPurpose.BEDROOM, p.displayName() + "'s Bedroom",
                "Basic shelter — a place to sleep",
                objects, memories, evidence(p));
    }

    private static RoomSpec buildKitchen(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Hearth", "cooking and warmth", "inferred", ""));
        objects.add(new ObjectSpec("Water Jug", "water for cooking", "inferred", ""));
        objects.add(new ObjectSpec("Food Storage Jar", "stores grain and dried food", "inferred", ""));
        objects.add(new ObjectSpec("Cutting Board", "food preparation", "inferred", ""));
        if (p.hasTrait("tea_drinker") || p.inventory().contains("tea_set")) {
            objects.add(new ObjectSpec("Tea Set", "brewing tea", "inferred",
                    "Old Chen brews tea every morning"));
        }
        return new RoomSpec(RoomPurpose.KITCHEN, "Kitchen",
                "Cooking — preparing meals daily",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildStorage(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Storage Chest", "valuables and supplies", "inferred", ""));
        if (p.occupation().contains("farmer")) {
            objects.add(new ObjectSpec("Grain Sacks", "harvest storage", "inferred", ""));
            objects.add(new ObjectSpec("Tool Rack", "farming tools", "inferred", ""));
        }
        if (p.occupation().contains("merchant")) {
            objects.add(new ObjectSpec("Ledger", "tracking trade", "inferred", ""));
            objects.add(new ObjectSpec("Coin Box", "currency storage", "inferred", ""));
        }
        return new RoomSpec(RoomPurpose.STORAGE, "Storage",
                "Storing tools, grain, and valuables for " + p.occupation(),
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildCultivationChamber(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Meditation Mat", "cultivation practice", "inferred", ""));
        objects.add(new ObjectSpec("Qi Gathering Array", "concentrates ambient qi", "inferred",
                "a simple formation etched into the floor"));
        if (p.cultivationStyle().contains("foundation") || p.cultivationStyle().contains("core")) {
            objects.add(new ObjectSpec("Spirit Stone Cache", "cultivation fuel", "inferred",
                    "a small cache of low-grade spirit stones"));
        }
        return new RoomSpec(RoomPurpose.CULTIVATION_CHAMBER, "Cultivation Chamber",
                "Cultivation practice — " + p.cultivationStyle(),
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildMeditationRoom(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Worn Cushion", "meditation", "inferred",
                "the only furnishing — a single cushion"));
        objects.add(new ObjectSpec("Dust", "the passage of time", "inferred",
                "untouched corners accumulate dust"));
        return new RoomSpec(RoomPurpose.MEDITATION_ROOM, "Meditation Room",
                "Cultivation — sparse practice (minimalist personality)",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildAlchemyRoom(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Pill Furnace", "alchemy refinement", "canon", ""));
        objects.add(new ObjectSpec("Herb Shelves", "storing medicinal ingredients", "inferred", ""));
        objects.add(new ObjectSpec("Mortar and Pestle", "grinding herbs", "inferred", ""));
        objects.add(new ObjectSpec("Ventilation Shaft", "clearing alchemical fumes", "inferred", ""));
        objects.add(new ObjectSpec("Workbench", "preparing pill recipes", "inferred", ""));
        return new RoomSpec(RoomPurpose.ALCHEMY_ROOM, "Alchemy Room",
                "Alchemy — pill refinement requires a furnace and ventilation",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildHerbGarden(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Planting Beds", "growing medicinal herbs", "inferred", ""));
        objects.add(new ObjectSpec("Spirit Water Source", "watering spirit plants", "inferred",
                "a small channel or rain-fed basin"));
        objects.add(new ObjectSpec("Drying Rack", "preserving harvested herbs", "inferred", ""));
        return new RoomSpec(RoomPurpose.HERB_GARDEN, "Herb Garden",
                "Herb garden — growing medicinal and spirit plants",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildTrainingYard(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Striking Post", "martial practice", "inferred", ""));
        objects.add(new ObjectSpec("Weapon Rack", "storing training weapons", "inferred", ""));
        objects.add(new ObjectSpec("Sandbox", "footwork practice", "inferred",
                "a raked sand patch for practicing movement"));
        return new RoomSpec(RoomPurpose.TRAINING_YARD, "Training Yard",
                "Combat training — martial practice space",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildLibrary(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Jade Slip Shelves", "storing cultivation manuals", "inferred", ""));
        objects.add(new ObjectSpec("Reading Desk", "studying texts", "inferred", ""));
        objects.add(new ObjectSpec("Locked Case", "rare or dangerous texts", "inferred",
                "a warded case for restricted knowledge"));
        return new RoomSpec(RoomPurpose.LIBRARY, "Library",
                "Library — collecting and studying knowledge",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildObservationPost(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Rooftop Platform", "elevated observation", "inferred",
                "a flat section of roof with sightlines in every direction"));
        objects.add(new ObjectSpec("Concealed Vantage", "watching without being seen", "inferred",
                "arranged so the observer is hidden from below"));
        return new RoomSpec(RoomPurpose.OBSERVATION_POST, "Observation Post",
                "Observation — " + p.displayName() + " watches from above",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildHiddenStash(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("False Floor Compartment", "concealing valuables", "inferred",
                "a section of floor that lifts to reveal a cache"));
        objects.add(new ObjectSpec("Warded Box", "protecting critical items", "inferred",
                "a small array-locked box"));
        if (p.inventory().contains("flying_sword")) {
            objects.add(new ObjectSpec("Flying Sword (concealed)", "primary weapon, hidden",
                    "canon", "Wang Lin's flying sword is never left in plain sight"));
        }
        if (p.inventory().contains("jade_slip")) {
            objects.add(new ObjectSpec("Jade Slips (hidden)", "cultivation knowledge, concealed",
                    "inferred", "knowledge is as valuable as weapons"));
        }
        return new RoomSpec(RoomPurpose.HIDDEN_STASH, "Hidden Stash",
                "Concealment — " + p.displayName() + " hides what matters most",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildAnimalPen(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        if (p.inventory().contains("dog") || p.habits().contains("has_dog")) {
            objects.add(new ObjectSpec("Dog Shelter", "housing the family dog", "inferred",
                    "Old Chen's dog sleeps here"));
        }
        if (p.occupation().contains("farmer")) {
            objects.add(new ObjectSpec("Chicken Coop", "poultry housing", "inferred", ""));
            objects.add(new ObjectSpec("Feed Trough", "animal feed", "inferred", ""));
        }
        return new RoomSpec(RoomPurpose.ANIMAL_PEN, "Animal Pen",
                "Animal housing — " + p.displayName() + " keeps animals",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildReception(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Tea Table", "receiving guests", "inferred", ""));
        objects.add(new ObjectSpec("Guest Mat", "seating for visitors", "inferred", ""));
        objects.add(new ObjectSpec("Tea Set", "serving tea to guests", "inferred",
                "offering tea is the first act of hospitality"));
        return new RoomSpec(RoomPurpose.RECEPTION, "Reception Room",
                "Social space — receiving visitors",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildMemorialNook(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Memorial Portrait", "remembering the dead", "inferred", ""));
        objects.add(new ObjectSpec("Incense Holder", "ritual offering", "inferred",
                "incense is lit on anniversaries"));
        objects.add(new ObjectSpec("Memorial Tablet", "names of the departed", "inferred", ""));
        List<ResidenceMemory> griefMemories = p.history().stream()
                .filter(m -> m.emotionalWeight().contains("grief") || m.emotionalWeight().contains("loss"))
                .toList();
        return new RoomSpec(RoomPurpose.MEMORIAL_NOOK, "Memorial Nook",
                "Mourning — remembering those who are gone",
                List.of(), griefMemories, evidence(p));
    }

    private static RoomSpec buildWorkshop(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Workbench", "occupation work", "inferred", ""));
        if (p.occupation().contains("farmer")) {
            objects.add(new ObjectSpec("Hoe", "tilling soil", "inferred", ""));
            objects.add(new ObjectSpec("Sickle", "harvesting grain", "inferred", ""));
            objects.add(new ObjectSpec("Repaired Plow", "repaired after breakage", "inferred",
                    "a plow with visible repair marks — this farmer fixes their own tools"));
        }
        if (p.occupation().contains("carpenter")) {
            objects.add(new ObjectSpec("Saw", "cutting wood", "inferred", ""));
            objects.add(new ObjectSpec("Hammer", "driving nails", "inferred", ""));
        }
        return new RoomSpec(RoomPurpose.WORKSHOP, "Workshop",
                "Workshop — " + p.occupation() + " tools and workspace",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildCourtyard(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Well", "water source", "inferred",
                "a shared or private well"));
        objects.add(new ObjectSpec("Drying Rack", "drying laundry or grain", "inferred", ""));
        return new RoomSpec(RoomPurpose.COURTYARD, "Courtyard",
                "Open-air center — outdoor activities and access",
                objects, List.of(), evidence(p));
    }

    private static RoomSpec buildDefensiveFeatures(ResidentProfile p) {
        List<ObjectSpec> objects = new ArrayList<>();
        objects.add(new ObjectSpec("Reinforced Door Bar", "strengthening the entry", "inferred",
                "a heavier bar than a normal household"));
        objects.add(new ObjectSpec("Escape Route", "fleeing if the door is breached", "inferred",
                "a back window or removable floorboard"));
        if (p.fears().contains("revealing_strength")) {
            objects.add(new ObjectSpec("Alarm Array", "detecting intruders silently", "inferred",
                    "a subtle qi array that alerts without visible signal"));
        }
        return new RoomSpec(RoomPurpose.ENTRY, "Defensive Entry",
                "Defense — " + p.displayName() + " has enemies or fears",
                objects, List.of(), evidence(p));
    }

    private static String evidence(ResidentProfile p) {
        return p.canonSourced() ? "canon" : "inferred";
    }
}
