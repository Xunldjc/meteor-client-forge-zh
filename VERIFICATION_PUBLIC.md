# Observed Verification Summary

Meteor Client 0.5.4 Forge port with embedded Baritone 1.10.1, Minecraft 1.20.1, Java 17.

## Delivered Binary
- meteor-client-0.5.4-mc1.20.1-embedded-baritone-zh.jar
- SHA256: 9998c37ebcbb2648d45812a20e192fa97509fa89fcf7adc7926c22c2a9dd3ea2
- Build target: Forge 47.2.0. Standalone/Farsight/external-duplicate runtime: Forge 47.4.10. ATM9 runtime: Forge 47.4.0. JDK 17.0.17.
- Translation, pinyin, text wrapping, ASM/FOV and distribution checks pass; build exits 0.
- The original Baritone Forge 1.10.1 JAR is nested through Forge JarJar without flattening or byte changes.
- Nested and original Baritone SHA256: 2fa972b94e25758cbbf797a12ba7217137cc5ac0233950835d0f640bdc8dbce6
- Installer mods/ contains only one Meteor JAR. Separate Baritone installation is not required.

## Real Chat Mining And Compatibility
Tests send literal #mine minecraft:stone through the player network chat method. The survival fixture supplies a diamond pickaxe and three stone blocks. It asserts chat interception, mine activation, actual movement/attack input, server-side removal and three collected cobblestone. #stop must stop mine/path/input before fixture state restoration.
Farsight replaces vanilla chunk storage with an independent map. The original Baritone worker-copy method copied its empty inherited vanilla array. The optional Farsight adapter now copies the actual loaded map into an immutable snapshot. Standalone chunk behavior remains unchanged. The first two full-pack attempts reproduced the empty-snapshot pathfinding failure; final tests below pass.

### Standalone Forge, one Meteor JAR
Process exit: 0. Literal observed results:
```text
BARITONE_CHUNK_SNAPSHOT_PASS provider=net.minecraft.client.multiplayer.ClientChunkCache snapshot=net.minecraft.client.multiplayer.ClientChunkCache loadedChunks=313 floor=true targets=3
BARITONE_MINE_PASS command="#mine minecraft:stone" removed=3 collectedCobblestone=3 movement=6.096179279421294 pathing=true attackInput=true mode=SURVIVAL
BARITONE_STOP_PASS command="#stop" mineActive=false pathing=false forcedInput=false
REGRESSION_FOV_PASS normal=true panorama=true eventListenerRemoved=true panoramaStateRestored=true
REGRESSION_CULLING_PASS installed=false
REGRESSION_SEARCH module=true setting=true block=true item=true entity=true language=zh_cn
REGRESSION_MASK name=RegressionNear distance=18.75165058412756 vanillaDistance=true pixels=1235 rect=766.7916870117188,366.1305847167969,799.6595458984375,407.45843505859375
REGRESSION_MASK name=RegressionFar distance=74.44880388313308 vanillaDistance=false pixels=54 rect=580.932373046875,361.5192565917969,587.861572265625,371.32421875
REGRESSION_MASK name=RegressionSmall distance=60.552658072368814 vanillaDistance=false pixels=50 rect=704.2135009765625,366.1236877441406,712.9542236328125,373.98651123046875
REGRESSION_RESULT requireFix=true maskPass=true moduleStateRestored=true
```

### Same-version external Baritone present
Process exit: 0. Literal observed results:
```text
BARITONE_CHUNK_SNAPSHOT_PASS provider=net.minecraft.client.multiplayer.ClientChunkCache snapshot=net.minecraft.client.multiplayer.ClientChunkCache loadedChunks=313 floor=true targets=3
BARITONE_MINE_PASS command="#mine minecraft:stone" removed=3 collectedCobblestone=3 movement=6.180613133282819 pathing=true attackInput=true mode=SURVIVAL
BARITONE_STOP_PASS command="#stop" mineActive=false pathing=false forcedInput=false
REGRESSION_FOV_PASS normal=true panorama=true eventListenerRemoved=true panoramaStateRestored=true
REGRESSION_CULLING_PASS installed=false
REGRESSION_SEARCH module=true setting=true block=true item=true entity=true language=zh_cn
REGRESSION_MASK name=RegressionNear distance=18.751650584127564 vanillaDistance=true pixels=1157 rect=766.7916870117188,366.1305847167969,799.6595458984375,407.45843505859375
REGRESSION_MASK name=RegressionFar distance=74.44880388313308 vanillaDistance=false pixels=64 rect=580.932373046875,361.5192565917969,587.861572265625,371.32421875
REGRESSION_MASK name=RegressionSmall distance=60.55265807236881 vanillaDistance=false pixels=61 rect=704.2135009765625,366.1236877441406,712.9542236328125,373.98651123046875
REGRESSION_RESULT requireFix=true maskPass=true moduleStateRestored=true
```

