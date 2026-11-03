package cs464.experiment.model;

import java.util.List;

/**
 * One block (round) as written in experiment.json.
 *
 * <p>Exactly one of {@code explanation} or {@code orderSlot} is set:
 * <ul>
 *   <li>{@code explanation}: a fixed level for every participant (used for the baseline block).</li>
 *   <li>{@code orderSlot}: 0-based position in the participant's counterbalancing order, so the
 *       level depends on which order they were assigned. The questions stay the same.</li>
 * </ul>
 *
 * <p>{@code aiAccuracyPercent} is optional. When it is missing, the AI suggests each question's
 * stored {@code ai_answer_index} (fixed mode). When it is set (0-100), each suggestion is drawn:
 * the correct choice with probability p, each wrong choice with (1 - p) / (number of wrong choices).
 */
public record BlockConfig(
        String id,
        String title,
        String instructions,
        String explanation,
        Integer orderSlot,
        List<String> questionIds,
        Double aiAccuracyPercent
) {
    /** A block in fixed mode (no {@code ai_accuracy_percent}). */
    public BlockConfig(String id, String title, String instructions, String explanation,
                       Integer orderSlot, List<String> questionIds) {
        this(id, title, instructions, explanation, orderSlot, questionIds, null);
    }

    public boolean drawsSuggestions() {
        return aiAccuracyPercent != null;
    }
}
