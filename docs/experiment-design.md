# Experiment design

How the proposal maps onto the software. Keep this in sync with the
"Updated Project Items" section of the interim report.

## Research questions & hypotheses (from the proposal)

- **RQ1 / H1**: More explanation means more confidence in the AI's suggestions.
- **RQ2 / H2**: Automation bias grows over the session. Is this affected by explanation level or AI accuracy?
- **RQ3 / H3**: The participants most confident in the AI are worst at judging their own accuracy.

## Variables

| Variable | Type | How the software implements it |
|---|---|---|
| Explanation level (none / brief / detailed) | IV, within-subjects | `explanation` / `order_slot` per block in `experiment.json`; text from `explanation_brief` / `explanation_detailed` |
| AI accuracy | IV, per block | **Used for the study: fixed** (AI is wrong when `ai_answer_index != correct_index`; identical for every participant; currently 2 of 6 wrong in rounds A-C). Also supported but not used, set on the block: `ai_accuracy_percent` (each suggestion drawn, correct with probability p, each wrong choice (1 − p)/3 for 4 choices) or `ai_wrong_count` (exactly k wrong per round for everyone), seeded by participant ID. Baseline = 100%. See [config-format](config-format.md#ai-accuracy-per-block-optional). |
| Score / following wrong AI | DV | `correct`, `followed_ai`, `ai_correct` in the trials CSV |
| Response time | DV | `response_time_ms` (question shown until Submit) |
| Self-reported accuracy | DV | `est_accuracy` rating (pre + post), compare with actual score |
| Confidence in self / AI, difficulty | DV | `conf_self`, `conf_ai`, `difficulty` ratings (pre + post) |

## Session structure

1. **Baseline round**: no explanation, AI always correct. Same for everyone.
2. **Rounds A, B, C**: each uses one explanation level, in the participant's counterbalanced order.
   The **question sets stay at a fixed position** (round A always has set 1, etc.); only the
   explanation level rotates. So every question in rounds A-C needs both a brief and a detailed explanation.
3. Before and after every round: four 1-10 ratings, collected **in the app**.

## Counterbalancing

All 6 orders of (none, brief, detailed) are listed in `experiment.json`. The participant
number picks the order (P01 → 1 ... P06 → 6, P07 → 1 ...), so 12 participants give 2 per order.

## Changes from the proposal (note in the interim report)

- The between-round ratings are collected **in the experiment app** instead of Google Forms, so they
  share the participant ID and timestamps with the trial data. Google Forms is still used for
  consent, demographics and any end-of-study questions.

## Open decisions

These can all be changed in the JSON config without code changes:

- [ ] Questions per round (teammate suggested 40-50 total → ~10 per round)
- [x] **How** AI accuracy is applied: fixed wrong questions, the same for everyone (matches the team decision
      that every participant sees identical suggestions). The per-participant draws are not used.
- [ ] AI accuracy **level** in rounds A-C (currently 2 of 6 wrong ≈ 67%; teammates suggested ~70% / 80%)
- [ ] Mix of sections (math vs. reading/writing) and difficulty per round. Keep the rounds balanced.
- [ ] Shuffle questions within a round (`shuffle_questions_within_block`), or keep a fixed order?
- [ ] Time limit per question? (`time_limit_seconds`, off by default)
- [ ] Helper look: `box` or `side_card`? (bundled: `side_card`)
- [x] "Use this answer" button: dropped (`use_answer_button: false`)
- [ ] Delay before the suggestion appears (`ai_delay_ms`, bundled 900)? Note `response_time_ms` includes the wait; subtract `time_to_suggestion_ms`.
- [ ] Practice question before the baseline? (would be a new step in `ExperimentFlow`)
- [ ] Final rating wording (current text is a first draft)
- [ ] Google Form contents: consent, demographics, prior AI use, end-of-study questions
