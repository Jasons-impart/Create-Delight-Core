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
 * 使用@Shadow 影子方法访问私有字段 heldItem，这是 Mixin 最优雅的方式
 * 在方法开始处添加空值检查，如果 heldItem 为 null 则返回 false 停止处理
 * 
 * @see <a href="https://github.com/Jasons-impart/Create-Delight-Remake/issues/1535">Issue #1535</a>
 */
@Mixin(value = ItemDrainBlockEntity.class, remap = false)
public abstract class ItemDrainBlockEntityMixin {

    /**
     * 使用@Shadow 访问私有字段 heldItem
     * 这是 Mixin 最优雅的方式：抽象类 + 抽象影子方法
     * 无需方法体，更简洁、更安全
     */
    @Shadow
    private TransportedItemStack heldItem;

    @Shadow
    private int processingTicks;

    @Inject(method = "continueProcessing()Z", at = @At("HEAD"), cancellable = true)
    private void checkHeldItemNull(CallbackInfoReturnable<Boolean> cir) {
        // 使用@Shadow 访问 heldItem，无需反射
        if (heldItem == null) {
            processingTicks = 0;
            cir.setReturnValue(false);
        }
        // 如果 heldItem.stack 为 null，也停止处理
        else if (heldItem.stack == null || heldItem.stack.isEmpty()) {
            processingTicks = 0;
            cir.setReturnValue(false);
        }
    }
}
