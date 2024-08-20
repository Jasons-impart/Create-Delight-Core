package io.github.jasonsimpart.createdelightcore;

import com.mojang.logging.LogUtils;
import io.github.jasonsimpart.createdelightcore.register.CDCreativeTab;
import io.github.jasonsimpart.createdelightcore.register.CDItems;
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
        CDCreativeTab.register(modEventBus);
        CDItems.register(modEventBus);
    }
}
