package io.github.jasonsimpart.createdelightcore.mixin.Minecraft;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseChunkMixin {

    @Inject(method = "buildSurface", at = @At("HEAD"))
    private void fillNoiseCavesBeforeSurface(
            WorldGenRegion region, StructureManager structureManager,
            RandomState randomState, ChunkAccess chunk, CallbackInfo ci
    ) {
        // 如果配置为0，禁用此功能
        if (CDConfig.surfaceDepthLimit <= 0) {
            return;
        }

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        BlockState stone = Blocks.STONE.defaultBlockState();

        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();

        // 遍历当前区块的每一柱坐标
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = startX + localX;
                int worldZ = startZ + localZ;

                // 获取该垂直柱子在世界生成阶段的"真实地表高度"，排除水
                int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ);

                // 从推算出的地表往下扫配置的深度限制格
                for (int y = surfaceY - CDConfig.surfaceDepthLimit; y <= surfaceY; y++) {
                    if (y < chunk.getMinBuildHeight() || y >= chunk.getMaxBuildHeight()) continue;

                    mPos.set(worldX, y, worldZ);
                    BlockState state = chunk.getBlockState(mPos);
                    
                    // 只要发现这 16 格内存在噪音洞穴造成的空气，或者暴露的地下水层，就将其替换为石头
                    // 后续原版的逻辑会将这些石头替换为正常的地面方块
                    if (state.isAir() || state.is(Blocks.WATER)) {
                        chunk.setBlockState(mPos, stone, false);
                    }
                }
            }
        }
    }
}