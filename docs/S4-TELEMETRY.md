# S4 USB telemetry — OpenRow 0.8.0

The decoder details below remain relevant. Shared recording, automatic watts selection, history charts, and the power gameplay validation gate are described in [the 0.19.0 integration](SHARED-WORKOUT-TELEMETRY.md), which supersedes the older game-specific recording limitations below.

The adapter reads the user's S4 firmware 2.10 (`IV40210`). Other firmware still
retains raw responses, but normalized new metrics remain unavailable until its
memory map is verified. Every accepted register sample has its raw hex payload,
integer value and monotonic reception timestamp. Original USB replies remain in
the app-private `s4-*.log` journal. No force curve or handle-force value is inferred.

## Channels

| Channel | Request/event | Interpretation |
| --- | --- | --- |
| Workout distance | IRD055 | Unsigned meters; canonical gameplay distance, independent of display countdown |
| Displayed distance | IRD057 | Raw unsigned value; retained for comparison |
| Stroke count | IRD140 | Unsigned count |
| Monitor cadence | IRS1A9 | SPM, separately from the existing event-derived cadence |
| Estimated power | IRD088 | Shared kcal/watts field: accepted as W only with fresh normal-screen watts flags |
| Intensity flags / screen | IRS041 / IRS00D | Watts bit 4, calorie bit 5, average bit 7; normal mode has upper nibble zero |
| Estimated calories | IRT08A | Full 24-bit counter, reference decoder uses cal; divide by 1000 for provisional kcal |
| Speed / instant average | IRD148 / IRD14A | 148 normalized as cm/s divided by 100; 14A retained raw pending validation |
| Stored speed / splits | IRD1A1 / IRD1A5 / IRD1A7 | Raw comparison channels; these use high-byte-first memory layout |
| Monitor clock | IRT1E1 | BCD hours/minutes/seconds, not a binary elapsed-duration counter; may count down |
| Optional heart rate | IRS1A0 | One byte, 30–240 bpm accepted; zero means unavailable |
| Tank setting | IRS0A9 | Tenths of liters; configuration, not a measured fill level |
| Pulse calibration | IRS083 / IRS084 | Raw edges / calibrated centimeters |
| Average stroke / pull timing | IRS142 / IRS143 | Raw timing values, retained for comparison |
| Misc flags | IRS049 | Raw low battery/work/rest flags |
| Stroke start/end | SS / SE | Event cadence plus detected drive/recovery intervals |
| Rotation pulses | Pxx | Hex number of sensor edges in the previous 25 ms; PING is separate |

The live screen shows normalized values plus a scrolling raw list with sample age.
Power expires after 1.5 seconds; slow normalized channels after 10 seconds; pulse
rate after 0.5 seconds. Connection loss clears live availability and event timing.
Unreported values stay missing rather than displaying fabricated zeros.

Polling is bounded to one command per 50+ ms (protocol recommends at least 25 ms).
Distance, power and mode checks have priority; other channels rotate. USB reads use
25 ms timeouts and do not block drawing. Pulse packets avoid native panel/logcat
updates; the display refreshes at 250 ms. Raw replies still enter the local journal.
The handshake requests version once. Disconnect sends EXIT; no monitor reset,
EEPROM writes or workout programming is performed.

**Enable S4 watts** sends DIWA to select the normal watts display. This explicitly
changes the monitor's display units; it does not change tank/calibration settings.
Conservative mode gating avoids treating the shared register as watts in a calorie
or average display. A user changing display mode interrupts energy integration.

## Athlete accounting

During eligible real Gravity Well play, RowingStats records:

- Estimated mechanical joules: trapezoidal integration of consecutive valid watts,
  with positive intervals no longer than 1.5 seconds. Separate coverage seconds
  support an average over observed power only. Coverage is not workout duration.
- Provisional estimated calorie counter deltas, with a first-reading baseline,
  reset/drop handling, bounded freshness and implausible-jump rejection.
- Sensor pulse totals and complete detected drive duration/count.
- Last-workout sampled peak power, peak received HR and sampled peak speed.

Shared stats show kJ, average observed watts, coverage, calorie estimates, pulses
and average detected drive duration. Last-workout details show its sampled power
peak and HR when received. Peaks are sampled values, not guaranteed true maxima.
Power is never summed as a lifetime quantity; its time integral is energy.

Games continue using their existing distance controls while power/scaling is being
validated. This does not claim a calibrated power algorithm or adaptive training.
Session stats are persisted alongside the existing athlete source counters, then
imported into the separate shared athlete file with high-water marks. Test input,
menus, explicit pause, disconnect and background time do not accumulate. Process
restart/resume starts new integration baselines; it cannot invent offline work.
Reset clears the new totals too and preserves import watermarks. Old workouts get
no invented power or calories. Game upgrades do not multiply athlete observations.

This bridge currently records during real Gravity Well sessions. Live stats is an
inspection screen, not a standalone workout recorder. A fully independent workout
runner remains the next architecture step; Castlebreaker history is not imported.

## Validation

Unit coverage includes hex widths, 24-bit calories, BCD clock, optional HR, stale
signals, mode/firmware guards, pulse/PING separation, event timing, canonical
distance, joule integration, gaps, duplicate times, pause/restart, calorie reset,
test exclusion and shared-history reset/replay. Existing game suites also pass.

Normal-water rowing remains necessary to compare nonzero power, speed, calorie
scaling and signal latency against the physical S4. Do not build calorie/energy
badges or fitness adaptation on these provisional channels yet. Detailed pulse
physics and force/technique analysis require additional validation.

## References

- [WaterRower S4/S5 USB protocol issue 1.04, manufacturer-authored document](https://github.com/jamesnesfield/node-waterrower/blob/develop/docs/Water%20Rower%20S4%20S5%20USB%20Protocol%20Iss%201%2004.pdf): packet formats, memory map and flags. Map is labeled provisional v2.00.
- [waterrower-core reference decoders](https://github.com/tbressler/waterrower-core/tree/master/src/main/java/de/tbressler/waterrower/subscriptions/values): 24-bit calorie counter, BCD clock, tenths-liter tank and single-byte HR. Its total-velocity interpretation has a TODO, reinforcing the need for hardware comparison.
- [Oarsman S4 adapter](https://github.com/olympum/oarsman/blob/master/s4/s4.go): event and pulse decoding, firmware 2.10 support. Its two-byte HR request is not used; the memory map/reference decoder identify a single byte.

Reference source code was inspected for interoperability; implementations were not
copied into the app. No new runtime dependency was added.

### Tablet check — October 28, 2026

Installed 0.8.0 on the Ergatta Android 9 tablet. S4 CDC handshake succeeded and
returned IV40210. All requested channels responded during idle polling. Observed
IRD055=0x0496 (1,174 monitor meters), IRD140=0x009A (154 monitor strokes),
IRT08A=0x01319D (78,237 raw calorie units), and IRS0A9=0xB4 (18.0 L configured).
DIWA returned OK and intensity flags changed from 0x01 to 0x10; live estimated
power then displayed 0 W at rest. The monitor's existing totals were not imported
as a new workout. Shared athlete totals stayed at 1,079 m, 143 accepted strokes,
6 finished workouts and 242.9167 motion seconds. Existing awards and last workout
were preserved. No reset was performed. Live/detail layouts were inspected.
AndroidRuntime had no error in this check. The device crash buffer also contains
an unrelated older system /system/xbin/test crash, not an OpenRow crash.

651 automated checks pass, including 42 new S4/rowing-stat checks. Nonzero rowing
and per-channel latency have not been verified in this installation; do not infer
that idle register reads establish measurement accuracy.
