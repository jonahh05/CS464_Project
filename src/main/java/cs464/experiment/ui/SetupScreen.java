package cs464.experiment.ui;

import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.session.OrderAssigner;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.function.BiConsumer;

/**
 * Filled in by the experimenter before handing the laptop over: participant ID and
 * counterbalancing order. The order is pre-selected from the ID but can be changed.
 */
public final class SetupScreen implements Screen {

    private final Parent root;

    /** @param onStart receives (participantId, 1-based orderId) */
    public SetupScreen(ExperimentConfig config, BiConsumer<String, Integer> onStart) {
        int orderCount = config.orders().size();

        TextField idField = new TextField();
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

        this.root = Layout.page(
                Layout.title(config.title()),
                Layout.muted("Experimenter setup — complete before the participant sits down."),
                new HBox(12, new Label("Participant ID"), idField),
                new HBox(12, new Label("Counterbalancing order"), orderBox),
                orderDescription,
                start);
    }

    @Override
    public Parent root() {
        return root;
    }
}
