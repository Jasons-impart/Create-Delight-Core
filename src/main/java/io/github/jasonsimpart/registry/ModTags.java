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
        public static final TagKey<Block> QUALITY_CROPS = tag("quality_crops");
        public static final TagKey<Block> QUALITY_HARVEST_CONTROLLERS = tag("quality_harvest_controllers");

        private static TagKey<Block> tag(String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, path));
        }
    }

    public static final class Items {
        public static final TagKey<net.minecraft.world.item.Item> LIFE_MATTER = tag("life_matter");
        public static final TagKey<net.minecraft.world.item.Item> QUALITY_HARVEST_CALIBRATORS = tag("quality_harvest_calibrators");
        public static final TagKey<net.minecraft.world.item.Item> QUALITY_HARVEST_CALIBRATORS_TIER_1 = tag("quality_harvest_calibrators/tier_1");
        public static final TagKey<net.minecraft.world.item.Item> QUALITY_HARVEST_CALIBRATORS_TIER_2 = tag("quality_harvest_calibrators/tier_2");
        public static final TagKey<net.minecraft.world.item.Item> QUALITY_HARVEST_CALIBRATORS_TIER_3 = tag("quality_harvest_calibrators/tier_3");

        private static TagKey<net.minecraft.world.item.Item> tag(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, path));
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
