# Meteor Client 1.20.1 Forge zh_CN

Unofficial native Forge port, based on upstream v0.5.4:
`6d76982a87ed175642d4f2ba2703179ab816cde3`.

## Build

Requires JDK 17. Run `gradlew.bat build` on Windows or `./gradlew build`
on Linux. Target: Minecraft 1.20.1, Forge 47.2.0. Yarn remains a build-time
mapping dependency; the distribution is remapped to Forge SRG names.
This is not a Connector wrapper and does not require Fabric Loader.

Install the remapped `meteor-client-0.5.4-forge-zh.jar` and the bundled
`libs/baritone-unoptimized-forge-1.10.1.jar` together in a client-only
Forge 1.20.1 instance. Do not use the `-all.jar` development artifact.
The Baritone release is from cabaletta/baritone v1.10.1 and is distributed
unmodified. Do not install the old Fabric Baritone or duplicate Baritone jars.

## Changes

- Native `@Mod` entrypoint: `MeteorForge`, mod ID `meteor_client`.
- Retains the original `meteor-client` configuration directory and IDs.
- Replaces Fabric metadata, game path and addon entrypoint discovery.
- Replaces Knot classloader reflection with Mixin post-apply ASM hooks.
- Converts the access widener to Forge access transformers during remapping.
- Handles Forge's entity bucket supplier and mining lambda changes.
- Chinese names and descriptions for all 168 registered built-in modules
  (169 translation pairs in the resource, including the unregistered Sneak module).
- Common settings, categories, tabs, buttons and labels translated.
- Uses Minecraft's font for Chinese glyphs. English fallback is available
  with the JVM option `-Dmeteor.locale=en_us`.
- Button and label getters retain their original values for state logic.
- All existing GUI search/filter entrypoints accept Chinese full pinyin and
  initials: modules/settings, single block/item pickers, registry list pickers,
  entity types, block data, effects, fonts, songs and HUD searches. Registry IDs
  and original module/setting names remain searchable. The pinyin4j dependency
  is shaded into the distribution; no additional user-installed JAR is needed.
- Shader ESP has an independent loaded-entity pass before outline buffer flush,
  rather than relying on the vanilla model distance cutoff. Selected types,
  configured view distance, self filtering, NoRender and camera frustum remain
  respected; entities not sent by the server cannot be drawn.

## Verification

`gradlew build` runs the Chinese/English translation smoke tests and ASM
stack verification, Unicode wrapping tests and distribution metadata/SRG checks.
`gradlew runClient -PsmokeTest` opens the module GUI and a module settings screen,
renders at least 20 frames each and requests a normal client exit after logging
`METEOR_FORGE_SMOKE_PASS`. This mode is off by default.
`-PworldSmoke` additionally creates a disposable creative superflat world,
moves for 40 player ticks, toggles/restores Fullbright and captures the world.
The same smoke test passed in a real Forge 47.2.0 installation with the
remapped distribution JAR, not just the development launcher: 168 modules,
Chinese GUI/settings, Mixin audit, world entry, movement and module state
restoration, followed by a normal exit (0). English fallback GUI was also
checked separately; see `VERIFICATION.txt` next to the source archive for
the final observed run labels, literal outputs and hashes.
A successful build alone is not evidence that startup or every module works.

The 2026-10-06 search/ESP revision includes an opt-in regression fixture
(`-Dmeteor.smoke.regression=true` with smoke/world enabled). It checks real GUI
filters and model-mask pixels at controlled entity distances. See the updated
ledger for baseline versus modified results. This is Meteor ESP's Shader mode,
not a claim of compatibility with third-party shader packs such as Oculus.

## Limitations

This is an experimental port. Individual module behavior beyond the observed
smoke test, long sessions, multiplayer, non-default renderer combinations and
native addon loading require separate testing. The smoke test does not prove
Fullbright's visual effect or correctness of every feature. Fabric addons
are not binary-compatible.
Native addons can implement MeteorAddon and declare a Java ServiceLoader
provider, with their own name and authors initialized.

Fabric-only Indigo, Indium, Sodium, Lithium and Canvas integrations are
excluded. Do not assume Embeddium/Oculus compatibility from a successful
vanilla-renderer test. Many setting descriptions, chat messages and dynamic
labels still fall back to English. This is not a complete localization.
Forge's experimental light pipeline is not verified; use the default pipeline.

## License

Meteor source remains GPL-3.0. The complete modified source and binary patch
are provided alongside the build. Retain upstream copyright and license files.
