# Gravity Well visual audit - 29 October 2026

Follow-up: [0.11.0 stellar boss enhancement](GRAVITY-WELL-BOSS-VISUALS.md) addresses class identity, spherical surface rotation and stellar encounter focus. The findings below describe the earlier 0.10.7 audit.

Reviewed on the connected 1920 x 1080 tablet, using native Android renderers and the actual game UI. Fixes are included in **0.10.7**.

## Coverage and evidence

- All 17 classes at 100%, 50%, and 8% remaining mass: 51 body/state combinations before and after fixes, captured as 18 native contact sheets.
- Live ring stripping, comet approach and inward flow, ordinary gameplay, pause and summary screens. Recent capture and stellar shearing regressions were rerun.
- Home, career, both upgrade pages and all three atlas pages inspected on the tablet.
- Every graphics renderer and its shared geometry reviewed in source, including draw ordering, texture caches and bounded particle/fragment budgets.
- Renderer contact sheets are isolated fixtures, not career progression. The gameplay/summary check used the separate synthetic career. No real workout was started or credited.

[Body sheet: intact](../build/visual-audit/after-1-page1.png) | [half consumed](../build/visual-audit/after-0.5-page1.png) | [nearly consumed](../build/visual-audit/after-0.08-page1.png) | [stars](../build/visual-audit/after-1-page2.png) | [ring/comet recording](../build/visual-audit/ring-comet.mp4)

## Fixed findings

| Finding | Visible effect | Fix |
| --- | --- | --- |
| Long-stream grains have a fixed 2.8-second history | Long comet inlets have a ribbon but no moving grains near the well | Grain lifetimes now span the actual flow length; birth still starts at the source |
| Stellar ejecta can use rocky mantle trajectories | Loose particles can appear detached from the star | Stars and gas clouds emit from their surface rather than invisible mantle-chip positions |
| Gas-cloud erosion clips a diffuse texture to a hard contour | A soft cloud turns into a small, hard-edged oval | Scale the diffuse texture with remaining mass; remove the solid silhouette and added central glow |
| Large planets have baked lighting plus a second directional shadow | Two competing light directions and excessive darkening as textures rotate | Use one consistent directional lighting pass |
| Remnant shading uses the original full-body bounds | Late-stage cores look like flat cropped disks | Fit the lighting to the current eroded silhouette |
| Ordinary-object glows retain the original size | Nearly consumed objects leave oversized soft blobs | Shrink glow with the remnant and fade very small masses |
| Planetary atmospheres retain a complete circular rim after chunks leave | A detached circle floats around the damaged planet | Fade the atmospheric rim during initial stripping, before major mantle loss |
| Stellar HUD reports planned filament count too early | It claims exposed filaments even before they appear, including a single unupgraded stream | Show filament detail only when multiple filaments have visibly begun developing |
| Home artwork uses the original neon oval | The game card misrepresents the current black hole graphics | Reuse the production singularity renderer for the static cover |
| Summary always uses plural body label | One capture reads as "1 bodies captured" | Correct singular/plural wording |

The visual lab also now respects ordinary/boss class flags and clamps invalid class indices. It includes static catalog contact sheets and an optional crowded scene to make future reviews reproducible.

### Before and after: diffuse cloud and shrinking cores

Before:

![Before: hard-edged cloud and flatter cores](../build/visual-audit/before-0.5-page1.png)

After:

![After: diffuse cloud and shading fitted to remaining cores](../build/visual-audit/after-0.5-page1.png)

## Remaining improvements, in recommended order

