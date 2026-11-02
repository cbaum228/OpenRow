# Stellar boss visuals - 0.11.0

[0.11.1 follow-up: rowing-responsive effects and six neutron beams](GRAVITY-WELL-ROWING-VISUALS.md).

Brown dwarf through neutron star now have individual surface designs, spherical texture rotation, and distinct activity. Existing capture, shrinking remnant, shearing and connected-stream behavior is retained.

| Boss | Surface and motion | Atmosphere and activity |
| --- | --- | --- |
| Brown dwarf | Dark amber storm bands with smoky violet-brown patches and ember windows | Restrained ember corona and small, slowly changing prominences |
| Red dwarf | Crimson mottling, hot active regions and dark spots carried around the sphere | Uneven evolving plasma loops |
| Sun-like star | Gold turbulent photosphere, granulation and rotating sunspots | Bright corona, limb prominences and intermittent arcs across the visible face |
| Red giant | Broad convection cells, dark channels and bright plasma patches | Wider corona and larger, slower prominences |
| White dwarf | Pearly cyan/white surface with fine wisps | Cool blue corona and compact magnetic arcs |
| Neutron star | Violet magnetic surface with bright polar regions | Two sweeping blue-white beams and a faint inclined magnetic ring |

[All six designs on the tablet](../build/boss-showcase/bosses-new.png) | [half consumed](../build/boss-showcase/bosses-eroding.png) | [live six-boss recording](../build/boss-showcase/boss-showcase.mp4)

## Keeping the encounter focused

Ordinary-object spawning already pauses during boss encounters. Stellar encounters now also ease into a visual focus over two seconds:

- Existing background bodies fade to 32% opacity, returning to normal when the encounter ends.
- After the fade, only the two highest-priority non-boss material streams remain, at 30% intensity. The boss retains its full filaments and their shared brightness budget.
- New ambient dust falls from the previous boss rate of 15% to 3% of ordinary gameplay.
- Existing non-boss decorative ejecta emission falls to one fifth of its usual rate. Physical mass loss and rowing processing rates are unchanged.

The fade has no separate saved state: it derives from transient visual age and rebuilds smoothly after loading. Planetary encounter presentation is unaffected.

## Rendering approach

Each stellar class uses a seamless procedural longitude/latitude atlas, projected into **24 cached 256px sphere frames**. Two neighboring frames are blended while drawing. The limb shading stays fixed while surface features travel around the sphere, avoiding the previous flat-image spin.

All six sets contain 36 MiB of ARGB frame pixels in total. Surface generation runs serially on a low-priority background worker. Gameplay prewarms the cache from the career/dashboard path; a resumed boss receives priority. The isolated preview also prewarms the sets. A surface that is not ready uses a small fallback and crossfades to the completed texture instead of blocking animation.

Coronas and beam textures are cached. Prominences and visible-face arcs use a small fixed number of paths; neutron beams use two bitmap draws. No live noise generation, pixel rebuilding, full-screen bloom or extra physical fragments are added to animation frames.

## Validation

- All **6,897 automated checks pass**, including smooth focus, full boss opacity, background opacity restoration, reduced ambient dust, unchanged physical mass from visual fading, and existing capture/shearing/stream/save tests.
- The deterministic 30-minute rowing scenario still produces 3,364.704 XP.
- Native tablet comparisons cover full and half-consumed states for all six classes. The live showcase covers all six encounters.
- First cache generation measured roughly 0.85-1.38 seconds per class on the tablet, on the background worker. A six-class contact sheet measured approximately 76 MiB total process PSS after all caches were generated. These figures are specific to this device and test scene.
- A fully cached live red giant preview measured 23 ms median, 40 ms p95 and 48 ms p99 frame latency over 15 seconds without recording. This is not a locked-60-FPS result; normal gameplay uses its existing redraw schedule.
- The 29-body focus fixture measured 30 ms median / 57 ms p95 / 61 ms p99, versus 36 / 61 / 69 ms in the earlier audit fixture. The scenes evolve and this is a short observation, not a controlled benchmark or a steady-frame-rate guarantee. [Crowded focus sample](../build/boss-showcase/focused-crowd.png).
- No AndroidRuntime exceptions were reported during the final tablet checks.
- Final steady-state frame and memory diagnostics are saved in [red-giant-performance.txt](../build/boss-showcase/red-giant-performance.txt). Screen recording is disabled during that measurement.

The isolated preview now offers Previous/Next boss controls. Contact-sheet mode supports `audit=true,bosses=true`. These tools never open career or athlete files.
