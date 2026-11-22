package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.container.BarrelRecipeContainer;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = BarrelRecipe.class, remap = false)
public abstract class FixedBarrelRecipeMixin {
    @Inject(method = "matches(Lcom/github/ysbbbbbb/kaleidoscopetavern/crafting/container/BarrelRecipeContainer;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"), cancellable = true)
    private void createdelightcore$matchFixedBatch(BarrelRecipeContainer input, Level level,
                                                        CallbackInfoReturnable<Boolean> cir) {
        var recipe = (BarrelRecipe) (Object) this;
        var batch = TavernBarrelRecipes.fixedBatch(recipe);
        if (batch != null) cir.setReturnValue(batch.matches(recipe, input));
    }

    @Inject(method = "assemble(Lcom/github/ysbbbbbb/kaleidoscopetavern/crafting/container/BarrelRecipeContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"), cancellable = true)
    private void createdelightcore$assembleFixedBatch(BarrelRecipeContainer input, RegistryAccess registries,
                                                           CallbackInfoReturnable<ItemStack> cir) {
        var recipe = (BarrelRecipe) (Object) this;
        if (TavernBarrelRecipes.fixedBatch(recipe) != null) cir.setReturnValue(recipe.result().copy());
    }
}
