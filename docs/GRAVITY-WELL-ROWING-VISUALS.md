# Rowing-responsive visuals - 0.11.1

The visual response now includes decorative particle emission, plasma ribbon brightness and width, corona brightness, prominence intensity, and neutron-star beam strength and reach. Previously, rowing primarily drove gravity, erosion, orbit speed, disk activity and the comet stream's brightness; most other streams and emitters were nearly constant.

## Input and smoothing

The existing gameplay field is used as the shared input. Live S4 input estimates intensity from measured rowing speed (`speed / 4`, bounded to 0-1); it is not a measurement of watts. Synthetic gameplay uses `testWatts / 240`, also bounded. The existing field smoothing takes roughly a second to respond. A gentle nonlinear curve, `energy = field * (1.5 - 0.5 * field)`, gives readable increases without an abrupt on/off threshold.

| Settled input | Visual energy | Particle emission multiplier | Plasma brightness multiplier | Ribbon width multiplier |
| --- | --- | --- | --- | --- |
| Coasting | 0 | 0.55 | 0.38 | 0.80 |
| Easy (15%) | 0.214 | 0.796 | 0.513 | 0.886 |
| Steady (50%) | 0.625 | 1.269 | 0.768 | 1.05 |
| Hard (100%) | 1 | 1.70 | 1 | 1.20 |

These are effect controls, not workout-performance targets. A low baseline keeps the scene alive when coasting; established flows continue to move. Pausing still freezes the game. Particle timing accumulates the smoothed emission rate so a change in effort does not dump a backlog of particles into a single frame.

## Neutron-star changes

Three beam pairs now sweep at different rates and directions: six beams total. The primary pair remains strongest, with the two secondary pairs fainter. Rowing increases their brightness, width and reach; it does not change the rotation angle abruptly. All six reuse one cached beam texture. The existing magnetic ring and violet spherical surface are retained.

## Budgets and progression

The global 240-particle cap, eight selected stream-source cap, stellar focus suppression and shared filament brightness normalization remain. Changing decorative emission cannot award XP or change mass consumption. Existing gameplay effects of rowing intensity on gravity and erosion remain intact.

All 7,010 automated checks pass, including monotonic bounded response, denser particles under harder rowing, smooth decay when easing off, pause behavior, propagation into boss rendering, and equal mass/reservoir/XP with a saturated versus unsaturated decorative particle scene. The deterministic 30-minute XP scenario remains 3,364.704.

## Tablet preview

[Easy / hard / coasting recording](../build/rowing-effects/neutron-rowing-response.mp4) | [easy still](../build/rowing-effects/neutron-easy.png) | [hard still](../build/rowing-effects/neutron-hard.png) | [coasting still](../build/rowing-effects/neutron-coast.png)

The isolated preview's **Input** button cycles steady, hard, coasting, and easy input. It also accepts the `effort` intent extra, bounded to 0-1. Its simulated intensity is shown in the subtitle, and no career or athlete files are opened. The response recording changes input on the same neutron star without resetting the scene between intensity changes.

[Hard-input frame diagnostics](../build/rowing-effects/neutron-hard-performance.txt) were collected without screen recording.


Final tablet check: the hard-input six-beam neutron preview measured 20 ms median, 22 ms p95 and 27 ms p99 frame latency over roughly 15 seconds with recording off. No AndroidRuntime exceptions appeared. This is a short preview measurement, not a locked-frame-rate or long-duration guarantee.
