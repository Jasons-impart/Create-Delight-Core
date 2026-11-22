package io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight;

import de.cadentem.quality_food.core.Quality;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public final class PackVinegar {
    public static final ResourceLocation ITEM = new ResourceLocation("northeast_china_delight", "vinegar");
    public static final ResourceLocation FLUID = new ResourceLocation("createdelight", "vinegar");
    public static final ResourceLocation DRY_YEAST = new ResourceLocation("createdelight", "dry_yeast");
    public static final ResourceLocation VAT_RECIPE = new ResourceLocation("northeast_china_delight", "vat/vinegar");

    private PackVinegar() {}

    public static boolean isBottle(Item item) {
        return ITEM.equals(ForgeRegistries.ITEMS.getKey(item));
    }

    public static boolean isBottle(ItemStack stack) {
        return !stack.isEmpty() && isBottle(stack.getItem());
    }

    public static boolean isYeast(ItemStack stack) {
        return !stack.isEmpty() && DRY_YEAST.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    public static boolean isFluid(Fluid fluid) {
        return FLUID.equals(ForgeRegistries.FLUIDS.getKey(fluid));
    }

    public static Quality capInitialQuality(Quality quality) {
        return Quality.get(Math.min(Quality.GOLD.level(), quality.level()));
    }
}
