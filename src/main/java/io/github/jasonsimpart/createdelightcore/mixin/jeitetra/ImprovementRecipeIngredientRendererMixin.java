package io.github.jasonsimpart.createdelightcore.mixin.jeitetra;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import se.mickelus.tetra.items.modular.IModularItem;

@Pseudo
@Mixin(targets = "net.yiran.jeitetra.ingredient.renderer.ImprovementRecipeIngredientRenderer", remap = false)
public abstract class ImprovementRecipeIngredientRendererMixin {
    @Redirect(
            method = {
                    "render(Lnet/minecraft/client/gui/GuiGraphics;Lse/mickelus/tetra/module/data/ImprovementData;)V",
                    "getTooltip(Lse/mickelus/tetra/module/data/ImprovementData;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lse/mickelus/tetra/items/modular/IModularItem;getImprovementName(Ljava/lang/String;I)Ljava/lang/String;",
                    remap = false
            ),
            require = 2
    )
    private String createdelightcore$getImprovementName(String key, int level) {
        return IModularItem.getImprovementName(key, level, ItemStack.EMPTY);
    }
}
