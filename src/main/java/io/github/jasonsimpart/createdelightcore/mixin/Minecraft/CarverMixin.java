package io.github.jasonsimpart.createdelightcore.mixin.Minecraft;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver; // 正确的类名
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(WorldCarver.class)
public abstract class CarverMixin {

    @Inject(
            method = "carveBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void restrictCarvingNearSurface(
            CarvingContext context,
            CarverConfiguration config,
            ChunkAccess chunk,
            Function biomeAccessor,
            CarvingMask carvingMask,
            BlockPos.MutableBlockPos pos,
            BlockPos.MutableBlockPos checkPos,
            Aquifer aquifer,
            MutableBoolean reachedSurface,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // 如果配置为0，禁用此功能
        if (CDConfig.surfaceDepthLimit <= 0) {
            return;
        }

        // 1. 获取当前方块在区块内的相对 X 和 Z 坐标 (范围 0-15)
        int localX = pos.getX() & 15;
        int localZ = pos.getZ() & 15;

        // 2. 获取该垂直柱子在世界生成阶段的"真实地表高度"，排除水
        int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ);

        // 3. 核心拦截逻辑：如果当前正在尝试雕刻的 Y 坐标，距离地表不到配置的深度限制
        if (pos.getY() >= surfaceY - CDConfig.surfaceDepthLimit) {
            // 强制返回 false，拦截雕刻行为
            cir.setReturnValue(false);
        }
    }
}