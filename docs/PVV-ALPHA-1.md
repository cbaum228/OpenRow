# Product verification and validation — alpha 1

**Candidate:** OpenRow 0.22.0-alpha.1, Android versionCode 39, package `org.openrow.probe`. **Audit date:** November 1, 2026 (America/Chicago). **Disposition:** conditional GO for one invited alpha tester on another compatible S4/Ergatta tablet. This is not general-release or fitness-prescription validation.

The candidate contains the 0.21.2 XP connection fix and 0.21.1 compact-silhouette correction. Alpha preparation changes packaging/signing, documentation, build portability and version reporting. Training policy, reward rates, save schema and physical input calibration are unchanged. The user confirmed physical XP accrual after 0.21.2.

## Verification matrix

| Area / requirement | Evidence | Result |
|---|---|---|
| Family identities and save isolation | JVM profile tests; Android independent profile/controller fixtures | PASS |
| S4 parsing, availability, stale/invalid readings, modes and stroke detection | S4 channel and rowing tests; recorded five-stroke fixture | PASS; hardware unit agreement remains open |
| Training phases, paused/missing telemetry, adaptation and return after inactivity | Training core, visual and game/campaign integration suites | PASS for authored policy; not physiological validation |
| Gravity Well campaign, bosses, material conservation and upgrades | Game/catalog/balance suites and eight complete campaign simulations | PASS |
| Castlebreaker charge/campaign behavior | Game and campaign JVM suites | PASS |
| Recorder lifecycle, receipts, migration, chart sample data and interrupted persistence | 60 recorder checks plus 24 Android controller/profile/migration/guided checks | PASS |
| Starting a game before S4 connection must earn XP after handshake | Native isolated handoff fixture, including actual signed non-debug release | PASS; subsequent disconnect still requires explicit Resume |
| Motion/flow bounds, frame statistics and visual isolation from economy | 47,367 graphics checks; prior native visual inspection | PASS; visual scores/thermal limits remain as documented |
| Existing owner records preserved | Four core save files had identical SHA-256 before/after native QA; original family registry restored and QA IDs removed | PASS |
| Signed, installable, non-debug alpha | v3 signature verified for API 28; stable original certificate; `adb install -r` succeeds and normal MainActivity launches | PASS on original Android 9 tablet |
| Release surface/privacy | Compiled manifest: minSdk 28, one exported launcher, preview declaration absent, `debuggable=false`, backup disabled, no requested permissions (including INTERNET) | PASS |
| Debug access removed | `run-as` rejected as non-debuggable; starting the removed preview activity returns Activity-not-found | PASS |
| New APK over an existing install | Original signing identity preserved, versionCode increased; in-place install accepted | PASS on original tablet; tester update-retention check still pending |
| USB-stick and browser package-installer paths on second tablet | Installation guide and exact same APK prepared for both paths | NOT YET PHYSICALLY VALIDATED |

`test.ps1` completed **55,894 assertions** across its suites (counts are assertions, not independent end-to-end cases). `build.ps1`, `build.ps1 -Release`, and the native telemetry/handoff fixtures passed. The final distributed binary is verified again by `verify-alpha.ps1`. CI repeats JVM/campaign/build checks before the prerelease is published; local assertions alone do not assert a successful remote CI run.

## Campaign simulation results

| Scenario | Eligible route hours | Total boss minutes |
|---|---:|---:|
| Balanced standard / easy / high | 48.000 each | 50 each |
| Focused standard | 48.000 | 50 |
| Balanced guided | 48.417 | 50 |
| Balanced alternate seeds 2 / 3 | 48.000 each | 50 each |
| No upgrades | 85.333 | 2,290 |

All eight scenarios completed without assertion failure. These model reference gameplay contribution and authored loadouts, not eight humans or measured training effectiveness. Prepared bosses total ten five-minute encounters; the unprepared route is intentionally much slower. Local synthetic evidence is in ignored `build/progression-audit/`; it contains no owner workouts.

## Validation boundaries and outstanding work

| Limit / remaining check | Alpha treatment |
|---|---|
| A second rower/tablet has not yet been exercised | Exactly one invited tester; use the install and session checklist before broader distribution |
| Power/calorie units and other firmware need normal-water comparison | Provisional display; personal power gameplay stays behind the existing disabled validation gate; observed pace fallback remains |
| Approximate 30 FPS is an average, not a locked frame budget | Prior unchanged graphics stress window: 29.986 FPS, p95 50.202 ms, p99 66.564 ms on original Android 9 tablet; this packaging audit did not repeat the graphics benchmark. Longer-session thermal behavior is open |
| Goals are currently endurance/general cardio; richer goals and physiological VO2/fat-loss prescriptions are not implemented | Do not advertise those as alpha capabilities |
| Complete install/update/launcher behavior on other Ergatta firmware is unknown | USB-stick preferred when no browser; computer fallback provided; no factory reset, launcher replacement or Ergatta data deletion |
| No cloud backup/general export-and-restore feature | Keep data local; preserve installs; never recommend uninstalling to update |
| Long-term training benefit and sustained visual appeal are unvalidated | Collect usability/bug feedback, not clinical or fitness-effectiveness claims |

There are no observed unresolved blockers to the scoped alpha in the checks above. A crash, data loss, duplicated workout, cross-profile data leak, recurring missing XP or inability to return to Ergatta is a stop-and-report condition. Do not expand the test group until installation, physical units, saved sessions, an update and a normal-length row succeed on the second device.

## Release and evidence handling

The APK SHA-256 is in the release's `SHA256SUMS.txt`; its certificate fingerprint is pinned in `release-policy.json`. The install kit contains only the APK, instructions, this report, privacy note, checklist and optional Windows installer. No owner backups/logs, private signing material or local attachments are published. Native evidence and a private owner backup remain in ignored `build/` locally. Crash-buffer review contained an older October 31 development-fixture error; it was not a new alpha-runtime crash.

The release is a private GitHub prerelease. The tester can be invited separately as a collaborator, or the owner can hand them the downloaded USB kit. No invitation, message or account-sharing action is part of this publication.
