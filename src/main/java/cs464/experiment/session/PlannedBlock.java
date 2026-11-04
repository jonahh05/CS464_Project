package cs464.experiment.session;

import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;

import java.util.List;

/** A block after counterbalancing and suggestion draws are applied: what this participant will actually see. */
public record PlannedBlock(
        int index,
        String id,
        String title,
        String instructions,
        ExplanationLevel explanationLevel,
        Double aiAccuracyPercent,   // the block's planned accuracy (see BlockConfig.plannedAccuracyPercent), null in fixed mode
        List<PlannedTrial> trials
) {
    /** The block's questions in presentation order. */
    public List<Question> questions() {
        return trials.stream().map(PlannedTrial::question).toList();
    }

    /** Share of this participant's trials where the AI suggestion is correct (0.0-1.0). */
    public double aiAccuracy() {
        if (trials.isEmpty()) {
            return 0.0;
        }
        long correct = trials.stream().filter(PlannedTrial::isAiCorrect).count();
        return (double) correct / trials.size();
    }
}
