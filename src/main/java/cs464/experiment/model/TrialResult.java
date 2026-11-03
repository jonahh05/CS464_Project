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
        int aiAnswerIndex,          // what the AI suggested to THIS participant (fixed or drawn)
        Double aiAccuracyPercent,   // the block's setting; null in fixed mode
        int chosenIndex,            // -1 if the time limit ran out with nothing selected
        long responseTimeMs,
        long timeToSuggestionMs,    // when the suggestion appeared, ms after the question
        boolean timedOut,
        boolean usedAnswerButton,   // the participant pressed "Use this answer" before submitting
        Instant answeredAt
) {
    /** Fixed mode, no time limit, no button: the original columns only. */
    public TrialResult(String participantId, int orderId, int blockIndex, String blockId,
                       ExplanationLevel explanationLevel, int trialIndex, Question question,
                       int chosenIndex, long responseTimeMs, Instant answeredAt) {
        this(participantId, orderId, blockIndex, blockId, explanationLevel, trialIndex, question,
                question.aiAnswerIndex(), null, chosenIndex, responseTimeMs, 0, false, false, answeredAt);
    }

    public boolean answered() {
        return chosenIndex >= 0;
    }

    public boolean isAiCorrect() {
        return aiAnswerIndex == question.correctIndex();
    }

    public boolean isCorrect() {
        return chosenIndex == question.correctIndex();
    }

    /** True when the participant picked the AI's suggestion (right or wrong). */
    public boolean followedAi() {
        return chosenIndex == aiAnswerIndex;
    }
}
