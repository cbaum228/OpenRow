# Gravity Well — Complete Feature Inventory

> **Status:** Design backlog / scope candidates, not an implementation commitment.  
> **Game:** Stationary black-hole rowing incremental game.  
> **Design intent:** Always-on *during active play* dust; rowing attracts objects **and** processes stored matter; massive skill tree; post-row upgrades; optional prestige resets **skill-tree progression only**; lifetime rowing performance remains permanent outside the game.

## Non-negotiable design decisions

- The black hole remains stationary. No directional-gravity aiming or mandatory handle buttons.
- Cosmic dust is inexhaustible **while actively rowing**. The supply is enough for rewarding easy-effort sessions.
- Capture and accretion are separate simultaneous processes. Actual rowing is required to turn captured matter into substantial XP/growth.
- No offline gains or idle resource processing. Closing or pausing freezes the simulation and earnings.
- Long runs and segmented short sessions both count toward cumulative game distance gates; no per-session start/stop bonuses.
- Game upgrades affect the **game economy and spectacle**, never multiply or fabricate measured fitness statistics.
- Purchases primarily occur after a workout, not during an interval.
- Prestige exists as a future skill-tree reset only; exact rewards, timing, and other game-state reset effects are deliberately undecided.

## Priority legend

- **MVP** — essential feature or supporting capability for the first integrated playable; begin with the even smaller vertical slice below.
- **V1** — expected polished release backlog after the central loop works; candidates, not mandatory for one release.
- **Later** — expansion candidates or deferred complexity.

## Inventory at a glance

| Epic | Focus | Features |
|---|---|---:|
| DES | Game identity and constraints | 11 |
| TEL | Rowing telemetry and athlete input | 14 |
| SES | Sessions, pauses, and workout flow | 12 |
| PHY | Gravitational simulation and movement | 12 |
| MAT | Matter, dust, and objects | 14 |
| ACC | Accretion, destruction, and ongoing rowing rewards | 14 |
| EVT | Event director, challenges and pacing | 12 |
| FIT | Training modes and adaptive intensity | 12 |
| ECO | In-game economy and rewards | 11 |
| TREE | Skill tree, unlocks and evolution | 16 |
| PRE | Prestige and reset boundary | 5 |
| UI | HUD, visual presentation and audio | 13 |
| PASS | Fitness Passport and shared achievements | 11 |
| DATA | Persistence and data integrity | 10 |
| DEV | Development, testing and performance | 11 |
| **Total** | | **178** |

Priority counts: **64 MVP · 94 V1 · 20 Later**.

## Complete feature backlog

### DES — Game identity and constraints

- [ ] **DES-001 · MVP · Stationary black hole.** The singularity stays centered; no movement, directional targeting, or mandatory aiming.
- [ ] **DES-002 · MVP · Rowing-only primary interaction.** Attraction and accretion respond to rowing; gameplay works without extra buttons.
- [ ] **DES-003 · MVP · Two simultaneous loops.** Capture passing matter and process accumulated matter at the same time.
- [ ] **DES-004 · MVP · Guaranteed activity floor.** An ongoing cloud of cosmic dust supplies low-value material during active rowing.
- [ ] **DES-005 · MVP · No idle or offline gains.** Closing or pausing a game never creates matter, XP, or game advancements.
- [ ] **DES-006 · MVP · Independent career.** Gravity Well XP, upgrades, and evolution do not modify any other game.
- [ ] **DES-007 · MVP · Permanent real exercise record.** Measured rowing statistics live outside the game and persist across game resets and prestige.
- [ ] **DES-008 · V1 · Low-friction short or long play.** Make 5-minute segments and longer continuous sessions both rewarding, without restart farming.
- [ ] **DES-009 · V1 · Session-end upgrade cadence.** Earn rewards during rowing and allocate purchases mainly when a workout ends.
- [ ] **DES-010 · V1 · Workouts guide encounters.** Use a selected training outcome to shape the challenge, without a complex personal trainer.
- [ ] **DES-011 · Later · Optional hands-free spectacle mode.** Allow simplified viewing and reduced visual complexity without losing legitimate rowing progress.

### TEL — Rowing telemetry and athlete input

