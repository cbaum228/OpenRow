# Gravity Well graphics development · 0.20.0

This pass follows the user's request to improve visual interest, material detail, black-hole presentation, destruction and chunking before the beta freeze. An independent, read-only review runs in the separate task **Gravity Well independent graphics review**. Its baseline is approximately 6/10 against the user's anchors: 0 means simple circles absorbed into a black circle; 10 means hyper-realistic worlds visibly breaking apart and stellar material drawn inward.

## Changes

- Four planetary boss classes now use cached longitude-rotation frames rather than spinning a flat image. Continents, cloud cover, polar ice, giant-planet bands and localized storms move across spherical projections. Sixteen 192-pixel frames per class are synthesized off the animation thread; total additional frame pixels are approximately 9 MiB.
- Solid bodies develop irregular surface faults as their existing mass decreases. Rock has warm exposed seams and ice has cooler fracture edges. Small/faded background bodies omit this detail, and at most eight bodies receive faults per frame.
- Detached mantle chips are larger, tumble at different rates and have textured top faces, shaded thickness and lit edges. Physical upgraded rocky fragments use angular surface patches instead of rendering miniature complete planets. Gas and ice giants no longer shed solid-looking mantle tiles.
- Stellar photospheres deform toward the well with a longer inward side and compressed neck. The erosion clip follows that deformation. Existing rotating stellar textures, coronas, prominences and neutron beams remain.
- Stellar and giant-planet streams contain narrow advecting-looking bright filaments inside their diffuse envelopes. The extra paths are limited to the main branch of the three largest stream sources, with no additional particle emitters.
- The accretion disk uses a small animated deformation mesh and asymmetric upper lens arcs. A separately drawn opaque shadow keeps the center black. This is illustrative light bending, not a ray-traced simulation.
- Bodies, debris and stream clipping now use the same projected front/back rule as the disk's -12-degree tilt. Whole-body center sorting remains an approximation; this is not full 3D occlusion.

The rendering pass changes no authored boss health, material yield, XP, training policy, exercise targets, save schema or physical fragment mass. The isolated preview accepts `fracture=0..3` for inspecting existing rocky fracture upgrades without modifying a career.

## Evidence

Native tablet screenshots:

- [Starting planetary destruction](../build/balance-tablet/graphics-before-planets.png)
- [Updated planetary destruction, 50% mass](../build/balance-tablet/graphics-020-planets-half.png)
- [Updated stellar contact sheet, warmed textures](../build/balance-tablet/graphics-020-stars-half-ready.png)
- [Upgraded rocky fragmentation](../build/balance-tablet/graphics-020-fracture-upgraded.png)
- [Final inward stellar deformation](../build/balance-tablet/graphics-020-sun-final.png)

Native recordings:

- [Upgraded planet fracture and chunking](../build/graphics-020/planet-fracture-upgraded.mp4)
- [Final stellar consumption](../build/graphics-020/sun-consumption-final.mp4)

The earlier `planet-destruction.mp4` and `sun-consumption.mp4` are intermediate iterations. `graphics-020-stars-half.png` shows temporary cold-cache fallback surfaces and must not be used as the final material comparison. Preview fixtures contain simulated input only and never open athlete or career files.

To reproduce contact sheets, launch `GravityWellPreviewActivity` with `audit=true`, `page=1`, and `remaining=0.5`, or `bosses=true` for the six stars. Wait for texture-cache completion before capture. Launch with `audit=false`, `kind=8`, `remaining=0.8`, `fracture=3` for rocky breakup, or `kind=13` for stellar consumption. Use a fresh activity/process to avoid retained preview extras. Recordings are laboratory scenes, not evidence of prescribed rowing effort or complete campaign balance.

## Verification and limits

The independent reviewer rescored the new stills and current source using the same rubric:

| Category | Starting evidence | 0.20.0 |
| --- | ---: | ---: |
| Overall visual interest | 6 | 6.5 |
| Textures and materials | 6 | 6.5 |
| Black hole and disk | 7 | 7 |
| Destruction | 4.5 | 6 |
| Object chunking | 4.5 | 6.5 |
| Stellar and boss consumption | 5.5 | 6.5 |

These are subjective scores. The reviewer did not play the recordings, so motion-dependent judgments remain provisional. It found that separated rocky pieces are much clearer, but their surface patches do not yet form complementary parts of the original planet. Stellar deformation is visible, but the corona and the leading surface-to-stream junction need more coherent attachment. Removing solid-looking chips from gas giants also removes a static damage cue; a future pass should replace it with stronger gaseous stripping rather than restoring rock slabs.

The full JVM regression suite and signed Android build pass. Native screenshots and recordings were inspected; runtime logs show no app exceptions. The original four owner save files are checksum-checked after the isolated lab runs. No real workout or save mutation is required to inspect these changes.

This remains a bounded Canvas renderer with projected textures and meshes. It does not yet provide complementary fracture faces from a unified planetary mesh, volumetric plasma, full 3D illumination, or physically traced lensing. Repeated local texture patches and rounded remnants remain visible limitations. A score of 8–10 requires stronger sustained-motion evidence and further work on those features, rather than simply adding more glow or particles.

Short tablet frame-time samples are stored beside the recordings. They are diagnostics, not a thermal endurance test or a guarantee of 60 FPS. Capture was disabled for timing samples. The final warm sun sample reported 25 ms median / 42 ms p95; the final crowded sample reported 40 ms / 61 ms. The old crowded sample reported 38 ms / 61 ms. Workload ages, sample lengths and body populations differ, so these numbers must not be claimed as a controlled speed comparison. The earlier 22/48 ms first-pass sample is not representative of the final crowded run. Crowded frame pacing remains a release-audit item.
