package cs464.experiment.io;

import cs464.experiment.model.Question;
import cs464.experiment.model.RatingResult;
import cs464.experiment.model.TrialResult;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Writes a participant's results to two CSV files in the output directory:
 * {@code <id>_<timestamp>_trials.csv} and {@code <id>_<timestamp>_ratings.csv}.
 *
 * <p>Every row is flushed as soon as it is written, so a crash loses at most the
 * current question. The timestamp in the file name means a re-run never overwrites data.
 * Column meanings are documented in docs/data-dictionary.md; change both together.
 */
public final class ResultWriter implements Closeable {

    public static final List<String> TRIAL_COLUMNS = List.of(
            "participant_id", "order_id", "block_index", "block_id", "explanation_level",
            "trial_index", "question_id", "section", "difficulty",
            "ai_suggestion", "ai_correct", "chosen_answer", "correct_answer",
            "correct", "followed_ai", "response_time_ms", "timestamp");

    public static final List<String> RATING_COLUMNS = List.of(
            "participant_id", "order_id", "block_index", "block_id", "explanation_level",
            "phase", "item_id", "value", "timestamp");

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Path trialsFile;
    private final Path ratingsFile;
    private final BufferedWriter trials;
    private final BufferedWriter ratings;

    public ResultWriter(Path outputDir, String participantId) throws IOException {
        Files.createDirectories(outputDir);
        String prefix = safeFileName(participantId) + "_" + LocalDateTime.now().format(FILE_STAMP);
        this.trialsFile = outputDir.resolve(prefix + "_trials.csv");
        this.ratingsFile = outputDir.resolve(prefix + "_ratings.csv");
        this.trials = Files.newBufferedWriter(trialsFile, StandardCharsets.UTF_8);
        this.ratings = Files.newBufferedWriter(ratingsFile, StandardCharsets.UTF_8);
        writeRow(trials, TRIAL_COLUMNS);
        writeRow(ratings, RATING_COLUMNS);
    }

    public void writeTrial(TrialResult r) throws IOException {
        Question q = r.question();
        writeRow(trials, List.of(
                r.participantId(),
                String.valueOf(r.orderId()),
                String.valueOf(r.blockIndex()),
                r.blockId(),
                r.explanationLevel().configName(),
                String.valueOf(r.trialIndex()),
                q.id(),
                nullToEmpty(q.section()),
                nullToEmpty(q.difficulty()),
                Question.letter(q.aiAnswerIndex()),
                String.valueOf(q.isAiCorrect()),
                Question.letter(r.chosenIndex()),
                Question.letter(q.correctIndex()),
                String.valueOf(r.isCorrect()),
                String.valueOf(r.followedAi()),
                String.valueOf(r.responseTimeMs()),
                r.answeredAt().toString()));
    }

    public void writeRating(RatingResult r) throws IOException {
        writeRow(ratings, List.of(
                r.participantId(),
                String.valueOf(r.orderId()),
                String.valueOf(r.blockIndex()),
                r.blockId(),
                r.explanationLevel().configName(),
                r.phase().configName(),
                r.itemId(),
                String.valueOf(r.value()),
                r.answeredAt().toString()));
    }

    public Path trialsFile() {
        return trialsFile;
    }

    public Path ratingsFile() {
        return ratingsFile;
    }

    @Override
    public void close() throws IOException {
        try {
            trials.close();
        } finally {
            ratings.close();
        }
    }

    private static void writeRow(BufferedWriter out, List<String> values) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escape(values.get(i)));
        }
        line.append('\n'); // same line ending on every OS
        out.write(line.toString());
        out.flush();
    }

    /** Quotes a CSV field if it contains a comma, quote or newline (RFC 4180 style). */
    static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String safeFileName(String participantId) {
        return participantId.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
