package io.github.jasonsimpart.createdelightcore.compat.alexsmobs;

/**
 * Pure packing math for the falconry 3×3. Kept free of Minecraft types so
 * the layout can be unit-tested without starting the game. The encoding
 * matches {@code net.minecraft.world.level.ChunkPos#asLong(int, int)}.
 */
public final class BaldEagleChunkGeometry {
    /** Inclusive Chebyshev radius, producing the same 3×3 as upstream. */
    public static final int CHUNK_RADIUS = 1;

    private BaldEagleChunkGeometry() {
    }

    public static long packChunk(int chunkX, int chunkZ) {
        return (long) chunkX & 0xFFFFFFFFL | ((long) chunkZ & 0xFFFFFFFFL) << 32;
    }

    public static long[] chunksAroundBlock(int blockX, int blockZ) {
        int centerX = blockX >> 4;
        int centerZ = blockZ >> 4;
        int size = 2 * CHUNK_RADIUS + 1;
        long[] packed = new long[size * size];
        int index = 0;
        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                packed[index++] = packChunk(centerX + dx, centerZ + dz);
            }
        }
        return packed;
    }
}
