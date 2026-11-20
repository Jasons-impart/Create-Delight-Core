package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DepotBehaviour.class, remap = false)
public interface DepotOutputBufferAccessor {
    @Accessor("processingOutputBuffer")
    ItemStackHandler createdelightcore$getProcessingOutputBuffer();
}
