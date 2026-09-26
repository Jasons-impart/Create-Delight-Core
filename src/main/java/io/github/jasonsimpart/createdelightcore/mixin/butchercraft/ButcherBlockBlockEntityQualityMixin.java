package io.github.jasonsimpart.createdelightcore.mixin.butchercraft;

import com.lance5057.butchercraft.workstations.butcherblock.ButcherBlockBlockEntity;
import de.cadentem.quality_food.util.QualityUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Consumer;

/**
 * 屠宰台按阶段 loot_table 产出物品，产物是全新堆叠，需从插入的屠体继承品质。
 */
@Mixin(value = ButcherBlockBlockEntity.class, remap = false)
public abstract class ButcherBlockBlockEntityQualityMixin {
    @Shadow(remap = false)
    public abstract ItemStack getInsertedItem();

    @Redirect(
            method = "dropLoot",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;forEach(Ljava/util/function/Consumer;)V"
            )
    )
    private void create_delight_core$applyQualityToLoot(ObjectArrayList<ItemStack> list, Consumer<ItemStack> consumer) {
        ItemStack inserted = getInsertedItem();
        List<ItemStack> sources = inserted == null || inserted.isEmpty() ? List.of() : List.of(inserted);
        for (ItemStack stack : list) {
            if (!stack.isEmpty() && !sources.isEmpty()) {
                QualityUtils.applyQuality(stack, sources, null);
            }
            consumer.accept(stack);
        }
    }
}
