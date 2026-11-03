package cs464.experiment.model;

import java.util.Locale;

/** How much explanation the AI helper shows next to its suggested answer. */
public enum ExplanationLevel {
    NONE,
    BRIEF,
    DETAILED;

    /** Parses the lowercase names used in experiment.json ("none", "brief", "detailed"). */
    public static ExplanationLevel fromConfig(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Explanation level is missing");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown explanation level '" + value + "' (expected none, brief or detailed)");
        }
    }

    /** Lowercase name, as written in config files and the CSV output. */
    public String configName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
