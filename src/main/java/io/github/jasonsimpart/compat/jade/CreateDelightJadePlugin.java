package io.github.jasonsimpart.compat.jade;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlock;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlockEntity;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class CreateDelightJadePlugin implements IWailaPlugin {
    public static final ResourceLocation BLAZE_BURNER =
            ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "create.blaze_burner");
    public static final ResourceLocation SNOWMAN_COOLER =
            ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "cmr.snowman_cooler");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(BlazeBurnerProvider.INSTANCE, BlazeBurnerBlockEntity.class);
        registration.registerBlockDataProvider(SnowmanCoolerProvider.INSTANCE, SnowmanCoolerBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BlazeBurnerProvider.INSTANCE, BlazeBurnerBlock.class);
        registration.registerBlockComponent(SnowmanCoolerProvider.INSTANCE, SnowmanCoolerBlock.class);
    }
}
