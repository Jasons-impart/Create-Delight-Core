package io.github.jasonsimpart.createdelightcore.compat.creategearsandtavern;

import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@GameTestHolder("createdelightcore")
@PrefixGameTestTemplate(false)
public final class CgtJeiRecipeTests {
    @GameTest(template = "humidity_regulator_empty")
    public static void actualCgtJeiRecipesUseOnePlainFluidPerDrink(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("creategearsandtavern")) {
            helper.succeed();
            return;
        }
        // The GameTest run adds a test-only common mixin configuration at
        // startup to exercise the production client injection on the real class.
        Class<?> cgtRecipes = Class.forName("com.yision.creategearsandtavern.compat.jei.CGTJeiDrinkRecipes");
        RecipeType<FillingRecipe> fillingType = new RecipeType<>(new ResourceLocation("create", "spout_filling"), FillingRecipe.class);
        RecipeType<EmptyingRecipe> emptyingType = new RecipeType<>(new ResourceLocation("create", "draining"), EmptyingRecipe.class);
        Map<RecipeType<?>, List<?>> recipes = new HashMap<>();
        IRecipeRegistration registration = (IRecipeRegistration) Proxy.newProxyInstance(
                IRecipeRegistration.class.getClassLoader(), new Class<?>[]{IRecipeRegistration.class}, (proxy, method, args) -> {
                    if (method.getName().equals("addRecipes")) {
                        recipes.put((RecipeType<?>) args[0], List.copyOf((List<?>) args[1]));
                        return null;
                    }
                    throw new AssertionError("unexpected JEI registration call " + method.getName());
                });
        cgtRecipes.getMethod("registerRecipes", IRecipeRegistration.class, RecipeType.class, RecipeType.class)
                .invoke(cgtRecipes.getConstructor().newInstance(), registration, fillingType, emptyingType);
        List<?> fillings = recipes.get(fillingType);
        List<?> emptyings = recipes.get(emptyingType);
        long drinks = ForgeRegistries.FLUIDS.getKeys().stream()
                .filter(id -> id.getNamespace().equals("creategearsandtavern") && !id.getPath().startsWith("flowing_")
                        && !id.getPath().equals("vinegar")).count();
        check(drinks > 0 && fillings.size() == drinks && emptyings.size() == drinks,
                "CGT still registers multiple brewing recipes per drink: " + fillings.size() + "/" + emptyings.size() + "/" + drinks);
        Map<ResourceLocation, Integer> amounts = new HashMap<>();
        for (Object value : fillings) {
            FillingRecipe recipe = (FillingRecipe) value;
            List<FluidStack> inputs = recipe.getFluidIngredients().get(0).getMatchingFluidStacks();
            check(inputs.size() == 1, "filling recipe retained alternative fluid stacks");
            FluidStack fluid = inputs.get(0);
            check(!fluid.hasTag() && fluid.getAmount() > 0, "filling fluid retained brewing, quality, or effect NBT");
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
            check(!new ResourceLocation("creategearsandtavern", "vinegar").equals(id),
                    "The legacy vinegar should not have a new JEI filling recipe");
            check(amounts.put(id, fluid.getAmount()) == null, "duplicate filling fluid " + id);
        }
        Set<ResourceLocation> emptied = new HashSet<>();
        for (Object value : emptyings) {
            EmptyingRecipe recipe = (EmptyingRecipe) value;
            check(recipe.getFluidResults().size() == 1, "emptying recipe retained alternative outputs");
            FluidStack fluid = recipe.getFluidResults().get(0);
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
            check(emptied.add(id), "duplicate emptying fluid " + id);
            check(!fluid.hasTag() && amounts.get(id) != null && fluid.getAmount() == amounts.get(id),
                    "emptying fluid retained NBT or changed bottle volume for " + id);
        }

        ItemStack aged = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("kaleidoscope_tavern", "plum_wine")));
        check(!aged.isEmpty(), "missing real drink fixture");
        Class.forName("com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem")
                .getMethod("setBrewLevel", ItemStack.class, int.class).invoke(null, aged, 6);
        CompoundTag before = aged.save(new CompoundTag()).copy();
        FluidStack real = (FluidStack) Class.forName("com.yision.creategearsandtavern.content.fluids.drink.DrinkFluidConversions")
                .getMethod("fromItem", ItemStack.class).invoke(null, aged);
        check(real.hasTag() && real.getTag().getCompound("DrinkVariant").getInt("brew_level") == 6,
                "JEI compatibility changed the real aged drink conversion");
        FluidStack plain = CgtJeiFluidEntries.baseRecipeFluid(real);
        check(!plain.hasTag() && plain.getAmount() == real.getAmount() && real.hasTag(), "display conversion changed real fluid data");
        check(before.equals(aged.save(new CompoundTag())), "JEI compatibility changed the real bottle");
        System.out.println("CGT JEI recipes verified: " + drinks + " plain filling + " + drinks + " plain emptying; real brewing NBT preserved");
        helper.succeed();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
