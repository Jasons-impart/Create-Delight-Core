package io.github.jasonsimpart.createdelightcore.content.order;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public final class OrderGoodsQualityGameTests {
    @GameTest(template = "humidity_regulator_empty")
    public static void westernWineUsesStandardQualityInOrders(GameTestHelper helper) {
        var cider = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("farmersdelight:apple_cider"));
        var inventory = new InventorySummary();
        for (var quality : List.of(Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND)) {
            var bottle = new ItemStack(cider);
            // An old Vinery field on an unqualified stack cannot substitute for QF quality.
            bottle.getOrCreateTag().putInt("EffectAmplifier", 1);
            QualityUtils.applyQuality(bottle, quality);
            int expected = quality == Quality.NONE ? 2 : 3;
            helper.assertTrue(QualityUtils.getQuality(bottle) == quality, "The test bottle must carry the requested QF quality");
            helper.assertTrue(OrderGoodsQuality.getQuality(bottle, "western_wine") == expected,
                    "Western wine should meet the order grade for " + quality);
            helper.assertTrue(OrderGoodsQuality.satisfies(bottle, "western_wine", 2)
                            && OrderGoodsQuality.satisfies(bottle, "western_wine", 3) == (expected == 3),
                    "The order threshold should accept the bottle only at its grade or lower");
            inventory.add(bottle);
        }
        inventory.add(new ItemStack(Items.APPLE));
        helper.assertTrue(OrderCandidateFinder.findCandidates(inventory, "western_wine", 2).size() == 4,
                "The order machine should accept all four grades for a grade two order");
        helper.assertTrue(OrderCandidateFinder.findCandidates(inventory, "western_wine", 3).size() == 3,
                "The order machine should accept only starred cider for a grade three order");
        helper.succeed();
    }
}
