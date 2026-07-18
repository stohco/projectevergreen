package dev.ergenverse.wanglin.behavior;

/**
 * VisualDescription — the physical, material, color, texture, animation, particle, lighting, and sound of the item.
 *
 * <p>This is what the artist / modeler / VFX designer needs to make the
 * Forge implementation look recognizably like the novel version.
 *
 * @param physicalDescription  free-text physical description
 * @param materials            materials the item is made of
 * @param primaryColor         primary color (hex or named)
 * @param secondaryColor       secondary color
 * @param textureNotes         texture / surface quality
 * @param dimensionsBlocks     physical dimensions in Minecraft blocks (e.g. "1×1×2")
 * @param idleAnimation        idle animation description
 * @param activatedAnimation   animation when activated
 * @param particles            particle effects (color, density, motion)
 * @param lighting             lighting / glow description
 * @param soundOnActivate      sound on activation
 * @param soundOnIdle          ambient idle sound
 * @param soundOnHit           sound on impact
 */
public record VisualDescription(
        String physicalDescription,
        java.util.List<String> materials,
        String primaryColor,
        String secondaryColor,
        String textureNotes,
        String dimensionsBlocks,
        String idleAnimation,
        String activatedAnimation,
        String particles,
        String lighting,
        String soundOnActivate,
        String soundOnIdle,
        String soundOnHit
) {
    public static final VisualDescription UNKNOWN = new VisualDescription(
            "UNKNOWN — canon silent on physical description.",
            java.util.List.of(), "UNKNOWN", "UNKNOWN", "UNKNOWN",
            "UNKNOWN", "UNKNOWN", "UNKNOWN",
            "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN");

    public VisualDescription {
        if (physicalDescription == null) physicalDescription = "UNKNOWN";
        if (materials == null) materials = java.util.List.of();
        if (primaryColor == null) primaryColor = "UNKNOWN";
        if (secondaryColor == null) secondaryColor = "UNKNOWN";
        if (textureNotes == null) textureNotes = "UNKNOWN";
        if (dimensionsBlocks == null) dimensionsBlocks = "UNKNOWN";
        if (idleAnimation == null) idleAnimation = "UNKNOWN";
        if (activatedAnimation == null) activatedAnimation = "UNKNOWN";
        if (particles == null) particles = "UNKNOWN";
        if (lighting == null) lighting = "UNKNOWN";
        if (soundOnActivate == null) soundOnActivate = "UNKNOWN";
        if (soundOnIdle == null) soundOnIdle = "UNKNOWN";
        if (soundOnHit == null) soundOnHit = "UNKNOWN";
    }
}
