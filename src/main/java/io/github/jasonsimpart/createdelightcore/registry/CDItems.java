package io.github.jasonsimpart.createdelightcore.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.jasonsimpart.createdelightcore.item.ChocolateMoldFilledItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;

public class CDItems {
    public static final ResourceKey<CreativeModeTab> MISC_TAB = CDCreativeTabs.MISC.getKey();
    public static final ResourceKey<CreativeModeTab> COIN_TAB = CDCreativeTabs.COIN.getKey();

    public static final ItemEntry<Item> BLACK_CHOCOLATE_MOLD_SOLID = simpleItem("black_chocolate_mold_solid");
    public static final ItemEntry<ChocolateMoldFilledItem> BLACK_CHOCOLATE_MOLD_FILLED = REGISTRATE
            .item("black_chocolate_mold_filled", properties -> new ChocolateMoldFilledItem(properties, new ItemStack(BLACK_CHOCOLATE_MOLD_SOLID.get())))
            .tab(MISC_TAB)
            .register();
    public static final ItemEntry<Item> WHITE_CHOCOLATE_MOLD_SOLID = simpleItem("white_chocolate_mold_solid");
    public static final ItemEntry<ChocolateMoldFilledItem> WHITE_CHOCOLATE_MOLD_FILLED = REGISTRATE
            .item("white_chocolate_mold_filled", properties -> new ChocolateMoldFilledItem(properties, new ItemStack(WHITE_CHOCOLATE_MOLD_SOLID.get())))
            .tab(MISC_TAB)
            .register();
    public static final ItemEntry<Item> RUBY_CHOCOLATE_MOLD_SOLID = simpleItem("ruby_chocolate_mold_solid");
    public static final ItemEntry<ChocolateMoldFilledItem> RUBY_CHOCOLATE_MOLD_FILLED = REGISTRATE
            .item("ruby_chocolate_mold_filled", properties -> new ChocolateMoldFilledItem(properties, new ItemStack(RUBY_CHOCOLATE_MOLD_SOLID.get())))
            .tab(MISC_TAB)
            .register();

    public static final ItemEntry<Item> IRON_COIN = simpleItem("iron_coin", CDCreativeTabs.COIN.getKey(), Rarity.COMMON);
    public static final ItemEntry<Item> COPPER_COIN = simpleItem("copper_coin", CDCreativeTabs.COIN.getKey(), Rarity.UNCOMMON);
    public static final ItemEntry<Item> GOLD_COIN = simpleItem("gold_coin", CDCreativeTabs.COIN.getKey(), Rarity.RARE);
    public static final ItemEntry<Item> EMERALD_COIN = simpleItem("emerald_coin", CDCreativeTabs.COIN.getKey(), Rarity.RARE);
    public static final ItemEntry<Item> NETHERITE_COIN = simpleItem("netherite_coin", CDCreativeTabs.COIN.getKey(), Rarity.EPIC);

    public static ItemEntry<Item> simpleItem(String name) {
        return simpleItem(name, MISC_TAB);
    }

    public static ItemEntry<Item> simpleItem(String name, ResourceKey<CreativeModeTab> tab) {
        return simpleItem(name, tab, Rarity.COMMON);
    }

    public static ItemEntry<Item> simpleItem(String name, ResourceKey<CreativeModeTab> tab, Rarity rarity) {
        return REGISTRATE.item(name, Item::new)
                .properties(properties -> properties.rarity(rarity))
                .tab(tab)
                .register();
    }

    public static void init() {

    }
}
