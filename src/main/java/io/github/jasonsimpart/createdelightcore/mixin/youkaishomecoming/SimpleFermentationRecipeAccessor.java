package io.github.jasonsimpart.createdelightcore.mixin.youkaishomecoming;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;

@Pseudo
@Mixin(targets = "dev.xkmc.youkaishomecoming.content.pot.ferment.SimpleFermentationRecipe", remap = false)
public interface SimpleFermentationRecipeAccessor {
    @Accessor(value = "results", remap = false)
    ArrayList<ItemStack> createdelightcore$getResults();
}
