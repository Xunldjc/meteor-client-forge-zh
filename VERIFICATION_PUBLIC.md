# Observed Verification Summary

Unofficial Meteor Client 0.5.4 native Forge port. This is not an upstream release.
Only Minecraft 1.20.1 is delivered. All statements below describe observed local
tests; GitHub Actions build results are separate and do not prove game startup.

## Forge 47.2.0

- JDK 17 `gradlew.bat build --no-daemon --console=plain`: exit 0.
- Chinese/English translations, Unicode wrapping, pinyin, ASM stack and
  distribution metadata/SRG smoke checks pass.
- Real Forge client, required Mixin audit, 168 modules, Chinese GUI/settings,
  creative flat singleplayer world and movement checks: normal Java exit 0.
- Module, shared setting matcher, real block/item/entity selector pinyin checks
  pass. The setting check is not a full settings GUI regression.
- At the controlled 0.5 entity-distance scale, the loaded cow at ~74.19 blocks
  and pig at ~60.30 blocks fail the vanilla model distance test but have valid
  Shader ESP mask pixels after the fix (59 / 50 in the delivered-JAR run).
- Pre-fix baseline and pre-fix rollback: pinyin absent, both distant masks empty.

Delivered JAR SHA256:
`0d9a8b9a574aa8057738e882d582d032e1bbb269654d7e37ddbec7370eae86f4`

## Forge 47.4.10

- Same source code and fixture; Forge property changed to 47.4.10.
- Full JDK 17 build: exit 0; all 16 tasks executed; all six smoke checks pass.
- Real Forge 47.4.10 client, required Mixin audit, Chinese GUI/world and all five
  pinyin checks pass; normal Java exit 0.
- Distant cow/pig Shader masks contain 58 / 53 green model pixels while vanilla
  distance acceptance is false. ESP selections, mode and active state restored.
- Source ZIP reconstructed from the base ZIP and supplemental patch; hashes and
  file set match for all 984 source files. Installer entries individually checked.
- Source rollback to the 47.2.0 base was executed on a separate ZIP copy, exact
  SHA256 restored, then rebuilt and rerun with the same fixture; exits 0.
  The restored build retains the pinyin/ESP fixes (distant masks 54 / 53).

Delivered JAR SHA256:
`8af9a973e5cd9168f36b1ef3db331015fdb51f069e63b71e6adc71ed83f6c8e0`

## Remaining Targets And Limits

40 selected Minecraft/Forge targets were validated against official version
catalogs. Only one Minecraft target has a completed port. 1.14.4 dependency
configuration fails (Java 16 Discord IPC versus Java 8 target; old MCP/Yarn
remapping conflicts; upstream Baritone 1.4.6 has no Forge runtime support).
Its pinyin component alone passes real javac 8 / java 8 tests, class version 52.
38 targets, including 26.3 / Forge 66.0.9, are not ported or built.

Warnings are not hidden: the recommended Forge build reports deprecated API and
Beacon @Shadow validation warnings; runtime audit passes. Not verified: every
module's effect, multiplayer, long sessions, user modpacks, custom renderers,
Embeddium/Oculus or third-party shader packs. Localization remains partial.
Unloaded/server-untracked entities cannot be drawn. No tracer code was changed.

Release asset checksums are in each release's SHA256SUMS.txt. Complete modified
sources and patches accompany the release assets. Publication-only README,
workflow and progress documents do not alter the tested Java code/resources.
Private local logs and machine paths are deliberately not published.
