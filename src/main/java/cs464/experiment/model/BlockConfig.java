package cs464.experiment.model;

import java.util.List;

/**
 * One block (section) as written in experiment.json. The experiment has 9 sections of 4 questions
 * each (2 "math" + 2 "reading_writing", i.e. English); pre/post ratings surround every section.
 *
 * <p>Exactly one of {@code explanation} or {@code orderSlot} is set:
 * <ul>
 *   <li>{@code explanation}: a fixed level for every participant.</li>
 *   <li>{@code orderSlot}: 0-based position in the participant's counterbalancing order, so the
 *       level depends on which order they were assigned. The questions stay the same. Several
 *       blocks may share a slot (3 sections per explanation level).</li>
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
