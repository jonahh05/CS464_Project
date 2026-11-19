package cs464.experiment;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cs464.experiment.io.JsonLoader;
import cs464.experiment.model.Question;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks wrong_answers.json: one common error answer per question in final_questions.json,
 * with a brief and a three step detailed explanation, like the correct answer explanations.
 * The app doesn't read this file yet (see issue #65).
 */
class WrongAnswersFileTest {

    record WrongAnswer(String id, int wrongAnswerIndex, String wrongAnswer, String commonError,
                       String explanationBrief, String explanationDetailed) {
    }

    private static List<WrongAnswer> load() throws Exception {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(
                WrongAnswersFileTest.class.getResourceAsStream("/questions/wrong_answers.json")), StandardCharsets.UTF_8)) {
            return Arrays.asList(gson.fromJson(reader, WrongAnswer[].class));
        }
    }

    @Test
    void everyQuestionHasOneCommonErrorAnswer() throws Exception {
        Map<String, Question> questions = JsonLoader.loadQuestions(JsonLoader.QUESTIONS_RESOURCE);
        List<WrongAnswer> wrong = load();

        Set<String> ids = new HashSet<>();
        for (WrongAnswer w : wrong) {
            assertTrue(ids.add(w.id()), "Duplicate entry for " + w.id());
            Question q = questions.get(w.id());
            assertNotNull(q, w.id() + " is not in final_questions.json");
            assertTrue(w.wrongAnswerIndex() >= 0 && w.wrongAnswerIndex() < q.choices().size(), "Index out of range: " + w.id());
            assertNotEquals(q.correctIndex(), w.wrongAnswerIndex(), "Wrong answer is the correct answer: " + w.id());
            assertEquals(String.valueOf(q.choices().get(w.wrongAnswerIndex())), w.wrongAnswer(),
                    "wrong_answer text doesn't match the choice for " + w.id());
            assertFalse(w.commonError().isBlank(), "Missing common_error note: " + w.id());
        }
        assertEquals(questions.keySet(), ids, "wrong_answers.json should cover every question");
    }

    @Test
    void wrongAnswerExplanationsFollowTheSameStyle() throws Exception {
        for (WrongAnswer w : load()) {
            assertFalse(w.explanationBrief().isBlank(), "Missing explanation_brief: " + w.id());
            String[] steps = w.explanationDetailed().split("\n");
            assertEquals(3, steps.length, "explanation_detailed should have exactly 3 steps, one per line: " + w.id());
            for (int i = 0; i < steps.length; i++) {
                assertTrue(steps[i].startsWith("Step " + (i + 1) + ": "), "Step " + (i + 1) + " is mislabeled in " + w.id());
            }
        }
    }
}
