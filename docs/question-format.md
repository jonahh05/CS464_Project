# Question format (`questions.json`)

`src/main/resources/questions/questions.json` is a JSON array of questions:

```json
{
  "id": "set1-02",
  "section": "math",
  "difficulty": "medium",
  "stem": "If f(x) = x² - 4x, what is the value of f(-2)?",
  "choices": ["-12", "-4", "4", "12"],
  "correct_index": 3,
  "ai_answer_index": 0,
  "explanation_brief": "Substituting -2 gives ... = -12.",
  "explanation_detailed": "Step 1: ...\nStep 2: ...",
  "source": "SAT Suite Question Bank, ID abc123"
}
```

| Field | Notes |
|---|---|
| `id` | Unique. Referenced from `experiment.json`. Convention: `base-NN`, `set1-NN`, `set2-NN`, `set3-NN`. |
| `section`, `difficulty` | Free text, copied to the CSV for analysis. Use consistent values (`math` / `reading_writing`, `easy` / `medium` / `hard`). |
| `stem` | The question. `\n\n` starts a new paragraph (e.g. passage, then question). |
| `choices` | Displayed as A, B, C, D in this order. |
| `correct_index` | **0-based**: 0 = A, 1 = B, 2 = C, 3 = D. |
| `ai_answer_index` | 0-based index the AI suggests. **Set it different from `correct_index` to make the AI wrong.** |
| `explanation_brief` | 1-2 sentences supporting the AI's answer. Required for questions in rounds A-C. |
| `explanation_detailed` | Step-by-step reasoning supporting the AI's answer. Required for questions in rounds A-C. |
| `source` | Where it came from. Also add it to `question-sourcing.md`. |

## Writing the scripted AI

- When the AI is **wrong**, its explanation should still sound confident and plausible
  (a "confidently wrong" answer, e.g. one subtle arithmetic or grammar slip). It should never hint that it might be wrong.
- Brief and detailed explanations for the same question should argue for the **same** answer.
- Keep tone and length consistent across questions, so explanation level is the only thing that changes.
- Baseline questions only need `""` for both explanations, since the baseline shows none.

Run `mvn test` after editing. It catches out-of-range indexes, missing explanations and unknown IDs.
