package io.github.jasonsimpart.registry;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateDelightCore.MODID);

    public static final DeferredItem<Item> UNFRIED_SHRIMP = rawFood("unfried_shrimp", 4, 0.3F);
    public static final DeferredItem<Item> UNFRIED_CHICKEN_CHIP = rawFood("unfried_chicken_chip", 2, 0.3F);
    public static final DeferredItem<Item> UNFRIED_CHICKEN_LEG = rawFood("unfried_chicken_leg", 2, 0.3F);
    public static final DeferredItem<Item> UNFRIED_TONKATSU = rawFood("unfried_tonkatsu", 4, 0.3F);
    public static final DeferredItem<Item> UNFRIED_FISH = rawFood("unfried_fish", 3, 0.3F);
    public static final DeferredItem<Item> UNFRIED_POTATO = rawFood("unfried_potato", 2, 0.3F);
    public static final DeferredItem<Item> UNFRIED_CALAMARI = rawFood("unfried_calamari", 3, 0.3F);

    public static final DeferredItem<Item> STRAWBERRY_ICE_CREAM_SCOOP = simpleItem("strawberry_ice_cream_scoop");
    public static final DeferredItem<Item> BANANA_ICE_CREAM_SCOOP = simpleItem("banana_ice_cream_scoop");
    public static final DeferredItem<Item> MINT_ICE_CREAM_SCOOP = simpleItem("mint_ice_cream_scoop");
    public static final DeferredItem<Item> ADZUKI_ICE_CREAM_SCOOP = simpleItem("adzuki_ice_cream_scoop");
    public static final DeferredItem<Item> POMEGRANATE_ICE_CREAM_SCOOP = simpleItem("pomegranate_ice_cream_scoop");
    public static final DeferredItem<Item> LIME_ICE_CREAM_SCOOP = simpleItem("lime_ice_cream_scoop");
    public static final DeferredItem<Item> APPLE_ICE_CREAM_SCOOP = simpleItem("apple_ice_cream_scoop");
    public static final DeferredItem<Item> BEETROOT_ICE_CREAM_SCOOP = simpleItem("beetroot_ice_cream_scoop");
    public static final DeferredItem<Item> CARROT_ICE_CREAM_SCOOP = simpleItem("carrot_ice_cream_scoop");
    public static final DeferredItem<Item> ENCHANTED_FRUIT_ICE_CREAM_SCOOP = simpleItem("enchanted_fruit_ice_cream_scoop");
    public static final DeferredItem<Item> GLOW_BERRY_ICE_CREAM_SCOOP = simpleItem("glow_berry_ice_cream_scoop");
    public static final DeferredItem<Item> PUMPKIN_ICE_CREAM_SCOOP = simpleItem("pumpkin_ice_cream_scoop");

    public static final DeferredItem<Item> BLACK_CHOCOLATE_MOLD_SOLID = simpleItem("black_chocolate_mold_solid");
    public static final DeferredItem<Item> BLACK_CHOCOLATE_MOLD_FILLED = simpleItem("black_chocolate_mold_filled");
    public static final DeferredItem<Item> WHITE_CHOCOLATE_MOLD_SOLID = simpleItem("white_chocolate_mold_solid");
    public static final DeferredItem<Item> WHITE_CHOCOLATE_MOLD_FILLED = simpleItem("white_chocolate_mold_filled");
    public static final DeferredItem<Item> RUBY_CHOCOLATE_MOLD_SOLID = simpleItem("ruby_chocolate_mold_solid");
    public static final DeferredItem<Item> RUBY_CHOCOLATE_MOLD_FILLED = simpleItem("ruby_chocolate_mold_filled");

    public static final DeferredItem<Item> IRON_COIN = coin("iron_coin", Rarity.COMMON);
    public static final DeferredItem<Item> COPPER_COIN = coin("copper_coin", Rarity.UNCOMMON);
    public static final DeferredItem<Item> GOLD_COIN = coin("gold_coin", Rarity.RARE);
    public static final DeferredItem<Item> EMERALD_COIN = coin("emerald_coin", Rarity.RARE);
    public static final DeferredItem<Item> NETHERITE_COIN = coin("netherite_coin", Rarity.EPIC);

    public static final DeferredItem<Item> TIN_INGOT = simpleItem("tin_ingot");
    public static final DeferredItem<Item> TIN_NUGGET = simpleItem("tin_nugget");
    public static final DeferredItem<Item> RAW_TIN = simpleItem("raw_tin");
    public static final DeferredItem<Item> BRONZE_INGOT = simpleItem("bronze_ingot");
    public static final DeferredItem<Item> BRONZE_NUGGET = simpleItem("bronze_nugget");

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    private static DeferredItem<Item> simpleItem(String name) {
        return ITEMS.registerSimpleItem(name);
    }

    private static DeferredItem<Item> rawFood(String name, int nutrition, float saturation) {
        FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).effect(() -> new MobEffectInstance(MobEffects.HUNGER, 50), 0.5F).build();
        return ITEMS.registerSimpleItem(name, new Item.Properties().food(food));
    }

    private static DeferredItem<Item> coin(String name, Rarity rarity) {
        return ITEMS.registerSimpleItem(name, new Item.Properties().rarity(rarity).fireResistant());
    }
}
