# Data dictionary

Each session writes two CSV files to `data/`. Timestamps are ISO-8601 UTC.
If you add or rename a column in `io/ResultWriter.java`, update this file in the same PR.

## `<ID>_<timestamp>_trials.csv`: one row per answered question

| Column | Example | Meaning |
|---|---|---|
| `participant_id` | `P01` | Entered on the setup screen |
| `order_id` | `3` | Counterbalancing order (1-6), see `orders` in `experiment.json` |
| `block_index` | `1` | 0 = baseline, then 1, 2, 3 in presentation order |
| `block_id` | `round_a` | Block id from `experiment.json` |
| `explanation_level` | `brief` | `none`, `brief` or `detailed` shown in this block |
| `trial_index` | `0` | Position of the question within the block (0-based) |
| `question_id` | `set1-02` | Question id from `questions.json` |
| `section` | `math` | From `questions.json` |
| `difficulty` | `medium` | From `questions.json` |
| `ai_suggestion` | `A` | Letter the AI suggested to this participant (drawn per participant when the block sets `ai_accuracy_percent`) |
| `ai_correct` | `false` | Was the AI's suggestion correct? |
| `chosen_answer` | `A` | Letter the participant submitted; empty if the time limit ran out with nothing selected |
| `correct_answer` | `D` | The correct letter |
| `correct` | `false` | Did the participant answer correctly? |
| `followed_ai` | `true` | Did the participant pick the AI's suggestion? |
| `response_time_ms` | `8421` | From question shown to Submit pressed |
| `timestamp` | `2026-11-01T18:03:12.512Z` | When Submit was pressed |
| `ai_accuracy_percent` | `80` | The block's `ai_accuracy_percent` setting; empty in fixed mode |
| `time_to_suggestion_ms` | `900` | When the suggestion appeared, ms after the question (`ai_delay_ms`); `-1` if they submitted before it appeared |
| `timed_out` | `false` | The time limit ran out before Submit |
| `used_answer_button` | `true` | They pressed "Use this answer" at least once on this question |

The last four columns were added at the end so the original columns keep their positions.

**Automation bias** = rows where `ai_correct == false`, then look at the share with `followed_ai == true`.

## `<ID>_<timestamp>_ratings.csv`: one row per rating item

| Column | Example | Meaning |
|---|---|---|
| `participant_id`, `order_id`, `block_index`, `block_id`, `explanation_level` | | Same as above |
| `phase` | `pre` | `pre` (before the block's questions) or `post` (after) |
| `item_id` | `conf_ai` | Rating item id from `experiment.json` (`conf_self`, `conf_ai`, `difficulty`, `est_accuracy`) |
| `value` | `7` | Chosen value on the 1-10 scale |
| `timestamp` | | When the ratings page was submitted |
