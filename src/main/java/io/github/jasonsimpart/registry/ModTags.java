package io.github.jasonsimpart.registry;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class ModTags {
    public static final class Blocks {
        public static final TagKey<Block> FAN_PROCESSING_CATALYSTS_FREEZING = tag("fan_processing_catalysts/freezing");

        private static TagKey<Block> tag(String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, path));
        }
    }

    public static final class Fluids {
        public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_FREEZING = tag("fan_processing_catalysts/freezing");

        private static TagKey<Fluid> tag(String path) {
            return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, path));
        }
    }

    private ModTags() {
    }
}
