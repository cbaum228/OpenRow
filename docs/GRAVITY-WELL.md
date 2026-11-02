# Gravity Well first playable â€” OpenRow 0.6.0

October 27, 2026. Implements the small vertical slice from
[the supplied inventory](gravity_well_feature_inventory.md), not the entire MVP backlog.

## Play

From OpenRow choose **Play Gravity Well**. Select 5 or 30 minutes and **Begin rowing**.
The S4 connection uses the existing USB adapter. Keep the monitor awake. A saved
unfinished session offers **Resume saved row**. Finishing early retains earned progress.

For a hardware-free preview, switch **Input: S4 DISTANCE** to **Input: TEST POWER**
before starting. Synthetic input starts at 120 W. Use the on-screen power controls
or left/right keys; Space pauses. Test distance and strokes are synthetic and the
test career is isolated from real rowing. Stop input freezes the loop.

The black hole stays stationary. Fixed-step gravity bends dust and asteroid
trajectories. Close encounters lose orbital energy and spiral inward; fast bodies
can escape. Captures fill a finite reservoir. New input converts stored matter into
XP, with a low-value dust floor when the reservoir is empty. Physics alone never
awards XP. Six multi-rank/prerequisite upgrade definitions support attraction,
processing, value, capacity, richer asteroids and a violet halo. Purchases take place
outside an unfinished session and require XP plus cumulative game distance.

## Honest input semantics

This prototype has two explicitly different input paths:

- **S4 distance fallback:** newly observed meters drive game processing at 20 game
  input units per meter. Recent distance rate controls attraction. These units are
  not watts or joules. Real strokes use accepted SS events. Existing monitor totals,
  reset/backward readings and resume baselines earn no distance credit.
- **Synthetic power:** generated watts are integrated over simulation input time.
  Synthetic distance uses a fixed 2.5 m/s and stroke events use 24 spm for testing;
  these do not model a calibrated human power/pace relationship.

Watts, metabolic calories, athlete-relative calibration and adaptive training are
not implemented. S4 active time is a motion estimate: fresh distance allows the
timer/world to advance for up to 1.6 seconds. Water-wheel coasting can count in
that window; this is not a validated physiological work-duration measurement.
Session durations are active motion time, excluding explicit pause and menus.

## Saves and boundaries

AtomicFile snapshots store the career, pending reservoir, body positions/velocities,
random sequence, spawning clocks, current session and upgrade ranks. No wall-clock
catch-up occurs. Backgrounding pauses and saves; return requires explicit Resume.
Snapshots occur about every three seconds and at pause, finish and purchase. A
process kill can lose progress since the last checkpoint. Unreadable saves are
preserved and block writes. A failed save pauses progression.

Real and test careers use separate private files, `gravity-real-v1.properties` and
`gravity-test-v1.properties`. Within the real atomic snapshot, `game.*` contains
game state; `athlete.*` contains unmultiplied distance, accepted strokes and estimated
motion duration. The shared athlete service is still a future extraction: this
first ledger covers Gravity Well only and does not retroactively include Castlebreaker.
There is no prestige/reset action, expedition integration, cloud sync or offline gain.
The Fitness Passport panel is the first view of that real ledger, not a complete
cross-game passport. Synthetic files never receive athlete measurements.

## Verification

Pure Java tests cover inactivity, input-only XP, exact distance/strokes, fixed-step
stability, bounded bodies, save/world restore, malformed nonfinite saves, upgrade
prerequisites, no purchases during sessions, five-minute completion, and equal
rewards for a 30-minute synthetic run versus six five-minute segments with saves.
Existing rowing/Castlebreaker/campaign tests remain included in `test.ps1`.

The native Android build is compiled and signed using `build.ps1`. Real physical
rowing and effort-response tuning still need a user row on the connected S4.

Device checks completed: installed on the Ergatta Android 9 tablet, exercised
synthetic play, paused at roughly 87 seconds, force-stopped/reopened and confirmed
unchanged saved XP/distance/time, finished early, and purchased Gravitational reach
rank 1 from earned test XP. Real career data remains separate. Screenshots are in
`build/gravity-play.png`, `build/gravity-resume.png`, and `build/gravity-upgrades.png`.
All 582 automated checks pass (89 Gravity Well, 493 existing). The 30-minute seeded
simulation remains bounded at 65 bodies and has catchable ordinary asteroids.

