package com.jasonsimpart.createdelightcore;

import com.jasonsimpart.createdelightcore.registry.CDItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateDelightCore.MODID)
public class CreateDelightCore
{
    public static final String MODID = "createdelightcore";
    private static final Logger LOGGER = LogUtils.getLogger();
    public CreateDelightCore()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CDItems.register(modEventBus);
    }
}
