package cs464.experiment.ui;

import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;
import cs464.experiment.session.TwoStepAnswer;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/**
 * One question answered in two steps. The participant selects and submits an original
 * answer; only then is the scripted AI helper's suggestion shown, and they submit again
 * to keep or change their answer. The initial response time runs from when the screen
 * appears until the first Submit; the total runs until the second Submit.
 */
public final class QuestionScreen implements Screen {

    /** Receives the participant's answer for one question. */
    public interface AnswerListener {
        void answered(int originalIndex, int finalIndex, long initialResponseTimeMs, long totalResponseTimeMs);
    }

    private final Parent root;
    private long shownAtNanos;
    private long originalTimeMs;

    public QuestionScreen(String progressText, Question question, ExplanationLevel level, AnswerListener onAnswer) {
        ToggleGroup choices = new ToggleGroup();
        VBox choiceList = new VBox(10);
        for (int i = 0; i < question.choices().size(); i++) {
            RadioButton option = new RadioButton(Question.letter(i) + ".  " + question.choices().get(i));
            option.setWrapText(true);
            option.setUserData(i);
            option.setToggleGroup(choices);
            choiceList.getChildren().add(option);
        }

        TwoStepAnswer state = new TwoStepAnswer();
        VBox ai = aiPanel(question, level);
        ai.setVisible(false);
        ai.setManaged(false);

        Label prompt = Layout.muted("Select your answer, then submit it.");
        Button submit = new Button("Submit answer");
        submit.getStyleClass().add("primary-button");
        submit.disableProperty().bind(choices.selectedToggleProperty().isNull());
        choices.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now != null) {
                state.select((Integer) now.getUserData());
            }
        });
        submit.setOnAction(e -> {
            state.submit();
            if (!state.isComplete()) {
                originalTimeMs = (System.nanoTime() - shownAtNanos) / 1_000_000;
                ai.setVisible(true);
                ai.setManaged(true);
                prompt.setText("The AI helper's suggestion is shown below. Keep your answer or choose a "
                        + "different one, then submit again to confirm your final answer.");
                submit.setText("Submit final answer");
            } else {
                long totalMs = (System.nanoTime() - shownAtNanos) / 1_000_000;
                onAnswer.answered(state.originalIndex(), state.finalIndex(), originalTimeMs, totalMs);
            }
        });

        this.root = Layout.page(
                Layout.muted(progressText),
                Layout.body(question.stem()),
                choiceList,
                prompt,
                ai,
                submit);
    }

    /** The AI helper box. Only this method changes between explanation levels. */
    private static VBox aiPanel(Question question, ExplanationLevel level) {
        Label heading = new Label("AI Helper");
        heading.getStyleClass().add("ai-heading");

        Label suggestion = new Label("Suggested answer: " + Question.letter(question.aiAnswerIndex()));
        suggestion.getStyleClass().add("ai-suggestion");

        VBox panel = new VBox(8, heading, suggestion);
        String explanation = question.explanationFor(level);
        if (explanation != null && !explanation.isBlank()) {
            panel.getChildren().add(Layout.body(explanation));
        }
        panel.getStyleClass().add("ai-panel");
        return panel;
    }

    @Override
    public Parent root() {
        return root;
    }

    @Override
    public void onShown() {
        shownAtNanos = System.nanoTime();
    }
}
