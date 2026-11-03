package cs464.experiment.ui;

import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.HelperStyle;
import cs464.experiment.model.Question;
import cs464.experiment.session.PlannedTrial;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * One question with the scripted AI helper's suggestion.
 *
 * <p>Two helper styles, chosen by {@code helper_style} in experiment.json:
 * <ul>
 *   <li>{@code box} (default): a box under the answer choices.</li>
 *   <li>{@code side_card}: a card beside the question with a letter badge, the choice text,
 *       a "Why" box, and the AI's pick marked in the answer list.</li>
 * </ul>
 * Options that apply to both: a "Use this answer" button, a delay before the suggestion appears,
 * and a per-question time limit.
 *
 * <p>Response time runs from when the screen appears until Submit is pressed (or time runs out),
 * so participants can change their selection before submitting.
 */
public final class QuestionScreen implements Screen {

    /** What the participant did on one question. {@code chosenIndex} is -1 if time ran out with nothing selected. */
    public record Answer(int chosenIndex, long responseTimeMs, long timeToSuggestionMs,
                         boolean timedOut, boolean usedAnswerButton) {
    }

    /** Receives the participant's answer for one question. */
    public interface AnswerListener {
        void answered(Answer answer);
    }

    /** How the helper is presented; read from experiment.json. */
    public record Options(HelperStyle style, boolean useAnswerButton, int aiDelayMs, int timeLimitSeconds) {
        public static Options from(ExperimentConfig config) {
            return new Options(config.helper(), config.showUseAnswerButton(), config.aiDelay(), config.timeLimit());
        }
    }

    private static final String BOX_TITLE = "AI Helper";
    private static final String CARD_TITLE = "Answer Assist";

    private final Parent root;
    private final PlannedTrial trial;
    private final ExplanationLevel level;
    private final Options options;
    private final AnswerListener onAnswer;

    private final ToggleGroup choices = new ToggleGroup();
    private final List<RadioButton> choiceButtons = new ArrayList<>();
    private final VBox helperBody = new VBox(10);
    private final Label timerText = Layout.muted("");
    private final ProgressBar timerBar = new ProgressBar(1);

    private long shownAtNanos;
    private long suggestionAtNanos = -1;
    private boolean usedAnswerButton;
    private boolean finished;
    private PauseTransition reveal;
    private Timeline countdown;

    public QuestionScreen(String progressText, PlannedTrial trial, ExplanationLevel level,
                          Options options, AnswerListener onAnswer) {
        this.trial = trial;
        this.level = level;
        this.options = options;
        this.onAnswer = onAnswer;
        Question question = trial.question();

        VBox choiceList = new VBox(10);
        for (int i = 0; i < question.choices().size(); i++) {
            RadioButton option = new RadioButton(Question.letter(i) + ".  " + question.choices().get(i));
            option.setWrapText(true);
            option.setUserData(i);
            option.setToggleGroup(choices);
            if (options.style() == HelperStyle.SIDE_CARD) {
                option.getStyleClass().add("choice"); // card-style choices; the box style keeps plain radio buttons
                option.setMaxWidth(Double.MAX_VALUE);
            }
            choiceButtons.add(option);
            choiceList.getChildren().add(option);
        }

        Button submit = new Button("Submit answer");
        submit.getStyleClass().add("primary-button");
        submit.disableProperty().bind(choices.selectedToggleProperty().isNull());
        submit.setOnAction(e -> finish(false));

        List<Node> header = new ArrayList<>();
        header.add(Layout.muted(progressText));
        if (options.timeLimitSeconds() > 0) {
            timerBar.setMaxWidth(Double.MAX_VALUE);
            timerBar.getStyleClass().add("timer-bar");
            header.add(new VBox(4, timerText, timerBar));
        }

        Label stem = Layout.body(question.stem());
        stem.getStyleClass().add("question-stem");

        if (options.style() == HelperStyle.SIDE_CARD) {
            VBox questionColumn = new VBox(18, stem, choiceList, submit);
            questionColumn.setMinWidth(0);
            HBox.setHgrow(questionColumn, Priority.ALWAYS);
            VBox card = sideCard();
            HBox columns = new HBox(28, questionColumn, card);
            columns.setAlignment(Pos.TOP_LEFT);
            columns.setFillHeight(false); // the card keeps its own height
            List<Node> page = new ArrayList<>(header);
            page.add(columns);
            this.root = Layout.page(Layout.WIDE_PAGE_WIDTH, page.toArray(new Node[0]));
        } else {
            List<Node> page = new ArrayList<>(header);
            page.add(stem);
            page.add(choiceList);
            page.add(box());
            page.add(submit);
            this.root = Layout.page(page.toArray(new Node[0]));
        }

        if (options.aiDelayMs() > 0) {
            Label waiting = Layout.muted(options.style() == HelperStyle.SIDE_CARD
                    ? "Analyzing the question…" : "Thinking…");
            helperBody.getChildren().setAll(waiting);
        } else {
            showSuggestion();
        }
    }

    // --- helper layouts ---------------------------------------------------------------------

