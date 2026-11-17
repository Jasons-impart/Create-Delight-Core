package io.github.jasonsimpart.createdelightcore.mixin.bakeries;

import com.renyigesai.bakeries.recipe.DoughCraftingRecipe;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Bakeries' one-input forming recipe bypasses Create RecipeApplier. */
@Mixin(DoughCraftingRecipe.class)
public abstract class DoughCraftingRecipeQualityMixin {
    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true, require = 1)
    private void createdelightcore$inheritDoughQuality(
            Container ingredients, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir
    ) {
        ItemStack result = cir.getReturnValue();
        if (result == null || result.isEmpty()) {
            return;
        }
        ItemStack output = result.copy();
        // Never retain a recipe template's quality or mutate that shared template.
        CompoundTag tag = output.getTag();
        if (tag != null) {
            tag.remove(QualityUtils.QUALITY_TAG);
            if (tag.isEmpty()) {
                output.setTag(null);
            }
        }
        if (ingredients.getContainerSize() > 0) {
            ItemStack input = ingredients.getItem(0);
            Quality quality = QualityUtils.getQuality(input);
            // Plain input must stay plain, even when a previous result carried quality.
            if (!input.isEmpty() && QualityUtils.isValidQuality(quality) && quality.level() > 0) {
                QualityUtils.applyQuality(output, List.of(input), null);
            }
        }
        cir.setReturnValue(output);
    }
}
