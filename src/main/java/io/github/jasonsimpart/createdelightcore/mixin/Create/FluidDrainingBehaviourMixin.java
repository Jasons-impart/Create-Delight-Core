package io.github.jasonsimpart.createdelightcore.mixin.Create;

import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.content.fluids.transfer.FluidManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import it.unimi.dsi.fastutil.PriorityQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
@Mixin(FluidDrainingBehaviour.class)
public abstract class FluidDrainingBehaviourMixin extends FluidManipulationBehaviour {
    @Shadow(remap = false)
    Fluid fluid;

    @Shadow(remap = false)
    Set<BlockPos> validationSet;
    @Shadow(remap = false)
    PriorityQueue<BlockPosEntry> queue;

    public FluidDrainingBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Inject(method = "continueSearch", at = @At("HEAD"), cancellable = true, remap = false)
    private void continueSearch(CallbackInfo ci) {

        List<BlockPosEntry> frontier = ((FluidManipulationBehaviourAccessor)this).getFrontier();
        Set<BlockPos> visited = ((FluidManipulationBehaviourAccessor)this).getVisited();
        boolean infinite = ((FluidManipulationBehaviourAccessor)this).getInfinite();
        try {
            fluid = search(fluid, frontier, visited, (e, d) -> {
                queue.enqueue(new BlockPosEntry(e, d));
                validationSet.add(e);
            }, false);
        } catch (ChunkNotLoadedException e) {
            blockEntity.sendData();
            frontier.clear();
            visited.clear();
        }

        int maxBlocks = maxBlocks();
        int sourceBlocks = 0;
        for (BlockPos pos : visited) {
            if (getWorld().getFluidState(pos).isSource())
                sourceBlocks++;

            if (sourceBlocks > maxBlocks)
                return;
        }

        if (sourceBlocks > maxBlocks && canDrainInfinitely(fluid) && !queue.isEmpty()) {
            ((FluidManipulationBehaviourAccessor)this).setInfinite(true);
            BlockPos firstValid = queue.first()
                    .pos();
            frontier.clear();
            visited.clear();
            queue.clear();
            queue.enqueue(new BlockPosEntry(firstValid, 0));
            blockEntity.sendData();
            return;
        }

        if (!frontier.isEmpty())
            return;

        blockEntity.sendData();
        visited.clear();
        frontier.clear();
        ci.cancel();
    }
}

