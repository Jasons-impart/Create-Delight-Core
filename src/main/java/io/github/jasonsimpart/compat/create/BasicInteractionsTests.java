package io.github.jasonsimpart.compat.create;

import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import io.github.jasonsimpart.util.ModIds;

@GameTestHolder("createdelightcore")
@PrefixGameTestTemplate(false)
public final class BasicInteractionsTests {
    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void chainCasingHonorsBlockUseDenial(GameTestHelper helper) {
        var origin = new BlockPos(3, 3, 3);
        var shaft = AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X);
        helper.setBlock(origin, shaft);
        helper.setBlock(origin.east(), shaft);
        var denied = helper.absolutePos(origin.east());
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock> protection = event -> {
            if (event.getPos().equals(denied)) event.setUseBlock(net.neoforged.neoforge.common.util.TriState.FALSE);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, protection);
        try {
            BasicInteractions.caseShaft(helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL),
                    helper.absolutePos(origin), AllBlocks.ANDESITE_ENCASED_SHAFT.get());
            helper.assertBlockPresent(AllBlocks.ANDESITE_ENCASED_SHAFT.get(), origin);
            helper.assertTrue(helper.getBlockState(origin).getValue(BlockStateProperties.AXIS) == Direction.Axis.X, "Casing preserves shared shaft properties");
            helper.assertBlockPresent(AllBlocks.SHAFT.get(), origin.east());
            BasicInteractions.caseShaft(helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL),
                    helper.absolutePos(origin), AllBlocks.SHAFT.get());
            helper.assertTrue(helper.getBlockState(origin).getValue(BlockStateProperties.AXIS) == Direction.Axis.X, "Uncasing preserves shared shaft properties");
        } finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(protection); }
        helper.succeed();
    }
    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void pipeOpeningsAndShaftBoundaries(GameTestHelper helper) {
        var state = AllBlocks.FLUID_PIPE.getDefaultState();
        for (var property : PipeBlock.PROPERTY_BY_DIRECTION.values()) state = state.setValue(property, false);
        state = state.setValue(PipeBlock.NORTH, true).setValue(PipeBlock.SOUTH, true);
        helper.assertTrue(BasicInteractions.togglePipe(state, Direction.NORTH) == state, "Two openings cannot be reduced");
        var three = BasicInteractions.togglePipe(state, Direction.UP);
        helper.assertTrue(three.getValue(PipeBlock.UP), "A third opening can be added");
        helper.assertTrue(!BasicInteractions.togglePipe(three, Direction.NORTH).getValue(PipeBlock.NORTH), "A third opening allows closing another side");
        var origin = new BlockPos(3, 3, 3);
        var shaft = AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X);
        helper.setBlock(origin, shaft);
        helper.setBlock(origin.east(), shaft);
        helper.setBlock(origin.west(), shaft);
        helper.setBlock(origin.east(2), shaft.setValue(BlockStateProperties.AXIS, Direction.Axis.Z));
        helper.setBlock(origin.east(3), shaft);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BasicInteractions.caseShaft(player, helper.absolutePos(origin), AllBlocks.ANDESITE_ENCASED_SHAFT.get());
        helper.assertBlockPresent(AllBlocks.ANDESITE_ENCASED_SHAFT.get(), origin);
        helper.assertBlockPresent(AllBlocks.ANDESITE_ENCASED_SHAFT.get(), origin.east());
        helper.assertBlockPresent(AllBlocks.SHAFT.get(), origin.east(2));
        helper.assertBlockPresent(AllBlocks.SHAFT.get(), origin.east(3));
        BasicInteractions.caseShaft(player, helper.absolutePos(origin), AllBlocks.SHAFT.get());
        helper.assertBlockPresent(AllBlocks.SHAFT.get(), origin.west());
        helper.assertBlockPresent(AllBlocks.SHAFT.get(), origin);
        helper.succeed();
    }
    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void structureLootPreservesPools(GameTestHelper helper) {
        var table = net.minecraft.world.level.storage.loot.LootTable.lootTable().build();
        var name = net.minecraft.resources.ResourceLocation.parse("northstar:chests/martian_base_seed_chest");
        io.github.jasonsimpart.content.event.LegacyStructureLoot.onLoad(new net.neoforged.neoforge.event.LootTableLoadEvent(
                helper.getLevel().registryAccess(), name, table));
        helper.assertTrue(table.getPool("createdelightcore_legacy_0") != null, "Tech salvage pool loaded");
        helper.assertTrue(table.getPool("createdelightcore_legacy_5") != null, "Martian biology pool retained separately");
        var unrelated = net.minecraft.world.level.storage.loot.LootTable.lootTable().build();
        io.github.jasonsimpart.content.event.LegacyStructureLoot.onLoad(new net.neoforged.neoforge.event.LootTableLoadEvent(
                helper.getLevel().registryAccess(), net.minecraft.resources.ResourceLocation.parse("minecraft:empty"), unrelated));
        helper.assertTrue(unrelated.getPool("createdelightcore_legacy_0") == null, "Unlisted tables remain unchanged");
        helper.succeed();
    }

    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void vintageAllOutputsSurvive(GameTestHelper helper) throws Exception {
        if (!net.neoforged.fml.ModList.get().isLoaded(ModIds.VINTAGE_IMPROVEMENTS)) { helper.succeed(); return; }
        var json = new com.google.gson.JsonObject();
        json.addProperty("type", "vintageimprovements:vibrating");
        json.add("ingredients", com.google.gson.JsonParser.parseString("[{\"item\":\"minecraft:gravel\"}]"));
        var outputs = new com.google.gson.JsonArray();
        for (String item : new String[]{"raw_iron", "raw_gold", "raw_copper", "coal", "diamond", "emerald", "lapis_lazuli"}) {
            var output = new com.google.gson.JsonObject(); output.addProperty("id", "minecraft:" + item); outputs.add(output);
        }
        json.add("results", outputs);
        var ops = net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, helper.getLevel().registryAccess());
        var recipe = net.minecraft.world.item.crafting.Recipe.CODEC.parse(ops, json).getOrThrow();
        var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("vintageimprovements:vibrating_table"));
        var pos = new BlockPos(3, 3, 3); helper.setBlock(pos, block);
        var machine = helper.getBlockEntity(pos);
        var input = (net.neoforged.neoforge.items.IItemHandlerModifiable) machine.getClass().getField("inputInv").get(machine);
        input.setStackInSlot(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GRAVEL));
        boolean applied = (boolean) recipe.getClass().getMethod("apply", machine.getClass(), net.minecraft.world.item.crafting.Recipe.class).invoke(null, machine, recipe);
        helper.assertTrue(applied && input.getStackInSlot(0).isEmpty(), "One ore input consumed successfully");
        var inventory = (net.neoforged.neoforge.items.IItemHandler) machine.getClass().getField("outputInv").get(machine);
        int stacks = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) if (!inventory.getStackInSlot(slot).isEmpty()) stacks++;
        helper.assertTrue(stacks == 7, "All seven distinct outputs reach the machine inventory");
        json.addProperty("type", "vintageimprovements:vacuumizing");
        var vacuum = (com.simibubi.create.content.processing.recipe.ProcessingRecipe<?, ?>)
                net.minecraft.world.item.crafting.Recipe.CODEC.parse(ops, json).getOrThrow();
        helper.assertTrue(vacuum.getRollableResults().size() == 7, "Vacuum recipe preserves all output entries");
        helper.succeed();
    }

}
