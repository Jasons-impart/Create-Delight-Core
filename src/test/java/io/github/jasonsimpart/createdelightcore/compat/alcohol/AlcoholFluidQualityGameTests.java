package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class AlcoholFluidQualityGameTests {
    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void youkaiNativeBottleRoundTripPreservesQuality(GameTestHelper helper) throws Exception {
        var block = ForgeRegistries.BLOCKS.getValue(id("youkaishomecoming:fermentation_tank"));
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var open = (net.minecraft.world.level.block.state.properties.BooleanProperty) block.getStateDefinition().getProperty("open");
        helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState().setValue(open, true));
        var tile = helper.getLevel().getBlockEntity(pos);
        var tank = tile.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.getAbilities().instabuild = false;
        var item = ForgeRegistries.ITEMS.getValue(id("youkaishomecoming:mio"));
        for (Quality quality : new Quality[]{Quality.GOLD, Quality.NONE}) {
            player.getInventory().clearContent();
            var bottle = new ItemStack(item);
            if (quality != Quality.NONE) QualityUtils.applyQuality(bottle, quality);
            var emptied = FluidUtil.tryEmptyContainer(bottle, tank, 250, player, true);
            helper.assertTrue(emptied.isSuccess() && emptied.getResult().is(Items.GLASS_BOTTLE),
                    "Native bottle capability must empty and return glass");
            helper.assertTrue(QualityFoodFluidsApi.getQuality(tank.getFluidInTank(0)) == quality,
                    "Native bottle -> tank must preserve " + quality);
            var empty = new ItemStack(Items.GLASS_BOTTLE);
            player.setItemInHand(InteractionHand.MAIN_HAND, empty);
            helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            ItemStack output = player.getInventory().items.stream().filter(stack -> stack.is(item)).findFirst().orElse(ItemStack.EMPTY);
            helper.assertTrue(empty.isEmpty() && tank.getFluidInTank(0).isEmpty() && !output.isEmpty()
                            && QualityUtils.getQuality(output) == quality,
                    "Native tank -> bottle must preserve " + quality + " even after draining the last 250 mB");
        }
        player.getInventory().clearContent();
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void youkaiPartialBottleCapabilityKeepsFluidQuality(GameTestHelper helper) {
        var emptyBottle = ForgeRegistries.ITEMS.getValue(id("youkaishomecoming:sake_bottle"));
        for (Quality quality : new Quality[]{Quality.GOLD, Quality.NONE}) {
            var handler = new ItemStack(emptyBottle).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                    .orElseThrow(IllegalStateException::new);
            int filled = handler.fill(QualityFoodFluidsApi.withQuality(fluid("mio", 750), quality), IFluidHandler.FluidAction.EXECUTE);
            helper.assertTrue(filled == 750 && handler.getContainer().is(emptyBottle), "Use the native partial container");
            var drained = handler.drain(250, IFluidHandler.FluidAction.EXECUTE);
            var remaining = handler.getFluidInTank(0);
            helper.assertTrue(drained.getAmount() == 250 && remaining.getAmount() == 500
                            && QualityFoodFluidsApi.getQuality(drained) == quality
                            && QualityFoodFluidsApi.getQuality(remaining) == quality,
                    "Partial large-bottle draining keeps both fluid batches at " + quality);
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void removedYoukaiWineCupsAndBottlesCannotReceiveQuality(GameTestHelper helper) {
        for (String path : List.of("red_wine", "white_wine", "burgundy", "champagne", "van_allen")) {
            for (String suffix : List.of("", "_bottle")) {
                var stack = new ItemStack(ForgeRegistries.ITEMS.getValue(id("youkaishomecoming:" + path + suffix)));
                helper.assertTrue(!de.cadentem.quality_food.util.Utils.isValidItem(stack), "Removed wine is blacklisted: " + path + suffix);
                QualityUtils.applyQuality(stack, Quality.GOLD);
                helper.assertTrue(QualityUtils.getQuality(stack) == Quality.NONE, "Never grade a removed wine");
                helper.assertTrue(AlcoholDrinks.effects(stack).isEmpty(), "No managed drink profile for removed wine");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void cauldronBottleRoundTripPreservesQualityAndClearsPreviousBatch(GameTestHelper helper) {
        var item = ForgeRegistries.ITEMS.getValue(id("youkaishomecoming:mead"));
        var templateItem = new ItemStack(item);
        var templateFluid = fluid("mead", 250);
        var fill = new FillAlchemistCauldronRecipe(id("createdelight:test/alcohol_fill"), Ingredient.of(item),
                new ItemStack(Items.GLASS_BOTTLE), templateFluid, true,
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOTTLE_FILL));
        var empty = new EmptyAlchemistCauldronRecipe(id("createdelight:test/alcohol_empty"), Ingredient.of(Items.GLASS_BOTTLE),
                templateItem, templateFluid, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOTTLE_EMPTY));
        withRecipes(helper, List.of(fill, empty), () -> {
            var tile = cauldron(helper);
            for (Quality quality : new Quality[]{Quality.GOLD, Quality.NONE}) {
                var bottle = new ItemStack(item);
                if (quality != Quality.NONE) QualityUtils.applyQuality(bottle, quality);
                var remainder = tile.tryExecuteRecipeInteractions(helper.getLevel(), bottle);
                helper.assertTrue(remainder.is(Items.GLASS_BOTTLE), "Native cauldron emptying returns a glass bottle");
                var stored = tile.fluidInventory.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                helper.assertTrue(stored.getAmount() == 250 && QualityFoodFluidsApi.getQuality(stored) == quality,
                        "Bottle -> cauldron must keep " + quality);
                var output = tile.tryExecuteRecipeInteractions(helper.getLevel(), new ItemStack(Items.GLASS_BOTTLE));
                helper.assertTrue(output.is(item) && QualityUtils.getQuality(output) == quality,
                        "Qualified fluids must match native empty recipes and keep " + quality);
                helper.assertTrue(tile.fluidInventory.drain(1000, IFluidHandler.FluidAction.SIMULATE).isEmpty(),
                        "Drain the actual qualified batch");
            }
            helper.assertTrue(QualityUtils.getQuality(templateItem) == Quality.NONE
                    && QualityFoodFluidsApi.getQuality(templateFluid) == Quality.NONE, "Never mutate shared bottle/fluid templates");
        });
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void cauldronBrewingConsumesQualifiedInputsAndCopiesOutputTemplates(GameTestHelper helper) {
        var outputTemplate = QualityFoodFluidsApi.withQuality(fluid("mead", 250), Quality.GOLD);
        var recipe = new BrewAlchemistCauldronRecipe(id("createdelight:test/alcohol_brew"), fluid("mio", 250),
                Ingredient.of(Items.APPLE), List.of(outputTemplate), Optional.empty());
        withRecipes(helper, List.of(recipe), () -> {
            for (Quality quality : new Quality[]{Quality.NONE, Quality.GOLD}) {
                var tile = cauldron(helper);
                var source = QualityFoodFluidsApi.withQuality(fluid("mio", 250), quality);
                tile.fluidInventory.fill(source, IFluidHandler.FluidAction.EXECUTE);
                var ingredient = new ItemStack(Items.APPLE);
                tile.tryMeltInput(ingredient);
                var output = tile.fluidInventory.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                helper.assertTrue(ingredient.isEmpty() && output.getFluid() == outputTemplate.getFluid()
                        && output.getAmount() == 250, "Match and consume the actual " + quality + " input batch");
                if (quality == Quality.NONE) helper.assertTrue(QualityFoodFluidsApi.getQuality(output) == Quality.NONE,
                        "Unqualified ingredients must clear template quality");
                helper.assertTrue(QualityFoodFluidsApi.getQuality(outputTemplate) == Quality.GOLD, "Keep recipe template untouched");
            }
        });
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void youkaiSimpleFermentationInheritsAndClearsQuality(GameTestHelper helper) throws Exception {
        var block = ForgeRegistries.BLOCKS.getValue(id("youkaishomecoming:fermentation_tank"));
        helper.assertTrue(block instanceof EntityBlock, "Load the real Youkai fermentation tank");
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var tile = ((EntityBlock) block).newBlockEntity(pos, block.defaultBlockState());
        tile.setLevel(helper.getLevel());
        var tank = tile.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var recipeClass = Class.forName("dev.xkmc.youkaishomecoming.content.pot.ferment.SimpleFermentationRecipe");
        var recipe = recipeClass.getConstructor(ResourceLocation.class).newInstance(id("createdelight:test/simple_wine"));
        recipeClass.getField("inputFluid").set(recipe, fluid("mio", 1000));
        var template = QualityFoodFluidsApi.withQuality(fluid("mead", 1000), Quality.GOLD);
        recipeClass.getField("outputFluid").set(recipe, template);
        var finish = tile.getClass().getDeclaredMethod("finishRecipe", Level.class,
                Class.forName("dev.xkmc.youkaishomecoming.content.pot.ferment.FermentationRecipe"));
        finish.setAccessible(true);
        for (Quality quality : new Quality[]{Quality.GOLD, Quality.NONE}) {
            var source = QualityFoodFluidsApi.withQuality(fluid("mio", 1000), quality);
            tank.fill(source, IFluidHandler.FluidAction.EXECUTE);
            // Native BaseTank.fill consumes the supplied FluidStack; sample the actual stored batch.
            var stored = tank.getFluidInTank(0).copy();
            var random = qffRandom();
            random.setSeed(2468L);
            var expected = QualityFoodFluidsCreateRules.rollBasinQuality(List.of(), List.of(stored));
            helper.assertTrue(QualityFoodFluidsApi.getQuality(tank.getFluidInTank(0)) == quality,
                    "Native tank must retain the actual input quality before fermentation");
            random.setSeed(2468L);
            finish.invoke(tile, helper.getLevel(), recipe);
            var output = tank.getFluidInTank(0);
            helper.assertTrue(output.getAmount() == 1000 && output.getFluid() == template.getFluid()
                    && QualityFoodFluidsApi.getQuality(output) == expected, "Simple fermentation uses QFF for " + quality
                            + ": expected " + expected + ", got " + QualityFoodFluidsApi.getQuality(output)
                            + ", output " + output);
            tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        }
        helper.assertTrue(QualityFoodFluidsApi.getQuality(template) == Quality.GOLD, "Do not modify Youkai's shared output");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void fermentationUsesItemAndFluidInputsForBothOutputs(GameTestHelper helper) throws Exception {
        var block = (EntityBlock) ForgeRegistries.BLOCKS.getValue(id("youkaishomecoming:fermentation_tank"));
        var state = ForgeRegistries.BLOCKS.getValue(id("youkaishomecoming:fermentation_tank")).defaultBlockState();
        var tile = block.newBlockEntity(helper.absolutePos(new BlockPos(2, 1, 1)), state);
        tile.setLevel(helper.getLevel());
        var tank = tile.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var inventory = (net.minecraft.world.SimpleContainer) tile.getClass().getMethod("getItemHandler").invoke(tile);
        var recipeClass = Class.forName("dev.xkmc.youkaishomecoming.content.pot.ferment.SimpleFermentationRecipe");
        var recipe = recipeClass.getConstructor(ResourceLocation.class).newInstance(id("createdelight:test/mixed_fermentation"));
        recipeClass.getField("ingredients").set(recipe, new ArrayList<>(List.of(Ingredient.of(Items.APPLE))));
        recipeClass.getField("inputFluid").set(recipe, fluid("mio", 1000));
        var fluidTemplate = QualityFoodFluidsApi.withQuality(fluid("mead", 1000), Quality.GOLD);
        var itemTemplate = new ItemStack(Items.CARROT, 4);
        QualityUtils.applyQuality(itemTemplate, Quality.GOLD);
        recipeClass.getField("results").set(recipe, new ArrayList<>(List.of(itemTemplate)));
        recipeClass.getField("outputFluid").set(recipe, fluidTemplate);
        var finish = tile.getClass().getDeclaredMethod("finishRecipe", Level.class,
                Class.forName("dev.xkmc.youkaishomecoming.content.pot.ferment.FermentationRecipe"));
        finish.setAccessible(true);
        Quality[][] batches = {{Quality.GOLD, Quality.NONE}, {Quality.NONE, Quality.GOLD}, {Quality.NONE, Quality.NONE}};
        for (Quality[] batch : batches) {
            inventory.clearContent();
            var item = new ItemStack(Items.APPLE);
            if (batch[0] != Quality.NONE) QualityUtils.applyQuality(item, batch[0]);
            var fluid = QualityFoodFluidsApi.withQuality(fluid("mio", 1000), batch[1]);
            inventory.setItem(0, item.copy());
            tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            var stored = tank.getFluidInTank(0).copy();
            var random = qffRandom();
            // Pick a repeatable QFF roll which actually produces a star for a qualified batch.
            long seed = 0;
            Quality expected;
            do {
                random.setSeed(seed++);
                expected = QualityFoodFluidsCreateRules.rollBasinQuality(List.of(item.copy()), List.of(stored));
            } while (expected == Quality.NONE && (batch[0] != Quality.NONE || batch[1] != Quality.NONE) && seed < 10000);
            helper.assertTrue(seed < 10000, "Qualified batch has an available QFF quality roll");
            random.setSeed(seed - 1);
            finish.invoke(tile, helper.getLevel(), recipe);
            helper.assertTrue(QualityFoodFluidsApi.getQuality(tank.getFluidInTank(0)) == expected,
                    "Fluid output uses actual item and fluid inputs: expected " + expected + ", got "
                            + QualityFoodFluidsApi.getQuality(tank.getFluidInTank(0)) + ", item " + batch[0] + ", fluid " + batch[1]);
            int carrots = 0;
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                var output = inventory.getItem(slot);
                if (output.is(Items.CARROT)) {
                    carrots += output.getCount();
                    helper.assertTrue(QualityUtils.getQuality(output) == expected, "Item output shares the batch roll and clears old quality");
                }
                helper.assertTrue(!output.is(Items.APPLE), "The native recipe consumed the actual item input");
            }
            helper.assertTrue(carrots == 4, "Keep native result counts");
            tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        }
        helper.assertTrue(QualityUtils.getQuality(itemTemplate) == Quality.GOLD
                && QualityFoodFluidsApi.getQuality(fluidTemplate) == Quality.GOLD, "Do not mutate shared fermentation templates");
        helper.succeed();
    }

    private static RandomSource qffRandom() throws Exception {
        var field = QualityFoodFluidsCreateRules.class.getDeclaredField("RANDOM");
        field.setAccessible(true);
        return (RandomSource) field.get(null);
    }

    private static AlchemistCauldronTile cauldron(GameTestHelper helper) {
        var tile = new AlchemistCauldronTile(helper.absolutePos(new BlockPos(1, 1, 1)), Blocks.CAULDRON.defaultBlockState());
        tile.setLevel(helper.getLevel());
        return tile;
    }

    private static FluidStack fluid(String path, int amount) {
        return new FluidStack(ForgeRegistries.FLUIDS.getValue(id("youkaishomecoming:" + path)), amount);
    }

    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }

    private static void withRecipes(GameTestHelper helper, List<Recipe<?>> additions, Runnable test) {
        var manager = helper.getLevel().getRecipeManager();
        var original = new ArrayList<Recipe<?>>(manager.getRecipes());
        var recipes = new ArrayList<>(original);
        recipes.addAll(additions);
        manager.replaceRecipes(recipes);
        try { test.run(); } finally { manager.replaceRecipes(original); }
    }
}
