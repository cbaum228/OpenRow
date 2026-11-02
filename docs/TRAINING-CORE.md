# OpenRow training core — proposed design

**Implementation update (October 29, 2026):** The foundation core, assessments,
adaptive duration policies, native controls and Gravity Well adapter are now
implemented. See [Training engine](TRAINING-ENGINE.md) for current behavior, exact
experimental defaults, validation boundaries and device acceptance checks. The
original proposal below remains design context; its unimplemented-status statements
describe the earlier version.

October 23, 2026. Design proposal, not an implemented training algorithm or an
individual exercise prescription. Personal goals and heart-rate hardware remain
open decisions. Initial scope: general adult fitness; clinical rehabilitation and
special-population programs require separate exercise-professional input.

## Product principle

OpenRow generates an appropriate workout for the person, then lets a compatible
game express that workout. Physical training and game progression have independent
records. An upgraded catapult does not change the player's prescribed effort.

The core must work without a game. Every game uses the same session runner,
workout history, effort targets, pause controls and feedback collection.

## Context from the other rowing chat

Read on October 23, 2026: **Game Ideas for Rowing App**. The chat reader exposed
the user's messages but returned content-reference placeholders for the assistant's
architecture replies. A browser fallback required login and did not expose the
conversation. The detailed prior architecture has therefore not been reconciled.

Confirmed preferences from the user's messages:

- Each game keeps independent progression; progress in one game should not change
  another game's progression.
- Shared trophies, major milestones and badges should recognize accomplishments
  across the app, alongside health and fitness progression.
- Expeditions can accumulate rowing toward the length of a chosen river, with
  milestone badges, similar in concept to the Garmin expeditions the user described.
- Power/energy or calorie milestones were ideas to explore, not validated metrics
  or committed requirements.
- The backend should tailor workouts to ability and drive game behavior to support
  athletic performance and conditioning.

Proposed extension pending access to the detailed architecture:

| Domain | Owns |
| --- | --- |
| Athlete data | Profile, real exercise observations, session history, assessments and derived fitness trends. |
| Training engine | Workout selection, targets, session execution and adaptation. |
| Game engine / individual games | Simulation, interaction, game-specific saves, upgrades and campaigns. |
| Challenges and expeditions | Cross-game goals, route milestones, trophies and eligibility rules based on recorded exercise. |

An expedition consumes eligible real-session distance from any supported game or
the ordinary rowing screen. It does not force a workout's duration or intensity.
One workout may advance an expedition and award a shared badge while changing only
the active game's campaign. Shared rewards must not transfer gameplay power between
games; any cosmetic unlock scheme remains a design decision.

Use stable session/event identifiers and versioned eligibility rules so reopening
results, reconnecting, or recomputing summaries cannot award distance twice. Retain
partial-session distance, reject simulated inputs, and define expedition start-date,
overlapping-goal and imported-session rules explicitly before implementing awards.
Energy or calorie challenges remain unavailable until their input and estimation
methods are validated and labeled. These data rules are new engineering proposals,
not recovered wording from the inaccessible replies.

## What exists today

`RowingMetrics` provides monitor distance/count and calculated cadence from stroke
events. `CampaignSession` scales charge distance and saves fort checkpoints.
Neither is an ability assessment or a fitness training engine.

The campaign's planning pace is manually entered. Its timer includes aiming, and
victory determines completion; misses can extend the distance. These are useful
game features but unsuitable as the authority for training dose.

Validated watts, instantaneous pace, heart rate, exercise history and adaptation
are not yet available. Empty-tank test rowing and simulated inputs must never
establish fitness baselines. Distance charging remains a game mechanic, not energy.

## Core components

| Component | Responsibility |
| --- | --- |
| AthleteProfile | Goals, recent activity, comfortable session duration, available days, preferred games, relevant restrictions, optional sensors and benchmark references. |
| TelemetryAdapter | Timestamped observations with units, source, quality and freshness; distinguish observed, derived, missing and simulated values. |
| WorkoutPlan | Versioned sequence of warm-up, work, recovery and cool-down blocks with duration, effort guidance and supported optional pace/HR targets. |
| WorkoutRunner | Authoritative block clock, transitions, pause/stop, connection handling, actual dose and user modifications. Independent of render frames and game wins. |
| SessionRecord | Intended workout, actual timeline, rowing measurements, pauses, effort feedback, readiness, sensor quality, rule version and completion reason. |
| TrainingPlanner | Select the next suitable workout from reviewed templates using goals, recent sessions, availability and confidence in the profile. |
| AdaptationPolicy | Propose repeat, reduce or progress with an explanation and bounded changes; never infer ability from game success alone. |
| GameWorkoutAdapter | Translate workout phases and valid rowing into game actions; declare supported workout types. |

