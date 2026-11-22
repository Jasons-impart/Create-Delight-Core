package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.gunmu.northeast_china_delight.item.SeasoningBottleFluidHandler;
import com.gunmu.northeast_china_delight.item.SeasoningBottleItem;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.jasonsimpart.createdelightcore.compat.alcohol.ItemFluidContainers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = SeasoningBottleFluidHandler.class, remap = false)
public abstract class SeasoningBottleFluidMixin {
    @Shadow private ItemStack stack;

    @Unique
    private static ItemFluidContainers.Rule createdelightcore$seasoningRule(Item item) {
        var rule = ItemFluidContainers.findForItem(item);
        return rule != null && rule.amount() == SeasoningBottleItem.CAPACITY_MB && rule.emptyContainer() == Items.GLASS_BOTTLE
                ? rule : null;
    }

    @ModifyReturnValue(method = "fluidOf", at = @At("RETURN"))
    private static Fluid createdelightcore$useRegisteredFluid(Fluid nativeFluid, Item item) {
        var rule = createdelightcore$seasoningRule(item);
        return nativeFluid != null && rule != null ? rule.fluid() : nativeFluid;
    }

    @ModifyReturnValue(method = "getFluidInTank", at = @At("RETURN"))
    private FluidStack createdelightcore$showQuality(FluidStack fluid) {
        var rule = createdelightcore$seasoningRule(stack.getItem());
        return rule != null && !fluid.isEmpty() && rule.fluid() == fluid.getFluid()
                ? QualityFoodFluidsApi.withQuality(fluid, QualityUtils.getQuality(stack)) : fluid;
    }

    @WrapMethod(method = "drain(ILnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;")
    private FluidStack createdelightcore$keepDrainedQuality(int amount, FluidAction action, Operation<FluidStack> original) {
        var rule = createdelightcore$seasoningRule(stack.getItem());
        if (rule == null) return original.call(amount, action);

        Quality quality = QualityUtils.getQuality(stack);
        FluidStack drained = original.call(amount, action);
        return !drained.isEmpty() && rule.fluid() == drained.getFluid()
                ? QualityFoodFluidsApi.withQuality(drained, quality) : drained;
    }

    @Inject(method = "drain(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("HEAD"), cancellable = true)
    private void createdelightcore$requireMatchingQuality(FluidStack request, FluidAction action,
                                                           CallbackInfoReturnable<FluidStack> cir) {
        var rule = createdelightcore$seasoningRule(stack.getItem());
        if (rule != null && !request.isEmpty() && rule.fluid() == request.getFluid()
                && QualityFoodFluidsApi.getQuality(request) != QualityUtils.getQuality(stack)) {
            cir.setReturnValue(FluidStack.EMPTY);
        }
    }

    @WrapMethod(method = "fill")
    private int createdelightcore$keepFilledQuality(FluidStack resource, FluidAction action, Operation<Integer> original) {
        var rule = createdelightcore$seasoningRule(stack.getItem());
        if (rule == null || resource.isEmpty() || rule.fluid() != resource.getFluid()) {
            return original.call(resource, action);
        }
        Quality incoming = QualityFoodFluidsApi.getQuality(resource);
        boolean empty = SeasoningBottleItem.remainingMb(stack) <= 0;
        if (!empty && incoming != QualityUtils.getQuality(stack)) return 0;
        int filled = original.call(resource, action);
        if (empty && filled > 0 && action.execute()) {
            stack.removeTagKey(QualityUtils.QUALITY_TAG);
            QualityUtils.applyQuality(stack, incoming);
        }
        return filled;
    }
}
