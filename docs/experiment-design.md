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
| AI accuracy (50% / 75% / 100%) | IV, per section, within-subjects | Set per participant and section by `schedule.json`: it says for every question whether the AI is right or wrong. A wrong AI suggests `wrong_ai_answer_index` and shows the `wrong_explanation_*` text. Logged as `ai_accuracy` (and per trial as `ai_correct`) in the CSVs. |
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
   Questions the schedule makes wrong for someone also need the `wrong_explanation_*` pair.
3. Within each group of three sections, one has a 50% accurate AI, one 75% and one 100% (see below).
4. Before and after every section: four 1-10 ratings, collected **in the app**.

## AI accuracy

`schedule.json` is the single source of truth. For each participant it lists the explanation order, the AI accuracy of
each section, and, for every question, whether the AI is right (`ai_correct`). At session start the app applies the
participant's row: right = the AI suggests `correct_index`, wrong = it suggests `wrong_ai_answer_index`
and shows the wrong-answer explanations. The same question is therefore right for some participants and wrong for others.

Rules the schedule must satisfy (checked by `ScheduleValidator` at startup and by `mvn test`):

- each group of three sections has exactly one 50%, one 75% and one 100% section;
- no section has two wrong math or two wrong English questions;
- a 50% section has one wrong math and one wrong English question; a 75% section has one wrong question; a 100% section has none;
- section question sets match `experiment.json`, and each section's explanation level matches the participant's order;
- every question that is ever wrong has a valid `wrong_ai_answer_index`.

## Counterbalancing and participants

The study has **exactly six participants, P1-P6. This is a fixed rule and will not change.**
Schedule levels map as E1 = none, E2 = brief, E3 = detailed, and the six participants cover
all 6 orders of (none, brief, detailed) once each:

| Participant | Order (E-codes) | Order (levels) | `order_id` in `experiment.json` |
|---|---|---|---|
| P1 | E3, E1, E2 | detailed, none, brief | 5 |
| P2 | E2, E1, E3 | brief, none, detailed | 3 |
| P3 | E3, E2, E1 | detailed, brief, none | 6 |
| P4 | E1, E2, E3 | none, brief, detailed | 1 |
| P5 | E1, E3, E2 | none, detailed, brief | 2 |
| P6 | E2, E3, E1 | brief, detailed, none | 4 |

The setup screen picks the schedule participant from the number in the participant ID (P03 → P3). The experimenter
can pick a different one, e.g. for a pilot run; `order_id` is then derived from that schedule row.

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
- [x] AI accuracy per section and **where** the wrong answers sit: decided by `schedule.json` (50% / 75% / 100%)
- [ ] Mix of sections (math vs. reading/writing) and difficulty per round. Keep the rounds balanced.
- [ ] Shuffle questions within a round (`shuffle_questions_within_block`), or keep a fixed order?
- [ ] Time limit per question? (not implemented; would go in `QuestionScreen`)
- [ ] Practice question before the baseline? (would be a new step in `ExperimentFlow`)
- [ ] Final rating wording (current text is a first draft)
- [ ] End-of-study questionnaire contents (prior AI use, end-of-study questions)
