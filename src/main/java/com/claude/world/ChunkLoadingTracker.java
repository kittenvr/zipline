package com.claude.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.Set;

public class ChunkLoadingTracker {
    private final Set<ChunkPos> loadedChunks = new HashSet<>();

    public boolean isChunkLoaded(ChunkPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;

        WorldChunk chunk = client.world.getChunkManager().getWorldChunk(pos.x, pos.z);
        return chunk != null;
    }

    public void markChunkLoaded(ChunkPos pos) {
        loadedChunks.add(pos);
    }

    public void clear() {
        loadedChunks.clear();
    }
}
