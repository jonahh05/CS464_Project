# Session config (`experiment.json`)

`src/main/resources/config/experiment.json` controls the session without code changes.

| Key | Meaning |
|---|---|
| `title` | Window title and setup screen heading |
| `output_dir` | Where CSVs are written, relative to where the app is started (`data`) |
| `rating_min`, `rating_max` | Rating scale (1-10) |
| `shuffle_questions_within_block` | `true` = shuffle each round's questions per participant (repeatable: seeded by participant ID) |
| `rating_items` | The ratings asked before (`pre_prompt`) and after (`post_prompt`) each round. `id` becomes `item_id` in the CSV. |
| `orders` | Counterbalancing orders of explanation levels. Order 1 is the first entry. |
| `blocks` | The rounds, in presentation order |
| `helper_style` | Optional. `box` (default): the helper box under the choices. `side_card`: a card beside the question with a letter badge, the choice text and a "Why" box, and the AI's pick outlined in the answer list. |
| `use_answer_button` | Optional, default `false`. `true` adds a "Use this answer" button to the helper that selects the AI's choice. Logged as `used_answer_button`. |
| `ai_delay_ms` | Optional, default `0`. Pause before the suggestion appears ("Analyzing the question…"). Logged as `time_to_suggestion_ms`. |
| `time_limit_seconds` | Optional, default `0` (no limit). Per-question countdown; when it hits 0 the answer is submitted as is and logged with `timed_out = true`. |

Leaving all four out gives the original behavior.

## Blocks

```json
{ "id": "baseline", "title": "Baseline", "instructions": "...",
  "explanation": "none",               // fixed level for everyone
  "question_ids": ["base-01", "base-02"] }

{ "id": "round_a", "title": "Round A", "instructions": "...",
  "order_slot": 0,                      // level = participant's order[0]
  "question_ids": ["set1-01", "set1-02"] }
```

Give each block **exactly one** of `explanation` (fixed) or `order_slot` (counterbalanced).
Every slot in the orders (0, 1, 2) must be used by exactly one block, and a question can only be in one block.
The app checks all of this at startup, and so does `mvn test`.

### AI accuracy per block (optional)

```json
{ "id": "round_a", ..., "order_slot": 0,
  "question_ids": ["set1-01", "set1-02"],
  "ai_accuracy_percent": 80 }
```

- **Without `ai_accuracy_percent`** (fixed mode): the AI suggests each question's `ai_answer_index`,
  so a block's accuracy is decided by which questions you put in it. Every participant sees the same suggestions.
- **With `ai_accuracy_percent`** (drawn mode, 0-100): for each question the AI suggests the correct choice
  with probability p and each wrong choice with probability (1 − p) / 3. At 80: correct 80%, each wrong choice 6.7%.
  - The draw is seeded from participant ID + block + question, so re-running the same participant ID gives
    exactly the same suggestions, and shuffling does not change them.
  - Different participants get different draws, so the **number** of wrong suggestions per round varies
    around (1 − p) × questions per round (at 80% and 6 questions: usually 0-3, average 1.2).
  - Every choice that can be drawn needs its own explanation (`explanations_by_choice`, see
    [question-format](question-format.md)). The validator lists any that are missing.
  - Set the baseline block to `100` so it stays 100% correct.

The console prints each participant's plan when a session starts, including each block's AI accuracy:

```
Participant P03, order 3
  block 0 baseline   explanation=none     questions=6 ai_accuracy=100% (drawn at 100%)
  block 1 round_a    explanation=brief    questions=6 ai_accuracy=83% (drawn at 80%)
  ...
```
