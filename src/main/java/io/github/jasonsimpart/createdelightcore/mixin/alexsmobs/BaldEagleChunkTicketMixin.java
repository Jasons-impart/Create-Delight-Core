package io.github.jasonsimpart.createdelightcore.mixin.alexsmobs;

import io.github.jasonsimpart.createdelightcore.compat.alexsmobs.BaldEagleChunkTickets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Alex's Mobs 1.22.9 falconry forces a 3×3 of chunks with
 * {@code ServerLevel.setChunkForced(..., true)} and never releases them.
 * Replace that with short-lived region tickets; see
 * {@link BaldEagleChunkTickets}.
 */
@Pseudo
@Mixin(targets = "com.github.alexthe666.alexsmobs.entity.EntityBaldEagle", remap = false)
public abstract class BaldEagleChunkTicketMixin {
    @Shadow(remap = false)
    private int lastPlayerControlTime;

    @Inject(method = "loadChunkOnServer(Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1)
    private void createdelightcore$useTemporaryChunkTickets(BlockPos center, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        Level level = self.level();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            BlockPos pos = self.blockPosition();
            BaldEagleChunkTickets.refresh(serverLevel, self.getUUID(), pos.getX(), pos.getZ());
        }
        ci.cancel();
    }

    @Inject(method = {"tick()V", "m_8119_()V"},
            at = @At("RETURN"),
            remap = false,
            require = 1,
            expect = 1,
            allow = 1)
    private void createdelightcore$releaseTicketsWhenControlEnds(CallbackInfo ci) {
        if (this.lastPlayerControlTime > 0) {
            return;
        }
        Entity self = (Entity) (Object) this;
        Level level = self.level();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            BaldEagleChunkTickets.release(serverLevel, self.getUUID());
        }
    }

    // Only fire when the entity actually calls super.remove; the eagle refuses
    // removal while lastPlayerControlTime != 0 or it is a passenger.
    @Inject(method = {
                    "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V",
                    "m_142687_(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/TamableAnimal;m_142687_(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"),
            remap = false,
            require = 1,
            expect = 1,
            allow = 1)
    private void createdelightcore$releaseTicketsOnRemove(RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        Level level = self.level();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            BaldEagleChunkTickets.release(serverLevel, self.getUUID());
        }
    }
}
