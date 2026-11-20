package cs464.experiment.model;

import java.time.Instant;

/** What happened on one question: one row of the trials CSV (see docs/data-dictionary.md). */
public record TrialResult(
        String participantId,
        int orderId,
        int blockIndex,
        String blockId,
        ExplanationLevel explanationLevel,
        int aiAccuracyPercent,
        int trialIndex,
        Question question,
        int originalIndex,
        int finalIndex,
        long initialResponseTimeMs,
        long totalResponseTimeMs,
        Instant answeredAt
) {
    /** True when the participant changed their answer after seeing the AI suggestion. */
    public boolean switchedAnswer() {
        return originalIndex != finalIndex;
    }

    /** Correctness of the final answer. */
    public boolean isCorrect() {
        return finalIndex == question.correctIndex();
    }

    /** True when the final answer matches the AI's suggestion (right or wrong). */
    public boolean followedAi() {
        return finalIndex == question.aiAnswerIndex();
    }
}
