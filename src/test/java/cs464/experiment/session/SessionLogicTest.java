package cs464.experiment.session;

import cs464.experiment.model.BlockConfig;
import cs464.experiment.model.DemographicQuestion;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;
import cs464.experiment.model.RatingItem;
import cs464.experiment.model.Schedule;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Counterbalancing, schedule application and validation, without any JSON or UI. */
class SessionLogicTest {

    @Test
    void scheduleParticipantIsPickedFromTheNumberInTheId() {
        Schedule schedule = sampleSchedule();
        assertEquals("P3", schedule.defaultFor("P03").participant());
        assertEquals("P1", schedule.defaultFor("P1").participant());
        assertEquals("P1", schedule.defaultFor("pilot").participant());
        assertEquals("P1", schedule.defaultFor("150").participant());
        assertEquals("P1", schedule.defaultFor(null).participant());
    }

    @Test
    void scheduleSetsOrderAndMakesAiWrongOnlyWhereScheduled() {
        // E3, E1, E2 = detailed, none, brief, which is order 5 in sampleConfig.
        Schedule.Participant p = new Schedule.Participant("P1", List.of("E3", "E1", "E2"), List.of(50, 100, 75, 100),
                List.of(section(1, "E1", 100, true), section(2, "E3", 50, false),
                        section(3, "E1", 100, true), section(4, "E2", 100, true)));
        ExperimentSession session = ExperimentSession.create(sampleConfig(), sampleQuestions(), "P01", p);

        assertEquals(5, session.orderId());
        assertEquals(ExplanationLevel.DETAILED, session.blocks().get(1).explanationLevel());

        Question wrong = session.blocks().get(1).questions().get(0);
        assertEquals(3, wrong.aiAnswerIndex());
        assertEquals("wrong brief", wrong.explanationBrief());
        assertEquals(0, session.blocks().get(1).aiAccuracyPercent());
        assertEquals(100, session.blocks().get(2).aiAccuracyPercent());
    }

    @Test
    void wrongAnswerRequiresAWrongAiAnswerIndex() {
        Question noWrong = question("x", 1, 1, "b", "d");
        assertThrows(IllegalStateException.class, () -> noWrong.withAiCorrect(false));
        assertEquals(1, noWrong.withAiCorrect(true).aiAnswerIndex());
    }

    @Test
    void orderSlotsRotateLevelsButKeepQuestionsInPlace() {
        ExperimentConfig config = sampleConfig();
        Map<String, Question> questions = sampleQuestions();

        ExperimentSession order1 = ExperimentSession.create(config, questions, "P01", 1);
        ExperimentSession order6 = ExperimentSession.create(config, questions, "P06", 6);

        // Baseline is fixed for everyone.
        assertEquals(ExplanationLevel.NONE, order1.blocks().get(0).explanationLevel());
        assertEquals(ExplanationLevel.NONE, order6.blocks().get(0).explanationLevel());

        // Order 1 = none, brief, detailed; order 6 = detailed, brief, none.
        assertEquals(ExplanationLevel.NONE, order1.blocks().get(1).explanationLevel());
        assertEquals(ExplanationLevel.DETAILED, order1.blocks().get(3).explanationLevel());
        assertEquals(ExplanationLevel.DETAILED, order6.blocks().get(1).explanationLevel());
        assertEquals(ExplanationLevel.NONE, order6.blocks().get(3).explanationLevel());

        // Question sets stay attached to their block position.
        assertEquals(order1.blocks().get(1).questions(), order6.blocks().get(1).questions());
    }

    @Test
    void validatorAcceptsSampleConfig() {
        assertEquals(List.of(), ConfigValidator.validate(sampleConfig(), sampleQuestions()));
    }

    @Test
    void validatorReportsMissingQuestionButAllowsEmptyExplanations() {
        Map<String, Question> questions = sampleQuestions();
        questions.put("q2", question("q2", 1, 1, "", ""));   // explanations not written yet
        questions.remove("q3");                               // referenced but missing

        List<String> problems = ConfigValidator.validate(sampleConfig(), questions);

        assertEquals(1, problems.size(), problems.toString());
        assertTrue(problems.get(0).contains("'q3' is not in final_questions.json"), problems.toString());
    }

