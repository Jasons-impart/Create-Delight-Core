package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import net.minecraft.world.item.ItemStack;

/** Shared by the barrel, its interaction message and its client-side Jade provider. */
public interface TavernBarrelAccess {
    TavernBarrelBatch createdelightcore$getQualityBatch();

    default TavernBarrelFluidOutput createdelightcore$getFluidOutput() {
        var batch = createdelightcore$getQualityBatch();
        return batch == null ? null : batch.fluidOutput();
    }

    void createdelightcore$fluidDrained();

    default ItemStack createdelightcore$qualifyOutput(ItemStack result) {
        var batch = createdelightcore$getQualityBatch();
        return batch == null || result.isEmpty() ? result : TavernQuality.qualify(result, batch.quality());
    }
}
