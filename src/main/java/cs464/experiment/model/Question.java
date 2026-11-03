package cs464.experiment.model;

import java.util.List;

/**
 * One multiple-choice question plus the scripted AI helper's suggestion for it.
 * Loaded from src/main/resources/questions/questions.json (see docs/question-format.md).
 *
 * <p>There are two ways the AI's suggestion is decided, chosen per block in experiment.json:
 * <ul>
 *   <li><b>Fixed</b> (no {@code ai_accuracy_percent} on the block): the AI always suggests
 *       {@code aiAnswerIndex}. It is "wrong" exactly when {@code aiAnswerIndex != correctIndex},
 *       so the block's AI accuracy is decided by which questions we put in it.</li>
 *   <li><b>Drawn</b> ({@code ai_accuracy_percent} set): the suggestion is drawn per participant
 *       (see {@code SuggestionDrawer}), so any choice can be suggested. Each choice then needs
 *       its own explanation in {@code explanationsByChoice}.</li>
 * </ul>
 */
public record Question(
        String id,
        String section,          // e.g. "math", "reading_writing"
        String difficulty,       // e.g. "easy", "medium", "hard"
        String stem,             // the question text shown to the participant
        List<String> choices,    // answer options in display order (A, B, C, D, ...)
        int correctIndex,        // 0-based index into choices
        int aiAnswerIndex,       // 0-based index the AI helper suggests in fixed mode
        String explanationBrief,    // one or two sentences supporting aiAnswerIndex
        String explanationDetailed, // step-by-step reasoning supporting aiAnswerIndex
        String source,           // where the question came from (for docs/question-sourcing.md)
        List<ChoiceExplanation> explanationsByChoice // optional: one entry per choice, same order as choices
) {

    /** Same as the full constructor without per-choice explanations (fixed mode only). */
    public Question(String id, String section, String difficulty, String stem, List<String> choices,
                    int correctIndex, int aiAnswerIndex, String explanationBrief, String explanationDetailed,
                    String source) {
        this(id, section, difficulty, stem, choices, correctIndex, aiAnswerIndex,
                explanationBrief, explanationDetailed, source, null);
    }

    public boolean isAiCorrect() {
        return aiAnswerIndex == correctIndex;
    }

    /** The explanation for the fixed-mode suggestion, or null for {@link ExplanationLevel#NONE}. */
    public String explanationFor(ExplanationLevel level) {
        return explanationFor(aiAnswerIndex, level);
    }

    /**
     * The explanation arguing for {@code choiceIndex}, or null for {@link ExplanationLevel#NONE}
     * or when none was written. {@code explanations_by_choice} wins when present; otherwise the
     * single {@code explanation_brief}/{@code explanation_detailed} pair is used for {@code aiAnswerIndex}.
     */
    public String explanationFor(int choiceIndex, ExplanationLevel level) {
        if (level == ExplanationLevel.NONE) {
            return null;
        }
        if (explanationsByChoice != null && choiceIndex >= 0 && choiceIndex < explanationsByChoice.size()
                && explanationsByChoice.get(choiceIndex) != null) {
            return explanationsByChoice.get(choiceIndex).forLevel(level);
        }
        if (choiceIndex == aiAnswerIndex) {
            return level == ExplanationLevel.BRIEF ? explanationBrief : explanationDetailed;
        }
        return null;
    }

    /** Converts a 0-based choice index to a letter: 0 -> "A", 1 -> "B", ... */
    public static String letter(int index) {
        return String.valueOf((char) ('A' + index));
    }
}
