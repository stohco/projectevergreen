package dev.ergenverse.simulation.actor;

/**
 * ActorType — 17 canonical actor types in the simulation.
 *
 * <p>The actor framework is unified: every world-actor — beast, NPC, player,
 * sect, settlement, spirit vein, formation, territory — is an {@link Actor}.
 * The {@link ActorType} distinguishes their behavior.
 */
public enum ActorType {
    BEAST          ("Spirit Beast"),
    NPC            ("NPC Cultivator"),
    PLAYER         ("Player Cultivator"),
    SECT           ("Sect"),
    SETTLEMENT     ("Settlement / Village / City"),
    SPIRIT_VEIN    ("Spirit Vein"),
    FORMATION      ("Formation"),
    TERRITORY      ("Territory"),
    CARAVAN        ("Caravan"),
    HERB_FIELD     ("Herb Field"),
    ARRAY          ("Array / Spirit Array"),
    ARTIFACT       ("Sentient Artifact"),
    MACRO_TERRAIN  ("Macro Terrain (terrain that IS a being)"),
    ANCIENT_BEAST  ("Ancient Apex Beast"),
    SPIRIT         ("Spirit / Soul Fragment"),
    INHERITANCE    ("Inheritance Tomb / Cave"),
    WORLD_WILL     ("World Will");

    public final String label;

    ActorType(String label) {
        this.label = label;
    }

    /** Canonical count — canon audit requires 17. */
    public static final int CANON_COUNT = 17;
}
