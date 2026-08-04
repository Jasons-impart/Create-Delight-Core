package io.github.jasonsimpart.createdelightcore.content.configuration;

import net.minecraft.resources.ResourceLocation;

public record ConfigurationModeSnapshot(ResourceLocation id, ResourceLocation target, int chargeCost) {
    public ConfigurationModeSnapshot {
        if (chargeCost < 0) {
            throw new IllegalArgumentException("Configuration mode snapshot charge cost cannot be negative");
        }
    }
}
