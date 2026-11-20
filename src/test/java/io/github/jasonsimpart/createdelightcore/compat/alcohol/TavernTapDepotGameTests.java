package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelBatch;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernTapDepot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class TavernTapDepotGameTests {
    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void singleNativeBottlePreservesEveryQualityAndResetsTheEmptyBarrel(GameTestHelper helper) {
        var depot = depot(helper);
        var recipe = recipe(new ItemStack(ModItems.WINE.get()), Ingredient.of(ModItems.EMPTY_BOTTLE.get()));
        withRecipe(helper, recipe, () -> {
            for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
                var barrel = barrel(helper, recipe, 1, quality, 0);
                depot.setHeldItem(new ItemStack(ModItems.EMPTY_BOTTLE.get()));
                helper.assertTrue(barrel.canTapExtract(helper.getLevel(), tapPos(depot), null),
                        "A compatible depot carrier passes the native tap gates");
                barrel.doTapExtract(helper.getLevel(), tapPos(depot));
                ItemStack filled = depot.getHeldItem();
                helper.assertTrue(filled.is(ModItems.WINE.get()) && filled.getCount() == 1
                                && QualityUtils.getQuality(filled) == quality,
                        "A single native bottle is replaced by one wine of the batch quality: " + quality);
                helper.assertTrue(barrel.getOutput().getStackInSlot(0).isEmpty() && barrel.getBrewLevel() == 0
                                && ((TavernBarrelAccess) barrel).createdelightcore$getQualityBatch() == null,
                        "The last bottle is qualified before the barrel resets");
            }
        });
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void stackedExternalCarriersKeepRemaindersAndWaitForBufferedOutput(GameTestHelper helper) {
        var depot = depot(helper);
        var product = new ItemStack(ForgeRegistries.ITEMS.getValue(id("farmersdelight:apple_cider")));
        product.getOrCreateTag().putString("Producer", "depot-test");
        var recipe = recipe(product, Ingredient.of(Items.GLASS_BOTTLE));
        var barrel = barrel(helper, recipe, 2, Quality.GOLD, 0);
        withRecipe(helper, recipe, () -> {
            depot.setHeldItem(new ItemStack(Items.GLASS_BOTTLE, 3));
            helper.assertTrue(barrel.canTapExtract(helper.getLevel(), tapPos(depot), null), "Stacked glass bottles are accepted");
            barrel.doTapExtract(helper.getLevel(), tapPos(depot));
            helper.assertTrue(depot.getHeldItem().is(Items.GLASS_BOTTLE) && depot.getHeldItem().getCount() == 2
                            && barrel.getOutput().getStackInSlot(0).getCount() == 1,
                    "Each native pour consumes exactly one bottle and one serving");
            helper.assertTrue(!barrel.canTapExtract(helper.getLevel(), tapPos(depot), null),
                    "Buffered output prevents another pour until collected");
            barrel.doTapExtract(helper.getLevel(), tapPos(depot));
            helper.assertTrue(depot.getHeldItem().getCount() == 2 && barrel.getOutput().getStackInSlot(0).getCount() == 1,
                    "A completed pour also rechecks output space before extracting");

            depot.load(depot.saveWithoutMetadata());
            ItemStack first = takeBufferedOutput(depot);
            assertExternalOutput(helper, first, product);
            helper.assertTrue(depot.getHeldItem().getCount() == 2
                            && barrel.canTapExtract(helper.getLevel(), tapPos(depot), null),
                    "Depot persistence keeps both carriers and buffered wine; collection permits the next pour");
            barrel.doTapExtract(helper.getLevel(), tapPos(depot));
            assertExternalOutput(helper, takeBufferedOutput(depot), product);
            helper.assertTrue(depot.getHeldItem().is(Items.GLASS_BOTTLE) && depot.getHeldItem().getCount() == 1
                            && barrel.getBrewLevel() == 0 && barrel.getRecipeId() == null,
                    "Two pours leave the third carrier intact and clear the exhausted source");
            barrel.doTapExtract(helper.getLevel(), tapPos(depot));
            helper.assertTrue(depot.getHeldItem().getCount() == 1 && takeBufferedOutput(depot).isEmpty(),
                    "An exhausted source neither duplicates wine nor consumes another carrier");
        });
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void invalidOrChangedCarriersAndEmptyExtractionDoNotConsumeAnything(GameTestHelper helper) {
        var depot = depot(helper);
        var recipe = recipe(new ItemStack(ModItems.WINE.get()), Ingredient.of(ModItems.EMPTY_BOTTLE.get()));
        var barrel = barrel(helper, recipe, 2, Quality.IRON, 0);
        withRecipe(helper, recipe, () -> {
            for (ItemStack invalid : List.of(ItemStack.EMPTY, new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.APPLE))) {
                depot.setHeldItem(invalid.copy());
                helper.assertTrue(!barrel.canTapExtract(helper.getLevel(), tapPos(depot), null),
                        "The depot must hold the current recipe's exact carrier");
                barrel.doTapExtract(helper.getLevel(), tapPos(depot));
                helper.assertTrue(ItemStack.matches(depot.getHeldItem(), invalid)
                                && barrel.getOutput().getStackInSlot(0).getCount() == 2,
                        "Missing or incompatible carriers leave both inventories intact");
            }
            depot.setHeldItem(new ItemStack(ModItems.EMPTY_BOTTLE.get(), 2));
            helper.assertTrue(barrel.canTapExtract(helper.getLevel(), tapPos(depot), null), "A valid carrier can open the tap");
            depot.setHeldItem(new ItemStack(Items.APPLE));
            barrel.doTapExtract(helper.getLevel(), tapPos(depot));
            helper.assertTrue(depot.getHeldItem().is(Items.APPLE) && barrel.getOutput().getStackInSlot(0).getCount() == 2,
                    "Changing the receiver while the tap is open cannot consume wine");
            depot.setHeldItem(new ItemStack(ModItems.EMPTY_BOTTLE.get(), 2));
            TavernTapDepot.fill(depot, recipe.carrier(), () -> ItemStack.EMPTY);
            helper.assertTrue(depot.getHeldItem().getCount() == 2 && takeBufferedOutput(depot).isEmpty(),
                    "A failed native extraction never consumes the carrier");
        });
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void unfinishedBrewingCannotPourIntoDepotAndResumesAfterCompletion(GameTestHelper helper) {
        var depot = depot(helper);
        var recipe = recipe(new ItemStack(ModItems.WINE.get()), Ingredient.of(ModItems.EMPTY_BOTTLE.get()));
        var barrel = barrel(helper, recipe, 2, Quality.IRON, 3);
        withRecipe(helper, recipe, () -> {
            depot.setHeldItem(new ItemStack(ModItems.EMPTY_BOTTLE.get(), 2));
            helper.assertTrue(!barrel.canTapExtract(helper.getLevel(), tapPos(depot), null), "Unfinished wine cannot open the tap");
            barrel.doTapExtract(helper.getLevel(), tapPos(depot));
            helper.assertTrue(depot.getHeldItem().getCount() == 2 && takeBufferedOutput(depot).isEmpty(),
                    "An attempted premature pour keeps its containers intact");
            barrel.setOpen(true);
            barrel.tick(helper.getLevel());
            helper.assertTrue(((TavernBarrelAccess) barrel).createdelightcore$getQualityBatch().brewingTicks() == 3,
                    "An open lid still pauses the batch");
            barrel.setOpen(false);
            for (int tick = 0; tick < 3; tick++) barrel.tick(helper.getLevel());
            helper.assertTrue(barrel.canTapExtract(helper.getLevel(), tapPos(depot), null),
                    "Completing brewing enables the existing tap operation");
            barrel.doTapExtract(helper.getLevel(), tapPos(depot));
            helper.assertTrue(depot.getHeldItem().getCount() == 1
                            && QualityUtils.getQuality(takeBufferedOutput(depot)) == Quality.IRON,
                    "The first completed serving inherits the persisted batch quality");
        });
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void anEmptyDepotKeepsTheNativeDroppedCarrierPath(GameTestHelper helper) {
        var depot = depot(helper);
        var recipe = recipe(new ItemStack(ModItems.WINE.get()), Ingredient.of(ModItems.EMPTY_BOTTLE.get()));
        var barrel = barrel(helper, recipe, 2, Quality.IRON, 0);
        withRecipe(helper, recipe, () -> {
            var pos = depot.getBlockPos();
            var dropped = new ItemEntity(helper.getLevel(), pos.getX() + .5, pos.getY() + .8, pos.getZ() + .5,
                    new ItemStack(ModItems.EMPTY_BOTTLE.get()));
            helper.getLevel().addFreshEntity(dropped);
            helper.assertTrue(barrel.canTapExtract(helper.getLevel(), tapPos(depot), null),
                    "An empty depot does not intercept the native dropped-carrier lookup");
            barrel.doTapExtract(helper.getLevel(), tapPos(depot));
            var products = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1),
                    entity -> entity.getItem().is(ModItems.WINE.get()));
            helper.assertTrue(dropped.isRemoved() && depot.getHeldItem().isEmpty()
                            && barrel.getOutput().getStackInSlot(0).getCount() == 1
                            && products.size() == 1 && QualityUtils.getQuality(products.get(0).getItem()) == Quality.IRON,
                    "The native fallback fills exactly one dropped bottle without changing depot storage");
            products.forEach(ItemEntity::discard);
        });
        helper.succeed();
    }

    private static void assertExternalOutput(GameTestHelper helper, ItemStack result, ItemStack product) {
        helper.assertTrue(result.is(product.getItem()) && result.getCount() == 1
                        && QualityUtils.getQuality(result) == Quality.GOLD
                        && result.getOrCreateTag().getString("Producer").equals("depot-test")
                        && !result.getOrCreateTag().contains("BrewLevel"),
                "Buffered external wine preserves its own NBT and QF quality without native BrewLevel");
    }

    private static DepotBlockEntity depot(GameTestHelper helper) {
        var relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, AllBlocks.DEPOT.getDefaultState());
        return (DepotBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
    }

    private static BlockPos tapPos(DepotBlockEntity depot) { return depot.getBlockPos().above(); }

    private static ItemStack takeBufferedOutput(DepotBlockEntity depot) {
        IItemHandler inventory = depot.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(AssertionError::new);
        for (int slot = 1; slot < inventory.getSlots(); slot++) {
            ItemStack result = inventory.extractItem(slot, 64, false);
            if (!result.isEmpty()) return result;
        }
        return ItemStack.EMPTY;
    }

    private static BarrelRecipe recipe(ItemStack output, Ingredient carrier) {
        return new BarrelRecipe(id("createdelight:test/tap_depot"), NonNullList.create(), Fluids.WATER, carrier, output, 3);
    }

    private static BarrelBlockEntity barrel(GameTestHelper helper, BarrelRecipe recipe, int count, Quality quality, int brewing) {
        var block = ForgeRegistries.BLOCKS.getValue(id("kaleidoscope_tavern:barrel"));
        var barrel = new BarrelBlockEntity(helper.absolutePos(new BlockPos(3, 1, 1)), block.defaultBlockState());
        barrel.setLevel(helper.getLevel());
        var batch = new TavernBarrelBatch(quality, brewing, true);
        ItemStack output = TavernQuality.qualify(recipe.result(), quality);
        output.setCount(count);
        if (brewing > 0) batch.deferOutput(output);
        else barrel.getOutput().setStackInSlot(0, output);
        var tag = barrel.saveWithoutMetadata();
        tag.putBoolean("open", false);
        tag.putInt("brew_level", brewing > 0 ? 1 : TavernQuality.NORMAL_BREW_LEVEL);
        tag.putString("recipe_id", recipe.getId().toString());
        var batchTag = batch.save();
        if (quality == Quality.DIAMOND) {
            batchTag.putBoolean("aged", true);
            batchTag.putInt("remaining_ticks", 0);
        }
        tag.put(TavernBarrelBatch.NBT_KEY, batchTag);
        barrel.load(tag);
        return barrel;
    }

    private static void withRecipe(GameTestHelper helper, Recipe<?> addition, Runnable action) {
        var manager = helper.getLevel().getRecipeManager();
        var original = new ArrayList<Recipe<?>>(manager.getRecipes());
        var recipes = new ArrayList<>(original);
        recipes.add(addition);
        manager.replaceRecipes(recipes);
        try { action.run(); } finally { manager.replaceRecipes(original); }
    }

    private static ResourceLocation id(String value) { return new ResourceLocation(value); }
}
