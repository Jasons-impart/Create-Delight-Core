package io.github.jasonsimpart.createdelightcore.compat.alexsmobs;

import java.util.HashSet;
import java.util.Set;

/**
 * Plain Java checks for the 3×3 chunk set used by falconry tickets.
 * Does not start Minecraft; packing must stay identical to {@code ChunkPos.asLong}.
 */
public final class BaldEagleChunkTicketsTest {
    public static void main(String[] args) {
        originChunk();
        neighboringChunk();
        chunkBoundary();
        uniqueNine();
        packFormula();
        constants();
        System.out.println("Bald eagle chunk tickets: 6 scenarios passed");
    }

    private static void originChunk() {
        long[] chunks = BaldEagleChunkGeometry.chunksAroundBlock(0, 0);
        equal(9, chunks.length, "origin should cover 9 chunks");
        Set<Long> set = toSet(chunks);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (!set.contains(pack(x, z))) {
                    throw new AssertionError("missing chunk " + x + "," + z);
                }
            }
        }
    }

    private static void neighboringChunk() {
        // Block (16, 16) is chunk (1, 1)
        long[] chunks = BaldEagleChunkGeometry.chunksAroundBlock(16, 16);
        Set<Long> set = toSet(chunks);
        equal(9, set.size(), "chunk (1,1) should still cover 9 unique chunks");
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 2; z++) {
                if (!set.contains(pack(x, z))) {
                    throw new AssertionError("missing chunk " + x + "," + z + " around (1,1)");
                }
            }
        }
        if (set.contains(pack(-1, -1))) {
            throw new AssertionError("chunk (-1,-1) should not be in the 3×3 around (1,1)");
        }
    }

    private static void chunkBoundary() {
        // Block 15 is still chunk 0; block 16 is chunk 1
        Set<Long> at15 = toSet(BaldEagleChunkGeometry.chunksAroundBlock(15, 15));
        Set<Long> at16 = toSet(BaldEagleChunkGeometry.chunksAroundBlock(16, 16));
        if (at15.equals(at16)) {
            throw new AssertionError("crossing a chunk border must change the 3×3");
        }
        if (!at15.contains(pack(0, 0)) || !at16.contains(pack(1, 1))) {
            throw new AssertionError("border samples should include their own center chunks");
        }
    }

    private static void uniqueNine() {
        long[] chunks = BaldEagleChunkGeometry.chunksAroundBlock(-35, 64);
        equal(9, toSet(chunks).size(), "every 3×3 must contain 9 unique packed positions");
    }

    private static void packFormula() {
        equal(pack(3, -4), BaldEagleChunkGeometry.packChunk(3, -4),
                "packChunk must match ChunkPos.asLong / test pack formula");
        equal(pack(-1, 1), BaldEagleChunkGeometry.packChunk(-1, 1),
                "negative chunk X must pack identically");
    }

    private static void constants() {
        equal(1, BaldEagleChunkGeometry.CHUNK_RADIUS, "radius must stay 1 for a 3×3");
    }

    private static long pack(int chunkX, int chunkZ) {
        return (long) chunkX & 0xFFFFFFFFL | ((long) chunkZ & 0xFFFFFFFFL) << 32;
    }

    private static Set<Long> toSet(long[] chunks) {
        Set<Long> set = new HashSet<>();
        for (long packed : chunks) {
            set.add(packed);
        }
        return set;
    }

    private static void equal(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " expected " + expected + " but was " + actual);
        }
    }
}
