package io.github.jasonsimpart.createdelightcore.compat.jade;

import fr.iglee42.cmr.cooler.SnowmanCoolerBlock;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.api.*;
import static io.github.jasonsimpart.createdelightcore.CreateDelightCore.MODID;

public class CDPlugin implements IWailaPlugin {
    public static final ResourceLocation SNOWMAN_COOLER = ResourceLocation.fromNamespaceAndPath(MODID, "cmr.snowman_cooler");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CoolerProvider.INSTANCE, SnowmanCoolerBlockEntity.class);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CoolerProvider.INSTANCE, SnowmanCoolerBlock.class);
    }

}
