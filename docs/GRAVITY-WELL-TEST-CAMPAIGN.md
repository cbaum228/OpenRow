> Upgrade-tree update: see [0.15.0 expanded catalog and progression rules](GRAVITY-WELL-EXPANDED-TREE.md). The 30-node/chapter-gate details below describe the previous release.

# Gravity Well test campaign — 0.13.0

The first playable campaign covers Debris field, Planetary systems and Giant worlds. Its first-clear bosses are dwarf planet, rocky planet, gas giant and ice giant. Stellar artwork remains available in the existing visual lab; stellar campaign content and real prestige are future work.

## Playing

Open **Gravity Well → Campaign map → Select next destination**. Start the selected activity directly, or open **Training & assessments** to use it with a trainer-led workout. Free play is a separate selectable activity using the same XP, upgrades and materials.

Survey, Harvest and Research are cumulative preparation destinations. Harvest nodes bias the ordinary body mixture toward the chapter's materials. Each preparation destination awards one material unit. A completed destination continues ordinary resource collection until the session ends; choose the next destination between sessions. Progress survives interruptions and shorter workouts.

Preparation totals 360 eligible minutes: 90 in chapter one, 120 in chapter two and 150 in chapter three (75 before each giant). At 20–30 eligible minutes per workout this is roughly 12–18 workouts plus encounters. Guided warm-up, recovery and cool-down are excluded, so shorter work blocks take more sessions. These are configurable game pacing values, not workout prescriptions or calendar gates.

Boss expeditions introduce their selected boss during verified movement. Guided introductions need at least 60 seconds remaining in a WORK block. Pending bosses wait through recovery; active bosses keep their health/capture state across workouts. Trainer completion always ends the workout. Earlier bosses become repeatable harvest destinations after their first clear; a repeat ends after one boss, without automatically starting another.

## Materials and upgrades

There are 30 upgrade definitions and 73 total purchasable ranks: the original nine definitions/31 ranks, plus 21 expedition nodes with two ranks each. Chapter and branch filters keep the tablet purchase screen to six cards per page. Nodes have stable IDs; prerequisites are validated and game save files reject unknown upgrade definitions non-destructively.

Each chapter adds attraction (+8%), processing (+8%), capacity (+30), XP refinement (+10%), inward drift (+8%), erosion (+8%) and material recovery (+10%) nodes. Values are per rank and additive within their stat family. XP prices are 50 × chapter × 1.7^ownedRank, rounded. Chapter-two ranks also cost one mineral; chapter-three ranks one volatile. No optional upgrade purchase blocks a campaign objective. Planetary fractures open after the dwarf-planet clear; existing prerequisites still apply.

Minerals come from rocky/metallic bodies, volatiles from comets and icy bodies, and giant gas from clouds/gas giants. One unit is earned per 100 mass actually stripped while rowing, retaining fractions and applying owned recovery bonuses. Boss completion adds five units once. Decorative particles and physical fragmentation cannot duplicate material. Preparation and first-clear bookkeeping do not grant measured distance, strokes or athlete records.

## Synthetic testing and prestige

Switch **Input: S4 USB** to the separate test career before opening its campaign map. The lab offers 1×/30×/120× preparation progress and checkpoints for each boss plus campaign completion. Acceleration affects preparation only: it does not speed up physics, workout clocks or athlete history. Checkpoints can be loaded between synthetic sessions and intentionally replace the synthetic world; they preserve existing test purchases. Real career code refuses these checkpoints and cannot load accelerated/cosmetic lab state.

Cosmetic previews cycle through violet accretion, a cyan horizon and a constellation legacy emblem. Their stable reward IDs reserve future cosmetic unlocks. No real prestige reset or permanent gameplay prestige bonus is enabled in this release. Completing the ice giant leaves harvesting, upgrades and free play available.

## Compatibility and boundaries

The existing atomic career checkpoint owns campaign progress, fractional materials, selected activity, boss association and reward claims together. Indexed legacy purchases migrate to stable IDs, retaining XP, ranks and active objects. Existing distance-based discovery access remains available in free play/atlas harvest selection. A legacy active boss may finish without skipping campaign preparation or awarding a campaign first clear.

Real and synthetic careers continue using separate files. Training history, athlete calibration and measured workout accounting retain independent ownership. Preparation is equal for equal eligible movement time at different speeds. Physics, material income and XP still reflect existing input and owned upgrades; this release does not claim equal total rewards across athlete capabilities.

Campaign ordinary population is bounded at 130 within the existing overall 150-body limit, reserving room for encounters and fragments. Pending boss arrivals stop new ordinary matter and guide accumulated dust inward, preventing a full scene of persistent high-input dust orbits from blocking a boss. The existing 240-particle cap and boss visual budgets remain unchanged.

## Verification

All **7,485 automated checks** pass, including 244 campaign/economy/migration/isolation checks and 22 trainer/campaign integration checks. The unchanged deterministic 30-minute legacy scenario still earns 3,364.704 XP.

Full deterministic campaigns at three inputs all require 360 preparation minutes. With one affordable new rank per branch purchased after each destination:

| Physics input / illustrative speed | Total boss time | New ranks purchased | Unspent XP |
|---|---:|---:|---:|
| 0.2 / 1.5 m/s | 4.0 min | 42 | 13,313 |
| 0.5 / 2.5 m/s | 3.8 min | 42 | 25,946 |
| 1.0 / 4.0 m/s | 3.3 min | 42 | 31,104 |

These are software fixtures, not exercise targets. Material supply is generous; all new ranks are affordable by the campaign's end in these paths. This is an initial test economy that needs real-play feedback before long-term costs and specialist harvesting requirements are tightened. Hundreds of authored nodes, branching campaign routes and the rest of the galaxy are not included yet.

Tablet screenshots and save backups are under `build/campaign-test/`. Native checks cover the map, saved synthetic boss continuation, new upgrade purchase, chapter controls and cosmetic previews. The normal campaign's multi-workout pacing has been simulated rather than validated through months of live exercise.

Native validation also completed a dwarf-planet first clear across two saved synthetic sessions, purchased and restored Field reach 1, exercised the gas-giant checkpoint and displayed campaign completion/cosmetic previews. Real XP, meters, strokes, original ranks and all athlete-history properties matched the pre-install backup. Only the athlete file comment timestamp was refreshed by the existing sync routine. No training-history file was created and the AndroidRuntime log contained no fatal exception. The tablet is left on the real campaign map with the initial survey selected and zero campaign progress credited.
