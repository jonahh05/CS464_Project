package cs464.experiment.session;

/**
 * Picks a default counterbalancing order from the participant ID.
 *
 * <p>The number in the ID is mapped onto the orders in turn: with 6 orders,
 * P01 -> order 1, P02 -> order 2, ... P06 -> order 6, P07 -> order 1 again.
 * Twelve participants (P01-P12) therefore give each order exactly twice.
 * The experimenter can still override the order on the setup screen.
 */
public final class OrderAssigner {

    private OrderAssigner() {
    }

    /**
     * @return a 1-based order ID, or 1 if the ID contains no number
     */
    public static int defaultOrderFor(String participantId, int numberOfOrders) {
        if (numberOfOrders <= 0) {
            throw new IllegalArgumentException("There must be at least one order");
        }
        String digits = participantId == null ? "" : participantId.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return 1;
        }
        // Use the last few digits only, so very long IDs can't overflow an int.
        if (digits.length() > 6) {
            digits = digits.substring(digits.length() - 6);
        }
        int number = Integer.parseInt(digits);
        if (number <= 0) {
            return 1;
        }
        return ((number - 1) % numberOfOrders) + 1;
    }
}
