package com.jasonsimpart.createdelightcore.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CDCreativeTab {
    public static final CreativeModeTab CREATE_DELIGHT_TAB = new CreativeModeTab("create_delight_tab") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(CDItems.BLACK_CHOCOLATE_MOLD_FILLED.get());
        }
    };
}
