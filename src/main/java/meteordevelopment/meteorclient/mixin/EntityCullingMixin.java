package meteordevelopment.meteorclient.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = 900)
public abstract class EntityCullingMixin {
    @Inject(method = "isForcedVisible", at = @At("HEAD"), cancellable = true, remap = false)
    private void meteor$allowCustomShader(CallbackInfoReturnable<Boolean> info) {
        if (PostProcessShaders.rendering && RenderSystem.isOnRenderThread()) info.setReturnValue(true);
    }
}
