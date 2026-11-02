# Castlebreaker prototype — October 23, 2026

## Visual refinements: 0.3.2

- Terrain starts at the perspective camera's actual ground horizon, so the castle's
  base has continuous ground beneath it. Distant mountains are smaller, paler layers
  placed near that horizon, separated from the playable field.
- Removed the broad oval castle shadow. A projected patch of compacted earth and small
  ground-plane contact shadows anchor the base blocks and settled debris.
- Block faces now use projected world-space corners rather than screen-aligned fake
  depth. Their bottom corners therefore coincide with their ground footprints.
  Falling blocks retain their tumble; settled debris turns on the ground plane.
- Catapult retains the same style and footprint, with a four-wheel wooden carriage,
  deck planks, braces, metal pivot axle, cup, rope-wrapped winding drum and animated crank.
- Distance charging, shot trajectory, aiming, controls and destruction simulation
  are unchanged. Existing 286 metric/game assertions still pass; compilation and APK
  signature verification pass.
- Installed on the tablet; actual scene screenshot `build/castle-grounded.png` inspected
  for ground contact and distant scenery. Charged view and impact inspected separately.

## Current tuning: 0.3.1

The initial stroke-based tuning described below has been superseded:

- Charging uses positive S4 distance increments, at 5% per meter. Ten meters unlocks
  firing; twenty fully charges. Initial readings only establish a baseline.
- Baselines are refreshed while inactive and cleared on game entry/resume, disconnect,
  mode change and fort reset. Decreasing totals (monitor reset/rollover) establish a new
  baseline without awarding a large delta. Duplicate and invalid readings grant nothing.
- Distance is polled more frequently, interleaved with existing diagnostic queries.
- Fixed elevation is now 6 degrees with launch speed 42–48 world m/s for ready shots.
  Flight to the fort is about a second; all ready charges pass through tower height.
  Extra charge increases impact strength with a small speed increase.
- The preview ends at a crosshair on the fort plane, replacing the ground landing ring.
- Test mode adds five simulated meters per button/W press. It does not alter real S4
  totals or the rowing dashboard. End-of-encounter totals are now meters, not strokes.
- Deterministic checks pass: 259 game assertions plus 27 rowing-metric assertions.
  These include meter baselines/reset/reconnect cases and 33 combinations of ready
  charge/aim that reach and damage a tower without a high lob.
- Device check: APK installed and S4 handshake succeeded. Its existing raw distance
  `00DC` (220 m) left charge at zero, visually verified. Two +5 m test inputs produced
  50% charge; the resulting centered shot toppled the center beacon. The preview,
  distance gauge and low fort-plane crosshair were inspected on the tablet. No runtime
  crash was reported. Fort reset and input returned to S4 rowing mode afterward.
  Evidence: `build/castle-distance-baseline.png`, `build/castle-flat-preview.png`,
  `build/castle-flat-impact.png`, and `build/castle-distance-device-test.log`.
  Physical distance charging from fresh user rowing remains the next playtest.

## Initial version 0.3.0 (historical)

## Playable scope

One catapult, one stone type, one fort, three red beacon targets. Strokes wind the
catapult; aim rotates left/right; launch energy controls range at fixed elevation.
Winning requires toppling the three beacon blocks. A reset control rebuilds the fort.
The dashboard remains available and owns the existing S4 connection.

Each accepted `SS` event adds a fixed 12.5 charge units, capped at 100. Stored charge
does not decay while resting. These are game units, not measured physical joules.
The view only accepts real strokes while it is visible, active, in rowing mode, and
connected. Duplicate rejection stays in the existing rowing-metrics model.

Test mode is labeled on screen and uses a separate path. It can add game charge but
does not fabricate S4 telemetry or rowing-dashboard metrics. Switching modes retains
the prototype encounter, so its scores should not be treated as verified workouts.
`Castlebreaker` logcat messages distinguish real (`S4`) and simulated (`TEST`) inputs.

## Controls

Touch controls are provided for aiming, firing, reset, connection, input mode and return
to the dashboard. Keyboard controls: arrows or A/D aim, Space/Enter fires, W creates
a test-mode stroke, R resets. Handle-mounted two-button hardware is not implemented yet.

## Implementation

- `CastlebreakerGame.java`: Android-independent physics and encounter model.
- `CastlebreakerView.java`: native perspective drawing, controls, preview and animation.
- `MainActivity.java`: dashboard entry, USB stroke delivery and lifecycle handling.
- Fixed simulation timestep: 1/120 second, with bounded frame catch-up.
- Ballistic projectile under gravity; sphere-to-block hit detection; impact impulses;
  support removal causes stacked blocks to fall. Debris bounces and settles on the ground.
- Simplified destruction: debris does not implement general block-to-block rigid-body
  contacts. This is a prototype for evaluating the rowing/aiming loop.
- Physics pauses on loss of rower connection in rowing mode and while backgrounded.
- Hidden diagnostics are not repeatedly laid out while playing, to avoid game stalls.

## Validation

`test.ps1` passes 27 rowing-metrics assertions and 46 Castlebreaker checks, including
initial structural stability, charge limits, power/range relationship, aim limits,
launch gating, charging during flight, three aimed shots completing the fort, reset
and deterministic simulation. APK compilation and signature verification pass.

Installed on the actual Android 9 Ergatta tablet, S4 USB handshake confirmed. Initial
and charged screens inspected using actual tablet screenshots. Test mode accepted
five simulated strokes (62.5% charge) and fired one centered shot; logs explicitly
identify the shot as TEST. The center beacon visibly fell, updating the objective to
1/3. Touch-and-hold left aiming produced a -12.3-degree second shot; held right aiming
produced a +12.6-degree third shot. The full encounter completed on the tablet with
3/3 beacons down in three shots and 15 explicitly simulated charging strokes.
The catapult's
base and wheels are visible after a layout refinement. No Android runtime crash
was reported during these checks.

Evidence: `build/castle-victory.png`, `build/castle-final-impact.png`, and
`build/castle-device-test.log` (local, ignored). The fort was reset and input switched
back to real S4 rowing mode for the user's playtest. Live game charging from new
physical strokes remains a user playtest; USB communication and the existing stroke
event path are confirmed.

After suppressing hidden diagnostic text layout while playing, a fresh approximately
38-second sample recorded 2,287 frames, median reported frame time 20 ms, 95th percentile
25 ms, and seven missed frame deadlines. Android also reported a high input-latency
flag count, so these are limited prototype observations, not a full performance pass.

## Next playtest

Try real rowing to charge a shot, experiment with range and aiming, and judge whether
charge per stroke and aiming speed feel comfortable. Sound, richer animation, additional
forts, progression and persistent upgrades follow after feedback on the core loop.
