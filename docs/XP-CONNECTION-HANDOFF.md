# XP connection handoff — 0.21.2

Starting Gravity Well before the USB handshake resumed the shared workout recorder while disconnected. The 250 ms recording safety gate (or disconnected telemetry) then paused the recorder. Once connected, the game accepted distance and strokes, but the recorder remained paused. Its effort contribution stayed zero, so campaign XP did not increase.

The initial connection transition now resumes a paused recorder when the foreground game is running, is requesting rowing, owns that workout and has not been explicitly paused. A subsequent disconnect still sets the game's paused state; reconnecting does not automatically resume it. Explicit Resume restarts both. Power validation and campaign reward rates are unchanged.

The native `WorkoutHandoffInstrumentation` reproduces the disconnected-start recording gate and handshake in an unregistered temporary profile. It failed against 0.21.1 at “S4 handshake must resume the recorder with the game.” Against 0.21.2 it passes: game and recorder both count 30 observed meters, campaign XP increases, disconnect stops input, and reconnect waits for explicit Resume. The fixture uses synthetic telemetry and never registers or switches family profiles. It removes its temporary data before reporting completion.

The signed build and full JVM regression suite pass. The corrected APK is installed on the connected tablet. This verifies the connection/accounting handoff with a native fixture; another physical row remains useful to confirm the complete USB path.

Owner game/training property values compare unchanged before and after testing. The game retains 2,024 cumulative meters and 318 cumulative strokes. The last completed game session contains 387 meters, 98 strokes and about 95.65 motion seconds, whereas its shared recorder contains no distance/strokes and only 0.052 elapsed seconds. Those are different scopes; cumulative totals cannot establish missing XP for a particular workout. No historical XP was estimated or backfilled, and no rowing statistics were fabricated.
