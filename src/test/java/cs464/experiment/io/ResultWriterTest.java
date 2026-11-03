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
        // Original 17 columns unchanged; the 4 appended columns are empty/default in fixed mode.
        assertEquals("P01,2,1,round_a,brief,0,q1,math,easy,A,false,A,D,false,true,4321,2026-11-01T12:00:00Z"
                        + ",,0,false,false",
                trialLines.get(1));

        List<String> ratingLines = Files.readAllLines(ratings);
        assertEquals(String.join(",", ResultWriter.RATING_COLUMNS), ratingLines.get(0));
        assertEquals("P01,2,1,round_a,brief,pre,conf_ai,7,2026-11-01T12:00:00Z", ratingLines.get(1));
    }

    @Test
    void drawnSuggestionAndTimeoutAreWritten() throws Exception {
        // Stored AI answer is A, but this participant's draw suggested C. Correct is D.
        Question q = new Question("q1", "math", "easy", "stem", List.of("a", "b", "c", "d"),
                3, 0, "brief", "detailed", "test");
        Instant t = Instant.parse("2026-11-01T12:00:00Z");
        Path trials;
        try (ResultWriter writer = new ResultWriter(tempDir, "P02")) {
            // Followed the drawn suggestion via the button.
            writer.writeTrial(new TrialResult("P02", 2, 1, "round_a", ExplanationLevel.DETAILED, 0, q,
                    2, 80.0, 2, 5000, 900, false, true, t));
            // Time ran out with nothing selected.
            writer.writeTrial(new TrialResult("P02", 2, 1, "round_a", ExplanationLevel.DETAILED, 1, q,
                    3, 80.0, -1, 60000, 900, true, false, t));
            trials = writer.trialsFile();
        }
        List<String> lines = Files.readAllLines(trials);
        assertEquals("P02,2,1,round_a,detailed,0,q1,math,easy,C,false,C,D,false,true,5000,2026-11-01T12:00:00Z"
                + ",80,900,false,true", lines.get(1));
        assertEquals("P02,2,1,round_a,detailed,1,q1,math,easy,D,true,,D,false,false,60000,2026-11-01T12:00:00Z"
                + ",80,900,true,false", lines.get(2));
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
