# Gravity Well expanded upgrade tree — 0.15.1

Implemented first-three-chapter test catalog: 81 nodes, including 63 main nodes, nine specialists and nine retained foundation tools. There is one tier per chapter, with three consecutive nodes per main branch inside each tier. Chapter bands describe the intended era; investment unlocks main tiers rather than chapter completion.

## Progression rules

- Earlier **currently owned main ranks** unlock tiers at 0, 8 and 23. Foundation gameplay ranks count as tier zero. Appearance and specialist ranks never count.
- Main nodes require one rank in their preceding branch node. Every main node has two ranks. XP prices increase by 1.7 per owned rank, rounded; material prices are per purchase.
- Specialists require four main ranks across at least two nodes in their own tier, the tier gate, specialty materials and any listed distance/discovery condition. Boss specialists require their relevant boss discovery; legacy discoveries remain valid.
- Distances are cumulative career distance, never speed or workout targets. Later main tiers require 10 km (last node in tier 2), 15 km (first two nodes in tier 3), or 25 km (last node in tier 3).
- Specialists are optional. Three-branch focused and seven-branch balanced builds can reach tier three without buying any.
- Six hours of campaign preparation remain unchanged. Training phase eligibility and athlete-relative graphics are unchanged.

## Harvesting and migration

The original minerals, volatiles and gas balances remain intact. Seven additional source-specific balances start at zero in older saves: dense minerals, comet ice, dwarf cores, planetary cores, ring debris, giant envelopes and ice condensates. No generic legacy mineral is reclassified as a core.

Physical mass stripping guarantees fractional source yield at one unit per 100 mass, affected by yield upgrades. Ringed worlds additionally award 0.25 ring debris per 100 stripped mass. Boss completion adds five source units and, for ringed worlds, five ring debris. This is an economy approximation of ring salvage; decorative ring particles never create resources. Completion rewards remain idempotent across reloads.

Existing 30 IDs, array positions, ranks and effects are preserved. Existing expedition nodes anchor the first node position in tiers 1, 2 and 3. Owned nodes retain access to subsequent ranks even if their new tier or predecessor conditions are not yet met; XP and material costs still apply. New IDs are appended, preserving indexed legacy saves. Future content should use metadata and stable IDs, never infer progression from array order.

## Tree interaction

Swipe horizontally to browse; chapter buttons jump into their tier group. Labels and the detail panel stay fixed. Only visible nodes are drawn. Tap to inspect, then explicitly purchase. Specialists sit between Discovery and Appearance. The detail panel shows progress toward requirements, material balance and harvest source. A selected node highlights its direct prerequisite link.

## Catalog

Foundation disruption tools retain their existing discovery restrictions: dwarf planet, future brown dwarf, and future white dwarf respectively. Foundation prerequisites remain unchanged.

