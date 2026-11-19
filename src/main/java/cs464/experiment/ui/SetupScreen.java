package cs464.experiment.ui;

import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.session.OrderAssigner;
import cs464.experiment.session.ParticipantRegistry;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Filled in by the experimenter before handing the laptop over: participant ID and
 * counterbalancing order. The ID is filled in with the next unused number on this laptop and the
 * order is pre-selected from it; both can still be changed.
 *
 * <p>The status line at the top says whether this laptop is still testing or running the pilot study.
 * The reset button moves the saved data into data/archive and starts numbering again.
 */
public final class SetupScreen implements Screen {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final Parent root;
    private final ParticipantRegistry registry;
    private final Label status = new Label();
    private final TextField idField = new TextField();
    private final Button reset = new Button();

    /** @param onStart receives (participantId, 1-based orderId) */
    public SetupScreen(ExperimentConfig config, ParticipantRegistry registry, BiConsumer<String, Integer> onStart) {
        this.registry = registry;
        int orderCount = config.orders().size();

        status.setWrapText(true);
        status.getStyleClass().add("status-banner");

        idField.setPromptText("e.g. P01");

        ComboBox<Integer> orderBox = new ComboBox<>();
        for (int i = 1; i <= orderCount; i++) {
            orderBox.getItems().add(i);
        }
        orderBox.setValue(1);

        Label orderDescription = Layout.muted("");
        Runnable describeOrder = () -> orderDescription.setText(
                "Order " + orderBox.getValue() + ": " + String.join(" → ", config.orders().get(orderBox.getValue() - 1)));
        describeOrder.run();

        idField.textProperty().addListener((obs, old, id) ->
                orderBox.setValue(OrderAssigner.defaultOrderFor(id, orderCount)));
        orderBox.valueProperty().addListener((obs, old, value) -> describeOrder.run());

        Button start = new Button("Start session");
        start.getStyleClass().add("primary-button");
        start.setDefaultButton(true);
        start.disableProperty().bind(Bindings.createBooleanBinding(
                () -> idField.getText().trim().isEmpty(), idField.textProperty()));
        start.setOnAction(e -> onStart.accept(idField.getText().trim(), orderBox.getValue()));

        reset.setOnAction(e -> confirmReset());

        refresh();

        this.root = Layout.page(
                status,
                Layout.title(config.title()),
                Layout.muted("Experimenter setup: complete before the participant sits down."),
                new HBox(12, new Label("Participant ID"), idField),
                Layout.muted("Filled in with the next participant number on this laptop. You can change it."),
                new HBox(12, new Label("Counterbalancing order"), orderBox),
                orderDescription,
                start,
                new VBox(6, Layout.muted("Before the first real participant:"), reset));
    }

    /** Updates the status line, the button label and the suggested participant ID. */
    private void refresh() {
        try {
            ParticipantRegistry.Status s = registry.status();
            int saved = registry.savedSessions();
            String next = registry.nextId();
            if (s.pilot()) {
                String since = s.pilotStarted() == null ? "" : " since " + s.pilotStarted().format(DAY);
                status.setText("Status: PILOT STUDY" + since + ". " + saved + " participant"
                        + (saved == 1 ? "" : "s") + " saved on this laptop. Next: " + next + ".");
                status.getStyleClass().setAll("status-banner", "status-pilot");
                reset.setText("Reset participant numbers and data");
            } else {
                status.setText("Status: TESTING. Sessions saved now are practice runs (" + saved
                        + " saved). Press \"Start pilot study\" before the first real participant.");
                status.getStyleClass().setAll("status-banner", "status-testing");
                reset.setText("Start pilot study (reset participant numbers and data)");
            }
            idField.setText(next);
        } catch (IOException | RuntimeException e) {
            status.setText("Status: could not read the data folder (" + e.getMessage() + ").");
            status.getStyleClass().setAll("status-banner", "status-testing");
            reset.setText("Start pilot study (reset participant numbers and data)");
        }
    }

    private void confirmReset() {
        Spinner<Integer> firstNumber = new Spinner<>(1, 999, 1);
        firstNumber.setEditable(true);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Start pilot study");
        confirm.setHeaderText("Reset participant numbers and data on this laptop?");
        Label explain = new Label("Every saved session in the data folder will be moved to data/"
                + ParticipantRegistry.ARCHIVE_DIR + "/ (nothing is deleted), and numbering starts again "
                + "from the number below.\n\nIf teammates run sessions on their own laptops, give each "
                + "laptop a different first number so no two participants get the same ID.");
        explain.setWrapText(true);
        confirm.getDialogPane().setContent(new VBox(12, explain,
                new HBox(12, new Label("First participant number on this laptop"), firstNumber)));
        confirm.getDialogPane().setPrefWidth(560);

        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }
        try {
            Path archive = registry.startPilot(firstNumber.getValue());
            refresh();
            if (archive != null) {
                Alert done = new Alert(Alert.AlertType.INFORMATION,
                        "The earlier sessions were moved to:\n" + archive.toAbsolutePath());
                done.setHeaderText("Pilot study started");
                done.showAndWait();
            }
        } catch (IOException | RuntimeException e) {
            Alert error = new Alert(Alert.AlertType.ERROR, "Could not reset the data folder: " + e.getMessage());
            error.setHeaderText("Reset failed");
            error.showAndWait();
        }
    }

    @Override
    public Parent root() {
        return root;
    }
}
