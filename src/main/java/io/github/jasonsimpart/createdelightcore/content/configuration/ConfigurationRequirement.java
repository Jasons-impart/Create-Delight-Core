package io.github.jasonsimpart.createdelightcore.content.configuration;

import net.minecraft.world.item.crafting.Ingredient;

public record ConfigurationRequirement(Ingredient ingredient, int count) {
    public ConfigurationRequirement {
        if (count < 1) {
            throw new IllegalArgumentException("Configuration requirement count must be positive");
        }
    }
}
