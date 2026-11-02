# Gravity Well visual scoring rubric

The scorer is the separate **Gravity Well independent graphics review** task. This rubric follows the user's expanded reference. The ideal is both convincing simulation-like behavior and exceptional optical presentation, not merely a larger particle count or brighter glow.

| Score | Anchor |
| --- | --- |
| 0 | Simple circles disappear into a basic black circle. No meaningful surfaces, breakup or flow. |
| 2 | Color, glow and particles identify consumption, but movement and destruction remain generic. |
| 4 | Recognizable materials and some fragments, with obvious sprites, repetitive motion or detached effects. |
| 6 | Convincing stylized surfaces, readable damage and mostly connected consumption. Simplifications remain conspicuous. |
| 8 | Coherent, detailed destruction, volume, source-connected flow, natural motion and consistent optics across a complete encounter. |
| 10 | An ideal visual simulation of real space objects consumed by a real black hole, enhanced by exceptional light and optical effects, with enough coherent detail and variation to remain captivating for hours. |

Ten is deliberately aspirational. A visual score does not establish scientific validity. Additional glow cannot substitute for believable material behavior, geometric continuity or convincing motion.

## Separate criteria, each scored 0–10

1. **Visual interest and composition:** focal hierarchy, scale, readability, variation and spectacle at normal tablet viewing distance.
2. **Textures and materials:** spherical appearance, surface landmarks, material identity, illumination and consistency between intact objects and fragments.
3. **Black hole and optics:** opaque shadow, disk depth and turbulence, lens-like images, coherent light response and foreground/background occlusion.
4. **Destruction:** cracks opening, matching missing regions, progressive layer loss, exposed interiors and continuity through completion.
5. **Chunking and volume:** substantial pieces, complementary fracture faces, changing faces during tumble, differentiated rock/ice/gas/plasma behavior.
6. **Stellar and boss consumption:** deformation, corona attachment, visible stripping and an unbroken transition from source into the feeding stream.
7. **Object motion:** coherent approach/capture/orbit transitions, apparent inertia, controlled tumbling, smooth silhouettes, depth crossings and absence of snaps or distracting loops.
8. **Material flow:** continuous source attachment, emission fronts, advection, inward acceleration, curved trajectories, heating and integration with the disk; absence of disconnected ribbons or stationary decorative noise.

Report an overall judgment with reasons, rather than treating procedural complexity as evidence. Use no finer precision than half-points. When a reference or scale changes, explicitly re-anchor prior scores; old and new overall ratings are not automatically comparable.

## Required evidence and independent performance gate

Use one identified final build and matched scenes, including intact, first stripping, fracture, late remnant and completion. Include rocky and gas worlds, a sun-like star and compact remnants, front/rear approaches and normal/coasting input. Stills support material/shape judgments; object-motion and material-flow scores require time sequences or recordings. Reserve 8–10 motion scores for direct sustained-motion evidence. If playback is unavailable, use extracted, timestamped frame sequences and explicitly limit confidence in smoothness. A short clip cannot establish hours-long appeal or lack of repetition.

Performance is a separate acceptance constraint: approximately 30 actual presented FPS on the target Android 9 tablet. Report median and tail frame intervals, cold-cache behavior, stable ordinary/boss workloads and a fixed crowded workload. Distinguish draw-callback cadence, SurfaceFlinger actual-present timestamps and Android gfxinfo pipeline latency. Neither a fast renderer nor a high visual score excuses failure of the other requirement.

The isolated `benchmark=true,crowd=true` preview holds 29 bodies at fixed mass while animating them. It measures a stable rendering workload and must not be presented as a recording of actual consumption. Normal preview recordings provide the consumption evidence. Timings are taken without screen recording active. Record remaining stutters honestly; do not describe an average near 30 FPS as a locked frame rate.
