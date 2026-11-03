package cs464.experiment.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Small helpers so every screen gets the same page layout and text styles (see app.css). */
final class Layout {

    private static final double PAGE_WIDTH = 820;
    /** Wider page for screens with two columns (question + side card). */
    static final double WIDE_PAGE_WIDTH = 1080;

    private Layout() {
    }

    /** A centered, scrollable column of content. */
    static Parent page(Node... children) {
        return page(PAGE_WIDTH, children);
    }

    /** A centered, scrollable column of content with a custom maximum width. */
    static Parent page(double maxWidth, Node... children) {
        VBox column = new VBox(18, children);
        column.setMaxWidth(maxWidth);
        column.setPadding(new Insets(40, 32, 40, 32));
        column.setAlignment(Pos.TOP_LEFT);

        StackPane centered = new StackPane(column);
        centered.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(centered);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page");
        return scroll;
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
