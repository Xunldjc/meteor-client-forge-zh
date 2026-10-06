import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import com.google.gson.JsonParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public final class ForgeDistributionSmoke {
    public static void main(String[] args) throws Exception {
        try (JarFile jar = new JarFile(args[0])) {
            assert jar.getEntry("META-INF/mods.toml") != null;
            assert jar.getEntry("META-INF/accesstransformer.cfg") != null;
            assert jar.getEntry("META-INF/jarjar/metadata.json") != null;
            assert jar.getEntry("fabric.mod.json") == null;
            checkBaritone(jar);
            ClassNode beacon = read(jar, "mixin/BeaconScreenMixin");
            assert beacon.methods.stream().anyMatch(method -> method.name.equals("m_169616_")
                && method.desc.equals("(Lnet/minecraft/client/gui/components/AbstractWidget;)V"));
            assert beacon.methods.stream().noneMatch(method -> method.name.equals("addButton"));
            assert beacon.superName.equals("net/minecraft/client/gui/screens/inventory/AbstractContainerScreen");
            ClassNode renderer = read(jar, "mixin/BlockModelRendererMixin");
            var hook = renderer.methods.stream().filter(method -> method.name.equals("onRender")).findFirst().orElseThrow();
            assert hook.desc.contains("Lnet/minecraftforge/client/model/data/ModelData;");
            assert hook.desc.contains("Lnet/minecraft/client/renderer/RenderType;");
            System.out.println("FORGE_DISTRIBUTION_PASS metadata accessTransformer nestedDependency beaconShadowSRG forgeRendererDescriptor");
        }
    }

    private static void checkBaritone(JarFile jar) throws Exception {
        var metadata = JsonParser.parseString(new String(jar.getInputStream(jar.getJarEntry("META-INF/jarjar/metadata.json")).readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        int copies = 0;
        for (var element : metadata.getAsJsonArray("jars")) {
            var nested = element.getAsJsonObject();
            if (!nested.getAsJsonObject("identifier").get("artifact").getAsString().equals("baritone-unoptimized-forge")) continue;
            copies++;
            assert nested.getAsJsonObject("version").get("artifactVersion").getAsString().equals("1.10.1");
            String path = nested.get("path").getAsString();
            byte[] bytes = jar.getInputStream(jar.getJarEntry(path)).readAllBytes();
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            assert hash.equals("2fa972b94e25758cbbf797a12ba7217137cc5ac0233950835d0f640bdc8dbce6") : hash;
            boolean mod = false, mixins = false, provider = false;
            try (var input = new JarInputStream(new ByteArrayInputStream(bytes))) {
                var manifest = input.getManifest().getMainAttributes();
                assert manifest.getValue("MixinConfigs").equals("mixins.baritone.json");
                assert manifest.getValue("MixinConnector").equals("baritone.launch.BaritoneMixinConnector");
                for (var entry = input.getNextJarEntry(); entry != null; entry = input.getNextJarEntry()) {
                    if (entry.getName().equals("META-INF/mods.toml")) {
                        String toml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                        mod = toml.contains("modId=\"baritoe\"") && toml.contains("version=\"1.10.1\"");
                    }
                    mixins |= entry.getName().equals("mixins.baritone.json");
                    provider |= entry.getName().equals("baritone/BaritoneProvider.class");
                }
            }
            assert mod && mixins && provider : "Baritone loader, mixins or provider missing";
            System.out.println("BARITONE_EMBEDDED_PASS version=1.10.1 sha256=" + hash + " originalJarBytes=true loader=true mixins=true provider=true");
        }
        assert copies == 1 : "Expected exactly one nested Baritone: " + copies;
        assert jar.getEntry("baritone/api/BaritoneAPI.class") == null : "Do not flatten Baritone classes";
    }

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode node = new ClassNode();
        try (var input = jar.getInputStream(jar.getJarEntry("meteordevelopment/meteorclient/" + name + ".class"))) {
            new ClassReader(input).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        }
        return node;
    }
}
