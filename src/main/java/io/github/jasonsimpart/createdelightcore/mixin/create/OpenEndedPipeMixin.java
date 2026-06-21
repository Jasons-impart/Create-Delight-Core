package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.content.fluids.OpenEndedPipe;
import com.simibubi.create.content.fluids.pipes.VanillaFluidTargets;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.mixin.accessor.FlowingFluidAccessor;
import io.github.jasonsimpart.createdelightcore.CDConfig;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

@Mixin(value = OpenEndedPipe.class, remap = false)
public class OpenEndedPipeMixin extends FlowSource {

    @Unique
    private static final BooleanProperty createdelightcore$LAVALOGGED = BooleanProperty.create("lavalogged");

    @Shadow
    private Level world;

    @Shadow
    private BlockPos outputPos;

    @Shadow
    private BlockPos pos;

    public OpenEndedPipeMixin(BlockFace location) {
        super(location);
    }

    @Inject(method = "removeFluidFromSpace", at = @At("HEAD"), cancellable = true, remap = false)
    private void createdelightcore$removeFluidFromSpace(boolean simulate, CallbackInfoReturnable<FluidStack> cir) {
        if (!CDConfig.enableOpenEndedPipeLavaDrainFix) {
            return;
        }

        FluidStack empty = FluidStack.EMPTY;
        if (world == null) {
            cir.setReturnValue(empty);
            cir.cancel();
            return;
        }
        if (!world.isLoaded(outputPos)) {
            cir.setReturnValue(empty);
            cir.cancel();
            return;
        }

        BlockState state = world.getBlockState(outputPos);
        FluidState fluidState = state.getFluidState();
        boolean waterlog = state.hasProperty(WATERLOGGED);
        boolean lavalog = state.hasProperty(createdelightcore$LAVALOGGED);

        FluidStack drainBlock = VanillaFluidTargets.drainBlock(world, outputPos, state, simulate);
        if (!drainBlock.isEmpty()) {
            if (!simulate && state.hasProperty(BlockStateProperties.LEVEL_HONEY)
                    && AllFluids.HONEY.is(drainBlock.getFluid())) {
                AdvancementBehaviour.tryAward(world, pos, AllAdvancements.HONEY_DRAIN);
            }
            cir.setReturnValue(drainBlock);
            cir.cancel();
            return;
        }

        if (!waterlog && !lavalog && !state.canBeReplaced()) {
            cir.setReturnValue(empty);
            cir.cancel();
            return;
        }

        if (fluidState.isEmpty() || !fluidState.isSource()) {
            cir.setReturnValue(empty);
            cir.cancel();
            return;
        }

        FluidStack stack = new FluidStack(fluidState.getType(), 1000);

        if (simulate) {
            cir.setReturnValue(stack);
            cir.cancel();
            return;
        }

        if (FluidHelper.isWater(stack.getFluid())) {
            AdvancementBehaviour.tryAward(world, pos, AllAdvancements.WATER_SUPPLY);
        }

        if (waterlog || lavalog) {
            if (waterlog) {
                world.setBlock(outputPos, state.setValue(WATERLOGGED, false), 3);
                world.scheduleTick(outputPos, Fluids.WATER, 1);
                state = world.getBlockState(outputPos);
            }
            if (lavalog) {
                world.setBlock(outputPos, state.setValue(createdelightcore$LAVALOGGED, false), 3);
                world.scheduleTick(outputPos, Fluids.LAVA, 1);
            }
            cir.setReturnValue(stack);
            cir.cancel();
            return;
        }

        BlockState newState = fluidState.createLegacyBlock()
                .setValue(LiquidBlock.LEVEL, 14);
        FluidState newFluidState = newState.getFluidState();

        if (newFluidState.getType() instanceof FlowingFluidAccessor flowing) {
            FluidState potentiallyFilled = flowing.create$getNewLiquid(world, outputPos, newState);
            if (potentiallyFilled.equals(fluidState)) {
                cir.setReturnValue(stack);
                cir.cancel();
                return;
            }
        }

        world.setBlock(outputPos, newState, 3);
        cir.setReturnValue(stack);
        cir.cancel();
    }

    /**
     * @author Pink_Cats
     * @reason Keep open-ended pipes eligible for the guarded drain path.
     */
    @Overwrite
    public boolean isEndpoint() {
        return true;
    }
}
