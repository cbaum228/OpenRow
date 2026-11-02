# Gravity Well body catalog and bosses — 0.10.0

Current stellar appearance and event rendering budgets: [0.11.0 boss visuals](GRAVITY-WELL-BOSS-VISUALS.md).

Presentation correction in 0.10.1: stellar pieces share one elongated remnant
feeding plasma filaments, instead of appearing as several complete smaller
stars. Comet vapor now curves inward toward the well. Existing saved encounters
and their surface-area upgrades retain the same mass, health and erosion rates.

The catalog separates an object's class from its rendering material. Class
controls size, approach, erosion rate, boss status and fragmentation family.
Materials reuse cached rocky, icy, banded-gas and ocean surfaces; stars, clouds,
metallic asteroids and ice giants add small cached class surfaces. Mass and
size are game-scaled, not astronomical units or a physically scaled solar system.

Open **Body atlas** from the career screen to see all classes and their gates.
Unlocks use career rowing distance, not a required power or sprint threshold.
Synthetic and S4 careers retain separate progression and workout accounting.

| Class | Career meters | Encounter | Visual / behavior |
|---|---:|---|---|
| Meteoroid | 0 | Ordinary | Small, fast rocky arrivals |
| Rocky asteroid | 0 | Ordinary | Loose rubble and textured chips |
| Metallic asteroid | 100 | Ordinary | Dense gray material; slower erosion |
| Comet | 150 | Ordinary | Icy nucleus feeding an inward vapor stream |
| Rocky moon | 250 | Ordinary | Cratered mantle and core remnant |
| Icy moon | 400 | Ordinary | Pale ice and cool material streams |
| Dwarf planet | 500 | Boss | Compact layered world |
| Gas cloud | 600 | Ordinary | Translucent gas without a solid core |
| Rocky planet | 900 | Boss | Large mantle and resistant core |
| Gas giant | 1,500 | Boss | Rings, banded gas and broad streams |
| Ice giant | 2,200 | Boss | Blue bands and fragile rings |
| Brown dwarf | 3,000 | Boss | Dim glowing envelope |
| Red dwarf | 3,500 | Boss | Red plasma disruption |
| Sun-like star | 5,000 | Boss | Larger luminous plasma body |
| Red giant | 7,500 | Boss | Very large, faster-eroding envelope |
| White dwarf | 10,000 | Boss | Compact, slowly eroding stellar remnant |
| Neutron star | 14,000 | Boss | Small, especially resistant remnant |

## Boss encounters

The director waits for 45 seconds of rowing since the last boss, with eligible
classes determined by career distance. If the clock is already ready when a
class unlocks, the encounter may arrive immediately. The first eligible boss
uses the highest unlocked class; subsequent encounters alternate that choice
with a selection from earlier unlocked bosses. Ordinary matter continues to
arrive. There is only one active boss group, including all of its fragments.

The boss HUD shows its class, combined remaining mass and chunk count. Bosses
use larger visible sizes and wider capture/orbit regions than ordinary bodies.
Boss erosion and automatic splitting require rowing. Orbits and visual ejecta
can continue while coasting; explicit pause freezes the world. Bosses and their
health persist across session boundaries and app restarts. Completion records
a game-only boss count once, with no flat XP or workout-stat bonus. Stripped
matter still passes through the existing finite reservoir and input-driven XP
processing; overflow is handled as before.

## Fragmentation upgrades

Open **Upgrades**, then **Boss fragmentation →**. Purchases remain between
sessions. Each upgrade has three ranks, producing two, three or four physical
chunks. Once a boss has been captured for four seconds and lost at least 8% of
its mass, an owned matching upgrade splits it automatically while rowing.

| Upgrade | Base XP | Career gate | Prerequisites | Classes |
|---|---:|---:|---|---|
| Planetary fractures | 180 | 400 m | Gravitational reach, Accretion flow | Dwarf/rocky planets, gas/ice giants |
| Stellar shearing | 400 | 1,800 m | Planetary fractures, Deep-field discovery | Brown/red dwarfs, Sun-like stars, red giants |
| Remnant rupture | 800 | 4,000 m | Stellar shearing, Orbital reservoir | White dwarfs, neutron stars |

Successive-rank prices follow the existing 1.7× curve. A split conserves the
parent's exact remaining mass. Pieces retain class and material and separate into
nearby simulated orbits. Solid planetary chunks are visibly smaller and ragged.
Stellar fragments share one stretched visual remnant and several plasma filaments;
their combined health and surface-area benefit still use the underlying pieces.
They cannot split again via
the same automatic upgrade, and cannot acquire new rings. Their summed exposed
surface is greater. Partial exposure of the dense new faces limits the net
erosion-rate gain to 12%, 17% or 22% at equal remaining mass, rather than granting
the entire idealized geometric surface-area increase.

Fragments retain a shared boss identity; the health bar sums them and the boss
finishes only when the last piece is gone. Extra pieces do not earn extra
capture counts. Splitting itself adds no matter, XP, meters, strokes or energy.
The existing decorative mantle pieces and particles remain separate from these
physical upgrade-created chunks.

## Saves and tablet bounds

Existing version-1 career files migrate through optional additions. The six
original rank indices are unchanged; absent new ranks default to zero. Legacy
bodies keep their original material appearance and are not retroactively made
bosses. New saves include stable class IDs, boss group, fragment depth, erosion
scale, director clock and boss counts. Unknown classes or invalid boss/fragment
state are rejected by the existing non-destructive load-error path.

The world remains capped at 150 bodies and 240 decorative particles. Ambient
spawning reserves space while a boss is active. Material streams remain capped
at eight; textured decorative mantle fragments at 48 draws per frame. Boss
pieces share these budgets. No third-party engine or downloaded art is added.

Validation covers all 17 class round trips and gates, encounter scheduling,
coasting/pause, one boss at a time, all boss classes through completion, three
fragmentation families, exact split mass conservation, bounded erosion gains,
save/restore, old-career migration, prerequisites, and workout-stat isolation.

## Hardware validation

The signed 0.10.0 APK is installed on the Android 9 tablet. Both existing careers
loaded successfully. Synthetic-career XP purchased the planetary and stellar
fragmentation ranks through the new page. Live red-giant and brown-dwarf bosses
split into four chunks; combined health and completion/director progression
updated correctly. The synthetic session is paused with its next boss saved.
No real-career purchases or fitness-stat changes were made for this test.

After recording stopped, a gameplay sample rendered 1,204 frames in about
40.5 seconds (roughly 30 fps), median 22 ms, 90th percentile 27 ms and 95th
percentile 30 ms. Android classified 97% as janky against 60 Hz; this is not a
locked 60 fps result. No AndroidRuntime errors were reported. All 1,777 checks
pass. Preview artifacts:

- `build/gravity-body-atlas.png`
- `build/gravity-fragmentation-upgrades.png`
- `build/gravity-boss-catalog-final.png`
- `build/gravity-boss-catalog-final.mp4` (45 seconds)
