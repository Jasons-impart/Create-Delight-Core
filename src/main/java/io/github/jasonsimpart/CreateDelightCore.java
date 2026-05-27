package io.github.jasonsimpart;

import com.mojang.logging.LogUtils;
import io.github.jasonsimpart.compat.cmr.CmrCompat;
import io.github.jasonsimpart.network.ModNetwork;
import io.github.jasonsimpart.registry.ModBlocks;
import io.github.jasonsimpart.registry.ModCreativeTabs;
import io.github.jasonsimpart.registry.ModFluids;
import io.github.jasonsimpart.registry.ModItems;
import io.github.jasonsimpart.registry.ModMobEffects;
import io.github.jasonsimpart.registry.ModSoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(CreateDelightCore.MODID)
public class CreateDelightCore {
    public static final String MODID = "createdelightcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateDelightCore(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModFluids.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModSoundEvents.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModCommonEvents.register(modEventBus);
        ModSpoutBehaviours.register(modEventBus);
        CmrCompat.register(modEventBus);
        ModNetwork.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerClientEvents(modEventBus);
        }
    }

    private static void registerClientEvents(IEventBus modEventBus) {
        try {
            Class.forName("io.github.jasonsimpart.client.ClientModEvents")
                    .getMethod("register", IEventBus.class)
                    .invoke(null, modEventBus);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register createdelightcore client events", exception);
        }
    }
}
