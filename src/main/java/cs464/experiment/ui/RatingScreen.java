package cs464.experiment.ui;

import cs464.experiment.model.RatingItem;
import cs464.experiment.model.RatingPhase;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The 1-10 ratings asked before and after each round. Buttons start unselected
 * (a slider would nudge everyone toward its default), and Continue stays disabled
 * until every item has an answer.
 */
public final class RatingScreen implements Screen {

    private final Parent root;

    /** @param onDone receives item id -> chosen value, in config order */
    public RatingScreen(String heading, List<RatingItem> items, RatingPhase phase,
                        int min, int max, Consumer<Map<String, Integer>> onDone) {
        List<Node> content = new ArrayList<>();
        content.add(Layout.title(heading));
        content.add(Layout.body("Rate each statement from " + min + " (lowest) to " + max + " (highest)."));

        Map<String, ToggleGroup> groups = new LinkedHashMap<>();
        for (RatingItem item : items) {
            ToggleGroup group = new ToggleGroup();
            HBox scale = new HBox(6);
            for (int value = min; value <= max; value++) {
                ToggleButton button = new ToggleButton(String.valueOf(value));
                button.setUserData(value);
                button.setToggleGroup(group);
                button.getStyleClass().add("rating-button");
                scale.getChildren().add(button);
            }
            groups.put(item.id(), group);

            Label prompt = Layout.body(item.promptFor(phase));
            VBox row = new VBox(8, prompt, scale);
            row.getStyleClass().add("rating-item");
            content.add(row);
        }

        Button next = new Button("Continue");
        next.getStyleClass().add("primary-button");
        next.setDisable(true);
        Runnable updateEnabled = () -> next.setDisable(
                groups.values().stream().anyMatch(g -> g.getSelectedToggle() == null));
        groups.values().forEach(g -> g.selectedToggleProperty().addListener((o, a, b) -> updateEnabled.run()));

        next.setOnAction(e -> {
            Map<String, Integer> values = new LinkedHashMap<>();
            groups.forEach((id, g) -> values.put(id, (Integer) g.getSelectedToggle().getUserData()));
            onDone.accept(values);
        });
        content.add(next);

        this.root = Layout.page(content.toArray(new Node[0]));
    }

    @Override
    public Parent root() {
        return root;
    }
}
