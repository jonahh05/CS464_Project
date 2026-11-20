package cs464.experiment.ui;

import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.Schedule;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.util.function.BiConsumer;

/**
 * Filled in by the experimenter before handing the laptop over: participant ID and which of the
 * six scheduled participants this is. The schedule participant is pre-selected from the number in
 * the ID (P03 -> P3) and decides the explanation order and the AI accuracy of each section.
 */
public final class SetupScreen implements Screen {

    private final Parent root;

    /** @param onStart receives (participantId, the schedule row to follow) */
    public SetupScreen(ExperimentConfig config, Schedule schedule,
                       BiConsumer<String, Schedule.Participant> onStart) {
        TextField idField = new TextField();
        idField.setPromptText("e.g. P01");

        ComboBox<Schedule.Participant> scheduleBox = new ComboBox<>();
        scheduleBox.getItems().addAll(schedule.participants());
        scheduleBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Schedule.Participant p) {
                return p == null ? "" : p.participant();
            }

            @Override
            public Schedule.Participant fromString(String text) {
                return null;
            }
        });
        scheduleBox.setValue(schedule.participants().get(0));

        Label scheduleDescription = Layout.muted("");
        Runnable describe = () -> {
            Schedule.Participant p = scheduleBox.getValue();
            scheduleDescription.setText("Explanation order: "
                    + String.join(" → ", p.explanationOrder().stream().map(l -> l.configName()).toList())
                    + "   |   AI accuracy by section: " + p.accuracyBySection());
        };
        describe.run();

        idField.textProperty().addListener((obs, old, id) -> scheduleBox.setValue(schedule.defaultFor(id)));
        scheduleBox.valueProperty().addListener((obs, old, value) -> describe.run());

        Button start = new Button("Start session");
        start.getStyleClass().add("primary-button");
        start.setDefaultButton(true);
        start.disableProperty().bind(Bindings.createBooleanBinding(
                () -> idField.getText().trim().isEmpty(), idField.textProperty()));
        start.setOnAction(e -> onStart.accept(idField.getText().trim(), scheduleBox.getValue()));

        this.root = Layout.page(
                Layout.title(config.title()),
                Layout.muted("Experimenter setup — complete before the participant sits down."),
                new HBox(12, new Label("Participant ID"), idField),
                new HBox(12, new Label("Schedule participant"), scheduleBox),
                scheduleDescription,
                start);
    }

    @Override
    public Parent root() {
        return root;
    }
}