### Farsight and Cupboard subset
Process exit: 0. Literal observed results:
```text
BARITONE_CHUNK_SNAPSHOT_PASS provider=com.farsight.FarsightClientChunkManager snapshot=meteordevelopment.meteorclient.forge.ForgeBaritoneChunkSnapshot loadedChunks=313 floor=true targets=3
BARITONE_MINE_PASS command="#mine minecraft:stone" removed=3 collectedCobblestone=3 movement=6.096223888576119 pathing=true attackInput=true mode=SURVIVAL
BARITONE_STOP_PASS command="#stop" mineActive=false pathing=false forcedInput=false
REGRESSION_FOV_PASS normal=true panorama=true eventListenerRemoved=true panoramaStateRestored=true
REGRESSION_CULLING_PASS installed=false
REGRESSION_SEARCH module=true setting=true block=true item=true entity=true language=zh_cn
REGRESSION_MASK name=RegressionNear distance=18.75165058412756 vanillaDistance=true pixels=1235 rect=766.7916870117188,366.1305847167969,799.6595458984375,407.45843505859375
REGRESSION_MASK name=RegressionFar distance=74.44880388313308 vanillaDistance=false pixels=64 rect=580.932373046875,361.5192565917969,587.861572265625,371.32421875
REGRESSION_MASK name=RegressionSmall distance=60.55265807236881 vanillaDistance=false pixels=49 rect=704.2135009765625,366.1236877441406,712.9542236328125,373.98651123046875
REGRESSION_RESULT requireFix=true maskPass=true moduleStateRestored=true
```

### ATM9, 435 top-level mod JARs, zero external Baritone
Process exit: 0. Literal observed results:
```text
BARITONE_CHUNK_SNAPSHOT_PASS provider=com.farsight.FarsightClientChunkManager snapshot=meteordevelopment.meteorclient.forge.ForgeBaritoneChunkSnapshot loadedChunks=313 floor=true targets=3
BARITONE_MINE_PASS command="#mine minecraft:stone" removed=3 collectedCobblestone=3 movement=6.166408978690531 pathing=true attackInput=true mode=SURVIVAL
BARITONE_STOP_PASS command="#stop" mineActive=false pathing=false forcedInput=false
REGRESSION_FOV_PASS normal=true panorama=true eventListenerRemoved=true panoramaStateRestored=true
REGRESSION_CULLING_PASS installed=true normalForced=false shaderForced=true restored=false cullingStatePreserved=true
REGRESSION_SEARCH module=true setting=true block=true item=true entity=true language=zh_cn
REGRESSION_MASK name=RegressionNear distance=18.75165058412756 vanillaDistance=true pixels=1157 rect=766.7916870117188,366.1305847167969,799.6595458984375,407.45843505859375
REGRESSION_MASK name=RegressionFar distance=74.44880388313308 vanillaDistance=false pixels=54 rect=580.932373046875,361.5192565917969,587.861572265625,371.32421875
REGRESSION_MASK name=RegressionSmall distance=60.55265807236881 vanillaDistance=false pixels=60 rect=704.2135009765625,366.1236877441406,712.9542236328125,373.98651123046875
REGRESSION_RESULT requireFix=true maskPass=true moduleStateRestored=true
```

## Baseline, Rollback And Publication
Baseline SHA256: 0a7abb89e49680dd2a40f1d1cc12f0234cc2931557fc2cf277c6e22b986e10eb. Baseline without external Baritone reaches the expected missing baritoe dependency screen; the test closes that error screen after 60 seconds (exit -1, timeout true).
A rollback script restores the original hash on a separate copy, exit 0. Restored distribution requires external Baritone again. The restored JAR passes original world/Shader regressions with external Baritone, exit 0. The delivered embedded JAR remains modified.
Existing Chinese/pinyin and independent Shader outline buffers are preserved. Distant cow and small pig masks remain nonzero beyond vanilla model distance limits. Full-pack payload/FOV/EntityCulling checks pass. Original modpacks and existing saves were not edited; all tests used isolated instances and disposable worlds.
These results do not cover every module, multiplayer, long sessions, custom entity renderers or every external shaderpack. GitHub Actions compile checks are separate from these real graphical-client tests.
Source ZIP contains exact tested code/resources/build inputs. Git normalization may change text line endings only. Installer includes the corresponding Meteor/Baritone/pinyin4j sources and notices. See SHA256SUMS.txt for download hashes.
