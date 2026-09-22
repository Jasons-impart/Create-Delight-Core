package io.github.jasonsimpart;

import com.mojang.logging.LogUtils;
import io.github.jasonsimpart.compat.cmr.CmrCompat;
import io.github.jasonsimpart.compat.createliquidfuel.CreateLiquidFuelCompat;
import io.github.jasonsimpart.compat.mbd2.MbdCompat;
import io.github.jasonsimpart.network.ModNetwork;
import io.github.jasonsimpart.registry.ModBlocks;
import io.github.jasonsimpart.registry.ModCreativeTabs;
import io.github.jasonsimpart.registry.ModFluids;
import io.github.jasonsimpart.registry.ModItems;
import io.github.jasonsimpart.registry.ModMobEffects;
import io.github.jasonsimpart.registry.ModRecipeTypes;
import io.github.jasonsimpart.registry.ModSoundEvents;
import io.github.jasonsimpart.content.recipe.CDFanProcessingTypes;
import io.github.jasonsimpart.content.worldgen.ShallowCaveDensityFunction;
import io.github.jasonsimpart.util.ModIds;
import io.github.jasonsimpart.util.OptionalMods;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
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

    private static final net.neoforged.neoforge.registries.DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.levelgen.DensityFunction>> DENSITY_FUNCTION_TYPES =
            net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.DENSITY_FUNCTION_TYPE, MODID);

    static {
        DENSITY_FUNCTION_TYPES.register("shallow_cave_suppression", () -> ShallowCaveDensityFunction.CODEC.codec());
    }

    public CreateDelightCore(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModFluids.register(modEventBus);
        ModBlocks.register(modEventBus);
        io.github.jasonsimpart.registry.ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        io.github.jasonsimpart.compat.dreadsteel.DreadsteelFallback.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModSoundEvents.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(EventPriority.HIGHEST, CDFanProcessingTypes::register);
        ModCommonEvents.register(modEventBus);
        DENSITY_FUNCTION_TYPES.register(modEventBus);
        ModSpoutBehaviours.register(modEventBus);
        CmrCompat.register(modEventBus);
        CreateLiquidFuelCompat.register(modEventBus);
        ModNetwork.register(modEventBus);
        if (OptionalMods.isLoaded(ModIds.MBD2)) {
            MbdCompat.register(modEventBus);
        }

        if (FMLEnvironment.dist == Dist.CLIENT) {
            OptionalMods.invoke(
                    "io.github.jasonsimpart.client.ClientModEvents",
                    "register", new Class<?>[]{IEventBus.class}, modEventBus);
        }
    }
}