    /** The original helper: a box under the answer choices. */
    private VBox box() {
        Label heading = new Label(BOX_TITLE);
        heading.getStyleClass().add("ai-heading");
        VBox panel = new VBox(8, heading, helperBody);
        panel.getStyleClass().add("ai-panel");
        return panel;
    }

    /** The suggestion card shown beside the question. */
    private VBox sideCard() {
        Region mark = new Region();
        mark.getStyleClass().add("ai-mark");
        Label title = new Label(CARD_TITLE);
        title.getStyleClass().add("ai-card-title");
        Label chip = new Label("AI");
        chip.getStyleClass().add("ai-chip");
        HBox head = new HBox(8, mark, title, chip);
        head.setAlignment(Pos.CENTER_LEFT);
        head.getStyleClass().add("ai-card-head");

        helperBody.getStyleClass().add("ai-card-body");
        VBox card = new VBox(head, helperBody);
        card.getStyleClass().add("ai-card");
        card.setPrefWidth(330);
        card.setMinWidth(280);
        card.setMaxWidth(360);
        return card;
    }

    /** Fills the helper with the suggestion (immediately, or after {@code ai_delay_ms}). */
    private void showSuggestion() {
        Question question = trial.question();
        int ai = trial.aiAnswerIndex();
        String letter = Question.letter(ai);
        String explanation = trial.explanationFor(level);
        boolean hasExplanation = explanation != null && !explanation.isBlank();

        List<Node> content = new ArrayList<>();
        if (options.style() == HelperStyle.SIDE_CARD) {
            content.add(eyebrow("Suggested answer"));
            Label badge = new Label(letter);
            badge.getStyleClass().add("ai-badge");
            Label choiceText = new Label(question.choices().get(ai));
            choiceText.setWrapText(true);
            choiceText.getStyleClass().add("ai-choice-text");
            HBox pick = new HBox(12, badge, choiceText);
            pick.setAlignment(Pos.CENTER_LEFT);
            content.add(pick);
            if (hasExplanation) {
                Label why = Layout.body(explanation);
                why.getStyleClass().add("ai-why-text");
                VBox whyBox = new VBox(4, eyebrow("Why"), why);
                whyBox.getStyleClass().add("ai-why");
                content.add(whyBox);
            }
            choiceButtons.get(ai).getStyleClass().add("ai-pick");
        } else {
            Label suggestion = new Label("Suggested answer: " + letter);
            suggestion.getStyleClass().add("ai-suggestion");
            content.add(suggestion);
            if (hasExplanation) {
                content.add(Layout.body(explanation));
            }
        }
        if (options.useAnswerButton()) {
            Button use = new Button("Use this answer");
            use.getStyleClass().add("use-answer-button");
            use.setOnAction(e -> {
                usedAnswerButton = true;
                choices.selectToggle(choiceButtons.get(ai));
            });
            content.add(use);
        }
        helperBody.getChildren().setAll(content);
        suggestionAtNanos = System.nanoTime();
    }

    private static Label eyebrow(String text) {
        Label label = new Label(text.toUpperCase());
        label.getStyleClass().add("eyebrow");
        return label;
    }

    // --- timing -------------------------------------------------------------------------------

    @Override
    public void onShown() {
        shownAtNanos = System.nanoTime();
        if (suggestionAtNanos >= 0) {
            suggestionAtNanos = shownAtNanos; // shown with the question
        }
        if (options.aiDelayMs() > 0) {
            reveal = new PauseTransition(Duration.millis(options.aiDelayMs()));
            reveal.setOnFinished(e -> {
                if (!finished) {
                    showSuggestion();
                }
            });
            reveal.play();
        }
        int limit = options.timeLimitSeconds();
        if (limit > 0) {
            int[] left = {limit};
            updateTimer(left[0], limit);
            countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                left[0]--;
                updateTimer(left[0], limit);
                if (left[0] <= 0) {
                    finish(true);
                }
            }));
            countdown.setCycleCount(limit);
            countdown.play();
        }
    }

    private void updateTimer(int left, int limit) {
        timerText.setText("Time remaining: " + Math.max(0, left) + " s");
        timerBar.setProgress(Math.max(0, left) / (double) limit);
        if (left <= 10 && !timerBar.getStyleClass().contains("urgent")) {
            timerBar.getStyleClass().add("urgent");
        }
    }

    private void finish(boolean timedOut) {
        if (finished) {
            return;
        }
        finished = true;
        if (reveal != null) {
            reveal.stop();
        }
        if (countdown != null) {
            countdown.stop();
        }
        long now = System.nanoTime();
        long responseMs = (now - shownAtNanos) / 1_000_000;
        long toSuggestionMs = suggestionAtNanos >= 0 ? (suggestionAtNanos - shownAtNanos) / 1_000_000 : -1;
        int chosen = choices.getSelectedToggle() == null ? -1 : (Integer) choices.getSelectedToggle().getUserData();
        onAnswer.answered(new Answer(chosen, responseMs, toSuggestionMs, timedOut, usedAnswerButton));
    }

    @Override
    public Parent root() {
        return root;
    }
}
