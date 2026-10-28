package cs464.experiment.ui;

import javafx.scene.Parent;

/**
 * One full-window page of the experiment (setup, ratings, a question, ...).
 * Screens only build UI and report what the participant did through a callback;
 * deciding what comes next is {@link ExperimentFlow}'s job.
 */
public interface Screen {

    /** The node shown in the window. */
    Parent root();

    /** Called right after the screen becomes visible (e.g. to start a response timer). */
    default void onShown() {
    }
}
