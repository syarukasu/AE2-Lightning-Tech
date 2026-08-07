# AE2LT 2.0.6 -> Forge 1.20.1 difference map

This document is a navigation index for the next Forge 1.20.1 update. It is not
an assertion that the listed 2.0.6 features already work on Forge.

## Source and target

```text
Source artifact          ae2lt-2.0.6.jar
Source SHA-256           bea3e8196a3f126e2d8fcedc86bdb44b536efd3bedd28d3f89bdb0848e5a687b
Source environment       Minecraft 1.21.1 / NeoForge 21+ / Java 21
Source AE2               [19.2.17, 19.3)
Source class major       65

Target branch            forge-1.20.1-port
Target environment       Minecraft 1.20.1 / Forge 47.4.20 / Java 17
Target AE2               15.4.10 / 15.4.x
Current Forge baseline   1.1.4-forge-1.20.1-r7
```

The older 2.0.3 planning document is superseded for scope selection. It remains
useful background, but the supplied 2.0.6 artifact is the new fixed comparison
input.

## Artifact inventory

```text
Total JAR entries        3,136
AE2LT class files        1,507
Top-level/non-inner      948
English language keys    1,301
Recipes                  244
Advancements             25
Blockstates              67
Item models              191
Block models             153
Textures                 382
```

The supplied JAR has no `ja_jp.json`. The current Forge r7 localization contains
823 keys, so the 2.0.6 English catalog adds at least 478 keys that need Japanese
coverage and placeholder validation.

## Platform-level differences

2.0.6 is not a binary-compatible update for the Forge port.

- NeoForge registration, events, capabilities/attachments, and payload APIs.
- Minecraft 1.21.1 Data Components, StreamCodec, data/resource schemas, and
  method signatures.
- AE2 19.2.x APIs instead of AE2 15.4.x APIs.
- Java 21 bytecode instead of the Forge ecosystem's Java 17 baseline.
- Required Thunderbolt dependency in range `[1.0.1, 2.0.0)`.
- Embedded AE2WTLib API 19.4.1 and MixinSquared libraries.
- Four Mixin configurations:
  - `ae2lt.mixins.json`
  - `ae2lt.recipeviewer.mixins.json`
  - `ae2lt.ae2wtlib.mixins.json`
  - `ae2lt.mekanism.mixins.json`

Tracked by Issue #12.

## Major new feature groups

### Matter Warping Matrix

A new crafting multiblock family with controller, casing, glass, ports, pattern
storage tiers, multiple compute-core tiers, crafting-thread units, energy,
formation scanning, auto-build planning, pattern repository, UI, networking,
and rendering.

Tracked by Issue #13.

### Tianshu Supercomputer

A second large compute system with its own controller, ports, compute units,
pattern/seed storage, closed-loop patterns, global reserved stock, maintenance
rules, direct upload, wired/wireless pattern terminals, and multiple encoding
panels.

Tracked by Issue #14.

### Pigmee Technology and Alien Starship

Adds Pigmee Fumo variants, Mentalmath Unit, Molecular Assembler, Pattern
Provider, Pigmee storage, ritual/conversion entities and recipes, an Alien
Starship world structure, loot, and advancement progression.

Tracked by Issue #15.

### LightningKey and expanded storage cells

Adds a lightning AE key type and client rendering, bulk lightning cells,
additional infinite/fixed cells, cell components/housings, and new persistence
requirements.

Tracked by Issue #16.

## Existing Forge feature groups with large 2.0.6 changes

### Overloaded Interface, Pattern Provider, Wireless, Frequency

The Forge baseline already contains these systems and several important
Forge-only loss-prevention and performance fixes. 2.0.6 reorganizes them into
separate logic, queue, fairness, return, overflow, cluster, link-index, and
security services. The port must preserve the existing fixes while adopting
useful new behavior.

Tracked by Issue #17.

### Celestweave, Phase systems, Railgun, Device modules

The Forge baseline already includes Celestweave armor, phase flight, railgun,
and device workbench/hub behavior. 2.0.6 adds/refactors persistent state,
service boundaries, movement assist, Phase Lock projection/vault state,
Mekanism protection, generic device capability/module storage, and expanded
railgun execution and visual effects.

Tracked by Issue #18.

## Integration, resource, and data differences

The 2.0.6 artifact contains JEI and EMI viewers and transfer handlers,
AE2WTLib terminal integration, Mekanism and Veil integration, render fallbacks,
large-stack rendering, interactive multiblock previews, and expanded optional
MOD gates.

Recipe inventory:

```text
59  crafting_shaped
53  lightning_assembly
46  overload_processing
17  crystal_catalyzer
15  crafting_shapeless
15  lightning_simulation
9   lightning_transform
7   storage_cell_disassembly
7   lightning_strike
4   firmament_conversion
3   AE2 inscriber
3   AE2WTLib combine
1   ExtendedAE crystal assembler
1   ExtendedAE circuit cutter
1   Mekanism crushing
1   AE2WTLib upgrade
1   creative Pigmee duplication
1   hyperdimensional Pigmee conversion
```

Assets, recipes, advancements, viewer integration, optional dependencies, and
localization are tracked by Issue #19.

## World and compatibility requirements

The following Forge r7 data must remain readable:

- existing block, item, BlockEntity, entity, menu, recipe, and public API IDs;
- frequency membership and wireless links;
- overloaded provider/interface inventories and overflow state;
- storage-cell contents and limits;
- machine progress and recipe state;
- Celestweave, railgun, and device-module NBT;
- existing Firmament Starship world data.

New 2.0.6 data must use explicit versioned NBT/SavedData schemas on 1.20.1.
Unsupported 1.21.1 state must not be silently discarded before a World opens.

The known `AEBaseEntityBlock.newBlockEntity` failure caused by a null
`BlockEntityType` must be covered for every BlockEntity-backed block.

Tracked by Issue #20.

## Repository and implementation order

```text
#11 Single canonical repository
  -> #12 Platform/API compatibility layer
      -> #13 Matrix
      -> #15 Pigmee
      -> #16 Storage
      -> #17 Provider/Wireless
      -> #18 Armor/Railgun
      -> #14 Tianshu (uses the common compute boundary)
      -> #19 Integration/Data/Assets
      -> #20 Migration/Runtime/Performance/Release
```

The complete checklist is Epic #21.

## Port classifications

Every source class, data file, and asset should be assigned one status before a
release is claimed:

```text
EXISTING_EQUIVALENT
PORT_REQUIRED
PORT_WITH_DIFFERENT_1_20_1_IMPLEMENTATION
NOT_APPLICABLE_TO_1_20_1
UNSUPPORTED_WITH_RECORDED_REASON
VERIFIED
```

A successful compile is not sufficient for `VERIFIED`. Runtime, feature, save,
and release verification remain separate states.
