# Shared training engine — OpenRow 0.12.0

The foundation engine is implemented. Its exercise templates and adaptation defaults
are experimental product policies, **not clinically validated prescriptions**. Hardware
and exercise-professional validation remain separate from software test results.

## Using training

Open Gravity Well in real S4 mode and select **Training & assessments**. Choose a
goal (endurance or general cardio), available time, and either recommendations with
alternatives or Coach mode. Coach chooses the next session after the readiness
check-in; the athlete can review the full workout before starting.

Training uses duration and a consistent 0–10 perceived-effort scale. It does not
prescribe watts, heart-rate zones, measured VO₂ max, or age-derived thresholds.
Current sensor power and pace validation is still pending. Sprint and VO₂-focused
programming are future catalog additions; controlled intervals are not advertised
as maximal sprint or HIIT training.

Three optional, repeatable assessment paths are available:

| Path | Fixed protocol |
| --- | --- |
| Quick baseline | 5 minutes: 90 seconds easy warm-up, 90 comfortable work, 60 sustained work, 60 cool-down. |
| Full benchmark | 35 minutes: 5-minute warm-up; 10-minute comfortable work and 2-minute recovery; 6-minute sustained work and 2-minute recovery; four 1-minute controlled/1-minute recovery pairs; 2-minute cool-down. |
| Guided starter series | Three separate 13-minute sessions, one per category: 3-minute warm-up, 8-minute category work/recovery, 2-minute cool-down. Repeatability uses four 1-minute work/recovery pairs. |

Assessments are self-selected, submaximal, and provisional. The starter menu allows
athletes to choose or repeat any stage; it does not require consecutive days or a
calendar commitment. Normal workouts and optional free-row feedback refine the model.

The live workout HUD shows phase, remaining block time, and effort guidance. **Adjust
workout** pauses the session and offers easier effort, shortened work, longer paused
recovery, or an early stop. Recovery clocks otherwise advance even without rowing.
Warm-up, work, recovery and cool-down are authoritative: a boss or game encounter
cannot extend them. Gravity Well remains a low-pressure spectacle in every phase;
no harder-than-prescribed reward multiplier was added.

## Evidence and adaptation

The model separates demonstrated observations, current duration tolerance, readiness,
and evidence confidence. There is no single fitness level or automatic physiological
diagnosis. Untested categories are unknown rather than weak.

The planner balances category exposure relative to the goal, with twice the endurance
weight and half the repeatability weight for the endurance goal. Comparable benchmark
trends can modestly favor a category making less progress. These trends compare an
athlete against their own same-protocol, similar-effort observations, not population
norms. Limited evidence falls back to balanced comfortable foundation work.

Explicit software defaults in policy `foundation-1-experimental`:

- Available session lengths: 5–45 minutes; provisional starting duration: 10 minutes,
  capped by available time. The UI offers 5, 10, 15, 20, 30, and 45 minutes.
- Evidence window: 28 days; confidence reaches its current maximum at three usable
  category observations. History and records do not expire when confidence falls.
- Current tolerance: median of up to five recent manageable, completed sessions.
  Composite assessments contribute category work/recovery plus three minutes of
  foundation overhead, never their entire duration to every category.
- Three consecutive usable, manageable category observations can add at most one
  minute. Missing feedback on the latest session holds progression. Only duration
  automatically increases; controlled effort remains bounded by its template.
- Difficulty means unmanageable work or effort substantially above the prescribed
  ceiling. One difficult observation can reduce the next recommendation by two
  minutes; two recent difficult category observations lower current tolerance too.
  These changes do not overwrite historical benchmarks or records.
- Tired readiness selects shorter easy endurance work. Rest/defer produces no workout.
  A difficult session within two days also favors easy work.
- Effort-5 controlled repetitions require at least three recorded sessions and two
  manageable completed category observations, no recent uncertainty, no return pathway, and no
  controlled/high-reported-effort workout within two days. Otherwise repetitions
  stay at comfortable effort. No automatic within-session escalation occurs.
- Usable evidence requires optional feedback, at least 60 seconds of observed motion,
  and at most 20% missing telemetry. Work needs at least 70% observed motion coverage
  to establish completion-based capacity evidence. These are **data sufficiency
  heuristics**, not physiological safety thresholds.

After **more than 14 days** without recorded real activity, the next training start
asks a friendly return question. Maintain, ease back a little, ready for a little
more, and ease back a lot adjust the recommendation by 0, −2, +1, or −5 minutes,
respectively, subject to the 5-minute floor and available-time cap. Ordinary automatic
progression does not stack with the return increase. After more than 28 days away,
the first recommendation is capped at 10 minutes and retains provisional confidence.
Because of floors/caps, some choices can produce the same short session.

