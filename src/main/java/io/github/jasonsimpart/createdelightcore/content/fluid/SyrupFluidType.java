package io.github.jasonsimpart.createdelightcore.content.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class SyrupFluidType extends FluidType {
    private final ResourceLocation texture;

    public SyrupFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        super(properties);
        this.texture = stillTexture;
    }

    @Override
    public String getDescriptionId() {
        ResourceLocation key = ForgeRegistries.FLUID_TYPES.get().getKey(this);
        return key == null ? super.getDescriptionId() : fluidDescriptionId(key);
    }

    @Override
    public String getDescriptionId(FluidStack stack) {
        ResourceLocation key = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        return key == null ? super.getDescriptionId(stack) : fluidDescriptionId(key);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return texture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return texture;
            }
        });
    }

    private static String fluidDescriptionId(ResourceLocation key) {
        String path = key.getPath();
        if (path.startsWith("flowing_"))
            path = path.substring("flowing_".length());
        return "fluid." + key.getNamespace() + "." + path;
    }
}
