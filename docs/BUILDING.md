# Build and release

Windows PowerShell 7, JDK 17, Android platform 35 and Build Tools 35.0.0 are required. Existing local installations under `tools/jdk`, `tools/platform/android-35` and `tools/build-tools/android-15` are also supported; that last folder's historical name is not a platform requirement. Otherwise set `JAVA_HOME` and `ANDROID_HOME` to SDK/JDK installations. The GitHub Actions Windows job uses the standard environment and installs platform/build-tools 35. Tool binaries are not committed.

Run `./test.ps1`, `./simulate-progression.ps1`, then `./build.ps1`. The last command builds a local **debug** APK in `build/OpenRow-Probe.apk`; it is not the distributed artifact. Run the isolated native instrumentation fixtures on a connected developer tablet as documented in [shared telemetry](SHARED-WORKOUT-TELEMETRY.md). The broad telemetry fixture temporarily selects QA profiles: back up and restore the registry, remove only the returned QA IDs, and verify owner hashes. Never run it unsupervised on a tester's device.

## Android Studio / Gradle (debug only, any OS)

The Gradle files (`settings.gradle`, `build.gradle`, `app/build.gradle`) let Android Studio build and run debug APKs on Linux, macOS or Windows. They use the existing `app/src`, `app/res` and `app/AndroidManifest.xml`; package, version and SDK levels are read from the manifest, so it remains the single source of truth. Open the repository root in Android Studio, let it sync, then use **Run** or `./gradlew assembleDebug` (APK in `app/build/outputs/apk/debug/`). If `build/debug.keystore` exists, Gradle signs with it, so the result installs over an existing `OpenRow-Probe.apk`. Gradle's release variant is disabled on purpose: release APKs come only from `./build.ps1 -Release` and `./package-alpha.ps1`. The JVM tests in `tests/` are still run by `./test.ps1`.

## Private signing identity

The release APK is non-debuggable. For the first alpha, `prepare-alpha-signing.ps1` imports the original owner's locally generated private certificate into a password-protected PKCS12 store so existing installs can update without losing data. Its certificate subject retains the historical “OpenRow Debug” name; Android's debug flag is a separate setting and is false in the release. A subject name is not a private key or an Android debug permission. The public certificate fingerprint is pinned in `release-policy.json`.

**Securely back up `.local-signing/` before distributing updates.** It contains the private key and its password file; both are ignored by Git and must never be included in a release, issue, or tester kit. Do not delete that folder during build cleanup. If no prior local identity exists, the preparation script generates a new one; its fingerprint must be intentionally pinned for a new distribution, and it cannot replace this alpha's identity while retaining update compatibility. Never regenerate the key between updates. The historical debug key also remains private in ignored `build/`.

Run `./prepare-alpha-signing.ps1` only once, then `./build.ps1 -Release`. Release packaging removes the preview activity declaration and sets `debuggable=false`. `./package-alpha.ps1` builds, checks the pinned signature and manifest, copies the signed APK into `distribution/`, and creates the local install kit. `./verify-alpha.ps1` verifies the distributed binary without needing a signing key.

## Publish

The repository stores one small signed alpha binary in `distribution/` so GitHub Actions can publish the **exact locally verified APK** without receiving a signing secret. Normal build outputs and private files stay ignored. This bootstrap choice is deliberate for the first alpha; subsequent versions should keep assets in Releases and update the workflow/version policy together.

The workflow runs JVM tests, all eight campaign simulations, builds a debug APK from the committed source, and checks the prebuilt release's signature, permissions, version and hash. Only after these pass does it create the private `v0.22.0-alpha.1` prerelease with APK, ZIP kit, checksum list and PV&V report. It never creates an Android signing key or resigns the tester artifact in CI. A GitHub prerelease is not returned by `/releases/latest`.

Source review and the local PV&V report tie this first binary to the release candidate. CI cannot independently reproduce a private-key signature; it verifies the pinned signature and exact artifact, while compiling/testing the source separately. Do not claim a reproducible signed build from that alone.

For a subsequent release, increase versionCode/versionName, update the policy/filenames/workflow, rerun checks, and build with the same private signing identity. A lost signing key or signature mismatch must not be worked around by asking a tester to uninstall and lose their progress.
