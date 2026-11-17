package cs464.experiment.model;

import java.util.List;

/**
 * One question on the "About you" screen shown before the Welcome screen.
 * With {@code choices} it is a single choice question; without, a free text box.
 * All demographic questions are optional, so give choice questions a "Prefer not to say" option.
 */
public record DemographicQuestion(
        String id,           // becomes item_id in the demographics CSV
        String prompt,
        List<String> choices, // empty or missing = free text answer
        boolean allowOther   // adds an "Other:" choice with a text box
) {

    public boolean isFreeText() {
        return choices == null || choices.isEmpty();
    }
}
