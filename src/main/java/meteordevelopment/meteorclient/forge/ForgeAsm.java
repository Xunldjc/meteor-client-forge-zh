package meteordevelopment.meteorclient.forge;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public final class ForgeAsm {
    private ForgeAsm() {}

    public static void transformFov(ClassNode target) {
        MethodNode method = null;
        for (MethodNode candidate : target.methods) {
            // Mod accessors can share vanilla's descriptor; select its mapped identity.
            boolean named = candidate.name.equals("getFov") && candidate.desc.equals("(Lnet/minecraft/client/render/Camera;FZ)D");
            boolean srg = candidate.name.equals("m_109141_") && candidate.desc.equals("(Lnet/minecraft/client/Camera;FZ)D");
            if ((named || srg) && (candidate.access & Opcodes.ACC_STATIC) == 0) {
                if (method != null) throw new IllegalStateException("Ambiguous getFov target in " + target.name);
                method = candidate;
            }
        }
        if (method == null) throw new IllegalStateException("getFov not found in " + target.name);

        List<AbstractInsnNode> anchors = new ArrayList<>(2);
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (insn instanceof LdcInsnNode ldc && Double.valueOf(90).equals(ldc.cst)) {
                anchors.add(insn);
            } else if (insn instanceof MethodInsnNode call && call.owner.equals("java/lang/Integer")
                && call.name.equals("intValue") && call.desc.equals("()I")) {
                AbstractInsnNode conversion = insn.getNext();
                while (conversion != null && conversion.getOpcode() < 0) conversion = conversion.getNext();
                if (conversion != null && conversion.getOpcode() == Opcodes.I2D) anchors.add(conversion);
            }
        }
        if (anchors.size() != 2) throw new IllegalStateException("Expected two FOV hooks, found " + anchors.size());
        for (AbstractInsnNode anchor : anchors) method.instructions.insert(anchor, fovEvent());
    }

    private static InsnList fovEvent() {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "meteordevelopment/meteorclient/events/render/GetFovEvent", "get", "(D)Lmeteordevelopment/meteorclient/events/render/GetFovEvent;", false));
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "meteordevelopment/meteorclient/MeteorClient", "EVENT_BUS", "Lmeteordevelopment/orbit/IEventBus;"));
        list.add(new InsnNode(Opcodes.SWAP));
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meteordevelopment/orbit/IEventBus", "post", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        list.add(new TypeInsnNode(Opcodes.CHECKCAST, "meteordevelopment/meteorclient/events/render/GetFovEvent"));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "meteordevelopment/meteorclient/events/render/GetFovEvent", "fov", "D"));
        return list;
    }

    public static void transformInflater(ClassNode target) {
        for (MethodNode method : target.methods) {
            if (!method.desc.equals("(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V")) continue;
            int exceptions = 0;
            LabelNode skip = new LabelNode();
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn instanceof TypeInsnNode type && type.getOpcode() == Opcodes.NEW && type.desc.equals("io/netty/handler/codec/DecoderException")) {
                    if (++exceptions == 2) {
                        InsnList guard = new InsnList();
                        guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "meteordevelopment/meteorclient/systems/modules/Modules", "get", "()Lmeteordevelopment/meteorclient/systems/modules/Modules;", false));
                        guard.add(new LdcInsnNode(Type.getObjectType("meteordevelopment/meteorclient/systems/modules/misc/AntiPacketKick")));
                        guard.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "meteordevelopment/meteorclient/systems/modules/Modules", "isActive", "(Ljava/lang/Class;)Z", false));
                        guard.add(new JumpInsnNode(Opcodes.IFNE, skip));
                        method.instructions.insertBefore(insn, guard);
                    }
                } else if (exceptions == 2 && insn.getOpcode() == Opcodes.ATHROW) {
                    method.instructions.insert(insn, skip);
                    return;
                }
            }
        }
        throw new IllegalStateException("PacketInflater guard injection failed in " + target.name);
    }
}
