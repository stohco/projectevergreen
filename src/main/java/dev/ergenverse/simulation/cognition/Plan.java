package dev.ergenverse.simulation.cognition;

import java.util.ArrayList;
import java.util.List;

/**
 * Plan — a hierarchical plan produced by the {@link Planner}.
 *
 * <p>A Plan consists of a list of {@link PlanStep}s. Each step references a
 * {@link Process} (or an {@link ActionOption.ActionId}), may carry sub-steps
 * (hierarchical), and has a status. The Planner fills the top-level steps;
 * the cognition tick descends into sub-steps when the parent step is
 * "in_progress".
 *
 * <p>Canon audit: Plans are HIERARCHICAL — a "breakthrough" plan has
 * sub-steps: gather resource → enter seclusion → endure tribulation →
 * stabilize. Each sub-step is itself a Process or action.
 */
public final class Plan {

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, ABANDONED
    }

    /** A single step in a hierarchical plan. */
    public static final class PlanStep {
        public final String label;
        public final Process process;          // may be null if action-based
        public final ActionOption.ActionId action; // may be null if process-based
        public Status status = Status.PENDING;
        public final List<PlanStep> subSteps = new ArrayList<>();

        public PlanStep(String label, Process process, ActionOption.ActionId action) {
            this.label = label;
            this.process = process;
            this.action = action;
        }

        public PlanStep withSubStep(PlanStep s) {
            subSteps.add(s);
            return this;
        }

        public boolean isProcessBased() { return process != null; }
        public boolean isActionBased()  { return action != null; }
    }

    public final CognitionGoal goal;
    public final List<PlanStep> steps = new ArrayList<>();
    public Status status = Status.PENDING;
    public int currentStepIndex = 0;

    public Plan(CognitionGoal goal) {
        this.goal = goal;
    }

    public Plan addStep(PlanStep s) {
        steps.add(s);
        return this;
    }

    public PlanStep currentStep() {
        if (currentStepIndex < 0 || currentStepIndex >= steps.size()) return null;
        return steps.get(currentStepIndex);
    }

    public void advance() {
        if (currentStepIndex < steps.size()) {
            steps.get(currentStepIndex).status = Status.COMPLETED;
            currentStepIndex++;
            if (currentStepIndex < steps.size()) {
                steps.get(currentStepIndex).status = Status.IN_PROGRESS;
            } else {
                status = Status.COMPLETED;
            }
        }
    }

    public void fail() {
        status = Status.FAILED;
        PlanStep cur = currentStep();
        if (cur != null) cur.status = Status.FAILED;
    }

    public void abandon() {
        status = Status.ABANDONED;
        PlanStep cur = currentStep();
        if (cur != null) cur.status = Status.ABANDONED;
    }

    public boolean isFinished() {
        return status == Status.COMPLETED || status == Status.FAILED || status == Status.ABANDONED;
    }
}