| Priority | Improvement | Evidence / reason | Tablet-conscious approach |
| --- | --- | --- | --- |
| 1 | Stabilize busy-scene frame pacing | Crowded 29-body fixture has 36 ms median / 61 ms p95 frame latency; see measurements below | Reduce decorative detail for small or distant objects, adapt ribbon segments and grain counts to frame cost, prioritize the boss, and measure cache warm-up separately |
| 2 | Improve overlap and depth consistency | Code partitions whole bodies and streams at world y=0, while the disk is tilted; dust is drawn entirely before the hole | Use a shared projected depth rule and sort bodies consistently; split foreground dust from background dust. This is a confirmed rendering limitation; a large overlap pop was not reproduced in the sampled scenes |
| 3 | Add more motion to the disk itself | Most accretion turbulence is a static cached image with moving wisps layered over it | Prototype a small number of cached flow phases, preserve disk orientation, and benchmark before adding more alpha layers |
| 4 | Give classes stronger visual identities | Meteoroid/asteroid/rocky moon share a crater map; comet/icy moon are similar; white dwarf/neutron star use similar blue stellar treatment | Add a few silhouette/material variants and distinct compact-boss accents; avoid simply increasing texture resolution |
| 5 | Make stellar surfaces less regular | Granulation reads as a repeated cellular pattern and prominences are four evenly distributed loops | Mix broader, softer turbulent patches and vary prominence placement/length while retaining cached surfaces and bounded paths |
| 6 | Improve spherical rotation | Surface maps rotate as 2D disks, which is especially obvious on giant-planet bands | Prototype a few cached longitude frames for the featured body, keeping the light direction fixed |
| 7 | Make capture and completion feel more rewarding | Game updates a flash signal, but the renderer does not consume it | Reuse that signal for a small accretion-brightness pulse or brief local light response, without adding particle emitters |
| 8 | Improve the atlas as a visual reference | All atlas pages are text-only despite a varied body catalog | Add small cached body thumbnails, keeping the current readable spacing |

Ring fragmentation already provides convincing whole-ring movement and breakup and should be preserved. Capture motion now settles without the former velocity snap. Stellar cloud intensity remains continuous across shearing in the regression checks. The black center stays opaque in the sampled scenes. Menus, pause and summary layouts fit the tablet without observed text overlap.

## Tablet measurements

Screen recording was off during the measurements. These are short samples, not a thermal endurance test.

| Sample | Rendered frames | Sampling window | Median | p95 | p99 |
| --- | --- | --- | --- | --- | --- |
| Lighter real gameplay renderer, synthetic input | 445 | about 15 seconds | 22 ms | 30 ms | 40 ms |
| Crowded native preview, 29 initial bodies | 699 | about 15 seconds | 36 ms | 61 ms | 69 ms |

The ordinary gameplay view runs at roughly 30 updates per second in this sample. The crowded preview uses a different redraw schedule; its frame count must not be treated as a direct gameplay FPS comparison. Android flags deadline misses in both samples, so this audit does not establish locked 60 FPS. The crowded scene exceeds a 33 ms budget at p95 and is the clearest reason to improve detail management before adding substantial rendering work.

[Gameplay raw diagnostics](../build/visual-audit/gameplay-gfxinfo.txt) | [crowd raw diagnostics](../build/visual-audit/crowd-gfxinfo.txt) | [crowd screenshot](../build/visual-audit/crowd.png)

## Validation and scope

- Build and APK signature verification pass.
- **6,574 automated checks pass**, including long-stream grain coverage without prefilled trails and stellar particle origins over many capture phases.
- Existing mass conservation, faster erosion, stream continuity, smooth capture, pause/save restoration and fitness isolation checks pass.
- Rendering-only lighting, cloud and glow changes were checked with native before/after screenshots rather than implementation-mirroring tests.
- No AndroidRuntime exceptions were reported during the tablet review.
- Only the connected tablet and landscape resolution were inspected. Other aspect ratios, every possible object overlap and long-duration thermal behavior remain unverified.

## Reproducing the isolated review

Launch `.GravityWellPreviewActivity` with `audit=true`, `page=0..2`, and `remaining=1`, `0.5`, or `0.08` for the contact sheets. Use optional `age` to inspect ring-strip stages. Normal preview mode accepts `kind=0..16`, `approach=1` or `2` for rear/front capture, and `crowd=true` for the stress fixture. These modes never open career or athlete files.
