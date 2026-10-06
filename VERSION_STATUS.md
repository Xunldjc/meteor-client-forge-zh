# Minecraft 1.20.1 / Forge 47.4.10 / Java 17

The latest Release embeds the original Baritone Forge 1.10.1 JAR through Forge
JarJar and retains the verified duplicate-entity rendering fix. Earlier Releases
have been replaced; their tags and Git history are retained.

The production JAR is compiled against Forge 47.2.0. The embedded build passes
real chat mining and Shader/world tests with standalone Forge 47.4.10, the
same-version external Baritone duplicate, a Farsight/Cupboard subset, and the
full ATM9 pack on Forge 47.4.0 (435 top-level mod JARs, no external Baritone).
Each test sends `#mine minecraft:stone` in survival, removes three stone blocks,
collects three cobblestone, then verifies `#stop` and restores fixture state.
The optional Farsight adapter snapshots its actual loaded-chunk map for Baritone
pathfinding. The source retains Chinese UI, pinyin, distant-entity Shader drawing
and ATM9 FOV/payload/culling compatibility changes. The earlier rendering build
was also tested in Zombie Invade 100 Days v2.3; that result is historical.

SHA256: `9998c37ebcbb2648d45812a20e192fa97509fa89fcf7adc7926c22c2a9dd3ea2`

Replace the previous Meteor JAR and remove external Baritone. Install only the
single Meteor JAR; its nested Baritone entrypoint, Mixins and API load through
Forge JarJar without modifying the original Baritone binary.
Build with JDK 17 using `gradlew.bat build` or `./gradlew build`.
The default build property remains 47.2.0 to match the delivered binary inputs.
Other Minecraft ports are paused. See VERIFICATION_PUBLIC.md for observed tests.
