package io.github.jasonsimpart.createdelightcore;

import com.mojang.logging.LogUtils;
import io.github.jasonsimpart.createdelightcore.data.CDCoreDatagen;
import io.github.jasonsimpart.createdelightcore.registry.CDCreativeTab;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
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
        CDItems.init();
        CDCreativeTab.init();

        CDCoreDatagen.init();
    }
}
