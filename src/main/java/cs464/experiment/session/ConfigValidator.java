package cs464.experiment.session;

import cs464.experiment.model.BlockConfig;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.ExplanationLevel;
import cs464.experiment.model.Question;
import cs464.experiment.model.RatingItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks experiment.json and questions.json against each other before a session starts,
 * so a typo in a JSON file shows up as a readable message instead of a crash mid-session.
 */
public final class ConfigValidator {

    private ConfigValidator() {
    }

    /** @return human-readable problems; empty if everything is fine */
    public static List<String> validate(ExperimentConfig config, Map<String, Question> questionsById) {
        List<String> errors = new ArrayList<>();

        for (Question q : questionsById.values()) {
            checkQuestion(q, errors);
        }

        if (config.ratingMin() >= config.ratingMax()) {
            errors.add("rating_min must be smaller than rating_max");
        }
        checkRatingItems(config.ratingItems(), errors);
        int slots = checkOrders(config.orders(), errors);
        checkBlocks(config.blocks(), slots, questionsById, errors);

        return errors;
    }

    private static void checkQuestion(Question q, List<String> errors) {
        String where = "Question '" + q.id() + "': ";
        if (isBlank(q.stem())) {
            errors.add(where + "stem is empty");
        }
        if (q.choices() == null || q.choices().size() < 2) {
            errors.add(where + "needs at least 2 choices");
            return;
        }
        if (q.correctIndex() < 0 || q.correctIndex() >= q.choices().size()) {
            errors.add(where + "correct_index " + q.correctIndex() + " is out of range");
        }
        if (q.aiAnswerIndex() < 0 || q.aiAnswerIndex() >= q.choices().size()) {
            errors.add(where + "ai_answer_index " + q.aiAnswerIndex() + " is out of range");
        }
    }

    private static void checkRatingItems(List<RatingItem> items, List<String> errors) {
        if (items == null || items.isEmpty()) {
            errors.add("rating_items is empty");
            return;
        }
        Set<String> ids = new HashSet<>();
        for (RatingItem item : items) {
            if (isBlank(item.id()) || !ids.add(item.id())) {
                errors.add("Rating item id '" + item.id() + "' is missing or duplicated");
            }
            if (isBlank(item.prePrompt()) || isBlank(item.postPrompt())) {
                errors.add("Rating item '" + item.id() + "' needs both pre_prompt and post_prompt");
            }
        }
    }

    /** @return the number of slots in each order (0 if orders are invalid) */
    private static int checkOrders(List<List<String>> orders, List<String> errors) {
        if (orders == null || orders.isEmpty()) {
            errors.add("orders is empty (add at least one counterbalancing order)");
            return 0;
        }
        int size = orders.get(0).size();
        Set<List<String>> seen = new HashSet<>();
        for (int i = 0; i < orders.size(); i++) {
            List<String> order = orders.get(i);
            String where = "Order " + (i + 1) + ": ";
            if (order.size() != size) {
                errors.add(where + "has " + order.size() + " entries but order 1 has " + size);
            }
            if (new HashSet<>(order).size() != order.size()) {
                errors.add(where + "repeats an explanation level");
            }
            if (!seen.add(order)) {
                errors.add(where + "is a duplicate of an earlier order");
            }
            for (String level : order) {
                try {
                    ExplanationLevel.fromConfig(level);
                } catch (IllegalArgumentException e) {
                    errors.add(where + e.getMessage());
                }
            }
        }
        return size;
    }

    private static void checkBlocks(List<BlockConfig> blocks, int slots,
                                    Map<String, Question> questionsById, List<String> errors) {
        if (blocks == null || blocks.isEmpty()) {
            errors.add("blocks is empty");
            return;
        }
        Set<String> usedQuestions = new HashSet<>();
        Set<Integer> usedSlots = new HashSet<>();
        for (BlockConfig block : blocks) {
            String where = "Block '" + block.id() + "': ";
            boolean hasFixed = !isBlank(block.explanation());
            boolean hasSlot = block.orderSlot() != null;
            if (hasFixed == hasSlot) {
                errors.add(where + "set exactly one of explanation or order_slot");
            }
            if (hasFixed) {
                try {
                    ExplanationLevel.fromConfig(block.explanation());
                } catch (IllegalArgumentException e) {
                    errors.add(where + e.getMessage());
                }
            }
            if (hasSlot) {
                if (block.orderSlot() < 0 || block.orderSlot() >= slots) {
                    errors.add(where + "order_slot " + block.orderSlot() + " is out of range (0-" + (slots - 1) + ")");
                } else if (!usedSlots.add(block.orderSlot())) {
                    errors.add(where + "order_slot " + block.orderSlot() + " is used by more than one block");
                }
            }
            if (block.questionIds() == null || block.questionIds().isEmpty()) {
                errors.add(where + "has no question_ids");
                continue;
            }
            for (String id : block.questionIds()) {
                Question q = questionsById.get(id);
                if (q == null) {
                    errors.add(where + "question '" + id + "' is not in questions.json");
                    continue;
                }
                if (!usedQuestions.add(id)) {
                    errors.add(where + "question '" + id + "' is used in more than one block");
                }
                // Order-slot blocks rotate through every level, so they need every explanation.
                // A fixed block only needs the explanation for its own level.
                boolean needsBrief = hasSlot || "brief".equalsIgnoreCase(block.explanation());
                boolean needsDetailed = hasSlot || "detailed".equalsIgnoreCase(block.explanation());
                if (needsBrief && isBlank(q.explanationBrief())) {
                    errors.add(where + "question '" + id + "' is missing explanation_brief");
                }
                if (needsDetailed && isBlank(q.explanationDetailed())) {
                    errors.add(where + "question '" + id + "' is missing explanation_detailed");
                }
            }
        }
        if (slots > 0 && usedSlots.size() != slots) {
            errors.add("Orders have " + slots + " slots but only " + usedSlots.size()
                    + " blocks use an order_slot");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
