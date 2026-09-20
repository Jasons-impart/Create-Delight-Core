package io.github.jasonsimpart.compat.mbd2;

import com.lance5057.butchercraft.workstations.hook.MeatHookBlockEntity;
import com.lance5057.butchercraft.workstations.butcherblock.ButcherBlockBlockEntity;
import com.lowdragmc.mbd2.api.blockentity.IMachineBlockEntity;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import com.lowdragmc.mbd2.common.machine.MBDMultiblockMachine;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.fluid.FluidTankCapabilityTrait;
import com.lowdragmc.mbd2.integration.create.CreateRotation;
import com.lowdragmc.mbd2.integration.create.CreateRotationRecipeCapability;
import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class MbdButcheryTests {
    private static final BlockPos CONTROLLER = new BlockPos(2, 1, 0);
    private static final BlockPos HOOK = new BlockPos(1, 4, 1);
    private static final BlockPos TABLE = new BlockPos(2, 2, 1);

    @GameTest(template = "mbd_butchery", templateNamespace = "createdelightcore", timeoutTicks = 800)
    public static void butcherHookAndBlood(GameTestHelper helper) { check(helper, "pig_carcass"); }

    @GameTest(template = "mbd_butchery", templateNamespace = "createdelightcore", rotationSteps = 1, timeoutTicks = 800)
    public static void butcherRotatedTable(GameTestHelper helper) { check(helper, "white_rabbit_carcass"); }

    private static void check(GameTestHelper helper, String carcass) {
        MbdMachineTests.preloadPatternChunks(helper, CONTROLLER);
        helper.runAfterDelay(1, () -> {
            var machine = ((IMachineBlockEntity) helper.getBlockEntity(CONTROLLER)).getMetaMachine();
            var facing = machine.getFrontFacing().orElse(Direction.NORTH);
            var motorPos = helper.absolutePos(new BlockPos(0, 1, 0)).relative(facing);
            helper.getLevel().setBlockAndUpdate(motorPos, com.simibubi.create.AllBlocks.CREATIVE_MOTOR.getDefaultState()
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, facing.getOpposite()));
            var motor = (com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) helper.getLevel().getBlockEntity(motorPos);
            motor.generatedSpeed.setValue(128);
        });
        MbdMachineTests.whenFormed(helper, CONTROLLER, () -> {
            var machine = (MBDMultiblockMachine) ((IMachineBlockEntity) helper.getBlockEntity(CONTROLLER)).getMetaMachine();
            helper.assertTrue(machine.checkPattern(), "Old butcher structure must match");
            helper.assertTrue(machine.isFormed(), "Butcher structure must form automatically");
            var input = (ItemSlotCapabilityTrait) machine.getTraitByName("andesite_import_item_slot");
            var output = (ItemSlotCapabilityTrait) machine.getTraitByName("andesite_export_item_slot_0");
            var tank = (FluidTankCapabilityTrait) machine.getTraitByName("andesite_export_fluid_tank");
            helper.assertTrue(input.storage.getSlots() == 1 && output.storage.getSlots() == 20, "Preserve 1 input / 20 output slots");
            helper.assertTrue(tank.storages[0].getCapacity() == 4000, "Preserve 4000 mB blood tank");
            var stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("butchercraft:" + carcass)));
            helper.assertTrue(input.storage.insertItem(0, new ItemStack(Items.STONE), false).getCount() == 1, "Reject non-carcass inputs");
            helper.assertTrue(input.storage.insertItem(0, stack, false).isEmpty(), "Accept old carcass input tag");
            var kinetic = (MBDKineticMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0));
            var hookCapability = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(HOOK), null);
            var tableCapability = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(TABLE), null);
            helper.assertTrue(hookCapability != null && tableCapability != null, "Normal workstations retain item capabilities");
            var builder = MBDRecipeBuilder.of(MbdCompat.id("test/butcher"), MBDRegistries.RECIPE_TYPES.get(MbdCompat.id("butchery")))
                    .inputItems(stack).outputItems(new ItemStack(Items.BONE, 13))
                    .outputFluids(new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("butchercraft:blood_fluid")), 1000))
                    .duration(100);
            builder.perTick = true;
            var recipe = builder.input(CreateRotationRecipeCapability.CAP, CreateRotation.stress(1024)).buildRawRecipe();
            var motorPos = kinetic.getBlockPos().relative(machine.getFrontFacing().orElse(Direction.NORTH));
            var motor = (com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) helper.getLevel().getBlockEntity(motorPos);
            helper.assertTrue(Math.abs(kinetic.getSpeed()) == 128, "Motor must drive input hatch at 128 RPM");
            machine.getRecipeLogic().setupRecipe(recipe);
            helper.assertTrue(input.storage.getStackInSlot(0).isEmpty(), "Consume exactly one carcass");
            helper.runAfterDelay(45, () -> {
                var hook = (MeatHookBlockEntity) helper.getBlockEntity(HOOK);
                var table = (ButcherBlockBlockEntity) helper.getBlockEntity(TABLE);
                boolean big = carcass.equals("pig_carcass");
                helper.assertTrue(big ? !hook.isEmpty() && hook.stage > 0 : !table.isEmpty() && table.stage > 0,
                        "Animate the correct workstation and advance stages; status=" + machine.getMachineStateName());
                var cached = big ? hookCapability : tableCapability;
                helper.assertTrue(cached.getStackInSlot(0).isEmpty() && cached.extractItem(0, 1, true).isEmpty()
                        && cached.extractItem(0, 1, false).isEmpty(), "A capability cached before processing must not expose or extract synthetic carcasses");
                var attempted = new ItemStack(stack.getItem());
                helper.assertTrue(cached.insertItem(0, attempted, false).getCount() == 1, "Automation cannot replace a synthetic display");
                if (cached instanceof net.neoforged.neoforge.items.IItemHandlerModifiable modifiable) {
                    modifiable.setStackInSlot(0, ItemStack.EMPTY);
                    helper.assertTrue(big ? !hook.isEmpty() : !table.isEmpty(), "Modifiable capability cannot clear a synthetic display");
                }
                var root = helper.absolutePos(big ? HOOK : TABLE);
                assertInteraction(helper, root, true);
                assertInteraction(helper, big ? root.below() : root.above(), true);
                if (big && helper.getLevel().getBlockState(root.below(2)).getBlock()
                        instanceof com.lance5057.butchercraft.workstations.hook.MeatHookBlock) {
                    assertInteraction(helper, root.below(2), true);
                }
                var otherCarcass = BuiltInRegistries.ITEM.get(ResourceLocation.parse(
                        big ? "butchercraft:white_rabbit_carcass" : "butchercraft:pig_carcass"));
                var switched = MBDRecipeBuilder.of(MbdCompat.id("test/display_switch"), recipe.recipeType)
                        .inputItems(otherCarcass).buildRawRecipe();
                MbdButchery.updateDisplay(machine, switched);
                helper.assertTrue(big ? hook.isEmpty() && !table.isEmpty() : !hook.isEmpty() && table.isEmpty(),
                        "Direct recipe transitions must release the previous display workstation");
                var noCarcass = MBDRecipeBuilder.of(MbdCompat.id("test/no_display"), recipe.recipeType).buildRawRecipe();
                MbdButchery.updateDisplay(machine, noCarcass);
                helper.assertTrue(hook.isEmpty() && table.isEmpty(), "A recipe without carcasses must release all display inventories");
                assertInteraction(helper, helper.absolutePos(HOOK), false);
                assertInteraction(helper, helper.absolutePos(TABLE), false);
                MbdButchery.updateDisplay(machine, recipe);
                helper.assertTrue(machine.checkPattern(), "Occupied animated workstations must still form");
                int progress = machine.getRecipeLogic().getProgress();
                motor.generatedSpeed.setValue(0);
                helper.runAfterDelay(25, () -> {
                    helper.assertTrue(machine.getRecipeLogic().getProgress() <= progress + 1, "Loss of kinetic input must stop processing");
                    helper.assertTrue(machine.getRecipeLogic().getProgress() == 0, "Preserve old damping of 2 progress/tick while waiting");
                    helper.assertTrue(hook.isEmpty() && table.isEmpty(), "Idle animation must clear both displays");
                    motor.generatedSpeed.setValue(128);
                    helper.runAfterDelay(125, () -> {
                        helper.assertTrue(output.storage.getStackInSlot(0).is(Items.BONE) && output.storage.getStackInSlot(0).getCount() == 13,
                                "Processing must produce exact items once; output=" + output.storage.getStackInSlot(0)
                                + ", status=" + machine.getRecipeLogic().getStatus() + ", progress=" + machine.getRecipeLogic().getProgress()
                                + ", reason=" + machine.getRecipeLogic().getWaitingReason() + ", speed=" + kinetic.getSpeed()
                                + ", formed=" + machine.isFormed());
                        helper.assertTrue(tank.storages[0].getFluidAmount() == 1000, "Processing must produce exact blood amount once");
                        var facing = machine.getFrontFacing().orElse(Direction.NORTH);
                        var port = helper.absolutePos(new BlockPos(3, 1, 0));
                        var fluid = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, port, facing);
                        helper.assertTrue(fluid != null && fluid.getFluidInTank(0).getAmount() == 1000, "Export bus must expose controller blood tank");
                        helper.assertTrue(hook.isEmpty() && table.isEmpty(), "Completed processing clears synthetic displays");
                        MbdButchery.updateDisplay(machine, recipe);
                        machine.setFrontFacing(facing.getClockWise());
                        MbdButchery.invalid(new com.lowdragmc.mbd2.common.machine.definition.config.event.MachineStructureInvalidEvent(machine));
                        helper.assertTrue(hook.isEmpty() && table.isEmpty(), "Invalidation after rotation must release displays at the old facing");
                        assertInteraction(helper, helper.absolutePos(big ? HOOK : TABLE), false);
                        hook.insertItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("butchercraft:pig_carcass"))));
                        helper.assertTrue(!hookCapability.extractItem(0, 1, true).isEmpty(), "Cached automation resumes for player-owned items after processing");
                        assertInteraction(helper, helper.absolutePos(HOOK), false);
                        helper.setBlock(CONTROLLER, Blocks.AIR);
                        helper.assertTrue(machine.getRecipeLogic().getProgress() == 0, "Preserve old damping of 2 progress/tick while waiting");
                        helper.assertTrue(!hook.isEmpty() && table.isEmpty(), "Removing controller must preserve player-owned carcasses");
                        helper.succeed();
                    });
                });
            });
        });
    }

    private static void assertInteraction(GameTestHelper helper, BlockPos pos, boolean blocked) {
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        var hit = new net.minecraft.world.phys.BlockHitResult(pos.getCenter(), Direction.NORTH, pos, false);
        var event = new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(
                player, net.minecraft.world.InteractionHand.MAIN_HAND, pos, hit);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
        helper.assertTrue(event.isCanceled() == blocked, "Only synthetic carcass interactions must be blocked at " + pos);
    }
}
