package dev.ergenverse.simulation.residence;

/**
 * ResidenceMemory — a memory attached to a location within a residence.
 *
 * <p>This is the user's 2026-07-26 directive: "Interiors become almost free
 * if every object has purpose." But purpose alone isn't enough — a residence
 * is also a <b>biography</b>. The Constitution (Article XLV §5) already
 * requires "Interiors Are Evidence, Not Furniture." A ResidenceMemory is the
 * unit of that evidence: a specific event that happened at a specific place
 * in this house, carrying emotional weight.
 *
 * <p>Examples:
 * <ul>
 *   <li>"Old Chen's dog died in the north corner of the courtyard" — grief</li>
 *   <li>"Wang Lin repaired the fence after the spring storm" — pride</li>
 *   <li>"The roof leaked during the autumn rains" — mild frustration</li>
 *   <li>"Wang Lin's mother keeps the family's savings in the bedroom chest" — trust</li>
 * </ul>
 *
 * <p>These memories are why two houses with identical room layouts feel
 * completely different. The blocks are the same; the memories are not.
 *
 * @param event          what happened (e.g. "dog died here")
 * @param location       where in the residence (e.g. "courtyard_north_corner")
 * @param evidence       "canon" if from the novels; "inferred" if derived
 * @param emotionalWeight the feeling this memory carries — guides whether a
 *                       future observer (player or NPC) should read the space
 *                       as heavy, warm, tense, etc.
 */
public record ResidenceMemory(
        String event,
        String location,
        String evidence,
        String emotionalWeight
) {
    public ResidenceMemory {
        if (event == null || event.isBlank()) throw new IllegalArgumentException("event");
        if (location == null || location.isBlank()) throw new IllegalArgumentException("location");
        if (evidence == null) evidence = "inferred";
        if (emotionalWeight == null) emotionalWeight = "neutral";
    }
}
