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
     * 如果为 null，返回 false，让 tick() 方法自然处理重置
     * 
     * 注意：
     * - 不修改 processingTicks，让 tick() 方法处理
     * - 不检查 heldItem.stack，让原方法处理正常逻辑
     */
    @Inject(method = "continueProcessing()Z", at = @At("HEAD"), cancellable = true)
    private void checkHeldItemNull(CallbackInfoReturnable<Boolean> cir) {
        if (heldItem == null) {
            cir.setReturnValue(false);
        }
    }
}
