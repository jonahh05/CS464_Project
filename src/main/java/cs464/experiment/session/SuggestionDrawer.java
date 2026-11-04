package cs464.experiment.session;

import java.nio.charset.StandardCharsets;

/**
 * Draws which answer the scripted AI suggests when a block sets {@code ai_accuracy_percent}.
 *
 * <p>With accuracy p, the correct choice is suggested with probability p and each wrong choice
 * with probability (1 - p) / (number of wrong choices). For 4 choices at 80%: correct 0.80,
 * each wrong choice 0.0667.
 *
 * <p>Each draw is seeded from (participant ID, block id, question id), so:
 * <ul>
 *   <li>the same participant always gets the same suggestions (re-running P03 reproduces P03);</li>
 *   <li>a question's draw does not depend on shuffling or on the other questions;</li>
 *   <li>different participants get different, independent draws.</li>
 * </ul>
 * Because each question is drawn on its own, the number of wrong suggestions a participant sees
 * in a round varies around (1 - p) x questions. That is a design choice the team may revisit.
 */
public final class SuggestionDrawer {

    private SuggestionDrawer() {
    }

    /** @return the 0-based choice index the AI suggests */
    public static int draw(String participantId, String blockId, String questionId,
                           int choiceCount, int correctIndex, double accuracyPercent) {
        double u = uniform(participantId + "|" + blockId + "|" + questionId);
        return pick(u, choiceCount, correctIndex, accuracyPercent);
    }

    /**
     * For {@code ai_wrong_count}: which questions in a round get a wrong suggestion. Ranks the round's
     * questions by a seeded number and marks the first {@code wrongCount} of them.
     *
     * @return one flag per question id, in the order given; true = this question gets a wrong suggestion
     */
    public static boolean[] pickWrongQuestions(String participantId, String blockId,
                                               java.util.List<String> questionIds, int wrongCount) {
        Integer[] order = new Integer[questionIds.size()];
        double[] keys = new double[questionIds.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
            keys[i] = uniform(participantId + "|" + blockId + "|" + questionIds.get(i) + "|position");
        }
        java.util.Arrays.sort(order, (a, b) -> Double.compare(keys[a], keys[b]));
        boolean[] wrong = new boolean[order.length];
        for (int i = 0; i < Math.min(wrongCount, order.length); i++) {
            wrong[order[i]] = true;
        }
        return wrong;
    }

    /** A wrong choice, each with equal chance, seeded like {@link #draw}. */
    public static int drawWrong(String participantId, String blockId, String questionId,
                                int choiceCount, int correctIndex) {
        double u = uniform(participantId + "|" + blockId + "|" + questionId);
        return pick(u, choiceCount, correctIndex, 0);
    }

    /** Maps a uniform number in [0, 1) to a choice. Separate from {@link #draw} so it can be tested exactly. */
    static int pick(double u, int choiceCount, int correctIndex, double accuracyPercent) {
        double p = Math.max(0, Math.min(100, accuracyPercent)) / 100.0;
        int wrongCount = choiceCount - 1;
        if (u < p || wrongCount <= 0) {
            return correctIndex;
        }
        double share = (1.0 - p) / wrongCount;
        int k = (int) Math.min(wrongCount - 1, Math.floor((u - p) / share));
        // k-th wrong choice in display order, skipping the correct one.
        return k < correctIndex ? k : k + 1;
    }

    /** A well-mixed number in [0, 1) from a string (FNV-1a hash, then SplitMix64 finalizer). */
    static double uniform(String key) {
        long h = 0xcbf29ce484222325L;
        for (byte b : key.getBytes(StandardCharsets.UTF_8)) {
            h ^= (b & 0xff);
            h *= 0x100000001b3L;
        }
        h += 0x9e3779b97f4a7c15L;
        h = (h ^ (h >>> 30)) * 0xbf58476d1ce4e5b9L;
        h = (h ^ (h >>> 27)) * 0x94d049bb133111ebL;
        h = h ^ (h >>> 31);
        return (h >>> 11) * 0x1.0p-53;
    }
}
