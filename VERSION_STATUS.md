# Minecraft 1.20.1 / Forge 47.4.10 / Java 17

The latest Release is the verified duplicate-entity rendering fix. Both earlier
Releases have been replaced; their tags and Git history are retained.

The production JAR is compiled against Forge 47.2.0 and tested with Forge 47.4.10
in Zombie Invade 100 Days v2.3, standalone Forge and an EntityCulling subset.
The source retains Chinese UI, pinyin, distant-entity Shader drawing and ATM9
FOV/payload/culling compatibility changes.

SHA256: `0a7abb89e49680dd2a40f1d1cc12f0234cc2931557fc2cf277c6e22b986e10eb`

Replace the previous Meteor JAR and retain one Baritone Forge 1.10.1 JAR.
Build with JDK 17 using `gradlew.bat build` or `./gradlew build`.
The default build property remains 47.2.0 to match the delivered binary inputs.
Other Minecraft ports are paused. See VERIFICATION_PUBLIC.md for observed tests.
