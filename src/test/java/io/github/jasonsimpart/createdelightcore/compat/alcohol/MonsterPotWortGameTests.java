package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.yirmiri.dungeonsdelight.common.block.entity.MonsterPotBlockEntity;
import net.yirmiri.dungeonsdelight.common.block.entity.container.MonsterPotRecipe;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class MonsterPotWortGameTests {
    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void bucketBatchRequiresFourServingsContainerAndFreeOutput(GameTestHelper helper) throws Exception {
        var pot = new MonsterPotBlockEntity(helper.absolutePos(new BlockPos(1, 1, 1)), Blocks.CAULDRON.defaultBlockState());
        pot.setLevel(helper.getLevel());
        var bucket = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("kaleidoscope_tavern", "grape_juice")).getBucket();
        var template = QualityFoodFluidsCreateRules.applyItemOutputQuality(Quality.GOLD, new ItemStack(bucket));
        var ingredients = NonNullList.withSize(6, Ingredient.of(Items.APPLE));
        var recipe = new MonsterPotRecipe(new ResourceLocation("createdelight", "alcohol_preparation/ghastly_spirits"),
                "", CookingPotRecipeBookTab.MISC, ingredients, template, new ItemStack(Items.BUCKET), 1, 80);
        var canCook = MonsterPotBlockEntity.class.getDeclaredMethod("canCook", MonsterPotRecipe.class);
        var process = MonsterPotBlockEntity.class.getDeclaredMethod("processCooking", MonsterPotRecipe.class, MonsterPotBlockEntity.class);
        var transfer = MonsterPotBlockEntity.class.getDeclaredMethod("useStoredContainersOnMeal");
        canCook.setAccessible(true);
        process.setAccessible(true);
        transfer.setAccessible(true);
        var inventory = pot.getInventory();
        for (int slot = 0; slot < 6; slot++) inventory.setStackInSlot(slot, new ItemStack(Items.APPLE, 4));
        helper.assertTrue(!(boolean) canCook.invoke(pot, recipe), "Do not cook a bucket without its empty container");
        inventory.setStackInSlot(7, new ItemStack(Items.BUCKET));
        inventory.setStackInSlot(0, new ItemStack(Items.APPLE, 3));
        helper.assertTrue(!(boolean) canCook.invoke(pot, recipe), "Three servings cannot produce a one-litre bucket");
        inventory.setStackInSlot(0, new ItemStack(Items.APPLE, 4));
        inventory.setStackInSlot(8, new ItemStack(Items.STONE));
        helper.assertTrue(!(boolean) canCook.invoke(pot, recipe), "Blocked output preserves all ingredients");
        inventory.setStackInSlot(8, ItemStack.EMPTY);
        helper.assertTrue((boolean) canCook.invoke(pot, recipe), "Four full servings, bucket and empty output permit cooking");
        for (int tick = 0; tick < 79; tick++) process.invoke(pot, recipe, pot);
        helper.assertTrue(inventory.getStackInSlot(0).getCount() == 4 && inventory.getStackInSlot(6).isEmpty(),
                "No materials are consumed before the eighty-tick deadline");
        process.invoke(pot, recipe, pot);
        for (int slot = 0; slot < 6; slot++) helper.assertTrue(inventory.getStackInSlot(slot).isEmpty(), "Consume four from each input slot");
        helper.assertTrue(inventory.getStackInSlot(6).getCount() == 1
                        && QualityUtils.getQuality(inventory.getStackInSlot(6)) == Quality.NONE
                        && QualityUtils.getQuality(template) == Quality.GOLD,
                "Ordinary input clears template quality on a copy, without contaminating later batches");
        transfer.invoke(pot);
        helper.assertTrue(inventory.getStackInSlot(7).isEmpty() && inventory.getStackInSlot(6).isEmpty()
                        && inventory.getStackInSlot(8).is(bucket)
                        && FluidUtil.getFluidContained(inventory.getStackInSlot(8)).orElseThrow().getAmount() == 1000,
                "One actual empty bucket becomes exactly one litre, with no free container");
        helper.succeed();
    }
}