## Next useful increments

1. Tune capture trajectories, dust density and processing feel after rowing feedback.
2. Validate S4 power/pace input; introduce a shared provenance-aware telemetry model.
3. Extract the athlete ledger into shared workout storage for both games and expeditions.
4. Add material-specific processing, encounter cues and workout recipes once input
   quality and the basic loop are established.

The full skill-tree content, prestige policy, training mode selector, HR/SmartRow,
calorie accounting, advanced cosmic visuals and sound remain backlog items.

## 0.7.1 visual and timing update

Session time now continues while the player coasts without rowing. Existing bodies
continue to move and the disk rotates slowly, but no new matter, XP, distance,
strokes or rowing-motion time are credited from coasting. Explicit pause, app
backgrounding and sensor disconnection still suspend play. The HUD labels the
clock Session Time; summary/athlete motion time remains separately tracked.
Old saves migrate their prior active-session duration into the new motion field.

The procedural singularity uses a layered tilted disk, rear light-bending arc,
photon rim, broad glow and fine particles. Rotation integrates a continuous phase
at 0.035–0.1 radians/second as effort changes, avoiding discontinuities from
multiplying elapsed time by current effort. Dust has no line trails; asteroids
remain distinguishable solid bodies. No external bitmap assets are required.

Tablet verification: with synthetic input stopped, saved session time advanced
from 15.08 to 54.30 seconds while XP remained 149.950136 and session rowing time
remained 8.8 seconds. Explicit pause still freezes the session. Static procedural
light layers are cached into a small bitmap; moving particles render separately.
After caching, a 593-frame tablet sample measured median 14 ms, 90th percentile
16 ms, and 95th percentile 17 ms. Screenshot: `build/gravity-visual-update.png`.


## Orbital visuals — 0.9.0

Dust still falls directly inward without trails. Larger rocky, icy, gas and ocean
bodies now settle into slow elliptical orbits inside the capture region. Their
surfaces shrink and shed luminous ejecta until exhausted. Drawing behind and in
front of the horizon establishes depth; the disk has a gaseous base, slowly
advecting filaments, and a fully masked dark center. A cached nebula/star field
and cached shaded body textures avoid external assets or new dependencies.

Bodies contribute only the mass actually stripped while rowing, limited by the
reservoir capacity. Capture counts are awarded once on entering orbit while
rowing. Coasting continues orbits, erosion and visual debris, with no new stored
matter or XP. Explicit pause freezes the simulation. Visible ejecta carries no
additional reward, preventing double credit. Existing processing still requires
rowing input; athlete counters are unaffected by all visual/game multipliers.

Orbit phase, radius, capture age, remaining/initial mass, material and spin are
persisted using backwards-compatible additions to body saves. Legacy captured
bodies initialize their orbits on the next simulation tick. Ejecta is transient
and restarts empty after restoring; mass and orbital progression are retained.

The erosion model depends on exposed surface area. A fragmentation API conserves
the parent's remaining mass while creating smaller orbiting pieces; their summed
surface causes faster breakdown. This is a tested foundation for future upgrades,
not a new purchasable skill-tree node. Body count stays capped at 150, debris at
240, and dust/debris draws are batched. Physics stays at a fixed 60 Hz.

Validation covers gradual capture/erosion, persistent orbit position/material,
coasting without rewards, explicit pause, fragmentation mass conservation and
faster shedding, full disappearance and migration of legacy saved bodies.
The entire test suite passes with the previous telemetry and athlete tests.

## Graphics refinement — October 28, 2026

The central well is about 20% smaller in both play and career views (play radius
41–47 instead of 52–60 logical pixels). Its tilted disk and foreground/background
layering remain. Gas filaments and a cached light overlay brighten with smoothed
rowing effort. Planets have a separate cached lighting mask that faces the well
while their surface texture rotates, plus a small depth-based size variation.
Gas giants have tilted rings drawn behind and in front of their spheres; both
ring halves are cached, avoiding repeated animated arc construction.

This is a Canvas rendering pass: physics, earning rules, saved progression and
athlete metrics are unchanged. New art uses one 128×128 lighting texture and two
256×128 ring textures (about 320 KiB total); no new dependencies or downloaded
assets. Existing 150-body/240-debris limits remain. The signed APK builds and all
677 existing checks pass. Further visual work should prioritize richer surfaces,
tidal stretching and occasional authored events, with tablet measurements before
adding full-screen effects or more independently simulated objects.

