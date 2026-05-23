package io.github.jasonsimpart.createdelightcore.mixin.Minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.jasonsimpart.createdelightcore.content.block.JellyBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HoneyBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoneyBlock.class)
public class HoneyBlockMixin {

    @WrapOperation(
            method = "fallOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"
            )
    )
    private void createDelightCore$showJellyJumpParticles(Level level, Entity entity, byte event, Operation<Void> original) {
        if ((Object) this instanceof JellyBlock jelly) {
            jelly.showJellyJumpParticles(entity);
            return;
        }
        original.call(level, entity, event);
    }

    @WrapOperation(
            method = "maybeDoSlideEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"
            )
    )
    private void createDelightCore$showJellySlideParticles(Level level, Entity entity, byte event, Operation<Void> original) {
        if ((Object) this instanceof JellyBlock jelly) {
            jelly.showJellySlideParticles(entity);
            return;
        }
        original.call(level, entity, event);
    }
}
