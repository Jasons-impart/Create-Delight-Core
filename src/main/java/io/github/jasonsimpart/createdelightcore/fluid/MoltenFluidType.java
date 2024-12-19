package io.github.jasonsimpart.createdelightcore.fluid;

import com.simibubi.create.AllFluids;
import io.github.jasonsimpart.createdelightcore.registry.CDCDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class MoltenFluidType extends AllFluids.TintedFluidType {

    public MoltenFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        super(properties, stillTexture, flowingTexture);
    }

    @Override
    protected int getTintColor(FluidStack stack) {
        return -1;
    }

    @Override
    protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return 16777215;
    }

    //entity move & hurt
    @Override
    public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
        entity.setDeltaMovement(entity.getDeltaMovement().scale(0.6));
        entity.hurt(CDCDamageTypes.moltenMetal(entity.level()), 4.0F);
        entity.setSecondsOnFire(15);
        return false;
    }

    @Override
    public void setItemMovement(ItemEntity entity) {
        BlockPos pos = entity.getOnPos();
        Level level = entity.level();
        double d0 = pos.getX();
        double d1 = pos.getY();
        double d2 = pos.getZ();
        entity.setSecondsOnFire(15);
        level.playLocalSound(d0, d1, d2, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 1.0F, false);
    }
//
//    public void animateTick(Level pLevel, BlockPos pPos, FluidState pState, RandomSource pRandom) {
//        BlockPos blockpos = pPos.above();
//        if (pLevel.getBlockState(blockpos).isAir() && !pLevel.getBlockState(blockpos).isSolidRender(pLevel, blockpos)) {
//            if (pRandom.nextInt(100) == 0) {
//                double d0 = (double)pPos.getX() + pRandom.nextDouble();
//                double d1 = (double)pPos.getY() + (double)1.0F;
//                double d2 = (double)pPos.getZ() + pRandom.nextDouble();
//                pLevel.addParticle(ParticleTypes.LAVA, d0, d1, d2, (double)0.0F, (double)0.0F, (double)0.0F);
//                pLevel.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + pRandom.nextFloat() * 0.2F, 0.9F + pRandom.nextFloat() * 0.15F, false);
//            }
//
//            if (pRandom.nextInt(200) == 0) {
//                pLevel.playLocalSound((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2F + pRandom.nextFloat() * 0.2F, 0.9F + pRandom.nextFloat() * 0.15F, false);
//            }
//        }
//
//    }

//    @Override
//    public void onVaporize(@Nullable Player player, Level level, BlockPos pos, FluidStack stack) {
//        BlockPos blockpos = pos.above();
//        RandomSource random = level.random;
//        if (level.getBlockState(blockpos).isAir() && !level.getBlockState(blockpos).isSolidRender(level, blockpos)) {
//            if (random.nextInt(100) == 0) {
//                double d0 = (double)pos.getX() + random.nextDouble();
//                double d1 = (double)pos.getY() + (double)1.0F;
//                double d2 = (double)pos.getZ() + random.nextDouble();
//                level.addParticle(ParticleTypes.LAVA, d0, d1, d2, (double)0.0F, (double)0.0F, (double)0.0F);
//                level.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
//            }
//
//            if (random.nextInt(200) == 0) {
//                level.playLocalSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
//            }
//        }
//    }


    public boolean canExtinguish(Entity entity) {
        return false;
    }
}
