package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBrewing;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class TavernProductionGameTests {
    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void nonAgingRecipeStillWaitsForItsDeclaredYeast(GameTestHelper helper) {
        var recipe = new BarrelRecipe(id("createdelight:test/non_aging_yeast"),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.WHEAT)), Fluids.WATER,
                Ingredient.of(Items.GLASS_BOTTLE), new ItemStack(Items.STONE), 2);
        helper.assertTrue(!TavernQuality.canAge(recipe.result()), "Fixture does not participate in quality aging");
        withRecipes(helper, recipe, () -> {
            var barrel = barrel(helper);
            barrel.getFluid().fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE);
            helper.assertTrue(TavernBrewing.awaitingYeast(barrel, helper.getLevel()),
                    "Every recipe declaring yeast waits for starter, even when its product cannot age");
            barrel.tick(helper.getLevel());
            helper.assertTrue(barrel.getOutput().getStackInSlot(0).isEmpty()
                            && barrel.getFluid().getFluidAmount() == 4000,
                    "A non-aging recipe must not fall back to vinegar while waiting for yeast");
        });
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void incompleteYeastWaitsWithoutConsumingOrMakingVinegar(GameTestHelper helper) {
        var recipe = new BarrelRecipe(id("createdelight:test/yeast"), NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(Items.APPLE), Ingredient.of(Items.APPLE), Ingredient.of(Items.SUGAR), Ingredient.of(Items.WHEAT)),
                Fluids.WATER, Ingredient.of(Items.GLASS_BOTTLE), nativeWine(), 3);
        withRecipes(helper, recipe, () -> {
            for (int count : new int[]{0, 1, 15, 16}) {
                var barrel = barrel(helper);
                barrel.getIngredient().setStackInSlot(0, new ItemStack(Items.APPLE, 16));
                barrel.getIngredient().setStackInSlot(1, new ItemStack(Items.APPLE, 16));
                barrel.getIngredient().setStackInSlot(2, new ItemStack(Items.SUGAR, 16));
                barrel.getIngredient().setStackInSlot(3, count == 0 ? ItemStack.EMPTY : new ItemStack(Items.WHEAT, count));
                barrel.getFluid().fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE);
                for (int tick = 0; tick < 5; tick++) barrel.tick(helper.getLevel());
                var batch = ((TavernBarrelAccess) barrel).createdelightcore$getQualityBatch();
                if (count < 16) {
                    helper.assertTrue(batch == null && barrel.getOutput().getStackInSlot(0).isEmpty()
                                    && barrel.getFluid().getFluidAmount() == 4000
                                    && barrel.getIngredient().getStackInSlot(0).getCount() == 16,
                            "Correct wine ingredients wait intact for sixteen yeast; never fall back to vinegar at " + count);
                } else {
                    helper.assertTrue(batch != null && !batch.brewing() && batch.canAge()
                                    && barrel.getOutput().getStackInSlot(0).getCount() == 16
                                    && barrel.getOutput().getStackInSlot(0).is(nativeWine().getItem()),
                            "Sixteen yeast and two full apple slots produce sixteen bottles after initial brewing");
                }
            }
        });
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void externalWineUsesItsOwnNbtAndCanAgeFromFluidRecipes(GameTestHelper helper) {
        var cider = new ItemStack(ForgeRegistries.ITEMS.getValue(id("farmersdelight:apple_cider")));
        cider.getOrCreateTag().putString("CustomProducer", "test");
        for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
            var result = TavernQuality.qualify(cider, quality);
            helper.assertTrue(TavernQuality.canAge(result) && TavernQuality.supportsQuality(result)
                            && QualityUtils.getQuality(result) == quality
                            && !result.getOrCreateTag().contains("BrewLevel")
                            && result.getOrCreateTag().getString("CustomProducer").equals("test"),
                    "External wine retains unrelated NBT and receives only QF quality: " + quality);
        }
        var recipe = new BarrelRecipe(id("createdelight:test/external_yeast"),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.WHEAT)), Fluids.WATER,
                Ingredient.of(Items.GLASS_BOTTLE), cider, 2);
        withRecipes(helper, recipe, () -> {
            var barrel = barrel(helper);
            barrel.getIngredient().setStackInSlot(0, new ItemStack(Items.WHEAT, 16));
            barrel.getFluid().fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE);
            barrel.tick(helper.getLevel());
            var batch = ((TavernBarrelAccess) barrel).createdelightcore$getQualityBatch();
            helper.assertTrue(batch != null && batch.canAge() && batch.hasPendingOutput(),
                    "A fluid-based external wine enters the persisted delayed batch");
            var restored = barrel(helper);
            restored.load(barrel.saveWithoutMetadata());
            restored.tick(helper.getLevel());
            restored.tick(helper.getLevel());
            helper.assertTrue(restored.getOutput().getStackInSlot(0).getCount() == 16
                            && restored.getOutput().getStackInSlot(0).is(cider.getItem())
                            && !restored.getOutput().getStackInSlot(0).getOrCreateTag().contains("BrewLevel"),
                    "Saved external batches resume and finish without native bottle NBT");
        });
        helper.succeed();
    }

    private static BarrelBlockEntity barrel(GameTestHelper helper) {
        var block = ForgeRegistries.BLOCKS.getValue(id("kaleidoscope_tavern:barrel"));
        var result = new BarrelBlockEntity(helper.absolutePos(new BlockPos(1, 1, 1)), block.defaultBlockState());
        result.setLevel(helper.getLevel());
        result.setOpen(false);
        return result;
    }

    private static ItemStack nativeWine() {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(id("kaleidoscope_tavern:wine")));
    }

    private static void withRecipes(GameTestHelper helper, Recipe<?> addition, Runnable run) {
        var manager = helper.getLevel().getRecipeManager();
        var original = new ArrayList<Recipe<?>>(manager.getRecipes());
        var recipes = new ArrayList<>(original);
        recipes.add(addition);
        manager.replaceRecipes(recipes);
        try { run.run(); } finally { manager.replaceRecipes(original); }
    }

    private static ResourceLocation id(String value) { return new ResourceLocation(value); }
}
