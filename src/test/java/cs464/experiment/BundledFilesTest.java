package cs464.experiment;

import cs464.experiment.io.JsonLoader;
import cs464.experiment.model.DemographicQuestion;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.Question;
import cs464.experiment.session.ConfigValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the JSON files we ship. If someone edits questions.json or experiment.json
 * and breaks a reference, `mvn test` fails with the validator's message.
 */
class BundledFilesTest {

    @Test
    void bundledConfigAndQuestionsAreValid() throws Exception {
        ExperimentConfig config = JsonLoader.loadConfig(JsonLoader.CONFIG_RESOURCE);
        Map<String, Question> questions = JsonLoader.loadQuestions(JsonLoader.QUESTIONS_RESOURCE);

        List<String> problems = ConfigValidator.validate(config, questions);

        assertEquals(List.of(), problems);
    }

    @Test
    void everySectionHasFourQuestionsTwoMathAndTwoEnglish() throws Exception {
        ExperimentConfig config = JsonLoader.loadConfig(JsonLoader.CONFIG_RESOURCE);
        Map<String, Question> questions = JsonLoader.loadQuestions(JsonLoader.QUESTIONS_RESOURCE);

        assertEquals(9, config.blocks().size());
        for (var block : config.blocks()) {
            assertEquals(4, block.questionIds().size(), block.id());
            assertEquals(2, block.questionIds().stream()
                    .filter(id -> "math".equals(questions.get(id).section())).count(), block.id() + " math");
            assertEquals(2, block.questionIds().stream()
                    .filter(id -> "reading_writing".equals(questions.get(id).section())).count(), block.id() + " english");
        }
    }

    @Test
    void bundledDemographicsAreOptionalAndNeverAskForAName() throws Exception {
        ExperimentConfig config = JsonLoader.loadConfig(JsonLoader.CONFIG_RESOURCE);

        assertFalse(config.demographicQuestions().isEmpty(), "demographic_questions is missing from experiment.json");
        for (DemographicQuestion q : config.demographicQuestions()) {
            // Names go only on the separate Google consent form, never into the study data.
            assertFalse(q.prompt().toLowerCase().contains("name"), "Don't ask for names in the app: " + q.id());
            if (!q.isFreeText()) {
                assertTrue(q.choices().contains("Prefer not to say") || q.choices().contains("Not sure"),
                        "Choice question " + q.id() + " needs a way to opt out");
            }
        }
        assertTrue(config.demographicQuestions().stream().anyMatch(q -> q.id().equals("gender") && q.allowOther()),
                "allow_other should map from snake_case JSON");
    }

    @Test
    void snakeCaseJsonMapsOntoRecordFields() throws Exception {
        Map<String, Question> questions = JsonLoader.loadQuestions(JsonLoader.QUESTIONS_RESOURCE);
        Question q = questions.get("3d1070c9");

        assertEquals(2, q.correctIndex());
        assertEquals(2, q.aiAnswerIndex());
        assertEquals("", q.explanationDetailed());
    }
}
