import meteordevelopment.meteorclient.forge.ForgeAsm;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public class ForgeAsmSmoke {
    public static void main(String[] args) throws Exception {
        ClassNode renderer = new ClassNode();
        renderer.name = "fixture/GameRenderer";
        MethodNode fov = new MethodNode(Opcodes.ACC_PUBLIC, "runtimeName", "(Ljava/lang/Object;FZ)D", null, null);
        fov.maxLocals = 6;
        fov.maxStack = 8;
        fov.instructions.add(new InsnNode(Opcodes.ICONST_5));
        fov.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
        fov.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
        fov.instructions.add(new InsnNode(Opcodes.I2D));
        fov.instructions.add(new VarInsnNode(Opcodes.DSTORE, 4));
        fov.instructions.add(new LdcInsnNode(90.0));
        fov.instructions.add(new InsnNode(Opcodes.DRETURN));
        renderer.methods.add(fov);
        ForgeAsm.transformFov(renderer);
        new Analyzer<>(new BasicVerifier()).analyze(renderer.name, fov);
        long hooks = java.util.Arrays.stream(fov.instructions.toArray()).filter(n -> n instanceof MethodInsnNode m && m.name.equals("post")).count();
        assert hooks == 2;
        System.out.println("fov_hooks=2; bytecode_stack=PASS");

        ClassNode inflater = new ClassNode();
        inflater.name = "fixture/Inflater";
        MethodNode decode = new MethodNode(Opcodes.ACC_PUBLIC, "runtimeName", "(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V", null, null);
        decode.maxLocals = 4;
        decode.maxStack = 8;
        for (int i = 0; i < 2; i++) {
            LabelNode next = new LabelNode();
            decode.instructions.add(new InsnNode(Opcodes.ICONST_0));
            decode.instructions.add(new JumpInsnNode(Opcodes.IFEQ, next));
            decode.instructions.add(new TypeInsnNode(Opcodes.NEW, "io/netty/handler/codec/DecoderException"));
            decode.instructions.add(new InsnNode(Opcodes.DUP));
            decode.instructions.add(new LdcInsnNode("fixture"));
            decode.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "io/netty/handler/codec/DecoderException", "<init>", "(Ljava/lang/String;)V", false));
            decode.instructions.add(new InsnNode(Opcodes.ATHROW));
            decode.instructions.add(next);
        }
        decode.instructions.add(new InsnNode(Opcodes.RETURN));
        inflater.methods.add(decode);
        ForgeAsm.transformInflater(inflater);
        new Analyzer<>(new BasicVerifier()).analyze(inflater.name, decode);
        System.out.println("packet_guard=PASS; bytecode_stack=PASS");

        boolean rejected = false;
        try { ForgeAsm.transformFov(new ClassNode()); } catch (IllegalStateException e) { rejected = true; }
        assert rejected;
        System.out.println("missing_target_rejected=PASS");
    }
}
