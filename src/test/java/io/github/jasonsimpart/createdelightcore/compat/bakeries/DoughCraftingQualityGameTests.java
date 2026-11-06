package io.github.jasonsimpart.createdelightcore.compat.bakeries;

import com.renyigesai.bakeries.recipe.DoughCraftingRecipe;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class DoughCraftingQualityGameTests {
    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 20)
    public static void formingKeepsQualityAndDoesNotPolluteTemplate(GameTestHelper helper) {
        ItemStack template = new ItemStack(Items.BREAD, 2);
        template.getOrCreateTag().putString("regression_marker", "keep");
        DoughCraftingRecipe recipe = new DoughCraftingRecipe(
                new ResourceLocation(CreateDelightCore.MODID, "test_dough_quality"),
                Ingredient.of(Items.BREAD), template);
        for (Quality quality : new Quality[]{Quality.NONE, Quality.IRON, Quality.GOLD, Quality.DIAMOND, Quality.NONE}) {
            ItemStack input = new ItemStack(Items.BREAD, 3);
            QualityUtils.applyQuality(input, quality);
            ItemStack output = recipe.assemble(new SimpleContainer(input), helper.getLevel().registryAccess());
            helper.assertTrue(output.getCount() == 2 && input.getCount() == 3, "Counts must not change");
            helper.assertTrue(output != template, "Do not return the shared recipe template");
            helper.assertTrue(output.getTag() != null && "keep".equals(output.getTag().getString("regression_marker")),
                    "Preserve unrelated output NBT");
            helper.assertTrue(QualityUtils.getQuality(template).level() == 0, "Do not pollute the recipe template");
            helper.assertTrue(QualityUtils.getQuality(input).level() == quality.level(), "Do not mutate input quality");
            helper.assertTrue((QualityUtils.getQuality(output).level() > 0) == (quality.level() > 0),
                    "Quality input must yield quality output; plain input must stay plain");
        }
        QualityUtils.applyQuality(template, Quality.GOLD);
        ItemStack plain = recipe.assemble(new SimpleContainer(new ItemStack(Items.BREAD)), helper.getLevel().registryAccess());
        helper.assertTrue(QualityUtils.getQuality(plain).level() == 0, "Strip stale template quality before inheriting NONE");
        helper.assertTrue(QualityUtils.getQuality(template).level() == Quality.GOLD.level(), "Do not clear the shared template");
        helper.succeed();
    }
}
