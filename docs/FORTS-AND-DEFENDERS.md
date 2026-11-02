# Four forts and defenders — version 0.4.0

October 23, 2026. The existing Watchpost is retained, with three new selectable layouts.

| Fort | Defenders | Layout |
| --- | --- | --- |
| The Watchpost | 3 archers | Three ordinary stone towers; introductory encounter |
| Iron Gate | 2 archers, 1 knight | Tall reinforced central keep, regular flanking towers |
| Split Rampart | 2 archers, 2 knights | Four towers; lower archer posts outside, reinforced knight posts inside |
| The Citadel | 3 knights | Reinforced flanks and tall central keep, armored connecting walls |

## Charging and damage

Distance charging remains 10 m to unlock a standard shot, 20 m for full charge.
Impact damage now scales with charge squared. Ordinary blocks have 0.65 health;
reinforced blocks have 2.8 health. Impact distance attenuates damage. Blocks retain
damage between shots, with cracks drawn on surviving masonry. Only broken blocks
are thrown by the impact; unsupported masonry then falls through the existing support
simulation. This prevents low-charge blast impulses from bypassing armor.

The centered Iron Gate test needs four 10 m standard shots or one full 20 m siege shot
to collapse the knight's keep. This is prototype game balance, not measured physical work.
The flatter trajectory and distance-baseline safeguards remain intact.

## Defenders and controls

Beacon cubes are replaced by native vector characters: green hooded archers with bows
and quivers, and armored knights with helmets, shields and swords. Their support
colliders stand on the tower caps. Falling/displaced defenders count toward victory;
dropped equipment marks defeated positions. Defenders do not attack the player in this version.
Knights' reinforced towers are blue-gray with iron bands and rivets; ordinary stone is tan.

Top arrows select any fort. Reset rebuilds the current fort; N selects the next.
After victory, Next fort advances to the next layout. Selection wraps after fort four.
Navigation resets that encounter's charge, distance and shot counts and establishes a new
S4 distance baseline. No campaign unlocks or persistent saves are introduced yet.

## Wheels

Removed the skewed oval wheel construction. Each wheel is a cached cylindrical mesh
with an X-axis axle and circular YZ faces, projected through the scene camera. Tread,
rim, spokes and hubs share this geometry; front/rear wheel planes are parallel.
The cached geometry avoids per-frame wheel-mesh construction.

## Verification

- 412 game assertions and 27 rowing-metric assertions pass.
- Tests cover stable structures and correct defender counts for all four layouts,
  completion of every fort with aimed siege shots, distinct layouts, reset and navigation,
  persistent armor damage, light/heavy effectiveness, existing trajectory and distance cases.
- APK compilation and signature verification pass; installed on the Android 9 Ergatta.
- Watchpost and Iron Gate inspected on the tablet. A 10 m test shot left Iron Gate's
  central knight standing with visible cracks; after resetting, a full 20 m test shot
  collapsed that keep and dislodged its knight. Inputs explicitly used TEST mode.
- Evidence: `build/castle-watchpost-defenders.png`, `build/castle-iron-gate.png`,
  `build/castle-armor-light.png`, `build/castle-armor-heavy.png` (local, ignored).
- Split Rampart and the Citadel were also inspected on the tablet; four-defender
  objective counts and reinforced tower colors were correct. The fort selector was
  moved upward slightly to clear the tallest knight's helmet. The final APK was
  installed and left on Iron Gate in real S4 rowing mode with a fresh encounter.
  Further evidence: `build/castle-split-rampart.png`, `build/castle-citadel.png`,
  and `build/castle-forts-device-test.log`.
- Final S4 connection required one manual retry after the previously observed
  `CDC control lines failed: -1` error. Retry completed the handshake and read
  the current distance successfully. Automatic recovery remains future work.
