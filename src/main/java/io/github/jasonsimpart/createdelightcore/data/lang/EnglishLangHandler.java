package io.github.jasonsimpart.createdelightcore.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class EnglishLangHandler {
    private static void coin(RegistrateLangProvider provider, Supplier<Item> item, String name, String initial) {
        var id = item.get().getDescriptionId();
        // provider.add(id, name);
        provider.add(id + ".initial", initial);
        provider.add(id + ".plural", name);
    }

    public static void init(RegistrateLangProvider provider) {
        coin(provider, CDItems.IRON_COIN, "Iron Coin", "Iron");
        coin(provider, CDItems.COPPER_COIN, "Copper Coin", "Copper");
        coin(provider, CDItems.GOLD_COIN, "Gold Coin", "Gold");
        coin(provider, CDItems.EMERALD_COIN, "Emerald Coin", "Emerald");
        coin(provider, CDItems.NETHERITE_COIN, "Netherite Coin", "Netherite");
    }
}
