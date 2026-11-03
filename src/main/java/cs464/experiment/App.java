package cs464.experiment;

import cs464.experiment.io.JsonLoader;
import cs464.experiment.io.ResultWriter;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.Question;
import cs464.experiment.session.ConfigValidator;
import cs464.experiment.session.ExperimentSession;
import cs464.experiment.session.PlannedBlock;
import cs464.experiment.ui.ExperimentFlow;
import cs464.experiment.ui.Navigator;
import cs464.experiment.ui.SetupScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Entry point. Loads and checks the JSON files, shows the experimenter setup screen,
 * then hands control to {@link ExperimentFlow} for the participant's session.
 */
public class App extends Application {

    private ResultWriter writer;

    @Override
    public void start(Stage stage) {
        ExperimentConfig config;
        Map<String, Question> questions;
        try {
            config = JsonLoader.loadConfig(JsonLoader.CONFIG_RESOURCE);
            questions = JsonLoader.loadQuestions(JsonLoader.QUESTIONS_RESOURCE);
        } catch (IOException e) {
            fatal(List.of(e.getMessage()));
            return;
        }
        List<String> problems = ConfigValidator.validate(config, questions);
        if (!problems.isEmpty()) {
            fatal(problems);
            return;
        }

        stage.setTitle(config.title());
        Navigator nav = new Navigator(stage);
        nav.show(new SetupScreen(config, (participantId, orderId) -> {
            ExperimentSession session = ExperimentSession.create(config, questions, participantId, orderId);
            printPlan(session);
            try {
                writer = new ResultWriter(Path.of(config.outputDir()), participantId);
            } catch (IOException e) {
                fatal(List.of("Could not create output files: " + e.getMessage()));
                return;
            }
            new ExperimentFlow(nav, config, session, writer).start();
        }));
        stage.show();
    }

    @Override
    public void stop() throws IOException {
        // Also runs if the window is closed mid-session; rows already written are kept.
        if (writer != null) {
            writer.close();
        }
    }

    /** Console summary for the experimenter, useful for checking counterbalancing. */
    private static void printPlan(ExperimentSession session) {
        System.out.println("Participant " + session.participantId() + ", order " + session.orderId());
        for (PlannedBlock block : session.blocks()) {
            System.out.printf("  block %d %-10s explanation=%-8s questions=%d ai_accuracy=%.0f%%%n",
                    block.index(), block.id(), block.explanationLevel().configName(),
                    block.questions().size(), block.aiAccuracy() * 100);
        }
    }

    private static void fatal(List<String> problems) {
        String text = String.join("\n", problems);
        System.err.println("Cannot start the experiment:\n" + text);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Cannot start the experiment. Fix these problems in the JSON files:");
        TextArea details = new TextArea(text);
        details.setEditable(false);
        details.setWrapText(true);
        alert.getDialogPane().setContent(details);
        alert.showAndWait();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
