package cs464.experiment.model;

import java.util.List;

/**
 * One multiple-choice question plus the scripted AI helper's suggestion for it.
 * Loaded from src/main/resources/questions/final_questions.json (see docs/question-format.md).
 *
 * <p>The explanation fields may be empty strings until the text is written; nothing is shown then.
 *
 * <p>The AI is "wrong" on a question exactly when {@code aiAnswerIndex != correctIndex}.
 * In the file, {@code aiAnswerIndex} is the correct answer and the {@code explanation*} fields support it.
 * Whether the AI is right or wrong for a given participant is decided by the schedule (see
 * {@link Schedule}); {@link #withAiCorrect(boolean)} produces that participant's version of the question,
 * using {@code wrongAiAnswerIndex} and the {@code wrongExplanation*} fields when the AI must be wrong.
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
        String source,           // where the question came from (for docs/question-sourcing.md)
        Integer wrongAiAnswerIndex,      // incorrect option the AI suggests when scheduled to be wrong; null if never wrong
        String wrongExplanationBrief,    // brief explanation supporting the wrong answer
        String wrongExplanationDetailed  // detailed explanation supporting the wrong answer
) {

    /** For questions that never need a wrong AI answer. */
    public Question(String id, String section, String difficulty, String stem, List<String> choices,
                    int correctIndex, int aiAnswerIndex, String explanationBrief, String explanationDetailed,
                    String source) {
        this(id, section, difficulty, stem, choices, correctIndex, aiAnswerIndex,
                explanationBrief, explanationDetailed, source, null, null, null);
    }

    public boolean isAiCorrect() {
        return aiAnswerIndex == correctIndex;
    }

    /**
     * This question as one participant sees it: the AI suggests the correct answer (with the normal
     * explanations) or the wrong answer (with the wrong-answer explanations).
     */
    public Question withAiCorrect(boolean aiCorrect) {
        if (aiCorrect) {
            return new Question(id, section, difficulty, stem, choices, correctIndex, correctIndex,
                    explanationBrief, explanationDetailed, source,
                    wrongAiAnswerIndex, wrongExplanationBrief, wrongExplanationDetailed);
        }
        if (wrongAiAnswerIndex == null || wrongAiAnswerIndex == correctIndex) {
            throw new IllegalStateException("Question '" + id + "' has no valid wrong_ai_answer_index");
        }
        return new Question(id, section, difficulty, stem, choices, correctIndex, wrongAiAnswerIndex,
                wrongExplanationBrief == null ? "" : wrongExplanationBrief,
                wrongExplanationDetailed == null ? "" : wrongExplanationDetailed, source,
                wrongAiAnswerIndex, wrongExplanationBrief, wrongExplanationDetailed);
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
