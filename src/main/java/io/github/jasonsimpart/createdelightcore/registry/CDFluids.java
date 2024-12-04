package io.github.jasonsimpart.createdelightcore.registry;

import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;

public class CDFluids {
    public static final ResourceKey<CreativeModeTab> MISC_TAB = CDCreativeTabs.MISC.getKey();
    public static final ResourceKey<CreativeModeTab> COIN_TAB = CDCreativeTabs.COIN.getKey();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> MOLTEN_ANDESITE = REGISTRATE
            .fluid( "molten_andesite")
            .defaultBucket()
            .register();
}
