package cs464.experiment.model;

import java.util.List;

/**
 * One multiple-choice question plus the scripted AI helper's suggestion for it.
 * Loaded from src/main/resources/questions/questions.json (see docs/question-format.md).
 *
 * <p>The AI is "wrong" on a question exactly when {@code aiAnswerIndex != correctIndex};
 * a block's AI accuracy is therefore decided by which questions we put in it.
 */
public record Question(
        String id,
        String section,          // e.g. "math", "reading_writing"
        String difficulty,       // e.g. "easy", "medium", "hard"
        String stem,             // the question text shown to the participant
        List<String> choices,    // answer options in display order (A, B, C, D, ...)
        int correctIndex,        // 0-based index into choices
        int aiAnswerIndex,       // 0-based index the AI helper suggests
        String explanationBrief,    // one or two sentences supporting the AI's answer
        String explanationDetailed, // step-by-step reasoning supporting the AI's answer
        String source            // where the question came from (for docs/question-sourcing.md)
) {

    public boolean isAiCorrect() {
        return aiAnswerIndex == correctIndex;
    }

    /** The explanation text to show for a level, or null for {@link ExplanationLevel#NONE}. */
    public String explanationFor(ExplanationLevel level) {
        switch (level) {
            case BRIEF:
                return explanationBrief;
            case DETAILED:
                return explanationDetailed;
            default:
                return null;
        }
    }

    /** Converts a 0-based choice index to a letter: 0 -> "A", 1 -> "B", ... */
    public static String letter(int index) {
        return String.valueOf((char) ('A' + index));
    }
}
