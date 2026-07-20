package io.github.jasonsimpart.createdelightcore.compat.jei;

import io.github.jasonsimpart.createdelightcore.content.fluid.GeneticCultureFluidType;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;

public class GeneticCultureFluidSubtypeInterpreter implements IIngredientSubtypeInterpreter<FluidStack> {
    @Override
    public String apply(FluidStack ingredient, UidContext context) {
        CompoundTag tag = ingredient.getTag();
        if (tag == null || tag.isEmpty()) {
            return NONE;
        }

        Tag colorTag = tag.get(GeneticCultureFluidType.COLOR_KEY);
        String color = colorTag == null ? "" : colorTag.getAsString();
        return tag.getString(GeneticCultureFluidType.VARIANT_KEY)
                + ";" + color
                + ";" + tag.getString(GeneticCultureFluidType.NAME_KEY)
                + ";" + tag.getString(GeneticCultureFluidType.CUSTOM_NAME_KEY);
    }
}
