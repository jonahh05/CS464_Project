package cs464.experiment.session;

import cs464.experiment.model.BlockConfig;
import cs464.experiment.model.ChoiceExplanation;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;
import cs464.experiment.model.RatingItem;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The ai_accuracy_percent draw: exact mapping, distribution, reproducibility, validation. */
class SuggestionDrawerTest {

    @Test
    void pickSplitsProbabilityExactlyAsSpecified() {
        // 80%, correct = B (1). [0, 0.8) -> B; then 0.0667 each for A, C, D in display order.
        assertEquals(1, SuggestionDrawer.pick(0.00, 4, 1, 80));
        assertEquals(1, SuggestionDrawer.pick(0.799, 4, 1, 80));
        assertEquals(0, SuggestionDrawer.pick(0.80, 4, 1, 80));
        assertEquals(0, SuggestionDrawer.pick(0.866, 4, 1, 80));
        assertEquals(2, SuggestionDrawer.pick(0.867, 4, 1, 80));
        assertEquals(3, SuggestionDrawer.pick(0.934, 4, 1, 80));
        assertEquals(3, SuggestionDrawer.pick(0.99999, 4, 1, 80));
    }

    @Test
    void hundredPercentIsAlwaysCorrectAndZeroNever() {
        for (int i = 0; i < 2000; i++) {
            assertEquals(2, SuggestionDrawer.draw("P" + i, "b", "q", 4, 2, 100));
            assertNotEquals(2, SuggestionDrawer.draw("P" + i, "b", "q", 4, 2, 0));
        }
    }

    @Test
    void drawsMatchTheTargetDistribution() {
        int n = 200_000;
        int[] counts = new int[4];
        for (int i = 0; i < n; i++) {
            counts[SuggestionDrawer.draw("P" + i, "round_a", "set1-0" + (i % 6), 4, 0, 80)]++;
        }
        assertEquals(0.80, counts[0] / (double) n, 0.005);
        for (int c = 1; c < 4; c++) {
            assertEquals(0.2 / 3, counts[c] / (double) n, 0.005, "choice " + c);
        }
    }

    @Test
    void sameParticipantGetsTheSameSuggestions() {
        ExperimentConfig config = drawnConfig(80.0);
        Map<String, Question> questions = questions();
        ExperimentSession a = ExperimentSession.create(config, questions, "P05", 5);
        ExperimentSession b = ExperimentSession.create(config, questions, "P05", 5);
        assertEquals(a.blocks(), b.blocks());
    }

    @Test
    void differentParticipantsGetDifferentDraws() {
        ExperimentConfig config = drawnConfig(50.0);
        Map<String, Question> questions = questions();
        long distinct = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(p -> ExperimentSession.create(config, questions, String.format("P%02d", p), 1)
                        .blocks().get(1).trials().stream().map(PlannedTrial::aiAnswerIndex).toList())
                .distinct().count();
        assertTrue(distinct > 6, "only " + distinct + " distinct suggestion patterns across 12 participants");
    }

    @Test
    void baselineAt100PercentIsAlwaysCorrect() {
        ExperimentConfig config = drawnConfig(80.0);
        for (int p = 1; p <= 50; p++) {
            ExperimentSession s = ExperimentSession.create(config, questions(), "P" + p, 1);
            assertEquals(1.0, s.blocks().get(0).aiAccuracy());
        }
    }

    @Test
    void drawnExplanationArguesForTheDrawnChoice() {
        Question q = questions().get("q2");
        PlannedTrial trial = new PlannedTrial(q, 3);
        assertEquals("brief for D", trial.explanationFor(ExplanationLevel.BRIEF));
        assertEquals("detailed for D", trial.explanationFor(ExplanationLevel.DETAILED));
        assertEquals(null, trial.explanationFor(ExplanationLevel.NONE));
    }

    @Test
    void validatorRequiresAnExplanationForEveryChoiceThatCanBeDrawn() {
        Map<String, Question> questions = questions();
        assertEquals(List.of(), ConfigValidator.validate(drawnConfig(80.0), questions));

        // q3 only has explanations for its stored answer, so drawing is not possible.
        questions.put("q3", new Question("q3", "math", "easy", "stem", List.of("a", "b", "c", "d"),
                2, 2, "brief", "detailed", "test"));
        List<String> problems = ConfigValidator.validate(drawnConfig(80.0), questions);
        assertTrue(problems.stream().anyMatch(p -> p.contains("'q3' needs a brief explanation for choice A")),
                problems.toString());
        assertTrue(problems.stream().noneMatch(p -> p.contains("'q3' needs a brief explanation for choice C")),
                problems.toString());
    }

    @Test
    void validatorRejectsAccuracyOutsideZeroToHundred() {
        List<String> problems = ConfigValidator.validate(drawnConfig(120.0), questions());
        assertTrue(problems.stream().anyMatch(p -> p.contains("must be between 0 and 100")), problems.toString());
    }

    @Test
    void validatorRejectsUnknownHelperStyle() {
        ExperimentConfig base = drawnConfig(80.0);
        ExperimentConfig bad = new ExperimentConfig(base.title(), base.outputDir(), 1, 10, false,
                base.ratingItems(), base.orders(), base.blocks(), "popup", true, 900, 0);
        assertTrue(ConfigValidator.validate(bad, questions()).stream().anyMatch(p -> p.contains("helper_style")));
    }