Final hardware spot check: a synthetic gameplay sample rendered 672 frames in
about 21 seconds (roughly 32 fps), with median 17 ms, 90th percentile 24 ms and
95th percentile 25 ms frame durations reported by Android gfxinfo. Its 66% jank
classification against the 60 Hz deadline means this is not a locked 60 fps
result. Scene load and warm-up differ from earlier samples; no controlled
before/after speedup is claimed. Screenshot: `build/gravity-refinement.png`.
The updated APK is installed on the tablet; the synthetic preview is paused.

## Directional planet consumption

Captured bodies now lose their inward-facing surface rather than shrinking as
perfect spheres. A clipped, slightly chipped face moves outward as mass falls;
the plane is derived from remaining sphere volume, so half mass produces a
hemisphere and the last material becomes a small outer cap. Surface spin remains
independent of the face, which always points toward the well. Lighting and gas
giant rings are clipped with the body. The former decorative capture arcs and
crack strokes are removed; a luminous exposed face shows where erosion occurs.

Ejecta starts along this same exposed face using shared geometry, then follows
the existing inward gravity. No per-planet particle meshes or bitmap rebuilding
are needed; paths are reused and the existing particle/body limits remain.
Mass processing, reward rules, saves and workout measurements are unchanged.
All 783 checks pass, including hemisphere volume, monotonic erosion and fragment
origins on the retreating face. Existing saves immediately show their current
remaining mass as an eroded shape.

Tablet preview: `build/gravity-erosion.png`. A short synthetic sample rendered
479 frames in about 15.7 seconds (roughly 30 fps), median 20 ms and 95th percentile
27 ms. Android classified 88% as janky against 60 Hz; this remains a roughly
30 fps presentation rather than locked 60 fps. No AndroidRuntime errors were
reported. The updated APK is installed and the test session is paused.

### Natural erosion edge

Removed both strokes along the exposed erosion boundary at user request. The
chipped silhouette and particles peeling off that face remain, without a bright
scan-line appearance. This removes a path construction loop and two stroke draws
per eroding body. The signed APK builds successfully; simulation is unchanged.

Visual direction: improve surface detail, consistent spherical lighting,
restrained atmosphere and material-specific fragments first. The current Canvas
renderer rotates flat surface images rather than mapping textures onto 3D
spheres. A larger step toward realistic rotating planets would use a dedicated
OpenGL ES scene renderer while retaining the existing simulation and Android HUD.
Prototype and benchmark one textured, lit, eroding planet on the actual tablet
before committing to a renderer migration. Existing roughly 30 fps samples do
not establish sufficient headroom for full-screen bloom, complex lensing, or
thousands of physical fragments.

## Tidal streams — reference-inspired tablet pass

Large captured gas/ocean worlds now feed soft curved ribbons into the accretion
disk. Their surviving surfaces stretch up to 22% along the inward/outward axis,
with a slight transverse compression. Fragment origins use the same transform.
The chipped erosion silhouette remains unoutlined. Ribbons start at that face,
curve with the disk's rotation and contain advecting bright grains. Warm and cool
material colors distinguish sources; the horizon masks the streams.

The effect is decorative and deterministic from existing body state and visual
phase. It follows the existing pause/coasting behavior and adds no rewards, mass,
save fields or physics bodies. At most three sources are drawn, with 20 mesh
segments per ribbon, two cached 128×32 soft gas textures (32 KiB total) and 32
grains per ribbon. The two passes establish rear/front occlusion at the disk plane.
No volumetric simulation, screen-sized blur, new dependencies or image assets are
required. All 786 checks pass, including shared tidal deformation geometry.

The supplied references guide flowing material and stretched shapes. The tablet
version emphasizes these consumption effects before a larger surface-texture or
3D-renderer project. Full-height jets and screen-filling plasma remain deferred
until the smaller effect is profiled and reviewed.

Final tablet spot check: 1,249 frames in about 38.6 seconds (roughly 32 fps),
median 18 ms, 90th percentile 24 ms and 95th percentile 25 ms. Android still
classifies 70% as janky against 60 Hz, so this is not a locked 60 fps result or a
controlled performance comparison. No AndroidRuntime errors were reported.
Screenshot: `build/gravity-tidal-streams.png`. The final APK is installed and the
synthetic session is paused.

