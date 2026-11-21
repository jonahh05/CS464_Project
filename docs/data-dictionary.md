# Data dictionary

Each session writes three CSV files to `data/`. Timestamps are ISO-8601 UTC.
If you add or rename a column in `io/ResultWriter.java`, update this file in the same PR.

## `<ID>_<timestamp>_demographics.csv`: one row per "About you" question

| Column | Example | Meaning |
|---|---|---|
| `participant_id`, `order_id` | `P01`, `1` | Same as in the trials file |
| `item_id` | `education` | Question id from `demographic_questions` in `experiment.json` |
| `answer` | `Undergraduate student` | The chosen option, `Other: <text>` for the Other box, the typed text for a text question, or empty if skipped |
| `timestamp` | | When the About you page was submitted |

No names are stored here. Consent (name and e-signature) is collected on a separate Google Form
that never records the participant ID.

## `<ID>_<timestamp>_trials.csv`: one row per answered question

| Column | Example | Meaning |
|---|---|---|
| `participant_id` | `P01` | Entered on the setup screen |
| `order_id` | `3` | Counterbalancing order (1-6), see `orders` in `experiment.json`. Determined by the participant's row in `schedule.json`. |
| `block_index` | `1` | 0-8 in presentation order (section 1 = 0) |
| `block_id` | `round_a` | Block id from `experiment.json` |
| `explanation_level` | `brief` | `none`, `brief` or `detailed` shown in this block |
| `ai_accuracy` | `75` | Share (%) of this block's questions where the AI suggestion was correct for this participant: 50, 75 or 100. Computed from the questions the participant actually saw, so it always agrees with `ai_correct`. |
| `trial_index` | `0` | Position of the question within the block (0-based) |
| `question_id` | `set1-02` | Question id from `questions.json` |
| `section` | `math` | From `questions.json` |
| `difficulty` | `medium` | From `questions.json` |
| `ai_suggestion` | `A` | Letter the AI suggested |
| `ai_correct` | `false` | Was the AI's suggestion correct **for this participant**? The same question can be right for one participant and wrong for another (see `schedule.json`). |
| `original_answer` | `B` | Letter submitted the first time, before the AI suggestion was shown |
| `final_answer` | `A` | Letter submitted the second time, after seeing the AI suggestion (same as `original_answer` if kept) |
| `correct_answer` | `D` | The correct letter |
| `correct` | `false` | Was `final_answer` correct? |
| `followed_ai` | `true` | Did `final_answer` match the AI's suggestion? |
| `switched_answer` | `true` | `true` if `original_answer` differs from `final_answer`, else `false` |
| `initial_response_time_ms` | `8421` | From question shown to the first Submit (original answer) |
| `total_response_time_ms` | `15230` | From question shown to the second Submit (final answer) |
| `timestamp` | `2026-11-01T18:03:12.512Z` | When the second Submit (final answer) was pressed |

**Automation bias** = rows where `ai_correct == false`, then look at the share with `followed_ai == true`.
`switched_answer` shows whether the AI changed the participant's mind.

## `<ID>_<timestamp>_ratings.csv`: one row per rating item

| Column | Example | Meaning |
|---|---|---|
| `participant_id`, `order_id`, `block_index`, `block_id`, `explanation_level`, `ai_accuracy` | | Same as above |
| `phase` | `pre` | `pre` (before the block's questions) or `post` (after) |
| `item_id` | `conf_ai` | Rating item id from `experiment.json` (`conf_self`, `conf_ai`, `difficulty`, `est_accuracy`) |
| `value` | `7` | Chosen value on the 1-10 scale |
| `timestamp` | | When the ratings page was submitted |
