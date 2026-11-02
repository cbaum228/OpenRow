# Shared S4 workout recording · 0.19.0

OpenRow now records real workouts through a shared profile-scoped recorder, independently of game drawing. Home offers Start Row, Resume Saved Workout, and Workout History & Charts. Gravity Well, Castlebreaker and standalone rowing use the same session identity and athlete accounting.

## Recording and navigation

Only one real workout can be active per profile. Leaving a game, disconnecting, backgrounding, or switching profiles saves and pauses it. Home offers the original game, standalone rowing, or finishing the workout. Resuming is explicit. Another game requires finishing the current row first. Castlebreaker campaign rests do not accumulate rowing; campaign completion finishes its recording. Test input never feeds the real recorder.

Guided workouts keep the existing training runner as the clock and phase authority. A guided row can run in Gravity Well or standalone, and a game workout can resume standalone with its phase and session identity intact. Game encounters remain parked while standalone rowing continues. Prescriptions still use duration and perceived effort; there are no new watt targets or physiological thresholds.

Workout history shows power, pace and cadence charts, phase boundaries, per-block distance and observed power/pace, sampled peaks, coverage, estimated mechanical energy, detected drive/recovery timing, and provisional monitor calorie deltas. Missing readings appear as unavailable or chart gaps. Optional perceived-effort feedback is available in workout details. There is no new heart-rate integration.

Power averages are joules divided by valid power coverage. Pace averages use distance and time from accepted distance intervals, rather than averaging splits. Cadence averages use valid one-second observations. Motion time is an estimate based on recent distance updates. Energy is not metabolic calorie burn. Peaks are sampled observations. Missing calorie data is not reported as a measured zero.

## S4 connection and game input

After a successful connection handshake, OpenRow automatically requests normal watts mode once using DIWA. It still requires fresh mode flags before interpreting the shared register as watts. A later monitor display change is respected: invalid power falls back without repeatedly changing the monitor. USB polling, register interpretation and firmware gating remain in the S4 adapter; no calibration settings are written.

`RowingTelemetryFrame` is the immutable metric contract. Values have fixed documented units, reception timestamps, connection/availability checks and power status. Missing numerical observations use NaN. Diagnostics retain the raw register views. Workout distance remains canonical for recording; observed distance speed supplies the gameplay pace fallback.

`RowingEffortFrame` exposes bounded contribution, the active source and provisional status to games. Gravity Well uses the shared effort signal for reward contribution and visual response. Castlebreaker keeps distance-based charge.

Personal power references use the median of up to five qualifying real workouts within 28 days, overall reported effort 5–6, at least 60 observed work seconds and 70% power coverage of work motion. Two workouts are required. Guided references use work blocks; free rows use their observed session coverage. Synthetic sessions are excluded. References are frozen per active session and expire for future sessions as evidence ages. Existing pace references remain available when power evidence is insufficient.

Once enabled, fresh power is preferred after three distinct valid readings; otherwise input uses pace or the neutral provisional fallback. Transitions smooth over two seconds. Pause, missing motion and valid zero watts suppress eligible power contribution immediately. The existing contribution cap (1.25), guided effort limits, recovery exclusions, boss windows and authored encounter health are retained.

### Hardware validation gate

**Power-led gameplay is disabled by default.** The device-level `s4_validation` preference `normalWaterPowerValidated` is false unless explicitly set after hardware validation. Recording and display of guarded S4 estimates remain available, and gameplay uses pace. No idle reading or synthetic test automatically enables this gate.

Before enabling it, compare nonzero watts, speed/split and cadence against the physical S4 during easy, steady and harder normal-water rowing. Check response latency, mode changes, coasting, cable reconnect, and optional calorie scaling separately. Record firmware and observations. The existing firmware whitelist and freshness checks continue to apply after enabling. These checks require a person rowing; the automated and idle tablet tests do not establish measurement accuracy.

## Storage, accounting and compatibility

`WorkoutSessionController` owns one recorder per immutable profile context. `WorkoutSession` is the pure accounting model; `WorkoutStore` owns profile-local `workouts/` storage. A versioned state file points to the active session, atomic properties summaries contain totals and committed timeline byte counts, and append-only CSV files retain one-second observations and pause markers. Sampling stops while paused. Files are kept locally without automatic retention deletion.

CSV fields are elapsed seconds, block index, phase, state, watts, observed speed in m/s, cadence in spm, accumulated meters, accumulated strokes, and monotonic sample timestamp. Empty metric fields mean unavailable. Timelines are committed before summaries; uncommitted tails are truncated on retry. Backup recovery restores the last complete summary. History edits preserve the current active-session pointer. Charts use pixel min/max columns and contiguous sample traces; missing observations break the trace.

Athlete totals consume per-session monotonic receipts. A checkpoint is committed before its receipt; replay after interruption is safe. Resetting lifetime totals retains receipt watermarks, so old work is not reimported. Games no longer independently accumulate athlete power, distance or workout totals.

Cutover imports outstanding legacy Gravity Well counters once and persists a migration marker. An unfinished legacy row retains its identity and existing session totals, with credited offsets for previously imported work. Only new activity creates new athlete credit. A finished resumed row credits its completion once and retains the full session summary. Its chart explicitly identifies the missing historical timeline. Existing careers, training records, awards and synthetic saves are retained; historical measurements are never invented.

## Verification

- `test.ps1`: decoder, legacy suites, recorder lifecycle, power integration and gaps, sampling duplicates, zero watts, mode/freshness gating, source transitions, phase exclusions, references, idempotent accounting/reset, migration offsets, profile-safe storage and interrupted timeline append tests.
- `build.ps1`: signed Android app build.
- `build-telemetry-tests.ps1`: builds an isolated Android instrumentation APK. Run `adb shell am instrument -w org.openrow.probe.telemetrytests/org.openrow.probe.telemetrytests.TelemetryInstrumentation` only on a debug/test device. The fixture adds explicitly named temporary QA profiles and selects one; remove those fixtures and restore the prior profile registry afterward. It never exercises the owner's profile.
- Android integration checks cover controller restore, exact session identity, separate profiles, receipt replay, legacy migration, guided standalone phase/block routing, Home pause and explicit resume, and preservation of the original game surface after background callbacks.
- Native workout history and charts were inspected on the Android tablet using isolated generated observations. No normal-water power calibration was performed. The original owner files were checksum-checked before and after fixture testing.

The eight campaign simulation fixtures remain the balance regression suite. They exercise authored campaign progression at reference contribution; recorder and effort tests separately verify the telemetry-to-contribution boundary.

Release verification: all JVM regressions passed, including 60 new shared-recorder checks; 24 Android integration checks passed. All eight full campaign simulations completed successfully. Balanced routes remained 48.000 eligible hours (guided: 48.417) with 50 total prepared-boss minutes. The four original owner save files retained their exact SHA-256 values after QA cleanup. The final signed 0.19.0 build was installed, with first-owner setup and all existing records retained.
