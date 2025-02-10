package io.github.jasonsimpart.createdelightcore.registry;

import com.simibubi.create.AllCreativeModeTabs;
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

    public static final RegistryObject<CreativeModeTab> FOOD = CREATIVE_MODE_TABS.register("food", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup."+ CreateDelightCore.MODID + ".food"))
                    .withTabsAfter(AllCreativeModeTabs.BASE_CREATIVE_TAB.getId())
                    .icon(() -> new ItemStack(CDItems.UNFRIED_SHRIMP.get()))
                    .displayItems((parameters, output) -> {})
                    .build());

    public static final RegistryObject<CreativeModeTab> FLUID = CREATIVE_MODE_TABS.register("fluid", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup."+ CreateDelightCore.MODID + ".fluid"))
                    .withTabsAfter(CDCreativeTabs.MISC.getId())
                    .icon(() -> new ItemStack(CDFluids.MOLTEN_ICE_STEEL.getBucket().get()))
                    .displayItems((parameters, output) -> {})
                    .build());

    public static final RegistryObject<CreativeModeTab> COIN = CREATIVE_MODE_TABS.register("coin", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup."+ CreateDelightCore.MODID + ".coin"))
                    .withTabsAfter(CDCreativeTabs.FLUID.getId())
                    .icon(() -> new ItemStack(CDItems.GOLD.get()))
                    .displayItems((parameters, output) -> {})
                    .build());

    public static final RegistryObject<CreativeModeTab> MISC = CREATIVE_MODE_TABS.register("misc", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup."+ CreateDelightCore.MODID + ".misc"))
                    .withTabsAfter(CDCreativeTabs.FOOD.getId())
                    .icon(() -> new ItemStack(CDBlocks.FRAGMENT_OF_BORDER.get()))
                    .displayItems((parameters, output) -> {})
                    .build());


    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
