package io.github.jasonsimpart.compat.iceandfire;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;

public final class IceAndFireWorldgenEntityGuard {
    private IceAndFireWorldgenEntityGuard() {
    }

    public static boolean addFreshEntityIfSafe(ServerLevelAccessor level, Entity entity) {
        if (!canPlaceWorldgenEntity(level, entity.blockPosition())) {
            return false;
        }

        try {
            return level.addFreshEntity(entity);
        } catch (IllegalStateException exception) {
            if (level instanceof WorldGenRegion) {
                return false;
            }
            throw exception;
        }
    }

    public static boolean canPlaceWorldgenEntity(ServerLevelAccessor level, BlockPos pos) {
        if (!level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        if (level instanceof WorldGenRegion region) {
            ChunkPos chunkPos = new ChunkPos(pos);
            return region.hasChunk(chunkPos.x, chunkPos.z);
        }
        return true;
    }
}
