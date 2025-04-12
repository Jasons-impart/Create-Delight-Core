package io.github.jasonsimpart.createdelightcore.content.util;

import com.teamtea.eclipticseasons.common.item.GrowthDetectorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EclipticSeasonsUtil {
    public static float getGrowChance(Level level, BlockPos pos, BlockState blockState) {
        return GrowthDetectorItem.getGrowChance(level, pos, blockState);
    }
}
