package io.github.jasonsimpart.createdelightcore.content.fluid;

import com.simibubi.create.AllFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class SlimeFluidType extends AllFluids.TintedFluidType {
    public SlimeFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        super(properties, stillTexture, flowingTexture);
    }

    @Override
    protected int getTintColor(FluidState fluidState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        return -1;
    }

    @Override
    protected int getTintColor(FluidStack fluidStack) {
        return -1;
    }

    //entity move & hurt
    @Override
    public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.y < -(double)0.4F) {
            entity.setDeltaMovement(0.8 * vec3.x, -0.8F * vec3.y, 0.8 * vec3.z);
        }
        return false;
    }

    @Override
    public void setItemMovement(ItemEntity entity) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.y < -(double)0.4F) {
            entity.setDeltaMovement(0.8 * vec3.x, -0.6F * vec3.y, 0.8 * vec3.z);
        }
    }

    @Override
    public boolean supportsBoating(Boat boat) {
        Vec3 vec3 = boat.getDeltaMovement();
        if (vec3.y < -(double)0.4F) {
            boat.setDeltaMovement(0.8 * vec3.x, -0.8F * vec3.y, 0.8 * vec3.z);
        }
        else {
            boat.setDeltaMovement(0.8 * vec3.x, vec3.y, 0.8 * vec3.z);
        }
        return super.supportsBoating(boat);
    }

    public boolean canExtinguish(Entity entity) {
        return true;
    }
}
