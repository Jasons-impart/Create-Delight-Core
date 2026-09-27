package io.github.jasonsimpart.createdelightcore.mixin.createbicbit;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.pyzpre.createbitterballen.block.mechanicalfryer.DeepFryingRecipe;
import com.pyzpre.createbitterballen.block.mechanicalfryer.MechanicalFryerEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MechanicalFryerEntity.class)
public class MechanicalFryerEntityMixin {
    @Shadow(remap = false)
    public SmartInventory inputInv;

    @Inject(
            method = "applyRecipe(Lcom/pyzpre/createbitterballen/block/mechanicalfryer/DeepFryingRecipe;)Z",
            at = @At("HEAD"),
            remap = false
    )
    private void createDelightCore$captureInputQuality(
            DeepFryingRecipe recipe,
            CallbackInfoReturnable<Boolean> cir,
            @Share("qualityInput") LocalRef<ItemStack> input
    ) {
        input.set(inputInv.getStackInSlot(0).copy());
    }

    @ModifyArg(
            method = "applyRecipe(Lcom/pyzpre/createbitterballen/block/mechanicalfryer/DeepFryingRecipe;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/items/ItemHandlerHelper;insertItemStacked(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"
            ),
            index = 1,
            remap = false
    )
    private ItemStack createDelightCore$applyQuality(
            ItemStack output,
            @Share("qualityInput") LocalRef<ItemStack> input
    ) {
        QualityUtils.applyQuality(output, List.of(input.get()), null);
        return output;
    }
}
