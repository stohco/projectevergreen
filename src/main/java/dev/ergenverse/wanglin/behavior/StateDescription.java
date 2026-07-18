package dev.ergenverse.wanglin.behavior;

/**
 * StateDescription — a single state of the item (idle / activated / damaged / spirit-manifested).
 *
 * @param stateName           the state's name
 * @param stateDescription    what the state looks and behaves like
 * @param entryCondition      how the item enters this state
 * @param exitCondition       how the item exits this state
 * @param canonBasis          chapter citation
 */
public record StateDescription(
        String stateName,
        String stateDescription,
        String entryCondition,
        String exitCondition,
        String canonBasis
) {
    public StateDescription {
        if (stateName == null || stateName.isBlank()) {
            throw new IllegalArgumentException("StateDescription requires a stateName");
        }
        if (stateDescription == null) stateDescription = "";
        if (entryCondition == null) entryCondition = "";
        if (exitCondition == null) exitCondition = "";
        if (canonBasis == null) canonBasis = "";
    }
}
