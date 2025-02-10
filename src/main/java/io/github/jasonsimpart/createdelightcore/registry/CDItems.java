package io.github.jasonsimpart.createdelightcore.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.jasonsimpart.createdelightcore.content.item.ChocolateMoldFilledItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.forsteri.ratatouille.entry.CRCreativeModeTabs;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;
import static io.github.jasonsimpart.createdelightcore.registry.CDTags.forgeItemTag;

public class CDItems {
    public static final ResourceKey<CreativeModeTab> MISC_TAB = CDCreativeTabs.MISC.getKey();
    public static final ResourceKey<CreativeModeTab> COIN_TAB = CDCreativeTabs.COIN.getKey();
    public static final ResourceKey<CreativeModeTab> RATATOUILLE_TAB = CRCreativeModeTabs.BASE_CREATIVE_TAB.getKey();
    public static final ResourceKey<CreativeModeTab> FOOD_TAB = CDCreativeTabs.FOOD.getKey();
    // food
    public static final ItemEntry<Item> UNFRIED_SHRIMP;
    public static final ItemEntry<Item> UNFRIED_CHICKEN_CHIP;
    // chocolate
    public static final ItemEntry<Item> BLACK_CHOCOLATE_MOLD_SOLID;
    public static final ItemEntry<ChocolateMoldFilledItem> BLACK_CHOCOLATE_MOLD_FILLED;
    public static final ItemEntry<Item> WHITE_CHOCOLATE_MOLD_SOLID;
    public static final ItemEntry<ChocolateMoldFilledItem> WHITE_CHOCOLATE_MOLD_FILLED;
    public static final ItemEntry<Item> RUBY_CHOCOLATE_MOLD_SOLID;
    public static final ItemEntry<ChocolateMoldFilledItem> RUBY_CHOCOLATE_MOLD_FILLED;
    // coin
    public static final ItemEntry<Item> IRON;
    public static final ItemEntry<Item> COPPER;
    public static final ItemEntry<Item> GOLD;
    public static final ItemEntry<Item> EMERALD;
    public static final ItemEntry<Item> NETHERITE;
    //tin
    public static final ItemEntry<Item> TIN_INGOT;
    public static final ItemEntry<Item> TIN_NUGGET;
    public static final ItemEntry<Item> RAW_TIN;
    //bronze
    public static final ItemEntry<Item> BRONZE_INGOT;
    public static final ItemEntry<Item> BRONZE_NUGGET;

    static {
        // food
        UNFRIED_SHRIMP = simpleRawFood("unfried_shrimp", 4, 0.3f);
        UNFRIED_CHICKEN_CHIP = simpleRawFood("unfried_chicken_chip", 2, 0.3f);
        // chocolate
        BLACK_CHOCOLATE_MOLD_SOLID = simpleItem("black_chocolate_mold_solid", RATATOUILLE_TAB);
        WHITE_CHOCOLATE_MOLD_SOLID = simpleItem("white_chocolate_mold_solid", RATATOUILLE_TAB);
        RUBY_CHOCOLATE_MOLD_SOLID = simpleItem("ruby_chocolate_mold_solid", RATATOUILLE_TAB);
        BLACK_CHOCOLATE_MOLD_FILLED = REGISTRATE.item("black_chocolate_mold_filled", properties -> new ChocolateMoldFilledItem(properties, new ItemStack(BLACK_CHOCOLATE_MOLD_SOLID.get()))).tab(RATATOUILLE_TAB).register();
        WHITE_CHOCOLATE_MOLD_FILLED = REGISTRATE.item("white_chocolate_mold_filled", properties -> new ChocolateMoldFilledItem(properties, new ItemStack(WHITE_CHOCOLATE_MOLD_SOLID.get()))).tab(RATATOUILLE_TAB).register();
        RUBY_CHOCOLATE_MOLD_FILLED = REGISTRATE.item("ruby_chocolate_mold_filled", properties -> new ChocolateMoldFilledItem(properties, new ItemStack(RUBY_CHOCOLATE_MOLD_SOLID.get()))).tab(RATATOUILLE_TAB).register();
        // coin
        IRON = coinItem("iron", Rarity.COMMON);
        COPPER = coinItem("copper", Rarity.UNCOMMON);
        GOLD = coinItem("gold", Rarity.RARE);
        EMERALD = coinItem("emerald", Rarity.RARE);
        NETHERITE = coinItem("netherite", Rarity.EPIC);
        // tin
        RAW_TIN = simpleRawMaterial("tin");
        TIN_INGOT = simpleIngot("tin");
        TIN_NUGGET = simpleNugget("tin");
        // bronze
        BRONZE_INGOT = simpleIngot("bronze");
        BRONZE_NUGGET = simpleNugget("bronze");
        // andesite
    }

    public static ItemEntry<Item> simpleFood(String name, int nutrition, float saturation) {
        return 	REGISTRATE.item(name, Item::new)
                .properties(p -> p
                        .food(new FoodProperties.Builder()
                                .nutrition(nutrition)
                                .saturationMod(saturation)
                                .build()))
                .tab(FOOD_TAB)
                .register();
    }

    public static ItemEntry<Item> simpleRawFood(String name, int nutrition, float saturation) {
        return 	REGISTRATE.item(name, Item::new)
                .properties(p -> p
                        .food(new FoodProperties.Builder()
                                .effect(new MobEffectInstance(MobEffects.HUNGER, 50, 0, false, false), 0.5f)
                                .nutrition(nutrition)
                                .saturationMod(saturation)
                                .build()))
                .tab(FOOD_TAB)
                .register();
    }


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

    public static ItemEntry<Item> coinItem(String coinTier, Rarity rarity) {
        return REGISTRATE.item(coinTier + "_coin", Item::new)
                .properties(properties -> properties
                        .rarity(rarity)
                        .fireResistant()
                )
                .tab(COIN_TAB)
                .register();
    }

    public static ItemEntry<Item> simpleIngot(String metalName) {
        return REGISTRATE.item(metalName + "_ingot", Item::new)
                .tag(forgeItemTag("ingots/" + metalName), forgeItemTag("ingots"))
                .tab(MISC_TAB)
                .register();
    }

    public static ItemEntry<Item> simpleNugget(String metalName) {
        return REGISTRATE.item(metalName + "_nugget", Item::new)
                .tag(forgeItemTag("nuggets/" + metalName), forgeItemTag("nuggets"))
                .tab(MISC_TAB)
                .register();
    }

    public static ItemEntry<Item> simpleRawMaterial(String metalName) {
        return REGISTRATE.item("raw_" + metalName, Item::new)
                .tag(forgeItemTag("raw_materials/" + metalName), forgeItemTag("raw_materials"))
                .tab(MISC_TAB)
                .register();
    }

    public static void init() {

    }
}
