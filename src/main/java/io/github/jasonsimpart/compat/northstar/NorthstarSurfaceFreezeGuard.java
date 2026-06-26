package io.github.jasonsimpart.compat.northstar;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public final class NorthstarSurfaceFreezeGuard {
    private static final ResourceKey<Level> ENCELADUS_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "enceladus_dimension"));
    private static final int SURFACE_FREEZE_DEPTH = 3;

    private NorthstarSurfaceFreezeGuard() {
    }

    public static boolean shouldKeepWaterLiquid(Level level, BlockPos pos) {
        if (!level.dimension().equals(ENCELADUS_DIMENSION)) {
            return false;
        }

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        return pos.getY() < surfaceY - SURFACE_FREEZE_DEPTH;
    }
}
