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

The console prints each participant's plan when a session starts, including each block's AI accuracy:

```
Participant P03, order 3
  block 0 baseline   explanation=none     questions=3 ai_accuracy=100%
  block 1 round_a    explanation=brief    questions=3 ai_accuracy=67%
  ...
```
