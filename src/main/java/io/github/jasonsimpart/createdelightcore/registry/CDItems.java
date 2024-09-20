package io.github.jasonsimpart.createdelightcore.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.jasonsimpart.createdelightcore.item.ChocolateMoldFilledItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;

public class CDItems {
    public static final ItemEntry<Item> BLACK_CHOCOLATE_MOLD_SOLID = simpleItem("black_chocolate_mold_solid");
    public static final ItemEntry<ChocolateMoldFilledItem> BLACK_CHOCOLATE_MOLD_FILLED = REGISTRATE
            .item("black_chocolate_mold_filled", properties -> new ChocolateMoldFilledItem(properties, new ItemStack(BLACK_CHOCOLATE_MOLD_SOLID.get())))
            .register();
    public static final ItemEntry<Item> WHITE_CHOCOLATE_MOLD_SOLID = simpleItem("white_chocolate_mold_solid");
    public static final ItemEntry<ChocolateMoldFilledItem> WHITE_CHOCOLATE_MOLD_FILLED = REGISTRATE
            .item("white_chocolate_mold_filled", properties -> new ChocolateMoldFilledItem(properties, new ItemStack(WHITE_CHOCOLATE_MOLD_SOLID.get())))
            .register();
    public static final ItemEntry<Item> RUBY_CHOCOLATE_MOLD_SOLID = simpleItem("ruby_chocolate_mold_solid");
    public static final ItemEntry<ChocolateMoldFilledItem> RUBY_CHOCOLATE_MOLD_FILLED = REGISTRATE
            .item("ruby_chocolate_mold_filled", properties -> new ChocolateMoldFilledItem(properties, new ItemStack(RUBY_CHOCOLATE_MOLD_SOLID.get())))
            .register();

    public static final ItemEntry<Item> IRON_COIN = simpleItem("iron_coin", Rarity.COMMON);
    public static final ItemEntry<Item> COPPER_COIN = simpleItem("copper_coin", Rarity.UNCOMMON);
    public static final ItemEntry<Item> GOLD_COIN = simpleItem("gold_coin", Rarity.RARE);
    public static final ItemEntry<Item> EMERALD_COIN = simpleItem("emerald_coin", Rarity.RARE);
    public static final ItemEntry<Item> NETHERITE_COIN = simpleItem("netherite_coin", Rarity.EPIC);

    public static ItemEntry<Item> simpleItem(String name) {
        return REGISTRATE.item(name, Item::new)
                .register();
    }

    public static ItemEntry<Item> simpleItem(String name, Rarity rarity) {
        return REGISTRATE.item(name, Item::new)
                .properties(properties -> properties.rarity(rarity))
                .register();
    }

    public static void init() {

    }
}
