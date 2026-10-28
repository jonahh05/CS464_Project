package cs464.experiment.ui;

import javafx.scene.Parent;
import javafx.scene.control.Button;

/** A title, some text and one button: used for instructions, round intros and the end. */
public final class MessageScreen implements Screen {

    private final Parent root;

    public MessageScreen(String title, String message, String buttonText, Runnable onContinue) {
        Button next = new Button(buttonText);
        next.getStyleClass().add("primary-button");
        next.setDefaultButton(true);
        next.setOnAction(e -> onContinue.run());

        this.root = Layout.page(Layout.title(title), Layout.body(message), next);
    }

    @Override
    public Parent root() {
        return root;
    }
}
