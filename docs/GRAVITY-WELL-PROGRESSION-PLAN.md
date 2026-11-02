# Gravity Well campaign, bosses and skill tree

Design proposal, 2026-10-29. The first three-chapter test campaign is now implemented in 0.13.0; see [current campaign guide](GRAVITY-WELL-TEST-CAMPAIGN.md). The broader galaxy/tree content below remains a future design proposal.

## Starting point

The current prototype has 17 body classes, ten boss classes, nine upgrade nodes and 31 purchasable ranks. Boss eligibility uses career distance. After 45 seconds of rowing without an active boss, the director selects the highest eligible class or an earlier eligible boss. Ordinary object spawning pauses during a boss; unfinished encounters persist across sessions.

The trainer owns workout timing, phase, effort guidance and completion. Athlete-relative visual references are display-only. Physics and XP still use their existing input: personalized graphics do not yet make game progression equally paced across athletes.

## Campaign structure

Use a permanent expedition with chapters. Each chapter adds a body behavior, a featured encounter and several upgrade routes. The first clear unlocks the next chapter and a new part of the tree. Further clears provide ordinary resources and mastery progress without repeatedly granting the first-clear reward.

| Chapter | Ordinary discoveries | Featured encounters, in order | Main lesson and upgrade opportunity |
|---|---|---|---|
| 1. Debris field | Meteoroids, rocky/metal asteroids, comets, rocky/icy moons | Dwarf planet | Attraction, capture, peeling and processing; first resistant core |
| 2. Planetary systems | Varied rocky and icy material | Rocky planet | Mantle erosion, exposed core and controlled fragmentation |
| 3. Giant worlds | Gas clouds and planetary debris | Gas giant, then ice giant | Rapid ring capture, envelope stripping and reservoir management |
| 4. First starlight | Earlier discoveries with stellar debris | Brown dwarf, then red dwarf | Sustained plasma extraction and stellar shearing |
| 5. Solar furnace | Solar material and selected earlier encounters | Sun-like star | Convection, prominences and multiple connected plasma streams |
| 6. Giant's envelope | Rich debris from previous chapters | Red giant | Fast outer-envelope loss around a more persistent core |
| 7. Compact remnants | Remnant debris | White dwarf | Small, dense target; concentrated extraction and processing |
| 8. Pulsar frontier | Rare remnant material | Neutron star | Magnetic/polar visual phases, dense core and advanced disruption |

These are encounter difficulty tiers, using the game's existing artistic mass and size scale. Later expansion can add binary encounters, magnetar variants, rogue worlds and unusual compositions after the core campaign works well. Binary encounters need a separate tablet rendering budget before inclusion.

Each boss has a recognizable approach, capture, outer-layer disruption, core and completion. Gas and stellar classes use envelopes instead of rocky shells. Magnetic beams are a visual identity, not an instruction to increase rowing effort. Existing irregular chips, ring breakup and round stellar remnants remain the visual foundation.

Earlier bosses become optional repeat encounters. Their debris can join the ordinary material mix. Do not automatically populate normal scenes with multiple full stellar bosses. Full-size stellar bodies retain featured-event presentation; rare elite variants can renew familiar encounters later.

## Unlocking and scheduling bosses

1. Discover the chapter through accumulated gameplay progress and the previous chapter's first clear.
2. Reveal its boss and show the available preparation upgrades. Recommended tools help; no single optional branch is mandatory. Every encounter has a viable baseline completion route.
3. Fill an encounter-readiness meter through eligible active play. Preserve it across sessions, together with the next selected encounter.
4. Introduce the boss during an appropriate work window, with a short approach cue and reduced background activity.
5. Save progress through recovery, session completion, disconnects and app restarts. Resume the same encounter in a later work window.
6. Record the clear once, issue its reward once, and begin the next encounter cycle after a brief return to ordinary play.

Replace random highest-unlocked selection with an explicit pending encounter. New campaign bosses take priority when ready. Between discoveries, draw from a weighted repeat pool with recent-repeat protection. Show the next campaign encounter on the career screen.

Provisional pacing targets for playtesting:

- First boss becomes ready after roughly 2-3 eligible work minutes of introductory play.
- Subsequent readiness takes roughly 4-6 eligible work minutes outside an active encounter. Stronger bosses need not imply longer empty waits.
- Early bosses take roughly 1-3 eligible work minutes to consume with appropriate upgrades; later featured encounters roughly 4-8. These are cumulative game-time targets, not workout prescriptions or deadlines.
- Start a fresh encounter only when a guided work window has enough time for its approach and capture presentation; otherwise retain readiness for the next work window. Short sessions must still advance readiness and saved bosses.
- Recovery does not fill readiness. An existing boss can remain visible with subdued effects; no timed escape, lost reward or demand to finish before recovery.
- Free rowing uses the same persisted director, with active rowing as its eligible time source.

Timing values must be configurable and measured in tests, not scattered constants. Longer sessions naturally cover more game time, but splitting equivalent play across sessions should not repeatedly reset or multiply readiness and rewards.

## Hundreds of upgrades

