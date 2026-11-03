package cs464.experiment.model;

import java.util.List;

/**
 * Everything about the session that we expect to tune without touching code.
 * Loaded from src/main/resources/config/experiment.json (see docs/config-format.md).
 */
public record ExperimentConfig(
        String title,
        String outputDir,                    // where CSVs are written, relative to the working directory
        int ratingMin,                       // lowest value on the rating scale (1)
        int ratingMax,                       // highest value on the rating scale (10)
        boolean shuffleQuestionsWithinBlock, // if true, order is shuffled per participant (seeded by ID)
        List<RatingItem> ratingItems,
        List<List<String>> orders,           // counterbalancing orders of explanation levels; order 1 = first entry
        List<BlockConfig> blocks
) {
}
