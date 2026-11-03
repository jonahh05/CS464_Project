package cs464.experiment.model;

import java.util.Locale;

/** Whether a set of ratings was collected before or after a block's questions. */
public enum RatingPhase {
    PRE,
    POST;

    public String configName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
