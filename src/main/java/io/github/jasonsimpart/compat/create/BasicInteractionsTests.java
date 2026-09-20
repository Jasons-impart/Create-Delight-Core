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

@GameTestHolder("createdelightcore")
@PrefixGameTestTemplate(false)
public final class BasicInteractionsTests {

    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void vintageAllOutputsSurvive(GameTestHelper helper) throws Exception {
        if (!net.neoforged.fml.ModList.get().isLoaded("vintageimprovements")) { helper.succeed(); return; }
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
