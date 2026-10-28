package cs464.experiment;

import cs464.experiment.io.JsonLoader;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.Question;
import cs464.experiment.session.ConfigValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void baselineBlockAiIsAlwaysCorrect() throws Exception {
        ExperimentConfig config = JsonLoader.loadConfig(JsonLoader.CONFIG_RESOURCE);
        Map<String, Question> questions = JsonLoader.loadQuestions(JsonLoader.QUESTIONS_RESOURCE);

        // The proposal requires the first (baseline) block to have a 100% accurate AI.
        for (String id : config.blocks().get(0).questionIds()) {
            assertTrue(questions.get(id).isAiCorrect(), "Baseline question " + id + " has a wrong AI answer");
        }
    }

    @Test
    void snakeCaseJsonMapsOntoRecordFields() throws Exception {
        Map<String, Question> questions = JsonLoader.loadQuestions(JsonLoader.QUESTIONS_RESOURCE);
        Question q = questions.get("set1-02");

        assertEquals(3, q.correctIndex());
        assertEquals(0, q.aiAnswerIndex());
        assertTrue(q.explanationDetailed().startsWith("Step 1"));
    }
}
