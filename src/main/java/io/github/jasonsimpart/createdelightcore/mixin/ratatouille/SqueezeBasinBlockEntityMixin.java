package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinBlockEntity;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Ratatouille 挤压盆 {@code process} 用 {@code rollResults} 新建产物堆叠，不经 Create RecipeApplier。
 * 旧注入点失效：类级 {@code remap=false} 导致 {@code ItemStack.shrink} 对不上运行时 {@code m_41774_}，
 * 且 {@code ModifyArg} 挂在不存在的 {@code lambda$process$5}。
 * 对齐机械炸锅：扣料前采样输入品质，{@code acceptOutputs} 前写回首个产物。
 */
@Mixin(value = SqueezeBasinBlockEntity.class, remap = false)
public abstract class SqueezeBasinBlockEntityMixin {

    @Shadow
    public SqueezeBasinInventory inputInventory;

    @Unique
    private static final ThreadLocal<Quality> create_Delight_Core$INPUT = new ThreadLocal<>();

    @Inject(
            method = "process",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V",
                    remap = true
            )
    )
    private void process$storeInput(CallbackInfo ci, @Local ItemStack itemStack) {
        Quality quality = QualityUtils.getQuality(itemStack);
        // 槽 0 无品质时再试槽 1（与 050x HotAI 补丁一致）
        if (!QualityUtils.isValidQuality(quality) || quality.level() <= 0) {
            ItemStack slot1 = this.inputInventory.getStackInSlot(1);
            if (!slot1.isEmpty()) {
                quality = QualityUtils.getQuality(slot1);
            }
        }
        create_Delight_Core$INPUT.set(quality);
    }

    @ModifyArg(
            method = "process",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/forsteri/ratatouille/content/squeeze_basin/SqueezeBasinBlockEntity;acceptOutputs(Ljava/util/List;Z)Z"
            ),
            index = 0
    )
    private List<ItemStack> process$applyQuality(List<ItemStack> outputItems) {
        Quality quality = create_Delight_Core$INPUT.get();
        create_Delight_Core$INPUT.remove();
        if (quality != null && QualityUtils.isValidQuality(quality) && quality.level() > 0
                && outputItems != null && !outputItems.isEmpty()) {
            QualityUtils.applyQuality(outputItems.get(0), quality);
        }
        return outputItems;
    }
}
