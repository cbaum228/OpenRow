# Alpha data and privacy

OpenRow has no INTERNET permission and no analytics SDK or automatic upload path. It stores local profile names, workouts, training feedback, progress and raw S4 diagnostic logs in the app's private storage. Profiles separate family records inside the app; they are not encrypted accounts or a protection boundary between people with access to the unlocked tablet.

No account or subscription is required to row after installation. A private GitHub download requires repository access, but those credentials are not entered into OpenRow. The app does not upload records to GitHub. Reports and screenshots are sent only if the tester chooses to share them.

The alpha has no cloud synchronization or general-purpose backup/restore screen. Uninstalling or clearing app storage erases local data. Android automatic backup is disabled. Training export is available from Training, but it is not a complete career/workout backup. Updates using the same package and signing identity preserve data under Android's normal update mechanism; retention still needs confirmation on the tester's tablet.

The owner should ask for the minimum evidence needed to reproduce a bug, keep raw diagnostic/workout data private, and remove personal names from screenshots. Raw owner logs, saved workouts, signing keys, passwords and local machine attachments are excluded from the repository and install kit.
