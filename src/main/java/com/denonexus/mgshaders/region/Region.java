package com.denonexus.mgshaders.region;

import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Arrays;

public final class Region {

    public static final int SIZE = 4;
    public static final int CHUNK_COUNT = 16;

    private final RegionPos pos;

    private final ChunkAccess[] chunks =
            new ChunkAccess[CHUNK_COUNT];

    private RegionState state =
            RegionState.EMPTY;

    private long lastTouchedTick;

    private int loadedCount;

    public Region(RegionPos pos) {
        this.pos = pos;
    }

    public RegionPos pos() {
        return pos;
    }

    public RegionState state() {
        return state;
    }

    public void state(RegionState state) {
        this.state = state;
    }

    public long lastTouchedTick() {
        return lastTouchedTick;
    }

    public void touch(long tick) {
        lastTouchedTick = tick;
    }

    public int loadedCount() {
        return loadedCount;
    }

    public boolean isComplete() {
        return loadedCount == CHUNK_COUNT;
    }

    public void put(
            int chunkX,
            int chunkZ,
            ChunkAccess chunk
    ) {
        int localX =
                Math.floorMod(chunkX, SIZE);

        int localZ =
                Math.floorMod(chunkZ, SIZE);

        int index =
                localZ * SIZE + localX;

        if (chunks[index] == null) {
            loadedCount++;
        }

        chunks[index] = chunk;
    }

    public ChunkAccess get(
            int chunkX,
            int chunkZ
    ) {
        int localX =
                Math.floorMod(chunkX, SIZE);

        int localZ =
                Math.floorMod(chunkZ, SIZE);

        return chunks[
                localZ * SIZE + localX
        ];
    }

    public void clear() {
        Arrays.fill(chunks, null);
        loadedCount = 0;
        state = RegionState.EMPTY;
    }
}