- [ ] **TEL-001 · MVP · S4 adapter.** Read supported monitor distance, strokes, cadence and estimated power when available.
- [ ] **TEL-002 · MVP · Normalized telemetry events.** Expose timestamp, source, power, stroke rate, stroke count and distance to game systems.
- [ ] **TEL-003 · MVP · Source-validity handling.** Mark stale, disconnected, absent and invalid telemetry instead of treating it as real zero-power input.
- [ ] **TEL-004 · MVP · Power smoothing.** Use separately configurable smoothing for attraction response, workout targets and display.
- [ ] **TEL-005 · MVP · Mechanical work integration.** Accumulate measured or estimated mechanical work from power over elapsed active time.
- [ ] **TEL-006 · V1 · Initial athlete ability estimate.** Use a brief introductory row and initial activity to establish relative effort bands.
- [ ] **TEL-007 · MVP · Simulated rowing input.** Support a keyboard/slider/test replay without a physical rower for development.
- [ ] **TEL-008 · V1 · SmartRow adapter.** Accept SmartRow data when connected, preserving provenance and differences from S4 estimates.
- [ ] **TEL-009 · V1 · Source selection and deduplication.** Avoid counting the same workout twice when multiple devices produce overlapping data.
- [ ] **TEL-010 · V1 · Per-athlete profiles.** Separate calibration, progression, workouts and personal records for people sharing the rower.
- [ ] **TEL-011 · V1 · Calibration refinement.** Update sustainable-effort and short-burst baselines conservatively from normal sessions.
- [ ] **TEL-012 · V1 · Optional heart-rate input.** Record compatible HR measurements; not required for core difficulty logic.
- [ ] **TEL-013 · V1 · Optional button support.** Support simple navigation and optional abilities with one or two Bluetooth buttons.
- [ ] **TEL-014 · Later · Workout import/export interoperability.** Provide standard exports and optional integrations without depending on cloud services.

### SES — Sessions, pauses, and workout flow

- [ ] **SES-001 · MVP · Start/resume/save workout.** Create a session and preserve both its earned rewards and physical metrics.
- [ ] **SES-002 · MVP · Duration presets.** Offer short, medium and long rowing sessions, including 5 and 30 minutes.
- [ ] **SES-003 · V1 · Active-play time accounting.** Count active rowing separately from menus, sensor dropouts and paused time.
- [ ] **SES-004 · MVP · Pause and resume.** Freeze progression and simulation on explicit pause; resume without losses.
- [ ] **SES-005 · MVP · Finish and reward screen.** Summarize rewards, objects, major encounters and permanent rowing statistics.
- [ ] **SES-006 · MVP · No start/stop XP bonus.** Repeatedly starting a five-minute session must not create bonus XP or matter.
- [ ] **SES-007 · V1 · Segmented workout chains.** Support a series of five-minute rows that aggregate toward the same cumulative milestones.
- [ ] **SES-008 · V1 · Uninterrupted long rows.** Allow extended rowing without forced upgrade screens or pauses.
- [ ] **SES-009 · V1 · Active recovery behavior.** During easy/zero-effort periods keep visual continuity without granting free meaningful resources.
- [ ] **SES-010 · V1 · Sensor disconnect behavior.** Pause progression safely and provide a clear reconnection path.
- [ ] **SES-011 · V1 · Session recovery.** Recover gracefully after app interruption or crash without duplicate rewards.
- [ ] **SES-012 · Later · Optional in-session milestone notice.** Show noninterruptive unlock previews without opening an upgrade menu.

### PHY — Gravitational simulation and movement

- [ ] **PHY-001 · MVP · Central 2D gravitational field.** Use a numerically stable, softened inverse-square-like attraction model.
- [ ] **PHY-002 · MVP · Power-based amplification.** Smoothed relative rowing power changes field strength within designed bounds.
- [ ] **PHY-003 · MVP · Object momentum.** Bodies enter with position/velocity; trajectories change via gravity rather than a progress meter.
- [ ] **PHY-004 · MVP · Distinct trajectory outcomes.** Support miss/flyby, temporary orbit, capture, and central consumption.
- [ ] **PHY-005 · MVP · Fixed-timestep physics.** Use a predictable physics update rate independent of rendering and sensor cadence.
- [ ] **PHY-006 · V1 · Consumption radius.** Define an inner region where adequately processed matter can be absorbed.
- [ ] **PHY-007 · V1 · Orbital-energy dissipation.** Apply simplified drag/accretion rules so selected captures spiral inward instead of orbiting forever.
- [ ] **PHY-008 · V1 · Escape and cleanup.** Cull objects that have genuinely left the play field.
- [ ] **PHY-009 · V1 · Rare encounter trajectories.** Generate controlled high-speed, oblique, and near-miss paths for meaningful intensity events.
- [ ] **PHY-010 · V1 · Selective collisions.** Allow collisions/fragmentation among limited important bodies.
- [ ] **PHY-011 · Later · Special multi-body encounters.** Approximate interactions with stars, moons or rival singularities only in authored events.
- [ ] **PHY-012 · Later · Advanced orbital interactions.** Offer optional resonance, slingshot or tidal-event behaviors as late-game unlocks.