| ID | Node | Tier | Ranks | Base XP | Effect | Requirements |
|---|---|---:|---:|---:|---|---|
| pull | Gravitational reach | 0 | 5 | 25 | Stronger attraction +18% / rank | Foundation |
| process | Accretion flow | 0 | 5 | 25 | More conversion per input +20% / rank | Foundation |
| value | Cosmic refinement | 0 | 4 | 50 | Processed matter XP +25% / rank | Foundation; process |
| capacity | Orbital reservoir | 0 | 4 | 60 | Stored matter capacity +60 / rank | Foundation; pull |
| rocks | Deep-field discovery | 0 | 3 | 100 | Richer asteroids +35% / rank | Foundation; pull; process |
| halo | Violet horizon | 0 | 1 | 150 | Unlock a violet accretion halo | Foundation; value; capacity |
| planet_fracture | Planetary fractures | 0 | 3 | 180 | Planet bosses split into 2 / 3 / 4 | Foundation; pull; process |
| stellar_shear | Stellar shearing | 0 | 3 | 400 | Expose 2 / 3 / 4 plasma filaments | Foundation; planet_fracture; rocks |
| core_rupture | Remnant rupture | 0 | 3 | 800 | Remnants split into 2 / 3 / 4 | Foundation; stellar_shear; capacity |
| exp_reach_1 | Field reach | 1 | 2 | 50 | Attraction +8% per rank | 0 earlier main ranks |
| exp_flow_1 | Matter flow | 1 | 2 | 50 | Processing +8% per rank | 0 earlier main ranks |
| exp_storage_1 | Deep storage | 1 | 2 | 50 | Capacity +30 per rank | 0 earlier main ranks |
| exp_refinement_1 | Refined matter | 1 | 2 | 50 | XP refinement +10% per rank | 0 earlier main ranks |
| exp_drift_1 | Inward drift | 1 | 2 | 50 | Inward drift +8% per rank | 0 earlier main ranks |
| exp_erosion_1 | Surface tools | 1 | 2 | 50 | Erosion +8% per rank | 0 earlier main ranks |
| exp_yield_1 | Material recovery | 1 | 2 | 50 | Material yield +10% per rank | 0 earlier main ranks |
| exp_reach_2 | Extended reach | 2 | 2 | 100 | Attraction +8% per rank | 8 earlier main ranks; tree_reach_3; 1 Minerals from rocky bodies |
| exp_flow_2 | Dense-matter processing | 2 | 2 | 100 | Processing +8% per rank | 8 earlier main ranks; tree_flow_3; 1 Minerals from rocky bodies |
| exp_storage_2 | Planetary storage | 2 | 2 | 100 | Capacity +30 per rank | 8 earlier main ranks; tree_storage_3; 1 Minerals from rocky bodies |
| exp_refinement_2 | Dense-mineral refinement | 2 | 2 | 100 | XP refinement +10% per rank | 8 earlier main ranks; tree_refinement_3; 1 Minerals from rocky bodies |
| exp_drift_2 | Debris routing | 2 | 2 | 100 | Inward drift +8% per rank | 8 earlier main ranks; tree_drift_3; 1 Minerals from rocky bodies |
| exp_erosion_2 | Planetary erosion | 2 | 2 | 100 | Erosion +8% per rank | 8 earlier main ranks; tree_erosion_3; 1 Minerals from rocky bodies |
| exp_yield_2 | Planetary survey | 2 | 2 | 100 | Material yield +10% per rank | 8 earlier main ranks; tree_yield_3; 1 Minerals from rocky bodies |
| exp_reach_3 | Deep reach | 3 | 2 | 150 | Attraction +8% per rank | 23 earlier main ranks; tree_reach_6; 15 km career; 1 Volatiles from icy bodies |
| exp_flow_3 | Gas processing | 3 | 2 | 150 | Processing +8% per rank | 23 earlier main ranks; tree_flow_6; 15 km career; 1 Volatiles from icy bodies |
| exp_storage_3 | Giant reservoir | 3 | 2 | 150 | Capacity +30 per rank | 23 earlier main ranks; tree_storage_6; 15 km career; 1 Volatiles from icy bodies |
| exp_refinement_3 | Gas separation | 3 | 2 | 150 | XP refinement +10% per rank | 23 earlier main ranks; tree_refinement_6; 15 km career; 1 Volatiles from icy bodies |
| exp_drift_3 | Giant drift | 3 | 2 | 150 | Inward drift +8% per rank | 23 earlier main ranks; tree_drift_6; 15 km career; 1 Volatiles from icy bodies |
| exp_erosion_3 | Ring tools | 3 | 2 | 150 | Erosion +8% per rank | 23 earlier main ranks; tree_erosion_6; 15 km career; 1 Volatiles from icy bodies |
| exp_yield_3 | Giant survey | 3 | 2 | 150 | Material yield +10% per rank | 23 earlier main ranks; tree_yield_6; 15 km career; 1 Volatiles from icy bodies |
| tree_reach_2 | Capture grip | 1 | 2 | 90 | Attraction +8% per rank | 0 earlier main ranks; exp_reach_1 |
| tree_flow_2 | Dust processing | 1 | 2 | 90 | Processing +8% per rank | 0 earlier main ranks; exp_flow_1 |
| tree_storage_2 | Reserve buffer | 1 | 2 | 90 | Capacity +30 per rank | 0 earlier main ranks; exp_storage_1 |
| tree_refinement_2 | Mineral sorting | 1 | 2 | 90 | XP refinement +10% per rank | 0 earlier main ranks; exp_refinement_1 |
| tree_drift_2 | Orbit damping | 1 | 2 | 90 | Inward drift +8% per rank | 0 earlier main ranks; exp_drift_1 |
| tree_erosion_2 | Mantle peeling | 1 | 2 | 90 | Erosion +8% per rank | 0 earlier main ranks; exp_erosion_1 |
| tree_yield_2 | Mineral survey | 1 | 2 | 90 | Material yield +10% per rank | 0 earlier main ranks; exp_yield_1 |
| tree_reach_3 | Comet approach | 1 | 2 | 115 | Attraction +8% per rank | 0 earlier main ranks; tree_reach_2 |
| tree_flow_3 | Steady feed | 1 | 2 | 115 | Processing +8% per rank | 0 earlier main ranks; tree_flow_2 |
| tree_storage_3 | Matter reserves | 1 | 2 | 115 | Capacity +30 per rank | 0 earlier main ranks; tree_storage_2 |
| tree_refinement_3 | Clean accretion | 1 | 2 | 115 | XP refinement +10% per rank | 0 earlier main ranks; tree_refinement_2 |
| tree_drift_3 | Debris retention | 1 | 2 | 115 | Inward drift +8% per rank | 0 earlier main ranks; tree_drift_2 |
| tree_erosion_3 | Polar erosion | 1 | 2 | 115 | Erosion +8% per rank | 0 earlier main ranks; tree_erosion_2 |
| tree_yield_3 | Debris sampling | 1 | 2 | 115 | Material yield +10% per rank | 0 earlier main ranks; tree_yield_2 |
| tree_reach_5 | Heavy-body coupling | 2 | 2 | 165 | Attraction +8% per rank | 8 earlier main ranks; exp_reach_2; 1 Minerals from rocky bodies |
| tree_flow_5 | Mixed-material flow | 2 | 2 | 165 | Processing +8% per rank | 8 earlier main ranks; exp_flow_2; 1 Minerals from rocky bodies |
| tree_storage_5 | Capture buffer | 2 | 2 | 165 | Capacity +30 per rank | 8 earlier main ranks; exp_storage_2; 1 Minerals from rocky bodies |
| tree_refinement_5 | Ice separation | 2 | 2 | 165 | XP refinement +10% per rank | 8 earlier main ranks; exp_refinement_2; 1 Minerals from rocky bodies |
| tree_drift_5 | Chunk guidance | 2 | 2 | 165 | Inward drift +8% per rank | 8 earlier main ranks; exp_drift_2; 1 Minerals from rocky bodies |
| tree_erosion_5 | Exposed-face erosion | 2 | 2 | 165 | Erosion +8% per rank | 8 earlier main ranks; exp_erosion_2; 1 Minerals from rocky bodies |
| tree_yield_5 | Core sampling | 2 | 2 | 165 | Material yield +10% per rank | 8 earlier main ranks; exp_yield_2; 1 Minerals from rocky bodies |
| tree_reach_6 | Deep capture | 2 | 2 | 190 | Attraction +8% per rank | 8 earlier main ranks; tree_reach_5; 10 km career; 1 Minerals from rocky bodies |
| tree_flow_6 | Core processing | 2 | 2 | 190 | Processing +8% per rank | 8 earlier main ranks; tree_flow_5; 10 km career; 1 Minerals from rocky bodies |
| tree_storage_6 | Planetary reserve | 2 | 2 | 190 | Capacity +30 per rank | 8 earlier main ranks; tree_storage_5; 10 km career; 1 Minerals from rocky bodies |
| tree_refinement_6 | Core refinement | 2 | 2 | 190 | XP refinement +10% per rank | 8 earlier main ranks; tree_refinement_5; 10 km career; 1 Minerals from rocky bodies |
| tree_drift_6 | Core delivery | 2 | 2 | 190 | Inward drift +8% per rank | 8 earlier main ranks; tree_drift_5; 10 km career; 1 Minerals from rocky bodies |
| tree_erosion_6 | Core exposure | 2 | 2 | 190 | Erosion +8% per rank | 8 earlier main ranks; tree_erosion_5; 10 km career; 1 Minerals from rocky bodies |
| tree_yield_6 | Planetary recovery | 2 | 2 | 190 | Material yield +10% per rank | 8 earlier main ranks; tree_yield_5; 10 km career; 1 Minerals from rocky bodies |
| tree_reach_8 | Envelope coupling | 3 | 2 | 240 | Attraction +8% per rank | 23 earlier main ranks; exp_reach_3; 15 km career; 1 Volatiles from icy bodies |
| tree_flow_8 | Plasma-ready conduits | 3 | 2 | 240 | Processing +8% per rank | 23 earlier main ranks; exp_flow_3; 15 km career; 1 Volatiles from icy bodies |
| tree_storage_8 | Surge buffer | 3 | 2 | 240 | Capacity +30 per rank | 23 earlier main ranks; exp_storage_3; 15 km career; 1 Volatiles from icy bodies |
| tree_refinement_8 | Volatile refinement | 3 | 2 | 240 | XP refinement +10% per rank | 23 earlier main ranks; exp_refinement_3; 15 km career; 1 Volatiles from icy bodies |
| tree_drift_8 | Envelope guidance | 3 | 2 | 240 | Inward drift +8% per rank | 23 earlier main ranks; exp_drift_3; 15 km career; 1 Volatiles from icy bodies |
| tree_erosion_8 | Envelope erosion | 3 | 2 | 240 | Erosion +8% per rank | 23 earlier main ranks; exp_erosion_3; 15 km career; 1 Volatiles from icy bodies |
| tree_yield_8 | Gas sampling | 3 | 2 | 240 | Material yield +10% per rank | 23 earlier main ranks; exp_yield_3; 15 km career; 1 Volatiles from icy bodies |
| tree_reach_9 | Giant-world reach | 3 | 2 | 265 | Attraction +8% per rank | 23 earlier main ranks; tree_reach_8; 25 km career; 1 Volatiles from icy bodies |
| tree_flow_9 | Giant throughput | 3 | 2 | 265 | Processing +8% per rank | 23 earlier main ranks; tree_flow_8; 25 km career; 1 Volatiles from icy bodies |
| tree_storage_9 | Accretion reserve | 3 | 2 | 265 | Capacity +30 per rank | 23 earlier main ranks; tree_storage_8; 25 km career; 1 Volatiles from icy bodies |
| tree_refinement_9 | Giant-matter refinement | 3 | 2 | 265 | XP refinement +10% per rank | 23 earlier main ranks; tree_refinement_8; 25 km career; 1 Volatiles from icy bodies |
| tree_drift_9 | Multi-stream routing | 3 | 2 | 265 | Inward drift +8% per rank | 23 earlier main ranks; tree_drift_8; 25 km career; 1 Volatiles from icy bodies |
| tree_erosion_9 | Giant erosion | 3 | 2 | 265 | Erosion +8% per rank | 23 earlier main ranks; tree_erosion_8; 25 km career; 1 Volatiles from icy bodies |
| tree_yield_9 | Rare-material recovery | 3 | 2 | 265 | Material yield +10% per rank | 23 earlier main ranks; tree_yield_8; 25 km career; 1 Volatiles from icy bodies |
| spec_comet_snare | Comet snare | 1 | 1 | 150 | Comet attraction +20% | 0 earlier main ranks; 4 same-tier ranks across 2 nodes; 2 Comet ice from comets |
| spec_overflow_recovery | Overflow recovery | 1 | 1 | 250 | Storage capacity +60 | 0 earlier main ranks; 4 same-tier ranks across 2 nodes; 2 Dense minerals from metallic asteroids |
| spec_comet_sampling | Comet sampling | 1 | 1 | 350 | Comet harvest yield +30% | 0 earlier main ranks; 4 same-tier ranks across 2 nodes; 2 Comet ice from comets |
| spec_planetary_anchor | Planetary anchor | 2 | 1 | 450 | Rocky boss attraction +20% | 8 earlier main ranks; 4 same-tier ranks across 2 nodes; 10 km career; 5 Dwarf cores from dwarf planets; relevant boss discovered |
| spec_core_extraction | Core extraction | 2 | 1 | 550 | Rocky boss harvest yield +30% | 8 earlier main ranks; 4 same-tier ranks across 2 nodes; 5 Planetary cores from rocky planets; relevant boss discovered |
| spec_precision_fracturing | Precision fracturing | 2 | 1 | 650 | Rocky boss erosion +15% | 8 earlier main ranks; 4 same-tier ranks across 2 nodes; 5 Planetary cores from rocky planets; relevant boss discovered |
| spec_ring_routing | Ring routing | 3 | 1 | 750 | Ringed-world inward drift +15% | 23 earlier main ranks; 4 same-tier ranks across 2 nodes; 5 Ring debris from ringed giants; relevant boss discovered |
| spec_envelope_shearing | Envelope shearing | 3 | 1 | 850 | Giant envelope erosion +15% | 23 earlier main ranks; 4 same-tier ranks across 2 nodes; 5 Giant envelopes from gas giants; relevant boss discovered |
| spec_giant_core_disruption | Giant-core disruption | 3 | 1 | 950 | Giant core erosion +25% below half mass | 23 earlier main ranks; 4 same-tier ranks across 2 nodes; 25 km career; 5 Ice condensates from ice giants; relevant boss discovered |

## Verification

7,589 checks pass. Focused (three branches, 54 ranks) and balanced (seven branches, 126 ranks) purchase simulations reach tier three without specialists. Full campaign simulations at three input levels retain 360 minutes of preparation, with total boss consumption time around 2.8–3.4 minutes across four bosses. Prices and yields remain generous test-campaign tuning, not a validated months-long economy.

Android 9 tablet build installed and inspected: chapter jumps, swipe scrolling, pinned labels/detail panel, specialist source display and Back navigation. Every pre-existing real career save property matched before and after the upgrade. No real workout or purchase was made during verification. Tablet preview: `build/tree-expansion/specialist.png`.
