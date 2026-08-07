# CurseForge submission checklist

## Project fields

```text
Project name   AE2 Lightning Tech - Unofficial Forge 1.20.1 Port
Category       Technology
Loader         Forge
Game version   Minecraft 1.20.1
Environment    Client and server
License        GNU Lesser General Public License version 3 only
Source         https://github.com/syarukasu/AE2-Lightning-Tech
Issues         https://github.com/syarukasu/AE2-Lightning-Tech/issues
```

Use the complete contents of `CURSEFORGE_DESCRIPTION.md` as the English project
description.

## File fields

Before each upload:

- [ ] Release type matches the actual verification level.
- [ ] Supported game version is `1.20.1`.
- [ ] Supported loader is `Forge`.
- [ ] Applied Energistics 2 is declared as required.
- [ ] Thunderbolt is declared only after the 2.0.6 port actually requires and
      verifies a Forge 1.20.1 release.
- [ ] Optional integrations are not declared as mandatory dependencies.
- [ ] The changelog matches the exact JAR and source tag.
- [ ] Only the unclassified reobfuscated Jar-in-Jar artifact is uploaded.
- [ ] The `-slim.jar` development artifact is not uploaded.

## Repository consistency

- [ ] README, JAR `mods.toml`, release notes, source, issues, and release links
      all point to `syarukasu/AE2-Lightning-Tech`.
- [ ] No active distribution field points to the retired duplicate repository.
- [ ] The upstream CurseForge/Modrinth pages are described as the original
      NeoForge edition, not as this port's download page.
- [ ] The project is identified as unofficial in the first paragraph.

## Artifact validation

- [ ] Build from a clean checkout with Java 17.
- [ ] Run `./gradlew clean build --no-daemon` or the Windows equivalent.
- [ ] Verify the final JAR contains reobfuscated production classes.
- [ ] Verify the final JAR includes the required Jar-in-Jar dependencies.
- [ ] Verify `META-INF/LICENSE_ae2lt.txt` exists.
- [ ] Verify `META-INF/LICENSE_ASSETS_ae2lt.md` exists.
- [ ] Verify `META-INF/CREDITS_ae2lt.md` exists.
- [ ] Verify `META-INF/PORT_NOTES_ae2lt.md` exists.
- [ ] Verify `TweakClass` and `TweakOrder` are absent from the manifest.
- [ ] Record the final SHA-256.

## Runtime validation

- [ ] Client reaches the title screen.
- [ ] Dedicated Server reaches `Done`.
- [ ] Client joins the Dedicated Server.
- [ ] Recipe synchronization completes without decoder errors.
- [ ] Existing Forge World migration requirements for the release are tested.
- [ ] Every new BlockEntity-backed block can be placed without a null
      `BlockEntityType` crash.
- [ ] Optional integration test results are recorded.

## Localization and data

- [ ] `en_us`, `zh_cn`, and `ja_jp` key coverage reports are attached.
- [ ] Formatting placeholders match across languages.
- [ ] Missing models, textures, recipes, tags, loot tables, and GuideME paths are
      checked automatically where possible.

## Release record

- [ ] Create the source tag in the canonical repository.
- [ ] Attach the exact built JAR to the canonical GitHub Release.
- [ ] Include the SHA-256, dependency versions, known limitations, and migration
      notes in the release body.
- [ ] Do not call a build `2.0.6` until the 2.0.6 feature and release gates in
      Epic #21 are complete.
