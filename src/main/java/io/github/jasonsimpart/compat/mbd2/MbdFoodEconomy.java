package io.github.jasonsimpart.compat.mbd2;

import io.github.jasonsimpart.compat.qualityfood.QualityFoodCompat;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import java.util.List;

final class MbdFoodEconomy {
    private static final TagKey<net.minecraft.world.item.Item> MATERIALS = TagKey.create(Registries.ITEM, ResourceLocation.parse("quality_food:material_whitelist"));
    private static final com.google.gson.JsonObject COMPLEXITY = MbdOrders.readData("complexity");
    private MbdFoodEconomy() {}
    static double complexity(ItemStack stack) {
        var value = COMPLEXITY.getAsJsonObject("items").get(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (value != null) return value.getAsDouble();
        double defaultValue = COMPLEXITY.get("default").getAsDouble();
        // SolApplePie 1.20.1 2.3.0 uses base-item nutrition, never Quality Food bonuses.
        var food = stack.getItem().getDefaultInstance().getFoodProperties(null);
        if (food == null) return defaultValue;
        double average = (food.nutrition() + food.saturation() / 2d) / 2d;
        return average < 5 ? average * defaultValue / 5d : defaultValue * 4 * Math.log10(average - 4) + 1;
    }
    static double price(ItemStack stack) {
        return price(stack, false);
    }
    static double price(ItemStack stack, boolean client) {
        var food = stack.getFoodProperties(null);
        if (food == null && !stack.is(MATERIALS)) return -1;
        double value = client ? io.github.jasonsimpart.network.SyncFoodValuesPayload.clientValue(BuiltInRegistries.ITEM.getKey(stack.getItem())) : MbdRecipeValues.value(stack);
        if (value <= 0) {
            if (food == null) return -1;
            double effects = 1;
            for (var effect : food.effects()) effects += 2 + effect.effect().getAmplifier();
            // 1.21 stores saturation points; the old API returned the modifier.
            double saturation = food.nutrition() > 0 ? food.saturation() / (2 * food.nutrition()) : 0;
            value = Math.max(food.nutrition(), 1) / 6d * Math.max(saturation, .1) / .6 * 5 * Math.sqrt(effects);
        }
        if (ModList.get().isLoaded("quality_food")) value *= qualityMultiplier(stack);
        return value;
    }
    private static double qualityMultiplier(ItemStack stack) {
        var quality = QualityFoodCompat.getQualityData(stack);
        if (quality == null || quality.level() <= 0) return 1;
        double chance = quality.getType().value().chance();
        return chance > 0 ? Math.round(Math.sqrt(2 / chance)) : 1;
    }
    static MoneyValue money(double amount) { return CoinValue.fromNumber("main", (long) Math.max(0, amount)); }
    static List<ItemStack> coins(MoneyValue value) { return value instanceof CoinValue coin ? coin.getAsItemList() : List.of(); }
    static Component text(MoneyValue value) {
        var text = Component.empty();
        for (var coin : coins(value)) {
            String glyph = switch (BuiltInRegistries.ITEM.getKey(coin.getItem()).getPath()) {
                case "iron_coin" -> "\uAA01"; case "copper_coin" -> "\uAA02"; case "gold_coin" -> "\uAA03";
                case "emerald_coin" -> "\uAA04"; case "netherite_coin" -> "\uAA05"; default -> null;
            };
            text.append(Component.literal(coin.getCount() + " "));
            text.append(glyph == null ? coin.getHoverName() : Component.literal(glyph).withStyle(style -> style.withFont(ResourceLocation.parse("createdelight:coin_font"))));
            text.append(" ");
        }
        return text;
    }
}
