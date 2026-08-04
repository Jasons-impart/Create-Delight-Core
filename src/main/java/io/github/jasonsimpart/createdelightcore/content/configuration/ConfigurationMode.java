package io.github.jasonsimpart.createdelightcore.content.configuration;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ConfigurationMode(
        ResourceLocation id,
        ResourceLocation module,
        ResourceLocation target,
        int chargeCost,
        int requiredTier,
        int sortIndex,
        List<ConfigurationRequirement> extraIngredients
) {
    public ConfigurationMode {
        if (chargeCost < 0) {
            throw new IllegalArgumentException("Configuration mode charge cost cannot be negative");
        }
        if (requiredTier < 0) {
            throw new IllegalArgumentException("Configuration mode tier cannot be negative");
        }
        extraIngredients = List.copyOf(extraIngredients);
    }
}
