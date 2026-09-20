package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.blockentity.IMachineBlockEntity;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;

@PrefixGameTestTemplate(false)
public final class MbdSingleMachineTests {
    static MBDMachine place(GameTestHelper helper, String name, BlockPos position) {
        helper.setBlock(position, MBDRegistries.MACHINE_DEFINITIONS.get(MbdCompat.id(name)).block());
        return (MBDMachine) ((IMachineBlockEntity) helper.getBlockEntity(position)).getMetaMachine();
    }


    @GameTest(batch = "mbd_single_machines", template = "mbd_single", templateNamespace = "createdelightcore", timeoutTicks = 100)
    public static void mortarRequiresClicksAndCompletes(GameTestHelper helper) {
        var machine = place(helper, "mortar", new BlockPos(4, 2, 4));
        helper.runAfterDelay(2, () -> {
            var input = ((ItemSlotCapabilityTrait) machine.getTraitByName("item_slot")).storage;
            var output = ((ItemSlotCapabilityTrait) machine.getTraitByName("item_slot_0")).storage;
            var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            helper.assertTrue(!MbdSmallProcessing.grind(machine, player, net.minecraft.world.InteractionHand.MAIN_HAND),
                    "Idle mortar must leave default item interaction available");
            input.setStackInSlot(0, new ItemStack(Items.WHEAT));
            var recipe = com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder.of(MbdCompat.id("test/mortar"),
                    MBDRegistries.RECIPE_TYPES.get(MbdCompat.id("mortar"))).inputItems(Items.WHEAT)
                    .outputItems(Items.BREAD).duration(100).buildRawRecipe();
            machine.getRecipeLogic().setupRecipe(recipe);
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(machine.getRecipeLogic().getProgress() == 0 && output.getStackInSlot(0).isEmpty(),
                        "Mortar must not progress without player clicks");
                for (int i = 0; i < 10; i++) MbdSmallProcessing.grind(machine, player, net.minecraft.world.InteractionHand.MAIN_HAND);
                helper.runAfterDelay(2, () -> {
                    helper.assertTrue(output.getStackInSlot(0).is(Items.BREAD), "Ten clicks must finish the recipe");
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(batch = "mbd_single_machines", template = "mbd_single", templateNamespace = "createdelightcore")
    public static void contractNeedsBurnersAndScalesHeat(GameTestHelper helper) {
        var pos = new BlockPos(4, 2, 4);
        var machine = place(helper, "contract_executor", pos);
        helper.runAfterDelay(2, () -> {
            var recipe = com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder.of(MbdCompat.id("test/contract"),
                    MBDRegistries.RECIPE_TYPES.get(MbdCompat.id("contract_executor"))).duration(120).buildRawRecipe();
            helper.assertTrue(machine.doModifyRecipe(recipe) == null, "No burner must prevent contract execution");
            var burner = com.simibubi.create.AllBlocks.BLAZE_BURNER.getDefaultState();
            var heat = com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HEAT_LEVEL;
            helper.setBlock(pos.north(), burner.setValue(heat, com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED));
            helper.assertTrue(machine.doModifyRecipe(recipe).duration == 60, "One kindled burner halves duration");
            helper.setBlock(pos.south(), burner.setValue(heat, com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.SEETHING));
            helper.assertTrue(machine.doModifyRecipe(recipe).duration == 33, "Combined heat scales by heat / sqrt(count)");
            helper.assertTrue(recipe.duration == 120, "Modifiers must not mutate shared recipe data");
            helper.succeed();
        });
    }

    private static final class MillingParams extends com.simibubi.create.content.processing.recipe.ProcessingRecipeParams {
        MillingParams() {
            ingredients.add(Ingredient.of(Items.WHEAT));
            results.add(new com.simibubi.create.content.processing.recipe.ProcessingOutput(new ItemStack(Items.BREAD), 1));
            results.add(new com.simibubi.create.content.processing.recipe.ProcessingOutput(new ItemStack(Items.WHEAT_SEEDS), .25f));
            processingDuration = 40;
        }
    }
    @GameTest(batch = "mbd_single_machines", template = "mbd_single", templateNamespace = "createdelightcore", timeoutTicks = 180)
    public static void smallCentrifugePreservesChanceAndTickEnergy(GameTestHelper helper) {
        var machine = place(helper, "small_centrifugation", new BlockPos(4, 2, 4));
        helper.runAfterDelay(2, () -> {
            var type = MBDRegistries.RECIPE_TYPES.get(MbdCompat.id("small_centrifugation"));
            var source = new com.simibubi.create.content.kinetics.millstone.MillingRecipe(new MillingParams());
            var event = new com.lowdragmc.mbd2.api.recipe.event.TransferProxyRecipeEvent(type,
                    net.minecraft.resources.ResourceLocation.parse("vintageimprovements:centrifugation"), source.getType(), MbdCompat.id("test/small"), source, null);
            MbdSmallProcessing.convert(event);
            var recipe = event.mbdRecipe;
            helper.assertTrue(recipe != null && recipe.duration == 100, "Small centrifuge proxy lasts exactly 100 ticks");
            var outputs = recipe.outputs.get(com.lowdragmc.mbd2.common.capability.recipe.ItemRecipeCapability.CAP);
            helper.assertTrue(outputs.size() == 2 && outputs.get(1).chance == .25f, "Proxy must preserve secondary output chance");
            var input = ((ItemSlotCapabilityTrait) machine.getTraitByName("item_slot_input")).storage;
            var output = ((ItemSlotCapabilityTrait) machine.getTraitByName("item_slot_output")).storage;
            var energy = ((com.lowdragmc.mbd2.common.trait.forgeenergy.ForgeEnergyCapabilityTrait) machine.getTraitByName("forge_energy_storage")).storage;
            input.setStackInSlot(0, new ItemStack(Items.WHEAT));
            energy.receiveEnergy(5000, false);
            energy.receiveEnergy(5000, false);
            machine.getRecipeLogic().setupRecipe(recipe);
            helper.runAfterDelay(115, () -> {
                helper.assertTrue(output.getStackInSlot(0).is(Items.BREAD), "Small centrifuge output=" + output.getStackInSlot(0) + ", status=" + machine.getRecipeLogic().getStatus() + ", progress=" + machine.getRecipeLogic().getProgress() + ", duration=" + machine.getRecipeLogic().getDuration() + ", energy=" + energy.getEnergyStored());
                helper.assertTrue(energy.getEnergyStored() == 0, "Consume 100 FE on each of 100 working ticks");
                helper.succeed();
            });
        });
    }


}
