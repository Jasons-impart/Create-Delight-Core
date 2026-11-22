package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.gunmu.northeast_china_delight.block.VatBlockEntity;
import com.gunmu.northeast_china_delight.crafting.VatBrewing;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.PackVinegar;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Pseudo
@Mixin(value = VatBlockEntity.class, remap = false)
public abstract class VatYeastMixin {
    @Shadow @Final private NonNullList<ItemStack> contents;
    @Shadow public abstract void sync();

    @Inject(method = "addContent", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$stackDryYeast(ItemStack input, CallbackInfoReturnable<Boolean> cir) {
        if (!PackVinegar.isYeast(input)) return;

        if (VatBrewing.forInsert((VatBlockEntity) (Object) this, input) == null) {
            cir.setReturnValue(false);
            return;
        }
        for (ItemStack stack : contents) {
            if (!ItemStack.isSameItemSameTags(stack, input)) continue;
            stack.grow(1);
            sync();
            cir.setReturnValue(true);
            return;
        }
    }

    @Inject(method = "count", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$countYeastByAmount(Predicate<ItemStack> matches, CallbackInfoReturnable<Integer> cir) {
        int count = 0;
        for (ItemStack stack : contents) {
            if (!stack.isEmpty() && matches.test(stack)) {
                count += PackVinegar.isYeast(stack) ? stack.getCount() : 1;
            }
        }
        cir.setReturnValue(count);
    }

    @Inject(method = "contentSnapshot", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$snapshotIndividualServings(CallbackInfoReturnable<List<ItemStack>> cir) {
        List<ItemStack> snapshot = new ArrayList<>();
        for (ItemStack stack : contents) {
            if (stack.isEmpty()) continue;
            // VatBrewing consumes or refunds one item for each entry in the snapshot.
            int copies = PackVinegar.isYeast(stack) ? stack.getCount() : 1;
            for (int index = 0; index < copies; index++) snapshot.add(stack.copyWithCount(1));
        }
        cir.setReturnValue(snapshot);
    }

    @WrapOperation(method = "removeOneMatching", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
    private Object createdelightcore$spendOneYeast(NonNullList<ItemStack> stacks, int slot, Object empty,
                                                   Operation<Object> original) {
        ItemStack held = stacks.get(slot);
        if (!PackVinegar.isYeast(held) || held.getCount() == 1) return original.call(stacks, slot, empty);
        held.shrink(1);
        return held;
    }

    @Inject(method = "replaceOneMatching", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$replaceOneYeast(Predicate<ItemStack> matches, ItemStack replacement,
                                                    CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack stack : contents) {
            if (stack.isEmpty() || !matches.test(stack)) continue;
            if (!PackVinegar.isYeast(stack) || stack.getCount() == 1) return;
            for (int slot = 0; slot < contents.size(); slot++) {
                if (!contents.get(slot).isEmpty()) continue;
                contents.set(slot, replacement.copyWithCount(1));
                stack.shrink(1);
                sync();
                cir.setReturnValue(true);
                return;
            }
            cir.setReturnValue(false);
            return;
        }
    }
}
