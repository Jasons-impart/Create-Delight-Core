package io.github.jasonsimpart.createdelightcore.register;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CDCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateDelightCore.MODID);

    public static final RegistryObject<CreativeModeTab> CREATE_DELIGHT_TAB = CREATIVE_MODE_TABS.register("create_delight_core",
            ()-> CreativeModeTab.builder().icon(()->new ItemStack(CDItems.BLACK_CHOCOLATE_MOLD_SOLID.get()))
                    .title(Component.translatable("itemGroup.create_delight_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(CDItems.BLACK_CHOCOLATE_MOLD_SOLID.get());
                        pOutput.accept(CDItems.BLACK_CHOCOLATE_MOLD_FILLED.get());
                        pOutput.accept(CDItems.WHITE_CHOCOLATE_MOLD_SOLID.get());
                        pOutput.accept(CDItems.WHITE_CHOCOLATE_MOLD_FILLED.get());
                        pOutput.accept(CDItems.RUBY_CHOCOLATE_MOLD_SOLID.get());
                        pOutput.accept(CDItems.RUBY_CHOCOLATE_MOLD_FILLED.get());
                    })
                    .build());
    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
