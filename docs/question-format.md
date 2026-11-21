# Question format (`final_questions.json`)

`src/main/resources/questions/final_questions.json` is a JSON array of questions:

```json
{
  "id": "set1-02",
  "section": "math",
  "difficulty": "medium",
  "stem": "If f(x) = x² - 4x, what is the value of f(-2)?",
  "choices": ["-12", "-4", "4", "12"],
  "correct_index": 3,
  "ai_answer_index": 3,
  "explanation_brief": "Substituting -2 gives ... = 12.",
  "explanation_detailed": "Step 1: ...\nStep 2: ...",
  "wrong_ai_answer_index": 0,
  "wrong_explanation_brief": "Substituting -2 gives ... = -12.",
  "wrong_explanation_detailed": "Step 1: ...\nStep 2: ...",
  "source": "SAT Suite Question Bank, ID abc123"
}
```

| Field | Notes |
|---|---|
| `id` | Unique. Referenced from `experiment.json` and `schedule.json`. Currently the College Board question ID. |
| `section`, `difficulty` | Free text, copied to the CSV for analysis. Use consistent values (`math` / `reading_writing`, `easy` / `medium` / `hard`). |
| `stem` | The question. `\n\n` starts a new paragraph (e.g. passage, then question). |
| `choices` | Displayed as A, B, C, D in this order. |
| `correct_index` | **0-based**: 0 = A, 1 = B, 2 = C, 3 = D. |
| `ai_answer_index` | The AI's suggestion when it is **right**. Keep it equal to `correct_index`; the app uses `correct_index` for right answers anyway. |
| `explanation_brief` | 1-2 sentences supporting the **right** AI answer. May be `""` until written; the app then shows only the AI's suggested answer. |
| `explanation_detailed` | Step-by-step reasoning supporting the **right** AI answer. May be `""` until written. |
| `wrong_ai_answer_index` | Only for questions the schedule makes wrong for someone: the incorrect option the AI suggests then. Must differ from `correct_index`. Omit it for questions that are never wrong. |
| `wrong_explanation_brief`, `wrong_explanation_detailed` | Same as the two fields above, but arguing for the **wrong** answer. Shown instead of them whenever the schedule makes the AI wrong. May be `""` until written. |
| `source` | Where it came from. Also add it to `question-sourcing.md`. |

Whether the AI is right or wrong on a question is decided **per participant** by `schedule.json`, not by this file.

## Writing the scripted AI

- When the AI is **wrong**, its explanation should still sound confident and plausible
  (a "confidently wrong" answer, e.g. one subtle arithmetic or grammar slip). It should never hint that it might be wrong.
- Brief and detailed explanations for the same question should argue for the **same** answer
  (the right pair for the correct answer, the wrong pair for `wrong_ai_answer_index`).
- Keep tone and length consistent across questions, so explanation level is the only thing that changes.
- Until the explanations are written, leave them as `""`; nothing extra is shown.

Run `mvn test` after editing. It catches out-of-range indexes, unknown IDs, and a question that the schedule
makes wrong but that has no `wrong_ai_answer_index`.
