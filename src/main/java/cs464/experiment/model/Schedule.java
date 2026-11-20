package cs464.experiment.model;

import java.util.List;
import java.util.Optional;

/**
 * The fixed plan for the six participants, loaded from src/main/resources/questions/schedule.json
 * (see docs/experiment-design.md). It says, for each participant and section, which explanation level
 * applies and for which questions the AI helper is right or wrong.
 */
public record Schedule(long seed, List<String> levels, List<Participant> participants) {

    public record Participant(
            String participant,                 // "P1" .. "P6"
            List<String> explainabilityOrder,   // e.g. ["E3", "E1", "E2"]
            List<Integer> accuracyBySection,    // target AI accuracy (50/75/100) for sections 1-9
            List<Section> sections
    ) {
        /** The number in the name, e.g. 3 for "P3". */
        public int number() {
            return Integer.parseInt(participant.replaceAll("\\D", ""));
        }

        /** Explanation levels in the order this participant sees them. */
        public List<ExplanationLevel> explanationOrder() {
            return explainabilityOrder.stream().map(Schedule::levelFor).toList();
        }

        public Optional<Section> section(int sectionNumber) {
            return sections.stream().filter(s -> s.section() == sectionNumber).findFirst();
        }
    }

    public record Section(
            int section,                // 1-based
            int phase,                  // 1-based group of three sections sharing an explanation level
            String explainability,
            int accuracy,               // share of questions where the AI is right (50, 75 or 100)
            List<ScheduledQuestion> questions
    ) {
    }

    public record ScheduledQuestion(int position, String id, boolean aiCorrect) {
    }

    /** E1 = no explanation, E2 = brief, E3 = detailed. */
    public static ExplanationLevel levelFor(String code) {
        switch (code) {
            case "E1":
                return ExplanationLevel.NONE;
            case "E2":
                return ExplanationLevel.BRIEF;
            case "E3":
                return ExplanationLevel.DETAILED;
            default:
                throw new IllegalArgumentException("Unknown explainability level '" + code + "' (expected E1, E2 or E3)");
        }
    }

    /** The schedule participant with this number (P1 -> 1), if there is one. */
    public Optional<Participant> participantNumber(int number) {
        return participants.stream().filter(p -> p.number() == number).findFirst();
    }

    /**
     * Picks the schedule participant that matches the number in a participant ID ("P03" -> P3).
     * IDs without a number from 1 to 6 get the first participant; the experimenter can change it.
     */
    public Participant defaultFor(String participantId) {
        String digits = participantId == null ? "" : participantId.replaceAll("\\D", "");
        if (!digits.isEmpty() && digits.length() <= 6) {
            Optional<Participant> match = participantNumber(Integer.parseInt(digits));
            if (match.isPresent()) {
                return match.get();
            }
        }
        return participants.get(0);
    }
}
