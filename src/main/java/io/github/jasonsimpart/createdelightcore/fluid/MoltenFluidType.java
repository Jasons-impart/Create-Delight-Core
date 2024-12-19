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
        if(entity.fireImmune()){}
        else {
            level.playLocalSound(d0, d1, d2, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 3.0F, false);
        }
    }


    public boolean canExtinguish(Entity entity) {
        return false;
    }


}
