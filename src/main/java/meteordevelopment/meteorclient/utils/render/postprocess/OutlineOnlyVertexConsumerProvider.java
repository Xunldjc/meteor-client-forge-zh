package meteordevelopment.meteorclient.utils.render.postprocess;

import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

/** Shader masks must never enqueue another textured model in the world buffers. */
public final class OutlineOnlyVertexConsumerProvider extends OutlineVertexConsumerProvider {
    private static final VertexConsumer DISCARD = new VertexConsumer() {
        @Override
        public VertexConsumer vertex(double x, double y, double z) { return this; }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) { return this; }

        @Override
        public VertexConsumer texture(float u, float v) { return this; }

        @Override
        public VertexConsumer overlay(int u, int v) { return this; }

        @Override
        public VertexConsumer light(int u, int v) { return this; }

        @Override
        public VertexConsumer normal(float x, float y, float z) { return this; }

        @Override
        public void next() {}

        @Override
        public void fixedColor(int red, int green, int blue, int alpha) {}

        @Override
        public void unfixColor() {}
    };

    public OutlineOnlyVertexConsumerProvider(VertexConsumerProvider.Immediate unusedParent) {
        super(unusedParent);
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        RenderLayer outline = layer.isOutline() ? layer : layer.getAffectedOutline().orElse(null);
        return outline == null ? DISCARD : super.getBuffer(outline);
    }
}