## Unstable mantle and accretion disk rework

The previous hemisphere/cap silhouette is replaced by a direction-dependent
radial contour. The inward mantle and poles erode fastest, with stable seeded
notches. A compact core remains after most mantle is gone, then shrinks away.
Every direction recedes monotonically as remaining mass decreases, avoiding
regrowth or outline flicker. This is an artistic density model; physical mass
accounting and the existing surface-area erosion rate remain unchanged. Ejecta
and stream origins share the new contour. Loose gas-giant rings fade over the
first six seconds of capture instead of being cut off by the new mask.

The disk now uses a cached turbulent gas image with hotter inner material,
fainter outer falloff, a brighter approaching side, a broad upper lensed image
and a thinner underside echo. An opaque circular shadow replaces gas drawn
across the dark center. A narrow photon rim remains. Seven drifting gas wisps
and 96 grains supply bounded animation; effort still brightens the gas.
The 640×640 cache per unlocked palette replaces the previous 720×720 layers.
No per-frame bitmap creation, screen-space blur or relativistic ray tracing is
introduced. The silhouette uses one reused 80-point path per captured body.

Visual reference: NASA's black-hole accretion disk visualization,
https://svs.gsfc.nasa.gov/13326/ . Brightness asymmetry and apparent rear-disk
light bending are illustrative approximations, not a scientific renderer.
All 1,495 checks pass, including monotonic mantle loss, core retention, eventual
core disappearance and debris emitted from the new surface.

Final tablet check: 353 frames in roughly 10.8 seconds (about 33 fps), with
median 20 ms and 95th percentile 29 ms. Android classified 88% as janky against
60 Hz; this remains roughly 30 fps, with no claim of locked 60 fps or measured
speedup. No AndroidRuntime errors were reported. The final signed APK is
installed, with the synthetic session paused. Preview:
`build/gravity-core-disk-final.png`.

### Rapid ring stripping

Ringed flyby worlds keep their rings until captured. Capture now strips rings
from the inward side across to the outward side over 2.4 seconds, replacing
the previous six-second opacity fade. Small grains peel from the retreating
ring region with a stronger initial inward velocity. The planet continues its
normal, slower mantle/core consumption and retains over 80% of its mass when
the rings finish disappearing in the supplied deterministic test.

Ring grains share the existing 240-particle cap and add no matter or XP. Ring
progress derives from saved capture age, requiring no new save fields. The
signed APK builds and is installed; all 1,503 checks pass, including ring timing,
save/restore continuity, debris bounds and unchanged XP accounting.

## Shearing ring flow, surface breakup and expanded trails

Captured rings now detach as a whole and move toward the well. Different
angular sectors accelerate at different rates, bending the bands out of their
original ellipse. Gaps open as the bands disperse; ring grains are emitted from
the same deforming geometry. The 2.4-second duration remains, but the former
one-sided clipping wipe is removed. The black-hole shadow occludes ring flow.
Intact flyby rings still use cached images. Captured rings use reused paths
with seven lanes and 48 segments; no extra rigid bodies or bitmap churn.

Three small jagged openings develop across the inward-facing mantle surface,
and a portion of the debris emerges there rather than only at its silhouette.
The core is protected from these openings. The disk's gas bands now include
spiral and angular turbulence, while moving filaments wander radially rather
than tracing uniform concentric arcs.

Up to eight material streams are now visible, including captured rocks and
moons. Meshes use 16 segments and 20 grains each to bound added drawing work.
Mass accounting, rewards, saves and the existing 240-particle limit remain
unchanged. All 1,516 checks pass, including whole-ring translation, shearing,
convergence into the well, and mantle openings preserving the core.

Final tablet sample after recording stopped: 424 frames in about 14.2 seconds
(roughly 30 fps), median 21 ms, 90th percentile 26 ms and 95th percentile 28 ms.
Android classified 95% as janky against its 60 Hz deadline, so smooth locked
60 fps is not claimed. No AndroidRuntime errors were reported. Final APK is
installed and synthetic gameplay is paused. Review artifacts:
`build/gravity-ring-flow-preview.png` and the 35-second
`build/gravity-ring-flow-preview.mp4`.

## Object destruction using the detached-ring visual language

