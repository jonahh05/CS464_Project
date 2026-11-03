package cs464.experiment.ui;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Swaps screens in a single window, so the participant never sees new windows pop up. */
public final class Navigator {

    private static final String STYLESHEET = "/styles/app.css";

    private final Scene scene;

    public Navigator(Stage stage) {
        this.scene = new Scene(new StackPane(), 1000, 750);
        var css = Navigator.class.getResource(STYLESHEET);
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setScene(scene);
    }

    public void show(Screen screen) {
        scene.setRoot(screen.root());
        screen.onShown();
    }
}
