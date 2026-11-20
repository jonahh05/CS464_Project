package cs464.experiment.session;

import cs464.experiment.model.BlockConfig;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;
import cs464.experiment.model.Schedule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks schedule.json against experiment.json and final_questions.json, so a schedule that
 * breaks a design rule is reported before any participant data is collected.
 */
public final class ScheduleValidator {

    private static final Set<Integer> ACCURACY_LEVELS = Set.of(50, 75, 100);

    private ScheduleValidator() {
    }

    /** @return human-readable problems; empty if the schedule follows every design rule */
    public static List<String> validate(Schedule schedule, ExperimentConfig config, Map<String, Question> questionsById) {
        List<String> errors = new ArrayList<>();
        Set<List<ExplanationLevel>> usedOrders = new HashSet<>();

        for (Schedule.Participant p : schedule.participants()) {
            String who = p.participant() + ": ";
            List<ExplanationLevel> order;
            try {
                order = p.explanationOrder();
            } catch (IllegalArgumentException e) {
                errors.add(who + e.getMessage());
                continue;
            }
            if (!usedOrders.add(order)) {
                errors.add(who + "explanation order " + order + " is used by another participant");
            }
            boolean orderExists = config.orders().stream()
                    .anyMatch(o -> o.stream().map(ExplanationLevel::fromConfig).toList().equals(order));
            if (!orderExists) {
                errors.add(who + "explanation order " + order + " is not one of the orders in experiment.json");
            }
            if (p.sections().size() != config.blocks().size()) {
                errors.add(who + "has " + p.sections().size() + " sections but experiment.json has "
                        + config.blocks().size() + " blocks");
                continue;
            }
            checkSections(p, order, config, questionsById, errors);
        }
        if (usedOrders.size() != config.orders().size()) {
            errors.add("The schedule uses " + usedOrders.size() + " explanation orders but experiment.json has "
                    + config.orders().size());
        }
        return errors;
    }

    private static void checkSections(Schedule.Participant p, List<ExplanationLevel> order, ExperimentConfig config,
                                      Map<String, Question> questionsById, List<String> errors) {
        List<Integer> accuracies = new ArrayList<>();
        List<List<Integer>> accuracyByPhase = new ArrayList<>();
        for (int i = 0; i < config.blocks().size(); i++) {
            BlockConfig block = config.blocks().get(i);
            String where = p.participant() + " section " + (i + 1) + ": ";
            Schedule.Section section = p.section(i + 1).orElse(null);
            if (section == null) {
                errors.add(where + "is missing");
                continue;
            }

            ExplanationLevel expectedLevel = block.orderSlot() != null
                    ? order.get(block.orderSlot())
                    : ExplanationLevel.fromConfig(block.explanation());
            if (Schedule.levelFor(section.explainability()) != expectedLevel) {
                errors.add(where + "explainability " + section.explainability() + " does not match " + expectedLevel);
            }

            Set<String> scheduledIds = new HashSet<>();
            section.questions().forEach(q -> scheduledIds.add(q.id()));
            if (!scheduledIds.equals(new HashSet<>(block.questionIds()))) {
                errors.add(where + "questions differ from block " + block.id() + " in experiment.json");
                continue;
            }

            int wrong = 0;
            int wrongMath = 0;
            int wrongEnglish = 0;
            for (Schedule.ScheduledQuestion sq : section.questions()) {
                if (sq.aiCorrect()) {
                    continue;
                }
                wrong++;
                Question q = questionsById.get(sq.id());
                if (q == null) {
                    continue;
                }
                if ("math".equals(q.section())) {
                    wrongMath++;
                } else {
                    wrongEnglish++;
                }
                if (q.wrongAiAnswerIndex() == null) {
                    errors.add(where + "question " + sq.id() + " is scheduled wrong but has no wrong_ai_answer_index");
                }
            }
            int percent = (int) Math.round(100.0 * (section.questions().size() - wrong) / section.questions().size());
            if (percent != section.accuracy()) {
                errors.add(where + "accuracy says " + section.accuracy() + "% but the questions give " + percent + "%");
            }
            if (!ACCURACY_LEVELS.contains(section.accuracy())) {
                errors.add(where + "accuracy " + section.accuracy() + " is not 50, 75 or 100");
            }
            if (wrongMath > 1 || wrongEnglish > 1) {
                errors.add(where + "has " + wrongMath + " wrong math and " + wrongEnglish + " wrong English questions (max 1 each)");
            }
            if (section.accuracy() == 50 && (wrongMath != 1 || wrongEnglish != 1)) {
                errors.add(where + "a 50% section needs one wrong math and one wrong English question");
            }

            accuracies.add(section.accuracy());
            if (block.orderSlot() != null) {
                while (accuracyByPhase.size() <= block.orderSlot()) {
                    accuracyByPhase.add(new ArrayList<>());
                }
                accuracyByPhase.get(block.orderSlot()).add(section.accuracy());
            }
        }

        if (!accuracies.equals(p.accuracyBySection())) {
            errors.add(p.participant() + ": accuracy_by_section " + p.accuracyBySection()
                    + " does not match the sections " + accuracies);
        }
        for (int phase = 0; phase < accuracyByPhase.size(); phase++) {
            if (accuracyByPhase.get(phase).stream().sorted().toList().equals(List.of(50, 75, 100))) {
                continue;
            }
            errors.add(p.participant() + " phase " + (phase + 1) + ": needs one section each at 50%, 75% and 100%, got "
                    + accuracyByPhase.get(phase));
        }
    }
}