    @Test
    void aiAccuracyIsComputedFromQuestions() {
        ExperimentSession session = ExperimentSession.create(sampleConfig(), sampleQuestions(), "P01", 1);
        assertEquals(1.0, session.blocks().get(0).aiAccuracy());
        assertEquals(0.0, session.blocks().get(3).aiAccuracy()); // q4 has a wrong AI answer
    }

    // --- tiny in-memory fixtures ------------------------------------------------------------

    @Test
    void missingDemographicQuestionsMeansNoAboutYouScreen() {
        ExperimentConfig config = sampleConfig(null);
        assertEquals(List.of(), config.demographicQuestions());
        assertEquals(List.of(), ConfigValidator.validate(config, sampleQuestions()));
    }

    @Test
    void validatorReportsDuplicateDemographicIdsAndEmptyChoices() {
        List<DemographicQuestion> demographics = List.of(
                new DemographicQuestion("age", "What is your age?", List.of("18 to 20", ""), false),
                new DemographicQuestion("age", "Age again?", null, false));

        List<String> problems = ConfigValidator.validate(sampleConfig(demographics), sampleQuestions());

        assertTrue(problems.stream().anyMatch(p -> p.contains("'age' is missing or duplicated")), problems.toString());
        assertTrue(problems.stream().anyMatch(p -> p.contains("'age' has an empty choice")), problems.toString());
    }

    @Test
    void demographicQuestionWithoutChoicesIsFreeText() {
        assertTrue(new DemographicQuestion("major", "Major?", null, false).isFreeText());
        assertTrue(new DemographicQuestion("major", "Major?", List.of(), false).isFreeText());
        assertFalse(new DemographicQuestion("age", "Age?", List.of("18 to 20"), false).isFreeText());
    }

    private static ExperimentConfig sampleConfig() {
        return sampleConfig(List.of(new DemographicQuestion("age", "What is your age?",
                List.of("18 to 20", "21 or older", "Prefer not to say"), false)));
    }

    private static ExperimentConfig sampleConfig(List<DemographicQuestion> demographics) {
        List<List<String>> orders = List.of(
                List.of("none", "brief", "detailed"),
                List.of("none", "detailed", "brief"),
                List.of("brief", "none", "detailed"),
                List.of("brief", "detailed", "none"),
                List.of("detailed", "none", "brief"),
                List.of("detailed", "brief", "none"));
        List<BlockConfig> blocks = List.of(
                new BlockConfig("baseline", "Baseline", "", "none", null, List.of("q1")),
                new BlockConfig("a", "A", "", null, 0, List.of("q2")),
                new BlockConfig("b", "B", "", null, 1, List.of("q3")),
                new BlockConfig("c", "C", "", null, 2, List.of("q4")));
        List<RatingItem> items = List.of(new RatingItem("conf_ai", "before?", "after?"));
        return new ExperimentConfig("Test", "data", 1, 10, false, items, orders, blocks, demographics);
    }

    private static Map<String, Question> sampleQuestions() {
        Map<String, Question> map = new HashMap<>();
        map.put("q1", question("q1", 0, 0, "", ""));
        map.put("q2", new Question("q2", "math", "easy", "stem q2", List.of("a", "b", "c", "d"),
                1, 1, "brief", "detailed", "test", 3, "wrong brief", "wrong detailed"));
        map.put("q3", question("q3", 2, 2, "brief", "detailed"));
        map.put("q4", question("q4", 0, 3, "brief", "detailed"));
        return map;
    }

    private static Schedule sampleSchedule() {
        List<Schedule.Participant> participants = new java.util.ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            participants.add(new Schedule.Participant("P" + i, List.of("E1", "E2", "E3"), List.of(), List.of()));
        }
        return new Schedule(1, List.of("E1", "E2", "E3"), participants);
    }

    private static Schedule.Section section(int number, String level, int accuracy, boolean aiCorrect) {
        return new Schedule.Section(number, 1, level, accuracy,
                List.of(new Schedule.ScheduledQuestion(1, "q" + number, aiCorrect)));
    }

    private static Question question(String id, int correct, int ai, String brief, String detailed) {
        return new Question(id, "math", "easy", "stem " + id, List.of("a", "b", "c", "d"),
                correct, ai, brief, detailed, "test");
    }
}
