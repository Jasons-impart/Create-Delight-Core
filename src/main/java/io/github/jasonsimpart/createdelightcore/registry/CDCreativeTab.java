package io.github.jasonsimpart.createdelightcore.registry;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;

public class CDCreativeTab {
    public static final RegistryEntry<CreativeModeTab> CREATE_DELIGHT_TAB = REGISTRATE.defaultCreativeTab("create_delight_core",
            builder -> builder
                    .displayItems((parameters, output) -> {
                        output.acceptAll(REGISTRATE.getAll(Registries.ITEM).stream()
                                .map(entry -> new ItemStack(entry.get()))
                                .toList());
                    })
                    .icon(()->new ItemStack(CDItems.BLACK_CHOCOLATE_MOLD_SOLID.get()))
                    .build())
            .register();

    public static void init(){

    }
}
