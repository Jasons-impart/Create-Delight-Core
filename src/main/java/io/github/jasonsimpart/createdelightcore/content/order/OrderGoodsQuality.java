package io.github.jasonsimpart.createdelightcore.content.order;

import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class OrderGoodsQuality {
    public static final String ORDER_TAG_PREFIX = "order/";

    private OrderGoodsQuality() {
    }

    public static ResourceLocation orderTagId(String orderType) {
        return ResourceLocation.fromNamespaceAndPath("createdelight", ORDER_TAG_PREFIX + orderType);
    }

    public static TagKey<Item> orderTag(String orderType) {
        return TagKey.create(Registries.ITEM, orderTagId(orderType));
    }

    public static boolean matchesOrderType(ItemStack stack, String orderType) {
        return !stack.isEmpty() && OrderTypeProperties.contains(orderType) && stack.is(orderTag(orderType));
    }

    public static boolean satisfies(ItemStack stack, String orderType, int minQuality) {
        int quality = getQuality(stack, orderType);
        return quality > 0 && quality >= minQuality;
    }

    public static int getQuality(ItemStack stack, String orderType) {
        if (!matchesOrderType(stack, orderType)) {
            return 0;
        }

        if ("western_wine".equals(orderType)) {
            return getWesternWineQuality(stack);
        }

        Optional<OrderTypeProperties.Properties> properties = OrderTypeProperties.get(orderType);
        if (properties.isEmpty()) {
            return 0;
        }

        int qualityLevel = Math.max(0, QualityUtils.getQuality(stack).level());
        int complexityLevel = getComplexityLevel(stack, properties.get().diversity());
        return clampQuality(qualityLevel + complexityLevel);
    }

    public static int getComplexityLevel(ItemStack stack, String orderType) {
        return OrderTypeProperties.get(orderType)
                .map(properties -> getComplexityLevel(stack, properties.diversity()))
                .orElse(0);
    }

    public static double getComplexity(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        try {
            return FoodList.getComplexity(new FoodInstance(stack.getItem()));
        } catch (RuntimeException exception) {
            CreateDelightCore.LOGGER.debug("Could not calculate order food complexity for {}", stack.getItem(), exception);
            return 0;
        }
    }

    private static int getComplexityLevel(ItemStack stack, double[] diversity) {
        double complexity = getComplexity(stack);
        int level = 0;
        for (double threshold : diversity) {
            if (complexity <= threshold) {
                break;
            }
            level++;
        }
        return level;
    }

    private static int getWesternWineQuality(ItemStack stack) {
        if (stack.hasTag() && stack.getOrCreateTag().contains("EffectAmplifier")) {
            return Math.min(stack.getOrCreateTag().getInt("EffectAmplifier") + 2, 3);
        }
        return 2;
    }

    private static int clampQuality(int value) {
        return Math.max(1, Math.min(3, value));
    }
}