### MAT — Matter, dust, and objects

- [ ] **MAT-001 · MVP · Infinite active-play dust stream.** Continuously replenish ordinary dust while a row is active so background matter never runs dry.
- [ ] **MAT-002 · MVP · Dust reacts to gravity.** Dust movement and collection visibly increase as rowing effort rises.
- [ ] **MAT-003 · MVP · Finite captured-matter reservoir.** Track harvested material awaiting active processing; avoid unlimited invisible stockpiling.
- [ ] **MAT-004 · MVP · Basic asteroid catalog.** Generate rocks with varied sizes, speeds, arrival angles and values.
- [ ] **MAT-005 · MVP · Processed versus captured distinction.** Award primary growth/XP after matter is actually processed, not only when entering orbit.
- [ ] **MAT-006 · V1 · Comets and ice bodies.** Faster arrivals and visually distinctive fragmentation.
- [ ] **MAT-007 · V1 · Metal-rich objects.** Higher-value matter with differing processing requirements.
- [ ] **MAT-008 · V1 · Debris swarms.** Collections of small bodies with shared stream behavior.
- [ ] **MAT-009 · V1 · Large moons and planetoids.** Longer processing challenges and milestone events.
- [ ] **MAT-010 · V1 · Material tags and rarity.** Define material class, density, work cost, XP, effects, and unlock tier from data.
- [ ] **MAT-011 · V1 · Individual object identities.** Track rare named objects or discoveries where relevant to trophies.
- [ ] **MAT-012 · Later · Planets and planetary fragments.** Multi-stage late-game material and dramatic breakup effects.
- [ ] **MAT-013 · Later · Stars and stellar remnants.** Major late-game mass and evolution milestones.
- [ ] **MAT-014 · Later · Exotic materials.** Fictional special properties that combine with upgrade effects.

### ACC — Accretion, destruction, and ongoing rowing rewards

- [ ] **ACC-001 · MVP · Continuous rowing-driven processing.** Convert stored matter to growth/XP only using genuine mechanical work during active rowing.
- [ ] **ACC-002 · MVP · Low-power processing floor.** Easy steady rowing must still consume dust and basic materials.
- [ ] **ACC-003 · MVP · Processing costs by material.** A material defines amount of game-scaled work required before consumption.
- [ ] **ACC-004 · MVP · Concurrent capture and accretion.** The athlete never has to switch between capture and chewing modes.
- [ ] **ACC-005 · MVP · No free processing at zero power.** At rest, animation can continue briefly but earned matter conversion stops.
- [ ] **ACC-006 · MVP · Visible consumption response.** Show dust streams and asteroid erosion tied to actual processed work.
- [ ] **ACC-007 · V1 · Layered object breakup.** Large material bodies visibly crack, shed particles and shrink over time.
- [ ] **ACC-008 · V1 · Accretion-disk density.** Reservoir size changes disk thickness, glow and visible fragments.
- [ ] **ACC-009 · V1 · Reservoir capacity and saturation.** Cap captured backlog and make overfilling affect capture behavior without destroying rewards.
- [ ] **ACC-010 · V1 · Dust starvation prevention.** When backlog is empty, dust supply remains processable and grants small rewards.
- [ ] **ACC-011 · V1 · Material-specific processing visuals.** Different erosion patterns for ice, rock, metal and later exotic materials.
- [ ] **ACC-012 · V1 · Chain-reaction processing.** Some upgrades/events produce cascades, but the awarded processing remains tied to active work.
- [ ] **ACC-013 · Later · Rare tidal disruption.** Large encounters burst into streams of material and staged consumption sequences.
- [ ] **ACC-014 · Later · Special accretion effects.** Plasma flares, jets and other spectacular visual milestones.

