package cs464.experiment.session;

import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;

import java.util.List;

/** A block after counterbalancing is applied: what this participant will actually see. */
public record PlannedBlock(
        int index,
        String id,
        String title,
        String instructions,
        ExplanationLevel explanationLevel,
        List<Question> questions
) {
    /** Share of questions in this block where the AI suggestion is correct (0.0-1.0). */
    public double aiAccuracy() {
        if (questions.isEmpty()) {
            return 0.0;
        }
        long correct = questions.stream().filter(Question::isAiCorrect).count();
        return (double) correct / questions.size();
    }
}
