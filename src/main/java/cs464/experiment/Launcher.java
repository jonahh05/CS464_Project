package cs464.experiment;

/**
 * Main class for the runnable jar ({@code java -jar target/experiment.jar}).
 * JavaFX refuses to start from a jar whose main class extends Application,
 * so this plain class just forwards to {@link App}. Use App directly everywhere else.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        App.main(args);
    }
}
