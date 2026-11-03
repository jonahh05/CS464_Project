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
 */
public record BlockConfig(
        String id,
        String title,
        String instructions,
        String explanation,
        Integer orderSlot,
        List<String> questionIds
) {
}
