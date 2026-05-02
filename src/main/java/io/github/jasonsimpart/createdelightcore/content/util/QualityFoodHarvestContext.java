package io.github.jasonsimpart.createdelightcore.content.util;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class QualityFoodHarvestContext {
    private static final ThreadLocal<BlockPos> CURRENT_CROP_POS = new ThreadLocal<>();

    private QualityFoodHarvestContext() {
    }

    public static @Nullable BlockPos getCropPos() {
        return CURRENT_CROP_POS.get();
    }

    public static @Nullable BlockPos push(BlockPos pos) {
        BlockPos previous = CURRENT_CROP_POS.get();
        CURRENT_CROP_POS.set(pos.immutable());
        return previous;
    }

    public static void pop(@Nullable BlockPos previous) {
        if (previous == null) {
            CURRENT_CROP_POS.remove();
            return;
        }
        CURRENT_CROP_POS.set(previous);
    }
}
