import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public final class ForgeDistributionSmoke {
    public static void main(String[] args) throws Exception {
        try (JarFile jar = new JarFile(args[0])) {
            assert jar.getEntry("META-INF/mods.toml") != null;
            assert jar.getEntry("META-INF/accesstransformer.cfg") != null;
            assert jar.getEntry("META-INF/jarjar/metadata.json") != null;
            assert jar.getEntry("fabric.mod.json") == null;
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

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode node = new ClassNode();
        try (var input = jar.getInputStream(jar.getJarEntry("meteordevelopment/meteorclient/" + name + ".class"))) {
            new ClassReader(input).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        }
        return node;
    }
}
