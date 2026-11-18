package cs464.experiment.model;

/**
 * One 1-10 rating asked before and after each block (e.g. confidence in the AI).
 * The wording differs before vs. after a round, so each item has two prompts.
 */
public record RatingItem(
        String id,          // column-friendly id written to the CSV, e.g. "conf_ai"
        String prePrompt,   // shown before the block's questions
        String postPrompt,  // shown after the block's questions
        Integer ratingMax   // optional: highest value for this item; null uses the global rating_max
) {
    public RatingItem(String id, String prePrompt, String postPrompt) {
        this(id, prePrompt, postPrompt, null);
    }

    public int maxOr(int defaultMax) {
        return ratingMax != null ? ratingMax : defaultMax;
    }

    public String promptFor(RatingPhase phase) {
        return phase == RatingPhase.PRE ? prePrompt : postPrompt;
    }
}
