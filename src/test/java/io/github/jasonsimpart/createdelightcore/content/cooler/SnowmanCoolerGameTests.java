package io.github.jasonsimpart.createdelightcore.content.cooler;

import com.google.gson.JsonParser;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDArmInteractionPointTypes;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class SnowmanCoolerGameTests {
    private static final BlockPos POS = new BlockPos(2, 2, 2);

    private static FakePlayer player(GameTestHelper helper, GameType mode) {
        // GameTestHelper.makeMockPlayer() hardcodes isCreative() to true.
        var player = new FakePlayer(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "cooler-test"));
        player.setGameMode(mode);
        return player;
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void wrenchPicksUpEmptyAndFilledCoolersExactlyOnce(GameTestHelper helper) {
        var player = player(helper, GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, AllItems.WRENCH.asStack());
        var pos = helper.absolutePos(POS);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
        for (var block : new net.minecraft.world.level.block.Block[]{
                CDBlocks.EMPTY_SNOWMAN_COOLER.get(), CDBlocks.SNOWMAN_COOLER.get()}) {
            helper.setBlock(POS, block);
            helper.assertTrue(AllItems.WRENCH.get().useOn(context).consumesAction(), "Wrench handles cooler pickup");
            helper.assertTrue(helper.getLevel().isEmptyBlock(pos), "Wrench removes cooler block");
            helper.assertTrue(player.getInventory().countItem(block.asItem()) == 1, "Return exactly one cooler to inventory");
            AllItems.WRENCH.get().useOn(context);
            helper.assertTrue(player.getInventory().countItem(block.asItem()) == 1, "Repeated use cannot duplicate cooler");
        }
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1)).isEmpty(),
                "Inventory pickup must not also spawn item drops");
        helper.assertTrue(AllItems.WRENCH.isIn(player.getMainHandItem()), "Wrench is retained");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void survivalCaptureConsumesOneEmptyCooler(GameTestHelper helper) {
        for (int count : new int[]{1, 2}) {
            var player = player(helper, GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, CDBlocks.EMPTY_SNOWMAN_COOLER.asStack(count));
            var golem = helper.spawn(EntityType.SNOW_GOLEM, POS);
            player.interactOn(golem, InteractionHand.MAIN_HAND);
            helper.assertTrue(golem.isRemoved(), "Captured snow golem is removed");
            helper.assertTrue(player.getInventory().countItem(CDBlocks.EMPTY_SNOWMAN_COOLER.asItem()) == count - 1,
                    "Capture consumes exactly one empty cooler");
            helper.assertTrue(player.getInventory().countItem(CDBlocks.SNOWMAN_COOLER.asItem()) == 1,
                    "Capture grants exactly one filled cooler");
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void creativeCaptureKeepsEmptyCoolerAndOtherMobsAreRejected(GameTestHelper helper) {
        var player = player(helper, GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, CDBlocks.EMPTY_SNOWMAN_COOLER.asStack());
        var cow = helper.spawn(EntityType.COW, POS);
        helper.assertTrue(!player.interactOn(cow, InteractionHand.MAIN_HAND).consumesAction() && !cow.isRemoved(),
                "Empty cooler does not capture other mobs");
        var golem = helper.spawn(EntityType.SNOW_GOLEM, POS);
        player.interactOn(golem, InteractionHand.MAIN_HAND);
        helper.assertTrue(golem.isRemoved(), "Creative capture removes snow golem");
        helper.assertTrue(player.getInventory().countItem(CDBlocks.EMPTY_SNOWMAN_COOLER.asItem()) == 1
                && player.getInventory().countItem(CDBlocks.SNOWMAN_COOLER.asItem()) == 1,
                "Creative capture retains empty cooler and grants one filled cooler");
        helper.succeed();
    }

    private static SnowmanCoolerBlockEntity cooler(GameTestHelper helper) {
        helper.setBlock(POS, CDBlocks.SNOWMAN_COOLER.get());
        return (SnowmanCoolerBlockEntity) helper.getBlockEntity(POS);
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void liquidBatchesConserveFuelAndPersist(GameTestHelper helper) {
        var be = cooler(helper);
        var tank = be.getFluidTank();
        // Test-only fuel: 7 mB water pays for 20 ticks. Leave an unusable 6 mB remainder.
        helper.assertTrue(tank.fill(new FluidStack(Fluids.WATER, 20), FluidAction.EXECUTE) == 20, "Accept registered fuel");
        be.tick();
        helper.assertTrue(tank.getFluidAmount() == 13 && be.getRemainingBurnTime() == 20, "Charge exactly one batch");
        for (int i = 0; i < 5; i++) be.tick();
        CompoundTag saved = new CompoundTag();
        be.write(saved, false);
        tank.setFluid(FluidStack.EMPTY);
        be.read(saved, false);
        helper.assertTrue(tank.getFluidAmount() == 13 && be.getRemainingBurnTime() == 15, "Persist paid cycle and remaining fluid");
        for (int i = 0; i < 35; i++) be.tick();
        helper.assertTrue(tank.getFluidAmount() == 6 && be.getRemainingBurnTime() == 0
                && be.getHeatLevelFromBlock() == SnowmanCoolerBlock.HeatLevel.IDLE, "Only full batches provide cooling");
        helper.assertTrue(tank.drain(6, FluidAction.EXECUTE).getAmount() == 6, "Unusable residue remains drainable");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void bucketsAndArmSimulationDoNotLoseOrDuplicateItems(GameTestHelper helper) {
        var be = cooler(helper);
        var world = helper.getLevel();
        var pos = helper.absolutePos(POS);
        var state = be.getBlockState();
        var tank = be.getFluidTank();
        helper.assertTrue(be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new)
                .getTankCapacity(0) == 1000, "Advertise one bucket of fluid capacity");
        ItemStack bucket = new ItemStack(Items.WATER_BUCKET);
        var simulated = SnowmanCoolerBlock.tryInsert(state, world, pos, bucket.copy(), false, false, true);
        helper.assertTrue(simulated.getObject().is(Items.BUCKET) && tank.isEmpty(), "Simulation predicts empty bucket without filling tank");
        tank.fill(new FluidStack(Fluids.WATER, 500), FluidAction.EXECUTE);
        var blocked = SnowmanCoolerBlock.tryInsert(state, world, pos, bucket, false, false, false);
        helper.assertTrue(!blocked.getResult().consumesAction() && bucket.is(Items.WATER_BUCKET)
                && bucket.getCount() == 1 && tank.getFluidAmount() == 500, "Insufficient room must not consume bucket or partially fill");
        tank.drain(500, FluidAction.EXECUTE);
        var accepted = SnowmanCoolerBlock.tryInsert(state, world, pos, bucket, false, false, false);
        helper.assertTrue(accepted.getObject().is(Items.BUCKET) && bucket.isEmpty()
                && tank.getFluidAmount() == 1000, "Successful fill returns one bucket and transfers 1000 mB");
        helper.assertTrue(tank.fill(new FluidStack(Fluids.WATER, 1), FluidAction.EXECUTE) == 0
                && tank.getFluidAmount() == 1000, "A full tank rejects further pipe input");
        var arm = new CDArmInteractionPointTypes.SnowmanCoolerType().createPoint(world, pos, state);
        ItemStack snowballs = new ItemStack(Items.SNOWBALL, 2);
        helper.assertTrue(arm.insert(snowballs, true).getCount() == 1 && snowballs.getCount() == 2
                && be.getRemainingBurnTime() == 0, "Arm simulation leaves caller and cooler unchanged");
        helper.assertTrue(arm.insert(snowballs, false).getCount() == 1 && be.getRemainingBurnTime() == 1600,
                "Arm execution consumes exactly one solid fuel");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void freezingLiquidExpiresWithoutFreeCooling(GameTestHelper helper) {
        var be = cooler(helper);
        // Lava is a test fixture only: 10 mB -> 32 freezing ticks.
        be.getFluidTank().fill(new FluidStack(Fluids.LAVA, 10), FluidAction.EXECUTE);
        be.tick();
        helper.assertTrue(be.getHeatLevelFromBlock() == SnowmanCoolerBlock.HeatLevel.FREEZING, "Special liquid activates freezing");
        for (int i = 0; i < 32; i++) be.tick();
        helper.assertTrue(be.getRemainingBurnTime() == 0 && be.getHeatLevelFromBlock() == SnowmanCoolerBlock.HeatLevel.IDLE,
                "Special liquid cannot grant a free normal-fuel tail");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void basinRequiresCorrectColdGradeAndKeepsHeatRecipes(GameTestHelper helper) {
        var be = cooler(helper);
        helper.setBlock(POS.above(), AllBlocks.BASIN.get());
        BasinBlockEntity basin = (BasinBlockEntity) helper.getBlockEntity(POS.above());
        var items = basin.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        items.insertItem(0, new ItemStack(Items.ICE, 4), false);
        BasinRecipe cold = recipe("cooled"), frozen = recipe("frozen"), hot = recipe("heated");
        helper.assertTrue(!BasinRecipe.match(basin, cold), "Idle cooler cannot process cooled recipe");
        be.tryUpdateFuel(new ItemStack(Items.SNOWBALL), false, false);
        helper.assertTrue(BasinRecipe.match(basin, cold) && !BasinRecipe.match(basin, frozen)
                && !BasinRecipe.match(basin, hot), "Normal cold satisfies cooled only");
        be.tryUpdateFuel(CDItems.FROZEN_CAKE.asStack(), false, false);
        helper.assertTrue(BasinRecipe.match(basin, cold) && BasinRecipe.match(basin, frozen), "Freezing satisfies both cold grades");
        helper.assertTrue(items.getStackInSlot(0).getCount() == 4, "Recipe matching does not consume inputs");
        helper.setBlock(POS, AllBlocks.BLAZE_BURNER.getDefaultState().setValue(
                com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HEAT_LEVEL,
                com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED));
        basin.tick(); // Create caches the heat level within a basin tick.
        helper.assertTrue(BasinRecipe.match(basin, hot) && !BasinRecipe.match(basin, cold)
                && !BasinRecipe.match(basin, frozen), "Burner keeps heating recipes and cannot supply cooling");
        helper.succeed();
    }

    private static BasinRecipe recipe(String heat) {
        return new ProcessingRecipeBuilder<>(BasinRecipe::new, CreateDelightCore.id("test_" + heat))
                .require(Items.ICE).output(Items.BLUE_ICE)
                .requiresHeat(HeatCondition.valueOf(heat.toUpperCase(java.util.Locale.ROOT))).build();
    }

    @GameTest(template = "humidity_regulator_empty")
    public static void fuelFormatRejectsInvalidBatchSizes(GameTestHelper helper) {
        var special = SnowmanCoolerFuelManager.parseFuel(JsonParser.parseString("{\"superHeat\":true}").getAsJsonObject());
        helper.assertTrue(special.freezing() && special.burnTime() == 32 && special.amountConsumedPerTick() == 10,
                "Liquid Fuel field names and defaults remain compatible");
        for (String json : new String[]{"{\"burnTime\":0}", "{\"amountConsumedPerTick\":0}", "{\"amountConsumedPerTick\":1001}"}) {
            boolean rejected = false;
            try { SnowmanCoolerFuelManager.parseFuel(JsonParser.parseString(json).getAsJsonObject()); }
            catch (com.google.gson.JsonParseException expected) { rejected = true; }
            helper.assertTrue(rejected, "Reject invalid fuel: " + json);
        }
        helper.succeed();
    }
}
