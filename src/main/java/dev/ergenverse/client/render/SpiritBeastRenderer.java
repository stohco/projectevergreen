package dev.ergenverse.client.render;

/**
 * DEPRECATED — this file is retained ONLY for git history continuity.
 *
 * <p>This class was the original "one renderer for all beasts" approach that used
 * vanilla Minecraft models (WolfModel, RabbitModel, CowModel, ParrotModel, PigModel).
 * It violated Constitution Article I: "If Minecraft conflicts with canon: Minecraft changes."
 * A Spirit Wolf is NOT a recolored vanilla WolfModel — it has its own anatomy.
 *
 * <p>REPLACED BY: {@link SpiritBeastRenderers} — per-beast-type renderers that each
 * use their own custom model (SpiritWolfModel, SpiritHawkModel, etc.).
 *
 * <p>This class is no longer registered in {@link dev.ergenverse.client.ClientEvents}.
 * It is dead code. Do NOT reference it from new code.
 *
 * @deprecated Use {@link SpiritBeastRenderers} instead.
 */
@Deprecated(since = "CRON-COMPLETIONIST-1", forRemoval = true)
public class SpiritBeastRenderer {
    // Intentionally empty. All rendering logic moved to SpiritBeastRenderers.
    // This class exists solely so git history shows a clean rename/replace chain.
}