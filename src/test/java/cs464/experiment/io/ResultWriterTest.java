package cs464.experiment.io;

import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;
import cs464.experiment.model.RatingPhase;
import cs464.experiment.model.RatingResult;
import cs464.experiment.model.TrialResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesOneRowPerTrialAndRating() throws Exception {
        // Correct answer is D, AI suggests A, participant starts with B then switches to the AI's A.
        Question q = new Question("q1", "math", "easy", "stem", List.of("a", "b", "c", "d"),
                3, 0, "brief", "detailed", "test");
        Instant t = Instant.parse("2026-11-01T12:00:00Z");

        Path trials;
        try (ResultWriter writer = new ResultWriter(tempDir, "P01")) {
            writer.writeTrial(new TrialResult("P01", 2, 1, "round_a", ExplanationLevel.BRIEF, 75, 0, q, 1, 0, 4321, 9876, t));
            writer.writeRating(new RatingResult("P01", 2, 1, "round_a", ExplanationLevel.BRIEF, 75,
                    RatingPhase.PRE, "conf_ai", 7, t));
            trials = writer.trialsFile();
        }

        List<String> trialLines = Files.readAllLines(trials);
        assertEquals(String.join(",", ResultWriter.TRIAL_COLUMNS), trialLines.get(0));
        assertEquals("P01,2,1,round_a,brief,75,0,q1,math,easy,A,false,B,A,D,false,true,true,4321,9876,2026-11-01T12:00:00Z",
                trialLines.get(1));
                assertTrue(trialLines.get(0).contains("original_answer,final_answer"));
                assertFalse(trialLines.get(0).contains("chosen_answer"));
                assertFalse(trialLines.get(0).contains(",response_time_ms"));
            }

            @Test
            void switchedAnswerIsFalseWhenOriginalEqualsFinal() throws Exception {
                Question q = new Question("q1", "math", "easy", "stem", List.of("a", "b", "c", "d"),
                        3, 0, "brief", "detailed", "test");
                Instant t = Instant.parse("2026-11-01T12:00:00Z");
                Path trials;
                try (ResultWriter writer = new ResultWriter(tempDir, "P03")) {
                    writer.writeTrial(new TrialResult("P03", 1, 0, "round_a", ExplanationLevel.NONE, 100, 0, q, 2, 2, 100, 200, t));
                    trials = writer.trialsFile();
                }
                assertEquals("P03,1,0,round_a,none,100,0,q1,math,easy,A,false,C,C,D,false,false,false,100,200,2026-11-01T12:00:00Z",
                        Files.readAllLines(trials).get(1));
            }

    @Test
    void writesOneRowPerDemographicAnswer() throws Exception {
        Instant t = Instant.parse("2026-11-01T12:00:00Z");
        Map<String, String> answers = new LinkedHashMap<>();
        answers.put("age", "21 to 24");
        answers.put("education", "College graduate, not currently a student");
        answers.put("major", "");

        Path demographics;
        try (ResultWriter writer = new ResultWriter(tempDir, "P02")) {
            writer.writeDemographics("P02", 2, answers, t);
            demographics = writer.demographicsFile();
        }

        List<String> lines = Files.readAllLines(demographics);
        assertEquals(String.join(",", ResultWriter.DEMOGRAPHIC_COLUMNS), lines.get(0));
        assertEquals("P02,2,age,21 to 24,2026-11-01T12:00:00Z", lines.get(1));
        assertEquals("P02,2,education,\"College graduate, not currently a student\",2026-11-01T12:00:00Z", lines.get(2));
        assertEquals("P02,2,major,,2026-11-01T12:00:00Z", lines.get(3));
        assertTrue(demographics.getFileName().toString().endsWith("_demographics.csv"));
    }

    @Test
    void escapesCommasQuotesAndNewlines() {
        assertEquals("plain", ResultWriter.escape("plain"));
        assertEquals("\"a,b\"", ResultWriter.escape("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", ResultWriter.escape("say \"hi\""));
        assertEquals("\"two\nlines\"", ResultWriter.escape("two\nlines"));
        assertEquals("", ResultWriter.escape(null));
    }
}
