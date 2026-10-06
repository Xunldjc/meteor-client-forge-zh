/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.forge;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

/** Keeps Farsight's loaded chunks in Baritone's structurally immutable worker snapshot. */
public final class ForgeBaritoneChunkSnapshot extends ClientChunkManager {
    private final Map<Long, WorldChunk> snapshot;

    private ForgeBaritoneChunkSnapshot(ClientWorld world, Map<Long, WorldChunk> chunks) {
        super(world, 2);
        snapshot = Map.copyOf(chunks);
    }

    public static ClientChunkManager copyFarsight(ClientChunkManager provider) {
        try {
            // Farsight replaces the vanilla ring buffer with its own nonblocking map.
            Field field = provider.getClass().getDeclaredField("chunks");
            field.setAccessible(true);
            Map<?, ?> loaded = (Map<?, ?>) field.get(provider);
            Map<Long, WorldChunk> copy = new HashMap<>();
            for (Object value : loaded.values()) {
                if (value instanceof WorldChunk chunk) copy.put(chunk.getPos().toLong(), chunk);
            }
            return new ForgeBaritoneChunkSnapshot(MeteorClient.mc.world, copy);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Farsight loaded-chunk snapshot contract changed", e);
        }
    }

    @Override
    public WorldChunk getChunk(int x, int z, ChunkStatus status, boolean create) {
        return snapshot.get(ChunkPos.toLong(x, z));
    }

    @Override
    public int getLoadedChunkCount() {
        return snapshot.size();
    }
}
