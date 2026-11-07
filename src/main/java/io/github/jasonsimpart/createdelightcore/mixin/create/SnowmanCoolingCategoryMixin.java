// Adapted from Create More Recipes by iglee42 (MIT). See META-INF/licenses/create-more-recipes.txt.
package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.compat.jei.category.MixingCategory;
import com.simibubi.create.compat.jei.category.PackingCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import io.github.jasonsimpart.createdelightcore.compat.jei.utils.AnimatedSnowmanCooler;
import io.github.jasonsimpart.createdelightcore.content.cooler.CoolerHeat;
import io.github.jasonsimpart.createdelightcore.content.cooler.SnowmanCoolerBlock;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = {MixingCategory.class, PackingCategory.class}, remap = false)
public abstract class SnowmanCoolingCategoryMixin {
    @WrapOperation(method = "draw(Lcom/simibubi/create/content/processing/basin/BasinRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/compat/jei/category/animations/AnimatedBlazeBurner;draw(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void createdelightcore$drawCooler(AnimatedBlazeBurner burner, GuiGraphics graphics, int x, int y,
                                             Operation<Void> original, @Local(argsOnly = true) BasinRecipe recipe) {
        if (CoolerHeat.isCooling(recipe.getRequiredHeat())) {
            new AnimatedSnowmanCooler().withHeat(CoolerHeat.isFreezing(recipe.getRequiredHeat())
                    ? SnowmanCoolerBlock.HeatLevel.FREEZING : SnowmanCoolerBlock.HeatLevel.COOLING).draw(graphics, x, y);
        } else original.call(burner, graphics, x, y);
    }
}
