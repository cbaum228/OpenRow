# Gravity Well balance — 0.17.0

Implemented October 30, 2026. This supersedes the pacing and economy in the earlier
[progression audit](GRAVITY-WELL-PROGRESSION-AUDIT.md). Training prescriptions and
measured athlete statistics remain independently owned by the training/athlete systems.

## Main route and encounters

The reference main route includes preparation, ordinary material gathering, and
first-clear encounters. Its target is 48 **eligible WORK hours**; warm-up, recovery,
cool-down, menus, interruptions, and optional completionist farming are excluded.

| Boss | Stage budget | Prepared fight | No-upgrade fight |
| --- | ---: | ---: | ---: |
| Dwarf planet | 1 hour | 5 min | 40 min |
| Rocky planet | 4 hours | 5 min | 60 min |
| Gas giant | 4.5 hours | 5 min | 90 min |
| Ice giant | 4.5 hours | 5 min | 120 min |
| Brown dwarf | 5 hours | 5 min | 180 min |
| Red dwarf | 5 hours | 5 min | 240 min |
| Sun-like star | 6 hours | 5 min | 300 min |
| Red giant | 6 hours | 5 min | 360 min |
| White dwarf | 6 hours | 5 min | 420 min |
| Neutron star | 6 hours | 5 min | 480 min |

Each stage reserves five minutes for its reference boss and divides the remaining
time equally between Survey, Harvest, and Research. The opening boss can also be
selected early after 25 eligible minutes using **Try first boss** on the campaign
map. Fully preparing first remains an option. These are game pacing targets, not
instructions to row continuously or exceed a prescribed effort.

`GravityWellBalance` owns the authored budgets, upgrade releases, prices, reserves,
and resistance curve. Encounter integrity is independent of visible body mass and
material reserves. Physical pieces share the same integrity fraction; splitting
cannot multiply health or resources. Upgrading changes consumption speed while
remaining damage stays saved; it never causes a boss to gain health.

Combat readiness compares the owned loadout with the main tools available before
that boss: attraction 20%, processing 25%, erosion 40%, and applicable fragmentation
15%. When fragmentation has not been unlocked, the other weights are renormalized.
Storage, XP efficiency, material efficiency, and appearance retain their own roles.
For readiness `r` between zero and one, duration is:

`unpreparedSeconds × (300 / unpreparedSeconds) ^ (r × r)`

Readiness above one accelerates old encounters, with at most a fourfold additional
speedup. Relevant erosion specialists add a separately bounded bonus; attraction
specialists affect capture and yield specialists affect resources. Yield purchases
are not secretly treated as damage upgrades. Fully prepared main-only loadouts take
five reference-effort minutes; applicable specialists can reduce this modestly.

## Personal game effort

`GameplayEffort` is a separate scoring policy, not the display-only `TrainingVisuals`
reference. It uses up to five recent successful real sessions reported at effort
5–6, with valid work-block distance and observed motion. Two observations are needed
before using a reference; records older than 28 days and simulated workouts are excluded.
The median observed work speed becomes a provisional personal game reference.

Gameplay contribution is valid observed speed divided by that reference, bounded
at 1.25. With insufficient evidence, valid movement receives neutral provisional
contribution. Guided contribution is additionally capped by prescribed effort divided
by five; non-WORK phases, pause, unavailable telemetry, and no movement contribute
zero. The HUD labels a provisional reference explicitly.

This is a game normalization heuristic, not measured aerobic capacity, power,
VO₂ max, or a clinically validated effort model. It changes no exercise targets,
calibration records, real meters, strokes, calories, or energy observations. Boss
progress is retained across work blocks and workouts. Easy prescribed work remains
productive at its lower contribution, without any demand to finish before recovery.

## Upgrades and a sustainable economy

All 201 node IDs and 391 possible ranks remain. New campaign purchases use encounter
stage gates rather than raw career distance. Owned ranks remain useful after migration.
Three main-node layers are released for most bosses; gas giant and brown dwarf
release two, followed by the third for ice giant and red dwarf respectively.

Main nodes after the opening stage use the **previous boss's source material**.
No main upgrade needs the materials of the boss it is meant to prepare against.
Foundation disruption tools retain their discovery requirements. Source labels in
the purchase panel now use the actual campaign cost source.

- XP is earned from eligible time and normalized contribution, not from uncapped
  processing × refinement multiplication. Initial rate is one XP per reference
  work second. Refinement shares a +20% cap, with a positive effect for every rank.
- Main XP prices are allocated by stage budget, preserving the within-node 1.7
  rank-price ratio. The complete main catalog costs 161,580 XP in this version.
- Main material efficiency shares a +20% cap. Target yield specialists provide a
  further bounded +20% for their material; the purchase description reflects this.
- A first clear typically supplies about half the next stage's main source bill.
  Its harvest reserve is fixed and paid proportionally while consuming the boss,
  with a completion portion and a one-time first-clear bonus.
- Regular full-replay reserves are roughly 4% of that next-stage bill, with a
  two-unit minimum. Replays never reissue first-clear bonuses. Longer unprepared
  fights and larger visual mass cannot create larger reserves.

The first clear starts the next tier, rather than being its only material source:

- Miniatures of previously defeated bosses enter ordinary spawning every 90
  eligible seconds when a miniature slot is free. At most one is alive. Their
  approach is directed inward so an unlucky permanent orbit cannot block supply.
