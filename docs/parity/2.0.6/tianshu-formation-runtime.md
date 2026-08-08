# Tianshu formation runtime status

This document records the Forge 1.20.1 formation portion of the AE2LT 2.0.6
Tianshu Supercomputing Array port.

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

## Explicit boundary

This step does not claim the complete Tianshu feature. The following are still
separate work:

- AE2 grid-node and crafting CPU registration.
- Pattern and seed inventories, menus, and terminal screens.
- Closed-loop pattern upload, execution, seed refill, and maintenance jobs.
- Tianshu controller GUI, networking, auto-build, and client preview.
- Thunderbolt integration and 2.0.6 NeoForge-only payload/data-component code.

The blocks are therefore usable for formation review and structure testing, but
the formed controller is not advertised as a functioning AE2 crafting CPU.

## Verification

`.\gradlew.bat clean build --no-daemon` passes on Java 17 with Forge 47.4.20
and AE2 15.4.10. Minecraft client/server startup was not run in this change.
