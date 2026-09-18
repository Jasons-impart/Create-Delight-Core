package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.blockentity.IMachineBlockEntity;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import com.lowdragmc.mbd2.common.machine.MBDMultiblockMachine;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.forgeenergy.ForgeEnergyCapabilityTrait;
import com.lowdragmc.mbd2.common.capability.recipe.ForgeEnergyRecipeCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Integration checks against a fixture exported independently from the old editor grid. */
@PrefixGameTestTemplate(false)
public final class MbdMachineTests {
    // GameTest's origin is the structure block, one block below the template.
    private static final BlockPos CONTROLLER = new BlockPos(2, 2, 0);

    static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent event) {
        // NeoForge 21.1 discovers holders before filtering namespaces. MBD2's bundled
        // optional integration tests therefore crash discovery without their mods.
        // For an explicitly Core-only development test run, omit those foreign holders.
        if ("createdelightcore".equals(System.getProperty("neoforge.enabledGameTestNamespaces"))) {
            for (var scan : net.neoforged.fml.ModList.get().getAllScanData()) {
                scan.getAnnotations().removeIf(annotation -> annotation.clazz().getClassName().startsWith("com.lowdragmc.mbd2.test.")
                        && annotation.annotationType().getClassName().equals("net.neoforged.neoforge.gametest.GameTestHolder"));
            }
        }
        event.register(MbdMachineTests.class);
        event.register(MbdHydropowerTests.class);
    }

    @GameTest(template = "mbd_alloy", templateNamespace = "createdelightcore", timeoutTicks = 200)
    public static void alloyStructureAndProcessing(GameTestHelper helper) {
        checkAlloy(helper, false);
    }

    @GameTest(template = "mbd_alloy_double", templateNamespace = "createdelightcore", timeoutTicks = 200)
    public static void alloyDoubleCoilAndPorts(GameTestHelper helper) {
        checkAlloy(helper, true);
    }

    @GameTest(template = "mbd_alloy", templateNamespace = "createdelightcore", rotationSteps = 1, timeoutTicks = 200)
    public static void alloyRotatedStructure(GameTestHelper helper) {
        checkAlloy(helper, false);
    }

    private static void checkAlloy(GameTestHelper helper, boolean doubled) {
        var port = new BlockPos(1, 2, 0);
        if (doubled) {
            helper.setBlock(port, MBDRegistries.MACHINE_DEFINITIONS.get(MbdCompat.id("forged_steel_import_bus")).block());
            helper.setBlock(new BlockPos(3, 2, 0), MBDRegistries.MACHINE_DEFINITIONS.get(MbdCompat.id("forged_steel_export_bus")).block());
        }
        helper.runAfterDelay(80, () -> {
            helper.assertTrue(helper.getBlockState(CONTROLLER).is(MBDRegistries.MACHINE_DEFINITIONS.get(MbdCompat.id("alloy_electric_furnace")).block()),
                    "Controller fixture block: " + helper.getBlockState(CONTROLLER));
            var machine = (MBDMultiblockMachine) ((IMachineBlockEntity) helper.getBlockEntity(CONTROLLER)).getMetaMachine();
            helper.assertTrue(machine.checkPattern(), "Original alloy structure must form");
            helper.assertTrue(machine.isFormed(), "Original alloy structure must form automatically after placement");
            var input = (ItemSlotCapabilityTrait) machine.getTraitByName("forged_steel_import_item_slot");
            var output = (ItemSlotCapabilityTrait) machine.getTraitByName("forged_steel_export_item_slot");
            var energy = (ForgeEnergyCapabilityTrait) machine.getTraitByName("forged_steel_import_forge_energy_storage");
            helper.assertTrue(input.storage.getSlots() == 2, "Preserve two input slots");
            helper.assertTrue(output.storage.getSlots() == 2, "Preserve two output slots");
            helper.assertTrue(energy.storage.getMaxEnergyStored() == 800000, "Preserve 800k FE capacity");
            if (doubled) {
                var level = helper.getLevel();
                var position = helper.absolutePos(port);
                var items = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, position, net.minecraft.core.Direction.NORTH);
                helper.assertTrue(items != null && items.getSlots() == 2, "Input bus must proxy controller inventory");
                helper.assertTrue(items.insertItem(0, new ItemStack(Items.IRON_INGOT), false).isEmpty(), "Input bus must accept input");
                helper.assertTrue(input.storage.getStackInSlot(0).is(Items.IRON_INGOT), "Proxy must use controller storage");
                helper.assertTrue(level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, position, net.minecraft.core.Direction.SOUTH) == null,
                        "Input bus back face must stay closed");
            }
            input.storage.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 64));
            helper.assertTrue(!input.storage.insertItem(1, new ItemStack(Items.IRON_INGOT), false).isEmpty(), "Full duplicate input must be rejected");
            input.storage.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 1));
            energy.storage.receiveEnergy(10000, false);
            int before = energy.storage.getEnergyStored();
            var type = MBDRegistries.RECIPE_TYPES.get(MbdCompat.id("alloy_electric_furnace"));
            var recipe = MBDRecipeBuilder.of(MbdCompat.id("test/alloy"), type)
                    .inputItems(Items.IRON_INGOT).outputItems(Items.GOLD_INGOT)
                    .input(ForgeEnergyRecipeCapability.CAP, 1000).duration(20).buildRawRecipe();
            var modifiers = machine.getDefinition().recipeLogicSettings().recipeModifiers();
            helper.assertTrue(modifiers.applyModifiers(machine.getRecipeLogic(), recipe).duration == (doubled ? 10 : 20), "Two coils halve duration; one does not");
            helper.assertTrue(modifiers.getMaxParallel(machine.getRecipeLogic(), recipe).apply(1).intValue() == (doubled ? 32 : 8), "Preserve coil-dependent parallel limits");
            machine.getRecipeLogic().setupRecipe(recipe);
            helper.assertTrue(input.storage.getStackInSlot(0).isEmpty(), "Recipe must consume input");
            helper.assertTrue(energy.storage.getEnergyStored() == before - 1000, "Recipe must consume exact FE");
            helper.runAfterDelay(30, () -> {
                helper.assertTrue(output.storage.getStackInSlot(0).is(Items.GOLD_INGOT), "Recipe must finish into output slot; status="
                        + machine.getRecipeLogic().getStatus() + ", progress=" + machine.getRecipeLogic().getProgress()
                        + ", valid=" + machine.getRecipeLogic().isValid() + ", run=" + machine.runRecipeLogic()
                        + ", formed=" + machine.isFormed());
                var casing = helper.getBlockState(new BlockPos(0, 1, 1));
                helper.setBlock(new BlockPos(0, 1, 1), Blocks.AIR);
                helper.assertTrue(!machine.checkPattern(), "Missing casing must invalidate structure");
                machine.onStructureInvalid(false);
                helper.setBlock(new BlockPos(0, 1, 1), casing);
                helper.assertTrue(machine.checkPattern(), "Restored casing must permit reforming");
                machine.onStructureFormed();
                var saved = helper.getBlockEntity(CONTROLLER).saveWithoutMetadata(helper.getLevel().registryAccess());
                var state = helper.getBlockState(CONTROLLER);
                helper.setBlock(CONTROLLER, Blocks.AIR);
                helper.setBlock(CONTROLLER, state);
                helper.getBlockEntity(CONTROLLER).loadWithComponents(saved, helper.getLevel().registryAccess());
                var restored = (MBDMultiblockMachine) ((IMachineBlockEntity) helper.getBlockEntity(CONTROLLER)).getMetaMachine();
                helper.assertTrue(((ItemSlotCapabilityTrait) restored.getTraitByName("forged_steel_export_item_slot")).storage.getStackInSlot(0).is(Items.GOLD_INGOT), "Output must survive save/load");
                helper.assertTrue(((ForgeEnergyCapabilityTrait) restored.getTraitByName("forged_steel_import_forge_energy_storage")).storage.getEnergyStored() == before - 1000, "FE must survive save/load");
                helper.succeed();
            });
        });
    }
}
