package io.github.jasonsimpart.createdelightcore.util;

import com.lightning.northstar.content.NorthstarFluids;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.Nullable;

public class NorthstarBucketHelper {
    @Nullable
    public static ItemStack getBucket(Fluid fluid) {
        Fluid source = sourceOf(fluid);
        if (source == NorthstarFluids.HYDROGEN.getSource()) {
            return new ItemStack(CDItems.HYDROGEN_BUCKET.get());
        }
        if (source == NorthstarFluids.OXYGEN.getSource()) {
            return new ItemStack(CDItems.OXYGEN_BUCKET.get());
        }
        if (source == NorthstarFluids.CARBON.getSource()) {
            return new ItemStack(CDItems.CARBON_BUCKET.get());
        }
        if (source == NorthstarFluids.CHLORINE.getSource()) {
            return new ItemStack(CDItems.CHLORINE_BUCKET.get());
        }
        if (source == NorthstarFluids.SODIUM.getSource()) {
            return new ItemStack(CDItems.SODIUM_BUCKET.get());
        }
        return null;
    }

    private static Fluid sourceOf(Fluid fluid) {
        return fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
    }
}
