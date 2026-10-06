/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.mixin.baritone;

import meteordevelopment.meteorclient.forge.ForgeBaritoneChunkSnapshot;
import net.minecraft.client.world.ClientChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.farsight.FarsightClientChunkManager", remap = false)
public abstract class FarsightBaritoneMixin {
    public ClientChunkManager createThreadSafeCopy() {
        return ForgeBaritoneChunkSnapshot.copyFarsight((ClientChunkManager) (Object) this);
    }
}
