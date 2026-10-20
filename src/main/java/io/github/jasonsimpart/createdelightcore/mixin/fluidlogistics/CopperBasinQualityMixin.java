package io.github.jasonsimpart.createdelightcore.mixin.fluidlogistics;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.yision.fluidlogistics.content.processing.copperBasin.CopperBasinBlockEntity;
import io.github.createdelight.qualityfoodfluids.internal.BasinRecipeQualityContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 铜工作盆（FluidLogistics）产出流体时丢失品质。
 *
 * 现象：动力搅拌器处理铜工作盆，配方产物为流体时品质丢失；Create 原版工作盆不受影响。
 *
 * 根因：CopperBasinBlockEntity 覆写了 acceptOutputs，有流体时把流体从 super 调用里剥掉
 * （传 Collections.emptyList()），改由 acceptFluidsIntoOutputTank 自己灌进 outputTank。
 * 而 Quality Food Fluids 的品质注入挂在父类 BasinBlockEntity.acceptOutputs 的 HEAD，
 * 拿到的永远是空流体列表，守卫条件两个都不成立时直接放行，品质被静默跳过。
 *
 * 修法：在子类方法开头主动调用一次 BasinRecipeQualityContext.applyToOutputs，
 * 此时 outputFluids 还是 BasinRecipe 传进来的可变原列表，QFF 会把带品质的 FluidStack
 * 写回该列表，铜工作盆随后灌罐即为带品质流体。
 *
 * 安全性：applyToOutputs 内部有 context == null 守卫，从非配方路径（管道插入等）调用时
 * 直接返回 true，无副作用；注入点在 outputTank.allowInsertion() 之前，不干扰其 try/finally。
 */
@Mixin(value = CopperBasinBlockEntity.class, remap = false)
public abstract class CopperBasinQualityMixin {

    @Inject(method = "acceptOutputs", at = @At("HEAD"), remap = false)
    private void createdelightcore$applyFluidQuality(List<ItemStack> outputItems, List<FluidStack> outputFluids,
                                                     boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        BasinRecipeQualityContext.applyToOutputs((BasinBlockEntity) (Object) this, outputItems, outputFluids, simulate);
    }
}