Plan for about 200 distinct nodes and roughly 500-700 total purchases across their ranks. This is a content capacity target, not a requirement to ship hundreds of nodes immediately. Use short 1-5-rank nodes, visible branch milestones and meaningful behavior changes.

| Branch | Initial full-tree allocation | Examples |
|---|---:|---|
| Attraction and capture | 25 nodes | Reach, smoother capture, comet retention, capture stability |
| Orbital control | 25 | Reduced escape, debris routing, controlled inward drift |
| Surface disruption | 30 | Mantle peeling, pole/face erosion, fractures and core exposure |
| Accretion and processing | 25 | Matter throughput, handling mixed materials, processing efficiency |
| Reservoir and flow | 20 | Capacity, overflow recovery, steadier supply during encounters |
| Discovery and expedition | 25 | New material variants, encounter choices, chapter side routes |
| Stellar and remnant tools | 30 | Envelope stripping, plasma channels, dense-core disruption |
| Visual expression | 20 | Disk palettes, material colors, beam styles and completion flourishes |
| **Total** | **200** | |

Branches open progressively. The tablet shows a chapter overview and a focused branch with a few nearby choices, prerequisites and next milestone. It never needs to render 200 equally detailed buttons at once. Keep purchasing between sessions and allow queued purchase intentions if useful.

Example planetary route: mantle erosion ranks -> uneven fracture unlock -> exposed-face erosion ranks -> core-coupling milestone. A parallel processing route can make the same boss rewarding through better use of stripped material. Both contribute; neither should require buying every node in the other route.

Use mostly additive bonuses within a stat family, explicit caps and a small number of deliberate synergies. Extend progression through new materials, tools and encounter variants rather than multiplying health, prices and bonuses forever. Visual purchases must respect the same particle, path and cache budgets as default artwork.

## Rewards and economy

Keep XP as the primary spendable resource. Use first-clear records as permanent chapter/branch unlocks; initially avoid adding a second mandatory spendable currency. A first clear can give a bounded, one-time XP grant and a distinctive visual unlock. Repeat encounters should remain worthwhile through their material yield and mastery milestones without outclassing every ordinary activity.

Budget each chapter so a player can afford a viable route before its boss becomes frustrating. Respec should recover enough value to correct a build without requiring a fresh campaign. No purchase should permanently block another chapter.

The current exponential 1.7-per-rank pricing is a prototype. Set costs by chapter budgets and expected time between meaningful choices. Tune prices alongside processing capacity and yields, so extra matter does not mostly become invisible overflow. Prevent feedback loops between XP multipliers, discovery yield and processing multipliers.

Keep workout progression and expedition progression independent. No boss unlock requires a raw speed, wattage, sprint or harder-than-prescribed effort. For equitable game pacing, evaluate a separate, bounded gameplay contribution based on verified movement and prescribed work participation. Define and test that rule before implementation: the existing visual reference is not a validated physical-capability estimate and must not silently become a scoring multiplier. Unknown profiles need a useful baseline. Easier prescribed sessions and returning athletes should continue advancing.

## Implementation foundation

- Store chapter, boss encounter and node definitions separately from rendering code, with stable string IDs and a content version.
- Nodes define rank costs, prerequisites, chapter gates and typed effects. Support explicit all/any prerequisite groups, with graph validation for cycles, dangling references, unreachable nodes, invalid ranks and impossible chapter dependencies.
- Save owned ranks by node ID rather than extending the current nine-position array. Migrate all existing ranks, XP, unlocked access and active bosses without resetting careers. Grandfather existing access where the new chapter sequence would otherwise relock it.
- Persist director state: chapter, pending encounter, readiness, active encounter, first-clear records, repeat history and claimed reward IDs. Completion and reward claims must survive save/reload without duplication.
- Keep one featured boss group and fixed production rendering caps. Upgrades change bounded gameplay parameters and visual choices; they do not grant unbounded fragments, trails, beams or concurrent bosses.
- Separate trainer signals, encounter rules, economy and presentation. Trainer completion always ends the session even with an unfinished boss. Missing telemetry and pause must not advance readiness or grant participation rewards.
- Version and validate content at load/build time. Unknown future content must fail through the existing non-destructive save-error behavior rather than silently dropping purchases.

## Delivery sequence

1. Build the data-driven node model, migrations and persistent chapter/director state. Keep current artwork and a small tree while verifying continuity.
2. Deliver chapters 1-3 with roughly 30-40 nodes, first-clear rewards and readable progression UI. Tune capture, boss duration, costs and overflow on the tablet.
3. Add chapters 4-6, stellar-specialist branches and distinct boss layers. Expand toward 80-100 nodes using measured pacing.
4. Add compact remnants, mastery variants and the remaining branches toward 200 nodes. Add prestige only if long-term play shows a need; never reset the athlete's training profile with game prestige.

Acceptance checks include deterministic boss order, repeat protection, reward idempotence, old-save migration, build viability, short-session continuity, recovery with a pending/active boss, a full-tree graph validation, and representative tablet rendering checks. Simulation should compare different session splits and gameplay inputs to expose grind, runaway income and progression gaps before large amounts of content are authored.