- A miniature has about 10% of the corresponding full encounter's durability and
  harvest reserve, uses smaller existing artwork, and keeps its original material
  identity. Missing materials for available upgrades influence which species appears.
- Full replays provide concentrated harvesting and become faster with later gear.
  Miniature or replay income never counts as a new first clear.
- Resource stripping can fill the game reservoir; its XP and material accounting
  remain separate from real physical observations.

## Retreat, queueing, and saved careers

Finish the current session, then select free play or another harvest on the map to
park a boss. Returning to its destination resumes its identity, integrity, and
physical pieces. A campaign encounter and old replays can be parked independently;
only one full featured encounter is active onscreen. The map displays the number
of saved encounters and an estimated remaining reference-work duration.

Reward IDs are individually recorded instead of relying only on a greatest-ID
watermark. An older parked encounter can therefore finish after a newer replay
without losing its legitimate reward or paying it twice.

**Preparation queue** is configurable on the real campaign map and enabled by
default in Coach mode. An explicit map choice is preserved across workouts and
restarts rather than being overwritten by that default. Completed preparation proceeds into the next preparation
activity, and a first clear can queue the next stage's preparation. New featured
bosses still require selection and a suitable work window. Selecting free play or
a repeat harvest also assigns the current preparation objective, so gathering
materials can advance the main route. Recovery and workout end times remain authoritative.

Save schema and balance version are now 2, using the existing file locations. Version-1
saves migrate preparation by completion percentage and active encounters by remaining
health fraction. XP, material balances, ranks, discoveries, and athlete records are
preserved. Legacy claimed-ID ceilings prevent old rewards from being reissued.
Invalid or future versions are rejected through the existing non-destructive save
error handling. No prestige or respec reset is introduced.

## Rebalance audit

Eight fresh full campaigns were run using production fixed-step physics, normal
career rules, and save/reload between sessions. No lab grants, gifted upgrades,
XP, or materials were used. The guided run uses synthetic 30-minute workouts
containing two ten-minute effort-5 work blocks with warm-up, recovery, and cool-down.
That is a software fixture, not an exercise recommendation.

| Path | Eligible route hours | Total boss work | Outcome |
| --- | ---: | ---: | --- |
| Balanced, low input / 1.5 m/s | 48.000 | 50 min | All main tools affordable before each boss |
| Balanced, standard / 2.5 m/s | 48.000 | 50 min | All main tools affordable before each boss |
| Balanced, high input / 4.0 m/s | 48.000 | 50 min | All main tools affordable before each boss |
| Balanced, second world seed | 48.000 | 50 min | Same budget; no material lock |
| Balanced, third world seed | 48.000 | 50 min | Same budget; no material lock |
| Focused combat branches + foundations | 48.000 | 50 min | Viable without every utility branch |
| Balanced, trainer-led with queue | 48.417 | 50 min | 146 workouts; prescription and recovery preserved |
| No upgrades | 85.333 | 2,290 min | First boss 40 min; neutron star 480 min |

All prepared first clears were five eligible minutes, and each balanced stage was
within 20% of its target budget. The guided path adds about 25 eligible minutes
through workout boundaries; its total session time is 73 hours including excluded
warm-up/recovery/cool-down. This distinction prevents reporting 48 hours as a promise
about total wall-clock time for every workout structure.

The standard route earned approximately 199,948 XP. Main upgrades consumed 161,580
(80.8%). Automatic optional shopping at the final clear left 384 total ranks and
about 2,368 XP. Reaching 391/391 then took a further 370 eligible minutes of ordinary
harvesting for XP, rather than two minutes and a few boss replays. The trainer-led
path needed 330 additional eligible minutes for optional completion. Focused players
retain more XP because they deliberately buy fewer utility branches.

Main-rank milestones now rise throughout the route: 63, 108, 136, 150, 178, 195,
237, 279, 321, and 366. This replaces the old stellar-era upgrade plateau. Three
world seeds and three raw input levels are still fixtures, not proof that every
player strategy will have identical timings; more player feedback can inform tuning.

Run `./simulate-progression.ps1` for all eight paths, or supply a named scenario.
`build/progression-audit/` contains per-stage eligible/session hours, per-destination
metrics, boss timings, purchase ledgers, completion summaries, and simulation-only
snapshots. Never import those snapshots into a real career. `./test.ps1` covers
1,186 campaign/balance/migration/reward assertions, 16 personal-contribution checks,
and the existing sensor, physics, history, training, and visual suites.

## Tablet verification

Version 0.17.0 was installed on the connected Android tablet after backing up its
app files. Verification used an isolated synthetic fixture:

- A miniature red giant rendered in the ordinary population and yielded its source.
- A prepared neutron star displayed a five-minute reference-work estimate.
- Pausing and leaving that encounter parked it at integrity `0.9565555555555314`.
  Selecting it again restored the same encounter ID and exact integrity.
- No fatal AndroidRuntime exception was logged during these checks.
- The original synthetic save was restored afterward. A property comparison verified
  547 original real-career properties, all 42 athlete-history properties, and all
  69 training-history properties were preserved. New migration metadata and appended
  body-schema fields are expected; measured observations and campaign resources were unchanged.

Backup, screenshots, and comparison helpers are in `build/balance-tablet/`. This
checks device integration and presentation; it does not establish physiological
validity or replace normal-water sensor validation. The tablet is left in the real
home dashboard with no synthetic activity credited to real records.
