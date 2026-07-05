package io.github.jasonsimpart.createdelightcore.content.contraption.components.actor;


import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.BlockHelper;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.DropData;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.block.FlowerClusterBlock;
import io.github.jasonsimpart.createdelightcore.content.util.QualityHarvestAutomationContext;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import net.brdle.collectorsreap.common.block.CRBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import plus.dragons.createcentralkitchen.content.contraptions.components.actor.HarvesterMovementBehaviourExtension;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateDelightCoreHarvesterMovementBehaviorExtensions {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

            registerLootTableHarvest(CDBlocks.FIRE_LILY_CLUSTER.get());
            registerLootTableHarvest(CDBlocks.FROST_LILY_CLUSTER.get());
            registerLootTableHarvest(CDBlocks.LIGHTNING_LILY_CLUSTER.get());
            registerLootTableHarvest(ModBlocks.RED_MUSHROOM_COLONY.get());
            registerLootTableHarvest(ModBlocks.BROWN_MUSHROOM_COLONY.get());
            registerLootTableHarvest(CRBlocks.PORTOBELLO_COLONY.get());

        });
    }

    private static void registerLootTableHarvest(Block block) {
        HarvesterMovementBehaviourExtension.REGISTRY.put(
                block,
                CreateDelightCoreHarvesterMovementBehaviorExtensions::harvestWithLootTable);
    }

    public static void harvestWithLootTable(
            HarvesterMovementBehaviour behaviour,
            MovementContext context,
            BlockPos pos,
            BlockState state,
            boolean replant,
            boolean partial) {
        Level level = context.world;
        if (level.isClientSide) {
            return;
        }

        IntegerProperty ageProperty = getAgeProperty(state);
        if (ageProperty == null) {
            return;
        }

        int age = state.getValue(ageProperty);
        if (age <= 0) {
            return;
        }

        int maxAge = getMaxAge(state);
        if (!partial && age < maxAge) {
            return;
        }

        QualityHarvestAutomationContext.HarvestData previousHarvest =
                QualityHarvestAutomationContext.push(context, pos, state);
        DropData previousDropData = DropData.CURRENT.get();
        DropData.CURRENT.set(new DropData(
                LevelData.get(level, pos, true),
                state,
                null,
                level.getBlockState(pos.below())));
        try {
            BlockHelper.destroyBlockAs(
                    level,
                    pos,
                    null,
                    ItemStack.EMPTY,
                    1,
                    stack -> {
                        QualityHarvestAutomationContext.applyQuality(stack);
                        behaviour.dropItem(context, stack);
                    });
        } finally {
            if (previousDropData == null) {
                DropData.CURRENT.remove();
            } else {
                DropData.CURRENT.set(previousDropData);
            }
            QualityHarvestAutomationContext.pop(previousHarvest);
        }

        if (replant) {
            level.setBlock(pos, state.setValue(ageProperty, 0), 2);
        }
    }

    private static IntegerProperty getAgeProperty(BlockState state) {
        if (state.getBlock() instanceof FlowerClusterBlock) {
            return FlowerClusterBlock.CLUSTER_AGE;
        }
        if (state.getBlock() instanceof MushroomColonyBlock colony) {
            return colony.getAgeProperty();
        }
        return null;
    }

    private static int getMaxAge(BlockState state) {
        if (state.getBlock() instanceof FlowerClusterBlock cluster) {
            return cluster.getMaxAge();
        }
        if (state.getBlock() instanceof MushroomColonyBlock colony) {
            return colony.getMaxAge();
        }
        return 0;
    }
}
