package cs464.experiment.ui;

import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/**
 * One question with the scripted AI helper's suggestion shown underneath.
 * Response time runs from when the screen appears until Submit is pressed,
 * so participants can change their selection before submitting.
 */
public final class QuestionScreen implements Screen {

    /** Receives the participant's answer for one question. */
    public interface AnswerListener {
        void answered(int chosenIndex, long responseTimeMs);
    }

    private final Parent root;
    private long shownAtNanos;

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

        Button submit = new Button("Submit answer");
        submit.getStyleClass().add("primary-button");
        submit.disableProperty().bind(choices.selectedToggleProperty().isNull());
        submit.setOnAction(e -> {
            long elapsedMs = (System.nanoTime() - shownAtNanos) / 1_000_000;
            onAnswer.answered((Integer) choices.getSelectedToggle().getUserData(), elapsedMs);
        });

        this.root = Layout.page(
                Layout.muted(progressText),
                Layout.body(question.stem()),
                choiceList,
                aiPanel(question, level),
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
