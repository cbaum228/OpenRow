# Alpha 1 tester checklist

Record tablet model, Android version, S4 firmware (OpenRow diagnostics), app version, connection type and the issue's approximate time. Report through the private GitHub repository or directly to the owner. Include a screenshot and steps when useful; remove personal profile names from screenshots. Do not post raw workout history publicly.

## First session — about 10–15 minutes of software checks

- Install the signed APK. Create a profile and find the app again from the launcher.
- Connect the S4; decline its USB permission once, then retry and grant it.
- Start Gravity Well **before** connecting the S4, connect, then row: meters, strokes and XP must increase.
- Pause for 30 seconds: no new XP or workout distance should accrue. Resume and check they increase again.
- End the row. Confirm a nonempty Workout History entry, plausible distance/strokes, and matching profile ownership.
- Close/reopen OpenRow: profile, XP, upgrades and completed workout remain.
- Launch Ergatta again and confirm it can reconnect to the rower.

## Second session — 15–20 minutes, at a comfortable self-selected pace

- Compare OpenRow's distance/strokes and displayed watts with the physical S4. Note missing/mismatched channels; do not change calibration to make them agree.
- Disconnect the S4 while paused, reconnect, and explicitly resume. It must not add the disconnected distance, old strokes or XP.
- Leave to Home, reopen, and resume the same saved workout once. The row must not be duplicated.
- Try standalone rowing and view its history/chart gaps after pauses. A missing power value should appear unavailable rather than as measured zero.
- Create a second family profile, perform a short row, then switch back. Careers, workouts and totals must be independent.
- If trying guided training, verify warm-up/recovery/cool-down instructions and pause/resume. Campaign rewards are for eligible WORK intervals; recovery should not require harder rowing to keep a game moving.
- Install a subsequent alpha update over the current build when the owner provides one; confirm both profiles survive. Do not uninstall to update.

## Longer session — normal use, up to 30–45 minutes if desired

Check sustained responsiveness, heat, animation pacing, rowing detection during slow recovery, and progress after ending/reopening the app. Report freezing, crashes, missing XP, unexpected pause, duplicate/lost workouts, wrong profile data, or inability to return to Ergatta immediately. This is a software coverage target, not a prescribed workout duration; stop or split it across sessions as needed.

## Feedback template

```text
App / tablet / Android / S4 firmware:
Install method:
What I did:
Expected / actual:
Distance & strokes on S4 / OpenRow:
Did XP increase while rowing? Did it stop while paused?
Did saved progress survive reopening?
Could Ergatta reconnect afterward?
Approximate time and screenshot (optional):
```

The alpha is limited to this invited tester. A second-device installation, USB-stick path, physical units, long-session stability and upgrade retention must be confirmed before expanding distribution.
