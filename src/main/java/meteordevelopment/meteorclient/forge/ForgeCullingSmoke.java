package meteordevelopment.meteorclient.forge;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.ModList;

/** Opt-in culling probe; no entities are spawned or registered. */
final class ForgeCullingSmoke {
    private ForgeCullingSmoke() {}

    static void run() {
        if (!ModList.get().isLoaded("entityculling")) {
            MeteorClient.LOG.info("REGRESSION_CULLING_PASS installed=false");
            return;
        }
        boolean original = PostProcessShaders.rendering;
        try {
            Entity entity = EntityType.COW.create(MeteorClient.mc.world);
            if (entity == null) throw new IllegalStateException("Culling probe entity missing");
            var setCulled = Entity.class.getMethod("setCulled", boolean.class);
            var culled = Entity.class.getMethod("isCulled");
            var forced = Entity.class.getMethod("isForcedVisible");
            setCulled.invoke(entity, true);
            PostProcessShaders.rendering = false;
            boolean normal = (boolean) forced.invoke(entity);
            boolean before = (boolean) culled.invoke(entity);
            PostProcessShaders.rendering = true;
            boolean custom = (boolean) forced.invoke(entity);
            PostProcessShaders.rendering = false;
            boolean restored = (boolean) forced.invoke(entity);
            boolean after = (boolean) culled.invoke(entity);
            if (normal || !custom || restored || before != after)
                throw new IllegalStateException("EntityCulling custom-pass isolation failed");
            MeteorClient.LOG.info("REGRESSION_CULLING_PASS installed=true normalForced={} shaderForced={} restored={} cullingStatePreserved=true", normal, custom, restored);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("EntityCulling runtime probe failed", e);
        } finally {
            PostProcessShaders.rendering = original;
        }
    }
}
