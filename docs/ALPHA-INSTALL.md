# OpenRow alpha 1 — start here

This build is for one invited tester with an Ergatta tablet running **Android 9 or newer** and a **WaterRower S4 USB monitor**. Other sensor connections are not supported. The tested monitor identifies as `IV40210`; other firmware may have unavailable power/extra metrics. OpenRow is independent of Ergatta. Ergatta [does not support third-party app installation](https://ergatta.com/blogs/faq-workout-experience-ergatta-workout-experience/third-party-applications-streaming).

## Choose an installation method

**Use a USB stick for the first install if the tablet has no browser.** Download the release on a computer and copy the APK to a FAT32 USB stick. The tested tablet has Android Files/DocumentsUI but no detected browser. A USB adapter may be needed on a different tablet. A browser download is easier for later updates if a browser is already available. Both methods install exactly the same signed APK.

Get **OpenRow-0.22.0-alpha.1.apk** or **OpenRow-0.22.0-alpha.1-install-kit.zip** from the [alpha 1 release](https://github.com/cbaum228/OpenRow/releases/tag/v0.22.0-alpha.1). The repository is private: the tester needs their own GitHub account invited as a collaborator, or the owner can download the bundle and hand over the USB stick. Never give the tester your GitHub password or a token. This is a prerelease; GitHub's `/releases/latest` link is not the correct link.

## Open Android settings

On the original tested tablet, tapping the upper-left corner of Ergatta's profile selection screen 7–10 times opens its Android access menu. Keep the S4 connected while opening that menu. This was observed on the owner's tablet; firmware and tablet variants may differ. If it does not work, stop and tell the owner the tablet model/Android version rather than trying a reset or firmware change.

In Android Settings → Apps & notifications → Ergatta → Force stop, stop Ergatta for the OpenRow session. This avoids competing access to the S4 USB device. Do not uninstall Ergatta, clear its storage, change its launcher defaults, or factory-reset the tablet.

## USB stick

1. On a computer, download the APK from the release. Compare its SHA-256 with `SHA256SUMS.txt` from that same release. On Windows: `Get-FileHash .\OpenRow-0.22.0-alpha.1.apk -Algorithm SHA256`. On macOS: `shasum -a 256 OpenRow-0.22.0-alpha.1.apk`.
2. Copy the **APK itself** to the USB stick. Android cannot install the ZIP; extract it on the computer first if you downloaded the kit.
3. After opening Android settings, connect the stick to a compatible USB host port and open Files/Documents → the USB drive → the APK. If the only host port is occupied by the S4, temporarily swap the S4 for the stick after entering Android settings; reconnect the S4 before rowing.
4. If Android asks, enable **Allow from this source** for Files/the file manager, return to the installer, and tap **Install**. Turn that permission off afterward. If the tablet blocks installation or cannot read the stick, report it; use the computer method below if USB debugging is already available.
5. Open **OpenRow** from the app drawer. Profiles → Home-screen icon can request a shortcut where the launcher supports it. The app does not replace Ergatta's launcher.

## Browser download

Open the [alpha 1 release](https://github.com/cbaum228/OpenRow/releases/tag/v0.22.0-alpha.1) in an existing tablet browser. Sign into the tester's invited GitHub account if needed. Under Assets, download the APK, open the downloaded file, allow installation from that browser when prompted, and install. Disable the browser's unknown-app installation permission afterward. If there is no working browser or the private GitHub login is awkward, use USB instead. Do not install random browser APKs to complete this test.

## Computer fallback

Download Google's [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools) on a computer. USB debugging requires Developer options and authorizing that computer on the tablet; these steps are unnecessary for browser/USB-stick installation. The tested tablet has a separate small computer USB port, so the S4 can remain connected to its host port. With one authorized tablet connected:

```powershell
adb devices
adb install -r OpenRow-0.22.0-alpha.1.apk
adb shell am start -n org.openrow.probe/.MainActivity
```

The kit also includes `Install-Windows.ps1`, which checks the APK hash, Android version and connected-device selection before installing. Run it from PowerShell 7: `pwsh -File .\Install-Windows.ps1 -AdbPath C:\platform-tools\adb.exe`. Google documents the [ADB installation flow](https://developer.android.com/tools/adb#move). Do not use `adb uninstall` to perform an update.

## First row

Create a local profile, choose a training goal, and open Gravity Well. Power on the S4, reconnect its USB cable if needed, tap **Connect**, and approve Android's USB permission for OpenRow. The app requests normal watts mode once; it does not change tank calibration. Check **S4 connected**, then start/resume a real row. Row briefly and confirm distance, strokes and XP increase. Use End session, then check Workout History. Follow the [alpha checklist](ALPHA-TEST-CHECKLIST.md) over subsequent sessions.

Power/calorie units are provisional on the second rower until compared with its monitor. Gameplay uses observed pace fallback; personal power-driven gameplay remains disabled. Start with a short self-selected row; the tester is validating software, not a fitness assessment.

## Updates and return to Ergatta

For updates, install the newer APK over the current app using the same steps and **Install/Update** or `adb install -r`. This preserves local profiles and careers when Android accepts the signing identity. If Android reports a signature mismatch or older-version error, stop and contact the owner. Do not uninstall, clear storage, or bypass the version check.

To use Ergatta again, end/save the OpenRow session, leave OpenRow, then launch Ergatta from the app drawer or restart the tablet. Only one app should use the S4 at a time. Uninstalling OpenRow removes its local profiles, workouts and progress. The alpha has no cloud backup or account sync; keep it installed if those records matter.
