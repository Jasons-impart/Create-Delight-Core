package io.github.jasonsimpart.mixin.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import io.github.jasonsimpart.compat.kubejs.KubeJsCreateRecipeJsonFixes;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = KubeRecipe.class, remap = false)
public abstract class KubeRecipeMixin {
    @Shadow
    public JsonObject json;

    @Shadow
    public abstract ResourceLocation getOrCreateId();

    @Inject(method = "serializeChanges", at = @At("RETURN"))
    private void createdelightcore$normalizeCreateRecipeJson(CallbackInfoReturnable<KubeRecipe> cir) {
        if (json != null) {
            KubeJsCreateRecipeJsonFixes.normalize(getOrCreateId(), json);
        }
    }
}
