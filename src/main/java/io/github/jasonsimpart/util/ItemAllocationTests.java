package io.github.jasonsimpart.util;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.List;

@GameTestHolder("createdelightcore")
@PrefixGameTestTemplate(false)
public final class ItemAllocationTests {
    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void overlappingRequirementsPreserveScarceMaterials(GameTestHelper helper) {
        for (boolean reverse : new boolean[]{false, true}) {
            var oak = new ItemStack(Items.OAK_PLANKS, 4);
            var birch = new ItemStack(Items.BIRCH_PLANKS, 3);
            var goods = reverse ? List.of(birch, oak) : List.of(oak, birch);
            var result = ItemAllocation.allocate(goods, new int[]{3, 4}, (entry, stack) -> entry == 0 || stack.is(Items.OAK_PLANKS));
            int oakSlot = reverse ? 1 : 0;
            helper.assertTrue(result != null && result[0][oakSlot] == 0 && result[1][oakSlot] == 4,
                    "Reserve all four oak planks for the specific requirement in either slot order");
            helper.assertTrue(oak.getCount() == 4 && birch.getCount() == 3, "Matching never consumes source stacks");
            helper.assertTrue(ItemAllocation.allocate(goods, new int[]{4, 4}, (entry, stack) -> entry == 0 || stack.is(Items.OAK_PLANKS)) == null,
                    "Shared inventories cannot satisfy more goods than available");
        }
        helper.succeed();
    }
}
