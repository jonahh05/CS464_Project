package cs464.experiment.session;

import cs464.experiment.model.BlockConfig;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * One participant's run through the experiment: who they are, which counterbalancing
 * order they got, and the resulting list of blocks in the order they will see them.
 *
 * <p>This class has no UI or file code, so the experiment logic can be unit tested.
 * Run {@link ConfigValidator} on the config first; this class assumes it is valid.
 */
public final class ExperimentSession {

    private final String participantId;
    private final int orderId;
    private final List<PlannedBlock> blocks;

    private ExperimentSession(String participantId, int orderId, List<PlannedBlock> blocks) {
        this.participantId = participantId;
        this.orderId = orderId;
        this.blocks = List.copyOf(blocks);
    }

    /**
     * @param orderId 1-based index into {@code config.orders()}
     */
    public static ExperimentSession create(ExperimentConfig config,
                                           Map<String, Question> questionsById,
                                           String participantId,
                                           int orderId) {
        if (orderId < 1 || orderId > config.orders().size()) {
            throw new IllegalArgumentException("Order " + orderId + " does not exist (1-"
                    + config.orders().size() + ")");
        }
        List<String> order = config.orders().get(orderId - 1);
        Random shuffleRandom = new Random(participantId.hashCode());

        List<PlannedBlock> planned = new ArrayList<>();
        for (int i = 0; i < config.blocks().size(); i++) {
            BlockConfig block = config.blocks().get(i);

            ExplanationLevel level = block.orderSlot() != null
                    ? ExplanationLevel.fromConfig(order.get(block.orderSlot()))
                    : ExplanationLevel.fromConfig(block.explanation());

            List<Question> questions = new ArrayList<>();
            for (String id : block.questionIds()) {
                questions.add(questionsById.get(id));
            }
            if (config.shuffleQuestionsWithinBlock()) {
                Collections.shuffle(questions, shuffleRandom);
            }

            planned.add(new PlannedBlock(i, block.id(), block.title(), block.instructions(), level, questions));
        }
        return new ExperimentSession(participantId, orderId, planned);
    }

    public String participantId() {
        return participantId;
    }

    public int orderId() {
        return orderId;
    }

    public List<PlannedBlock> blocks() {
        return blocks;
    }
}
