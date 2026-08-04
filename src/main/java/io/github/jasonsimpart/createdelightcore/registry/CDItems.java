package io.github.jasonsimpart.createdelightcore.registry;

import com.github.alexmodguy.alexscaves.server.entity.item.ThrownIceCreamScoopEntity;
import com.github.alexmodguy.alexscaves.server.item.ThrownProjectileItem;
import com.gumillea.cosmopolitan.common.item.DrinkItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.xkmc.fruitsdelight.init.food.FoodType;
import dev.xkmc.fruitsdelight.init.food.IFDFood;
import io.github.jasonsimpart.createdelightcore.compat.fruitsdelight.LushConfitureFood;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleItem;
import io.github.jasonsimpart.createdelightcore.content.item.CoinItem;
import io.github.jasonsimpart.createdelightcore.content.item.IceCreamItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import org.forsteri.ratatouille.entry.CRCreativeModeTabs;

import java.util.function.Supplier;

import static io.github.jasonsimpart.createdelightcore.CreateDelightCore.REGISTRATE;
import static io.github.jasonsimpart.createdelightcore.registry.CDTags.forgeItemTag;

public class CDItems {
    public static final ResourceKey<CreativeModeTab> MISC_TAB = CDCreativeTabs.MISC.getKey();
    public static final ResourceKey<CreativeModeTab> COIN_TAB = CDCreativeTabs.COIN.getKey();
    public static final ResourceKey<CreativeModeTab> RATATOUILLE_TAB = CRCreativeModeTabs.BASE_CREATIVE_TAB.getKey();
    public static final ResourceKey<CreativeModeTab> FOOD_TAB = CDCreativeTabs.FOOD.getKey();
    // food
    public static final ItemEntry<Item> UNFRIED_SHRIMP = simpleRawFood("unfried_shrimp", 4, 0.3f);
    public static final ItemEntry<Item> UNFRIED_CHICKEN_CHIP = simpleRawFood("unfried_chicken_chip", 2, 0.3f);
    public static final ItemEntry<Item> UNFRIED_CHICKEN_LEG = simpleRawFood("unfried_chicken_leg", 2, 0.3f);
    public static final ItemEntry<Item> UNFRIED_TONKATSU = simpleRawFood("unfried_tonkatsu", 4, 0.3f);
    public static final ItemEntry<Item> UNFRIED_FISH = simpleRawFood("unfried_fish", 3, 0.3f);
    public static final ItemEntry<Item> UNFRIED_POTATO = simpleRawFood("unfried_potato", 2, 0.3f);
    public static final ItemEntry<Item> UNFRIED_CALAMARI = simpleRawFood("unfried_calamari", 3, 0.3f);
    // pizza slice
    public static final ItemEntry<Item> PIZZA_SLICE = simpleFood("pizza_slice", 2, 0.1f);
    public static final ItemEntry<Item> VEGETABLE_PIZZA_SLICE = simpleFood("vegetable_pizza_slice", 2, 0.1f);
    public static final ItemEntry<Item> MEATLOVERS_PIZZA_SLICE = simpleFood("meatlovers_pizza_slice", 2, 0.1f);
    public static final ItemEntry<Item> NETHER_PIZZA_SLICE = simpleFood("nether_pizza_slice", 2, 0.1f);
    //jello item
    public static final ItemEntry<Item> LUSH_CONFITURE_JELLO = jelloItem("lush_confiture_jello", LushConfitureFood::jelloFood);
    //ice-cream scoop
    public static final ItemEntry<ThrownProjectileItem> STRAWBERRY_ICE_CREAM_SCOOP = iceCreamScoop("strawberry");
    public static final ItemEntry<ThrownProjectileItem> BANANA_ICE_CREAM_SCOOP = iceCreamScoop("banana");
    public static final ItemEntry<ThrownProjectileItem> MINT_ICE_CREAM_SCOOP = iceCreamScoop("mint");
    public static final ItemEntry<ThrownProjectileItem> ADZUKI_ICE_CREAM_SCOOP = iceCreamScoop("adzuki");
    public static final ItemEntry<ThrownProjectileItem> POMEGRANATE_ICE_CREAM_SCOOP = iceCreamScoop("pomegranate");
    public static final ItemEntry<ThrownProjectileItem> LIME_ICE_CREAM_SCOOP = iceCreamScoop("lime");
    public static final ItemEntry<ThrownProjectileItem> APPLE_ICE_CREAM_SCOOP = iceCreamScoop("apple");
    public static final ItemEntry<ThrownProjectileItem> BEETROOT_ICE_CREAM_SCOOP = iceCreamScoop("beetroot");
    public static final ItemEntry<ThrownProjectileItem> CARROT_ICE_CREAM_SCOOP = iceCreamScoop("carrot");
    public static final ItemEntry<ThrownProjectileItem> ENCHANTED_FRUIT_ICE_CREAM_SCOOP = iceCreamScoop("enchanted_fruit");
    public static final ItemEntry<ThrownProjectileItem> GLOW_BERRY_ICE_CREAM_SCOOP = iceCreamScoop("glow_berry");
    public static final ItemEntry<ThrownProjectileItem> PUMPKIN_ICE_CREAM_SCOOP = iceCreamScoop("pumpkin");
    public static final ItemEntry<ThrownProjectileItem> LUCUMA_ICE_CREAM_SCOOP = iceCreamScoop("lucuma");
    public static final ItemEntry<ThrownProjectileItem> PINK_DRAGON_FRUIT_ICE_CREAM_SCOOP = iceCreamScoop("pink_dragon_fruit");
    //ice-cream sandwich
    //ice-cream cone

