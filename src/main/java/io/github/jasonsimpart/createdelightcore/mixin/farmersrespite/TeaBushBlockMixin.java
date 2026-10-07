package io.github.jasonsimpart.createdelightcore.mixin.farmersrespite;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import umpaz.farmersrespite.common.block.TeaBushBlock;

/** Replaces only harvested leaves; the original tea plant and its harvest lifecycle remain intact. */
@Mixin(TeaBushBlock.class)
public abstract class TeaBushBlockMixin {
    @Unique
    private static final ResourceLocation createdelightcore$rawTea =
            ResourceLocation.fromNamespaceAndPath("youkaishomecoming", "tea_leaves");

    // FR 2.1.2 has five leaf-stack sites and one stick-stack site in this method.
    @ModifyArg(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;I)V"),
            index = 0, require = 6, allow = 6)
    private ItemLike createdelightcore$harvestRawTea(ItemLike original) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(original.asItem());
        if (id != null && "farmersrespite".equals(id.getNamespace())
                && ("green_tea_leaves".equals(id.getPath())
                || "yellow_tea_leaves".equals(id.getPath())
                || "black_tea_leaves".equals(id.getPath()))
                && ForgeRegistries.ITEMS.containsKey(createdelightcore$rawTea)) {
            return ForgeRegistries.ITEMS.getValue(createdelightcore$rawTea);
        }
        return original;
    }
}
