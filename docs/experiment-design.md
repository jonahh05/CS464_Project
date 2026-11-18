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
| AI accuracy | IV, per block | Implied by the questions: AI is wrong when `ai_answer_index != correct_index`. Currently every question has a correct AI answer. |
| Score / following wrong AI | DV | `correct`, `followed_ai`, `ai_correct` in the trials CSV |
| Response time | DV | `initial_response_time_ms` (to first Submit), `total_response_time_ms` (to second Submit) |
| Answer change after AI | DV | `switched_answer` (and `original_answer` vs `final_answer`) in the trials CSV |
| Self-reported accuracy | DV | `est_accuracy` rating (pre + post), compare with actual score |
| Confidence in self / AI, difficulty | DV | `conf_self`, `conf_ai`, `difficulty` ratings (pre + post) |

## Session structure

0. **Before the app**: the participant signs the separate Google consent form.
   **In the app**: the "About you" demographic questions (all optional), then Welcome.
1. **Nine sections** of four questions each, two `math` and two `reading_writing` (English), all from `final_questions.json`.
   Question order inside a section does not matter.
2. Each explanation level covers three sections: sections 1-3 use the participant's order[0], 4-6 use order[1], 7-9 use order[2].
   The **question sets stay at a fixed position**; only the explanation level rotates, so every question
   eventually needs both a brief and a detailed explanation (still empty for now; the app shows nothing extra for them).
3. Before and after every section: four 1-10 ratings, collected **in the app**.

## Counterbalancing

All 6 orders of (none, brief, detailed) are listed in `experiment.json`. The participant
number picks the order (P01 → 1 ... P06 → 6, P07 → 1 ...), so 12 participants give 2 per order.

## Changes from the proposal (note in the interim report)

- The between-round ratings are collected **in the experiment app** instead of Google Forms, so they
  share the participant ID and timestamps with the trial data.
- The demographic questions are also asked **in the app** (the "About you" screen before Welcome) and
  saved to the demographics CSV under the participant ID.
- Consent is a **separate Google Form** with a typed e-signature and date. It is the only place names are
  collected, and it never records the participant ID, so names are never linked to the study data.

## Open decisions

These can all be changed in the JSON config without code changes:

- [ ] Questions per round (teammate suggested 40-50 total → ~10 per round)
- [ ] AI accuracy in rounds A-C (e.g. 70%?) and **where** the wrong answers sit within a round
- [ ] Mix of sections (math vs. reading/writing) and difficulty per round. Keep the rounds balanced.
- [ ] Shuffle questions within a round (`shuffle_questions_within_block`), or keep a fixed order?
- [ ] Time limit per question? (not implemented; would go in `QuestionScreen`)
- [ ] Practice question before the baseline? (would be a new step in `ExperimentFlow`)
- [ ] Final rating wording (current text is a first draft)
- [ ] End-of-study questionnaire contents (prior AI use, end-of-study questions)
