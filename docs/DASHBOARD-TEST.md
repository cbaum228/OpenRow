# Dashboard 0.2.0 — October 22, 2026

Implemented live distance and stroke-count decoding, a large landscape dashboard,
calculated cadence from S4 `SS` events, the latest interval cadence, and an expandable
diagnostics panel. Distance/count are current monitor totals, not a new app workout.

Cadence uses a monotonic clock and the most recent three start-to-start intervals.
Two start events are required. Duplicates within 500 ms are ignored. Cadence becomes
unavailable after 2.5 average intervals (bounded to 6–15 seconds); the first interval
allows 15 seconds. Resume starts a new average. Disconnect and monitor stroke-count
reset clear cadence history. S4 cadence is logged independently.

Validation:

- `test.ps1`: 27 assertions pass, covering timestamps, duplicate events, smoothing,
  idle boundaries, slow cadence, resume, monitor resets, malformed replies and hex decoding.
- Recorded S4 event replay produces 28.0 spm, 15 m and 5 strokes. The tracked fixture
  is a compact selection of actual event timestamps and final replies from the first rowing test.
- APK compiled, signature verified, and installed on the actual Android 9 tablet.
- Dashboard and expanded diagnostics inspected using actual tablet screenshots.
- Final installed build connected, completed handshake and displayed 15 m / 5 strokes.
- One connection attempt reported `CDC control lines failed: -1`; a manual retry
  succeeded. The cause is unconfirmed; automatic connection recovery remains future work.
- No live new strokes were simulated or injected into the tablet. Live cadence display
  still needs user rowing validation; the calculation has been verified against recorded events.

The final build is left open, connected, with diagnostics collapsed.
Screenshots are local in `build/dashboard-final.png` and `build/dashboard-diagnostics.png`.
