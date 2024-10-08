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
        List<Fluid> superHeatFluids = new ArrayList<>();
//        superHeatFluids.add(AllFluids.CHOCOLATE.get());
//        superHeatFluids.add(Fluids.WATER.getSource());
        BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.forEach((fluid, pair) -> {
            if(pair != null){
                var a = pair.getSecond();
                if(a != null){
                    Boolean b  = a.getSecond();
                    if(b != null && b){
                        superHeatFluids.add(fluid);
                    }
                }
            }
        });

        System.out.println("-------------");
        System.out.println(superHeatFluids.size());

        registration.addRecipes(JeiCategoryBlazeBurnerSuperHeat.RECIPE_TYPE, superHeatFluids);
    }
}
