package io.github.jasonsimpart.createdelightcore.compat.creategearsandtavern;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

@GameTestHolder("createdelightcore")
@PrefixGameTestTemplate(false)
public final class CgtJeiDisplayTests {
    @GameTest(template = "empty")
    public static void listKeepsOneBaseFluidAndPreservesSamples(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("creategearsandtavern")) {
            helper.succeed();
            return;
        }
        @SuppressWarnings("unchecked")
        List<FluidStack> samples = (List<FluidStack>) Class.forName("com.yision.creategearsandtavern.compat.jei.CGTJeiDrinkRecipes")
                .getMethod("fluidVariants").invoke(null);
        List<CompoundTag> originals = samples.stream().map(s -> s.writeToNBT(new CompoundTag())).toList();
        List<FluidStack> repeated = new ArrayList<>(samples);
        repeated.addAll(samples);
        repeated.add(FluidStack.EMPTY);
        var entries = CgtJeiFluidEntries.baseEntries(repeated);
        check(!entries.isEmpty() && entries.size() == samples.size(), "CGT list did not collapse brewing variants");
        for (FluidStack stack : entries) {
            check(!stack.hasTag() && stack.getAmount() == 1000, "CGT list retained NBT or changed representative volume");
        }
        for (int i = 0; i < samples.size(); i++) {
            check(originals.get(i).equals(samples.get(i).writeToNBT(new CompoundTag())), "CGT list mutated a real recipe sample");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void subtypeIgnoresOnlyCgtBrewingNbt(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("creategearsandtavern")) {
            helper.succeed();
            return;
        }
        Class<?> registrationType = Class.forName("mezz.jei.library.load.registration.SubtypeRegistration");
        ISubtypeRegistration registration = (ISubtypeRegistration) registrationType.getConstructor().newInstance();
        var vinegar = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("creategearsandtavern", "vinegar"));
        registration.registerSubtypeInterpreter(ForgeTypes.FLUID_STACK, vinegar,
                (stack, context) -> stack.hasTag() ? stack.getTag().toString() : "");
        Object interpreters = registrationType.getMethod("getInterpreters").invoke(registration);
        Class<?> managerType = Class.forName("mezz.jei.library.ingredients.subtypes.SubtypeManager");
        Object manager = Arrays.stream(managerType.getConstructors())
                .filter(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0].isInstance(interpreters))
                .findFirst().orElseThrow().newInstance(interpreters);
        Method subtype = managerType.getMethod("getSubtypeInfo", IIngredientTypeWithSubtypes.class, Object.class, UidContext.class);
        String base = (String) subtype.invoke(manager, ForgeTypes.FLUID_STACK, new FluidStack(vinegar, 1000), UidContext.Recipe);
        for (int level = 0; level <= 6; level++) {
            FluidStack stack = new FluidStack(vinegar, 1000);
            CompoundTag variant = new CompoundTag();
            variant.putString("drink_id", "kaleidoscope_tavern:vinegar");
            variant.putInt("brew_level", level);
            stack.getOrCreateTag().put("DrinkVariant", variant);
            CompoundTag before = stack.getTag().copy();
            check(base.equals(subtype.invoke(manager, ForgeTypes.FLUID_STACK, stack, UidContext.Recipe)), "CGT brewing levels split recipe lookup");
            check(before.equals(stack.getTag()), "subtype lookup mutated the real fluid");
            stack.getOrCreateTag().putInt("SignatureCocktailColor", 0x123456);
            String color = (String) subtype.invoke(manager, ForgeTypes.FLUID_STACK, stack, UidContext.Ingredient);
            stack.getOrCreateTag().putInt("SignatureCocktailColor", 0x654321);
            check(!color.equals(subtype.invoke(manager, ForgeTypes.FLUID_STACK, stack, UidContext.Ingredient)), "subtype lookup discarded cocktail data");
        }
        FluidStack water = new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000);
        water.getOrCreateTag().putString("DrinkVariant", "other mod data");
        check(CgtJeiFluidEntries.withoutBrewingVariant(water) == water, "CGT compatibility changed another namespace");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void fluidSubtypeRegistrationUsesOnePluginInEitherOrder(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("creategearsandtavern") || !ModList.get().isLoaded("quality_food_fluids")) {
            helper.succeed();
            return;
        }
        var cgt = (IModPlugin) Class.forName("com.yision.creategearsandtavern.compat.jei.CGTJeiPlugin")
                .getConstructor().newInstance();
        var qff = (IModPlugin) Class.forName("io.github.createdelight.qualityfoodfluids.compat.jei.QualityFoodFluidsJeiPlugin")
                .getConstructor().newInstance();
        for (var order : List.of(List.of(cgt, qff), List.of(qff, cgt))) {
            Map<Object, IModPlugin> owners = new IdentityHashMap<>();
            for (var plugin : order) {
                plugin.registerFluidSubtypes(new ISubtypeRegistration() {
                    @Override
                    public <B, I> void registerSubtypeInterpreter(IIngredientTypeWithSubtypes<B, I> type, B base,
                                                                  IIngredientSubtypeInterpreter<I> interpreter) {
                        check(owners.putIfAbsent(base, plugin) == null, "Fluid received two subtype interpreters: " + base);
                    }

                    @Override
                    public void useNbtForSubtypes(Item... items) {
                        throw new AssertionError("Expected explicit fluid subtype registration");
                    }

                    @Override
                    public void useNbtForSubtypes(Fluid... fluids) {
                        throw new AssertionError("Expected explicit fluid subtype registration");
                    }
                }, null);
            }
            for (String path : List.of("vinegar", "flowing_vinegar", "molotov", "flowing_molotov")) {
                var fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.fromNamespaceAndPath("creategearsandtavern", path));
                var expected = path.endsWith("vinegar") ? qff : cgt;
                check(owners.get(fluid) == expected, "Wrong subtype interpreter owner for " + path);
            }
        }
        helper.succeed();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
