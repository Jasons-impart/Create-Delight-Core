package io.github.jasonsimpart.createdelightcore.mixin.createmetallurgy;

import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.CrucibleBlockEntity;
import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.foundry.FoundryData;
import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.foundry.FoundryTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CrucibleBlockEntity.class, remap = false)
public interface CrucibleAccess {
    @Accessor("foundryData")
    FoundryData createdelightcore$getFoundryData();

    @Accessor("tankInventory")
    FoundryTank createdelightcore$getTankInventory();
}
