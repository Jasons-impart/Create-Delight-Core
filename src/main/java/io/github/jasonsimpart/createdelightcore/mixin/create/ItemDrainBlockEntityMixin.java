package io.github.jasonsimpart.createdelightcore.mixin.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;

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
 * 在方法开始处添加空值检查，如果 heldItem 为 null 则返回 false 停止处理
 * 
 * @see <a href="https://github.com/Jasons-impart/Create-Delight-Remake/issues/1535">Issue #1535</a>
 */
@Mixin(value = ItemDrainBlockEntity.class, remap = false)
public abstract class ItemDrainBlockEntityMixin {

    @Inject(method = "continueProcessing()Z", at = @At("HEAD"), cancellable = true)
    private void checkHeldItemNull(CallbackInfoReturnable<Boolean> cir) {
        // 通过反射访问私有字段 heldItem
        try {
            var heldItemField = ItemDrainBlockEntity.class.getDeclaredField("heldItem");
            heldItemField.setAccessible(true);
            Object heldItem = heldItemField.get((ItemDrainBlockEntity) (Object) this);
            
            // 如果 heldItem 为 null，重置 processingTicks 并返回 false
            if (heldItem == null) {
                var processingTicksField = ItemDrainBlockEntity.class.getDeclaredField("processingTicks");
                processingTicksField.setAccessible(true);
                processingTicksField.setInt((ItemDrainBlockEntity) (Object) this, 0);
                
                cir.setReturnValue(false);
            }
        } catch (Exception e) {
            // 如果反射失败，不干预原逻辑，让原代码处理
            // 这样至少不会引入新的问题
        }
    }
}
