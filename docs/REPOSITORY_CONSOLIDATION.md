# Forge 1.20.1 repository consolidation

## Canonical location

The unofficial Forge 1.20.1 port has one canonical project location:

```text
Repository   https://github.com/syarukasu/AE2-Lightning-Tech
Branch       forge-1.20.1-port
Issues       https://github.com/syarukasu/AE2-Lightning-Tech/issues
Releases     https://github.com/syarukasu/AE2-Lightning-Tech/releases
```

The repository `syarukasu/AE2-Lightning-Tech-Forge-1.20.1` duplicated the same
port source and created ambiguity over which issue tracker, release page, and
metadata were authoritative. It is now a redirect-only historical repository
and must not be used for new work.

## What is preserved

Consolidation does not delete either repository's Git history. The following
changes from the duplicate repository are intentionally carried into the
canonical branch:

- exact Minecraft, Forge, and AE2 dependency ranges;
- SPDX source-license metadata;
- removal of obsolete LaunchWrapper Mixin manifest attributes;
- separate slim development and distributable Jar-in-Jar artifacts;
- correct `reobfJarJar` use for the final distribution;
- CurseForge description and submission guidance;
- explicit source, issue, release, license, credit, and port-maintainer data.

The canonical repository also retains all later r4-r7 fixes and releases,
including Forge runtime compatibility, recipe-network field ordering, and the
complete Japanese localization.

## Contributor rules

- Open Forge 1.20.1 issues only in the canonical repository.
- Create Forge work from `forge-1.20.1-port`.
- Publish Forge releases only from the canonical repository.
- Do not reopen development in the duplicate repository.
- Do not rewrite or delete historical commits merely to make both repositories
  look identical.
- Links embedded in `mods.toml`, README files, release notes, and distribution
  metadata must point to the canonical project.

## Duplicate repository retirement

The duplicate repository should contain only:

1. a README redirecting users here;
2. an Issue configuration that disables blank issues and links here;
3. no active release workflow or development branches intended for use.

After the redirect pull request is merged, archive the duplicate repository in
GitHub under:

```text
Settings
  -> General
  -> Danger Zone
  -> Archive this repository
```

Archiving is intentionally a manual repository-owner action. The historical
repository remains readable, while new issues, pull requests, and pushes are
prevented.

## Tracking

- Repository consolidation: Issue #11
- AE2LT 2.0.6 Forge backport: Epic #21