Keep domain models as pure Java, separately testable from Android/USB/Canvas.
Use local versioned storage with export and deletion. Save exercise progress during
a fort and after interruptions; game checkpoint granularity must not discard actual
training. Keep original samples, aggregates and later interpretations distinguishable.

## Establishing a starting point

1. Ask goals, recent activity, comfortable rowing duration, weekly availability and
   any exercise restrictions. Explain effort ratings in consistent language.
2. Offer a short familiarization session the player can shorten or stop. Start with
   comfortable self-selected effort, not a mandatory maximal test.
3. Record actual duration/distance, interruptions and perceived effort. If available,
   record reliable HR and pace without pretending to know physiological thresholds.
4. Use several ordinary sessions to establish a provisional baseline. Show that it
   is provisional; do not turn one good or bad day into a new fitness level.
5. Later offer repeatable submaximal benchmarks under comparable conditions and
   optionally performance tests for players whose goals and readiness support them.

Without HR, effort feedback and the talk test support relative intensity guidance.
CDC describes moderate effort as 5–6 on a 0–10 scale and the ability to talk but not
sing. Do not silently substitute a different RPE scale or call this a measured zone.
HR adds context; it does not justify automatic threshold detection, universal
age-derived zones, or conclusions about health from a single reading.

## Workout generation and adaptation

Begin with explainable rules and reviewed templates: familiarization, easy aerobic,
steady endurance, and controlled work/recovery sessions. Add threshold and harder
interval programming only after the measurements and progression rules are validated.

The planner considers total recent exercise, including activity outside OpenRow when
reported. It fits sessions around the player's available time and recovery rather
than creating a daily requirement to play. A missed session does not create a debt.

Before a session, collect a brief readiness response and allow an easier or shorter
option. Afterward, collect effort, whether the intended work was manageable, and
unusual fatigue or discomfort. Ask enough to make useful decisions without requiring
a questionnaire every time someone rows.

Proposed decision behavior:

- Repeated manageable sessions and acceptable recovery: consider a small increase
  in one variable, initially comfortable duration. Explain the evidence and change.
- Work substantially harder than intended, incomplete work, or poor recovery:
  repeat or reduce the next session; a sensor dropout alone is not a fitness decline.
- Illness, pain or concerning symptoms: stop progression and support stopping or
  deferring training; the game must not encourage pushing through them.
- Sparse/missing feedback or unreliable telemetry: retain uncertainty, avoid
  increasing intensity from assumed success, and offer a repeatable easy option.

Exact templates, review windows, change limits and harder-session eligibility need
exercise-professional review before automatic progression ships. Do not invent a
universal percentage increase or claim that a fixed rule guarantees recovery.
Log every recommendation with its inputs, policy version and reason so it can be
replayed, tested and corrected. Player overrides remain possible and are recorded.

Within a session, use stable targets and allow reduction. Do not continuously chase
HR with sharp pace changes or increase difficulty because the player is winning.

## Contract between workout and game

The core publishes block purpose, elapsed/remaining time, effort guidance, available
target ranges, valid observations, quality, and pause/finish events. Games publish
their own actions and outcomes; they cannot extend work blocks or delete recovery.

For Castlebreaker:

- Warm-up/easy blocks: low-pressure preparation, supply collection or easy targets.
- Work blocks: rowing winds the catapult; firing and aiming remain skill choices.
- Recovery: action can continue through aiming, projectile resolution, repairs or
  narration without demanding hard rowing. Honor active or passive recovery in the plan.
- A miss does not add required meters or delay recovery. A pending shot can remain
  available while a block ends. Fort state persists across workout boundaries.
- At session end, enter cool-down and save unfinished campaign progress. A player
  does not need to capture the final fort to complete the workout successfully.
- Reward following the intended effort, including easy days and recovery. Never
  award mandatory progression bonuses for exceeding the prescription.

A continuous-flight game can consume the same easy endurance block; Castlebreaker
can use several volleys during it. Match games to supported workouts rather than
forcing every game to turn every session into intervals.

