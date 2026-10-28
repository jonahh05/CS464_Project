package cs464.experiment.model;

import java.time.Instant;

/** One answered rating item: one row of the ratings CSV (see docs/data-dictionary.md). */
public record RatingResult(
        String participantId,
        int orderId,
        int blockIndex,
        String blockId,
        ExplanationLevel explanationLevel,
        RatingPhase phase,
        String itemId,
        int value,
        Instant answeredAt
) {
}
