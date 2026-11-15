package io.github.jasonsimpart.createdelightcore.mixin.youkaishomecoming;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.core.Quality;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/** Roll once from the actual batch before native fermentation replaces its inputs. */
@Pseudo
@Mixin(targets = "dev.xkmc.youkaishomecoming.content.pot.ferment.FermentationTankBlockEntity", remap = false)
public abstract class FermentationTankQualityMixin {
    @Shadow(remap = false) public abstract SimpleContainer getItemHandler();

    @Inject(method = "finishRecipe(Lnet/minecraft/world/level/Level;Ldev/xkmc/youkaishomecoming/content/pot/ferment/FermentationRecipe;)V",
            at = @At("HEAD"))
    private void createdelightcore$captureInput(Level level, @Coerce Object recipe, CallbackInfo ci,
                                                @Share("batchQuality") LocalRef<Quality> quality,
                                                @Share("qualityTank") LocalRef<IFluidHandler> tank) {
        if (!(recipe instanceof SimpleFermentationRecipeAccessor)) return;
        var handler = ((BlockEntity) (Object) this).getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if (handler == null) return;
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0; slot < getItemHandler().getContainerSize(); slot++) {
            ItemStack item = getItemHandler().getItem(slot);
            if (!item.isEmpty()) items.add(item.copy());
        }
        FluidStack fluid = handler.getFluidInTank(0).copy();
        quality.set(QualityFoodFluidsCreateRules.rollBasinQuality(items,
                fluid.isEmpty() ? List.of() : List.of(fluid)));
        tank.set(handler);
    }

    @Inject(method = "finishRecipe(Lnet/minecraft/world/level/Level;Ldev/xkmc/youkaishomecoming/content/pot/ferment/FermentationRecipe;)V",
            at = @At("RETURN"))
    private void createdelightcore$inheritQuality(Level level, @Coerce Object recipe, CallbackInfo ci,
                                                  @Share("batchQuality") LocalRef<Quality> quality,
                                                  @Share("qualityTank") LocalRef<IFluidHandler> tank) {
        if (quality.get() == null || tank.get() == null) return;
        var results = ((SimpleFermentationRecipeAccessor) recipe).createdelightcore$getResults();
        var inventory = getItemHandler();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack output = inventory.getItem(slot);
            if (!output.isEmpty() && results.stream().anyMatch(template -> template.is(output.getItem()))) {
                inventory.setItem(slot, QualityFoodFluidsCreateRules.applyItemOutputQuality(quality.get(),
                        QualityFoodFluidsApi.clearItemQuality(output)));
            }
        }
        FluidStack output = tank.get().getFluidInTank(0);
        if (QualityFoodFluidsApi.canCarryQuality(output)) {
            FluidStack qualified = QualityFoodFluidsCreateRules.applyFluidOutputQuality(quality.get(), output.copy());
            // BaseTank returns its live stack; native assemble copied the output template.
            output.setTag(qualified.getTag());
        }
        BlockEntity tile = (BlockEntity) (Object) this;
        tile.setChanged();
        level.sendBlockUpdated(tile.getBlockPos(), tile.getBlockState(), tile.getBlockState(), 3);
    }
}
