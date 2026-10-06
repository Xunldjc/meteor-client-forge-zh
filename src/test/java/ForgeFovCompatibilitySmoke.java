import meteordevelopment.meteorclient.forge.ForgeAsm;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.JarFile;

public final class ForgeFovCompatibilitySmoke {
    public static void main(String[] args) throws Exception {
        fixture("getFov", "net/minecraft/client/render/Camera");
        fixture("m_109141_", "net/minecraft/client/Camera");
        rejectedTargets();
        for (String path : args) {
            ClassNode target = new ClassNode();
            if (path.endsWith(".class")) {
                new ClassReader(Files.readAllBytes(Path.of(path))).accept(target, 0);
            } else {
                try (JarFile jar = new JarFile(path)) {
                    var entry = jar.getJarEntry("net/minecraft/client/render/GameRenderer.class");
                    if (entry == null) entry = jar.getJarEntry("net/minecraft/client/renderer/GameRenderer.class");
                    assert entry != null : path;
                    try (var input = jar.getInputStream(entry)) {
                        new ClassReader(input).accept(target, 0);
                    }
                }
            }
            long before = hooks(target);
            ForgeAsm.transformFov(target);
            assert hooks(target) == before + 2 : path;
            MethodNode fov = target.methods.stream().filter(m -> m.name.equals("getFov") || m.name.equals("m_109141_")).findFirst().orElseThrow();
            new Analyzer<>(new BasicVerifier()).analyze(target.name, fov);
            for (MethodNode method : target.methods) {
                if (method.name.equals("invokeGetFov") || method.name.equals("catnip$callGetFov")) {
                    assert hooks(method) == 0 : method.name;
                }
            }
            System.out.println("NATIVE_FOV_PASS target=" + target.name + " method=" + fov.name + " hooks=2 input=" + path);
        }
        System.out.println("FOV_COMPATIBILITY_PASS named srg sameDescriptorInvokers untouchedDecoys debugNodes strictValidation bytecodeStack");
    }

    private static void fixture(String name, String camera) throws Exception {
        ClassNode renderer = new ClassNode();
        renderer.name = "fixture/GameRenderer";
        String descriptor = "(L" + camera + ";FZ)D";
        // Accessors from Oculus and Ponder have the same descriptor as vanilla getFov.
        MethodNode oculus = fov("invokeGetFov", descriptor);
        MethodNode ponder = fov("catnip$callGetFov", descriptor);
        MethodNode overload = fov(name, "(Ljava/lang/Object;FZ)D");
        MethodNode actual = fov(name, descriptor);
        renderer.methods.add(oculus);
        renderer.methods.add(actual);
        renderer.methods.add(ponder);
        renderer.methods.add(overload);
        ForgeAsm.transformFov(renderer);
        assert hooks(actual) == 2;
        assert hooks(oculus) == 0 && hooks(ponder) == 0 && hooks(overload) == 0;
        new Analyzer<>(new BasicVerifier()).analyze(renderer.name, actual);
        System.out.println("FOV_INVOKER_REGRESSION_PASS method=" + name + " hooks=2 invokers=untouched");
    }

    private static MethodNode fov(String name, String descriptor) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, name, descriptor, null, null);
        method.maxLocals = 6;
        method.maxStack = 8;
        method.instructions.add(new InsnNode(Opcodes.ICONST_5));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
        LabelNode label = new LabelNode();
        method.instructions.add(label);
        method.instructions.add(new LineNumberNode(100, label));
        method.instructions.add(new InsnNode(Opcodes.I2D));
        method.instructions.add(new VarInsnNode(Opcodes.DSTORE, 4));
        method.instructions.add(new LdcInsnNode(90.0));
        method.instructions.add(new InsnNode(Opcodes.DRETURN));
        return method;
    }

    private static void rejectedTargets() {
        expectRejected(new ClassNode());
        ClassNode wrongType = new ClassNode();
        wrongType.methods.add(fov("getFov", "(Ljava/lang/Object;FZ)D"));
        expectRejected(wrongType);
        ClassNode wrongBody = new ClassNode();
        MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE, "getFov", "(Lnet/minecraft/client/render/Camera;FZ)D", null, null);
        method.instructions.add(new LdcInsnNode(90.0));
        method.instructions.add(new InsnNode(Opcodes.DRETURN));
        wrongBody.methods.add(method);
        expectRejected(wrongBody);
        ClassNode truncated = new ClassNode();
        MethodNode invalid = new MethodNode(Opcodes.ACC_PRIVATE, "getFov", method.desc, null, null);
        invalid.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
        truncated.methods.add(invalid);
        expectRejected(truncated);
        System.out.println("FOV_REJECTION_PASS noPartialMutation=true");
    }

    private static void expectRejected(ClassNode target) {
        long before = hooks(target);
        try {
            ForgeAsm.transformFov(target);
            throw new AssertionError("Unsupported target was accepted");
        } catch (IllegalStateException expected) {
            assert hooks(target) == before : "Partial mutation before validation";
        }
    }

    private static long hooks(ClassNode target) {
        return target.methods.stream().mapToLong(ForgeFovCompatibilitySmoke::hooks).sum();
    }

    private static long hooks(MethodNode method) {
        return Arrays.stream(method.instructions.toArray()).filter(n -> n instanceof MethodInsnNode call
            && call.owner.equals("meteordevelopment/meteorclient/events/render/GetFovEvent") && call.name.equals("get")).count();
    }
}
