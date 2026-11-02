# Trainer / Gravity Well visual integration - 0.12.1

## What was missing

The 0.12.0 runner and adapter already supplied authoritative phase timing, effort guidance, adjustments, pause state, telemetry availability and completion to Gravity Well. The visuals still used the generic `speed / 4` input. That meant equivalent effort from athletes with different observed speeds could look different, and recovery did not impose an artwork ceiling.

The trainer currently prescribes duration and perceived effort, not validated quantitative power or pace targets. This update does not invent those targets or change the training policy. It adds **display-only reference estimates** from usable local observations and a phase-guided fallback.

## Current integration

`TrainingController` publishes the existing immutable `GameWorkoutAdapter.Frame`, now with category, visual reference speed and observation count. `TrainingVisuals` is a shared, pure-Java adapter. Other games can use the same phase ceilings and reference estimates.

- A slower athlete at their own observed reference and a faster athlete at theirs receive the same visual request.
- References are kept separate by category and prescribed effort. Untested categories or effort levels stay unknown; no RPE-to-watts conversion or cross-category physiological inference is used.
- The visual estimate is the median of up to five recent observations, with at most one contribution per workout/category/effort combination. Data must be real, completed, manageable, unmodified, uninterrupted, feedback-bearing and sufficiently observed. Structured sessions also require overall reported effort within one point of the maximum prescribed effort.
- References use the last 14 days. Older benchmarks remain in training history but do not act as current display references after a break. Simulated, future-dated, incomplete, poorly observed, difficult or unanswered results are excluded.
- A session cannot calibrate itself. The controller snapshots references for the session instead of changing them continuously as the athlete rows. Foreground/resume refreshes the evidence; values still smooth into the scene.
- With no suitable reference, verified movement receives the prescribed phase's visual level. This is provisional presentation, not a claim that speed measured actual personal effort.

## Prescribed work stays authoritative

Work phases map the trainer's effort guidance onto a bounded visual level. Warm-up, recovery and cool-down have ceilings of 0.35, 0.22 and 0.25 respectively, and lower prescribed effort can reduce them further. These are artwork levels, not exercise targets.

During guided work, speed below the relevant observed reference modulates the display. At or above that reference, artwork saturates at the current phase's ceiling. Extra speed therefore cannot unlock brighter beams or plasma beyond the prescription. Easier adjustments lower that ceiling. Recovery can advance without rowing; no motion, missing telemetry, paused or completed frames request no active visual drive.

Free rowing also uses recent comfortable endurance observations when available. Without them it uses a steady provisional display, rather than interpreting a fixed absolute speed as everybody's effort.

The existing smoothed visual response still drives plasma, moving grains, decorative emission, corona and neutron beams. The disk uses that same visual input. Simulator/test power remains a separate, explicit mode.

## Data and game boundaries

Gravity Well now accepts separate physics and visual inputs. Visual calibration cannot change gravity input, consumed mass, distance, XP or workout duration. Game upgrades cannot change trainer guidance. Existing training clocks, pause/reconnect rules and completion remain authoritative.

The game checkpoint adds optional `game.visualField` and `game.separateVisuals` values. Old checkpoints fall back to the previous shared field. Smoothed visual response survives a game save/restore. No new training-history schema or athlete-record migration is required.

Training history deletion invalidates the visual reference cache too; it leaves game careers and lifetime stats intact. Real training and athlete history are never used as storage for preview fixtures.

## Verification

- **7,219 automated checks pass**, including the existing 72 trainer scenarios and seven trainer/game checks, plus **130 athlete-relative integration checks**.
- New tests cover two athletes with different speeds, reference equality, overspeed saturation, all phase/effort ceilings, easier adjustments, stale/missing/paused data, unknown and stale history, simulation exclusion, outlier resistance, category separation, save/restore, and independent completion with an unfinished boss and a lagging game simulation.
- Equal physical input with different visual calibration produces identical distance, mass consumption and XP. The deterministic 30-minute XP scenario remains 3,364.704.
- The isolated native tablet demo runs actual `TrainingCore.Runner` frames through the shared adapter and the production renderers. Its phases are deliberately shortened for software inspection and are not an exercise session.

[Slower athlete](../build/trainer-visuals/trainer-slow-work.png) | [faster athlete](../build/trainer-visuals/trainer-fast-work.png) | [recovery](../build/trainer-visuals/trainer-recovery.png) | [no recent reference](../build/trainer-visuals/trainer-provisional.png) | [live comparison](../build/trainer-visuals/trainer-visuals.mp4)

Launch the visual lab with `trainer=true` and `athlete=0`, `1` or `2`. **Input** switches between 1.5 m/s reference, 3 m/s reference and unknown history. All reference records are generated only in memory; the demo does not open training or athlete stores. The production training menu and workout review still use the real controller.

Normal-water sensor validation and assessment of exercise policy remain the separate checks described in TRAINING-ENGINE.md. The visual-reference estimates are not validated personal power/pace prescriptions or a physiological fitness score.


Tablet acceptance in this pass: inspected the real training menu, readiness check-in and review dialog without starting the reviewed workout. The native visual demo produced the expected personal-reference and recovery scenes with no AndroidRuntime exceptions. Training history was absent before and after the isolated demo, confirming that its generated reference records were not persisted. Live real-athlete calibration during a normal-water workout remains a separate acceptance check.
