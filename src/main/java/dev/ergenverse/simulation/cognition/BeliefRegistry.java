package dev.ergenverse.simulation.cognition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BeliefRegistry — holds all beliefs for a single actor.
 *
 * <p>Provides operations for the four canonical belief dynamics:
 * <ul>
 *   <li>{@link #decay} — apply per-tick decay to all beliefs.</li>
 *   <li>{@link #reinforce} — when a new observation matches an existing belief,
 *       strengthen it.</li>
 *   <li>{@link #contradict} — when a new observation conflicts with an existing
 *       belief, lower its strength and possibly replace it.</li>
 *   <li>{@link #replace} — substitute one belief's value with a new value.</li>
 * </ul>
 *
 * <p>Beliefs that decay below strength 0.05 are removed. Beliefs that reach
 * confidence ≥ 0.7 AND strength ≥ 0.7 are returned by {@link #promoteReady()}
 * so the actor can move them into {@link Knowledge}.
 */
public final class BeliefRegistry {

    private final Map<String, Belief> beliefs = new HashMap<>();

    public void add(Belief b) {
        beliefs.put(b.key(), b);
    }

    public Belief get(String subject, String predicate) {
        return beliefs.get(subject + "." + predicate);
    }

    public Collection<Belief> all() {
        return beliefs.values();
    }

    public int size() {
        return beliefs.size();
    }

    /** Apply per-tick decay to all beliefs. */
    public void decay(double amount) {
        List<String> toRemove = new ArrayList<>();
        for (Belief b : beliefs.values()) {
            b.decay(amount);
            if (b.strength < 0.05) toRemove.add(b.key());
        }
        for (String k : toRemove) beliefs.remove(k);
    }

    /** Reinforce an existing belief by subject+predicate. No-op if absent. */
    public void reinforce(String subject, String predicate, double amount) {
        Belief b = beliefs.get(subject + "." + predicate);
        if (b != null) b.reinforce(amount);
    }

    /** Contradict an existing belief — lowers strength, replaces value if strength collapses. */
    public void contradict(String subject, String predicate, String newValue,
                           Belief.Source newSource, long tick) {
        Belief existing = beliefs.get(subject + "." + predicate);
        if (existing == null) {
            // No prior belief — just add the new one.
            add(new Belief(subject, predicate, newValue, newSource, 0.5, tick));
            return;
        }
        existing.strength -= 0.3;
        if (existing.strength < 0.2) {
            // Replace.
            existing.value = newValue;
            existing.strength = 0.5;
            existing.confidence = 0.5;
            existing.lastUpdatedTick = tick;
        }
    }

    /** Replace a belief's value outright (used when authority supersedes). */
    public void replace(String subject, String predicate, String newValue,
                        Belief.Source newSource, long tick) {
        Belief existing = beliefs.get(subject + "." + predicate);
        if (existing == null) {
            add(new Belief(subject, predicate, newValue, newSource, 0.6, tick));
            return;
        }
        existing.value = newValue;
        existing.confidence = 0.6;
        existing.strength = 0.6;
        existing.lastUpdatedTick = tick;
    }

    /** Returns beliefs ready to be promoted to Knowledge. */
    public List<Belief> promoteReady() {
        List<Belief> ready = new ArrayList<>();
        for (Belief b : beliefs.values()) {
            if (b.readyToPromote()) ready.add(b);
        }
        return ready;
    }
}
