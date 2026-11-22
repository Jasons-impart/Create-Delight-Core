package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelBatch;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelFluidOutput;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import io.github.jasonsimpart.createdelightcore.mixin.create.DepotOutputBufferAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class TavernFluidOutputGameTests {
    static final String CIDER_ITEM = "farmersdelight:apple_cider";
    static final String CIDER_FLUID = "farmersrespite:apple_cider";
    static final String GLASS = "minecraft:glass_bottle";
    private static ItemStack earlyCider;

    @Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Fixtures {
        @SubscribeEvent
        public static void register(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                earlyCider = cider();
                ItemFluidContainers.register(CIDER_ITEM, CIDER_FLUID, 250, GLASS);
                ItemFluidContainers.register("minecraft:carrot", CIDER_FLUID, 100, GLASS);
                ItemFluidContainers.register("minecraft:golden_carrot", CIDER_FLUID, 500, GLASS);
                ItemFluidContainers.registerIfPresent("minecraft:beetroot", "createdelight:test_missing_fluid", 250, GLASS);
                ItemFluidContainers.registerIfPresent("youkaishomecoming:mead", "youkaishomecoming:mead", 250, GLASS);
            });
        }
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void fixedBottlesDrainWholeServingsAndKeepExistingCapabilities(GameTestHelper helper) {
        helper.assertTrue(earlyCider.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent(),
                "Bottles built before registration was frozen get the fallback on query");
        ItemFluidContainers.register(CIDER_ITEM, CIDER_FLUID, 250, GLASS);
        helper.assertTrue(ItemFluidContainers.find(new ItemStack(Items.BEETROOT)) == null
                        && invalid(() -> ItemFluidContainers.register(CIDER_ITEM, CIDER_FLUID, 249, GLASS))
                        && invalid(() -> ItemFluidContainers.register(CIDER_ITEM, CIDER_FLUID, 0, GLASS))
                        && invalid(() -> ItemFluidContainers.register(CIDER_ITEM, CIDER_FLUID, 1.5, GLASS))
                        && invalid(() -> ItemFluidContainers.registerIfPresent("invalid$id", CIDER_FLUID, 250, GLASS)),
                "Optional missing entries are skipped and invalid or conflicting entries are rejected");
        for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
            ItemStack bottle = TavernQuality.qualify(cider(), quality);
            CompoundTag before = bottle.save(new CompoundTag());
            var handler = bottle.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(AssertionError::new);
            FluidStack held = handler.getFluidInTank(0);
            helper.assertTrue(held.getFluid() == ciderFluid() && held.getAmount() == 250
                            && QualityFoodFluidsApi.getQuality(held) == quality && handler.getTankCapacity(0) == 250
                            && handler.getFluidInTank(1).isEmpty() && handler.fill(held, FluidAction.EXECUTE) == 0,
                    "The fixed bottle exposes its registered serving and exact quality");
            FluidStack wrongNbt = held.copy();
            wrongNbt.getOrCreateTag().putString("different", "identity");
            helper.assertTrue(handler.drain(249, FluidAction.EXECUTE).isEmpty()
                            && handler.drain(wrongNbt, FluidAction.EXECUTE).isEmpty()
                            && handler.drain(held, FluidAction.SIMULATE).getAmount() == 250
                            && handler.drain(300, FluidAction.SIMULATE).getAmount() == 250
                            && bottle.save(new CompoundTag()).equals(before),
                    "Only complete matching servings drain; simulation leaves the bottle untouched");
            helper.assertTrue(handler.drain(held, FluidAction.EXECUTE).getAmount() == 250
                            && handler.getContainer().is(Items.GLASS_BOTTLE) && !handler.getContainer().hasTag()
                            && bottle.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(AssertionError::new) == handler
                            && handler.drain(250, FluidAction.EXECUTE).isEmpty(),
                    "Execution returns a clean bottle and does not refill the cached handler");
            var savedBottle = ItemStack.of(before).copyWithCount(2);
            var stacked = savedBottle.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(AssertionError::new);
            helper.assertTrue(stacked.drain(250, FluidAction.EXECUTE).isEmpty()
                            && savedBottle.split(1).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(AssertionError::new)
                            .drain(250, FluidAction.EXECUTE).getAmount() == 250,
                    "Saved bottles have independent handlers and stacks must supply one item per operation");
        }
        var nativeMead = new ItemStack(ForgeRegistries.ITEMS.getValue(id("youkaishomecoming:mead")));
        helper.assertTrue(nativeMead.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(AssertionError::new)
                        .getClass() != FixedItemFluidHandler.class,
                "An existing Youkai drink handler has priority over the fixed fallback");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void selectionUsesRegistrationCarrierAndActualServingCount(GameTestHelper helper) {
        var glass = Ingredient.of(Items.GLASS_BOTTLE);
        var recipe = id("createdelight:test/sizes");
        var smallStack = new ItemStack(Items.CARROT, 16);
        smallStack.invalidateCaps();
        helper.assertTrue(!smallStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent(),
                "The recipe can be resolved without consulting the bottle capability");
        var small = TavernBarrelFluidOutput.select(recipe, smallStack, glass, 3);
        var large = TavernBarrelFluidOutput.select(recipe, new ItemStack(Items.GOLDEN_CARROT, 8), glass, 3);
        helper.assertTrue(small != null && small.completeBrewing(Quality.NONE) && small.amount() == 1600
                        && large != null && large.completeBrewing(Quality.NONE) && large.amount() == 4000
                        && TavernBarrelFluidOutput.select(recipe, new ItemStack(Items.GOLDEN_CARROT, 9), glass, 3) == null,
                "The actual assembled count times 100 or 500 mB is checked against capacity");
        helper.assertTrue(TavernBarrelFluidOutput.select(recipe, cider(), Ingredient.of(Items.BUCKET), 3) == null
                        && TavernBarrelFluidOutput.select(recipe, new ItemStack(Items.HONEY_BOTTLE), glass, 3) == null,
                "Wrong carriers and unregistered drinks use item output");
        helper.assertTrue(small.transferServing(result -> false).isEmpty() && small.amount() == 1600,
                "A rejected delivery does not debit the tank");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void pipeAndDepotShareLegacyStockAndPersistTheExactRemainder(GameTestHelper helper) {
        if (!ModList.get().isLoaded("creategearsandtavern")) { helper.succeed(); return; }
        var relative = new BlockPos(3, 1, 3);
        var barrel = barrel(helper, relative);
        var part = relative.north();
        helper.setBlock(part, barrel.getBlockState().setValue(BarrelBlock.INDEX, 1));
        barrel.tick(helper.getLevel());
        var proxy = helper.getLevel().getBlockEntity(helper.absolutePos(part));
        var proxyPipe = proxy.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElseThrow(AssertionError::new);
        IFluidHandler pipe = barrel.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.NORTH).orElseThrow(AssertionError::new);
        helper.assertTrue(!barrel.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.EAST).isPresent(),
                "The new stock respects CGT's existing side rules");

        var recipe = recipe(cider(), Ingredient.of(Items.GLASS_BOTTLE));
        loadLegacy(barrel, recipe, TavernQuality.qualify(cider(), Quality.GOLD).copyWithCount(16), 2);
        withRecipe(helper, recipe, () -> barrel.tick(helper.getLevel()));
        FluidStack served = pipe.getFluidInTank(0);
        helper.assertTrue(served.getFluid() == ciderFluid() && served.getAmount() == 4000
                        && pipe.getTankCapacity(0) == 4000 && proxyPipe.getFluidInTank(0).getAmount() == 4000
                        && barrel.getOutput().getStackInSlot(0).isEmpty() && barrel.getFluid().isEmpty()
                        && QualityFoodFluidsApi.getQuality(served) == Quality.GOLD,
                "The controller and proxy see the same migrated stock and there is no item inventory");
        served.setAmount(100);
        var wrongQuality = QualityFoodFluidsApi.withQuality(served, Quality.IRON);
        helper.assertTrue(proxyPipe.drain(wrongQuality, FluidAction.EXECUTE).isEmpty()
                        && proxyPipe.fill(served, FluidAction.EXECUTE) == 0
                        && proxyPipe.drain(served, FluidAction.SIMULATE).getAmount() == 100
                        && pipe.getFluidInTank(0).getAmount() == 4000,
                "The output is drain-only and simulation matches exact NBT without changing stock");
        helper.assertTrue(proxyPipe.drain(served, FluidAction.EXECUTE).getAmount() == 100
                        && pipe.getFluidInTank(0).getAmount() == 3900, "The proxy debits the controller");
        barrel.tick(helper.getLevel());
        helper.assertTrue(QualityFoodFluidsApi.getQuality(pipe.getFluidInTank(0)) == Quality.DIAMOND
                        && QualityFoodFluidsApi.getQuality(served) == Quality.GOLD,
                "Aging upgrades only the wine still in the tank");

        var depotPosition = new BlockPos(1, 1, 1);
        helper.setBlock(depotPosition, AllBlocks.DEPOT.getDefaultState());
        var depot = (DepotBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(depotPosition));
        var buffer = ((DepotOutputBufferAccessor) depot.getBehaviour(DepotBehaviour.TYPE)).createdelightcore$getProcessingOutputBuffer();
        for (int slot = 0; slot < buffer.getSlots(); slot++) buffer.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, 64));
        var tapPos = depot.getBlockPos().above();
        depot.setHeldItem(new ItemStack(Items.GLASS_BOTTLE, 2));
        helper.assertTrue(!barrel.canTapExtract(helper.getLevel(), tapPos, null), "A full buffer blocks the stacked carrier");
        barrel.doTapExtract(helper.getLevel(), tapPos);
        helper.assertTrue(depot.getHeldItem().getCount() == 2 && pipe.getFluidInTank(0).getAmount() == 3900,
                "A blocked depot preserves both bottles and liquid");
        buffer.setStackInSlot(0, ItemStack.EMPTY);
        barrel.doTapExtract(helper.getLevel(), tapPos);
        helper.assertTrue(depot.getHeldItem().is(Items.GLASS_BOTTLE) && depot.getHeldItem().getCount() == 1
                        && buffer.getStackInSlot(0).is(recipe.result().getItem())
                        && QualityUtils.getQuality(buffer.getStackInSlot(0)) == Quality.DIAMOND,
                "Freeing one slot resumes transfer to the buffer and consumes one carrier");
        for (int bottle = 0; bottle < 14; bottle++) {
            depot.setHeldItem(new ItemStack(Items.GLASS_BOTTLE));
            helper.assertTrue(barrel.canTapExtract(helper.getLevel(), depot.getBlockPos().above(), null),
                    "A full serving allows a glass bottle on the depot");
            barrel.doTapExtract(helper.getLevel(), depot.getBlockPos().above());
            var filled = depot.getHeldItem();
            helper.assertTrue(filled.is(recipe.result().getItem()) && QualityUtils.getQuality(filled) == Quality.DIAMOND
                            && filled.getOrCreateTag().getString("Producer").equals("shared-stock")
                            && !filled.getOrCreateTag().contains("BrewLevel"), "Bottling keeps the original assembled item");
        }
        depot.setHeldItem(new ItemStack(Items.GLASS_BOTTLE));
        barrel.load(barrel.saveWithoutMetadata());
        helper.assertTrue(!barrel.canTapExtract(helper.getLevel(), depot.getBlockPos().above(), null)
                        && pipe.getFluidInTank(0).getAmount() == 150, "A reload preserves the fraction below one serving");
        barrel.doTapExtract(helper.getLevel(), depot.getBlockPos().above());
        helper.assertTrue(depot.getHeldItem().is(Items.GLASS_BOTTLE) && pipe.drain(600, FluidAction.EXECUTE).getAmount() == 150
                        && barrel.getBrewLevel() == 0 && ((TavernBarrelAccess) barrel).createdelightcore$getQualityBatch() == null,
                "The sixteenth bottle is rejected and a pipe can finish the batch");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void nativeCgtVariantReturnsToAPlacedBottle(GameTestHelper helper) {
        if (!ModList.get().isLoaded("creategearsandtavern")) { helper.succeed(); return; }
        var barrel = barrel(helper, new BlockPos(3, 1, 3));
        var wine = new ItemStack(ModItems.WINE.get());
        wine.getOrCreateTag().putString("Producer", "native-variant");
        var recipe = recipe(wine, Ingredient.of(ModItems.EMPTY_BOTTLE.get()));
        loadLegacy(barrel, recipe, wine, 100);
        withRecipe(helper, recipe, () -> barrel.tick(helper.getLevel()));
        var pipe = barrel.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(AssertionError::new);
        var fluid = pipe.getFluidInTank(0);
        helper.assertTrue(ForgeRegistries.FLUIDS.getKey(fluid.getFluid()).getNamespace().equals("creategearsandtavern")
                        && fluid.getAmount() == 250 && fluid.hasTag(), "Use the original CGT fluid variant");
        var below = new BlockPos(1, 1, 1);
        var bottle = (BlockItem) ModItems.EMPTY_BOTTLE.get();
        helper.setBlock(below, bottle.getBlock().defaultBlockState());
        barrel.doTapExtract(helper.getLevel(), helper.absolutePos(below.above()));
        var placed = (DrinkBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(below));
        var filled = placed.getItems().get(0);
        helper.assertTrue(filled.is(ModItems.WINE.get()) && QualityUtils.getQuality(filled) == Quality.GOLD
                        && filled.getTag().getString("Producer").equals("native-variant") && fluid.getTag().contains("DrinkVariant")
                        && barrel.getBrewLevel() == 0 && pipe.getFluidInTank(0).isEmpty(),
                "The native placed bottle keeps its identity, all assembled NBT and the last serving's quality");
        helper.succeed();
    }

    private static boolean invalid(Runnable registration) {
        try { registration.run(); } catch (IllegalArgumentException expected) { return true; }
        return false;
    }

    private static ItemStack cider() {
        var product = new ItemStack(ForgeRegistries.ITEMS.getValue(id(CIDER_ITEM)));
        product.getOrCreateTag().putString("Producer", "shared-stock");
        return product;
    }

    private static Fluid ciderFluid() { return ForgeRegistries.FLUIDS.getValue(id(CIDER_FLUID)); }
    private static ResourceLocation id(String value) { return new ResourceLocation(value); }

    private static BarrelBlockEntity barrel(GameTestHelper helper, BlockPos pos) {
        var state = ModBlocks.BARREL.get().defaultBlockState().setValue(BarrelBlock.LAYER, AttachFace.FLOOR)
                .setValue(BarrelBlock.INDEX, 4).setValue(BarrelBlock.FACING, Direction.NORTH);
        helper.setBlock(pos, state);
        return (BarrelBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static BarrelRecipe recipe(ItemStack result, Ingredient carrier) {
        return new BarrelRecipe(id("createdelight:test/shared_stock"), NonNullList.create(), Fluids.WATER, carrier, result, 3);
    }

    private static void loadLegacy(BarrelBlockEntity barrel, BarrelRecipe recipe, ItemStack output, int aging) {
        barrel.getOutput().setStackInSlot(0, output);
        var tag = barrel.saveWithoutMetadata();
        tag.putBoolean("open", false);
        tag.putInt("brew_level", 3);
        tag.putString("recipe_id", recipe.getId().toString());
        var batch = new TavernBarrelBatch(Quality.GOLD).save();
        batch.putInt("version", 3);
        batch.putInt("remaining_ticks", aging);
        tag.put(TavernBarrelBatch.NBT_KEY, batch);
        barrel.load(tag);
    }

    private static void withRecipe(GameTestHelper helper, Recipe<?> recipe, Runnable run) {
        var manager = helper.getLevel().getRecipeManager();
        var original = new ArrayList<Recipe<?>>(manager.getRecipes());
        var recipes = new ArrayList<>(original);
        recipes.add(recipe);
        manager.replaceRecipes(recipes);
        try { run.run(); } finally { manager.replaceRecipes(original); }
    }
}
