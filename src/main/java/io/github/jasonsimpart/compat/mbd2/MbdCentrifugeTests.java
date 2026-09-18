package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.blockentity.IMachineBlockEntity;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import com.lowdragmc.mbd2.common.machine.MBDMultiblockMachine;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.fluid.FluidTankCapabilityTrait;
import com.lowdragmc.mbd2.integration.create.CreateRotation;
import com.lowdragmc.mbd2.integration.create.CreateRotationRecipeCapability;
import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlockEntity;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class MbdCentrifugeTests {
    private static final BlockPos CONTROLLER = new BlockPos(2, 2, 0);
    private static final BlockPos KINETIC = new BlockPos(2, 2, 2);
    private static final BlockPos MOTOR = new BlockPos(2, 1, 2);

    @GameTest(template = "mbd_centrifuge", templateNamespace = "createdelightcore", timeoutTicks = 500)
    public static void centrifugeFuelAndProcessing(GameTestHelper helper) {
        var type = MBDRegistries.RECIPE_TYPES.get(MbdCompat.id("big_centrifugation"));
        var fuelType = MBDRegistries.RECIPE_TYPES.get(MbdCompat.id("big_centrifugation_fuel"));
        var fuel = MBDRecipeBuilder.of(MbdCompat.id("test/centrifuge_fuel"), fuelType)
                .input(CreateRotationRecipeCapability.CAP, CreateRotation.rpm(32)).duration(10).buildRawRecipe();
        var recipes = new java.util.ArrayList<>(helper.getLevel().getRecipeManager().getRecipes());
        recipes.add(new RecipeHolder<>(fuel.id, fuel));
        helper.getLevel().getRecipeManager().replaceRecipes(recipes);
        helper.runAfterDelay(1, () -> {
            helper.setBlock(MOTOR, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP));
            ((CreativeMotorBlockEntity) helper.getBlockEntity(MOTOR)).generatedSpeed.setValue(16);
        });
        helper.runAfterDelay(120, () -> {
            var machine = (MBDMultiblockMachine) ((IMachineBlockEntity) helper.getBlockEntity(CONTROLLER)).getMetaMachine();
            helper.assertTrue(machine.checkPattern() && machine.isFormed(), "Legacy centrifuge must form automatically");
            var input = (ItemSlotCapabilityTrait) machine.getTraitByName("steel_import_item_slot");
            var output = (ItemSlotCapabilityTrait) machine.getTraitByName("steel_export_item_slot");
            var fluidIn = (FluidTankCapabilityTrait) machine.getTraitByName("steel_import_fluid_tank");
            var fluidOut = (FluidTankCapabilityTrait) machine.getTraitByName("steel_export_fluid_tank");
            helper.assertTrue(input.storage.getSlots() == 4 && output.storage.getSlots() == 9, "Preserve 4/9 item slots");
            helper.assertTrue(fluidIn.storages.length == 1 && fluidOut.storages.length == 2 && fluidOut.storages[0].getCapacity() == 8000,
                    "Preserve one input/two output 8000mB tanks");
            var kinetic = (MBDKineticMachineBlockEntity) helper.getBlockEntity(KINETIC);
            helper.assertTrue(Math.abs(kinetic.getSpeed()) == 16, "Motor must connect through the bottom kinetic hatch");
            helper.assertTrue(!fuel.matchRecipe(machine).isSuccess(), "Below 32 RPM must fail fuel requirement");
            input.storage.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
            fluidIn.storages[0].setFluid(new FluidStack(Fluids.WATER, 1000));
            var recipe = MBDRecipeBuilder.of(MbdCompat.id("test/centrifuge"), type).inputItems(Items.IRON_INGOT)
                    .inputFluids(new FluidStack(Fluids.WATER, 1000)).outputItems(Items.GOLD_INGOT)
                    .outputFluids(new FluidStack(Fluids.LAVA, 500)).duration(100).buildRawRecipe();
            machine.getRecipeLogic().setupRecipe(recipe);
            helper.assertTrue(!input.storage.getStackInSlot(0).isEmpty(), "Insufficient rotation must not consume input");
            var motor = (CreativeMotorBlockEntity) helper.getBlockEntity(MOTOR);
            motor.generatedSpeed.setValue(32);
            helper.runAfterDelay(10, () -> {
                helper.assertTrue(fuel.matchRecipe(machine).isSuccess(), "32 RPM must satisfy the original fuel condition");
                helper.assertTrue(machine.doModifyRecipe(recipe).duration == 200, "32 RPM: controller 2x, rotor 0.25x, speed 4x duration");
                helper.assertTrue(machine.getDefinition().recipeLogicSettings().recipeModifiers().getMaxParallel(machine.getRecipeLogic(), recipe).apply(1).intValue() == 16,
                        "Preserve 16 maximum parallel recipes");
                motor.generatedSpeed.setValue(256);
                helper.runAfterDelay(10, () -> {
                    var modified = machine.doModifyRecipe(recipe);
                    helper.assertTrue(modified.duration == 50, "256 RPM: rotor and controller give 0.5x duration");
                    machine.getRecipeLogic().setupRecipe(modified);
                    helper.assertTrue(input.storage.getStackInSlot(0).isEmpty() && fluidIn.storages[0].isEmpty(), "Consume item and fluid once; item=" + input.storage.getStackInSlot(0) + ", fluid=" + fluidIn.storages[0].getFluidAmount()
                            + ", status=" + machine.getRecipeLogic().getStatus() + ", fuel=" + machine.getRecipeLogic().getFuelTime()
                            + ", candidates=" + type.searchFuelRecipe(helper.getLevel().getRecipeManager(), machine).size());
                    helper.runAfterDelay(65, () -> {
                        helper.assertTrue(output.storage.getStackInSlot(0).is(Items.GOLD_INGOT), "Centrifuge must finish processing");
                        helper.assertTrue(fluidOut.storages[0].getFluidAmount() == 500, "Preserve exact fluid yield");
                        helper.succeed();
                    });
                });
            });
        });
    }
}
