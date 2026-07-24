# Living Observation Count

> The user's 2026-07-26 design review: "I would add one permanent metric. Not
> '175 canon checks passed.' Instead: **Living Observation Count**. After a
> year you'll have hundreds of these. That's much more valuable than another
> hundred architectural documents, because it records how the simulation
> actually felt to a human observer."

This directory is the permanent home for human observation notes from
playtests of the Er Gen Verse mod. Each entry records a single moment where a
human watched an NPC and judged whether the body language read as alive. The
count of these entries IS the project's primary quality metric — more
important than canon-check counts, file counts, or commit counts.

## Format

Each observation is a single JSON object appended to `living_observations.json`.
The schema:

```json
{
  "id": 1,
  "date": "2026-07-26",
  "scene": "Wang Family Village, west ridge, dusk",
  "setup": "Forced wolf event. Watched Wang Lin for ~5 minutes.",
  "timeUntilNoticed": "12s",
  "playerInterpretation": "He noticed something.",
  "unexpectedBehavior": "Looked away too often.",
  "artificialMoment": "Head instantly snapped back after a glance.",
  "believableScore": 8,
  "fix": "Increase glance recovery duration.",
  "followup": "Retest after fix."
}
```

### Field meanings

- **id** — sequential integer. The "Living Observation Count" metric is the
  highest id ever assigned. This number only goes up.
- **date** — ISO date of the observation.
- **scene** — where in the world this happened.
- **setup** — what was forced / what the observer did.
- **timeUntilNoticed** — how long until the observer could tell the NPC was
  doing something intentional (the user's "Time until noticed" metric).
- **playerInterpretation** — one sentence: what did the observer THINK the
  NPC was doing, based only on watching? This is the critical field. It
  records the SIMULATION AS PERCEIVED, not the simulation as coded.
- **unexpectedBehavior** — anything the NPC did that surprised the observer
  (positively or negatively).
- **artificialMoment** — the single moment that broke the illusion, if any.
  The user's directive: "make exactly one change based on what looked
  artificial. Repeat until the scene no longer feels obviously scripted."
- **believableScore** — 1–10. How alive did this moment feel? The user's
  template uses "Believable? 8/10."
- **fix** — the ONE change to make before the next observation. Per the user:
  never fix two things at once, or you can't tell which fix mattered.
- **followup** — what to retest after the fix is applied.

## The rule this directory enforces

> The user: "You're not evaluating code. You're evaluating perception. That
> matches your Constitution much more closely than measuring files, classes,
> or commits."

A commit that changes code but produces no Living Observation entry is a
commit whose effect on the player's experience is unverified. The Acting
Layer (CRON-21), the Cognitive Body-Language Layer (CRON-19), and the
cognitive predicates (CRON-20) are all HYPOTHETICAL until an observation
note records how they actually felt to watch.

## Current status

The observation log is empty. The first entry cannot be written until a
client playtest is run — the project's most stubborn gap (14+ cycles without
one). The scaffold exists so the moment a playtest is possible, the observer
knows exactly what to record and where.
