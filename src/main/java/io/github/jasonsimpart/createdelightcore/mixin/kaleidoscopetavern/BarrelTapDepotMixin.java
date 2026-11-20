package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernTapDepot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = BarrelBlockEntity.class, remap = false)
public abstract class BarrelTapDepotMixin {
    @Shadow private Ingredient getCurrentCarrier(Level level) { throw new AssertionError(); }
    @Shadow private ItemStack extractOneOutput(ItemStack result) { throw new AssertionError(); }
    @Shadow private void resetIfOutputEmpty() { throw new AssertionError(); }

    // This point is after the native brewing, output and recipe-carrier checks.
    @Inject(method = "canTapExtract", at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
            cancellable = true)
    private void createdelightcore$recognizeDepot(Level level, BlockPos tapPos, LivingEntity entity,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (level.getBlockEntity(tapPos.below()) instanceof DepotBlockEntity depot
                && TavernTapDepot.canFill(depot, getCurrentCarrier(level))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "transform", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$fillDepot(Level level, BlockPos tapPos, Ingredient carrier, ItemStack result,
                                             CallbackInfo ci) {
        if (!(level.getBlockEntity(tapPos.below()) instanceof DepotBlockEntity depot)
                || !TavernTapDepot.canFill(depot, carrier)) return;
        ci.cancel();
        if (level.isClientSide) return;
        TavernTapDepot.fill(depot, carrier, () -> {
            ItemStack filled = extractOneOutput(result);
            if (!filled.isEmpty()) resetIfOutputEmpty();
            return filled;
        });
    }
}
