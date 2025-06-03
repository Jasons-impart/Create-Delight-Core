package io.github.jasonsimpart.createdelightcore.mixin.createore;

import com.tom.createores.jei.ExcavatingCategory;
import com.tom.createores.recipe.ExcavatingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExcavatingCategory.class, remap = false)
public class ExcavatingCategoryMixin {
    @Inject(method = "draw(Ljava/lang/Object;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V", at = @At(value = "TAIL"), require = 1)
    public void setRecipeMixin(Object recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY, CallbackInfo ci) {
        if (recipe instanceof ExcavatingRecipe excavatingRecipe) {

            stack.drawString(Minecraft.getInstance().font, excavatingRecipe.getStress() +
                    Component.translatable("create.generic.unit.stress")
                            .append("/")
                            .append(Component.translatable("create.generic.unit.rpm")).getString(), 90, 46, 0x808080);
        }
    }
}
