# Minecraft 1.20.1 / Forge 47.4.10

This is the selected recommended Forge target in the 1.14.4-26.3 matrix.
The base source is the existing Meteor 0.5.4 native Forge port with Chinese
GUI, pinyin search and the independent loaded-entity Shader ESP pass.
The only executable-source change from the verified Forge 47.2.0 source is
`gradle.properties: forge_version=47.4.10`. This document supersedes the
Forge 47.2.0 target reference in the base port's historical documentation.

## Observed Checks

- `gradlew.bat build --no-daemon --console=plain`: exit 0; 16 tasks executed.
- Chinese/English translation, pinyin, wrapping, ASM and distribution tests pass.
- Real Forge 47.4.10 client with the remapped distribution JAR: Java exit 0.
- 168 registered modules; Chinese GUI/settings and required Mixin audit pass.
- Five regression search checks pass; the setting check is a shared matcher
  check, while the block/item/entity selectors exercise actual filter widgets.
- Loaded cow at ~74.19 blocks: 58 green model-mask pixels; loaded pig at
  ~60.30 blocks: 53 pixels; both fail the vanilla model distance test.
- `REGRESSION_RESULT requireFix=true maskPass=true moduleStateRestored=true`.

## Install And Build

Use Minecraft 1.20.1, Forge 47.4.10 and Java 17. Replace the old Meteor JAR
with this version; keep one copy of `baritone-unoptimized-forge-1.10.1.jar`.
Never install two Meteor JARs together. This is a client-only mod.
Use `gradlew.bat build` or `./gradlew build` with JDK 17.

Pinyin uses the displayed registry names: select Chinese game language for
Chinese block/item/entity pinyin. Existing partial-localization limitations,
unverified custom renderers, shader packs, addons and multiplayer remain.

## Multi-Version Status

This archive is ONLY a 1.20.1 build. It is NOT a universal 1.14.4-26.3 JAR.
The other 39 selected targets are not delivered as completed ports.
See the sibling build matrix and the main VERIFICATION.txt for actual statuses.
Meteor is GPL-3.0; retain the base source notices and pinyin4j corresponding source.