Captured bodies now shed textured mantle pieces that shear, elongate and curl
inward using flow geometry similar to the detached planetary rings. Outer
mantle pieces release first, followed by inner layers and small final core
pieces. Some sources begin across the visible face, others near its edges.
The existing compact remnant, mantle openings, gas ribbons and grain debris
remain as supporting layers; no bright cut outlines are introduced.

Each fragment's release and flow progress derive from remaining mass, so this
effect freezes on explicit pause and survives save/restore through the existing
body fields. Grain debris can emerge from these moving pieces. Fragments are
decorative: no mass, XP, physics bodies or save fields are added. Rendering uses
three cached 40×40 textured chips per material, warped as small quads; at most
48 pieces are drawn per frame. This is about 75 KiB of chip textures across all
four materials and reused geometry buffers, with no per-frame bitmap creation.

All 1,569 checks pass, covering staggered mantle/core release, inward travel,
eventual convergence, bounded debris, reward invariants and existing workouts.

Final tablet sample with recording stopped: 375 frames in about 12.6 seconds
(roughly 30 fps), median 23 ms, 90th percentile 27 ms and 95th percentile 29 ms.
Android classified 95% as janky against 60 Hz; this is not a locked 60 fps
result. No AndroidRuntime errors were reported. The signed update is installed
and synthetic gameplay is paused. Review artifacts:
`build/gravity-mantle-flow-preview.png` and the 35-second
`build/gravity-mantle-flow-preview.mp4`.

## 0.10.0 — Body classes, boss encounters and physical fragmentation

The game now has 17 classes: meteoroids, rocky/metallic asteroids, comets,
rocky/icy moons, gas clouds, dwarf/rocky planets, gas/ice giants, brown/red
dwarfs, Sun-like stars, red giants, white dwarfs and neutron stars. The career
screen's **Body atlas** lists class traits and career-distance unlocks.

Larger classes arrive as rare single boss groups with a combined health bar.
Three new upgrade lines on **Upgrades → Boss fragmentation** automatically split
matching bosses into 2/3/4 physical chunks. Mass is conserved and newly exposed
area yields a bounded 12/17/22% erosion-rate benefit. Existing decorative chips
remain separate from these real, persistent game bodies. Boss completion gives
a game-only count; captured matter still requires rowing input to become XP.

See [Body catalog and bosses](GRAVITY-WELL-CATALOG.md) for all distance gates,
upgrade prices, prerequisites, encounter scheduling, save migration and budgets.
Existing careers retain their six original upgrade ranks and old bodies; new
ranks and encounter state migrate through optional version-1 save additions.
The signed 0.10.0 APK builds and all 1,777 checks pass, including 213 new catalog,
boss, fragmentation-family, conservation and migration checks.

## 0.10.1 — Stellar presentation and inward comet flow

Stellar fragmentation no longer renders a cluster of complete smaller stars.
A transient scene representation combines the surviving physical pieces into
one stretched remnant, with several narrower plasma filaments peeling from its
inward surface. The remnant includes combined remaining mass and the original
boss scale, including when only one underlying physical fragment remains.
Stellar ejecta originates from this same shared remnant; rigid decorative
mantle chips are suppressed for stars and clouds. Existing split bosses use
the new presentation immediately, without changing their saved pieces, mass,
health, upgrades, erosion benefit or rewards. Rocky bodies still show chunks.

Comets now feed curved, advecting gas toward the well. The former outward
bitmap tail is removed. Uncaptured comet inlets have bounded length, and
captured comet streams feed into the disk. Comets and the active boss receive
priority within the eight-source stream budget. One sheared stellar source can
add up to three extra narrow branches; each uses the existing cached gas texture.
No screen-sized effects or new physics bodies are introduced.

All 2,626 checks pass. Added checks cover one-remnant stellar grouping, unchanged
physical mass, save/restore of an already split star, the final surviving piece,
preserved rocky chunks and inward comet flow from multiple distances/directions.

Tablet validation: a red giant with the stellar upgrade now renders as one
elongated remnant with four plasma filaments. Comets on multiple approaches
show inward-curving vapor. After screen recording stopped, 1,619 frames rendered
in roughly 54.5 seconds (about 30 fps), median 23 ms, 90th percentile 27 ms,
95th percentile 29 ms. Android classified 98% as janky against its 60 Hz
deadline; locked 60 fps is not claimed. No AndroidRuntime errors were reported.
The signed 0.10.1 update is installed, with the synthetic preview paused.
Artifacts: `build/gravity-stellar-comet-fix.png` and the 65-second
`build/gravity-stellar-comet-fix.mp4`.