An assessment requested while tired or returning offers an easy session instead,
preserving the assessment's fixed protocol for another day. Missed workouts create
no debt. There is no outside-exercise logging. A prior real-workout date can seed
break detection during migration without inventing prior training evidence.

All constants and protocols above need exercise-professional review before this is
presented as validated automatic coaching. They are deliberately visible for review.

## Shared implementation contract

`TrainingCore` is pure Java. It owns the versioned `Plan`/`Block`, `State`, deterministic
planner, monotonic `Runner`, `Record`, and history codec. No Android, USB, Canvas,
network, or game dependency is required to generate or execute a workout.

`GameWorkoutAdapter` publishes an immutable frame: session ID, phase, effort/guidance,
elapsed/block-remaining time, pause/completion, telemetry availability, and recorded
distance/motion/missing-data totals. Games receive no clock or target mutation API.
The current effort/duration output is the minimum shared contract; quantitative
sensor target fields can be added under a new schema when those channels are validated.

`TrainingController` provides native tablet controls and a 250-ms scheduler independent
of rendering. `TrainingStore` atomically commits active checkpoints and all history in
`training-v1.properties`, separate from game state and lifetime achievements. Sessions
have stable UUIDs; repeated result commits replace the same record rather than adding
duplicate exercise. Plans, policy versions, check-in inputs, recommendation times,
block observations, pauses, adjustments and transition timelines are retained.

Gravity Well uses an untimed game session during prescribed training. The training
runner ends the workout regardless of game simulation lag or unfinished encounters.
The game retains its career, unfinished boss state, and ordinary rewards. Training
never multiplies measured distance, calories, energy or lifetime stats. Real free
rows are logged separately as self-selected endurance observations with optional
feedback; they never masquerade as fixed assessments.

Explicit pause, disconnection, stale distance-channel updates (over five seconds),
backgrounding, or save failure pauses training demands. Restart always restores the
runner paused, resets monotonic timing, and records interrupted assessments. Reconnect
requires resuming; stale distance baselines cannot grant extra meters. Saves occur
every three seconds and at adjustments/pause/completion; a process kill can lose the
unsaved tail, but cannot invent offline work. The game file and training file are
individually atomic, not a cross-file transaction; their independently durable
observations must not be summed as duplicate sources of athlete totals.

Progress charts distinguish current tolerance estimates from raw benchmark observations.
Comparisons use the same protocol and reported effort within one point. Paused,
modified, incomplete, simulated, or poorly observed assessments stay in history but
are excluded from benchmark comparisons. Rower setup and environmental comparability
remain athlete instructions, not verified claims.

Export uses the Android file picker to save a versioned properties file containing
completed training history and recommendation context. It excludes the active
checkpoint and raw USB journals. Training deletion has an explicit confirmation and
is available after the current session has been reviewed; it leaves lifetime rowing
totals, achievements and game careers intact. Existing athlete reset behavior is unchanged.

## Verification and remaining device checks

Run `./test.ps1` for deterministic training scenarios and a real Gravity Well model
integration test, alongside the existing sensor, history and game suites. Run
`./build.ps1` for the signed Android APK. No new runtime dependency is required.

Tablet acceptance checks still required:

1. Start a short real workout, verify phase/time guidance, pause/resume, reduction,
   shortened work, recovery extension and early-stop feedback.
2. Unplug/reconnect the S4, let its channel go stale, background the app, and restart
   during a workout. Verify frozen demands, no offline credit, and explicit resume.
3. Complete each assessment path; repeat a protocol and inspect category results,
   including an interrupted assessment that must not be compared.
4. Finish a normal free row, skip or submit feedback, and verify a single history entry.
5. Export through the tablet's document picker and verify deletion boundaries.
6. Validate S4 readings and motion coverage while rowing with normal water. Software
   replay tests do not establish physiological appropriateness or hardware accuracy.


## 0.12.1 - athlete-relative game artwork

The trainer's immutable game frame now includes category and a display-only recent observed-speed reference with sample count. Gravity Well uses the shared TrainingVisuals adapter for personal visual normalization, provisional phase-guided presentation, and explicit warm-up/recovery/cool-down ceilings. Overspeed cannot brighten guided artwork beyond the current prescription. The game physics/economy and trainer timing/policy remain separate. See [Trainer visual integration](TRAINER-VISUAL-INTEGRATION.md) for evidence rules, boundaries and tablet demo.
