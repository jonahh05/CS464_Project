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
 * <p>How the AI's suggestions are decided (set at most one of the two optional keys):
 * <ul>
 *   <li>Neither: the AI suggests each question's stored {@code ai_answer_index} (fixed mode).</li>
 *   <li>{@code aiAccuracyPercent} (0-100): each suggestion is drawn on its own: the correct choice with
 *       probability p, each wrong choice with (1 - p) / (number of wrong choices). The number of wrong
 *       suggestions per round varies between participants.</li>
 *   <li>{@code aiWrongCount}: exactly this many questions in the round get a wrong suggestion, for every
 *       participant. Which questions, and which wrong choice, are drawn per participant.</li>
 * </ul>
 */
public record BlockConfig(
        String id,
        String title,
        String instructions,
        String explanation,
        Integer orderSlot,
        List<String> questionIds,
        Double aiAccuracyPercent,
        Integer aiWrongCount
) {
    /** A block in fixed mode (no {@code ai_accuracy_percent} or {@code ai_wrong_count}). */
    public BlockConfig(String id, String title, String instructions, String explanation,
                       Integer orderSlot, List<String> questionIds) {
        this(id, title, instructions, explanation, orderSlot, questionIds, null, null);
    }

    /** A block with {@code ai_accuracy_percent} (or fixed mode when null). */
    public BlockConfig(String id, String title, String instructions, String explanation,
                       Integer orderSlot, List<String> questionIds, Double aiAccuracyPercent) {
        this(id, title, instructions, explanation, orderSlot, questionIds, aiAccuracyPercent, null);
    }

    /** True when suggestions are drawn per participant ({@code ai_accuracy_percent} or {@code ai_wrong_count}). */
    public boolean drawsSuggestions() {
        return aiAccuracyPercent != null || aiWrongCount != null;
    }

    /**
     * The round's planned accuracy in percent: the setting for {@code ai_accuracy_percent}, the exact share of
     * correct suggestions for {@code ai_wrong_count}, or null in fixed mode.
     */
    public Double plannedAccuracyPercent() {
        if (aiWrongCount != null && questionIds != null && !questionIds.isEmpty()) {
            return 100.0 * (questionIds.size() - aiWrongCount) / questionIds.size();
        }
        return aiAccuracyPercent;
    }
}