### EVT — Event director, challenges and pacing

- [ ] **EVT-001 · MVP · Ambient event scheduler.** Keep ordinary asteroids and dust arriving during play.
- [ ] **EVT-002 · MVP · Near-miss challenge.** Spawn some valuable objects whose capture rewards meaningfully higher effort.
- [ ] **EVT-003 · V1 · Effort-aware events.** Constrain spawn requirements to the current athlete baseline and workout objective.
- [ ] **EVT-004 · V1 · No mandatory sprint in easy mode.** Easy training remains achievable without violating its chosen intensity.
- [ ] **EVT-005 · V1 · Timed comet events.** Introduce obvious high-value windows with fair approach indicators.
- [ ] **EVT-006 · V1 · Asteroid streams and belts.** Provide long, recognizable capture sequences that suit steady rowing.
- [ ] **EVT-007 · V1 · Fragment swarms and collision events.** Create visually intense, finite encounters.
- [ ] **EVT-008 · V1 · Encounter diversity and cooldowns.** Avoid repetitive high-intensity events and overloading recovery periods.
- [ ] **EVT-009 · V1 · Catchable rewards with partial success.** Let an event award partial gains even when the biggest object escapes.
- [ ] **EVT-010 · V1 · Environmental stages.** Unlock regions with different backgrounds, distributions and event types.
- [ ] **EVT-011 · Later · Planetary-system flybys.** Authored or procedurally assembled large-scale interactions.
- [ ] **EVT-012 · Later · Cosmic boss events.** Rare major objects or rival gravity sources without conventional fighting controls.

### FIT — Training modes and adaptive intensity

- [ ] **FIT-001 · MVP · Workout goal selector.** Expose endurance, sustained power, HIIT, sprint and free-play choices initially.
- [ ] **FIT-002 · MVP · Athlete-relative effort.** Normalize challenge intensity against personal steady and short-duration output.
- [ ] **FIT-003 · V1 · Per-mode event recipes.** Define event frequency, capture difficulty and recovery windows from goal and duration.
- [ ] **FIT-004 · V1 · Effort success feedback.** Show whether current effort is suitable without punishing easy rowing.
- [ ] **FIT-005 · V1 · Training/game difficulty separation.** A skilled player can select easy physical effort without making every in-game interaction trivial.
- [ ] **FIT-006 · V1 · Aerobic-power/threshold variants.** Offer more specific sustained-hard and 2–5 minute interval patterns.
- [ ] **FIT-007 · V1 · Calorie-oriented session.** Prioritize sustainable accumulated activity, not continuously maximal effort.
- [ ] **FIT-008 · V1 · Recovery and technique mode.** Offer low-intensity sessions with gentle cadence consistency cues.
- [ ] **FIT-009 · V1 · Conservative ability adaptation.** Raise or lower targets after multiple meaningful sessions, not one lucky event.
- [ ] **FIT-010 · V1 · Player difficulty override.** Allow an easier/normal/harder physical challenge modifier.
- [ ] **FIT-011 · V1 · Duration-aware event scheduling.** Keep hard efforts and recovery suitable for selected session length.
- [ ] **FIT-012 · Later · Optional HR/RPE refinement.** Use optional heart-rate and perceived-effort feedback to adjust future estimates.

### ECO — In-game economy and rewards

- [ ] **ECO-001 · MVP · Cosmic XP currency.** Earn spendable game-specific XP primarily through rowing-powered consumption.
- [ ] **ECO-002 · MVP · Base dust rewards.** Guarantee low but nonzero active-rowing rewards during ordinary moments.
- [ ] **ECO-003 · V1 · Special-object bonuses.** Higher-value bodies and challenges provide exciting reward spikes.
- [ ] **ECO-004 · MVP · Distance-based unlock gates.** Important skill nodes also require cumulative *Gravity Well* rowing distance.
- [ ] **ECO-005 · MVP · No game multipliers on fitness data.** Boosting cosmic XP never multiplies real distance, strokes, energy or calories.
- [ ] **ECO-006 · V1 · Upgrade affordability display.** Show cost, purchased ranks, unmet prerequisites and distances still needed.
- [ ] **ECO-007 · V1 · Reward scaling curve.** Support very large numbers while preserving meaningful early and late progression.
- [ ] **ECO-008 · V1 · Reward provenance.** Allow XP receipts to be traced to actual processed matter/events for tuning and debugging.
- [ ] **ECO-009 · V1 · Alternative rewards.** Unlock cosmetic variants, rare matter, discoveries and objects without another mandatory currency.
- [ ] **ECO-010 · V1 · Repeat-session fairness.** Comparable total active work should produce comparable baseline economy regardless of split sessions.
- [ ] **ECO-011 · Later · Endgame economy tuning.** Balance exponential costs, diminishing returns and optional repeatable ranks.

