package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.PressingTubBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.container.BarrelRecipeContainer;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.BarrelRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.resources.DrinkEffectDataReloadListener;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModCreativeTabs;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.SignatureCocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import de.cadentem.quality_food.capability.LevelData;
import com.github.ysbbbbbb.kaleidoscopetavern.util.FluidUtils;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.FoodUtils;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelBatch;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernCreativeTabs;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.MutableHashedLinkedMap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class TavernQualityGameTests {
    @GameTestGenerator
    public static List<TestFunction> optionalCgtDeferredBarrelOutput() {
        if (!ModList.get().isLoaded("creategearsandtavern")) return List.of();
        return List.of(new TestFunction("tavern_quality", "tavernqualitygametests.cgtdeferredbarreloutput",
                "createdelightcore:humidity_regulator_empty", Rotation.NONE, 40, 0, true, helper -> {
            try {
                var barrel = barrel(helper);
                barrel.getIngredient().setStackInSlot(0, new ItemStack(Items.APPLE, 2));
                var recipe = new BarrelRecipe(id("createdelight:test/cgt_brewing_wait"),
                        NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.APPLE)),
                        ForgeRegistries.FLUIDS.getValue(id("kaleidoscope_tavern:grape_juice")),
                        Ingredient.of(ModItems.EMPTY_BOTTLE.get()), wine(), 7);
                barrel.getFluid().fill(new FluidStack(recipe.fluid(), 4000), IFluidHandler.FluidAction.EXECUTE);
                start(barrel, recipe, helper.getLevel());
                String wrapper = "com.yision.creategearsandtavern.compat.kaleidoscope.CGTKaleidoscopeBarrelFluids$";
                var fluidConstructor = Class.forName(wrapper + "BarrelFluidHandler")
                        .getDeclaredConstructor(BarrelBlockEntity.class, Direction.class);
                var itemConstructor = Class.forName(wrapper + "BarrelItemHandler")
                        .getDeclaredConstructor(BarrelBlockEntity.class, Direction.class);
                fluidConstructor.setAccessible(true);
                itemConstructor.setAccessible(true);
                var fluid = (IFluidHandler) fluidConstructor.newInstance(barrel, null);
                var items = (IItemHandler) itemConstructor.newInstance(barrel, null);
                for (int tick = 0; tick < recipe.unitTime(); tick++) {
                    helper.assertTrue(fluid.getFluidInTank(0).isEmpty()
                                    && fluid.drain(1000, IFluidHandler.FluidAction.SIMULATE).isEmpty()
                                    && fluid.drain(1000, IFluidHandler.FluidAction.EXECUTE).isEmpty()
                                    && items.getStackInSlot(0).isEmpty() && items.extractItem(0, 64, false).isEmpty(),
                            "Real CGT fluid and item wrappers expose no product before the brewing deadline");
                    barrel.tick(helper.getLevel());
                }
                helper.assertTrue(fluid.getFluidInTank(0).getAmount() == 500
                                && fluid.getTankCapacity(0) == 4000 && items.getStackInSlot(0).isEmpty(),
                        "The deadline exposes both servings in the product tank and leaves the item slot empty");
                var drained = fluid.drain(250, IFluidHandler.FluidAction.EXECUTE);
                helper.assertTrue(drained.getAmount() == 250 && QualityFoodFluidsApi.getQuality(drained) == batch(barrel).quality()
                                && fluid.getFluidInTank(0).getAmount() == 250,
                        "Completed CGT fluid extraction keeps QFF quality and consumes exactly one bottle");
                helper.assertTrue(items.extractItem(0, 1, false).isEmpty()
                                && fluid.drain(250, IFluidHandler.FluidAction.EXECUTE).getAmount() == 250,
                        "The actual CGT item wrapper cannot extract a second inventory");
                helper.assertTrue(batch(barrel) == null && fluid.getFluidInTank(0).isEmpty(),
                        "Draining a completed batch clears its state and cannot regenerate deferred output");
                helper.succeed();
            } catch (Exception exception) {
                throw new RuntimeException("Cannot exercise CGT's installed barrel wrappers", exception);
            }
        }));
    }

    @GameTestGenerator
    public static List<TestFunction> optionalCgtBottleFluidQuality() {
        if (!ModList.get().isLoaded("creategearsandtavern")) return List.of();
        return List.of(new TestFunction("tavern_quality", "tavernqualitygametests.cgtbottlefluidquality",
                "createdelightcore:humidity_regulator_empty", Rotation.NONE, 40, 0, true, helper -> {
            try {
                var conversions = Class.forName("com.yision.creategearsandtavern.content.fluids.drink.DrinkFluidConversions");
                var empty = conversions.getMethod("fromItem", ItemStack.class);
                var fill = conversions.getMethod("toItem", FluidStack.class);
                int wines = 0;
                for (var item : ForgeRegistries.ITEMS.getValues()) {
                    if (!ForgeRegistries.ITEMS.getKey(item).getNamespace().equals("kaleidoscope_tavern")) continue;
                    if (!TavernQuality.supportsQuality(new ItemStack(item))) continue;
                    wines++;
                    for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
                        var bottle = TavernQuality.qualify(new ItemStack(item), quality);
                        if (item instanceof SignatureCocktailBlockItem) {
                            SignatureCocktailBlockItem.setEffects(bottle, List.of(new DrinkEffectData.Entry(MobEffects.MOVEMENT_SPEED, 60, 0, 1)));
                            SignatureCocktailBlockItem.setColor(bottle, 0x5a6b7c);
                        }
                        var fluid = (FluidStack) empty.invoke(null, bottle);
                        helper.assertTrue(!fluid.isEmpty() && QualityFoodFluidsApi.getQuality(fluid) == quality,
                                "CGT exposes actual QFF fluid quality to tanks and pipes for " + item + " " + quality);
                        var name = fluid.getFluid().getFluidType().getDescription(fluid);
                        helper.assertTrue(name.equals(fluid.getFluid().getFluidType().getDescription()),
                                "CGT fluid names have no native aging prefix for " + item + " " + quality + ": " + name);
                        var intact = (ItemStack) fill.invoke(null, fluid.copy());
                        if (item instanceof SignatureCocktailBlockItem) {
                            helper.assertTrue(SignatureCocktailBlockItem.getEffects(intact).equals(SignatureCocktailBlockItem.getEffects(bottle))
                                            && SignatureCocktailBlockItem.getColor(intact) == 0x5a6b7c,
                                    "CGT quality bridging preserves signature cocktail effects and color");
                        }
                        var transferred = fluid.copy();
                        if (transferred.getTag() != null) transferred.getTag().remove("DrinkItemTag");
                        var restored = (ItemStack) fill.invoke(null, transferred);
                        helper.assertTrue(restored.is(item) && QualityUtils.getQuality(restored) == quality
                                        && !restored.getOrCreateTag().contains("createdelightcore:tavern_quality"),
                                "QFF root quality alone restores the same bottle without a CDC marker");
                    }
                }
                helper.assertTrue(wines == 38, "Cover 23 base wines, vinegar and all 14 cocktails in the CGT bridge");
                helper.succeed();
            } catch (ReflectiveOperationException exception) {
                throw new RuntimeException("Cannot exercise the installed CGT conversion API", exception);
            }
        }));
    }

    // Kept as a regression for the upstream QFF Forge bucket-capability fix; no CDC bucket workaround.
    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernJuiceBucketsPreserveAllQualitiesThroughTheNativeBarrel(GameTestHelper helper) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        boolean creative = player.getAbilities().instabuild;
        player.getAbilities().instabuild = false;
        try {
            for (String fruit : List.of("grape", "ice_grape", "gold_grape", "green_grape", "sweet_berries", "glow_berries")) {
                var fluid = ForgeRegistries.FLUIDS.getValue(id("kaleidoscope_tavern:" + fruit + "_juice"));
                var bucketItem = ForgeRegistries.ITEMS.getValue(id("kaleidoscope_tavern:" + fruit + "_bucket"));
                for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
                    player.getInventory().clearContent();
                    var qualified = QualityFoodFluidsApi.withQuality(new FluidStack(fluid, 1000), quality);
                    var bucket = FluidUtil.getFilledBucket(qualified.copy());
                    helper.assertTrue(bucket.is(bucketItem) && QualityUtils.getQuality(bucket) == quality,
                            "Native " + fruit + " bucket inherits " + quality + ", got " + bucket + " " + bucket.getTag());
                    helper.assertTrue(QualityFoodFluidsApi.getQuality(FluidUtil.getFluidContained(bucket).orElse(FluidStack.EMPTY)) == quality,
                            "Bucket capability exposes the same qualified juice");
                    var barrel = barrel(helper);
                    barrel.setOpen(true);
                    helper.assertTrue(barrel.addFluid(player, bucket), "Fill the barrel through its real bucket interaction");
                    helper.assertTrue(barrel.getFluid().getFluidAmount() == 1000
                                    && QualityFoodFluidsApi.getQuality(barrel.getFluid().getFluid()) == quality,
                            "Bucket-to-barrel transfer preserves " + quality + " " + fruit + " juice");
                    helper.assertTrue(FluidUtils.fillItem(player, new ItemStack(Items.BUCKET), barrel.getFluid(), 1000),
                            "Extract the last bucket through Tavern's native container helper");
                    helper.assertTrue(barrel.getFluid().isEmpty() && player.getInventory().items.stream()
                                    .anyMatch(stack -> stack.is(bucketItem) && QualityUtils.getQuality(stack) == quality),
                            "The final extracted juice bucket retains its quality");
                }
            }
        } finally {
            player.getInventory().clearContent();
            player.getAbilities().instabuild = creative;
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernPressingInheritsFruitQualityAndDoesNotConsumeRejectedInputs(GameTestHelper helper) throws Exception {
        var block = ForgeRegistries.BLOCKS.getValue(id("kaleidoscope_tavern:pressing_tub"));
        var press = PressingTubBlockEntity.class.getDeclaredMethod("lambda$press$0", ItemStack.class,
                SimpleContainer.class, PressingTubRecipe.class);
        press.setAccessible(true);
        var fluid = ForgeRegistries.FLUIDS.getValue(id("kaleidoscope_tavern:grape_juice"));
        var recipe = new PressingTubRecipe(id("createdelight:test/qualified_juice"), Ingredient.of(Items.APPLE), fluid, 125);
        for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
            var tub = new PressingTubBlockEntity(helper.absolutePos(new BlockPos(2, 1, 1)), block.defaultBlockState());
            tub.setLevel(helper.getLevel());
            var apples = new ItemStack(Items.APPLE, 2);
            if (quality != Quality.NONE) QualityUtils.applyQuality(apples, quality);
            tub.getItems().setStackInSlot(0, apples);
            for (int count = 1; count <= 2; count++) {
                var input = tub.getItems().getStackInSlot(0);
                helper.assertTrue(Boolean.TRUE.equals(press.invoke(tub, input, new SimpleContainer(input), recipe)),
                        "Repeated pressing accepts the same qualified ingredient");
                helper.assertTrue(tub.getFluidAmount() == 125 * count
                                && QualityFoodFluidsApi.getQuality(tub.getFluid().getFluid()) == quality,
                        "Juice inherits the pressed fruit's QF quality before native fluid comparison");
            }
            helper.assertTrue(tub.getItems().getStackInSlot(0).isEmpty(), "Consume exactly one fruit per successful press");
            tub.getFluid().setFluid(QualityFoodFluidsApi.withQuality(new FluidStack(fluid, 950), quality));
            var last = new ItemStack(Items.APPLE);
            if (quality != Quality.NONE) QualityUtils.applyQuality(last, quality);
            tub.getItems().setStackInSlot(0, last);
            helper.assertTrue(Boolean.FALSE.equals(press.invoke(tub, last, new SimpleContainer(last), recipe))
                            && tub.getItems().getStackInSlot(0).getCount() == 1 && tub.getFluidAmount() == 950,
                    "Insufficient capacity neither partially fills nor consumes fruit");
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernCreativeTabUsesDiamondBaseWineAndPreservesOtherItems(GameTestHelper helper) throws Exception {
        var entries = new MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility>();
        var parameters = new CreativeModeTab.ItemDisplayParameters(
                FeatureFlags.DEFAULT_FLAGS, false, helper.getLevel().registryAccess());
        var generator = ModCreativeTabs.class.getDeclaredMethod("lambda$static$0",
                CreativeModeTab.ItemDisplayParameters.class, CreativeModeTab.Output.class);
        generator.setAccessible(true);
        generator.invoke(null, parameters, (CreativeModeTab.Output) (stack, visibility) -> entries.put(stack.copy(), visibility));
        var event = new BuildCreativeModeTabContentsEvent(ModCreativeTabs.TAVERN_MAIN_TAB.get(),
                ModCreativeTabs.TAVERN_MAIN_TAB.getKey(), parameters, entries);
        TavernCreativeTabs.onBuildContents(event);
        List<ItemStack> displayed = new ArrayList<>();
        for (var entry : entries) displayed.add(entry.getKey());
        var wines = displayed.stream().filter(TavernQuality::isQualityWine).toList();
        helper.assertTrue(wines.size() == 23 && wines.stream().map(ItemStack::getItem).distinct().count() == 23,
                "The native tab displays each of its 23 base wines exactly once, got "
                        + wines.size() + ": " + wines.stream().map(stack -> stack.getItem().toString()).toList());
        helper.assertTrue(wines.stream().allMatch(stack -> QualityUtils.getQuality(stack) == Quality.DIAMOND
                        && BottleBlockItem.getBrewLevel(stack) == TavernQuality.NORMAL_BREW_LEVEL
                        && BottleBlockItem.isValidForShaker(stack)),
                "Creative base wines use the same diamond quality, effects and shaker rules as aged wine");
        helper.assertTrue(displayed.stream().anyMatch(stack -> stack.is(ModItems.VINEGAR.get())
                        && !TavernQuality.isQualityWine(stack) && QualityUtils.getQuality(stack) == Quality.DIAMOND
                        && BottleBlockItem.getBrewLevel(stack) == 3), "The standalone Tavern tab qualifies vinegar without aging it");
        helper.assertTrue(displayed.stream().anyMatch(stack -> stack.is(ModItems.SIGNATURE_COCKTAIL.get())
                        && !TavernQuality.isQualityWine(stack) && QualityUtils.getQuality(stack) == Quality.DIAMOND),
                "Creative signature cocktail uses diamond quality");
        var cocktails = displayed.stream().filter(stack -> stack.getItem() instanceof CocktailBlockItem).toList();
        helper.assertTrue(cocktails.size() == 14 && cocktails.stream().allMatch(stack -> QualityUtils.getQuality(stack) == Quality.DIAMOND),
                "All 14 creative cocktails display diamond quality");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernBatchUsesActualItemsAndFluidsAndCapsInitialQuality(GameTestHelper helper) throws Exception {
        var barrel = barrel(helper);
        var template = wine();
        QualityUtils.applyQuality(template, Quality.DIAMOND);
        template.setCount(16);
        var recipe = recipe(template);
        Quality[][] cases = {{Quality.NONE, Quality.NONE}, {Quality.GOLD, Quality.NONE},
                {Quality.NONE, Quality.GOLD}, {Quality.DIAMOND, Quality.DIAMOND}, {Quality.NONE, Quality.NONE}};
        for (Quality[] qualities : cases) {
            var apple = new ItemStack(Items.APPLE, 4);
            if (qualities[0] != Quality.NONE) QualityUtils.applyQuality(apple, qualities[0]);
            var fluid = QualityFoodFluidsApi.withQuality(new FluidStack(recipe.fluid(), 4000), qualities[1]);
            barrel.getIngredient().setStackInSlot(0, apple.copy());
            barrel.getFluid().fill(fluid.copy(), IFluidHandler.FluidAction.EXECUTE);
            long seed = 0;
            Quality expected;
            do {
                qffRandom().setSeed(seed++);
                expected = QualityFoodFluidsCreateRules.rollBasinQuality(List.of(apple.copy()), List.of(fluid.copy()));
            } while ((qualities[0] != Quality.NONE || qualities[1] != Quality.NONE)
                    && expected == Quality.NONE && seed < 10000);
            helper.assertTrue(seed < 10000, "Qualified inputs must offer a non-normal QFF roll");
            qffRandom().setSeed(seed - 1);
            start(barrel, recipe, helper.getLevel());
            Quality capped = expected == Quality.DIAMOND ? Quality.GOLD : expected;
            helper.assertTrue(batch(barrel) != null && batch(barrel).quality() == capped
                            && batch(barrel).brewingTicks() == recipe.unitTime(),
                    "Use actual inputs and the recipe's exact brewing time");
            assertNoBrewedOutput(helper, barrel);
            barrel.tick(helper.getLevel());
            var output = barrel.getOutput().getStackInSlot(0);
            helper.assertTrue(batch(barrel) != null && batch(barrel).quality() == capped
                    && QualityUtils.getQuality(output) == capped && output.getCount() == 4,
                    "Use both actual inputs, clear old template quality, and cap the initial roll at gold");
            helper.assertTrue(TavernQuality.isQualityWine(output) && barrel.getBrewLevel() == 3 && !batch(barrel).brewing()
                            && !output.getOrCreateTag().contains("createdelightcore:tavern_quality"),
                    "Only completed base wine enters the output slot and uses QF directly");
            helper.assertTrue(barrel.getIngredient().getStackInSlot(0).isEmpty() && barrel.getFluid().isEmpty(),
                    "Keep native ingredient consumption");
            barrel.getOutput().setStackInSlot(0, ItemStack.EMPTY);
            invoke(barrel, "resetIfOutputEmpty", new Class<?>[0]);
            helper.assertTrue(batch(barrel) == null, "Emptying the barrel clears the old batch");
        }
        helper.assertTrue(QualityUtils.getQuality(template) == Quality.DIAMOND && !template.getOrCreateTag().contains("BrewLevel"),
                "Never mutate the shared recipe result");
        var capped = new TavernBarrelBatch(Quality.DIAMOND);
        helper.assertTrue(capped.quality() == Quality.GOLD && capped.durationTicks() == 108000,
                "A diamond roll always starts at gold");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernAgingTakesThirtySixtyNinetyMinutesAndUpgradesOnlyOnce(GameTestHelper helper) {
        for (Quality initial : List.of(Quality.NONE, Quality.IRON, Quality.GOLD)) {
            var batch = new TavernBarrelBatch(initial);
            int duration = 36000 * (initial.level() + 1);
            helper.assertTrue(batch.remainingTicks() == duration, "Use 30/60/90 minute durations");
            for (int tick = 1; tick < duration; tick++) {
                if (batch.tick()) throw new AssertionError("Aging finished before its deadline");
            }
            helper.assertTrue(batch.quality() == initial && batch.remainingTicks() == 1,
                    "Do not upgrade before the last tick");
            helper.assertTrue(batch.tick() && batch.aged() && batch.quality() == Quality.get(initial.level() + 1),
                    "The deadline upgrades exactly one QF level");
            var restored = TavernBarrelBatch.load(batch.save());
            for (int tick = 0; tick < duration; tick++) {
                if (restored.tick()) throw new AssertionError("A completed batch upgraded twice");
            }
            helper.assertTrue(restored.quality() == batch.quality() && restored.remainingTicks() == 0,
                    "A completed upgrade survives persistence");
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernRealBarrelTickReplacesNativeAgingAndPersistsForJade(GameTestHelper helper) throws Exception {
        var barrel = barrel(helper);
        start(barrel, recipe(wine()), helper.getLevel());
        barrel.tick(helper.getLevel());
        var saved = barrel.saveWithoutMetadata();
        var state = saved.getCompound(TavernBarrelBatch.NBT_KEY);
        state.putInt("initial_quality", 2);
        state.putInt("brewing_ticks", 0);
        state.putInt("remaining_ticks", 2);
        barrel.load(saved);
        barrel.setOpen(true);
        barrel.tick(helper.getLevel());
        helper.assertTrue(batch(barrel).remainingTicks() == 2, "Open barrels do not age");
        barrel.setOpen(false);
        barrel.tick(helper.getLevel());
        helper.assertTrue(batch(barrel).remainingTicks() == 1 && !barrel.isMaxBrewLevel(), "Advance only the QF timer");
        var restored = barrel(helper);
        restored.load(barrel.getUpdateTag());
        helper.assertTrue(batch(restored).remainingTicks() == 1 && batch(restored).quality() == Quality.GOLD,
                "The vanilla BE update tag carries the same state as disk persistence");
        restored.tick(helper.getLevel());
        helper.assertTrue(batch(restored).aged() && restored.isMaxBrewLevel() && restored.getBrewLevel() == 3
                        && QualityUtils.getQuality(restored.getOutput().getStackInSlot(0)) == Quality.DIAMOND,
                "Upgrade QF to diamond without advancing the native BrewLevel");
        for (int tick = 0; tick < 200; tick++) restored.tick(helper.getLevel());
        helper.assertTrue(batch(restored).quality() == Quality.DIAMOND && restored.getBrewTime() == 0,
                "No further native or QF aging after completion");
        var lines = TavernQuality.barrelTooltip(restored);
        helper.assertTrue(lines.size() == 3 && translation(lines.get(1)).endsWith(".quality")
                        && translation(lines.get(2)).endsWith(".aged"), "Jade shows current QF quality and completion");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernBottlingAndPlacedBottlesPreserveTheFinalBottleQuality(GameTestHelper helper) throws Exception {
        var barrel = barrel(helper);
        var template = wine();
        template.setCount(2);
        // Native assemble ignores the result template count and uses min(16, actual input counts).
        barrel.getIngredient().setStackInSlot(0, new ItemStack(Items.APPLE, 2));
        start(barrel, recipe(template), helper.getLevel());
        barrel.tick(helper.getLevel());
        var saved = barrel.saveWithoutMetadata();
        saved.getCompound(TavernBarrelBatch.NBT_KEY).putInt("initial_quality", 2);
        saved.getCompound(TavernBarrelBatch.NBT_KEY).putInt("brewing_ticks", 0);
        barrel.load(saved);
        var first = (ItemStack) invoke(barrel, "extractOneOutput", new Class<?>[]{ItemStack.class}, wine());
        helper.assertTrue(QualityUtils.getQuality(first) == Quality.GOLD && TavernQuality.isQualityWine(first),
                "Extracted bottles inherit the batch quality");
        var before = batch(barrel).remainingTicks();
        helper.assertTrue(barrel.getOutput().getStackInSlot(0).getCount() == 1 && before == 36000,
                "Partial extraction neither rerolls nor restarts the current timer");
        var last = (ItemStack) invoke(barrel, "extractOneOutput", new Class<?>[]{ItemStack.class}, wine());
        var placedPos = helper.absolutePos(new BlockPos(2, 1, 2));
        invoke(barrel, "placeBottleResult", new Class<?>[]{Level.class, BlockPos.class,
                        net.minecraft.world.level.block.state.BlockState.class, BottleBlockItem.class},
                helper.getLevel(), placedPos, Blocks.AIR.defaultBlockState(), (BottleBlockItem) wine().getItem());
        var placed = (DrinkBlockEntity) helper.getLevel().getBlockEntity(placedPos);
        var returned = placed.removeItem();
        helper.assertTrue(TavernQuality.isQualityWine(last) && QualityUtils.getQuality(last) == Quality.GOLD
                        && TavernQuality.isQualityWine(returned) && QualityUtils.getQuality(returned) == Quality.GOLD,
                "Even the last extraction and native placement reconstruction preserve quality");
        placed.addItem(returned.copy());
        var stored = placed.saveWithoutMetadata();
        placed.load(stored);
        helper.assertTrue(QualityUtils.getQuality(placed.removeItem()) == Quality.GOLD,
                "Placed bottle NBT survives save/load and retrieval");
        placed.addItem(returned.copy());
        var drops = helper.getLevel().getBlockState(placedPos).getDrops(
                new net.minecraft.world.level.storage.loot.LootParams.Builder(helper.getLevel())
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY, placed)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                                net.minecraft.world.phys.Vec3.atCenterOf(placedPos))
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, ItemStack.EMPTY));
        helper.assertTrue(drops.stream().anyMatch(stack -> TavernQuality.isQualityWine(stack)
                        && QualityUtils.getQuality(stack) == Quality.GOLD), "Ordinary bottle-block drops retain quality NBT");
        invoke(barrel, "resetIfOutputEmpty", new Class<?>[0]);
        helper.assertTrue(batch(barrel) == null && barrel.getBrewLevel() == 0, "Reset only after the last bottle is handed off");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernShakerUsesOnlyQfQualityAndQualifiesCocktailOutput(GameTestHelper helper) throws Exception {
        for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
            var bottle = TavernQuality.qualify(wine(), quality);
            helper.assertTrue(BottleBlockItem.isValidForShaker(bottle) == (quality.level() >= Quality.IRON.level()),
                    "Base spirits require QF iron or better, despite native BrewLevel 3");
        }
        var plain = wine();
        BottleBlockItem.setBrewLevel(plain, 6);
        helper.assertTrue(!BottleBlockItem.isValidForShaker(plain), "Native BrewLevel cannot replace QF iron quality");
        QualityUtils.applyQuality(plain, Quality.IRON);
        helper.assertTrue(BottleBlockItem.isValidForShaker(plain), "QF iron alone is enough without a CDC marker");
        for (var item : cocktailItems()) {
            var result = TavernQuality.qualify(new ItemStack(item, 2), Quality.DIAMOND);
            result.getOrCreateTag().putString("fixture", "unchanged");
            var recipe = new ShakerRecipe(id("createdelight:test/tavern_cocktail"),
                    NonNullList.of(Ingredient.EMPTY, Ingredient.of(wine().getItem())), result, new Int2ObjectOpenHashMap<>());
            for (Quality quality : List.of(Quality.DIAMOND, Quality.GOLD, Quality.IRON, Quality.NONE)) {
                var ingredient = TavernQuality.qualify(wine(), quality);
                qffRandom().setSeed(104);
                Quality expected = QualityFoodFluidsCreateRules.rollBasinQuality(List.of(ingredient.copy()), List.of());
                qffRandom().setSeed(104);
                var output = recipe.assemble(new SimpleContainer(ingredient), helper.getLevel().registryAccess());
                helper.assertTrue(output.is(item) && output.getCount() == 2 && output != result
                                && QualityUtils.getQuality(output) == expected
                                && output.getOrCreateTag().getString("fixture").equals("unchanged"),
                        "Cocktail quality uses actual inputs, clears stale template quality and keeps custom NBT");
            }
            helper.assertTrue(QualityUtils.getQuality(result) == Quality.DIAMOND, "Do not mutate the shared cocktail recipe template");
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernDrinkingAndTooltipUseQfScaledOrdinaryEffects(GameTestHelper helper) {
        var item = wine().getItem();
        var previous = DrinkEffectDataReloadListener.INSTANCE.get(item);
        var ordinary = List.of(new DrinkEffectData.Entry(MobEffects.MOVEMENT_SPEED, 60, 0, 1),
                new DrinkEffectData.Entry(MobEffects.MOVEMENT_SLOWDOWN, 60, 3, 1));
        var elite = List.of(new DrinkEffectData.Entry(MobEffects.DAMAGE_BOOST, 300, 10, 1));
        DrinkEffectDataReloadListener.INSTANCE.put(item,
                new DrinkEffectData(item, List.of(elite, elite, ordinary, elite, elite, elite)));
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        try {
            for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
                helper.assertTrue(translation(TavernQuality.qualityName(quality)).startsWith("quality_type.quality_food."),
                        "Quality names stay translatable when sent from the server to Jade and the action bar");
                player.removeAllEffects();
                var bottle = TavernQuality.qualify(wine(), quality);
                var base = new FoodProperties.Builder().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200), 1)
                        .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 3), 1).build();
                var expected = FoodUtils.handleFoodProperties(bottle, base).getEffects();
                helper.assertTrue(TavernQuality.effects(bottle).equals(expected), "Use QF's own effect scaling and probabilities");
                ((com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem) item)
                        .finishUsingItem(bottle.copy(), helper.getLevel(), player);
                helper.assertTrue(!player.hasEffect(MobEffects.DAMAGE_BOOST), "Never use native high-grade effects for new wine");
                for (var entry : expected) {
                    if (entry.getSecond() >= 1) helper.assertTrue(entry.getFirst().equals(player.getEffect(entry.getFirst().getEffect())),
                            "Native drink action applies guaranteed QF-scaled effects exactly once");
                }
                List<Component> tooltip = new ArrayList<>();
                item.appendHoverText(bottle, helper.getLevel(), tooltip, TooltipFlag.NORMAL);
                helper.assertTrue(tooltip.stream().noneMatch(line -> translation(line).equals("tooltip.kaleidoscope_tavern.bottle_block.brew_level")),
                        "New bottle tooltips have no native quality line");
            }
            player.removeAllEffects();
            var plain = wine();
            BottleBlockItem.setBrewLevel(plain, 6);
            ((com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem) item).finishUsingItem(plain, helper.getLevel(), player);
            helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED) && !player.hasEffect(MobEffects.DAMAGE_BOOST),
                    "Unmarked ordinary wine also uses QF-scaled ordinary effects");
        } finally {
            player.removeAllEffects();
            player.getInventory().clearContent();
            if (previous == null) DrinkEffectDataReloadListener.INSTANCE.remove(item);
            else DrinkEffectDataReloadListener.INSTANCE.put(item, previous);
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernWineUsesQfWithoutAnyCdcBottleMarker(GameTestHelper helper) {
        for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
            var bottle = wine();
            if (quality != Quality.NONE) QualityUtils.applyQuality(bottle, quality);
            helper.assertTrue(TavernQuality.isQualityWine(bottle), "Identify base wine by item, including untagged ordinary wine");
            List<Component> tooltip = new ArrayList<>();
            bottle.getItem().appendHoverText(bottle, helper.getLevel(), tooltip, TooltipFlag.NORMAL);
            helper.assertTrue(tooltip.stream().anyMatch(line -> translation(line).equals("tooltip.createdelightcore.tavern.quality"))
                            && tooltip.stream().noneMatch(line -> translation(line).equals("tooltip.kaleidoscope_tavern.bottle_block.brew_level")),
                    "QF tags alone select QF tooltips without any native aging row");
            helper.assertTrue(bottle.getTag() == null || !bottle.getTag().contains("createdelightcore:tavern_quality"),
                    "Do not create a second bottle-quality marker");
        }
        helper.assertTrue(!TavernQuality.isQualityWine(new ItemStack(ModItems.VINEGAR.get()))
                        && !TavernQuality.isQualityWine(new ItemStack(ModItems.SIGNATURE_COCKTAIL.get()))
                        && !TavernQuality.isQualityWine(new ItemStack(ModItems.WATERMELON_JUICE.get()))
                        && TavernQuality.supportsQuality(new ItemStack(ModItems.VINEGAR.get()))
                        && TavernQuality.supportsQuality(new ItemStack(ModItems.SIGNATURE_COCKTAIL.get())),
                "Vinegar and cocktails support QF while remaining separate from aging base wine and watermelon juice");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernBrewingUsesRecipeDurationAndCreatesOutputOnlyAtCompletion(GameTestHelper helper) throws Exception {
        var barrel = barrel(helper);
        barrel.getIngredient().setStackInSlot(0, new ItemStack(Items.APPLE, 2));
        var recipe = new BarrelRecipe(id("createdelight:test/brewing_wait"),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.APPLE)),
                ForgeRegistries.FLUIDS.getValue(id("kaleidoscope_tavern:grape_juice")), Ingredient.of(Items.GLASS_BOTTLE), wine(), 7);
        barrel.getFluid().fill(new FluidStack(recipe.fluid(), 4000), IFluidHandler.FluidAction.EXECUTE);
        start(barrel, recipe, helper.getLevel());
        helper.assertTrue(batch(barrel).brewingTicks() == 7 && batch(barrel).remainingTicks() == 36000,
                "Use unit_time once and leave the aging timer untouched during brewing");
        assertNoBrewedOutput(helper, barrel);
        invoke(barrel, "resetIfOutputEmpty", new Class<?>[0]);
        helper.assertTrue(batch(barrel) != null && batch(barrel).brewingTicks() == 7,
                "An empty native output slot must not reset an unfinished batch");
        var preview = batch(barrel).pendingOutput();
        preview.shrink(preview.getCount());
        helper.assertTrue(batch(barrel).pendingOutput().getCount() == 2,
                "Display snapshots cannot mutate the pending recipe record");
        var carrier = new ItemStack(Items.GLASS_BOTTLE, 3);
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        helper.assertTrue(Boolean.FALSE.equals(invoke(barrel, "canTapExtract", new Class<?>[]{Level.class, BlockPos.class,
                        net.minecraft.world.entity.LivingEntity.class}, helper.getLevel(), barrel.getBlockPos(), player)),
                "The tap cannot advertise extraction during brewing");
        var dropped = new ItemEntity(helper.getLevel(), 0, 0, 0, carrier.copy());
        invoke(barrel, "transformItemCarrier", new Class<?>[]{Level.class, BlockPos.class, ItemEntity.class, ItemStack.class},
                helper.getLevel(), barrel.getBlockPos(), dropped, wine());
        var placedPos = helper.absolutePos(new BlockPos(3, 1, 3));
        helper.getLevel().setBlock(placedPos, Blocks.GLASS.defaultBlockState(), 3);
        invoke(barrel, "transformPlacedCarrier", new Class<?>[]{Level.class, BlockPos.class,
                        net.minecraft.world.level.block.state.BlockState.class, ItemStack.class},
                helper.getLevel(), placedPos, Blocks.GLASS.defaultBlockState(), wine());
        helper.assertTrue(dropped.getItem().getCount() == 3 && helper.getLevel().getBlockState(placedPos).is(Blocks.GLASS)
                        && barrel.getOutput().getStackInSlot(0).isEmpty(),
                "Dropped and placed carriers cannot consume output or containers while brewing");
        barrel.setOpen(true);
        barrel.tick(helper.getLevel());
        helper.assertTrue(batch(barrel).brewingTicks() == 7, "Opening the barrel pauses brewing");
        barrel.setOpen(false);
        for (int tick = 0; tick < 3; tick++) {
            barrel.tick(helper.getLevel());
            assertNoBrewedOutput(helper, barrel);
        }
        var restored = barrel(helper);
        restored.load(barrel.getUpdateTag());
        helper.assertTrue(batch(restored).brewingTicks() == 4
                        && translation(TavernQuality.barrelTooltip(restored).get(2)).endsWith(".brewing"),
                "Persistence and the native update tag carry brewing progress to Jade");
        for (int tick = 0; tick < 3; tick++) {
            assertNoBrewedOutput(helper, restored);
            restored.tick(helper.getLevel());
        }
        assertNoBrewedOutput(helper, restored);
        var early = (ItemStack) invoke(restored, "extractOneOutput", new Class<?>[]{ItemStack.class}, wine());
        helper.assertTrue(early.isEmpty() && batch(restored).brewingTicks() == 1 && carrier.getCount() == 3,
                "Even the tick before completion cannot extract or consume a bottle");
        restored.tick(helper.getLevel());
        helper.assertTrue(!batch(restored).brewing() && batch(restored).remainingTicks() == 36000
                        && !batch(restored).hasPendingOutput() && restored.getOutput().getStackInSlot(0).getCount() == 2
                        && restored.getBrewLevel() == 3 && translation(TavernQuality.barrelTooltip(restored).get(2)).endsWith(".aging"),
                "The deadline generates the exact batch once, then starts the full QF aging countdown");
        var completedSave = restored.saveWithoutMetadata();
        restored.load(completedSave);
        restored.tick(helper.getLevel());
        helper.assertTrue(restored.getOutput().getStackInSlot(0).getCount() == 2 && !batch(restored).hasPendingOutput(),
                "Reloading a completed batch cannot generate its output twice");
        var output = (ItemStack) invoke(restored, "extractOneOutput", new Class<?>[]{ItemStack.class}, wine());
        helper.assertTrue(output.is(wine().getItem()) && batch(restored).remainingTicks() == 35999
                        && restored.getOutput().getStackInSlot(0).getCount() == 1,
                "Bottling becomes available after brewing without resetting the remaining batch");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernUnfinishedSavedWineIsHiddenUntilItsDeadline(GameTestHelper helper) throws Exception {
        var barrel = barrel(helper);
        barrel.getIngredient().setStackInSlot(0, new ItemStack(Items.APPLE, 2));
        start(barrel, recipe(wine()), helper.getLevel());
        barrel.tick(helper.getLevel());
        var previousBuild = barrel.saveWithoutMetadata();
        var state = previousBuild.getCompound(TavernBarrelBatch.NBT_KEY);
        state.putInt("version", 2);
        state.putInt("brewing_ticks", 2);
        state.remove("pending_output");
        barrel.load(previousBuild);
        helper.assertTrue(batch(barrel).pendingOutput().getCount() == 2,
                "Loading an unfinished earlier test batch removes its premature output from the native inventory");
        assertNoBrewedOutput(helper, barrel);
        barrel.tick(helper.getLevel());
        assertNoBrewedOutput(helper, barrel);
        barrel.tick(helper.getLevel());
        helper.assertTrue(barrel.getOutput().getStackInSlot(0).getCount() == 2 && !batch(barrel).hasPendingOutput(),
                "The saved unfinished batch generates its original quantity exactly once at completion");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernKeepsUnmatchedInputsAndPreviouslySavedVinegar(GameTestHelper helper) throws Exception {
        var barrel = barrel(helper);
        var ingredient = new ItemStack(Items.COBBLESTONE, 2);
        var fluid = new FluidStack(ForgeRegistries.FLUIDS.getValue(id("kaleidoscope_tavern:grape_juice")), 4000);
        barrel.getIngredient().setStackInSlot(0, ingredient.copy());
        barrel.getFluid().fill(fluid.copy(), IFluidHandler.FluidAction.EXECUTE);

        invoke(barrel, "lambda$tick$1", new Class<?>[0]);
        helper.assertTrue(batch(barrel) == null && barrel.getBrewLevel() == 0
                        && barrel.getOutput().getStackInSlot(0).isEmpty()
                        && ItemStack.matches(barrel.getIngredient().getStackInSlot(0), ingredient)
                        && barrel.getFluid().getFluidAmount() == 4000,
                "Closing a full barrel without a recipe preserves all materials and creates no batch");

        var oldBarrel = barrel(helper);
        var tag = oldBarrel.saveWithoutMetadata();
        tag.putInt("brew_level", 1);
        tag.putInt("brew_time", 1);
        tag.putString("recipe_id", BarrelRecipeSerializer.EMPTY_RECIPE_ID.toString());
        var previous = new TavernBarrelBatch(Quality.GOLD, 1, false);
        previous.deferOutput(new ItemStack(ModItems.VINEGAR.get(), 16));
        var previousTag = previous.save();
        previousTag.putInt("version", 3);
        tag.put(TavernBarrelBatch.NBT_KEY, previousTag);
        oldBarrel.load(tag);
        assertNoBrewedOutput(helper, oldBarrel);
        oldBarrel.tick(helper.getLevel());

        var tank = batch(oldBarrel).fluidOutput();
        int servings = tank == null ? oldBarrel.getOutput().getStackInSlot(0).getCount() : tank.servings();
        helper.assertTrue(servings == 16 && batch(oldBarrel).finished(),
                "The saved legacy vinegar batch retains its original servings and quality state");
        var bottle = (ItemStack) invoke(oldBarrel, "extractOneOutput", new Class<?>[]{ItemStack.class},
                new ItemStack(ModItems.EMPTY_BOTTLE.get()));
        helper.assertTrue(bottle.is(ModItems.VINEGAR.get()) && QualityUtils.getQuality(bottle) == Quality.GOLD,
                "Legacy stored vinegar still bottles with its original quality");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernVinegarAndAllCocktailsUseQfEffectsAndTooltips(GameTestHelper helper) {
        var ordinary = List.of(new DrinkEffectData.Entry(MobEffects.MOVEMENT_SPEED, 60, 0, 1),
                new DrinkEffectData.Entry(MobEffects.MOVEMENT_SLOWDOWN, 60, 3, 1));
        var elite = List.of(new DrinkEffectData.Entry(MobEffects.DAMAGE_BOOST, 300, 10, 1));
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        List<Item> drinks = new ArrayList<>(cocktailItems());
        drinks.add(ModItems.VINEGAR.get());
        for (var item : drinks) {
            var previous = DrinkEffectDataReloadListener.INSTANCE.get(item);
            boolean vinegar = item == ModItems.VINEGAR.get();
            DrinkEffectDataReloadListener.INSTANCE.put(item, new DrinkEffectData(item,
                    List.of(vinegar ? elite : ordinary, elite, vinegar ? ordinary : elite, elite, elite, elite)));
            try {
                for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
                    var bottle = TavernQuality.qualify(new ItemStack(item), quality);
                    if (item instanceof SignatureCocktailBlockItem) {
                        SignatureCocktailBlockItem.setEffects(bottle, ordinary);
                        SignatureCocktailBlockItem.setColor(bottle, 0x5a6b7c);
                    }
                    var base = new FoodProperties.Builder().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200), 1)
                            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 3), 1).build();
                    var expected = FoodUtils.handleFoodProperties(bottle, base).getEffects();
                    helper.assertTrue(TavernQuality.effects(bottle).equals(expected),
                            "Vinegar uses ordinary effects, cocktails use their own first-level or custom NBT effects through QF");
                    player.removeAllEffects();
                    item.finishUsingItem(bottle.copy(), helper.getLevel(), player);
                    helper.assertTrue(!player.hasEffect(MobEffects.DAMAGE_BOOST), "Never substitute unrelated native effect levels");
                    for (var entry : expected) {
                        if (entry.getSecond() >= 1) helper.assertTrue(entry.getFirst().equals(player.getEffect(entry.getFirst().getEffect())),
                                "Vinegar and every cocktail apply guaranteed QF-scaled effects exactly once");
                    }
                    List<Component> tooltip = new ArrayList<>();
                    item.appendHoverText(bottle, helper.getLevel(), tooltip, TooltipFlag.NORMAL);
                    helper.assertTrue(tooltip.stream().filter(line -> translation(line).endsWith("tavern.quality")).count() == 1,
                            "Vinegar and cocktail tooltip contain one translated QF quality row");
                    if (item instanceof SignatureCocktailBlockItem) {
                        helper.assertTrue(SignatureCocktailBlockItem.getColor(bottle) == 0x5a6b7c
                                        && SignatureCocktailBlockItem.getEffects(bottle).equals(ordinary),
                                "QF effect handling preserves signature color and custom effects NBT");
                    }
                }
            } finally {
                player.removeAllEffects();
                player.getInventory().clearContent();
                if (previous == null) DrinkEffectDataReloadListener.INSTANCE.remove(item);
                else DrinkEffectDataReloadListener.INSTANCE.put(item, previous);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernNativeInstantEffectsSurviveAllQualities(GameTestHelper helper) {
        String[][] drinks = {
                {"carignan", "minecraft:instant_health"},
                {"godfather", "kaleidoscope_tavern:zenith"},
                {"screwdriver", "kaleidoscope_tavern:upside_down"},
                {"sculk_special", "kaleidoscope_tavern:shriek_attack"}
        };
        for (var drink : drinks) {
            var item = ForgeRegistries.ITEMS.getValue(id("kaleidoscope_tavern:" + drink[0]));
            var instant = ForgeRegistries.MOB_EFFECTS.getValue(id(drink[1]));
            helper.assertTrue(item != null && instant != null && instant.isInstantenous(), "Load the real instant-effect fixture");
            var data = DrinkEffectDataReloadListener.INSTANCE.get(item);
            int level = TavernQuality.isBarrelProduct(new ItemStack(item)) ? TavernQuality.NORMAL_BREW_LEVEL : 1;
            var source = data.effects().get(level - 1).stream().filter(entry -> entry.effect() == instant).findFirst().orElseThrow();
            helper.assertTrue(source.duration() == 0, "Exercise the native zero-duration entry for " + drink[0]);
            for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
                var bottle = TavernQuality.qualify(new ItemStack(item), quality);
                var actual = TavernQuality.effects(bottle).stream().filter(entry -> entry.getFirst().getEffect() == instant)
                        .findFirst().orElseThrow(() -> new AssertionError("Missing instant effect for " + drink[0] + " / " + quality));
                // A positive-duration control already passes QF's original validation and exposes its exact scaling.
                var control = new FoodProperties.Builder().effect(() -> new MobEffectInstance(instant, 20, source.amplifier()),
                        source.probability()).build();
                var expected = FoodUtils.handleFoodProperties(bottle, control).getEffects().get(0);
                helper.assertTrue(actual.getFirst().getDuration() == 0
                                && actual.getFirst().getAmplifier() == expected.getFirst().getAmplifier()
                                && actual.getSecond().equals(expected.getSecond()),
                        "Keep instant duration zero and preserve QF amplifier/probability for " + drink[0] + " / " + quality);
                if (drink[0].equals("carignan")) {
                    var drinker = EntityType.PIG.create(helper.getLevel());
                    drinker.setHealth(2);
                    TavernQuality.drink(bottle, drinker);
                    helper.assertTrue(drinker.getHealth() > 2, "Zero-duration healing actually applies at " + quality);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernSignatureInstantEffectsKeepQfScalingAndFiltering(GameTestHelper helper) {
        var heal = ForgeRegistries.MOB_EFFECTS.getValue(id("minecraft:instant_health"));
        var harm = ForgeRegistries.MOB_EFFECTS.getValue(id("minecraft:instant_damage"));
        var source = List.of(new DrinkEffectData.Entry(heal, 0, 0, 0.4f),
                new DrinkEffectData.Entry(harm, 0, 2, 1),
                new DrinkEffectData.Entry(MobEffects.MOVEMENT_SPEED, 0, 0, 1),
                new DrinkEffectData.Entry(MobEffects.REGENERATION, 10, 0, 1));
        for (Quality quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
            var bottle = TavernQuality.qualify(new ItemStack(ModItems.SIGNATURE_COCKTAIL.get()), quality);
            SignatureCocktailBlockItem.setEffects(bottle, source);
            var actual = TavernQuality.effects(bottle);
            var control = new FoodProperties.Builder().effect(() -> new MobEffectInstance(heal, 20), 0.4f)
                    .effect(() -> new MobEffectInstance(harm, 20, 2), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 200), 1).build();
            var expected = FoodUtils.handleFoodProperties(bottle, control).getEffects();
            helper.assertTrue(actual.size() == expected.size() + (quality == Quality.NONE ? 1 : 0),
                    "Only instant zero-duration effects bypass duration validation at " + quality);
            for (var entry : expected) {
                var effect = entry.getFirst();
                var restored = actual.stream().filter(candidate -> candidate.getFirst().getEffect() == effect.getEffect())
                        .findFirst().orElseThrow();
                helper.assertTrue(restored.getFirst().getAmplifier() == effect.getAmplifier()
                                && restored.getSecond().equals(entry.getSecond())
                                && restored.getFirst().getDuration() == (effect.getEffect().isInstantenous() ? 0 : effect.getDuration()),
                        "Signature NBT effects retain QF duration, amplifier and probability rules at " + quality);
            }
            helper.assertTrue(SignatureCocktailBlockItem.getEffects(bottle).equals(source), "Never rewrite stored cocktail effects");
            SignatureCocktailBlockItem.setEffects(bottle, List.of(new DrinkEffectData.Entry(harm, 0, 0, 1)));
            var weakControl = new FoodProperties.Builder().effect(() -> new MobEffectInstance(harm, 20), 1).build();
            var weakExpected = FoodUtils.handleFoodProperties(bottle, weakControl).getEffects();
            var weakActual = TavernQuality.effects(bottle);
            helper.assertTrue(weakActual.size() == weakExpected.size(),
                    "Keep QF's removal of harmful effects whose adjusted amplifier is negative at " + quality);
            if (!weakExpected.isEmpty()) {
                helper.assertTrue(weakActual.get(0).getFirst().getAmplifier() == weakExpected.get(0).getFirst().getAmplifier()
                                && weakActual.get(0).getSecond().equals(weakExpected.get(0).getSecond()),
                        "Retained harmful instant effects still use QF scaling and chance");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernShakerTimingAndMissingRecipeFallbacksRollQualityOnce(GameTestHelper helper) throws Exception {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        var item = (ShakerItem) ModItems.SHAKER.get();
        for (int usedTicks : List.of(20, 80, 95, 100)) {
            for (Quality quality : List.of(Quality.DIAMOND, Quality.GOLD, Quality.IRON, Quality.NONE)) {
                var shaker = new ItemStack(item);
                var input = new ItemStack(Items.APPLE);
                if (quality != Quality.NONE) QualityUtils.applyQuality(input, quality);
                var storage = new ItemStackHandler(3);
                storage.setStackInSlot(0, input.copy());
                ShakerItem.setStorage(shaker, storage);
                qffRandom().setSeed(104);
                Quality expected = QualityFoodFluidsCreateRules.rollBasinQuality(List.of(input.copy()), List.of());
                qffRandom().setSeed(104);
                item.releaseUsing(shaker, helper.getLevel(), player, item.getUseDuration(shaker) - usedTicks);
                var output = ShakerItem.getResult(shaker);
                var expectedItem = usedTicks == 20 || usedTicks == 100 ? ModItems.MYSTERY_COCKTAIL.get() : ModItems.SIGNATURE_COCKTAIL.get();
                helper.assertTrue(output.is(expectedItem) && QualityUtils.getQuality(output) == expected,
                        "Timing and no-recipe cocktails use their real ingredients in one QF roll: " + usedTicks + " " + quality);
                var stored = output.copy();
                ShakerItem.setResult(shaker, stored);
                helper.assertTrue(ItemStack.matches(stored, ShakerItem.getResult(shaker)),
                        "Serializing an existing shaker result never rerolls its quality");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void tavernCocktailPouringPlacementAndBothDropPathsKeepQuality(GameTestHelper helper) throws Exception {
        var pos = helper.absolutePos(new BlockPos(2, 1, 2));
        var emptyState = ((BlockItem) ModItems.EMPTY_GLASSWARE.get()).getBlock().defaultBlockState();
        var effects = List.of(new DrinkEffectData.Entry(MobEffects.MOVEMENT_SPEED, 60, 0, 1));
        var pour = ShakerItem.class.getDeclaredMethod("pourResult", Level.class, ItemStack.class, BlockPos.class,
                net.minecraft.world.level.block.state.BlockState.class);
        pour.setAccessible(true);
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        boolean creative = player.getAbilities().instabuild;
        player.getAbilities().instabuild = false;
        try {
            helper.getLevel().setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
            for (var item : cocktailItems()) {
                for (Quality quality : List.of(Quality.DIAMOND, Quality.GOLD, Quality.IRON, Quality.NONE)) {
                    helper.getLevel().setBlock(pos, emptyState, 3);
                    var shaker = new ItemStack(ModItems.SHAKER.get());
                    ShakerItem.setStorage(shaker, new ItemStackHandler(3));
                    ShakerItem.setResult(shaker, TavernQuality.qualify(new ItemStack(item), quality));
                    pour.invoke(shaker.getItem(), helper.getLevel(), shaker, pos, emptyState);
                    helper.assertTrue(helper.getLevel().getBlockState(pos).getBlock().asItem() == item
                                    && LevelData.get(helper.getLevel(), pos).level() == quality.level(),
                            "Poured cocktails store standard QF position quality for " + item + " " + quality);
                    var entity = helper.getLevel().getBlockEntity(pos);
                    if (item instanceof SignatureCocktailBlockItem) {
                        entity.getClass().getMethod("setEffects", List.class).invoke(entity, effects);
                        entity.getClass().getMethod("setColor", int.class).invoke(entity, 0x5a6b7c);
                    }
                    var state = helper.getLevel().getBlockState(pos);
                    var pickup = Block.getDrops(state, helper.getLevel(), pos, entity);
                    var broken = Block.getDrops(state, helper.getLevel(), pos, entity, player, ItemStack.EMPTY);
                    for (var drops : List.of(pickup, broken)) {
                        var output = drops.stream().filter(stack -> stack.is(item)).findFirst().orElse(ItemStack.EMPTY);
                        helper.assertTrue(!output.isEmpty() && QualityUtils.getQuality(output) == quality,
                                "Pickup and breaking preserve poured cocktail quality: " + item + " " + quality + " " + drops);
                        if (item instanceof SignatureCocktailBlockItem) {
                            helper.assertTrue(SignatureCocktailBlockItem.getEffects(output).equals(effects)
                                            && SignatureCocktailBlockItem.getColor(output) == 0x5a6b7c,
                                    "Reconstructed signature drops keep effects and color alongside QF quality");
                        }
                    }
                    player.getInventory().clearContent();
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    state.use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
                    helper.assertTrue(player.getInventory().items.stream().anyMatch(stack -> stack.is(item)
                                    && QualityUtils.getQuality(stack) == quality) && helper.getLevel().getBlockState(pos).isAir(),
                            "Empty-hand pickup returns a poured cocktail with the same quality: " + item + " " + quality);
                    helper.assertTrue(LevelData.get(helper.getLevel(), pos).level() == 0, "Removing a cocktail clears QF position quality");
                    var placed = TavernQuality.qualify(new ItemStack(item, 2), quality);
                    if (item instanceof SignatureCocktailBlockItem) {
                        SignatureCocktailBlockItem.setEffects(placed, effects);
                        SignatureCocktailBlockItem.setColor(placed, 0x5a6b7c);
                    }
                    player.setItemInHand(InteractionHand.MAIN_HAND, placed);
                    var hit = new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false);
                    var context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, placed, hit);
                    helper.assertTrue(((BlockItem) item).place(context).consumesAction(), "Place the qualified cocktail through BlockItem");
                    var returned = Block.getDrops(helper.getLevel().getBlockState(pos), helper.getLevel(), pos,
                            helper.getLevel().getBlockEntity(pos), player, ItemStack.EMPTY);
                    helper.assertTrue(returned.stream().anyMatch(stack -> stack.is(item) && QualityUtils.getQuality(stack) == quality),
                            "Manual placement and retrieval retain the same QF quality for " + item + " " + quality);
                    player.getInventory().clearContent();
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
                    helper.assertTrue(player.getInventory().items.stream().anyMatch(stack -> stack.is(item)
                                    && QualityUtils.getQuality(stack) == quality) && helper.getLevel().getBlockState(pos).isAir(),
                            "Empty-hand pickup also returns a manually placed cocktail with its quality");
                }
            }
        } finally {
            player.getInventory().clearContent();
            player.getAbilities().instabuild = creative;
        }
        helper.succeed();
    }

    private static TavernBarrelBatch batch(BarrelBlockEntity barrel) {
        return ((TavernBarrelAccess) barrel).createdelightcore$getQualityBatch();
    }

    private static List<Item> cocktailItems() {
        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item instanceof CocktailBlockItem && TavernQuality.supportsQuality(new ItemStack(item))).toList();
    }

    private static BarrelBlockEntity barrel(GameTestHelper helper) {
        var block = ForgeRegistries.BLOCKS.getValue(id("kaleidoscope_tavern:barrel"));
        var barrel = new BarrelBlockEntity(helper.absolutePos(new BlockPos(1, 1, 1)), block.defaultBlockState());
        barrel.setLevel(helper.getLevel());
        barrel.setOpen(false);
        return barrel;
    }

    private static void assertNoBrewedOutput(GameTestHelper helper, BarrelBlockEntity barrel) {
        helper.assertTrue(barrel.getOutput().getStackInSlot(0).isEmpty()
                        && barrel.getOutput().extractItem(0, 64, false).isEmpty(),
                "Unfinished wine does not exist in the real output inventory, even for direct extraction");
        for (int index = 0; index <= Direction.values().length; index++) {
            Direction side = index < Direction.values().length ? Direction.values()[index] : null;
            var fluid = barrel.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
            if (fluid == null) continue;
            for (int tank = 0; tank < fluid.getTanks(); tank++) {
                helper.assertTrue(fluid.getFluidInTank(tank).isEmpty(),
                        "The actual fluid view contains no unfinished wine from " + side);
            }
            helper.assertTrue(fluid.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE).isEmpty()
                            && fluid.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE).isEmpty(),
                    "No barrel fluid capability can expose or extract unfinished wine from " + side);
        }
    }

    private static ItemStack wine() { return new ItemStack(ForgeRegistries.ITEMS.getValue(id("kaleidoscope_tavern:wine"))); }

    private static BarrelRecipe recipe(ItemStack output) {
        return new BarrelRecipe(id("createdelight:test/tavern_wine"),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.APPLE)),
                ForgeRegistries.FLUIDS.getValue(id("kaleidoscope_tavern:grape_juice")), Ingredient.of(Items.GLASS_BOTTLE), output, 1);
    }

    private static void start(BarrelBlockEntity barrel, BarrelRecipe recipe, Level level) throws Exception {
        invoke(barrel, "lambda$tick$0", new Class<?>[]{BarrelRecipeContainer.class, Level.class, BarrelRecipe.class},
                new BarrelRecipeContainer(barrel.getIngredient(), barrel.getFluid()), level, recipe);
    }

    private static Object invoke(BarrelBlockEntity barrel, String name, Class<?>[] parameters, Object... args) throws Exception {
        var method = BarrelBlockEntity.class.getDeclaredMethod(name, parameters);
        method.setAccessible(true);
        return method.invoke(barrel, args);
    }

    private static RandomSource qffRandom() throws Exception {
        var field = QualityFoodFluidsCreateRules.class.getDeclaredField("RANDOM");
        field.setAccessible(true);
        return (RandomSource) field.get(null);
    }

    private static String translation(Component component) {
        return component.getContents() instanceof TranslatableContents translatable ? translatable.getKey() : "";
    }

    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }
}
