package io.github.jasonsimpart.createdelightcore.content.fluid;

import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.simibubi.create.AllFluids;
import io.github.jasonsimpart.createdelightcore.registry.CDCDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class RadiationFluidType extends AllFluids.TintedFluidType {
    public RadiationFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        super(properties, stillTexture, flowingTexture);
    }

    @Override
    protected int getTintColor(FluidStack fluidStack) {
        return -1;
    }

    @Override
    protected int getTintColor(FluidState fluidState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        return 16777215;
    }

    //entity hurt and effect
    @Override
    public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.6F, 1.0F, 0.6F));
        entity.hurt(CDCDamageTypes.radiation(entity.level()), 4.0F);
        entity.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(), 6000, 4));
        return false;
    }

    @Override
    public void setItemMovement(ItemEntity entity) {
        BlockPos pos = entity.getOnPos();
        Level level = entity.level();
        double pX = pos.getX();
        double pY = pos.getY();
        double pZ = pos.getZ();
        entity.hurt(CDCDamageTypes.radiation(entity.level()), 4.0F);
        level.playLocalSound(pX, pY, pZ, ACSoundRegistry.ACID_BURN.get(), SoundSource.BLOCKS, 2.0F, 1.0F, false);
    }

    @Override
    public boolean supportsBoating(Boat boat) {
        boat.hurt(CDCDamageTypes.radiation(boat.level()), 4.0F);
        return super.supportsBoating(boat);
    }
}
