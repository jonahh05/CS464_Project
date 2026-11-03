package cs464.experiment.model;

/**
 * The scripted AI's explanation for suggesting one particular answer choice.
 * Used when a block draws the AI's suggestion at random (see {@code ai_accuracy_percent}),
 * because then any choice can be suggested and each needs its own argument.
 */
public record ChoiceExplanation(
        String brief,     // one or two sentences arguing for this choice
        String detailed   // step-by-step reasoning arguing for this choice
) {
    public String forLevel(ExplanationLevel level) {
        switch (level) {
            case BRIEF:
                return brief;
            case DETAILED:
                return detailed;
            default:
                return null;
        }
    }
}
