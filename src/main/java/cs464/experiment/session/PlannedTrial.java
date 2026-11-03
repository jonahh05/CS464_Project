package cs464.experiment.session;

import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;

/** One question as this participant will see it, including what the AI will suggest. */
public record PlannedTrial(Question question, int aiAnswerIndex) {

    public boolean isAiCorrect() {
        return aiAnswerIndex == question.correctIndex();
    }

    /** The explanation arguing for this trial's suggestion, or null for no explanation. */
    public String explanationFor(ExplanationLevel level) {
        return question.explanationFor(aiAnswerIndex, level);
    }
}
