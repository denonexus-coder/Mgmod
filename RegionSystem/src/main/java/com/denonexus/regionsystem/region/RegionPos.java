package com.denonexus.regionsystem.region;

public record RegionPos(int x, int z) {

    public static final int SIZE = 4;

    public static RegionPos fromChunk(
            int chunkX,
            int chunkZ
    ) {
        return new RegionPos(
                Math.floorDiv(chunkX, SIZE),
                Math.floorDiv(chunkZ, SIZE)
        );
    }

    public int minChunkX() {
        return x * SIZE;
    }

    public int minChunkZ() {
        return z * SIZE;
    }

    public int maxChunkX() {
        return minChunkX() + SIZE - 1;
    }

    public int maxChunkZ() {
        return minChunkZ() + SIZE - 1;
    }

    public long key() {
        return ((long) x << 32)
                ^ (z & 0xffffffffL);
    }

    @Override
    public String toString() {
        return "Region[" + x + "," + z + "]";
    }
}
