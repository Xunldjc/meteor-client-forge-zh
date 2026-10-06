package meteor.shadertest;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;

/** Separate test mod; this class is never packaged in the production JAR. */
@Mod("meteor_shader_probe")
public final class ForgeShaderBufferProbe {
    private boolean ran;

    public ForgeShaderBufferProbe() {
        MinecraftForge.EVENT_BUS.addListener(this::tick);
    }

    private void tick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("meteor.shaderProbe") || event.phase != TickEvent.Phase.END || ran
            || MeteorClient.mc == null || MeteorClient.mc.getOverlay() != null
            || PostProcessShaders.ENTITY_OUTLINE == null || MeteorClient.mc.currentScreen == null) return;
        ran = true;
        Identifier texture = new Identifier("minecraft", "textures/entity/cow/cow.png");
        boolean modelLeak = emitsToParent("entity_model", RenderLayer.getEntityCutoutNoCull(texture));
        boolean featureLeak = emitsToParent("non_outline_feature", RenderLayer.getLightning());
        boolean directLeak = emitsToParent("direct_outline", RenderLayer.getOutline(texture));
        MeteorClient.LOG.info("SHADER_BUFFER_RESULT modelLeak={} featureLeak={} directOutlineLeak={}", modelLeak, featureLeak, directLeak);
        if (modelLeak || featureLeak || directLeak) throw new IllegalStateException("SHADER_BUFFER_LEAK: mask pass writes textured geometry to normal entity buffers");
        MeteorClient.LOG.info("SHADER_BUFFER_ISOLATION_PASS cases=3 normalVertices=0");
        MeteorClient.mc.scheduleStop();
    }

    private static boolean emitsToParent(String name, RenderLayer layer) {
        BufferBuilder parentBuffer = new BufferBuilder(256);
        VertexConsumerProvider.Immediate parent = VertexConsumerProvider.immediate(parentBuffer);
        OutlineVertexConsumerProvider provider;
        try {
            provider = PostProcessShaders.ENTITY_OUTLINE.vertexConsumerProvider.getClass()
                .getConstructor(VertexConsumerProvider.Immediate.class).newInstance(parent);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Shader provider probe constructor missing", e);
        }
        provider.setColor(0, 255, 0, 255);
        VertexConsumer consumer = provider.getBuffer(layer);
        for (int i = 0; i < 4; i++) {
            consumer.vertex(i & 1, (i >>> 1) & 1, 0).color(255, 255, 255, 255)
                .texture(0, 0).overlay(0).light(15728880).normal(0, 0, 1).next();
        }
        boolean leak = parentBuffer.isBuilding() && !parentBuffer.isBatchEmpty();
        MeteorClient.LOG.info("SHADER_BUFFER_CASE name={} parentBuilding={} parentNonempty={} provider={}", name,
            parentBuffer.isBuilding(), leak, provider.getClass().getSimpleName());
        // Probe buffers never submit their normal geometry to a framebuffer.
        if (parentBuffer.isBuilding()) {
            var built = parentBuffer.endNullable();
            if (built != null) built.release();
        }
        return leak;
    }
}
