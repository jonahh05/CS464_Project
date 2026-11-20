# Session config (`experiment.json`)

`src/main/resources/config/experiment.json` controls the session without code changes.

| Key | Meaning |
|---|---|
| `title` | Window title and setup screen heading |
| `output_dir` | Where CSVs are written, relative to where the app is started (`data`) |
| `rating_min`, `rating_max` | Rating scale (1-10) |
| `shuffle_questions_within_block` | `true` = shuffle each round's questions per participant (repeatable: seeded by participant ID) |
| `demographic_questions` | The optional "About you" questions on the first participant screen, before Welcome. Leave it out to skip that screen. See below. |
| `rating_items` | The ratings asked before (`pre_prompt`) and after (`post_prompt`) each round. `id` becomes `item_id` in the CSV. |
| `orders` | Counterbalancing orders of explanation levels. Order 1 is the first entry. Each participant's row in `schedule.json` selects one; the six participants use all six. |
| `blocks` | The rounds, in presentation order |

## Demographic questions

```json
{ "id": "gender", "prompt": "What is your gender?",
  "choices": ["Woman", "Man", "Nonbinary", "Prefer not to say"],
  "allow_other": true }                 // adds "Other:" with a text box

{ "id": "major", "prompt": "What is your major or field of study?" }   // no choices = text box
```

Every question is optional, so give choice questions a "Prefer not to say" option.
`id` becomes `item_id` in the demographics CSV. Never ask for a name here: names go only on the
separate Google consent form, so they are never stored with the study data.

## Blocks

```json
{ "id": "section_1", "title": "Section 1", "instructions": "...",
  "order_slot": 0,                      // level = participant's order[0]
  "question_ids": ["3d1070c9", "fa80893a", "87aa7bab", "8c1be131"] }

// A block can instead use a fixed level for everyone:  "explanation": "none"
```

Give each block **exactly one** of `explanation` (fixed) or `order_slot` (counterbalanced).
The bundled config has 9 blocks of 4 questions (2 `math` + 2 `reading_writing`); sections 1-3 use slot 0, 4-6 slot 1, 7-9 slot 2.
Several blocks may share a slot, but every slot in the orders (0, 1, 2) must be used by at least one block, and a question can only be in one block.
The app checks all of this at startup, and so does `mvn test`.

The console prints each participant's plan when a session starts, including each block's AI accuracy (from `schedule.json`, applied to the questions):

```
Participant P03, order 3
  block 0 section_1  explanation=brief    questions=4 ai_accuracy=100%
  block 1 section_2  explanation=brief    questions=4 ai_accuracy=75%
  ...
```
