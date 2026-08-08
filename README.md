# AE2 Lightning Tech - Unofficial Forge 1.20.1 Port

[中文文档](README_zh_CN.md)

> [!IMPORTANT]
> This repository is the **single canonical source, issue tracker, and release
> location** for the unofficial Minecraft 1.20.1 Forge port of
> [AE2 Lightning Tech](https://github.com/ae2lt/AE2-Lightning-Tech).
> It is not maintained, sponsored, or endorsed by the original AE2 Lightning
> Tech authors or by the Applied Energistics 2 team.
>
> The upstream CurseForge and Modrinth projects distribute the original
> NeoForge edition. Forge 1.20.1 downloads and support belong to this repository.

An [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)
add-on that introduces a lightning energy system, advanced machines, and
high-throughput overloaded ME network components.

## Canonical Forge project

```text
Repository   https://github.com/syarukasu/AE2-Lightning-Tech
Branch       forge-1.20.1-port
Issues       https://github.com/syarukasu/AE2-Lightning-Tech/issues
Releases     https://github.com/syarukasu/AE2-Lightning-Tech/releases
```

The former `syarukasu/AE2-Lightning-Tech-Forge-1.20.1` repository is retired as
a duplicate and must not receive new development, issues, or releases. See
[`docs/REPOSITORY_CONSOLIDATION.md`](docs/REPOSITORY_CONSOLIDATION.md).

## Current and next release lines

The current published Forge baseline is:

```text
AE2LT       2.0.6-forge-1.20.1-r1
Minecraft   1.20.1
Forge       47.4.20
AE2         15.4.10 through 15.4.x
Java        17
```

The next major port target is upstream **AE2LT 2.0.6**. The supplied NeoForge
1.21.1 artifact cannot be loaded directly on Forge 1.20.1; its Loader, Minecraft,
AE2, networking, Data Component, Mixin, world-data, and Java 21 differences must
be ported feature by feature. Progress is tracked in
[Epic #21](https://github.com/syarukasu/AE2-Lightning-Tech/issues/21) and
[`docs/UPSTREAM_2_0_6_DIFF.md`](docs/UPSTREAM_2_0_6_DIFF.md).

## About

AE2 Lightning Tech turns lightning into a usable resource. Capture natural
strikes, refine them into High Voltage and Extreme High Voltage tiers, and feed
them into machines to grow Overload Crystals. These materials support advanced
processing, high-throughput ME components, wireless pattern routing, equipment,
and new progression paths.

## Features in the current Forge baseline

### Lightning energy

Two tiers of lightning energy—High Voltage and Extreme High Voltage—can be
stored, transferred, generated, and consumed alongside FE.

### Lightning collection and production

- **Lightning Collector** — captures lightning that strikes nearby rods.
- **Atmospheric Ionizer** — a multiblock weather-processing machine.
- **Tesla Coil** — produces HV and EHV lightning from materials and FE.

### Overload crystal progression

Damaged, Cracked, Flawed, and Flawless budding stages grow into Overload Crystal
Clusters and support the rest of the overloaded progression.

### Lightning machinery

- **Lightning Assembly Chamber**
- **Lightning Simulation Room**
- **Overload Processing Factory**
- **Crystal Catalyzer**
- **Firmament Conversion Core**

### Overloaded ME network

- Overloaded ME Controller, Interface, Pattern Provider, and colored cables.
- Extended Overloaded Pattern Provider with paged pattern capacity.
- Wireless Overloaded Controller, Receiver, frequency management, and connection
  tools.
- Overloaded Pattern Encoder with byproduct and Ignore-NBT support.

### Equipment and devices

- Celestweave armor and installable armor submodules.
- Electromagnetic Railgun, ammunition, energy, network binding, and effects.
- Device Hub and Overload Device Workbench.

## Public API for add-on authors

`com.moakiee.ae2lt.api.*` is the only stable surface exposed to third-party
mods. Anything outside that package is internal and may change between port
releases.

Public areas include:

- `AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK`
- `ILightningEnergyHandler`
- `LightningTier`
- `LightningCollectedEvent`
- `AE2LTBlockEntityIds` and `AE2LTRecipeIds`
- `FrequencyApi`, immutable frequency snapshots, and binding helpers
- `PatternProviderUiProfile`

The 2.0.6 port must preserve this Forge API where practical or provide an
explicit compatibility facade and migration table.

## Dependencies

| Mod | Requirement |
|---|---|
| Applied Energistics 2 | Required |
| Jade, JEI, AE2 Wireless Terminals, Curios | Optional integrations |
| Advanced AE, ExtendedAE, ExtendedAE Plus, Applied Flux | Optional integrations |
| Mekanism, Neo ECO AE Extension, Flywheel, Ponder | Optional integrations |

Exact declared ranges are in `gradle.properties` and
`src/main/resources/META-INF/mods.toml`.

## Building

Use Java 17:

```bash
./gradlew clean build --no-daemon
```

Windows:

```powershell
.\gradlew.bat clean build --no-daemon
```

The build separates the slim development JAR from the reobfuscated Jar-in-Jar
distribution. Publish only the unclassified distribution artifact:

```text
build/libs/ae2lt-2.0.6-forge-1.20.1-r1.jar
```

Do not distribute the `-slim.jar` artifact.

## Reporting issues

Report Forge 1.20.1 port problems at:

- https://github.com/syarukasu/AE2-Lightning-Tech/issues

Include the Minecraft, Forge, AE2, AE2LT, and optional integration versions,
plus a complete log or crash report. Issues specific to this port should not be
reported to the upstream NeoForge maintainers unless they can be reproduced on
the upstream release.

## Licenses

[![Source License](https://img.shields.io/badge/Source-LGPL--3.0--only-blue)](LICENSE)
[![Assets License](https://img.shields.io/badge/Assets-CC%20BY--NC--SA%203.0-lightgrey)](LICENSE_ASSETS.md)

- Source code: **LGPL-3.0-only**
- Textures and other visual assets: **CC BY-NC-SA 3.0**

Every distributable JAR includes the source license, asset license, permanent
credits, and port provenance notices under `META-INF`.

## Credits

- **Original AE2 Lightning Tech:** created by **MOAKIEE** and developed with
  **CystrySU**, **gjmhmm8**, **_leng**, **TedXenon**, and **MHanHanBing**.
- **Applied Energistics 2:** created and maintained by
  **TeamAppliedEnergistics**.
- **Forge 1.20.1 port:** maintained by **syarukasu** as an independent,
  unofficial port.

See [CREDITS.md](CREDITS.md) and [PORT_NOTES.md](PORT_NOTES.md) for the permanent
attribution and provenance records.
