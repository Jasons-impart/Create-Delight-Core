package io.github.jasonsimpart.createdelightcore.compat.jei;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.simibubi.create.AllBlocks;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.jei.category.JeiCategoryBlazeBurnerFluid;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JeiPage implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return CreateDelightCore.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JeiCategoryBlazeBurnerFluid(registration.getJeiHelpers()));
        // registration.addRecipeCategories();
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(AllBlocks.BLAZE_BURNER.asStack(), JeiCategoryBlazeBurnerFluid.RECIPE_TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe> heatFluids = new ArrayList<>();
        //test
        // heatFluids.add(new JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe(AllFluids.CHOCOLATE.get(), 20, 10));
        // heatFluids.add(new JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe(Fluids.WATER.getSource(), 32, 16));
        
        BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.forEach((fluid, item) -> {
            // item: <ResourceLocation, info: <burnTime: int, isSuperHeat: bool, amountConsume: int>>
            if(item != null){
                var info = item.getSecond();
                if(info != null){
                    Boolean isSuperHeat = info.getSecond();
                    Integer burnTime = info.getFirst();
                    Integer amountConsume = info.getThird();
                    if(isSuperHeat != null && burnTime != null && amountConsume != null){
                        heatFluids.add(new JeiCategoryBlazeBurnerFluid.BlazeBurnerFluidRecipe(fluid, isSuperHeat, burnTime, amountConsume));
                    }
                }
            }
        });
        registration.addRecipes(JeiCategoryBlazeBurnerFluid.RECIPE_TYPE, heatFluids);
    }
}
