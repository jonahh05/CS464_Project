package cs464.experiment.model;

import java.util.Locale;

/** How the AI helper is drawn on the question screen ({@code helper_style} in experiment.json). */
public enum HelperStyle {
    /** A box under the answer choices: heading, suggested letter, explanation. */
    BOX,
    /** A card beside the question: letter badge, the choice text, a "Why" box. */
    SIDE_CARD;

    /** Parses "box" or "side_card"; missing means {@link #BOX}. */
    public static HelperStyle fromConfig(String value) {
        if (value == null || value.isBlank()) {
            return BOX;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown helper_style '" + value + "' (expected box or side_card)");
        }
    }

    public String configName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