## Measurement and timing rules

- Store wall-clock timestamps for history and monotonic time for elapsed durations.
  Track elapsed session time, block time, rowing time, recovery and interruptions
  separately. Existing campaign active time is not exercise time.
- Aiming time is not automatically rowing time. Absence of strokes is not proof of
  passive recovery if telemetry is missing. Record gaps explicitly.
- An explicit pause suspends the workout. A connection loss pauses game demands
  and prompts a resume decision; never replay missed work or credit stale distance.
- A normal stroke pause is not automatically an explicit session pause. Recovery
  clocks advance as planned unless the user pauses or the session is interrupted.
- Derive pace from distance over suitable time windows only after normal-water
  validation against the monitor. Coarse distance deltas must not imply precise
  instantaneous power. Reject resets/rollovers without losing prior workout totals.
- Keep cadence as a rhythm measure. Do not equate higher cadence with higher effort
  or diagnose technique from stroke-start events alone.
- Exclude simulated sessions from ability and training-load calculations. Retain
  legitimate partial real sessions even when their game encounter was unfinished.

## Showing meaningful progress

Show consistency, completed aerobic work, prescribed versus reported effort, and
comparable benchmark trends. With validated pace/HR, show changes such as more
distance at comparable reported effort or a repeatable task feeling easier. Label
comparability limits and avoid interpreting isolated changes as improvement.

Separate performance evidence from health outcomes. Do not invent a single health
score, inferred VO2 max, calories, disease-risk change or readiness score from the
current S4 signals. General health includes activity beyond rowing: CDC adult
guidance includes aerobic activity and muscle-strengthening work. Its weekly
recommendations are population goals, not automatic starting doses for every player.

## Implementation sequence

1. **Measurement and history:** workout-local recording, restart recovery, actual
   rowing time, quality markers, session effort feedback, local session list/export.
   Validate distance/pace in normal-water rowing. No automatic intensity escalation.
2. **Shared workout runner:** profile, familiarization flow, explicit block plans,
   independent timers, training display and Castlebreaker adapter. Complete a timed
   workout even if the fort remains standing. Preserve current free play.
3. **Conservative personalization:** reviewed template catalog and explainable
   next-session choices, provisional baselines, repeat/reduce decisions and reviewed
   progression bounds. Compare recommendations against recorded scenarios before use.
4. **Richer coaching:** optional HR integration, repeatable benchmark trends,
   goal-specific plans and additional game adapters as data quality permits.

Acceptance scenarios include reconnect without extra credit, game miss without extra
work, recovery without game pressure, easy-day rewards, early stop with retained
history, app restart during a fort, missing HR/RPE, simulated-session exclusion,
insufficient evidence preventing progression, and recommendation replay.

## Sources and review boundary

- [CDC: Measuring physical activity intensity](https://www.cdc.gov/physical-activity-basics/measuring/index.html)
  — relative intensity, effort ratings and talk test.
- [CDC: Adult activity overview](https://www.cdc.gov/physical-activity-basics/guidelines/adults.html)
  — aerobic and muscle-strengthening recommendations; some activity is better than none.
- [ACSM position stand: Quantity and quality of exercise (2011)](https://pubmed.ncbi.nlm.nih.gov/21694556/)
  — individualize programs to habitual activity, physical function, health status,
  responses and goals; gradual progression.

These sources ground the design principles. The component architecture, game
mapping and adaptation behavior above are engineering proposals, not clinically
validated algorithms. Review concrete exercise templates and progression policies
separately from reviewing software correctness.


## October 28 telemetry integration

Re-read **Game Ideas for Rowing App**: the newly accessible user messages explicitly
prefer S4-only support (no SmartRow dependency), describe cadence latency, and require
athlete distance, strokes, calories and energy to survive game prestige. Assistant
replies still appear as content-reference placeholders, so their exact architecture
was not available. The supplied S4 research in this chat is additional context.

S4Channels now keeps timestamped raw register observations and guarded normalized
values; RowingStats accounts for additional real exercise separately from game XP.
AthleteHistory retains its independent save and replay watermarks. The initial
bridge still runs during Gravity Well sessions; a game-independent workout recorder
and training runner remain necessary. Live inspection alone does not start a workout.
See S4-TELEMETRY.md for implemented channels, provisional units and validation.
