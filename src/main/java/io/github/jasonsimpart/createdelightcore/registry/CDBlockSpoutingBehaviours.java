package io.github.jasonsimpart.createdelightcore.registry;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import net.jadenxgamer.netherexp.registry.fluid.JNEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class CDBlockSpoutingBehaviours {
    private static final int ECTOPLASM_AMOUNT = 250;

    public static void register() {
        BlockSpoutingBehaviour.BY_BLOCK.register(
                CDBlocks.PHANTOM_COMPOST.get(),
                CDBlockSpoutingBehaviours::fillPhantomCompost);
    }

    private static int fillPhantomCompost(Level level, BlockPos pos, SpoutBlockEntity spout,
                                          FluidStack fluid, boolean simulate) {
        if (!fluid.getFluid().isSame(JNEFluids.ECTOPLASM_SOURCE.get())
                || !CDTags.isAlienPlanet(level)) {
            return 0;
        }
        if (!simulate && level instanceof ServerLevel serverLevel) {
            BlockState state = level.getBlockState(pos);
            state.randomTick(serverLevel, pos, level.random);
        }
        return ECTOPLASM_AMOUNT;
    }
}
