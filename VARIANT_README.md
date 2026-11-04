# Experiment Variant (`JP-Experiment-Variant`)

Team 13 · CS464 · *How AI Explanations Influence Trust, Reliance, and Automation Bias*

This branch is built on top of `main` (Caleb's JavaFX app). It adds the **Suggestion Panel** helper the team picked from the browser prototypes, plus a few optional study controls. Every new option is optional in the code, so if a key is left out of the config, the app behaves like `main`.

**Aligned with team decisions:** the AI is scripted, so **every participant sees identical suggestions** (fixed mode), and the "Use this answer" button is off.

---

## How to run

Requirements: **JDK 17+** and **Maven**.

```bash
git fetch
git checkout JP-Experiment-Variant
mvn javafx:run
```

Running this shows the variant right away. The bundled `experiment.json` turns on the side card and the suggestion delay.

To go back to the main version:

```bash
git checkout main
mvn javafx:run
```

---

## Study specs (shared with `main`)

| Spec | Value |
|---|---|
| Task | SAT-style multiple choice (A–D) with a scripted AI helper that suggests an answer |
| IV 1 | Explanation level: none, brief (1 sentence), detailed (short worked explanation) |
| IV 2 | AI accuracy: share of correct suggestions in a round |
| Design | Within-subjects. Round 0 baseline (no explanation, AI 100% correct), then rounds 1–3 |
| Counterbalancing | 6 orders of the 3 levels, picked from the participant number ((n − 1) mod 6); 12 participants, 2 per order |
| Ratings | Confidence in self, confidence in AI, difficulty, estimated accuracy; 1–10; before and after every round; collected in the app |
| DVs | Score, followed AI (especially when wrong), response time, self-estimate vs. actual, confidence ratings |
| AI | Scripted, stored answers and explanations (not a live model) |
| Stack | Java 17 + JavaFX + Maven; questions in JSON, results in CSV |
| Session flow | Setup → welcome → baseline → rounds 1–3 (intro → pre ratings → questions → post ratings) → thank-you |

---

## What's different from `main`

| Area | `main` | This branch |
|---|---|---|
| Helper layout | Plain box under the answer choices | `side_card`: card beside the question with the letter, the choice text and a "Why" box (`box` still available) |
| "Use this answer" button | None | Optional; **off** (team doesn't need it) |
| Suggestion timing | Shown immediately | Optional delay with "Analyzing the question…" (900 ms in the bundled config) |
| Time limit | None | Optional per-question countdown (off in the bundled config) |
| How AI accuracy works | Fixed: each question has one stored AI answer | **Same as `main` (fixed)**; 2 of 6 wrong in rounds A–C. Per-participant draw modes exist but are not used |
| Wrong-answer explanations | One explanation per question | `explanations_by_choice`: an explanation for every letter the AI might suggest |
| Questions per round | 3 (placeholders) | 6 (placeholders) |
| CSV output | Base trial columns | Adds `ai_accuracy_percent`, `time_to_suggestion_ms`, `timed_out`, `used_answer_button` |
| Tests | Base suite | 27 tests, including `SuggestionDrawerTest` and a check that no round draws suggestions per participant |

---

## How AI accuracy is applied

**Used for the study: fixed mode.** No block sets `ai_accuracy_percent` or `ai_wrong_count`, so the AI suggests each question's stored `ai_answer_index`. Every participant sees **identical suggestions**, which matches the team decision and the report's Apparatus section.

- Baseline: all 6 AI answers correct (100%)
- Rounds A–C: 2 of 6 questions have a wrong AI answer (~67%). Change it by editing `ai_answer_index` in `questions.json`
- The explanation shown is the one written for the AI's letter (`explanations_by_choice`)

**Also in the code, but not used** (testing only):

- `ai_accuracy_percent`: each suggestion drawn per participant (correct with probability *p*). The number of wrong suggestions varies, and some participants get rounds with none
- `ai_wrong_count`: the same number wrong per round for everyone, but which questions are wrong varies per participant

A test (`bundledRoundsUseFixedSuggestionsForEveryone`) fails if a round in the bundled config switches to a draw mode.

---

## New config keys

**Top level (`experiment.json`)**

| Key | Default | Bundled | What it does |
|---|---|---|---|
| `helper_style` | `box` | `side_card` | Helper layout |
| `use_answer_button` | `false` | `false` | Adds the "Use this answer" button |
| `ai_delay_ms` | `0` | `900` | Pause before the suggestion appears |
| `time_limit_seconds` | `0` (no limit) | `0` | Per-question countdown; the answer is submitted when it hits 0 |

**Per round (block)**

| Key | What it does |
|---|---|
| `ai_accuracy_percent` | Per-participant draw (testing only). Leave it out for fixed mode |
| `ai_wrong_count` | Exact number wrong per round, positions drawn per participant (testing only) |

**Per question (`questions.json`)**

| Key | What it does |
|---|---|
| `explanations_by_choice` | Brief and detailed explanation for each letter the AI could suggest |

See `docs/config-format.md` and `docs/question-format.md` on this branch for the full format.

---

## New CSV columns

| Column | Example | Meaning |
|---|---|---|
| `ai_accuracy_percent` | `80` | The round's accuracy setting; empty in fixed mode |
| `time_to_suggestion_ms` | `900` | When the suggestion appeared, in ms after the question |
| `timed_out` | `false` | The time limit ran out before Submit |
| `used_answer_button` | `false` | The participant pressed "Use this answer" (always false while the button is off) |

`chosen_answer` is empty if the time limit ran out with nothing selected. Full list in `docs/data-dictionary.md`.

---

## Changed files

- **New:** `ChoiceExplanation`, `HelperStyle`, `PlannedTrial`, `SuggestionDrawer`, `SuggestionDrawerTest`
- **Updated:** session, config validation, question model, result writer, question screen, layout, styles, bundled `experiment.json` and `questions.json`, and the docs in `docs/`

---

## Decided

- Every participant sees identical AI suggestions (fixed mode)
- No "Use this answer" button

## Still to decide (team)

- Accuracy level in rounds A–C (currently 2 of 6 wrong ≈ 67%; ~70% and 80% were suggested)
- Questions per round
- Per-question time limit on or off
- Delay before the suggestion appears (`ai_delay_ms`, now 900 ms). Note: `response_time_ms` includes the wait
- Helper layout: `box` vs. `side_card`
