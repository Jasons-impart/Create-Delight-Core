package io.github.jasonsimpart.createdelightcore;

import com.mojang.logging.LogUtils;
import io.github.jasonsimpart.createdelightcore.data.CDCoreDatagen;
import io.github.jasonsimpart.createdelightcore.registry.CDCreativeTab;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import io.github.jasonsimpart.createdelightcore.server.ItemEntityEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.StreamSupport;

@Mod(CreateDelightCore.MODID)
public class CreateDelightCore
{
    public static final String MODID = "createdelightcore";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateDelightCore()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(ItemEntityEvent.class);
        CDItems.init();
        CDCreativeTab.init();

        CDCoreDatagen.init();
    }


}
