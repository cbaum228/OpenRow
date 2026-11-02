# Gravity Well motion, flow and 30 FPS · 0.21.0

This pass targets the user's expanded visual standard while keeping the target Android 9 tablet near 30 presented FPS. The [independent scoring rubric](GRAPHICS-SCORING.md) now includes separate object-motion and material-flow criteria. Its 10 anchor is an ideal simulation-quality view of real space objects consumed by a real black hole, enhanced with exceptional optics and prolonged visual appeal. That is an aspirational visual reference, not a claim that this renderer is physically exact.

## Motion and optical changes

- The deforming fluid surface and its stream inlet now use shared geometry. Stars, gas giants and ice giants keep an attached inward-facing material root as their silhouettes change. The corona and stellar emission effects follow the deformation more closely.
- Gas giants now deform and feed fluid streams instead of remaining undamaged-looking round bodies after solid chips were removed.
- Feeding paths curve toward the inner disk. Bright material grains follow a nonlinear progression, accelerating inward rather than traveling at constant speed. Emission fronts still grow from the source and do not appear prefilled.
- Small looping material atlases advect internal density patterns. The primary fluid ribbon interpolates neighboring phases. Color progresses toward hotter/brighter material near the disk; the stream remains bounded and dark behind the opaque well.
- Substantial rocky fragments rotate with their existing angular state and change projected face width during tumble. This improves motion depth but does not yet provide a complete volumetric fracture mesh.
- Existing consumption activity produces a small, localized disk-light response, without a new particle emitter or a full-screen flash. It never changes mass, XP, fitness data or workout prescriptions.

## Rendering budget

The visual lab and active Gravity Well gameplay now use a vsync-aligned render loop targeting approximately 30 FPS. Game simulation still advances through its existing fixed steps using elapsed time; training and shared workout recording retain their independent clocks.

Ribbon selection, centerlines, vertices, filaments and grain positions are built once and reused for both front/back depth passes. Workload-based detail reduction prioritizes the featured body. Background stream sources are capped at three, four or six by detail level. On the target API-28 tablet the ceiling is the middle level; higher detail does not silently return after a light scene. Expensive background crossfades, small faults and extra compact-star beams can be reduced while preserving the primary consumption cue.

`GraphicsPerformance` records view-draw intervals and changes detail after sustained frame-budget pressure. `RenderLoop` schedules those frames rather than running the preview uncapped. These diagnostics are separate from actual display presentation and from Android gfxinfo pipeline latency.

For repeatable stress testing, the isolated preview accepts `benchmark=true,crowd=true`. This holds all 29 objects at fixed mass while animating their orbits and streams. It is explicitly labeled a fixed-load benchmark, not a recording of actual consumption. Ordinary preview mode remains the consumption fixture, with the same production renderers and no saved progression.

## Target-tablet presentation measurements

`measure-graphics.ps1` polls SurfaceFlinger actual-present timestamps, removes duplicate timestamps across samples and reports the measured display intervals. Screen recording is off during these runs. The final short fixed-load measurements were:

| Fixed workload | Actual presented FPS | Median interval | p95 interval | p99 interval |
| --- | ---: | ---: | ---: | ---: |
| Sun-like star + 28 bodies | 29.75 | 33.30 ms | 66.74 ms | 83.37 ms |
| Neutron star + 28 bodies | 29.93 | 33.30 ms | 66.67 ms | 83.41 ms |

Each sample contains roughly 650 displayed frames over about 22 seconds. Draw-callback intervals typically have lower tails than these presentation intervals and must not replace them in a performance claim. Average delivery meets the approximate 30 FPS goal in these short stress windows; occasional presentation hitches remain. This is not locked 30 FPS or proof of hours-long thermal stability. Cold-cache texture preparation also causes initial dips and must remain in release testing.

After the final corona correction, shorter repeated fixed-load samples confirmed:

| Final shared-corona workload | Actual FPS | Median | p95 | p99 |
| --- | ---: | ---: | ---: | ---: |
| Sun + 28 bodies | 29.93 | 33.31 ms | 50.13 ms | 50.45 ms |
| Neutron + 28 bodies | 29.93 | 33.31 ms | 50.30 ms | 66.59 ms |

These windows contain 327 and 324 frames over about 11 seconds each. Different window lengths and scene phases prevent claiming the corona correction caused a timing improvement. Raw results are `sun-fixed29-shared-corona-summary.json` and `neutron-fixed29-shared-corona-summary.json` beside the earlier samples.

Evidence: [sun summary](../build/graphics-021/sun-fixed29-capped-summary.json), [neutron summary](../build/graphics-021/neutron-fixed29-summary.json), plus raw SurfaceFlinger timestamps and draw logs in the same folder.

## Review evidence and checks

