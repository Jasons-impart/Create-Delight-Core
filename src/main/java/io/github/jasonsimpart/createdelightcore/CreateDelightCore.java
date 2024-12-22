package io.github.jasonsimpart.createdelightcore;

import com.mojang.logging.LogUtils;
import io.github.jasonsimpart.createdelightcore.data.CDCoreDatagen;
import io.github.jasonsimpart.createdelightcore.content.recipe.CDFanProcessingTypes;
import io.github.jasonsimpart.createdelightcore.registry.*;
import io.github.jasonsimpart.createdelightcore.server.ItemEntityEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateDelightCore.MODID)
public class CreateDelightCore {
    public static final String MODID = "createdelightcore";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateDelightCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(ItemEntityEvent.class);
        CDItems.init();
        CDFluids.init();
        CDBlocks.init();
        CDCreativeTabs.register(modEventBus);
        CDRecipeTypes.register(modEventBus);

        CDTags.init();
        CDCoreDatagen.init();

        CDFanProcessingTypes.register();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
