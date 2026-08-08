# Matter Warping Matrix runtime status

This document records the Forge 1.20.1 Matrix runtime added on the Matrix
port branch.

## Implemented in this step

- Registered the Matter Warping Matrix casing, constraint frame, glass,
  controller, port, core, thread, thermal, and pattern-storage blocks.
- Registered controller, port, and pattern-storage block entities.
- Added the exact 7x11x7 template scanner from the 2.0.6 contract.
- Added horizontal orientation transforms and inverse controller lookup.
- Rechecks only candidate controllers near a changed component.
- Defers a scan when any required corner chunk is unavailable; it does not
  force-load chunks.
- Applies and clears the `formed` block state for the scanned members.
- Persists controller bindings on the port and pattern-storage block entities.
- Added block models, item models, blockstates, loot tables, and English/Japanese
  names. The formed casing/frame/glass models use standard Forge cube models;
  the upstream 2.0.6 custom connected-texture loader is not present in this
  Forge baseline and is intentionally not referenced.

## Deliberately not claimed here

- AE2 grid attachment and crafting CPU registration.
- Pattern inventory menus and pattern encoding.
- Energy transfer, processing, or recipe execution.
- Auto-build, client preview, or Thunderbolt integration.

Those require the separate 1.20.1 bridge work described by Issue #23 and must
be added after the formation lifecycle is reviewed in a real Forge world.

## Verification

`.\gradlew.bat clean build --no-daemon` passes on Java 17 with the existing
Forge 47.4.20 and AE2 15.4.10 dependency coordinates. Minecraft client/server
runtime verification has not been performed in this change.