    @Test
    void validatorRejectsDelayLongerThanTimeLimit() {
        ExperimentConfig base = drawnConfig(80.0);
        ExperimentConfig bad = new ExperimentConfig(base.title(), base.outputDir(), 1, 10, false,
                base.ratingItems(), base.orders(), base.blocks(), "side_card", true, 5000, 5);
        assertTrue(ConfigValidator.validate(bad, questions()).stream().anyMatch(p -> p.contains("ai_delay_ms must be shorter")));
    }

    @Test
    void wrongCountGivesEveryParticipantExactlyThatManyWrongSuggestions() {
        ExperimentConfig config = countConfig(2);
        for (int p = 1; p <= 200; p++) {
            ExperimentSession s = ExperimentSession.create(config, questions(), "P" + p, 1);
            PlannedBlock a = s.blocks().get(1);
            long wrong = a.trials().stream().filter(t -> !t.isAiCorrect()).count();
            assertEquals(2, wrong, "participant P" + p);
            assertEquals(50.0, a.aiAccuracyPercent()); // 2 of 4 wrong -> planned 50%
            assertEquals(1.0, s.blocks().get(0).aiAccuracy()); // baseline stays 100%
        }
    }

    @Test
    void wrongCountVariesWhichQuestionsAndLettersAcrossParticipants() {
        ExperimentConfig config = countConfig(1);
        java.util.Set<String> patterns = new java.util.HashSet<>();
        for (int p = 1; p <= 12; p++) {
            patterns.add(ExperimentSession.create(config, questions(), String.format("P%02d", p), 1)
                    .blocks().get(1).trials().stream().map(t -> String.valueOf(t.aiAnswerIndex())).reduce("", String::concat));
        }
        assertTrue(patterns.size() > 4, "only " + patterns.size() + " patterns");
        // Reproducible for the same participant.
        assertEquals(ExperimentSession.create(config, questions(), "P07", 1).blocks(),
                ExperimentSession.create(config, questions(), "P07", 1).blocks());
    }

    @Test
    void validatorChecksWrongCountRangeAndExclusivity() {
        assertEquals(List.of(), ConfigValidator.validate(countConfig(1), questions()));
        assertTrue(ConfigValidator.validate(countConfig(5), questions()).stream()
                .anyMatch(p -> p.contains("ai_wrong_count 5 must be between 0")));
        ExperimentConfig base = countConfig(1);
        List<BlockConfig> blocks = new java.util.ArrayList<>(base.blocks());
        BlockConfig a = blocks.get(1);
        blocks.set(1, new BlockConfig(a.id(), a.title(), a.instructions(), a.explanation(), a.orderSlot(),
                a.questionIds(), 80.0, 1));
        ExperimentConfig both = new ExperimentConfig(base.title(), base.outputDir(), 1, 10, false,
                base.ratingItems(), base.orders(), blocks, "side_card", true, 900, 0);
        assertTrue(ConfigValidator.validate(both, questions()).stream().anyMatch(p -> p.contains("not both")));
    }

    // --- fixtures ---------------------------------------------------------------------------

    private static ExperimentConfig countConfig(int wrongPerRound) {
        ExperimentConfig base = drawnConfig(80.0);
        List<BlockConfig> blocks = new java.util.ArrayList<>();
        for (BlockConfig b : base.blocks()) {
            Integer count = b.id().equals("baseline") ? 0 : wrongPerRound;
            blocks.add(new BlockConfig(b.id(), b.title(), b.instructions(), b.explanation(), b.orderSlot(),
                    b.questionIds(), null, count));
        }
        return new ExperimentConfig(base.title(), base.outputDir(), 1, 10, false,
                base.ratingItems(), base.orders(), blocks, "side_card", true, 900, 0);
    }

    private static ExperimentConfig drawnConfig(double accuracy) {
        List<List<String>> orders = List.of(
                List.of("none", "brief", "detailed"),
                List.of("none", "detailed", "brief"),
                List.of("brief", "none", "detailed"),
                List.of("brief", "detailed", "none"),
                List.of("detailed", "none", "brief"),
                List.of("detailed", "brief", "none"));
        List<BlockConfig> blocks = List.of(
                new BlockConfig("baseline", "Baseline", "", "none", null, List.of("q1"), 100.0),
                new BlockConfig("a", "A", "", null, 0, List.of("q2", "q5", "q6", "q7"), accuracy),
                new BlockConfig("b", "B", "", null, 1, List.of("q3"), accuracy),
                new BlockConfig("c", "C", "", null, 2, List.of("q4"), accuracy));
        List<RatingItem> items = List.of(new RatingItem("conf_ai", "before?", "after?"));
        return new ExperimentConfig("Test", "data", 1, 10, false, items, orders, blocks,
                "side_card", true, 900, 0);
    }

    private static Map<String, Question> questions() {
        Map<String, Question> map = new HashMap<>();
        map.put("q1", drawable("q1", 0));
        for (String id : List.of("q2", "q3", "q4", "q5", "q6", "q7")) {
            map.put(id, drawable(id, 1));
        }
        return map;
    }

    private static Question drawable(String id, int correct) {
        List<ChoiceExplanation> byChoice = List.of(
                new ChoiceExplanation("brief for A", "detailed for A"),
                new ChoiceExplanation("brief for B", "detailed for B"),
                new ChoiceExplanation("brief for C", "detailed for C"),
                new ChoiceExplanation("brief for D", "detailed for D"));
        return new Question(id, "math", "easy", "stem " + id, List.of("a", "b", "c", "d"),
                correct, correct, null, null, "test", byChoice);
    }
}
