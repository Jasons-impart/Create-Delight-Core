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
import net.brdle.collectorsreap.common.block.FruitBushBlock;
import net.brdle.collectorsreap.common.block.LimeBushBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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
            registerCollectorsReapFruitBush(CRBlocks.LIME_BUSH.get());
            registerCollectorsReapFruitBush(CRBlocks.POMEGRANATE_BUSH.get());

        });
    }

    private static void registerLootTableHarvest(Block block) {
        HarvesterMovementBehaviourExtension.REGISTRY.put(
                block,
                CreateDelightCoreHarvesterMovementBehaviorExtensions::harvestWithLootTable);
    }

    private static void registerCollectorsReapFruitBush(Block block) {
        HarvesterMovementBehaviourExtension.REGISTRY.put(
                block,
                CreateDelightCoreHarvesterMovementBehaviorExtensions::harvestCollectorsReapFruitBush);
    }

    public static void harvestCollectorsReapFruitBush(
            HarvesterMovementBehaviour behaviour,
            MovementContext context,
            BlockPos pos,
            BlockState state,
            boolean replant,
            boolean partial) {
        if (!(state.getBlock() instanceof FruitBushBlock fruitBush)
                || state.getValue(FruitBushBlock.STUNTED)) {
            return;
        }

        Level level = context.world;
        if (level.isClientSide) {
            return;
        }

        BlockPos lowerPos = state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
                ? pos.below()
                : pos;
        BlockPos upperPos = lowerPos.above();
        BlockState lowerState = level.getBlockState(lowerPos);
        if (!lowerState.is(state.getBlock())
                || lowerState.getValue(DoublePlantBlock.HALF) != DoubleBlockHalf.LOWER
                || lowerState.getValue(FruitBushBlock.STUNTED)) {
            return;
        }

        int age = lowerState.getValue(FruitBushBlock.AGE);
        int maxAge = fruitBush.getMaxAge();
        if (age <= 0 || (!partial && age < maxAge)) {
            return;
        }

        QualityHarvestAutomationContext.HarvestData previousHarvest =
                QualityHarvestAutomationContext.push(context, lowerPos, lowerState);
        DropData previousDropData = DropData.CURRENT.get();
        DropData.CURRENT.set(new DropData(
                LevelData.get(level, lowerPos, true),
                lowerState,
                null,
                level.getBlockState(lowerPos.below())));
        try {
            if (age >= maxAge) {
                int baseCount = fruitBush instanceof LimeBushBlock ? 2 : 1;
                ItemStack fruit = new ItemStack(fruitBush.getFruit(), baseCount + level.random.nextInt(2));
                dropWithQuality(behaviour, context, fruit);

                if (replant) {
                    BlockState upperState = level.getBlockState(upperPos);
                    if (!upperState.is(lowerState.getBlock())) {
                        upperState = lowerState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                    }

                    level.setBlock(lowerPos, lowerState
                            .setValue(FruitBushBlock.AGE, 2)
                            .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), 2);
                    level.setBlock(upperPos, upperState
                            .setValue(FruitBushBlock.AGE, 2)
                            .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), 2);
                    level.playSound(null, lowerPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                            SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
                    return;
                }
            }

            if (partial || age >= maxAge) {
                BlockHelper.destroyBlock(level, upperPos, 1, stack -> dropWithQuality(behaviour, context, stack));
                BlockHelper.destroyBlock(level, lowerPos, 1, stack -> dropWithQuality(behaviour, context, stack));
            }
        } finally {
            if (previousDropData == null) {
                DropData.CURRENT.remove();
            } else {
                DropData.CURRENT.set(previousDropData);
            }
            QualityHarvestAutomationContext.pop(previousHarvest);
        }
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

    private static void dropWithQuality(
            HarvesterMovementBehaviour behaviour,
            MovementContext context,
            ItemStack stack) {
        QualityHarvestAutomationContext.applyQuality(stack);
        behaviour.dropItem(context, stack);
    }
}