## 0.10.2 — Born-at-source trails and compact breakup

New bodies no longer draw a ready-made gas ribbon populated with grains along
its whole length. A transient stripping age begins only after capture, or for
an inward-moving comet inside the visible attraction region. The leading edge
advances from the surface at a bounded visual speed, and grains are born at
the source at short intervals. No grain may appear ahead of that front. Newly
created physical fragments start with empty trails. Loading rebuilds trails
from empty without changing saved mass, capture progress or upgrades.

Planetary bodies no longer use the uniform tidal stretch that produced oval
remnants. Mantle sectors peel away at different times, exposing angular missing
sections around a compact central remnant. Physical fragments use a faceted
outline from birth. Dense core size decreases with remaining mass; mantle
openings do not puncture that core. Detached decorative chunks now remain
compact and tumble along the inward flow instead of stretching into long
slivers. Stellar remnants also use a much smaller overall deformation, leaving
the pronounced stretching to their external plasma filaments.

This changes visual geometry and particle placement only. Erosion rates, split
mass conservation, boss health, XP processing and fitness accounting remain
unchanged. No new persistent save fields or textures are needed. Existing body,
grain, mesh and per-frame fragment budgets remain in force. All 2,686 checks
pass, including empty trails on spawn/load, inward activation, advancing fronts,
grain birth limits, pause, compact remnants and outline continuity.

Final tablet sample after recording stopped: 1,616 frames in about 54.8 seconds
(roughly 29.5 fps), median 24 ms, 90th percentile 28 ms and 95th percentile 31 ms.
Android classified 98% as janky against its 60 Hz deadline; no locked 60 fps
claim is made. No AndroidRuntime errors were reported. Version 0.10.2 is
installed and the synthetic preview is paused. Artifacts:
`build/gravity-trail-early.png`, `build/gravity-trail-grown.png`, and the
35-second `build/gravity-trail-birth-breakup-final.mp4`.


### 0.10.3 - boss graphics and isolated visual sample

Boss surfaces use cached 256px spherical textures, with granular stellar surfaces, sunspots, animated convection and limb plasma loops. Cached coronas avoid per-frame blur. Ordinary objects pause spawning during a boss; ambient dust drops to 15%, without accruing a spawn backlog. Comets retain an approach wake that bends toward the well and sheds over 2.2 seconds after capture. Their mass, orbit and erosion balance remain unchanged.

GravityWellPreviewActivity renders a Sun, red giant or comet with the same native renderers as gameplay. It never opens career or athlete files. Launch with `adb shell am start -n org.openrow.probe/.GravityWellPreviewActivity`. Replay resets only the sample.

Validation: 2,696 automated checks passed. Live Sun sample recorded on the tablet, with no AndroidRuntime errors. Recording adds significant rendering overhead; this sample is not a guarantee of steady gameplay frame rate.


### 0.10.4 - connected streams and rounded stellar cores

All material ribbons now terminate beneath the opaque horizon instead of at an angle-dependent disk ellipse. Comet inlets can grow to the horizon and keep their growth on the outward leg of an orbit. Stellar fragmentation preserves established stream and animation ages, avoiding a second stream birth mid-encounter.

Sun-like stars and red giants retain a spherical core that shrinks with the cube root of remaining mass. Their surface textures and corona shrink together; stretching stays in the plasma ribbons. The spherical surface uses cached bitmap draws rather than mesh deformation. Ring breakup is unchanged. The isolated preview accepts a float `remaining` intent extra for late-consumption inspection.


### 0.10.5 - smooth capture handoff

Capture retains incoming velocity and eases the orbital angular rate and ellipse over 2.4 seconds. A critically damped orbit follower replaces direct position interpolation, with acceleration bounded to 140 scene units per second squared. Rear-side entries no longer immediately drop toward a smaller fixed ellipse. Settled bodies keep the same feeding-orbit scale and mass-based shrinkage. No new save fields are needed; saved position, velocity, phase and capture age continue the transition.

