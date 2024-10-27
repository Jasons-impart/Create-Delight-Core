package io.github.jasonsimpart.createdelightcore.jei;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JeiPage implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return Create.asResource("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
            registration.addRecipeCategories(new JeiCategoryBlazeBurnerSuperHeat(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<JeiCategoryBlazeBurnerSuperHeat.BlazeBurnerRecipe> superHeatFluids = new ArrayList<>();
        //test
        //superHeatFluids.add(new JeiCategoryBlazeBurnerSuperHeat.BlazeBurnerRecipe(AllFluids.CHOCOLATE.get(), 20, 10));
        //superHeatFluids.add(new JeiCategoryBlazeBurnerSuperHeat.BlazeBurnerRecipe(Fluids.WATER.getSource(), 32, 16));
        BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.forEach((fluid, pair) -> {
            if(pair != null){
                var a = pair.getSecond();
                if(a != null){
                    Boolean b  = a.getSecond();
                    if(b != null && b && a.getFirst() != null && a.getThird() != null){
                        superHeatFluids.add(new JeiCategoryBlazeBurnerSuperHeat.BlazeBurnerRecipe(fluid, a.getFirst(), a.getThird()));
                    }
                }
            }
        });
        registration.addRecipes(JeiCategoryBlazeBurnerSuperHeat.RECIPE_TYPE, superHeatFluids);
    }
}
