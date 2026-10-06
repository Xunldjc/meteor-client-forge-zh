package meteordevelopment.meteorclient.forge;

import io.netty.buffer.Unpooled;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AntiPacketKick;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.ModList;

/** Opt-in packet-limit regression; never invoked during ordinary gameplay. */
final class ForgePayloadSmoke {
    private static final int BYTES = 2 * 1024 * 1024;
    private static final ClientPlayPacketListener DISCARD = (ClientPlayPacketListener) java.lang.reflect.Proxy.newProxyInstance(
        ForgePayloadSmoke.class.getClassLoader(), new Class<?>[]{ClientPlayPacketListener.class}, (proxy, method, args) -> null);

    private ForgePayloadSmoke() {}

    static void run() {
        AntiPacketKick module = Modules.get().get(AntiPacketKick.class);
        boolean original = module.isActive();
        boolean external = ModList.get().isLoaded("packetfixer") || ModList.get().isLoaded("connectivity");
        try {
            if (module.isActive()) module.toggle();
            boolean inactiveConstructor = accepts(false);
            boolean inactiveDecoder = accepts(true);
            module.toggle();
            boolean activeConstructor = accepts(false);
            boolean activeDecoder = accepts(true);
            MeteorClient.LOG.info("REGRESSION_PAYLOAD externalLimits={} bytes={} inactiveConstructor={} inactiveDecoder={} activeConstructor={} activeDecoder={}",
                external, BYTES, inactiveConstructor, inactiveDecoder, activeConstructor, activeDecoder);
            if (inactiveConstructor != external || inactiveDecoder != external || !activeConstructor || !activeDecoder)
                throw new IllegalStateException("Custom payload limits did not preserve the installed packet provider");
        } finally {
            if (module.isActive() != original) module.toggle();
        }
        if (module.isActive() != original) throw new IllegalStateException("AntiPacketKick state was not restored");
        MeteorClient.LOG.info("REGRESSION_PAYLOAD_PASS externalLimits={} bytes={} moduleStateRestored=true", external, BYTES);
    }

    private static boolean accepts(boolean decode) {
        PacketByteBuf input = new PacketByteBuf(Unpooled.buffer(BYTES + 128));
        try {
            Identifier channel = new Identifier("meteor_client", "compat_smoke");
            if (decode) input.writeIdentifier(channel);
            input.writeZero(BYTES);
            CustomPayloadS2CPacket packet = decode ? new CustomPayloadS2CPacket(input) : new CustomPayloadS2CPacket(channel, input);
            PacketByteBuf copy = packet.getData();
            try {
                return copy.readableBytes() == BYTES;
            } finally {
                copy.release();
                // Normal packet dispatch releases Forge's decoder-owned buffer.
                packet.apply(DISCARD);
            }
        } catch (IllegalArgumentException expectedLimit) {
            return false;
        } finally {
            input.release();
        }
    }
}
