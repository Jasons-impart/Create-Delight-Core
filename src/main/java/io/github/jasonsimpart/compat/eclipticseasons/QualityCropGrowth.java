package io.github.jasonsimpart.compat.eclipticseasons;

import de.cadentem.quality_food.core.attachments.LevelData;
import de.cadentem.quality_food.registry.QFComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Legacy crop resistance, shared by Ecliptic's actual growth checks and detector. */
public final class QualityCropGrowth {
    private QualityCropGrowth() {}

    public static boolean isQualityCrop(ItemStack stack) {
        var quality = stack.get(QFComponents.QUALITY_DATA_COMPONENT.get());
        return quality != null && quality.level() > 0
                && stack.getTags().anyMatch(tag -> tag.location().getNamespace().equals("eclipticseasons"));
    }

    public static float apply(float chance, Level level, BlockPos pos) {
        int rank = LevelData.get(level, pos).level();
        if (rank <= 0) return chance;
        float boost = rank >= 3 ? 1.0F : rank == 2 ? 0.5F : 0.25F;
        return boost + (1.0F - boost) * chance;
    }
}
