package io.github.jasonsimpart.createdelightcore.content.fluid;

import com.simibubi.create.content.fluids.potion.PotionFluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class SlimeFluidType extends PotionFluid.PotionFluidType {
    public SlimeFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        super(properties, stillTexture, flowingTexture);
    }
    //entity move & hurt
    @Override
    public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.y < -(double)0.4F) {
            entity.setDeltaMovement(vec3.x, -0.8F * vec3.y, vec3.z);
        }
        return false;
    }

    @Override
    public void setItemMovement(ItemEntity entity) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.y < -(double)0.4F) {
            entity.setDeltaMovement(vec3.x, -0.6F * vec3.y, vec3.z);
        }
    }

    public boolean canExtinguish(Entity entity) {
        return true;
    }
}
