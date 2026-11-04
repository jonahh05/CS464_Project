package cs464.experiment;

import cs464.experiment.io.JsonLoader;
import cs464.experiment.model.BlockConfig;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.HelperStyle;
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
    void baselineBlockAiIsAlwaysCorrect() throws Exception {
        ExperimentConfig config = JsonLoader.loadConfig(JsonLoader.CONFIG_RESOURCE);
        Map<String, Question> questions = JsonLoader.loadQuestions(JsonLoader.QUESTIONS_RESOURCE);

        // The proposal requires the first (baseline) block to have a 100% accurate AI.
        BlockConfig baseline = config.blocks().get(0);
        if (baseline.drawsSuggestions()) {
            assertEquals(100.0, baseline.aiAccuracyPercent(), "Baseline ai_accuracy_percent must be 100");
        } else {
            for (String id : baseline.questionIds()) {
                assertTrue(questions.get(id).isAiCorrect(), "Baseline question " + id + " has a wrong AI answer");
            }
        }
    }

    @Test
    void snakeCaseJsonMapsOntoRecordFields() throws Exception {
        Map<String, Question> questions = JsonLoader.loadQuestions(JsonLoader.QUESTIONS_RESOURCE);
        Question q = questions.get("set1-02");

        assertEquals(1, q.correctIndex());
        assertEquals("reading_writing", q.section());
        assertEquals(4, q.explanationsByChoice().size());
        assertTrue(q.explanationFor(0, ExplanationLevel.DETAILED).contains("contrast"));

        ExperimentConfig config = JsonLoader.loadConfig(JsonLoader.CONFIG_RESOURCE);
        assertEquals(HelperStyle.SIDE_CARD, config.helper());
        assertFalse(config.showUseAnswerButton());
    }

    @Test
    void bundledRoundsUseFixedSuggestionsForEveryone() throws Exception {
        // Team decision: the AI is scripted, so every participant sees identical suggestions.
        // That means no block may draw suggestions per participant (ai_accuracy_percent / ai_wrong_count).
        ExperimentConfig config = JsonLoader.loadConfig(JsonLoader.CONFIG_RESOURCE);
        for (BlockConfig block : config.blocks()) {
            assertFalse(block.drawsSuggestions(), "Block " + block.id() + " draws suggestions per participant");
        }
    }
}
