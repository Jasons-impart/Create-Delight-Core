package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.blockentity.IMachineBlockEntity;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import com.lowdragmc.mbd2.common.capability.recipe.ForgeEnergyRecipeCapability;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.MBDMultiblockMachine;
import com.lowdragmc.mbd2.common.trait.fluid.FluidTankCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.forgeenergy.ForgeEnergyCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class MbdReactorTests {
    @GameTest(template = "mbd_reactor", templateNamespace = "createdelightcore", timeoutTicks = 650)
    public static void reactorTwoAssemblies(GameTestHelper helper) { run(helper, 2); }

    @GameTest(template = "mbd_reactor_full", templateNamespace = "createdelightcore", timeoutTicks = 650)
    public static void reactorSeventyFourAssemblies(GameTestHelper helper) { run(helper, 74); }

    private static void run(GameTestHelper helper, int count) {
        helper.runAfterDelay(300, () -> {
            var machine = (MBDMultiblockMachine) ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(4, 1, 0))).getMetaMachine();
            helper.assertTrue(machine.checkPattern() && machine.isFormed(), "Original 9x10x10 reactor must form");
            helper.assertTrue(machine.getCustomData().getInt("assembly_count") == count, "Preserve 2..74 fuel assembly range");
            helper.assertTrue(Math.abs(MbdReactor.data(machine, "temperature") - 298.15) < 0.001, "Cold reactor starts at ambient temperature");
            var input = (ItemSlotCapabilityTrait) machine.getTraitByName("forged_steel_import_item_slot");
            var fluidIn = (FluidTankCapabilityTrait) machine.getTraitByName("forged_steel_import_fluid_tank");
            var fluidOut = (FluidTankCapabilityTrait) machine.getTraitByName("forged_steel_export_fluid_tank");
            var energy = (ForgeEnergyCapabilityTrait) machine.getTraitByName("forged_steel_export_energy_storage");
            helper.assertTrue(input.storage.getSlots() == 1 && fluidIn.storages[0].getCapacity() == 8000
                    && fluidOut.storages[0].getCapacity() == 8000 && energy.storage.getMaxEnergyStored() == 10000000, "Reactor storage capacities");
            var type = MBDRegistries.RECIPE_TYPES.get(MbdCompat.id("fission_react"));
            var fuelType = MBDRegistries.RECIPE_TYPES.get(MbdCompat.id("fission_react_fuel"));
            var fuelBuilder = MBDRecipeBuilder.of(MbdCompat.id("test/reactor_fuel"), fuelType);
            fuelBuilder.slotName = "fuel";
            var fuel = fuelBuilder.inputItems(Items.COAL).duration(800).buildRawRecipe();
            var recipes = new java.util.ArrayList<>(helper.getLevel().getRecipeManager().getRecipes());
            recipes.removeIf(r -> r.id().equals(fuel.id));
            recipes.add(new RecipeHolder<>(fuel.id, fuel));
            helper.getLevel().getRecipeManager().replaceRecipes(recipes);
            MbdRecipeValidation.started(new net.neoforged.neoforge.event.server.ServerStartedEvent(helper.getLevel().getServer()));
            input.storage.setStackInSlot(0, new ItemStack(Items.COAL));
            helper.assertTrue(machine.modifyFuelRecipe(fuel) == null, "Inserted control rods forbid new fuel");
            var recipe = MBDRecipeBuilder.of(MbdCompat.id("test/reactor_water"), type)
                    .inputFluids(new FluidStack(Fluids.WATER, 20)).outputFluids(new FluidStack(Fluids.LAVA, 20))
                    .output(ForgeEnergyRecipeCapability.CAP, 40960).duration(1).buildRawRecipe();
            MbdReactor.set(machine, "burning_rate", 1);
            fluidIn.storages[0].setFluid(new FluidStack(Fluids.WATER, 8000));
            helper.assertTrue(machine.modifyFuelRecipe(fuel) != null, "Removed control rods enable fuel");
            helper.assertTrue(machine.getRecipeLogic().handleFuelRecipe(), "Reactor consumes fuel using named fuel slot");
            helper.assertTrue(input.storage.getStackInSlot(0).isEmpty() && machine.getRecipeLogic().getFuelTime() == 800, "Fuel burns for original 800 ticks");
            helper.assertTrue(Math.abs(MbdReactor.heat(machine) - count * 0.08) < 0.00001, "Original cold heat rate");
            var cold = machine.doModifyRecipe(recipe);
            helper.assertTrue(cold.outputs.isEmpty(), "No power or waste below 1000K");
            machine.getRecipeLogic().setupRecipe(cold);
            MbdReactor.set(machine, "temperature", 1250);
            MbdReactor.set(machine, "multiplier", 2);
            machine.getRecipeLogic().onRecipeFinish();
            helper.assertTrue(machine.getRecipeLogic().isIdle(), "Heating must invalidate the cached cold recipe instead of repeating its empty outputs");
            var modified = machine.doModifyRecipe(recipe);
            long expectedEnergy = (Integer) modified.getOutputContents(ForgeEnergyRecipeCapability.CAP).getFirst().content;
            helper.assertTrue(expectedEnergy > 40960, "Assembly and temperature multipliers increase output");
            machine.getRecipeLogic().setupRecipe(modified);
            helper.assertTrue(Math.abs(MbdReactor.cooling(machine) - 0.024 * count) < 0.00001, "Working water cooling rate");
            helper.runAfterDelay(4, () -> {
                helper.assertTrue(energy.storage.getEnergyStored() == expectedEnergy, "Exact modified FE output");
                helper.assertTrue(fluidOut.storages[0].getFluidAmount() > 20, "Modified waste output");
                MbdReactor.set(machine, "burning_rate", 0);
                helper.assertTrue(MbdReactor.heat(machine) == 0 && MbdReactor.amount(machine, 20, true) == 0, "Emergency stop removes heat and coolant demand");
                var port = (MBDMachine) ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 1))).getMetaMachine();
                port.getCustomData().putInt("state", 2);
                MbdReactor.set(machine, "temperature", 1600);
                MbdReactor.control(port);
                helper.assertTrue(port.getOutputSignal(port.getFrontFacing().orElse(net.minecraft.core.Direction.NORTH)) == 15, "High temperature mode emits 15 redstone");
                MbdReactor.set(machine, "temperature", 298.15);
                MbdReactor.control(port);
                helper.assertTrue(port.getOutputSignal(port.getFrontFacing().orElse(net.minecraft.core.Direction.NORTH)) == 0, "Normal temperature clears alarm");
                var facing = port.getFrontFacing().orElse(net.minecraft.core.Direction.NORTH);
                port.getCustomData().putInt("state", 5);
                MbdReactor.control(port);
                helper.assertTrue(port.getOutputSignal(facing) == 15, "Empty fuel alarm");
                input.storage.setStackInSlot(0, new ItemStack(Items.COAL));
                MbdReactor.control(port);
                helper.assertTrue(port.getOutputSignal(facing) == 0, "Fuel insertion clears alarm");
                port.getCustomData().putInt("state", 3);
                fluidOut.storages[0].setFluid(new FluidStack(Fluids.LAVA, 8000));
                MbdReactor.control(port);
                helper.assertTrue(port.getOutputSignal(facing) == 15, "Waste output blockage alarm");
                fluidOut.storages[0].setFluid(FluidStack.EMPTY);
                MbdReactor.control(port);
                helper.assertTrue(port.getOutputSignal(facing) == 0, "Draining waste clears alarm");
                port.getCustomData().putInt("state", 4);
                energy.storage.receiveEnergy(10000000, false);
                MbdReactor.control(port);
                helper.assertTrue(port.getOutputSignal(facing) == 15, "Power output blockage alarm");
                energy.storage.extractEnergy(10000000, false);
                MbdReactor.control(port);
                helper.assertTrue(port.getOutputSignal(facing) == 0, "Extracting power clears alarm");
                port.getCustomData().putInt("state", 1);
                port.getCustomData().putInt("last_signal", 0);
                var signalPos = port.getPos().relative(facing);
                var observer = net.minecraft.world.level.block.Blocks.OBSERVER.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.ObserverBlock.FACING, facing)
                        .setValue(net.minecraft.world.level.block.ObserverBlock.POWERED, true);
                helper.getLevel().setBlock(signalPos, observer.setValue(net.minecraft.world.level.block.ObserverBlock.POWERED, false), 2);
                helper.getLevel().setBlock(signalPos, observer, 2);
                helper.assertTrue(MbdReactor.signal(port) == 15, "Observer output directed toward the control port must be detected");
                helper.getLevel().setBlock(signalPos, observer.setValue(net.minecraft.world.level.block.ObserverBlock.FACING, facing.getOpposite()), 2);
                helper.assertTrue(MbdReactor.signal(port) == 0, "Observer output directed away from the control port must be ignored");
                helper.getLevel().setBlock(signalPos, net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
                helper.assertTrue(helper.getLevel().getBlockState(signalPos).is(net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK), "Signal block must persist before control tick");
                MbdReactor.control(port);
                helper.assertTrue(MbdReactor.data(machine, "burning_rate") == 1, "Rising edge starts reactor; facing=" + facing + ", signal=" + MbdReactor.signal(port) + ", state=" + port.getMachineStateName() + ", source=" + helper.getLevel().getBlockState(signalPos) + ", pos=" + signalPos + ", port=" + port.getPos());
                MbdReactor.control(port);
                helper.assertTrue(MbdReactor.data(machine, "burning_rate") == 1, "Sustained signal must not toggle twice");
                helper.getLevel().setBlockAndUpdate(signalPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                MbdReactor.control(port);
                helper.getLevel().setBlock(signalPos, net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
                helper.assertTrue(helper.getLevel().getBlockState(signalPos).is(net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK), "Signal block must persist before control tick");
                MbdReactor.control(port);
                helper.assertTrue(MbdReactor.data(machine, "burning_rate") == 0, "Next rising edge stops reactor");
                var ui = machine.getDefinition().machineSettings().uiTemplate().createUI();
                MbdReactorUI.opened(new com.lowdragmc.mbd2.common.machine.definition.config.event.MachineUIEvent(machine, ui, null));
                ui.selectId("legacy_tab_button_1", com.lowdragmc.lowdraglib2.gui.ui.elements.Toggle.class).findFirst().orElseThrow().setOn(true);
                helper.assertTrue(!ui.selectId("legacy_tab_button_0", com.lowdragmc.lowdraglib2.gui.ui.elements.Toggle.class).findFirst().orElseThrow().isOn()
                        && ui.selectId("legacy_tab_button_1", com.lowdragmc.lowdraglib2.gui.ui.elements.Toggle.class).findFirst().orElseThrow().isOn(),
                        "Tab selection listeners survive template serialization");
                MbdReactor.set(machine, "degree_of_damage", 0);
                MbdReactor.set(machine, "temperature", 1600);
                MbdReactor.updateTemperature(machine);
                helper.assertTrue(Math.abs(MbdReactor.data(machine, "degree_of_damage") - 0.03 * count) < 0.00001, "Critical temperature damage every three ticks");
                MbdReactor.set(machine, "temperature", count == 2 ? 2501 : 298.15);
                MbdReactor.set(machine, "degree_of_damage", count == 2 ? 0 : 100);
                MbdReactor.updateTemperature(machine);
                var explosions = helper.getLevel().getEntitiesOfClass(com.github.alexmodguy.alexscaves.server.entity.item.NuclearExplosionEntity.class,
                        new net.minecraft.world.phys.AABB(machine.getPos()).inflate(2));
                helper.assertTrue(explosions.size() == 1, "Temperature or damage threshold creates one nuclear explosion");
                helper.assertTrue(Math.abs(explosions.getFirst().getSize() - Math.sqrt(count / 9.0 + 1)) < 0.00001, "Nuclear explosion size scales with assembly count");
                explosions.forEach(net.minecraft.world.entity.Entity::discard);
                machine.getCustomData().putBoolean("meltdown_started", false);
                MbdReactor.set(machine, "temperature", 1250);
                MbdReactor.set(machine, "degree_of_damage", 0);
                MbdReactor.set(machine, "burning_rate", 1);
                var ongoing = recipe.copy();
                ongoing.duration = 100;
                machine.getRecipeLogic().setupRecipe(ongoing);
                helper.assertTrue(machine.getMachineStateName().equals("working"), "Reactor must be working before destruction test");
                helper.getLevel().destroyBlock(machine.getPos(), false);
                var destructionExplosions = helper.getLevel().getEntitiesOfClass(com.github.alexmodguy.alexscaves.server.entity.item.NuclearExplosionEntity.class,
                        new net.minecraft.world.phys.AABB(machine.getPos()).inflate(2));
                helper.assertTrue(destructionExplosions.size() == 1, "Removing working reactor triggers exactly one nuclear explosion");
                destructionExplosions.forEach(net.minecraft.world.entity.Entity::discard);
                helper.succeed();
            });
        });
    }
}
