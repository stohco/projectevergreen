package dev.ergenverse.wanglin.registry;

import dev.ergenverse.core.Ergenverse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * AbstractSubRegistry — shared boilerplate for all 18 canonical sub-registries.
 *
 * <p>Every sub-registry extends this class. It maintains an insertion-ordered
 * map of entries keyed by id, plus convenience query methods (by-id, by-tag,
 * by-category, by-ownership, by-teachability). Subclasses populate the
 * registry inside their {@link #bootstrap()} method by calling {@link
 * #register(CanonicalEntry)}.
 *
 * <p>Idempotent: a second call to {@link #bootstrap()} is a no-op.
 */
public abstract class AbstractSubRegistry {

    private final Map<String, CanonicalEntry> entries = new LinkedHashMap<>();
    private volatile boolean bootstrapped = false;
    private final String registryName;

    protected AbstractSubRegistry(String registryName) {
        this.registryName = registryName;
    }

    /** Subclasses implement this — populate the registry by calling register(). */
    protected abstract void doBootstrap();

    /** Idempotent bootstrap. Safe to call multiple times. */
    public final synchronized void bootstrap() {
        if (bootstrapped) return;
        doBootstrap();
        bootstrapped = true;
        Ergenverse.LOGGER.info("[WangLinMasterRegistry]   ✓ {}: {} entries",
                registryName, entries.size());
    }

    /** Register an entry. Throws if a duplicate id is already registered. */
    protected final void register(CanonicalEntry entry) {
        if (entry == null) return;
        if (entries.containsKey(entry.id())) {
            throw new IllegalStateException("Duplicate CanonicalEntry id '" + entry.id()
                    + "' in " + registryName);
        }
        entries.put(entry.id(), entry);
    }

    /** Look up by id. Returns null if not present. */
    public CanonicalEntry get(String id) {
        ensureBootstrapped();
        return entries.get(id);
    }

    /** All entries (immutable copy). */
    public List<CanonicalEntry> all() {
        ensureBootstrapped();
        return Collections.unmodifiableList(new ArrayList<>(entries.values()));
    }

    /** Count. */
    public int size() {
        ensureBootstrapped();
        return entries.size();
    }

    /** Filter by predicate. */
    public List<CanonicalEntry> filter(Predicate<CanonicalEntry> predicate) {
        ensureBootstrapped();
        List<CanonicalEntry> out = new ArrayList<>();
        for (CanonicalEntry e : entries.values()) {
            if (predicate.test(e)) out.add(e);
        }
        return out;
    }

    /** All entries that have the given interaction tag. */
    public List<CanonicalEntry> byTag(String tag) {
        return filter(e -> e.hasTag(tag));
    }

    /** All entries in a given ownership state. */
    public List<CanonicalEntry> byOwnership(OwnershipState state) {
        return filter(e -> e.ownership() == state);
    }

    /** All entries Wang Lin can teach (passes ownership gate AND canTeach != CANNOT/REFUSES). */
    public List<CanonicalEntry> teachable() {
        return filter(e -> e.passesOwnershipGate()
                && e.transferability().canTeach() != Transferability.CanTeach.CANNOT_TEACH
                && e.transferability().canTeach() != Transferability.CanTeach.REFUSES);
    }

    /** All entries with confidence >= the given threshold. */
    public List<CanonicalEntry> byMinConfidence(int minConf) {
        return filter(e -> e.provenance().confidence() >= minConf);
    }

    private void ensureBootstrapped() {
        if (!bootstrapped) bootstrap();
    }
}
