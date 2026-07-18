package dev.ergenverse.wanglin.behavior;

/**
 * ActivationModel — how the item / technique is activated.
 *
 * @param trigger        the trigger type (incantation, will, blood, gesture, ritual, etc.)
 * @param triggerDetail  free-text canon detail
 * @param requiredRealm  the realm the cultivator must be at (descriptive, not a gate)
 * @param requiredItems  items that must be present (e.g. inkstones for Restriction Flag)
 * @param requiredConditions conditions that must hold (e.g. extreme-Yin site, daytime, etc.)
 */
public record ActivationModel(
        Trigger trigger,
        String triggerDetail,
        String requiredRealm,
        java.util.List<String> requiredItems,
        java.util.List<String> requiredConditions
) {
    public enum Trigger {
        INCANTATION,            // spoken spell
        WILL_THOUGHT,           // thought-only activation
        BLOOD_SACRIFICE,        // blood offering
        GESTURE_HANDSIGN,       // hand seal / mudra
        RITUAL_CEREMONY,        // multi-step ritual
        PASSIVE_ALWAYS_ON,      // always active
        ITEM_INTERACTION,       // right-click / use-item interaction
        DIVINE_SENSE_STRIKE,    // launched via divine sense
        TRIBULATION_TRIGGER,    // triggered by divine tribulation
        ENVIRONMENTAL_TRIGGER,  // triggered by environmental condition
        UNKNOWN                 // canon silent
    }

    public static final ActivationModel UNKNOWN = new ActivationModel(
            Trigger.UNKNOWN, "UNKNOWN — canon silent on activation.",
            "UNKNOWN", java.util.List.of(), java.util.List.of());

    public ActivationModel {
        if (trigger == null) trigger = Trigger.UNKNOWN;
        if (triggerDetail == null) triggerDetail = "";
        if (requiredRealm == null) requiredRealm = "UNKNOWN";
        if (requiredItems == null) requiredItems = java.util.List.of();
        if (requiredConditions == null) requiredConditions = java.util.List.of();
    }
}
