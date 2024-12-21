package io.github.jasonsimpart.createdelightcore.fluid;

import com.simibubi.create.AllFluids;
import io.github.jasonsimpart.createdelightcore.registry.CDCDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class IceCreamFluidType extends AllFluids.TintedFluidType {

    public IceCreamFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
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
        Vec3 movement = entity.getDeltaMovement();
        Vec3 vec3 = new Vec3(movement.x, 0.0d, movement.z);
        Vec3 newMovement = new Vec3(vec3.x * 0.6d, 0.0d - (vec3.y < 0.12d ? 5.0E-3d : vec3.y), vec3.z * 0.6d);
        entity.setDeltaMovement(newMovement);
        entity.setIsInPowderSnow(true);
        entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze(), entity.getTicksFrozen() + 5));
        entity.hurt(CDCDamageTypes.iceCream(entity.level()), 1.0F);
        return false;
    }

    @Override
    public void setItemMovement(ItemEntity entity) {
        Vec3 movement = entity.getDeltaMovement();
        Vec3 vec3 = new Vec3(movement.x, 0.0d, movement.z);
        Vec3 newMovement = new Vec3(vec3.x * 0.6d, 0.0d - (vec3.y < 0.48d ? 5.0E-3d : vec3.y), vec3.z * 0.6d);
        entity.setDeltaMovement(newMovement);
    }


    public boolean canExtinguish(Entity entity) {
        return true;
    }
}
