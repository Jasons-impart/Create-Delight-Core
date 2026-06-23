package io.github.jasonsimpart.createdelightcore.content.block;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.util.RandomSource;

public class CDFruitTreeGrower extends AbstractTreeGrower {
    private final String featurePath;

    public CDFruitTreeGrower(String featurePath) {
        this.featurePath = featurePath;
    }

    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, CreateDelightCore.id(featurePath));
    }
}
