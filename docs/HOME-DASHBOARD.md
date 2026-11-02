# OpenRow home dashboard — 0.8.0

The Android launcher now has a custom OpenRow oars icon. On first launch the app
requests a pinned home-screen shortcut; the dashboard also offers **Home-screen
icon** to retry. Both the app icon and pinned shortcut open the dashboard. Opening
the shortcut while a game is running pauses/saves that game and returns home.

The home screen has two equal-width columns beneath its header. The left column
allocates its vertical space 40/40/20 to the last completed real workout, lifetime
summary, and buttons for detailed stats and milestones/trophies. The right column
is a native vertical ScrollView containing Gravity Well, the sole offered game.
The rower connection/diagnostics screen remains available from the header.

## Athlete data

`AthleteHistory` is a pure Java model; `AthleteStore` uses an independent atomic
`athlete-history-v1.properties` file. Gravity Well first commits its existing game
save, then imports cumulative real counters into athlete history. Import watermarks
make replay/restart safe. Test input never contributes. Existing Gravity Well real
distance, strokes and motion time migrate automatically. Historical completed
workout counts that were not recorded cannot be reconstructed.

Finished real sessions now write a completion identifier, timestamp, distance,
strokes and motion time. The last completed workout survives starting the next
session. An unfinished saved session does not replace the last completed workout.
Legacy completed session summaries can be recovered without inventing a finish date.

Lifetime fields are real meters, accepted stroke starts, estimated motion duration,
and recorded finished workouts, plus valid-sample S4 estimated energy, power coverage, provisional calorie deltas, pulses and detected drive duration. See S4-TELEMETRY.md for validation limits. Time
retains the prototype's distance-freshness estimate. Older Castlebreaker sessions
are not retroactively counted.

## Reset behavior

The detail page explicitly states the reset scope. The button requires one
continuous five-second finger hold, with visible countdown and progress fill.
Release, moving outside the button, a second pointer, loss of window focus,
navigation or backgrounding cancels it. Partial holds never accumulate.

Reset zeroes all lifetime counters, removes the last workout, milestones,
badges and trophies, and records a reset date. It preserves source import
watermarks (including the last imported workout ID) and Gravity Well career state. Old source totals therefore cannot reappear after a reset;
only new source increments count. Reset writes atomically. The reset action is
never invoked automatically during device testing with user data.

## Achievements

Six initial milestones recognize a first finished workout, 1 km, 10 km, 1,000
strokes, one hour of motion, and ten finished workouts. Unlocks use real imported
totals and are cleared by a complete athlete reset. The awards page separates
earned from upcoming milestones and shows progress. Game-specific trophy
challenges remain future content; no trophies or fitness achievements are fabricated.

## Validation

The test suite includes synthetic exclusion, idempotent import, reset/restart
without recrediting old meters, post-reset increments, cleared awards/last workout without reimport,
source rollback handling, and the exact five-second hold boundary/cancellation.
The existing metric, campaign and Gravity Well suites are retained.

605 automated checks passed (23 athlete/hold checks plus 582 existing checks).
Installed on the Ergatta Android 9 tablet; accepted the launcher pin prompt and
launched the dashboard from the actual home-screen icon. Inspected dashboard,
details and milestone views. A two-second reset hold cancelled with the existing
8 meters and 2 strokes intact. Tested the Gravity Well game entry and back navigation.
The Android crash log contained no errors from these checks. The complete reset
was tested on disposable model data, not the user's physical workout record.

Local evidence: `build/openrow-dashboard.png`, `build/openrow-stats.png`,
`build/openrow-awards.png`, `build/openrow-game-launch.png`.

0.7.1: five-second hold and complete athlete reset verified with model tests;
cleared badges, trophies and last workout do not return when the same game save
is replayed. Button labels are Detailed Lifetime Stats and Milestones & Trophies.
No reset was performed on the user's tablet data during verification.
