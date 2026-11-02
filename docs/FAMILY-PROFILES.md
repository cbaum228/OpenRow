# First-run setup and family profiles · 0.18.0

OpenRow asks for a local profile name, training goal (endurance or general cardio), and workout-choice preference (one coach suggestion or recommendation with alternatives). Setup is required once per profile. Assessments remain optional. Equipment connection stays available from Live Rowing Stats; no personal measurements or mandatory fitness test are collected.

Home shows the current profile beside the Profiles button. The picker switches profiles, adds family members, edits the current name and coaching settings, and offers the home-screen shortcut. Adding a profile switches to it. Switching saves and pauses unfinished workouts; returning offers their saved session. Profile names can be changed without moving saves. Names may repeat, because identity uses stable generated IDs.

Each profile owns Gravity Well real and synthetic careers, training settings, assessments, workout history, personalized gameplay reference, athlete totals and awards, and Castlebreaker campaign checkpoints. S4 connection and hardware diagnostics belong to the device. Profiles are local identities, without passwords or cloud sync; the picker does not provide privacy between tablet users.

## Existing installations and persistence

The first profile has the reserved ID `legacy` and uses the original files and Castlebreaker preference namespace in place. There is no destructive migration or copying of progress. Existing training preferences are preselected in setup. Completing setup attaches a display name to those records; it does not reset them.

New profiles use `files/profiles/<UUID>/` and a distinct SharedPreferences namespace. `family-profiles-v1.properties` stores the profile list, selected ID, names, and setup completion. Registry writes use AtomicFile. Invalid registries fail closed and preserve the originals. UUID validation and fixed filenames prevent display names becoming paths.

Every screen receives an immutable ProfileContext. On a switch, the old screen checkpoints before the registry changes; Activity recreation builds fresh game and training controllers. Old lifecycle callbacks retain the old storage scope. A career save failure prevents switching. Editing preferences updates the live training controller before recreation so a subsequent pause cannot restore stale settings. Training exports unwrap the owning Activity while retaining the profile-scoped source data.

Profile deletion and cloud accounts are outside this release. Future persistent game modes should receive the same profile context and store progress through it.

## Verification

`test.ps1` includes 24 profile registry checks covering setup state, selection persistence, duplicate names, rename stability, snapshot isolation, invalid names, unknown IDs, invalid registry versions, and traversal rejection. Existing game, training, accounting, and campaign regressions pass. `build.ps1` produces the signed Android APK.

On the connected Android tablet, isolated temporary profiles verified setup completion, adding a family member, switching both ways, different coaching preferences, empty independent real careers, and restoration of a paused synthetic row. Screens were visually inspected and AndroidRuntime showed no errors. SHA-256 checks confirmed the original real career, synthetic career, athlete history, and training files were unchanged. Test profiles and their registry fixture were removed afterward, leaving first-owner setup ready. No real workout was performed during verification.
