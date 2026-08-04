package io.github.jasonsimpart.createdelightcore.content.configuration;

import net.minecraft.resources.ResourceLocation;

public record ConfigurationModuleDefinition(
        ResourceLocation item,
        int maxCharge,
        int initialCharge,
        ResourceLocation defaultMode,
        ResourceLocation fallbackMode
) {
    public ConfigurationModuleDefinition {
        if (maxCharge < 1) {
            throw new IllegalArgumentException("Configuration module max charge must be positive");
        }
        if (initialCharge < 0 || initialCharge > maxCharge) {
            throw new IllegalArgumentException("Configuration module initial charge must be within its capacity");
        }
    }
}
