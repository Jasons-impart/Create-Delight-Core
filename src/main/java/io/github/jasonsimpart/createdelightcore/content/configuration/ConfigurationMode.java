package io.github.jasonsimpart.createdelightcore.content.configuration;

import net.minecraft.resources.ResourceLocation;

public record ConfigurationMode(
        ResourceLocation id,
        ResourceLocation module,
        ResourceLocation target,
        int chargeCost,
        int requiredTier,
        int sortIndex
) {
    public ConfigurationMode {
        if (chargeCost < 0) {
            throw new IllegalArgumentException("Configuration mode charge cost cannot be negative");
        }
        if (requiredTier < 0) {
            throw new IllegalArgumentException("Configuration mode tier cannot be negative");
        }
    }
}
