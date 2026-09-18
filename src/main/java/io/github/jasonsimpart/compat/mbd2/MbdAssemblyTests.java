package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.blockentity.IMachineBlockEntity;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import com.lowdragmc.mbd2.common.capability.recipe.ItemRecipeCapability;
import com.lowdragmc.mbd2.common.machine.MBDMultiblockMachine;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineBeforeRecipeWorkingEvent;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.fluid.FluidTankCapabilityTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class MbdAssemblyTests {
    private static final BlockPos CONTROLLER = new BlockPos(0, 2, 2);

    @GameTest(template = "mbd_assembly", templateNamespace = "createdelightcore", timeoutTicks = 400)
    public static void assemblyOrderedPorts(GameTestHelper helper) { check(helper, false); }

    @GameTest(template = "mbd_assembly_long", templateNamespace = "createdelightcore", rotationSteps = 1, timeoutTicks = 400)
    public static void assemblyMaximumRotated(GameTestHelper helper) { check(helper, true); }

    private static void check(GameTestHelper helper, boolean extended) {
        var center = helper.absolutePos(CONTROLLER);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
            helper.getLevel().getChunk((center.getX() >> 4) + x, (center.getZ() >> 4) + z);
        helper.runAfterDelay(220, () -> {
            var machine = (MBDMultiblockMachine) ((IMachineBlockEntity) helper.getBlockEntity(CONTROLLER)).getMetaMachine();
            helper.assertTrue(machine.checkPattern(), "Legacy assembly grid must match");
            helper.assertTrue(machine.isFormed(), "Assembly line must form automatically");
            helper.assertTrue(machine.getParts().size() == (extended ? 14 : 9), "Preserve four fluid ports and 5..10 item ports");
            for (int i = 0; i < 4; i++) helper.assertTrue(machine.getParts().get(i).getPos().equals(helper.absolutePos(new BlockPos(i + 1, 3, 0))),
                    "Fluid ports must sort by depth, including rotated structures");
            for (int i = 4; i < machine.getParts().size(); i++) helper.assertTrue(machine.getParts().get(i).getPos().equals(helper.absolutePos(new BlockPos(i - 3, 4, 0))),
                    "Item ports must follow fluids and sort by depth");
            var fluid = (FluidTankCapabilityTrait) ((MBDMachine) machine.getParts().getFirst()).getTraitByName("fluid_tank");
            var first = (ItemSlotCapabilityTrait) ((MBDMachine) machine.getParts().get(4)).getTraitByName("item_slot");
            var second = (ItemSlotCapabilityTrait) ((MBDMachine) machine.getParts().get(5)).getTraitByName("item_slot");
            var output = (ItemSlotCapabilityTrait) machine.getTraitByName("item_slot");
            helper.assertTrue(fluid.storages[0].getCapacity() == 64000 && output.storage.getSlots() == 27, "Preserve port capacity and 27 output slots");
            var type = MBDRegistries.RECIPE_TYPES.get(MbdCompat.id("assembly_line"));
            var builder = MBDRecipeBuilder.of(MbdCompat.id("test/assembly"), type)
                    .inputFluids(new FluidStack(Fluids.WATER, 1000)).inputItems(Items.IRON_INGOT).inputItems(Items.GOLD_INGOT)
                    .outputItems(Items.DIAMOND).duration(20);
            var recipe = builder.buildRawRecipe();
            first.storage.setStackInSlot(0, new ItemStack(Items.GOLD_INGOT));
            second.storage.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
            fluid.storages[0].setFluid(new FluidStack(Fluids.WATER, 1000));
            var wrong = new MachineBeforeRecipeWorkingEvent(machine, recipe);
            MbdAssembly.before(wrong);
            helper.assertTrue(wrong.isCanceled(), "Wrong port order must reject despite identical total ingredients");
            first.storage.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
            second.storage.setStackInSlot(0, new ItemStack(Items.GOLD_INGOT));
            fluid.storages[0].setFluid(new FluidStack(Fluids.WATER, 1001));
            var excess = new MachineBeforeRecipeWorkingEvent(machine, recipe);
            MbdAssembly.before(excess);
            helper.assertTrue(excess.isCanceled(), "Legacy input quantities must match exactly");
            fluid.storages[0].setFluid(new FluidStack(Fluids.WATER, 1000));
            machine.getRecipeLogic().setupRecipe(recipe);
            helper.assertTrue(first.storage.getStackInSlot(0).isEmpty() && second.storage.getStackInSlot(0).isEmpty(), "Consume ordered inputs");
            helper.assertTrue(fluid.storages[0].isEmpty(), "Consume exact input fluid");
            helper.runAfterDelay(30, () -> {
                helper.assertTrue(output.storage.getStackInSlot(0).is(Items.DIAMOND), "Produce assembly result");
                // Two equal ingredients must not cause the consumed one to steal a retained tool.
                first.storage.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
                second.storage.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
                var retainedBuilder = MBDRecipeBuilder.of(MbdCompat.id("test/assembly_retained"), type).duration(20);
                retainedBuilder.chance = 0;
                retainedBuilder.inputItems(Items.IRON_INGOT);
                retainedBuilder.chance = 1;
                var retained = retainedBuilder.inputItems(Items.IRON_INGOT).outputItems(Items.EMERALD).buildRawRecipe();
                machine.getRecipeLogic().setupRecipe(retained);
                helper.assertTrue(first.storage.getStackInSlot(0).is(Items.IRON_INGOT) && second.storage.getStackInSlot(0).isEmpty(),
                        "Retained tool must stay in its own ordered port");
                var holder = helper.getLevel().getRecipeManager().byKey(ResourceLocation.parse("create:sequenced_assembly/precision_mechanism")).orElseThrow();
                var converted = type.toMBDrecipe(holder.value().getType(), holder.id(), holder.value());
                helper.assertTrue(converted != null && converted.duration == 223, "Five-loop sequence duration is floor(sqrt(5)*100)");
                helper.assertTrue(ItemRecipeCapability.CAP.of(converted.getInputContents(ItemRecipeCapability.CAP).getFirst().content).count() == 12,
                        "Five-loop sequence base batch is floor(64/5)");
                helper.assertTrue(ItemRecipeCapability.CAP.of(converted.getOutputContents(ItemRecipeCapability.CAP).getFirst().content).count() == 12,
                        "Five-loop sequence output batch is floor(64/5)");
                helper.succeed();
            });
        });
    }
}
