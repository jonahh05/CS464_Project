# CS464 Team 13 — AI Explanations, Trust & Automation Bias

Experiment software for our study on how the **level of explanation** (none / brief / detailed)
from an AI helper affects trust, reliance and self-assessment on SAT-style questions.

The study design is in [`docs/experiment-design.md`](docs/experiment-design.md).

## Quick start

Requirements: **JDK 17+** and **Maven**.

```bash
mvn javafx:run      # launch the experiment
mvn test            # check the code and validate the JSON files
mvn package         # build target/experiment.jar  ->  java -jar target/experiment.jar
```

Results go to `data/` (gitignored). Each session writes `<ID>_<timestamp>_demographics.csv`,
`<ID>_<timestamp>_trials.csv` and `<ID>_<timestamp>_ratings.csv`.

## What a session looks like

```
(consent: signed on the separate Google consent form before the app starts)
Setup (experimenter)  ->  About you (optional demographic questions)  ->  Welcome
  -> Sections 1-9 (4 questions each: 2 math + 2 English; sections 1-3, 4-6 and 7-9 each use one
     explanation level, in the participant's counterbalanced none / brief / detailed order)
       each section = intro -> pre-section ratings -> questions -> post-section ratings
  -> Thank you
```

### Two-step answering

Each question is answered in two steps. The participant selects and submits an **original answer**
with no AI suggestion visible. The AI helper's suggestion (plus any explanation) is then revealed, and
the participant may keep or change their selection and submit again to give a **final answer**. The
app does not advance until the second submit. These are recorded as `original_answer` and
`final_answer` in the trials CSV (see [data-dictionary](docs/data-dictionary.md)); the logic lives in
`TwoStepAnswer` and is covered by `TwoStepAnswerTest`.

The order (1-6) is picked from the participant ID: P01 gets 1, P02 gets 2, ... P07 gets 1 again.
That gives 2 participants per order for P01-P12. The experimenter can override it on the setup screen.

### Participant numbers and the pilot study

The setup screen fills in the next participant ID for you: one more than the highest number already
saved in `data/` on this laptop (P01 if none). You can still type a different ID.

The banner at the top shows the status. **TESTING** means sessions are practice runs. Before the first
real participant, press **Start pilot study**: every saved session is moved to `data/archive/` (nothing
is deleted), numbering starts again, and the banner switches to **PILOT STUDY** with the number of
participants saved on this laptop. The status is kept in `data/study_status.properties`.

Each of us runs sessions on our own laptop, so pick a different **first participant number** for each
laptop when you press the button (for example 1, 3, 5) so no two participants share an ID.

## Common changes and where to make them

| I want to...                                   | Edit                                               | Docs |
|------------------------------------------------|----------------------------------------------------|------|
| Add / replace questions, write AI explanations | `src/main/resources/questions/final_questions.json`| [question-format](docs/question-format.md) |
| Change which questions go in which section       | `src/main/resources/config/experiment.json`        | [config-format](docs/config-format.md) |
| Change rating wording, scale, orders           | `src/main/resources/config/experiment.json`        | [config-format](docs/config-format.md) |
| Change how a screen looks                      | `src/main/java/.../ui/*Screen.java`, `styles/app.css` | — |
| Add a step to the session (practice, break)    | `src/main/java/.../ui/ExperimentFlow.java`         | — |
| Add a column to the output                     | `io/ResultWriter.java` **and** `docs/data-dictionary.md` | [data-dictionary](docs/data-dictionary.md) |

Run `mvn test` after editing the JSON files. It reports problems like a missing question ID or an unknown order slot.
The app also refuses to start and lists the problems.

## Project layout

```
pom.xml                         Maven build (JavaFX 17, Gson, JUnit 5)
src/main/java/cs464/experiment/
  App.java                      entry point: load + validate JSON, show setup screen
  Launcher.java                 main class for the runnable jar (just calls App)
  model/                        plain data records: Question, ExperimentConfig, TrialResult, ...
  io/                           JsonLoader (reads JSON), ResultWriter (writes CSV)
  session/                      experiment logic, no UI: counterbalancing, validation
  ui/                           one class per screen + ExperimentFlow (screen order)
src/main/resources/
  questions/final_questions.json question bank + scripted AI answers/explanations
  config/experiment.json        sections, orders, rating items
  styles/app.css                look and feel
src/test/java/                  unit tests (logic + bundled JSON validity)
docs/                           design, formats, data dictionary, question sourcing
analysis/                       analysis scripts (later)
data/                           participant output (gitignored)
```

**Rule of thumb:** `session/` and `io/` know nothing about JavaFX, and `ui/` holds no
experiment logic. That means anyone can redesign a screen without touching data collection.

## Status

The bundled `final_questions.json` holds 36 questions (18 math, 18 English) in 9 sections of 4.
The brief and detailed explanations are still empty strings, so the AI helper shows only its suggested answer. See [`docs/experiment-design.md`](docs/experiment-design.md#open-decisions)
for what the team still needs to decide.
