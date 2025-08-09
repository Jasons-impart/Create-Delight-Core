package io.github.jasonsimpart.createdelightcore.data;

import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;

import java.util.Set;

public class CDBlockLoot extends BlockLootSubProvider {
    protected CDBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropOther(CDBlocks.LUNA_SOIL_FARMLAND.get(), CDBlocks.LUNA_SOIL.get());
    }
}
