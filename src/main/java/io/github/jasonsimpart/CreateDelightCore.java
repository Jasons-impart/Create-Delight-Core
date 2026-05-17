package io.github.jasonsimpart;

import com.mojang.logging.LogUtils;
import io.github.jasonsimpart.client.ClientModEvents;
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
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(CreateDelightCore.MODID)
public class CreateDelightCore {
    public static final String MODID = "createdelightcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateDelightCore(IEventBus modEventBus, ModContainer modContainer) {
        ModFluids.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModSoundEvents.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientModEvents.register(modEventBus);
        }
    }
}
