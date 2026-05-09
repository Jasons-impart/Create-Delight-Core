package io.github.jasonsimpart.createdelightcore.content.util;

import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import de.cadentem.quality_food.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public final class FruitsDelightTreeQualityContext {
    private static final String FRUITS_DELIGHT_NAMESPACE = "fruitsdelight";
    private static final ThreadLocal<Quality> SAPLING_QUALITY = new ThreadLocal<>();
    private static final ThreadLocal<Set<Long>> CHANGED_TREE_BLOCKS = new ThreadLocal<>();

    private FruitsDelightTreeQualityContext() {
    }

    public static void begin(ServerLevel level, BlockPos pos, BlockState state) {
        clear();
        if (!isFruitsDelightSapling(state)) {
            return;
        }
        Quality quality = LevelData.get(level, pos);
        if (!QualityUtils.isValidQuality(quality)) {
            return;
        }
        SAPLING_QUALITY.set(quality);
        CHANGED_TREE_BLOCKS.set(new HashSet<>());
    }

    public static void recordChangedBlock(BlockPos pos, BlockState newState) {
        Set<Long> changedTreeBlocks = CHANGED_TREE_BLOCKS.get();
        if (changedTreeBlocks == null || !isTreeGrowthBlock(newState)) {
            return;
        }
        changedTreeBlocks.add(pos.asLong());
    }

    public static void finish(ServerLevel level, boolean success) {
        Quality quality = SAPLING_QUALITY.get();
        Set<Long> changedTreeBlocks = CHANGED_TREE_BLOCKS.get();
        if (!success || quality == null || changedTreeBlocks == null || changedTreeBlocks.isEmpty()) {
            clear();
            return;
        }

        try {
            for (long packedPos : changedTreeBlocks) {
                BlockPos changedPos = BlockPos.of(packedPos);
                BlockState changedState = level.getBlockState(changedPos);
                if (!isTreeGrowthBlock(changedState)) {
                    continue;
                }
                if (!Utils.isValidBlock(changedState)) {
                    continue;
                }
                LevelData.set(level, changedPos, quality);
            }
        } finally {
            clear();
        }
    }

    private static boolean isFruitsDelightSapling(BlockState state) {
        if (!state.is(BlockTags.SAPLINGS)) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return FRUITS_DELIGHT_NAMESPACE.equals(id.getNamespace());
    }

    private static boolean isTreeGrowthBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return FRUITS_DELIGHT_NAMESPACE.equals(id.getNamespace());
    }

    private static void clear() {
        CHANGED_TREE_BLOCKS.remove();
        SAPLING_QUALITY.remove();
    }
}
