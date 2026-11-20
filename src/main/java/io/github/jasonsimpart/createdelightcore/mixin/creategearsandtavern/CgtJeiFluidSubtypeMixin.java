package io.github.jasonsimpart.createdelightcore.mixin.creategearsandtavern;

import io.github.jasonsimpart.createdelightcore.compat.creategearsandtavern.CgtJeiFluidEntries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Keep CGT's display-only brewing variants out of native JEI fluid identities. */
@Pseudo
@Mixin(targets = "mezz.jei.library.ingredients.subtypes.SubtypeManager", remap = false)
public abstract class CgtJeiFluidSubtypeMixin {
    @ModifyVariable(
            method = "getSubtypeInfo(Lmezz/jei/api/ingredients/IIngredientTypeWithSubtypes;Ljava/lang/Object;Lmezz/jei/api/ingredients/subtypes/UidContext;)Ljava/lang/String;",
            at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false
    )
    private Object createdelightcore$withoutBrewingVariant(Object ingredient) {
        return CgtJeiFluidEntries.withoutBrewingVariant(ingredient);
    }
}
