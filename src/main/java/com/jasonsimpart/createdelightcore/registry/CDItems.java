package com.jasonsimpart.createdelightcore.registry;

import com.jasonsimpart.createdelightcore.CreateDelightCore;
import com.jasonsimpart.createdelightcore.item.ChocolateMoldFilledItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CDItems {
    public static final DeferredRegister<Item> register = DeferredRegister.create(ForgeRegistries.ITEMS, CreateDelightCore.MODID);

    private static Item.Properties properties() {
        return new Item.Properties().tab(CDCreativeTab.CREATE_DELIGHT_TAB);
    }

    public static final RegistryObject<Item> BLACK_CHOCOLATE_MOLD_SOLID = register.register("black_chocolate_mold_solid", () -> new Item(properties()));
    public static final RegistryObject<Item> BLACK_CHOCOLATE_MOLD_FILLED = register.register("black_chocolate_mold_filled",
            () -> new ChocolateMoldFilledItem(properties(), new ItemStack(BLACK_CHOCOLATE_MOLD_SOLID.get())));

    public static final RegistryObject<Item> WHITE_CHOCOLATE_MOLD_SOLID = register.register("white_chocolate_mold_solid", () -> new Item(properties()));
    public static final RegistryObject<Item> WHITE_CHOCOLATE_MOLD_FILLED = register.register("white_chocolate_mold_filled",
            () -> new ChocolateMoldFilledItem(properties(), new ItemStack(WHITE_CHOCOLATE_MOLD_SOLID.get())));

    public static final RegistryObject<Item> RUBY_CHOCOLATE_MOLD_SOLID = register.register("ruby_chocolate_mold_solid", () -> new Item(properties()));
    public static final RegistryObject<Item> RUBY_CHOCOLATE_MOLD_FILLED = register.register("ruby_chocolate_mold_filled",
            () -> new ChocolateMoldFilledItem(properties(), new ItemStack(RUBY_CHOCOLATE_MOLD_SOLID.get())));

    public static void register(IEventBus eventBus) {
        register.register(eventBus);
    }
}
