package cs464.experiment.model;

import java.time.Instant;

/** What happened on one question: one row of the trials CSV (see docs/data-dictionary.md). */
public record TrialResult(
        String participantId,
        int orderId,
        int blockIndex,
        String blockId,
        ExplanationLevel explanationLevel,
        int trialIndex,
        Question question,
        int chosenIndex,
        long responseTimeMs,
        Instant answeredAt
) {
    public boolean isCorrect() {
        return chosenIndex == question.correctIndex();
    }

    /** True when the participant picked the AI's suggestion (right or wrong). */
    public boolean followedAi() {
        return chosenIndex == question.aiAnswerIndex();
    }
}
