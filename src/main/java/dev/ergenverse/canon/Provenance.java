package dev.ergenverse.canon;

import java.util.List;

/**
 * Provenance — the source-evidence record attached to EVERY canonical fact.
 *
 * <h2>The Prime Directive (rewritten)</h2>
 * <blockquote>
 *   The Er Gen novels are the specification. The game is an implementation of
 *   that specification. If canon is silent, do not invent mechanics during the
 *   reconstruction phase. Record the silence as a gap. Gaps are filled only
 *   after the canon layer is complete, and every gap-filled addition must be
 *   clearly marked as inferred rather than canonical.
 * </blockquote>
 *
 * <h2>Why Provenance?</h2>
 * <p>Every item, technique, beast, character, location, realm, and event in the
 * Canon Layer MUST carry a {@code Provenance} that records:
 * <ul>
 *   <li><b>sourceNovel</b> — which Er Gen novel (Renegade Immortal, ISSTH, etc.)</li>
 *   <li><b>chapters</b> — the chapter(s) where the fact is attested</li>
 *   <li><b>attestation</b> — EXPLICIT (Er Gen wrote it) or INFERRED (reconstructed
 *       from context, cross-referenced wikis, or necessary implication)</li>
 *   <li><b>confidence</b> — 1..5 per the BridgingPolicy scale:
 *        5 = direct canon quote, 4 = canonical implication, 3 = reasonable
 *        reconstruction, 2 = speculation, 1 = low confidence</li>
 *   <li><b>ambiguities</b> — conflicting descriptions, translation variants,
 *       or known gaps. Empty list = no known ambiguity.</li>
 * </ul>
 *
 * <h2>Three-Layer Architecture</h2>
 * <p>This class exists in <b>Layer 1 — Canon Reconstruction</b>, which is
 * immutable. Provenance is never attached to Simulation-layer or Emergent-History
 * facts; those layers have their own provenance types (to be defined when those
 * layers are built). This separation guarantees you can ALWAYS distinguish
 * "this is what Er Gen wrote" from "this is what the simulation created after
 * the player entered the world."
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * Provenance beadOrigin = Provenance.explicit("Renegade Immortal",
 *     List.of("Ch. 8"), 5, "Found in Heng Yue Sect stone bead; recognized later");
 * Provenance bridgingNote = Provenance.inferred("Renegade Immortal",
 *     List.of("Ch. 1290+"), 3, List.of("Exact chapter of Slaughter Crystal condensation ambiguous"));
 * }</pre>
 */
public record Provenance(
        String sourceNovel,
        List<String> chapters,
        Attestation attestation,
        int confidence,
        List<String> ambiguities
) {
    /** Whether a fact is directly written by Er Gen or reconstructed. */
    public enum Attestation {
        /** Er Gen explicitly wrote this. Confidence should be 4-5. */
        EXPLICIT,
        /** Reconstructed from context, wiki cross-reference, or necessary implication. Confidence 1-3. */
        INFERRED
    }

    public Provenance {
        if (sourceNovel == null || sourceNovel.isBlank()) {
            throw new IllegalArgumentException("Provenance requires a sourceNovel");
        }
        if (chapters == null) chapters = List.of();
        if (attestation == null) attestation = Attestation.INFERRED;
        confidence = Math.max(1, Math.min(5, confidence));
        if (ambiguities == null) ambiguities = List.of();
    }

    // ── Convenience factories ─────────────────────────────────────────

    /** An explicitly canon-attested fact (Er Gen wrote it). Confidence defaults to 5. */
    public static Provenance explicit(String novel, List<String> chapters, int confidence, String... ambiguities) {
        return new Provenance(novel, chapters, Attestation.EXPLICIT, confidence,
                ambiguities.length == 0 ? List.of() : List.of(ambiguities));
    }

    /** An explicitly canon-attested fact with no known ambiguities. */
    public static Provenance explicit(String novel, List<String> chapters, int confidence) {
        return new Provenance(novel, chapters, Attestation.EXPLICIT, confidence, List.of());
    }

    /** An inferred/reconstructed fact. Confidence should be 1-3. */
    public static Provenance inferred(String novel, List<String> chapters, int confidence, String... ambiguities) {
        return new Provenance(novel, chapters, Attestation.INFERRED, confidence,
                ambiguities.length == 0 ? List.of() : List.of(ambiguities));
    }

    /** A fact drawn from cross-referenced wiki research (Fandom/Baidu Baike). */
    public static Provenance wiki(String novel, List<String> chapters, int confidence, String... ambiguities) {
        return new Provenance(novel, chapters, Attestation.INFERRED, confidence,
                ambiguities.length == 0 ? List.of() : List.of(ambiguities));
    }

    // ── Display helpers ───────────────────────────────────────────────

    /** A compact one-line citation for tooltips, e.g. "RI Ch. 8 [EXPLICIT 5/5]". */
    public String citation() {
        StringBuilder sb = new StringBuilder();
        sb.append(novelAbbreviation()).append(" ");
        sb.append(chapters.isEmpty() ? "(no chapter)" : String.join(",", chapters));
        sb.append(" [").append(attestation == Attestation.EXPLICIT ? "EXPLICIT" : "INFERRED")
          .append(" ").append(confidence).append("/5]");
        return sb.toString();
    }

    /** Abbreviate the novel name for compact display. */
    public String novelAbbreviation() {
        return switch (sourceNovel.toLowerCase()) {
            case "renegade immortal", "仙逆", "xian ni" -> "RI";
            case "i shall seal the heavens", "issth" -> "ISSTH";
            case "a world worth protecting", "awwp" -> "AWWP";
            case "pursuit of truth" -> "PoT";
            case "renegade immortal: wang lin" -> "RI";
            default -> sourceNovel.length() > 8 ? sourceNovel.substring(0, 8) : sourceNovel;
        };
    }

    /** Whether this fact is directly canonical (EXPLICIT with confidence >= 4). */
    public boolean isCanonConcrete() {
        return attestation == Attestation.EXPLICIT && confidence >= 4;
    }

    /** Whether this fact is a reasonable reconstruction (INFERRED with confidence >= 3). */
    public boolean isReasonableReconstruction() {
        return attestation == Attestation.INFERRED && confidence >= 3;
    }
}
