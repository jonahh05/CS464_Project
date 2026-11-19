package cs464.experiment.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Small helpers so every screen gets the same page layout and text styles (see app.css). */
final class Layout {

    private static final double PAGE_WIDTH = 820;
    /** How many times JavaFX's default scroll step to move per wheel notch or touchpad swipe. */
    private static final double SCROLL_SPEED = 2.5;

    private Layout() {
    }

    /** A centered, scrollable column of content. */
    static Parent page(Node... children) {
        VBox column = new VBox(18, children);
        column.setMaxWidth(PAGE_WIDTH);
        column.setPadding(new Insets(40, 32, 40, 32));
        column.setAlignment(Pos.TOP_LEFT);

        StackPane centered = new StackPane(column);
        centered.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(centered);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page");
        speedUpScrolling(scroll, centered);
        return scroll;
    }

    /**
     * JavaFX's ScrollPane moves only a few pixels per mouse wheel notch or touchpad swipe, which feels
     * sluggish on a large full screen window. Scale each scroll step instead.
     */
    private static void speedUpScrolling(ScrollPane scroll, Node content) {
        scroll.addEventFilter(ScrollEvent.SCROLL, event -> {
            double scrollable = content.getBoundsInLocal().getHeight() - scroll.getViewportBounds().getHeight();
            if (event.getDeltaY() == 0 || scrollable <= 0) {
                return;
            }
            double next = scroll.getVvalue() - event.getDeltaY() * SCROLL_SPEED / scrollable;
            scroll.setVvalue(Math.max(scroll.getVmin(), Math.min(scroll.getVmax(), next)));
            event.consume();
        });
    }

    static Label title(String text) {
        return styled(text, "title");
    }

    static Label body(String text) {
        return styled(text, "body-text");
    }

    static Label muted(String text) {
        return styled(text, "muted");
    }

    private static Label styled(String text, String styleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
