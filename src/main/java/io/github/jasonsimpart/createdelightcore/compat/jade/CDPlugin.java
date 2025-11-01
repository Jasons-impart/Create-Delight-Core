package io.github.jasonsimpart.createdelightcore.compat.jade;

import fr.iglee42.cmr.cooler.SnowmanCoolerBlock;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.api.*;

public class CDPlugin implements IWailaPlugin {
    public static final String ID = "createdelightcore.cmr";
    public static final ResourceLocation SNOWMAN_COOLER = new ResourceLocation(ID, "snowman_cooler");

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
