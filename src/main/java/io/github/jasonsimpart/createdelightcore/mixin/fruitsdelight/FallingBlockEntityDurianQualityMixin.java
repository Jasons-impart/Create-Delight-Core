package io.github.jasonsimpart.createdelightcore.mixin.fruitsdelight;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import dev.xkmc.fruitsdelight.content.block.DurianBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityDurianQualityMixin {
    @WrapOperation(
            method = "tick",
            at = @org.spongepowered.asm.mixin.injection.At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
    )
    private boolean create_Delight_Core$storeDurianBlockQuality(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {
        boolean placed = original.call(level, pos, state, flags);
        if (!placed || !create_Delight_Core$isDurian(state)) {
            return placed;
        }

        FallingBlockEntity self = (FallingBlockEntity) (Object) this;
        Quality sourceQuality = LevelData.get(level, self.getStartPos(), true);
        if (QualityUtils.isValidQuality(sourceQuality)) {
            LevelData.set(level, pos, sourceQuality);
        }
        return placed;
    }

    @WrapOperation(
            method = "tick",
            at = @org.spongepowered.asm.mixin.injection.At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private ItemEntity create_Delight_Core$applyDurianItemQuality(FallingBlockEntity self, ItemLike itemLike, Operation<ItemEntity> original) {
        if (!create_Delight_Core$isDurian(self.getBlockState())) {
            return original.call(self, itemLike);
        }

        ItemStack stack = new ItemStack(itemLike);
        Quality sourceQuality = LevelData.get(self.level(), self.getStartPos(), true);
        if (QualityUtils.isValidQuality(sourceQuality)) {
            QualityUtils.applyQuality(stack, sourceQuality);
        }
        return self.spawnAtLocation(stack);
    }

    private static boolean create_Delight_Core$isDurian(BlockState state) {
        return state.getBlock() instanceof DurianBlock;
    }
}
