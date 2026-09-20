package io.github.jasonsimpart.mixin.createmetallurgy;

import fr.lucreeper74.createmetallurgy.content.blocks.foundry_basin.FoundryBasinRecipe;
import fr.lucreeper74.createmetallurgy.content.blocks.foundry_mixer.AlloyingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FoundryBasinRecipe.class, remap = false)
public abstract class FoundryBasinRecipeMixin {
    @Inject(method = "getMaxInputCount", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$allowFourScrapAlloying(CallbackInfoReturnable<Integer> result) {
        // Repeated ingredients count separately in Create 6. The basin can consume
        // all four from one stack; this is a recipe validation limit, not slot count.
        if ((Object) this instanceof AlloyingRecipe) result.setReturnValue(Math.max(4, result.getReturnValue()));
    }
}
