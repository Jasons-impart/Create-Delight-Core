package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopetavern.game.tap.impl.BarrelTapBehavior", remap = false)
public abstract class BarrelTapParticlesMixin {
    @ModifyExpressionValue(method = "onStartExtract", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/items/ItemStackHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack createdelightcore$useProductForParticles(ItemStack nativeSlot, @Local BarrelBlockEntity barrel) {
        var output = ((TavernBarrelAccess) barrel).createdelightcore$getFluidOutput();
        return output == null ? nativeSlot : output.assembledOutput();
    }
}
