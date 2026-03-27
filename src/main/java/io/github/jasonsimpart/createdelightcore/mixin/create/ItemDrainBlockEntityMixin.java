package io.github.jasonsimpart.createdelightcore.mixin.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;

/**
 * 修复分液池 ItemDrainBlockEntity 的 NullPointerException 问题
 * 
 * 问题描述:
 * 在 continueProcessing() 方法中，heldItem 在某些情况下为 null，
 * 但代码直接访问 heldItem.stack 导致 NullPointerException
 * 
 * 错误日志:
 * java.lang.NullPointerException: Cannot read field "stack" because "this.heldItem" is null
 * at com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity.continueProcessing(ItemDrainBlockEntity.java:216)
 * 
 * 修复方案:
 * 在方法开始处检查 heldItem 是否为 null，如果是则重置 processingTicks 并返回 false
 * 这样 tick() 方法会执行后续的 notifyUpdate() 和 return 逻辑
 * 
 * @see <a href="https://github.com/Jasons-impart/Create-Delight-Remake/issues/1535">Issue #1535</a>
 */
@Mixin(value = ItemDrainBlockEntity.class, remap = false)
public abstract class ItemDrainBlockEntityMixin {

    @Shadow
    private TransportedItemStack heldItem;

    @Shadow
    private int processingTicks;

    /**
     * 在 continueProcessing 方法开始处检查 heldItem 是否为 null
     * 如果为 null，重置 processingTicks 并返回 false
     * 这样 tick() 方法会执行：processingTicks = 0; notifyUpdate(); return;
     */
    @Inject(method = "continueProcessing()Z", at = @At("HEAD"), cancellable = true)
    private void checkHeldItemNull(CallbackInfoReturnable<Boolean> cir) {
        // 如果 heldItem 为 null，重置 processingTicks 并返回 false
        if (heldItem == null) {
            processingTicks = 0;
            cir.setReturnValue(false);
        }
        // 如果 heldItem.stack 为 null 或空，也重置并返回 false
        else if (heldItem.stack == null || heldItem.stack.isEmpty()) {
            processingTicks = 0;
            cir.setReturnValue(false);
        }
    }
}
