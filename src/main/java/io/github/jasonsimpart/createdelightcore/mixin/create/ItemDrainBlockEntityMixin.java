package io.github.jasonsimpart.createdelightcore.mixin.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
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
 * 使用@Accessor 访问私有字段 heldItem，避免使用反射
 * 在方法开始处添加空值检查，如果 heldItem 为 null 则返回 false 停止处理
 * 
 * @see <a href="https://github.com/Jasons-impart/Create-Delight-Remake/issues/1535">Issue #1535</a>
 */
@Mixin(value = ItemDrainBlockEntity.class, remap = false)
public abstract class ItemDrainBlockEntityMixin {

    /**
     * 使用@Accessor 访问私有字段 heldItem
     * 这是 Mixin 推荐的方式，比反射更安全、更高效
     */
    @Accessor("heldItem")
    abstract TransportedItemStack getHeldItem();

    @Accessor("processingTicks")
    abstract int getProcessingTicks();

    @Accessor("processingTicks")
    abstract void setProcessingTicks(int ticks);

    @Inject(method = "continueProcessing()Z", at = @At("HEAD"), cancellable = true)
    private void checkHeldItemNull(CallbackInfoReturnable<Boolean> cir) {
        // 使用@Accessor 访问 heldItem，无需反射
        TransportedItemStack heldItem = getHeldItem();
        
        // 如果 heldItem 为 null，重置 processingTicks 并返回 false
        if (heldItem == null) {
            setProcessingTicks(0);
            cir.setReturnValue(false);
        }
        // 如果 heldItem.stack 为 null，也停止处理
        else if (heldItem.stack == null || heldItem.stack.isEmpty()) {
            setProcessingTicks(0);
            cir.setReturnValue(false);
        }
    }
}
