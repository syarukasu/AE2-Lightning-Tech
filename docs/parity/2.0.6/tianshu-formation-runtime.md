# Tianshu Forge 1.20.1 runtime status

This document records the Forge 1.20.1 Tianshu Supercomputing Array port and
the AE2 integration layer added after formation support.

## Implemented

- Registered the Tianshu casing, glass, controller, port, main cores, blank,
  storage, parallel, amplifier, cooling, closed-loop pattern, and closed-loop
  seed blocks.
- Added the exact 7x7x7 template and horizontal orientation transforms.
- Added loaded-chunk-only formation scanning with no forced chunk tickets.
- Validated the controller, exactly one port candidate, central main core,
  supported peripheral units, parallel-unit requirements, and amplifier limits.
- Reused the existing pure Java 17 compute calculator for the scanned core
  profile.
- Applied and cleared the `formed` state when structure components change.
- Rechecks are event-driven around changed components, with a delayed retry for
  missing chunks.
- Added unit tests for a valid structure, missing parallel unit, and coordinate
  transforms.
- Ported the upstream Tianshu textures and standard Forge block/item models.
  The upstream custom connected-texture loader is intentionally not referenced
  because it is not part of this Forge 1.20.1 baseline.

## AE2 integration implemented in this port

- The formed Tianshu port is an AE2 network node with the standard channel and
  crafting-provider/requester contracts.
- AE2's `CraftingService` receives the formed Tianshu CPU through a narrow
  service mixin. Existing AE2 CPUs and providers remain on their normal path.
- Tianshu accepts only its own deterministic closed-loop pattern wrapper. A
  normal AE2, GT, or Mekanism pattern is never silently redirected into this
  executor.
- The port is also an AE2 `PatternContainer`. The Pattern Access Terminal sees
  the pattern-storage units as one virtual inventory, while each storage unit
  reuses AE2's pattern inventory and decoding rules.
- Added the `ae2lt:closed_loop_pattern` encoded-pattern item and its hand-held
  encoder screen. The screen accepts up to nine ordinary AE2 encoded patterns,
  derives external inputs and net outputs, and writes a server-authoritative
  NBT payload.
- The payload is decoded by AE2's `PatternDetailsHelper` and is visible to the
  Tianshu provider without changing ordinary AE2 pattern decoding.
- The Tianshu CPU executes each member pattern in order as one loop cycle,
  records an inventory delta, and rolls the cycle back when any member cannot
  complete. This prevents partial closed-loop execution from leaking items.
- Submitting a Tianshu-compatible plan uses AE2's initial inventory extraction
  and crafting links. The closed-loop executor then processes the deterministic
  pattern plan in bounded batches and returns the final output through AE2.
- The controller GUI reports formation state, pattern count, configured
  parallelism, storage capacity, busy state, and optional Thunderbolt status.

## Deliberate boundary

- This is not a jar-in-jar copy of the NeoForge 2.0.6 Thunderbolt dependency.
  A NeoForge 1.21.1 Thunderbolt artifact is not a Forge 1.20.1 dependency, so
  bundling it would not port its runtime and could create class-loader or
  loader conflicts.
- `ThunderboltBridge` remains optional class-presence detection only. It does
  not bundle a loader-incompatible NeoForge Thunderbolt JAR, and it does not
  pretend to provide Thunderbolt time-wheel or maintenance services when the
  compatible Forge API is absent.
- The Forge port's closed-loop executor is self-contained and does not require
  Thunderbolt. A real Forge 1.20.1 Thunderbolt API can be connected behind this
  boundary without changing the AE2 pattern item or CPU contract.
- NeoForge-only features such as Thunderbolt time-wheel scheduling, seed-cell
  maintenance, auto-build, and client preview are not represented as fake
  implementations in this Forge port.

## Verification

`./gradlew.bat compileJava --no-daemon` passes on Java 17 with Forge 47.4.20
and AE2 15.4.10 after the AE2 runtime integration changes. Minecraft
client/server startup was not run in this change.
