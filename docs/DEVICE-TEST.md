# First tablet test — October 22, 2026

## Hardware

- Model reported by Android: Ergatta Rower.
- Android 9 / API 28.
- Board: rk30sdk_elc_boe; hardware: rk30board.
- CPU ABIs: arm64-v8a, armeabi-v7a, armeabi.
- Display: 1920 × 1080, density 160 dpi.
- Tablet has separate power, full-size USB for the S4, a small USB port used for computer debugging, HDMI and audio.
- S4 USB device: 04d8:000a, Microchip Technology Inc., CDC RS-232: WR-S4.2.
- Debugging and S4 connections operated simultaneously.

## Tablet access

Tap the upper-left corner of the Ergatta profile selection screen 7–10 times.
Settings remains accessible. Keep the S4 connected during this procedure: the user
reported that disconnecting it interferes with accessing the menu.
Developer options were unlocked using seven taps on Build number; USB debugging is enabled.

## Software state

Android Platform Tools are in `tools/platform-tools`. The rowing app package is
`com.ergatta.seaborn`. It was temporarily force-stopped for testing, with no uninstall,
package disable, launcher change or firmware modification. To return to Ergatta,
disconnect the probe and launch Ergatta from the home screen (or restart the tablet).

## Results

- APK compiled and its signature verified; final APK installed successfully.
- Probe launched in landscape; layout inspected from an actual tablet screenshot.
- CDC control/data interfaces claimed; 115200 baud, 8N1, DTR/RTS enabled.
- `USB` produced `_WR_`: handshake confirmed.
- `IV?` produced `IV40210`: raw monitor version code 40210.
- `IRD057` produced `IDD0570000` (distance register).
- `IRD140` produced `IDD1400000` (stroke count register).
- `IRS1A9` produced `IDS1A900` (stroke rate register).
- Regular `PING` messages received, with repeated successful query responses.
- Disconnect released the USB connection; the final build successfully connected again.
- Runtime crash log check showed no reported crash during the test.
- Diagnostic files accessible via `run-as` in the final debug build.
- User rowing test confirmed live stroke-start (`SS`) and stroke-end (`SE`) events.
- Stroke-count register advanced from 0 to 5; distance register advanced from 0 to hex `000F` (15, expected meters; physical display comparison pending).
- Stroke-rate register replies remained `IDS1A900` during this short test, so stroke-rate decoding/register choice needs investigation before presenting it as a live metric.

Evidence: `build/tablet-connected.png`, `build/s4-device-test.log`, and `build/s4-rowing-test.log` (local, ignored).

## Still to test

- Validate decoded metric units against the physical monitor.
- Resolve zero stroke-rate register replies despite confirmed stroke events.
- Physical cable removal and reconnection, monitor power cycling, long sessions.
- Boot/launcher behavior when Ergatta is allowed to run normally.

The probe is left open and connected for the user to perform the rowing test.
