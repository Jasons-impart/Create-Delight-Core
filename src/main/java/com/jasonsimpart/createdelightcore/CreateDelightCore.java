package com.jasonsimpart.createdelightcore;

import com.jasonsimpart.createdelightcore.data.recipe.FreezingRecipeGenerator;
import com.jasonsimpart.createdelightcore.register.CDCreativeTab;
import com.jasonsimpart.createdelightcore.register.CDItems;
import com.mojang.logging.LogUtils;
import net.minecraft.data.DataProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static com.jasonsimpart.createdelightcore.register.CDCreativeTab.CREATE_DELIGHT_TAB;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CreateDelightCore.MODID)
public class CreateDelightCore
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "createdelightcore";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateDelightCore()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CDCreativeTab.register(modEventBus);
        CDItems.register(modEventBus);

    }



}