The isolated visual lab has rear/front capture controls (optional integer intent extra `approach`: 1 rear, 2 front, 0 orbit). Tests cover first-frame direction, bounded velocity changes throughout settling, bounded positions and deterministic save/restore for planets, stars and comets from both sides. All 6,093 checks pass, including unchanged 30-minute progression results.


### 0.10.6 - continuous stellar cloud intensity

The stellar visual remnant is now stable before and after physical shearing. Extra filaments gradually gain opacity and spread over capture age and lost mass; geometry and stream ages stay continuous through the split. Filament widths and opacity share a combined brightness budget. Physical stellar fragments also share the original particle emission budget, avoiding a 2-4x jump in decorative particles when upgrades activate. Faster physical erosion is unchanged.

Validation: 6,351 checks pass. New regressions cover Sun and red giant ranks 1-3, identical stream geometry and fade across fragmentation, combined brightness normalization, and actual emitted particle counts over the same interval. Tablet sample: build/gravity-smooth-cloud.mp4.


### 0.10.7 - full visual audit

Reviewed all 17 body classes at three destruction stages, ring/comet motion, gameplay and menu layouts on the tablet. Fixed full-length moving grains, stellar particle origins, hard gas-cloud edges, competing planetary lighting, shading/glow scale on remnants, detached planetary atmosphere rims, premature filament HUD labels, cover-art consistency and summary singular wording. Added isolated native contact sheets and crowded-scene fixtures. See GRAVITY-WELL-VISUAL-AUDIT.md for evidence, performance measurements, remaining priorities and validation. All 6,574 automated checks pass.


### 0.11.0 - featured stellar encounters

Brown dwarf, red dwarf, Sun, red giant, white dwarf and neutron star now have individual surface palettes and structure, cached spherical rotation, varied coronas and evolving prominence activity. The neutron star adds sweeping beams and a magnetic ring. Twenty-four 256px frames per class are generated off the animation thread and blended with stable spherical limb shading. Career startup prewarms the cache, prioritizing a resumed boss.

Stellar encounters smoothly dim surrounding bodies and suppress extra streams, ambient dust and non-boss decorative ejecta while keeping physical erosion and rowing conversion intact. Planetary presentation is unchanged. Preview controls cycle through all six classes. See GRAVITY-WELL-BOSS-VISUALS.md for design, budgets and tablet evidence. All 6,897 automated checks pass.


### 0.11.1 - rowing-responsive effects and six neutron beams

Added three independently sweeping neutron-star beam pairs. A shared smoothed and eased rowing response now drives decorative particle density, plasma brightness/width, corona intensity, prominence brightness and beam strength/reach. The global particle and stream caps remain, and decorative intensity does not create mass or XP. Live S4 intensity uses measured speed, while synthetic mode uses simulated watts. The isolated preview has an Input control for easy/steady/hard/coasting comparison. See GRAVITY-WELL-ROWING-VISUALS.md for response values and tablet evidence. All 7,010 automated checks pass.


### 0.12.1 - trainer-aware athlete-relative visuals

Replaced fixed absolute-speed artwork input in real rowing with recent athlete observations where available and phase-guided provisional visuals otherwise. The shared trainer frame carries category and display-only reference metadata. Guided visuals saturate at prescribed phase ceilings, including quiet recovery and cool-down, while the trainer clock remains independent. Separate smoothed physics/visual inputs preserve mass, XP and measured metrics, with backward-compatible optional checkpoint keys. Added an isolated shortened real-runner demonstration for two athlete references and unknown history. All 7,219 automated checks pass. See TRAINER-VISUAL-INTEGRATION.md.


## 0.13.0 — first test campaign

The first three campaign chapters, deterministic first-clear and repeat boss expeditions, shared harvesting economy, stable-ID 30-node upgrades and synthetic-only acceleration/checkpoints are now implemented. See [test campaign guide](GRAVITY-WELL-TEST-CAMPAIGN.md) for pacing, migration and validation. The trainer retains workout authority; real prestige is deferred while cosmetic legacy rewards can be previewed in the lab.


## 0.14.0 — node tree and guided-rowing UX

The upgrade card pages are now a connected branch/chapter overview with tap-to-inspect details and deliberate purchases. Guided rowing is organized into Train today, Progress and Preferences, with readable phase previews and persistent start controls. Menu and feedback copy has been simplified. See [UX guide](GRAVITY-WELL-UX.md).
