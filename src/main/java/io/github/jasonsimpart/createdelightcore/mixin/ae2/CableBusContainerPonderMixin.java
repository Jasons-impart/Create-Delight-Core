package io.github.jasonsimpart.createdelightcore.mixin.ae2;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.util.AEColor;
import appeng.api.util.AECableType;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.parts.CableBusContainer;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CableBusContainer.class)
public abstract class CableBusContainerPonderMixin {
    @Shadow(remap = false)
    public abstract BlockEntity getBlockEntity();

    @Shadow(remap = false)
    public abstract AECableType getCableConnectionType(Direction side);

    @Shadow(remap = false)
    public abstract AEColor getColor();

    @Inject(method = "getRenderState", at = @At("RETURN"), remap = false)
    private void create_Delight_Core$fixPonderConnections(CallbackInfoReturnable<CableBusRenderState> cir) {
        BlockEntity blockEntity = getBlockEntity();
        if (blockEntity == null) {
            return;
        }

        Level level = blockEntity.getLevel();
        if (!(level instanceof PonderLevel)) {
            return;
        }

        CableBusRenderState renderState = cir.getReturnValue();
        BlockPos pos = blockEntity.getBlockPos();

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (!level.getBlockState(neighborPos).isAir()) {
                create_Delight_Core$addPonderConnection(level, renderState, direction, neighborPos);
                continue;
            }

            renderState.getConnectionTypes().remove(direction);
            renderState.getCableBusAdjacent().remove(direction);
            renderState.getChannelsOnSide().put(direction, 0);
        }
    }

    @Unique
    private void create_Delight_Core$addPonderConnection(Level level, CableBusRenderState renderState, Direction direction, BlockPos neighborPos) {
        IInWorldGridNodeHost neighborHost = create_Delight_Core$getNeighborHost(level, neighborPos);
        if (neighborHost == null) {
            return;
        }

        AECableType localType = getCableConnectionType(direction);
        AECableType neighborType = neighborHost.getCableConnectionType(direction.getOpposite());
        if (!localType.isValid() || !neighborType.isValid()) {
            return;
        }

        if (neighborHost instanceof CableBusContainer neighborCableBus && !create_Delight_Core$canConnectToCableBus(neighborCableBus)) {
            return;
        }

        renderState.getConnectionTypes().put(direction, AECableType.min(localType, neighborType));
        if (neighborHost instanceof CableBusContainer) {
            renderState.getCableBusAdjacent().add(direction);
        }
    }

    @Unique
    private IInWorldGridNodeHost create_Delight_Core$getNeighborHost(Level level, BlockPos neighborPos) {
        BlockEntity neighborBlockEntity = level.getBlockEntity(neighborPos);
        if (neighborBlockEntity instanceof CableBusBlockEntity cableBusBlockEntity) {
            return cableBusBlockEntity.getCableBus();
        }

        return GridHelper.getNodeHost(level, neighborPos);
    }

    @Unique
    private boolean create_Delight_Core$canConnectToCableBus(CableBusContainer neighborCableBus) {
        AEColor color = getColor();
        AEColor neighborColor = neighborCableBus.getColor();
        return color == AEColor.TRANSPARENT || neighborColor == AEColor.TRANSPARENT || color == neighborColor;
    }
}
