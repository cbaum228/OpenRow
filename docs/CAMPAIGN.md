# Castlebreaker campaign setup — 0.5.0

October 23, 2026.

## Session design

The campaign runs Watchpost → Iron Gate → Split Rampart → Citadel, with a rest screen
after the first three victories and a summary after the last. Navigation cannot skip
campaign forts. Free play retains its independent encounter and original charge scale.

Setup supports 500/1,000/2,000 m plans or 15/30/45 min estimated rowing plans, a planning
pace of 1:30–5:00 per 500 m, and manual/30 s/60 s rests. Duration estimates are explicitly
estimates of rowing time: the campaign ends on four victories, not at a timer/distance cutoff.
Aiming, missed shots, extra charge and rests can increase the actual session.

The efficient baseline route costs 190 original game meters (ordinary defenders at
standard charge, knights at full charge). The plan scales charging thresholds against
that baseline, rounding standard-shot thresholds up to whole meters. A 1,000 m plan
uses 53/106 m shots; an efficient tested route consumes 1,007 m. Trajectory, armor rules
and aim speed remain unchanged. Distance while the winch is full still counts as active
rowing, even though surplus charge is not banked.

## Flow and storage

- `CampaignSession.java`: pure plan, phase, counters, rest and checkpoint model.
- `CampaignSetupView.java`: native planning, rest and summary controls.
- `CastlebreakerView`: separate free-play and campaign encounters, scaled distance,
  input-mode lock, live progress, victory-driven transitions and checkpoint saves.
- `MainActivity`: screen routing, lifecycle pause and existing S4 connection.

Local SharedPreferences `openrow_campaign` stores a versioned checkpoint at campaign
start, after a victory, and before the next fort. Rest-state updates are saved when
leaving that screen/backgrounding. Completed results remain available until a new
campaign replaces them. No cloud data, accounts, unlocks or upgrade economy are added.
Mid-fort physics is not serialized: process restart returns to the start-of-fort
checkpoint, excluding unfinished-fort totals. Setup states this limitation explicitly.

Actual and simulated runs are distinguished in saved data and on screen. Test mode
adds a quarter-winch of simulated distance per button/W press. These inputs never
fabricate monitor metrics. Campaign input mode cannot change midway through a run.

Active play time includes aiming but excludes explicit rests, background time, hidden
game views and disconnected real-input play. Rest countdown never auto-launches a fort;
the user must continue or explicitly skip the remaining rest. No old S4 distance is
credited on start, resume, mode transitions, or between forts.

## Verification

- 27 metric assertions, 412 game assertions, and 54 campaign assertions pass.
- Campaign checks cover pace-derived budgets, whole-meter charge thresholds, active/rest
  accounting, ordered progression, countdown/skip behavior, malformed checkpoints,
  serialized state and simulated markers, and a complete four-fort 1,007 m route.
- APK builds and verifies; installed on the actual Android 9 tablet.
- Setup and time-estimate screens inspected. Changing pace from 2:30 to 2:45 updated
  a 15 min plan to 2,730 m and 144/288 m standard/siege thresholds.
- Device campaign tests use explicitly labeled TEST mode; they do not claim physical
  rowing validation of a full campaign.

Local evidence: `build/campaign-setup-ready.png`, `build/campaign-time-plan.png`.

Further device checks:

- A 500 m TEST campaign captured the Watchpost in three standard shots and 81 simulated
  meters, entering the timed rest screen. The checkpoint correctly stored phase REST,
  one completed fort, three shots, 81 m and the explicit simulated marker.
- Force-stopping and reopening the app restored the checkpoint. Resume returned to
  the rest screen; Skip rest advanced to Iron Gate with scaled charging intact.
- Iron Gate finished at six cumulative shots / 189 m; Split Rampart finished at ten
  cumulative shots / 351 m. The four-defender stage transitioned to its rest screen.
- Setup preferences are remembered separately from the active campaign plan. Editing
  them affects a new campaign; Resume retains the saved plan and input mode.
- Rest countdown disables and dims Continue until ready; remaining rest can be skipped.

Evidence: `build/campaign-rest.png`, `build/campaign-resume.png`,
`build/campaign-stage-two.png`, `build/campaign-stage-three.png`.

The full device TEST campaign completed all four forts with 513 simulated meters and
13 shots for its 500 m plan. The summary correctly retained the TEST label and saved
the complete result. Final APK refinements remember setup choices independently and
make the timed Continue button visibly disabled. The final version was installed,
with setup left on 1,000 m, 2:30 planning pace, manual rests and real S4 input; no real
campaign was started on the user's behalf. Completed simulated test results remain
available under View last results.

Additional evidence: `build/campaign-complete.png`, `build/campaign-device-test.log`.