### TREE — Skill tree, unlocks and evolution

- [ ] **TREE-001 · MVP · Data-defined node system.** Represent nodes by ID, cost curve, prerequisites, ranks, effect and tier.
- [ ] **TREE-002 · MVP · Branching prerequisite graph.** Support multiple branches, forks and nodes requiring more than one prerequisite.
- [ ] **TREE-003 · MVP · Purchase on session end.** Spend earned XP in a dedicated upgrade screen after rowing.
- [ ] **TREE-004 · MVP · Large-tree architecture.** Design for hundreds of nodes and multi-rank upgrades without hardcoding each behavior.
- [ ] **TREE-005 · V1 · Dust and attraction branch.** Nodes improve particle values, dust behavior or strength/shape of isotropic attraction.
- [ ] **TREE-006 · V1 · Accretion and processing branch.** Nodes improve rowing-powered conversion, fragmentation and process visuals.
- [ ] **TREE-007 · V1 · Encounter and discovery branch.** Nodes unlock body types, events and rare-material opportunities.
- [ ] **TREE-008 · V1 · Evolution and cosmetic branch.** Nodes unlock new singularity looks and visual effects.
- [ ] **TREE-009 · V1 · Cross-branch synergies.** Nodes combine two effect types for more dramatic results.
- [ ] **TREE-010 · V1 · Multi-rank purchases.** Support capped or repeatable rank effects with escalating costs.
- [ ] **TREE-011 · V1 · Expandable tier unlocks.** Introduce tiers when both in-game requirements and cumulative rowing gates are met.
- [ ] **TREE-012 · V1 · Progress-to-next-node UI.** Surface recommended affordable upgrades and exact remaining requirements.
- [ ] **TREE-013 · V1 · Build choices and respec policy.** Define optional in-run respec rules separately from prestige, if desired.
- [ ] **TREE-014 · V1 · Visual evolution stages.** Black hole and environment gain distinct looks at major tier milestones.
- [ ] **TREE-015 · Later · Expanded effect registry.** Add exotic gravitational, collision, resonance and visual-combination effects.
- [ ] **TREE-016 · Later · Large multi-hundred-node content set.** Populate and balance the full tree using reusable effects and data, not bespoke code.

### PRE — Prestige and reset boundary

- [ ] **PRE-001 · V1 · Prestige entry point.** Provide an explicit, confirmed action for the eventual prestige reset.
- [ ] **PRE-002 · V1 · Reset skill-tree progression.** Clear currently purchased skill-tree ranks and dependent tree unlock state; final details remain TBD.
- [ ] **PRE-003 · V1 · Preserve lifetime fitness history.** Never reset accumulated real distance, strokes, duration, mechanical energy, calories, or records.
- [ ] **PRE-004 · V1 · Preserve shared achievements and expeditions.** Fitness Passport, trophies, expedition progress and history are outside prestige.
- [ ] **PRE-005 · V1 · Keep additional prestige rewards unspecified.** Do not precommit to prestige currency, multiplicative bonuses or world reset rules yet.

### UI — HUD, visual presentation and audio

