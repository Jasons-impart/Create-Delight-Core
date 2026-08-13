package io.github.jasonsimpart.createdelightcore;

import com.mojang.logging.LogUtils;
import io.github.jasonsimpart.createdelightcore.compat.createenchantmentindustry.TetraScrollPrinterCompat;
import io.github.jasonsimpart.createdelightcore.compat.ftbranks.FTBRanksCompat;
import io.github.jasonsimpart.createdelightcore.content.event.TeleportHandler;
import io.github.jasonsimpart.createdelightcore.data.CDCoreDatagen;
import io.github.jasonsimpart.createdelightcore.eventhandlers.ForgeEventsHandler;
import io.github.jasonsimpart.createdelightcore.eventhandlers.ModEventHandler;
import io.github.jasonsimpart.createdelightcore.network.CDNetwork;
import io.github.jasonsimpart.createdelightcore.registry.*;
import io.github.jasonsimpart.createdelightcore.server.ItemEntityEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import plus.dragons.createcentralkitchen.dragonLibLegacy.init.SafeRegistrate;

@Mod(CreateDelightCore.MODID)
public class CreateDelightCore {
    public static final String MODID = "createdelightcore";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final SafeRegistrate REGISTRATE = new SafeRegistrate(MODID);

    public CreateDelightCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CDNetwork.register();
        MinecraftForge.EVENT_BUS.register(ItemEntityEvent.class);
        MinecraftForge.EVENT_BUS.register(ForgeEventsHandler.class);
        modEventBus.register(ModEventHandler.class);
        MinecraftForge.EVENT_BUS.register(new TeleportHandler());
        if (ModList.get().isLoaded("create_enchantment_industry") && ModList.get().isLoaded("tetra")) {
            TetraScrollPrinterCompat.register(modEventBus);
        }
        if (ModList.get().isLoaded("ftbranks")) {
            FTBRanksCompat.register();
        }
        CDItems.init();
        CDFluids.init();
        CDBlocks.init();
        CDBlockEntities.init();
        CDCreativeTabs.register(modEventBus);
        CDMenus.register(modEventBus);
        CDRecipeTypes.register(modEventBus);

        REGISTRATE.registerEventListeners(modEventBus);

        CDTags.init();
        CDCoreDatagen.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CDConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CDConfig.SERVER_SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
