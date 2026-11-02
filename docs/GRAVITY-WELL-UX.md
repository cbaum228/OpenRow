> Upgrade-tree update: see [0.15.0 expanded catalog and progression rules](GRAVITY-WELL-EXPANDED-TREE.md). The 30-node/chapter-gate details below describe the previous release.

# Gravity Well menu and training UX — 0.14.0

The upgrade cards have been replaced by a single node-tree view. Seven branch tracks organize all 30 definitions into foundation and chapter columns, with the appearance node below. Select a node to inspect its effect, rank, prerequisites and availability in the detail panel. Purchase from that panel; touching a node alone never spends resources. Tap a branch label or Focus a branch to dim other tracks. Selecting another branch's node moves the focus with it. Existing prerequisite links remain visible, while chapter tracks group related tools without introducing new prerequisite rules.

No upgrade IDs, ranks, prices, material requirements, campaign progression or training policies change in this update. The current 30-node view is an overview for the test campaign; a future hundreds-node catalog will need branch/chapter drill-down rather than shrinking these touch targets.

Guided rowing now opens three sections: Train today, Progress and Preferences. Train today offers a direct duration picker, one primary workout action and a separate assessment browser. Progress displays the history and charts directly, including an empty state. Preferences contains goals, recommendation choices, explanatory detail, export and confirmed deletion. The experimental-template explanation remains available under About effort and recommendations.

Workout previews show readable phase names and durations, an ordered phase strip, effort guidance and an always-visible Start workout button outside the scroll area. Back returns to training. Readiness and returning-athlete choices retain their existing effects; a redundant returning-athlete dialog has been removed. Active workout controls retain resume, lower effort and shorten-work actions, with confirmation before ending early. The controller still owns timing and persistence independently of the game renderer.

Career and campaign labels use shorter actions such as Start rowing, Guided workout, Upgrade tree and Choose next expedition. Hardware Back returns from game menus to the career. Feedback asks about the completed row in plain language and makes optional feedback clear.

## Validation

All 7,485 existing automated checks pass. The APK builds and installs on the Android 9 tablet. Native visual checks cover the full tree, node detail selection, branch focus, guided-rowing tabs, assessment selection, readiness, workout preview and Back navigation. A purchase is exercised only in the separate synthetic career; no real workout or assessment is started for UI testing. Screenshots and data-preservation checks are saved under `build/ux-refinement/`.


## OpenRow training access — 0.15.2

The main OpenRow menu now includes a Training & Assessments card above Gravity Well. Assessments, guided workouts and training progress have direct buttons. Assessments have their own tab with durations, descriptions and a quick-baseline starting suggestion. Preview Back returns to the originating tab. Starting or resuming a workout opens the existing Gravity Well workout display; browsing does not enter the game. Existing training history, prescriptions and effort policies are unchanged.

Android build and tablet navigation verified: main menu to assessments, quick-baseline preview, Back to assessments, and direct progress access. No real workout started during verification.
