# CS464 Team 13 — AI Explanations, Trust & Automation Bias

Experiment software for our study on how the **level of explanation** (none / brief / detailed)
from an AI helper affects trust, reliance and self-assessment on SAT-style questions.

The study design is in [`docs/experiment-design.md`](docs/experiment-design.md).

## Quick start

Requirements: **JDK 17+** and **Maven** (same setup as Assignment 2).

```bash
mvn javafx:run      # launch the experiment
mvn test            # check the code and validate the JSON files
mvn package         # build target/experiment.jar  ->  java -jar target/experiment.jar
```

Build the jar on the laptop that will run the study, because JavaFX ships separate jars for each OS.
Results go to `data/` (gitignored). Each session writes `<ID>_<timestamp>_trials.csv` and
`<ID>_<timestamp>_ratings.csv`.

## What a session looks like

```
Setup (experimenter)  ->  Welcome
  -> Round 1 (baseline: no explanation, AI 100% correct)
  -> Round 2, 3, 4 (none / brief / detailed in the participant's counterbalanced order)
       each round = intro -> pre-round ratings -> questions -> post-round ratings
  -> Thank you
```

The order (1-6) is picked from the participant ID: P01 gets 1, P02 gets 2, ... P07 gets 1 again.
That gives 2 participants per order for P01-P12. The experimenter can override it on the setup screen.

## Common changes and where to make them

| I want to...                                   | Edit                                               | Docs |
|------------------------------------------------|----------------------------------------------------|------|
| Add / replace questions, write AI explanations | `src/main/resources/questions/questions.json`      | [question-format](docs/question-format.md) |
| Change which questions go in which round       | `src/main/resources/config/experiment.json`        | [config-format](docs/config-format.md) |
| Change rating wording, scale, orders           | `src/main/resources/config/experiment.json`        | [config-format](docs/config-format.md) |
| Change how a screen looks                      | `src/main/java/.../ui/*Screen.java`, `styles/app.css` | — |
| Add a step to the session (practice, break)    | `src/main/java/.../ui/ExperimentFlow.java`         | — |
| Add a column to the output                     | `io/ResultWriter.java` **and** `docs/data-dictionary.md` | [data-dictionary](docs/data-dictionary.md) |

Run `mvn test` after editing the JSON files. It reports problems like a missing question ID or explanation.
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
  questions/questions.json      question bank + scripted AI answers/explanations
  config/experiment.json        rounds, orders, rating items
  styles/app.css                look and feel
src/test/java/                  unit tests (logic + bundled JSON validity)
docs/                           design, formats, data dictionary, question sourcing
analysis/                       analysis scripts (later)
data/                           participant output (gitignored)
```

**Rule of thumb:** `session/` and `io/` know nothing about JavaFX, and `ui/` holds no
experiment logic. That means anyone can redesign a screen without touching data collection.

## Status

The current `questions.json` contains **12 placeholder questions** (3 per round) so the
whole flow can be run and demoed. See [`docs/experiment-design.md`](docs/experiment-design.md#open-decisions)
for what the team still needs to decide.
