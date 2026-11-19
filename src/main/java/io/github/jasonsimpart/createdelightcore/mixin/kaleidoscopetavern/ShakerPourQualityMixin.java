package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(value = ShakerItem.class, remap = false)
public abstract class ShakerPourQualityMixin {
    @WrapOperation(method = "pourResult", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            remap = true))
    private boolean createdelightcore$storePouredQuality(Level level, BlockPos pos, BlockState state,
                                                         Operation<Boolean> original, @Local(ordinal = 1) ItemStack result) {
        boolean placed = original.call(level, pos, state);
        if (placed && !level.isClientSide && result.getItem() instanceof CocktailBlockItem) {
            if (level.getBlockEntity(pos) instanceof DrinkBlockEntity drink) {
                // Native Mojito pouring creates an empty DrinkBlockEntity, which otherwise returns no drink on pickup.
                drink.addItem(result.copyWithCount(1));
            }
            Quality quality = QualityUtils.getQuality(result);
            // NONE is a no-op in LevelData.set; the native placed marker also prevents a fresh harvest roll.
            LevelData.set(level, pos, quality == Quality.NONE ? Quality.NONE_PLAYER_PLACED : quality);
        }
        return placed;
    }
}
