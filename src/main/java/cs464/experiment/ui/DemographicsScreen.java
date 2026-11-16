package cs464.experiment.ui;

import cs464.experiment.model.DemographicQuestion;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The "About you" questions asked once, before the Welcome screen. Every question is optional,
 * so Continue is always enabled; a skipped question is saved as an empty answer.
 * Names are never asked here: they go only on the separate consent form.
 */
public final class DemographicsScreen implements Screen {

    static final String OTHER_PREFIX = "Other: ";

    private final Parent root;

    /** @param onDone receives question id -> answer ("" if skipped), in config order */
    public DemographicsScreen(List<DemographicQuestion> questions, Consumer<Map<String, String>> onDone) {
        List<Node> content = new ArrayList<>();
        content.add(Layout.title("About you"));
        content.add(Layout.body("These questions help us describe the group of people who took part. "
                + "Choose \"Prefer not to say\" or leave a question blank for anything you would rather not answer."));

        Map<String, Supplier<String>> answers = new LinkedHashMap<>();
        for (DemographicQuestion question : questions) {
            VBox row = new VBox(8, Layout.body(question.prompt()));
            row.getStyleClass().add("rating-item");
            if (question.isFreeText()) {
                TextField field = new TextField();
                field.setMaxWidth(420);
                row.getChildren().add(field);
                answers.put(question.id(), () -> field.getText().trim());
            } else {
                answers.put(question.id(), addChoices(question, row));
            }
            content.add(row);
        }

        Button next = new Button("Continue");
        next.getStyleClass().add("primary-button");
        next.setDefaultButton(true);
        next.setOnAction(e -> {
            Map<String, String> values = new LinkedHashMap<>();
            answers.forEach((id, answer) -> values.put(id, answer.get()));
            onDone.accept(values);
        });
        content.add(next);

        this.root = Layout.page(content.toArray(new Node[0]));
    }

    /** Adds one radio button per choice (plus "Other:" with a text box) and returns how to read the answer. */
    private static Supplier<String> addChoices(DemographicQuestion question, VBox row) {
        ToggleGroup group = new ToggleGroup();
        for (String choice : question.choices()) {
            RadioButton button = new RadioButton(choice);
            button.setUserData(choice);
            button.setToggleGroup(group);
            row.getChildren().add(button);
        }
        if (!question.allowOther()) {
            return () -> group.getSelectedToggle() == null ? "" : (String) group.getSelectedToggle().getUserData();
        }

        RadioButton other = new RadioButton("Other:");
        other.setToggleGroup(group);
        TextField otherText = new TextField();
        otherText.setPrefColumnCount(20);
        otherText.textProperty().addListener((obs, old, text) -> {
            if (!text.isBlank()) {
                other.setSelected(true);
            }
        });
        row.getChildren().add(new HBox(8, other, otherText));

        return () -> {
            if (group.getSelectedToggle() == null) {
                return "";
            }
            if (group.getSelectedToggle() == other) {
                return OTHER_PREFIX + otherText.getText().trim();
            }
            return (String) group.getSelectedToggle().getUserData();
        };
    }

    @Override
    public Parent root() {
        return root;
    }
}
