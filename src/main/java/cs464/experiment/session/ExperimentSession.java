package cs464.experiment.session;

import cs464.experiment.model.BlockConfig;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;
import cs464.experiment.model.Schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
        return build(config, questionsById, participantId, orderId, null);
    }

    /**
     * A session that follows one participant's row of the schedule: the counterbalancing order comes
     * from the schedule, and each question's AI answer is made right or wrong as scheduled.
     */
    public static ExperimentSession create(ExperimentConfig config,
                                           Map<String, Question> questionsById,
                                           String participantId,
                                           Schedule.Participant scheduled) {
        List<ExplanationLevel> levels = scheduled.explanationOrder();
        for (int i = 0; i < config.orders().size(); i++) {
            List<ExplanationLevel> candidate = config.orders().get(i).stream()
                    .map(ExplanationLevel::fromConfig).toList();
            if (candidate.equals(levels)) {
                return build(config, questionsById, participantId, i + 1, scheduled);
            }
        }
        throw new IllegalArgumentException("No order in experiment.json matches the schedule for "
                + scheduled.participant() + ": " + levels);
    }

    private static ExperimentSession build(ExperimentConfig config,
                                           Map<String, Question> questionsById,
                                           String participantId,
                                           int orderId,
                                           Schedule.Participant scheduled) {
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

            Map<String, Boolean> aiCorrectById = new HashMap<>();
            if (scheduled != null) {
                Schedule.Section section = scheduled.section(i + 1).orElseThrow(() -> new IllegalArgumentException(
                        "Schedule for " + scheduled.participant() + " has no section for block " + block.id()));
                section.questions().forEach(q -> aiCorrectById.put(q.id(), q.aiCorrect()));
            }

            List<Question> questions = new ArrayList<>();
            for (String id : block.questionIds()) {
                Question question = questionsById.get(id);
                Boolean aiCorrect = aiCorrectById.get(id);
                questions.add(aiCorrect == null ? question : question.withAiCorrect(aiCorrect));
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
