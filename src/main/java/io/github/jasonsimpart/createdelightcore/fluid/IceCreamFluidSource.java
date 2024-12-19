package io.github.jasonsimpart.createdelightcore.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class IceCreamFluidSource extends ForgeFlowingFluid.Source {
    public IceCreamFluidSource(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(Level pLevel, BlockPos pPos, FluidState pState, RandomSource pRandom) {
        super.animateTick(pLevel, pPos, pState, pRandom);

        BlockPos blockpos = pPos.above();
        if (pLevel.getBlockState(blockpos).isAir() && !pLevel.getBlockState(blockpos).isSolidRender(pLevel, blockpos)) {
            if (pRandom.nextInt(100) == 0) {
                double d0 = (double)pPos.getX() + pRandom.nextDouble();
                double d1 = (double)pPos.getY() + (double)1.0F;
                double d2 = (double)pPos.getZ() + pRandom.nextDouble();
                pLevel.addParticle(ParticleTypes.SNOWFLAKE, d0, d1, d2, 0.0F, 0.0F, 0.0F);
            }
        }
    }
}
