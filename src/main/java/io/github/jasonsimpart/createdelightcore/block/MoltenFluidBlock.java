package io.github.jasonsimpart.createdelightcore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.lwjgl.system.NonnullDefault;

import java.util.function.Supplier;

public class MoltenFluidBlock extends LiquidBlock {
    public MoltenFluidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    @NonnullDefault
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        moltenFluidEntityEffects(level, entity);
    }

    @NonnullDefault
    public static void moltenFluidEntityEffects(Level level, Entity entity) {
        if (entity.isAlive()) {
            entity.setSecondsOnFire(5);
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20));
            }
        }
    }
}
