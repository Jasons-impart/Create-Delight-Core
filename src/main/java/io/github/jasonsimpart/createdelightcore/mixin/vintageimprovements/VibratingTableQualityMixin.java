package io.github.jasonsimpart.createdelightcore.mixin.vintageimprovements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.jasonsimpart.createdelightcore.content.util.StorageRecipeQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Pseudo
@Mixin(targets = "com.negodya1.vintageimprovements.content.kinetics.vibration.VibratingTableBlockEntity", remap = false)
public abstract class VibratingTableQualityMixin extends BlockEntity {
    protected VibratingTableQualityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapOperation(method = "process", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraft/world/item/ItemStack;m_41774_(I)V", ordinal = 0), require = 1)
    private void createdelightcore$snapshotSource(ItemStack source, int amount, Operation<Void> original,
            @Share("unpackingSource") LocalRef<ItemStack> snapshot) {
        snapshot.set(source.copyWithCount(1));
        original.call(source, amount);
    }

    @ModifyExpressionValue(method = "process", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraft/world/item/ItemStack;m_41777_()Lnet/minecraft/world/item/ItemStack;",
            ordinal = 0), require = 1)
    private ItemStack createdelightcore$inheritUnpackingQuality(ItemStack output,
            @Share("unpackingSource") LocalRef<ItemStack> snapshot,
            @Local(ordinal = 1) Optional<?> unpackingRecipe) {
        ItemStack source = snapshot.get();
        if (source == null || level == null || unpackingRecipe.isEmpty()) {
            return output;
        }
        return StorageRecipeQuality.convert(output, new SimpleContainer(source),
                (Recipe<?>) unpackingRecipe.get(), level);
    }
}
