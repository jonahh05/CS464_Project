package cs464.experiment.model;

import java.util.List;

/**
 * Everything about the session that we expect to tune without touching code.
 * Loaded from src/main/resources/config/experiment.json (see docs/config-format.md).
 *
 * <p>The last four fields are optional; when they are missing from the JSON the app behaves
 * as before (plain helper box, no button, no delay, no time limit).
 */
public record ExperimentConfig(
        String title,
        String outputDir,                    // where CSVs are written, relative to the working directory
        int ratingMin,                       // lowest value on the rating scale (1)
        int ratingMax,                       // highest value on the rating scale (10)
        boolean shuffleQuestionsWithinBlock, // if true, order is shuffled per participant (seeded by ID)
        List<RatingItem> ratingItems,
        List<List<String>> orders,           // counterbalancing orders of explanation levels; order 1 = first entry
        List<BlockConfig> blocks,
        String helperStyle,                  // "box" (default) or "side_card"
        Boolean useAnswerButton,             // show a "Use this answer" button on the helper (default false)
        Integer aiDelayMs,                   // pause before the suggestion appears, in ms (default 0)
        Integer timeLimitSeconds             // per-question time limit, 0 or missing = none
) {
    /** The original eight settings; the helper options take their defaults. */
    public ExperimentConfig(String title, String outputDir, int ratingMin, int ratingMax,
                            boolean shuffleQuestionsWithinBlock, List<RatingItem> ratingItems,
                            List<List<String>> orders, List<BlockConfig> blocks) {
        this(title, outputDir, ratingMin, ratingMax, shuffleQuestionsWithinBlock, ratingItems, orders, blocks,
                null, null, null, null);
    }

    public HelperStyle helper() {
        return HelperStyle.fromConfig(helperStyle);
    }

    public boolean showUseAnswerButton() {
        return Boolean.TRUE.equals(useAnswerButton);
    }

    public int aiDelay() {
        return aiDelayMs == null ? 0 : aiDelayMs;
    }

    public int timeLimit() {
        return timeLimitSeconds == null ? 0 : timeLimitSeconds;
    }
}
