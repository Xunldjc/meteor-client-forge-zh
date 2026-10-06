# Observed Verification Summary

Meteor Client 0.5.4 native Forge port, Minecraft 1.20.1, Java 17.
The current Release replaces both older public Releases with the tested render fix.
Local runtime tests and GitHub Actions builds are separate checks.

## Delivered Binary

- `meteor-client-0.5.4-mc1.20.1-render-fix-zh.jar`
- SHA256: `0a7abb89e49680dd2a40f1d1cc12f0234cc2931557fc2cf277c6e22b986e10eb`
- Build target: Forge 47.2.0; production modpack runtime: Forge 47.4.10.
- JDK 17 build exit 0. Translation, pinyin, Unicode wrapping, distribution,
  ASM stack and FOV compatibility checks passed.
- Test probe is a separate test-only mod and is absent from the production JAR.

## Duplicate Entity Rendering

Vanilla OutlineVertexConsumerProvider sends normal render layers to both normal
and outline consumers. Meteor's extra mask pass used the shared entity buffers,
but its outline draw flushed only outlines. Pending textured model geometry was
therefore submitted again by later draws under different render state.

PostProcessShader now uses an isolated OutlineOnlyVertexConsumerProvider.
It accepts direct outline layers, resolves outline layers from normal models,
and discards non-outline features without writing ordinary entity buffers.

Real Forge probe, four vertices per render-layer case:

```text
BASELINE: SHADER_BUFFER_RESULT modelLeak=true featureLeak=true directOutlineLeak=false
          expected assertion SHADER_BUFFER_LEAK; process exit -1
MODIFIED: SHADER_BUFFER_ISOLATION_PASS cases=3 normalVertices=0
          process exit 0
ROLLBACK: SHADER_BUFFER_RESULT modelLeak=true featureLeak=true directOutlineLeak=false
          original JAR hash restored on a separate copy; process exit -1
```

JAR and source archive rollback commands both exited 0. Delivered files remain fixed.

## Production Runtime Tests

- Zombie Invade 100 Days v2.3, 145 top-level mod JARs, Forge 47.4.10,
  Java 17.0.17: startup, Chinese GUI/settings, new flat world, movement,
  module state restoration, 2 MB payload and five pinyin checks passed; exit 0.
- Full-pack near cow, distant cow (~74.19 blocks), small pig (~60.30 blocks):
  317 / 17 / 12 mask pixels. Both distant entities fail vanilla distance
  acceptance but retain their Meteor outline. Screenshot has no floating copies.
- Standalone Forge 47.4.10: full Mixin audit, strict normal/panorama FOV return,
  GUI/world/search/payload checks and 319 / 17 / 12 mask pixels; exit 0.
- EntityCulling-only Forge 47.2.0 subset: ordinary culling remains unchanged,
  Meteor Shader pass is forced-visible only within that pass, state restored;
  GUI/world/search and all three masks passed; exit 0.
- Existing ATM9 canonical-FOV selector and external payload-provider integration
  are preserved. Build regression also checks the captured ATM9 GameRenderer.

The full-pack FOV probe allows another mod to override the final normal-FOV
return while requiring one callback, finite output and restored state. Standalone
checks remain strict. Full-pack forced global Mixin audit is disabled for absent
optional targets; required injections remain enabled, and standalone audit runs.

Oculus was present with `enableShaders=false`. This verifies Meteor Shader ESP,
not all external shaderpacks, custom entity renderers, modules or multiplayer.
Tests used isolated directories and disposable worlds, not existing saves.

## Source And Publication

The source ZIP is the exact build-input archive. The Git tag has the same code,
resources and build inputs; Git normalizes text line endings. Publication README
and progress documents are updated separately. The existing two-target build
workflow is retained. No personal paths, authentication data or raw local logs
are included in the public assets.

Meteor remains GPL-3.0. The installer includes the modified source archive and
corresponding Baritone/pinyin4j sources and notices. See SHA256SUMS.txt for all
download hashes. Other Minecraft-version ports remain paused.
