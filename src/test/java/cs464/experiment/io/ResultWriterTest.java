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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesOneRowPerTrialAndRating() throws Exception {
        // Correct answer is D, AI suggests A, participant follows the AI.
        Question q = new Question("q1", "math", "easy", "stem", List.of("a", "b", "c", "d"),
                3, 0, "brief", "detailed", "test");
        Instant t = Instant.parse("2026-11-01T12:00:00Z");

        Path trials;
        Path ratings;
        try (ResultWriter writer = new ResultWriter(tempDir, "P01")) {
            writer.writeTrial(new TrialResult("P01", 2, 1, "round_a", ExplanationLevel.BRIEF, 0, q, 0, 4321, t));
            writer.writeRating(new RatingResult("P01", 2, 1, "round_a", ExplanationLevel.BRIEF,
                    RatingPhase.PRE, "conf_ai", 7, t));
            trials = writer.trialsFile();
            ratings = writer.ratingsFile();
        }

        List<String> trialLines = Files.readAllLines(trials);
        assertEquals(String.join(",", ResultWriter.TRIAL_COLUMNS), trialLines.get(0));
        assertEquals("P01,2,1,round_a,brief,0,q1,math,easy,A,false,A,D,false,true,4321,2026-11-01T12:00:00Z",
                trialLines.get(1));

        List<String> ratingLines = Files.readAllLines(ratings);
        assertEquals(String.join(",", ResultWriter.RATING_COLUMNS), ratingLines.get(0));
        assertEquals("P01,2,1,round_a,brief,pre,conf_ai,7,2026-11-01T12:00:00Z", ratingLines.get(1));
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
