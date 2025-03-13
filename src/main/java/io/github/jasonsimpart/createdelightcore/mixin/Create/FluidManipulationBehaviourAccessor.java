package io.github.jasonsimpart.createdelightcore.mixin.Create;

import com.simibubi.create.content.fluids.transfer.FluidManipulationBehaviour;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Set;

@Mixin(FluidManipulationBehaviour.class)
public interface FluidManipulationBehaviourAccessor {
    @Accessor(remap = false)
    List<FluidManipulationBehaviour.BlockPosEntry> getFrontier();
    @Accessor(remap = false)
    Set<BlockPos> getVisited();
    @Accessor(remap = false)
    boolean getInfinite();

    @Accessor("infinite")
    void setInfinite(boolean infinite);
}