- [ ] **UI-001 · MVP · Readable main playfield.** Stationary black hole, active dust, objects, visible trajectories and consumption.
- [ ] **UI-002 · MVP · Minimal workout HUD.** Show time, distance, current power/effort and relevant status without covering gameplay.
- [ ] **UI-003 · MVP · Progression HUD.** Show XP earned this session, captured reservoir and approximate processing activity.
- [ ] **UI-004 · MVP · Clear session end summary.** Show workout metrics, earned currency and major unlocks.
- [ ] **UI-005 · MVP · Noticeable response to rowing.** Field visuals, pull strength and processing respond within a perceptually useful interval.
- [ ] **UI-006 · V1 · Scalable accretion disk rendering.** Use particle instancing or stylized effects rather than thousands of expensive physics bodies.
- [ ] **UI-007 · V1 · Rare object arrival cues.** Provide clear visual/sound previews before time-limited encounters.
- [ ] **UI-008 · V1 · Upgrade reveal cinematics.** Highlight newly enabled celestial interactions after purchases.
- [ ] **UI-009 · V1 · Node tree navigation.** Zoom, pan, filter branches, view prerequisites and purchase multiple ranks.
- [ ] **UI-010 · V1 · Sound design by mechanic.** Distinct cues for capture, orbit, erosion, breakage, consumption and evolution.
- [ ] **UI-011 · V1 · Accessibility settings.** Adjust contrast, flash, visual density, motion and sound without changing earned fitness credit.
- [ ] **UI-012 · V1 · Adjustable quality settings.** Provide low-end Android presets, especially for particles and postprocessing.
- [ ] **UI-013 · Later · Advanced cosmic visuals.** Gravitational lensing, bright plasma jets, nebula changes, stylized high-energy events.

### PASS — Fitness Passport and shared achievements

- [ ] **PASS-001 · MVP · Permanent rowing distance.** Maintain actual cumulative rowing distance independent of game XP and resets.
- [ ] **PASS-002 · MVP · Permanent strokes and time.** Track stroke count and active rowing duration per user.
- [ ] **PASS-003 · MVP · Mechanical energy and power statistics.** Accumulate work in joules/kWh; separately store instantaneous, average and peak power in watts.
- [ ] **PASS-004 · MVP · Estimated calorie accounting.** Show estimated metabolic calories, clearly labeled and not confused with mechanical kWh.
- [ ] **PASS-005 · MVP · Lifetime records survive prestige.** Prestige cannot delete or change real fitness metrics.
- [ ] **PASS-006 · V1 · Per-session workouts log.** Preserve historical summary and source provenance for each workout.
- [ ] **PASS-007 · V1 · Lifetime milestone badges.** Award milestones for distance, energy, strokes, time, workouts and estimated calories.
- [ ] **PASS-008 · V1 · Expedition Atlas contributions.** Credit real rowing distance toward selected expedition regardless of which game is played.
- [ ] **PASS-009 · V1 · Game-specific trophy awards.** Track Singularity feats in a shared museum without changing mechanics in any other game.
- [ ] **PASS-010 · V1 · Record integrity and deduplication.** Prevent duplicate sessions, resume exploits and simulated-input credit toward real fitness totals.
- [ ] **PASS-011 · Later · Performance trend displays.** Show optional improvements in sustainable power, sprint performance and consistency.

### DATA — Persistence and data integrity

- [ ] **DATA-001 · MVP · Local-first storage.** Play, track progression and save without internet.
- [ ] **DATA-002 · MVP · Separate save namespaces.** Keep game economy/tree apart from shared athlete/fitness records.
- [ ] **DATA-003 · MVP · Atomic save at session end.** Commit earned XP and physical measurements without duplicated rewards.
- [ ] **DATA-004 · MVP · Freeze game when closed.** No background simulation, offline resource accrual or delayed offline XP.
- [ ] **DATA-005 · V1 · Versioned game schemas.** Support future updates to upgrades and save formats.
- [ ] **DATA-006 · V1 · Mid-session checkpoints.** Preserve captured reservoir and current world state through interruptions.
- [ ] **DATA-007 · V1 · Per-profile progression.** Different athletes keep independent black holes and rowing milestones.
- [ ] **DATA-008 · V1 · Export and backup.** Allow user-owned save and exercise data to be backed up or exported.
- [ ] **DATA-009 · V1 · Currency and stat audit.** Record what was earned and prevent economy operations from mutating fitness history.
- [ ] **DATA-010 · Later · Optional account sync.** Potential future convenience only; never required for playing.

### DEV — Development, testing and performance

