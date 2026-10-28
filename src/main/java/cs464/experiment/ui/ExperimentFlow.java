package cs464.experiment.ui;

import cs464.experiment.io.ResultWriter;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.Question;
import cs464.experiment.model.RatingPhase;
import cs464.experiment.model.RatingResult;
import cs464.experiment.model.TrialResult;
import cs464.experiment.session.ExperimentSession;
import cs464.experiment.session.PlannedBlock;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * The order of screens for one participant:
 *
 * <pre>
 * welcome
 * for each block:
 *     round intro -> pre-round ratings -> each question -> post-round ratings
 * thank-you
 * </pre>
 *
 * Each step shows a screen and passes a callback that moves to the next step.
 * To add a step (e.g. a practice question or a break), add a method and call it from here.
 */
public final class ExperimentFlow {

    private final Navigator nav;
    private final ExperimentConfig config;
    private final ExperimentSession session;
    private final ResultWriter writer;

    public ExperimentFlow(Navigator nav, ExperimentConfig config, ExperimentSession session, ResultWriter writer) {
        this.nav = nav;
        this.config = config;
        this.session = session;
        this.writer = writer;
    }

    public void start() {
        nav.show(new MessageScreen(
                "Welcome",
                "In this study you will answer several rounds of SAT-style multiple-choice questions. "
                        + "An AI helper will suggest an answer for each question. You may use or ignore it.\n\n"
                        + "Before and after each round you will answer a few quick ratings. "
                        + "Please work at your normal pace.",
                "Begin",
                () -> showBlockIntro(0)));
    }

    private void showBlockIntro(int blockIndex) {
        PlannedBlock block = session.blocks().get(blockIndex);
        String heading = "Round " + (blockIndex + 1) + " of " + session.blocks().size();
        String text = block.instructions() == null ? "" : block.instructions();
        nav.show(new MessageScreen(heading, text, "Continue", () -> showRatings(blockIndex, RatingPhase.PRE)));
    }

    private void showRatings(int blockIndex, RatingPhase phase) {
        PlannedBlock block = session.blocks().get(blockIndex);
        String heading = phase == RatingPhase.PRE
                ? "Before round " + (blockIndex + 1)
                : "After round " + (blockIndex + 1);
        nav.show(new RatingScreen(heading, config.ratingItems(), phase, config.ratingMin(), config.ratingMax(),
                values -> {
                    saveRatings(block, phase, values);
                    if (phase == RatingPhase.PRE) {
                        showQuestion(blockIndex, 0);
                    } else {
                        afterBlock(blockIndex);
                    }
                }));
    }

    private void showQuestion(int blockIndex, int trialIndex) {
        PlannedBlock block = session.blocks().get(blockIndex);
        Question question = block.questions().get(trialIndex);
        String progress = "Round " + (blockIndex + 1) + " • Question " + (trialIndex + 1)
                + " of " + block.questions().size();

        nav.show(new QuestionScreen(progress, question, block.explanationLevel(), (chosen, ms) -> {
            saveTrial(new TrialResult(session.participantId(), session.orderId(), block.index(), block.id(),
                    block.explanationLevel(), trialIndex, question, chosen, ms, Instant.now()));
            if (trialIndex + 1 < block.questions().size()) {
                showQuestion(blockIndex, trialIndex + 1);
            } else {
                showRatings(blockIndex, RatingPhase.POST);
            }
        }));
    }

    private void afterBlock(int blockIndex) {
        if (blockIndex + 1 < session.blocks().size()) {
            showBlockIntro(blockIndex + 1);
        } else {
            finish();
        }
    }

    private void finish() {
        closeWriter();
        System.out.println("Session complete. Data written to:\n  " + writer.trialsFile() + "\n  " + writer.ratingsFile());
        nav.show(new MessageScreen(
                "Thank you!",
                "You have finished the study. Your participant ID is " + session.participantId()
                        + ".\n\nPlease let the experimenter know you are done.",
                "Close",
                Platform::exit));
    }

    // --- saving ---------------------------------------------------------------------------

    private void saveTrial(TrialResult result) {
        try {
            writer.writeTrial(result);
        } catch (IOException e) {
            showSaveError(e);
        }
    }

    private void saveRatings(PlannedBlock block, RatingPhase phase, Map<String, Integer> values) {
        Instant now = Instant.now();
        try {
            for (Map.Entry<String, Integer> entry : values.entrySet()) {
                writer.writeRating(new RatingResult(session.participantId(), session.orderId(), block.index(),
                        block.id(), block.explanationLevel(), phase, entry.getKey(), entry.getValue(), now));
            }
        } catch (IOException e) {
            showSaveError(e);
        }
    }

    private void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            showSaveError(e);
        }
    }

    private static void showSaveError(IOException e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Could not save results: " + e.getMessage() + "\n\nPlease get the experimenter.");
        alert.setHeaderText("Save error");
        alert.showAndWait();
    }
}
