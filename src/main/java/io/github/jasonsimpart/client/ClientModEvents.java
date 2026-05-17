package io.github.jasonsimpart.client;

import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import io.github.jasonsimpart.registry.ModBlocks;
import io.github.jasonsimpart.registry.ModFluids;
import io.github.jasonsimpart.registry.ModItems;
import io.github.jasonsimpart.registry.ModSpriteShifts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientModEvents {
    private static final int LUSH_CONFITURE_COLOR = 0xF0612E;

    private ClientModEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientModEvents::clientSetup);
        modEventBus.addListener(ClientModEvents::registerClientExtensions);
        modEventBus.addListener(ClientModEvents::registerBlockColors);
        modEventBus.addListener(ClientModEvents::registerItemColors);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerConnectedTextures(ModBlocks.STEEL_CASING.get(), ModSpriteShifts.STEEL_CASING);
            registerConnectedTextures(ModBlocks.FORGE_STEEL_CASING.get(), ModSpriteShifts.FORGE_STEEL_CASING);
            registerConnectedTextures(ModBlocks.STEEL_GLASS_CASING.get(), ModSpriteShifts.STEEL_GLASS_CASING);
            registerConnectedTextures(ModBlocks.STEEL_CLEAR_GLASS_CASING.get(), ModSpriteShifts.STEEL_CLEAR_GLASS_CASING);
            ModFluids.SIMPLE_FLUIDS.forEach(fluid -> {
                ItemBlockRenderTypes.setRenderLayer(fluid.source().get(), RenderType.solid());
                ItemBlockRenderTypes.setRenderLayer(fluid.flowing().get(), RenderType.solid());
            });
        });
    }

    private static void registerConnectedTextures(net.minecraft.world.level.block.Block block, com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry shift) {
        CreateRegistrate.connectedTextures(() -> new SimpleCTBehaviour(shift)).accept(block);
    }

    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> LUSH_CONFITURE_COLOR, ModBlocks.LUSH_CONFITURE_JELLY.get(), ModBlocks.LUSH_CONFITURE_JELLO.get());
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? -1 : LUSH_CONFITURE_COLOR, ModBlocks.LUSH_CONFITURE_JELLY_BOTTLE.asItem());
        event.register((stack, tintIndex) -> LUSH_CONFITURE_COLOR, ModBlocks.LUSH_CONFITURE_JELLY.asItem(), ModBlocks.LUSH_CONFITURE_JELLO.asItem());
        event.register((stack, tintIndex) -> 0x808080, ModItems.UNACTIVATED_CRYSTALLINE_FLOWER.get());
        ModFluids.SIMPLE_FLUIDS.stream()
                .filter(ModFluids.SimpleFluid::hasBucket)
                .forEach(fluid -> event.register((stack, tintIndex) -> tintIndex == 1 ? fluid.tintColor() : -1, fluid.bucket().get()));
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        ModFluids.SIMPLE_FLUIDS.forEach(fluid -> event.registerFluidType(new SimpleFluidClientExtensions(fluid), fluid.fluidType()));
    }

    private record SimpleFluidClientExtensions(ModFluids.SimpleFluid fluid) implements IClientFluidTypeExtensions {
        @Override
        public ResourceLocation getStillTexture() {
            return fluid.stillTexture();
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return fluid.flowingTexture();
        }

        @Override
        public ResourceLocation getOverlayTexture() {
            return fluid.overlayTexture();
        }

        @Override
        public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
            return ModFluids.UNDERWATER_OVERLAY;
        }

        @Override
        public int getTintColor() {
            return fluid.tintColor();
        }

        @Override
        public int getTintColor(FluidState state, BlockAndTintGetter getter, net.minecraft.core.BlockPos pos) {
            return fluid.tintColor();
        }
    }
}
