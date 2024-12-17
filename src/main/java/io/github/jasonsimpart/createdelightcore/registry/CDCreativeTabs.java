package io.github.jasonsimpart.createdelightcore.registry;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CDCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateDelightCore.MODID);

    public static final RegistryObject<CreativeModeTab> MISC = CREATIVE_MODE_TABS.register("misc", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup."+ CreateDelightCore.MODID + ".misc"))
                    .icon(() -> new ItemStack(CDItems.RUBY_CHOCOLATE_MOLD_SOLID.get()))
                    .displayItems((parameters, output) -> {})
                    .build());

    public static final RegistryObject<CreativeModeTab> COIN = CREATIVE_MODE_TABS.register("coin", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup."+ CreateDelightCore.MODID + ".coin"))
                    .icon(() -> new ItemStack(CDItems.GOLD_COIN.get()))
                    .displayItems((parameters, output) -> {})
                    .build());

    public static final RegistryObject<CreativeModeTab> Fluid = CREATIVE_MODE_TABS.register("fluid", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup."+ CreateDelightCore.MODID + ".fluid"))
                    .icon(() -> new ItemStack(CDFluids.MOLTEN_ANDESITE.getBucket().get()))
                    .displayItems((parameters, output) -> {})
                    .build());


    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
