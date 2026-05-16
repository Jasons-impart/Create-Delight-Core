package io.github.jasonsimpart.registry;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateDelightCore.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FOOD = CREATIVE_TABS.register("food", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.createdelightcore.food")).withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS).icon(() -> ModItems.STRAWBERRY_ICE_CREAM_SCOOP.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(ModItems.UNFRIED_SHRIMP);
        output.accept(ModItems.UNFRIED_CHICKEN_CHIP);
        output.accept(ModItems.UNFRIED_CHICKEN_LEG);
        output.accept(ModItems.UNFRIED_TONKATSU);
        output.accept(ModItems.UNFRIED_FISH);
        output.accept(ModItems.UNFRIED_POTATO);
        output.accept(ModItems.UNFRIED_CALAMARI);
        output.accept(ModItems.STRAWBERRY_ICE_CREAM_SCOOP);
        output.accept(ModItems.BANANA_ICE_CREAM_SCOOP);
        output.accept(ModItems.MINT_ICE_CREAM_SCOOP);
        output.accept(ModItems.ADZUKI_ICE_CREAM_SCOOP);
        output.accept(ModItems.POMEGRANATE_ICE_CREAM_SCOOP);
        output.accept(ModItems.LIME_ICE_CREAM_SCOOP);
        output.accept(ModItems.APPLE_ICE_CREAM_SCOOP);
        output.accept(ModItems.BEETROOT_ICE_CREAM_SCOOP);
        output.accept(ModItems.CARROT_ICE_CREAM_SCOOP);
        output.accept(ModItems.ENCHANTED_FRUIT_ICE_CREAM_SCOOP);
        output.accept(ModItems.GLOW_BERRY_ICE_CREAM_SCOOP);
        output.accept(ModItems.PUMPKIN_ICE_CREAM_SCOOP);
        output.accept(ModBlocks.BASE_SYRUP.get());
        output.accept(ModBlocks.STRAWBERRY_SYRUP.get());
        output.accept(ModBlocks.VANILLA_SYRUP.get());
        output.accept(ModBlocks.MINT_SYRUP.get());
        output.accept(ModBlocks.BANANA_SYRUP.get());
        output.accept(ModBlocks.LUSH_CONFITURE_JELLY.get());
        output.accept(ModBlocks.LUSH_CONFITURE_JELLO.get());
        output.accept(ModBlocks.LUSH_CONFITURE_JELLY_BOTTLE.get());
    }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS = CREATIVE_TABS.register("materials", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.createdelightcore.materials")).withTabsBefore(FOOD.getKey()).icon(() -> ModItems.TIN_INGOT.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(ModItems.BLACK_CHOCOLATE_MOLD_SOLID);
        output.accept(ModItems.BLACK_CHOCOLATE_MOLD_FILLED);
        output.accept(ModItems.WHITE_CHOCOLATE_MOLD_SOLID);
        output.accept(ModItems.WHITE_CHOCOLATE_MOLD_FILLED);
        output.accept(ModItems.RUBY_CHOCOLATE_MOLD_SOLID);
        output.accept(ModItems.RUBY_CHOCOLATE_MOLD_FILLED);
        output.accept(ModBlocks.TIN_ORE.get());
        output.accept(ModBlocks.DEEPSLATE_TIN_ORE.get());
        output.accept(ModBlocks.RAW_TIN_BLOCK.get());
        output.accept(ModBlocks.TIN_BLOCK.get());
        output.accept(ModBlocks.BRONZE_BLOCK.get());
        output.accept(ModBlocks.FORGED_STEEL_BLOCK.get());
        output.accept(ModBlocks.FRAGMENT_OF_BORDER.get());
        output.accept(ModItems.TIN_INGOT);
        output.accept(ModItems.TIN_NUGGET);
        output.accept(ModItems.RAW_TIN);
        output.accept(ModItems.BRONZE_INGOT);
        output.accept(ModItems.BRONZE_NUGGET);
    }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COINS = CREATIVE_TABS.register("coins", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.createdelightcore.coins")).withTabsBefore(MATERIALS.getKey()).icon(() -> ModItems.GOLD_COIN.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(ModItems.IRON_COIN);
        output.accept(ModItems.COPPER_COIN);
        output.accept(ModItems.GOLD_COIN);
        output.accept(ModItems.EMERALD_COIN);
        output.accept(ModItems.NETHERITE_COIN);
    }).build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
