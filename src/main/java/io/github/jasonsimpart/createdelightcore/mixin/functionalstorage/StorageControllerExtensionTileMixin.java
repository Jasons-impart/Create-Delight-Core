package io.github.jasonsimpart.createdelightcore.mixin.functionalstorage;

import com.buuz135.functionalstorage.block.tile.StorageControllerExtensionTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(value = StorageControllerExtensionTile.class, remap = false)
public class StorageControllerExtensionTileMixin {
    // Keep player/UI paths unchanged; getStorage/getOptional can use this helper too if they surface the same deadlock.
    @Redirect(
            method = "getCapability",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/buuz135/functionalstorage/block/tile/StorageControllerExtensionTile;getControllerInstance()Ljava/util/Optional;"
            ),
            remap = false
    )
    private Optional<StorageControllerTile> createdelightcore$getCapabilityControllerNonBlocking(StorageControllerExtensionTile<?> self) {
        return createdelightcore$getLoadedController(self);
    }

    @Unique
    private static Optional<StorageControllerTile> createdelightcore$getLoadedController(StorageControllerExtensionTile<?> self) {
        BlockPos controllerPos = self.getControllerPos();
        Level level = self.getLevel();

        if (!(level instanceof ServerLevel serverLevel) || controllerPos == null) {
            return Optional.empty();
        }

        LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(controllerPos.getX() >> 4, controllerPos.getZ() >> 4);
        if (chunk == null) {
            return Optional.empty();
        }

        BlockEntity blockEntity = chunk.getBlockEntity(controllerPos, LevelChunk.EntityCreationType.CHECK);
        return blockEntity instanceof StorageControllerTile controller ? Optional.of(controller) : Optional.empty();
    }
}
