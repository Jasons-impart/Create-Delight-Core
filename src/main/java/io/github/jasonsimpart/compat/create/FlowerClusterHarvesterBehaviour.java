package io.github.jasonsimpart.compat.create;

import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;

public class FlowerClusterHarvesterBehaviour extends HarvesterMovementBehaviour {
    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        Level level = context.world;
        if (level.isClientSide()) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof MushroomColonyBlock colony)) {
            return;
        }
        IntegerProperty ageProperty = colony.getAgeProperty();
        int age = state.getValue(ageProperty);
        if (age <= 0) {
            return;
        }
        boolean replant = AllConfigs.server().kinetics.harvesterReplants.get();
        boolean partial = AllConfigs.server().kinetics.harvestPartiallyGrown.get();
        if (!partial && age < colony.getMaxAge()) {
            return;
        }
        if (replant) {
            level.playSound(null, pos, SoundEvents.MOOSHROOM_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlock(pos, state.setValue(ageProperty, 0), 2);
        } else {
            BlockHelper.destroyBlock(level, pos, 1, stack -> {
            });
        }
        collectOrDropItem(context, age < colony.getMaxAge()
                ? new ItemStack(colony.mushroomType.value(), age)
                : new ItemStack(colony.asItem()));
    }
}
