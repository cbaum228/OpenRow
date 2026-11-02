# OpenRow — first private alpha

Native offline rowing games and workout recording for an Android 9+ tablet connected to a WaterRower S4 over USB. This alpha has Gravity Well, Castlebreaker, per-family profiles, endurance/general-cardio coaching and local workout history. It is independent of Ergatta and intended for one invited tester on a second rower.

**[Download alpha 1](https://github.com/cbaum228/OpenRow/releases/tag/v0.22.0-alpha.1)** · **[Install instructions](docs/ALPHA-INSTALL.md)** · **[Tester checklist](docs/ALPHA-TEST-CHECKLIST.md)** · **[PV&V report](docs/PVV-ALPHA-1.md)** · **[Privacy](docs/PRIVACY.md)** · **[Build and signing](docs/BUILDING.md)**

Prefer a USB stick if the tablet has no browser. The repository is private: download access requires an invitation, or the owner can provide the offline kit. Install the APK, not the ZIP. The alpha uses pace fallback for gameplay; personal power gameplay remains disabled pending physical validation. See the PV&V report for the distinction between passing software checks and second-device checks that remain open.

## Development history

# OpenRow 0.21.2 · S4 connection handoff

Fixed missing campaign XP when starting Gravity Well before the S4 connects. The initial handshake now resumes the shared recorder alongside the game, restoring its effort signal. Disconnects during rowing still pause the session until explicitly resumed. See [diagnosis and regression coverage](docs/XP-CONNECTION-HANDOFF.md).

# OpenRow 0.21.1 · Compact object silhouettes

Corrected excessive whole-body stretching during consumption. Stars and gas giants retain compact cores while stripped material extends into the feeding streams. Shared geometry keeps surfaces, corona and stream inlets attached. The tablet's 29-object sample averaged 29.99 FPS; see the [correction and verification](docs/GRAVITY-WELL-GRAPHICS-021.md#0211-silhouette-correction).

# OpenRow 0.21.0 · Motion, flow and frame budget

Gravity Well now targets approximately 30 FPS with reusable stream geometry and adaptive background detail. Deformed stellar and gaseous surfaces connect to curved, inward-accelerating material streams; corona attachment, fragment tumble and localized disk light improve motion. The independent rubric now scores object motion and material flow against the expanded simulation-quality reference. See [changes, recordings and actual tablet presentation timings](docs/GRAVITY-WELL-GRAPHICS-021.md).

# OpenRow 0.20.0 · Graphics development

Planetary bosses rotate across spherical surfaces; crust faults and thicker fragments make rocky destruction more readable; stellar surfaces stretch toward the well; and plasma filaments, disk motion and tilted depth ordering improve consumption. An independent graphics-review task scores the results against the user's 0–10 scale. See [graphics changes and native evidence](docs/GRAVITY-WELL-GRAPHICS-020.md).

# OpenRow 0.19.0 · Shared S4 workouts

Record Gravity Well, Castlebreaker, or standalone rowing with per-profile workout history, power/pace/cadence charts, observed energy and interval summaries. Home can resume a paused workout in its game or standalone. OpenRow selects normal watts mode on connection. Personal power response is implemented behind the normal-water validation gate; gameplay currently uses pace fallback. See [shared workout telemetry](docs/SHARED-WORKOUT-TELEMETRY.md).

# OpenRow 0.18.0 · Family profiles

First-run setup creates a named local profile with a training goal and coaching preference. Use **Profiles** on Home to add family members, switch careers, or edit your profile. Game progress, training, assessments, and athlete history are independent. Existing saves stay with the first profile. See [family profile behavior and verification](docs/FAMILY-PROFILES.md).

# Campaign balance — 0.17.0

Prepared bosses now take about five reference-effort minutes, while no-upgrade
encounters scale from 40 minutes to eight hours. The main route is balanced across
48 eligible hours, with miniature defeated bosses, repeat harvesting, saved retreat,
and Coach preparation queueing. Existing careers migrate without losing purchases
or athlete records. Eight full campaign simulations and synthetic tablet checks pass.
See [balance rules and validation](docs/GRAVITY-WELL-BALANCE.md).

# Menu and training refinement — 0.14.0

[Full eight-chapter campaign simulation and skill-tree balance audit](docs/GRAVITY-WELL-PROGRESSION-AUDIT.md).

A connected upgrade-node overview, clearer guided-workout navigation and readable workout previews. See [UX guide](docs/GRAVITY-WELL-UX.md).

# Gravity Well campaign update — 0.13.0

Three campaign chapters, four featured bosses, shared harvesting materials, 30 upgrade nodes, and an isolated accelerated test lab. See [campaign guide and validation](docs/GRAVITY-WELL-TEST-CAMPAIGN.md). Real prestige remains a future feature; cosmetic rewards can be previewed in the synthetic lab.

# OpenRow

[Trainer-aware athlete-relative visuals and comparison](docs/TRAINER-VISUAL-INTEGRATION.md).

Version **0.12.1** connects trainer guidance and recent athlete observations to personal game visuals.
The shared foundation training engine is available in Gravity Well's
real S4 career, open **Training & assessments** for recommendations, Coach mode,
a five-minute baseline, a 35-minute benchmark, or a three-session starter series.
Training adapts duration using reported effort, recent evidence, readiness and
return-from-break check-ins. It includes progress charts and local export/deletion.
Exercise policies remain experimental pending professional and normal-water validation.
See [Training engine](docs/TRAINING-ENGINE.md) for the contract and validation limits.

[Gravity Well visual audit and next improvements](docs/GRAVITY-WELL-VISUAL-AUDIT.md).

Version **0.11.1** adds rowing-responsive particle density, plasma brightness and six sweeping neutron-star beams.
[Rowing visual response and tablet sample](docs/GRAVITY-WELL-ROWING-VISUALS.md).

Gravity Well features six distinct stellar bosses with cached spherical surface animation,
evolving prominences, neutron-star beams and a quieter surrounding scene.
[Boss visuals and tablet showcase](docs/GRAVITY-WELL-BOSS-VISUALS.md).

Gravity Well includes a 17-class body catalog, rare boss encounters,
and three upgrade lines for physical boss fragmentation. **Body atlas** shows
career-distance unlocks; **Boss fragmentation** is the second upgrades page.
Boss pieces conserve mass and expose enough extra area for 12–22% faster erosion.
Stellar shearing is shown as one shrinking spherical remnant feeding plasma filaments;
star and comet streams extend beneath the event horizon throughout their orbits.
Extra stellar filaments fade in smoothly and share the cloud brightness and particle budget.
Bosses have detailed surfaces, coronas and plasma loops, with a separate visual preview.
Capture now eases incoming momentum into the feeding orbit, including rear-side approaches.
Trails now grow from freshly stripped material, and planetary remnants break
into compact, tumbling chunks instead of stretching into oblong shapes.

OpenRow opens an athlete dashboard from a custom Android home-screen icon:
last workout, lifetime stats, earned milestones, and a scrolling Gravity Well game
list. Detailed stats include a continuous five-second hold to clear athlete totals, last workout, milestones and trophies.
See [Home dashboard](docs/HOME-DASHBOARD.md) for data migration and reset behavior.

New: **Live Rowing Stats** exposes S4 power, speed/split, calorie counter, monitor clock, optional heart rate, drive/recovery timing, pulses and tank configuration. Real Gravity Well sessions record estimated energy and calories in independent athlete stats. Select **Enable S4 watts** to use the normal watts display; gaps never earn energy. See [S4 telemetry](docs/S4-TELEMETRY.md) for units, validation limits and sensor architecture.

Gravity Well now captures textured moons, rocky bodies and planets into persistent orbits. Larger bodies erode into luminous particles; dust falls directly inward. A cached nebula and layered accretion disk add depth while effects remain bounded for the tablet.

Large captured worlds also stretch toward the well and feed curved gas streams
into its disk. The unoutlined, chipped consumption face sheds debris; a maximum
of eight cached gas ribbons keeps the spectacle bounded for the tablet.
Polar mantle now crumbles around a longer-lived core. The redesigned disk adds
turbulent gas, asymmetric illumination and an illustrative lensed rear image
around a fully dark central shadow.
Captured planetary rings detach into broken, shearing bands that fall toward the
well. Mantle breakup also opens across the inward-facing surface.
Textured mantle chunks now peel, stretch and shear into the same flow, with
outer layers releasing before inner layers and the compact core.

An open source rowing app for an Android 9 Ergatta tablet and a USB WaterRower S4.
Version 0.5 adds campaign setup, scaled session plans, rest stops, saved fort checkpoints, and campaign results alongside free play and the rowing dashboard.

Version **0.6.0** adds **Gravity Well**, a stationary black-hole rowing game with
dust, asteroid trajectories, a captured-matter reservoir, input-driven processing,
5/30-minute sessions, atomic saves and six prerequisite-based upgrades. Open
**Play Gravity Well** from the dashboard. S4 mode uses distance as a labeled game
control; synthetic power mode has its own isolated test career. S4 power
and calorie scaling still need normal-water validation; adaptive workouts remain future work. See [Gravity Well](docs/GRAVITY-WELL.md)
for controls, save semantics, limitations and the supplied feature inventory.

The shared training implementation is described in [Training engine](docs/TRAINING-ENGINE.md),
with the original architecture preserved in [Training core](docs/TRAINING-CORE.md).
It covers assessment, workout generation, history, adaptation, and the game contract.
Castlebreaker campaign distance scaling remains separate from adaptive training.

## Castlebreaker

Tap **Play Castlebreaker** on the dashboard to open campaign setup. Choose **Free play**
for the original individual-fort mode, or start a campaign through all four forts.
OpenRow attempts to connect to the S4 when you enter a game.

### Campaigns

- Plan by **500 / 1,000 / 2,000 meters**, or **15 / 30 / 45 minutes of estimated rowing**.
- Adjust planning pace in 15-second steps (per 500 m). Time choices derive a distance budget from that pace; they are not hard session timers.
- The winch scales to the planned distance. For example, a 1,000 m plan uses 53 m standard shots and 106 m siege shots; efficient play totals about 1,007 m.
- Clear the four forts in order. Misses, extra charging, aiming and rests can extend the plan. Campaigns finish when all four forts are captured.
- Choose manual rests, 30 seconds, or 60 seconds. Continue explicitly when ready; timed rests can be skipped. Rest time and rest-stop distance do not count toward active play totals.
- The input mode is locked for a campaign. Test campaigns and their saved results are clearly labeled; simulated meters never change the S4 or rowing-dashboard data.
- Progress is checkpointed locally at fort boundaries. Returning to setup pauses the current encounter in memory. After the app closes, the unfinished fort restarts from its checkpoint and partial-fort totals are not retained.
- The summary reports forts captured, distance, shots, and active play time (including aiming, excluding rest stops and background/disconnected time).
- **Resume campaign** restores the checkpoint; free play remains separate and does not alter campaign progress.

### Free play

New S4 distance gained while playing winds the catapult: **10 m readies a shot**, and
**20 m fully charges it**. Charge is held until firing. Existing monitor totals and
meters accumulated outside the game do not grant charge.
Hold the aim buttons to turn left/right and tap **FIRE** to launch. The low 6-degree
trajectory reaches the fort in roughly one second. Ready shots pass through tower
height; extra charge adds speed and impact rather than a large range change. The
dotted preview and crosshair show the path and its intersection with the fort plane.
Dislodge the defenders from their towers to win. Archers guard ordinary tan stone;
knights guard tougher blue-gray reinforced stone. Standard 10 m shots break ordinary
masonry. Full 20 m siege shots are much more effective against reinforcement, while
lighter hits leave visible cracks and cumulative damage. You can charge during flight.

Choose a fort using the arrows above the battlefield: **The Watchpost**, **Iron Gate**,
**Split Rampart**, or **The Citadel**. The original fort remains as the Watchpost;
the other three are new layouts. A **Next fort** button appears after victory.

- Touch: aim left/right, fire, reset fort, return to rowing dashboard.
- Keyboard: Left/Right or A/D to aim, Space/Enter to fire, R to reset the selected fort, N to select the next fort.
- **Switch to test mode** enables a +5 m button and W key without physical rowing.
  Test inputs never feed the rowing metrics or the S4 connection.
- The game pauses when its activity is backgrounded or the rower disconnects in rowing
  mode. Returning to the dashboard stops game simulation and charging.
- Free-play encounters last while the app stays open. Persistent upgrades,
  sound and force-based charging are future work. Distance is the current game
  charging input, not a measurement of mechanical energy.

The game uses a small fixed-step ballistic and block-support simulation, rendered
with native Android Canvas. Blocks fall and scatter after impacts; this is simplified
prototype destruction rather than a full rigid-body solver. No third-party game engine,
downloaded art, or network services are used.

## What it does

- Finds the S4 USB identity `04d8:000a` and requests Android USB permission.
- Claims the CDC serial interfaces at 115200 baud, 8N1, DTR/RTS enabled.
- Sends `USB\r\n`, waits for `_WR_`, then queries version (`IV?`), distance (`IRD057`), stroke count (`IRD140`), and stroke rate (`IRS1A9`).
- Shows large distance (meters), stroke-count and calculated cadence readings.
- Calculates cadence from `SS` stroke-start events, independently of the S4 cadence register. The main number is 60,000 divided by the mean of the last three intervals in milliseconds; the latest interval rate appears below it. The first reading requires two starts.
- Rejects duplicate starts less than 500 ms apart. After a pause, cadence becomes unavailable and the next two starts establish a new rate. The idle threshold is 2.5 times the mean interval, bounded to 6–15 seconds (15 seconds before the second stroke).
- Retains raw replies, byte counts, monitor version, and handshake status under **Show diagnostics**. The S4 cadence register remains in the log for comparison, but does not drive the dashboard.
- Saves timestamped logs in private app storage. It makes no network requests and sends no workout reset commands.
- Provides explicit disconnect and manual reconnect after cable removal.

## Hardware verified

- Android 9 / API 28; ARM64 and ARM32 supported; 1920 × 1080 display.
- Separate computer debugging connection and S4 USB connection work simultaneously.
- S4 USB descriptor: `CDC RS-232: WR-S4.2`.

## Build

Run `./build.ps1` in PowerShell. This uses the project-local JDK 17, Android 35 platform,
and Android build tools 35.0.0 under `tools/`. It produces `build/OpenRow-Probe.apk`.
The build currently expects the locally downloaded tools (not committed to Git):

- JDK 17 ZIP from https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse, extracted to `tools/jdk/`.
- https://dl.google.com/android/repository/build-tools_r35_windows.zip extracted to `tools/build-tools/` (archive directory is `android-15`).
- https://dl.google.com/android/repository/platform-35_r02.zip extracted to `tools/platform/`.
- https://dl.google.com/android/repository/platform-tools-latest-windows.zip extracted to `tools/`.

Run `./test.ps1` for deterministic metric and Castlebreaker physics tests, including
replay of recorded S4 strokes, every fort, armor effectiveness, and full campaign progression/checkpoint round trips.
The compact fixture preserves the event timestamps and final totals from the hardware test.

No Gradle, Android Studio or third-party runtime libraries are required. APKs are signed
with a local development key in `build/debug.keystore`; retain this key for updates.

## Device test

1. Keep tablet power and S4 cable connected, and keep the monitor awake.
2. Install: `tools/platform-tools/adb.exe install -r build/OpenRow-Probe.apk`.
3. Close Ergatta before connecting from the probe; the two apps must not use the S4 simultaneously.
4. Launch: `tools/platform-tools/adb.exe shell am start -n org.openrow.probe/.MainActivity`.
5. Tap **Connect to S4** and approve USB access. Distance and stroke count should populate from current S4 totals.
6. Row a few strokes; calculated cadence appears after the second start event. Pause and resume to check that cadence resets. Use **Show diagnostics** for raw messages. Disconnect, reconnect, and test cable removal.
7. Inspect logs: `tools/platform-tools/adb.exe logcat -d -s OpenRow:I AndroidRuntime:E`.
   Private logs can also be read with `adb shell run-as org.openrow.probe cat files/<session-name>.log`.

This does not replace the launcher, disable Ergatta, change firmware or change boot behavior.
Automatic recovery and workout storage belong to later milestones.

## Protocol references

Connection parameters and query meanings were checked against
[Coxswain Protocol4](https://github.com/svenmeier/coxswain/blob/master/app/src/main/java/svenmeier/coxswain/rower/wired/Protocol4.java).
The application is an independent implementation using Android's USB host API.


## Gravity Well 0.15.0

Expanded to 81 upgrade nodes across nine tiers, with rank-based unlocks, source-specific specialist materials, a scrolling tree and chapter navigation. Existing purchases are retained. See [the catalog and progression rules](docs/GRAVITY-WELL-EXPANDED-TREE.md).


### Gravity Well 0.15.1

The upgrade tree now groups three consecutive nodes per branch into each of three tiers. Tier gates use 0 / 8 / 23 earlier main ranks; individual nodes retain branch prerequisites. Specialist investment counts all main nodes in the same tier. Existing IDs and purchases are unchanged.