Normal-consumption recordings: [sun](../build/graphics-021/sun.mp4), [gas giant](../build/graphics-021/gas.mp4), [rocky fragments](../build/graphics-021/rock.mp4). These are separate from the fixed-load FPS benchmark and use simulated input only.

An attempted native video-frame extraction returned identical decoded images for every requested timestamp on this tablet. Hash checks detected this: **the files in `build/graphics-021/frames/` are invalid as motion evidence and must not be scored as a sequence.** The debug-only extractor now rejects duplicate-only output rather than reporting a false pass. Its baseline-sun input was the prior 0.20.0 recording, renamed solely for extraction, not new rendering.

Replacement evidence is captured directly from the live renderer: `build/balance-tablet/graphics-021-live-{sun,gas,rock}-{0..5}.png`, with actual sample times in `build/graphics-021/live-sequences.csv`. These sequences show real changes in source position, silhouette, fragment orientation and flow attachment. Sparse captures cannot establish sub-second smoothness or hours-long appeal; the separate presentation measurements and videos provide additional evidence without overstating that limit.

The sequences and videos precede the final corona-only alignment correction. The final surface and corona now use the same deformation map, replacing an approximate ellipse that left a dark crescent on off-axis views. [Final view A](../build/balance-tablet/graphics-021-corona-shared-map-a.png) and [view B](../build/balance-tablet/graphics-021-corona-shared-map-b.png) show the corrected attachment. The independent reviewer confirmed the previously reported gap is resolved in these two views; this is not proof across every point in a complete encounter.

## Independent scoring

Under the expanded ideal-simulation anchor, the separate reviewer rated overall interest 6, materials 6, black hole/optics 6.5, destruction 5.5, chunking 6, stellar/boss consumption 6, object motion 5.5 and material flow 6. Scores are subjective, with moderate confidence in sampled motion and lower confidence in sub-second smoothness. Its re-anchored 0.20 overall was 6; the earlier 6.5 used the previous reference and should not be treated as a directly comparable regression. Flow improved from its provisional re-anchored 5.5 to 6 with direct evidence. The corona defect correction did not justify an extra half-point from two stills alone.

The reviewer observed coherent sampled orbital progression, changing fragment orientation, and source-connected feeding paths. Remaining gaps to 8–10 include complementary fracture faces, true changing interior volume, less repetitive surfaces, visible surface peeling and mixing into the disk, capture/completion transitions, and sustained long-session evidence. Approximate 30 FPS passes on average in the supplied stress windows, with pacing-tail and thermal caveats remaining separate from visual scores.

All existing JVM regressions pass, including campaign, athlete accounting, profile, telemetry and training scenarios. A further 10,887 checks cover frame statistics, adaptive detail, shared inlet attachment across object classes and orbit angles, finite continuous paths, inward acceleration, horizon endpoints and unchanged saved physical/economy state. The signed Android build passes. Original owner records are checksum-checked after native fixtures. No normal-water exercise or real fitness input is used by these visual tests.

Remaining limits include complementary fracture geometry, true volumetric plasma, full 3D occlusion and scientifically traced lensing. Long-session visual variety, thermal behavior, and the remaining presentation hitches need further validation before assigning top-end scores or claiming beta readiness solely from this pass.

## 0.21.1 silhouette correction

The 0.21.0 deformation extended the inward hemisphere by as much as 65% and compressed its perpendicular width, making intact bodies look like eggs or rubber. Version 0.21.1 removes the width compression and limits inward surface extension to 8% for ordinary stars, 4% for gas/ice giants and at most 1% for white dwarfs/neutron stars (their existing pull-strength reduction lowers this further). Maximum overall axial aspect change is therefore about 4% for ordinary stars. Detached material retains the larger extension in the feeding streams.

One pure geometry helper now controls the silhouette, texture mesh, corona and material inlet. Physical mass, campaign balance and save formats are unchanged. The existing suite passes with 47,367 graphics assertions, including compactness bounds throughout consumption for all six fluid object types and agreement between surface and shared mapping. The signed 0.21.1 APK is installed on the tablet.

Direct native evidence: [star](../build/balance-tablet/graphics-0211-sun-compact.png) and [gas giant](../build/balance-tablet/graphics-0211-gas-compact.png), both launched at half mass and captured during isolated simulated consumption. Visual inspection confirms compact cores, attached glow and feeding paths. These stills do not establish smoothness across a full encounter or a new independent graphics score.

A separate fixed 29-object Sun benchmark measured 391 actual presented frames across 13.006 seconds: **29.986 FPS**, median 33.302 ms, p95 50.202 ms, p99 66.564 ms. Source: `build/graphics-021/sun-fixed29-compact-0211-summary.json` and its SurfaceFlinger timestamp log. This meets the approximate average target in this short sample, with occasional longer frames; it is not a locked-30 or thermal-duration result. All four original owner save hashes still match the recorded pre-correction baseline.
