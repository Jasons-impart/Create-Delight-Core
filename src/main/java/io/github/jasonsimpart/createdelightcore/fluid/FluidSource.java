package io.github.jasonsimpart.createdelightcore.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class FluidSource extends ForgeFlowingFluid.Source {
    public FluidSource(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(Level pLevel, BlockPos pPos, FluidState pState, RandomSource pRandom) {
        super.animateTick(pLevel, pPos, pState, pRandom);

        // 控制粒子生成概率
        if (pRandom.nextFloat() < 0.3F) {
            double x = pPos.getX() + pRandom.nextDouble();
            double y = pPos.getY() + pRandom.nextDouble();
            double z = pPos.getZ() + pRandom.nextDouble();

            // 使用内置的熔岩粒子
            pLevel.addParticle(ParticleTypes.DRIPPING_LAVA, x, y, z, 0, 0, 0);
        }
    }
}