- [ ] **DEV-001 · MVP · Desktop physics sandbox.** Run attraction, orbit, processing and events with synthetic power input.
- [ ] **DEV-002 · MVP · Telemetry replay harness.** Replay timestamped sample rows repeatedly for reproducible balancing.
- [ ] **DEV-003 · MVP · Low-end Android target.** Keep GPU/CPU/ram use within measured Ergatta hardware limits.
- [ ] **DEV-004 · MVP · Config-driven tuning.** Store spawn rates, force scaling, resource costs and upgrade parameters in data.
- [ ] **DEV-005 · V1 · Fixed seeded random events.** Allow a seed to reproduce rare-event sequences in tests.
- [ ] **DEV-006 · V1 · Performance counters.** Measure physics timings, render frame rates, allocations and active bodies.
- [ ] **DEV-007 · V1 · Automated balance simulations.** Compare equal-work five-minute and thirty-minute session outcomes.
- [ ] **DEV-008 · V1 · Long-run progression tests.** Simulate many hours to detect runaway rewards and no-effort progression.
- [ ] **DEV-009 · V1 · Save migration tests.** Verify version upgrades and prestige leave fitness history intact.
- [ ] **DEV-010 · V1 · Optional asset-quality tiers.** Allow lower-cost rendering without changing physics, economy or achievements.
- [ ] **DEV-011 · Later · Accessibility and device matrix.** Validate tablets, monitors, rower adapters and input variants.

## Shared data contracts and persistence rules

| Data | Owner | Prestige impact |
|---|---|---|
| Rowing distance, strokes, duration | Shared fitness/athlete record | Never reset |
| Mechanical energy (J/kWh) | Shared fitness/athlete record | Never reset |
| Power samples and average/peak watts | Shared workout history | Never reset |
| Estimated calories | Shared fitness/athlete record | Never reset |
| Expedition progress and lifetime badges | Shared Rowing Passport | Never reset |
| Game-specific achievements in shared museum | Shared achievement record | Remain unlocked |
| Singularity XP, upgrade ranks, tier availability | Gravity Well career | Tree progression resets on prestige; economy specifics TBD |
| Captured matter and current world layout | Gravity Well save | Session/save retention required; prestige behavior TBD |

**Measurement semantics:** Power in watts is not a cumulative quantity. Accumulate *mechanical work/energy* as the time integral of power, in joules or Wh. Average power, peak power, and power-duration benchmarks are derived metrics. Calories are an estimate of human metabolic energy expenditure, not identical to mechanical work.

## Important balancing contracts

1. **Active effort invariant:** With no verified active rowing, no new meaningful XP or matter conversion can be earned. Visually coasting objects may remain on-screen during short pauses.
2. **Easy rowing invariant:** At an athlete-appropriate easy cruising effort, the dust/accretion loop never runs out of something to do.
3. **Session fairness invariant:** At comparable total verified work, 6 × 5-minute sessions and 1 × 30-minute session have comparable baseline earning potential; only continuous-session-specific awards may differ.
4. **Progression invariant:** Skill-tree multipliers affect game-only currency/materials/visuals. Game resets and prestige cannot touch fitness history.
5. **Difficulty invariant:** Training-intensity selection and strategic game difficulty are distinct settings; an endurance choice cannot force high-power sprints.
6. **Hardware invariant:** Decorative particles can be numerous; independently simulated important bodies must stay within real-world tablet performance budgets.

## Suggested first playable vertical slice

- Use synthetic or replayed rowing power with a stationary singularity and clearly visible dust.
- Continuously spawn drifting dust while rowing; attract it according to smoothed power.
- Spawn asteroids on plausible flyby and capture trajectories.
- Build a finite captured reservoir and consume it using integrated active rowing work.
- Award small dust XP and larger processed-asteroid XP.
- End a five-minute test session; show earnings and purchase a few data-defined upgrades.
- Save and resume with no offline growth; record separate lifetime distance/time/strokes/energy.
- Repeat with a 30-minute replay to test reward pacing and stability.

## Intentionally unresolved design choices

- Exact calibration curve mapping rowing power to field strength and processing rate.
- XP inflation curves, tier-distance thresholds, and proportions of rewards from dust versus events.
- Whether purchases are strictly end-of-session or optionally available during explicit manually paused sessions.
- Final prestige benefits, additional in-game assets reset, and whether distance gates use all-time or cycle-specific game distance; only skill-tree reset is committed.
- Whether major endgame bodies are absorbed continuously or pass through authored multi-stage sequences.
- Exact active-rowing threshold versus tolerable gaps between strokes; source-dependent detection matters.

## Excluded from current scope

- No moving black hole or manually aimed gravity cone.
- No autonomous idling, offline resource generation or automatic progression while not rowing.
- No cloud-first service, heavy physiological training optimizer, or required AI coach.
- No fully simulated N-body astrophysics or thousands of physical debris rigid bodies.
- No cross-game combat/economy advantages from Gravity Well upgrades.
