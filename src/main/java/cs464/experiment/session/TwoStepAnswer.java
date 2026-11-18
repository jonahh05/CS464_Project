package cs464.experiment.session;

/**
 * State of one question's two-step answer: the participant selects and submits an
 * original answer, only then sees the AI suggestion, and may keep or change their
 * selection before submitting a second time. UI-independent so it can be unit tested.
 */
public final class TwoStepAnswer {

    private int selected = -1;
    private int original = -1;
    private int finalAnswer = -1;
    private boolean aiVisible;
    private boolean complete;

    public void select(int index) {
        if (complete) {
            throw new IllegalStateException("Question already completed");
        }
        selected = index;
    }

    public boolean canSubmit() {
        return selected >= 0 && !complete;
    }

    /** First submit records the original answer and reveals the AI; second submit records the final answer. */
    public void submit() {
        if (!canSubmit()) {
            throw new IllegalStateException("Nothing selected or question already completed");
        }
        if (!aiVisible) {
            original = selected;
            aiVisible = true;
        } else {
            finalAnswer = selected;
            complete = true;
        }
    }

    public boolean aiVisible() {
        return aiVisible;
    }

    /** True once the second submit has happened; only then may the experiment move on. */
    public boolean isComplete() {
        return complete;
    }

    public int originalIndex() {
        return original;
    }

    public int finalIndex() {
        return finalAnswer;
    }
}