    //chocolate
    public static final ItemEntry<Item> BLACK_CHOCOLATE_MOLD_SOLID = simpleItem("black_chocolate_mold_solid", RATATOUILLE_TAB);
    public static final ItemEntry<Item> BLACK_CHOCOLATE_MOLD_FILLED = simpleItem("black_chocolate_mold_filled", RATATOUILLE_TAB);
    public static final ItemEntry<Item> WHITE_CHOCOLATE_MOLD_SOLID = simpleItem("white_chocolate_mold_solid", RATATOUILLE_TAB);
    public static final ItemEntry<Item> WHITE_CHOCOLATE_MOLD_FILLED = simpleItem("white_chocolate_mold_filled", RATATOUILLE_TAB);
    public static final ItemEntry<Item> RUBY_CHOCOLATE_MOLD_SOLID = simpleItem("ruby_chocolate_mold_solid", RATATOUILLE_TAB);
    public static final ItemEntry<Item> RUBY_CHOCOLATE_MOLD_FILLED = simpleItem("ruby_chocolate_mold_filled", RATATOUILLE_TAB);
    //coin
    public static final ItemEntry<Item> IRON = coinItem("iron", Rarity.COMMON);
    public static final ItemEntry<Item> COPPER = coinItem("copper", Rarity.UNCOMMON);
    public static final ItemEntry<Item> GOLD = coinItem("gold", Rarity.RARE);
    public static final ItemEntry<Item> EMERALD = coinItem("emerald", Rarity.RARE);
    public static final ItemEntry<Item> NETHERITE = coinItem("netherite", Rarity.EPIC);
    // tin
    public static final ItemEntry<Item> TIN_INGOT = simpleIngot("tin");
    public static final ItemEntry<Item> TIN_NUGGET = simpleNugget("tin");
    public static final ItemEntry<Item> RAW_TIN = simpleRawMaterial("tin");
    // bronze
    public static final ItemEntry<Item> BRONZE_INGOT = simpleIngot("bronze");
    public static final ItemEntry<Item> BRONZE_NUGGET = simpleNugget("bronze");

    public static final ItemEntry<ConfigurationModuleItem> KINETIC_CONFIGURATION_MODULE = REGISTRATE
            .item("kinetic_configuration_module", ConfigurationModuleItem::new)
            .properties(properties -> properties.stacksTo(1))
            .tab(MISC_TAB)
            .register();


    public static ItemEntry<IceCreamItem> iceCreamItem(String name, int nutrition, float saturation, boolean bowl, int tFrozen){
        return REGISTRATE.item(name, p -> new IceCreamItem(p, bowl, tFrozen))
                .properties(p -> p
                        .food(new FoodProperties.Builder()
                                .nutrition(nutrition)
                                .saturationMod(saturation)
                                .build())
                )
                .tab(FOOD_TAB)
                .register();
    }

    public static ItemEntry<IceCreamItem> iceCreamSandwich(String name, int nutrition, float saturation){
        return iceCreamItem(name, nutrition, saturation, false, 100);
    }

    public static ItemEntry<IceCreamItem> iceCreamCone(String name, int nutrition, float saturation){
        return iceCreamItem(name, nutrition, saturation, false, 80);
    }

    public static ItemEntry<DrinkItem> milkShakeItem(String name, int nutrition, float saturation, boolean tooltip){
        return REGISTRATE.item(name + "_milkshake", p -> new DrinkItem(p, true, tooltip))
                .properties(p -> p
                        .food(new FoodProperties.Builder()
                                .nutrition(nutrition)
                                .saturationMod(saturation)
                                .build())
                        .craftRemainder(Items.GLASS_BOTTLE)
                        .stacksTo(16)
                )
                .tab(FOOD_TAB)
                .register();
    }

    public static ItemEntry<ThrownProjectileItem> iceCreamScoop(String name) {
        return REGISTRATE.item(name + "_ice_cream_scoop", properties ->
                        new ThrownProjectileItem(properties, player ->
                                new ThrownIceCreamScoopEntity(player.level(), player), -10.0F, 1.0F, 0.2F))
                .tab(FOOD_TAB)
                .register();
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

    public static ItemEntry<Item> jelloItem(String name, Supplier<IFDFood> food) {
        return REGISTRATE.item(name, p -> FoodType.JELLO.build(p, food.get()))
                .tag(FoodType.JELLO.tags)
                .transform(b ->
                        b.model((ctx, provider) ->
                        provider.generated(ctx, provider.modLoc("item/" + name))))
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
        return REGISTRATE.<Item>item(coinTier + "_coin", properties -> new CoinItem(coinTier, properties))
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
