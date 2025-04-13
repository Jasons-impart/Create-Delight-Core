package io.github.jasonsimpart.createdelightcore.content.contraption.components.actor;


import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.BlockHelper;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.satisfy.nethervinery.core.registry.NetherObjectRegistry;
import net.satisfy.vinery.core.block.GrapeBush;
import net.satisfy.vinery.core.block.GrapeVineBlock;
import net.satisfy.vinery.core.block.StemBlock;
import net.satisfy.vinery.core.registry.GrapeTypeRegistry;
import net.satisfy.vinery.core.registry.ObjectRegistry;

import static plus.dragons.createcentralkitchen.content.contraptions.components.actor.HarvesterMovementBehaviourExtension.REGISTRY;
@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VineryHarvesterMovementBehaviorExtensions {
    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            REGISTRY.put(ObjectRegistry.RED_GRAPE_BUSH.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeBush);
            REGISTRY.put(ObjectRegistry.WHITE_GRAPE_BUSH.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeBush);
            REGISTRY.put(ObjectRegistry.TAIGA_RED_GRAPE_BUSH.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeBush);
            REGISTRY.put(ObjectRegistry.TAIGA_WHITE_GRAPE_BUSH.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeBush);
            REGISTRY.put(ObjectRegistry.SAVANNA_RED_GRAPE_BUSH.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeBush);
            REGISTRY.put(ObjectRegistry.SAVANNA_WHITE_GRAPE_BUSH.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeBush);
            REGISTRY.put(NetherObjectRegistry.CRIMSON_GRAPE_BUSH.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeBush);
            REGISTRY.put(NetherObjectRegistry.WARPED_GRAPE_BUSH.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeBush);
            REGISTRY.put(ObjectRegistry.JUNGLE_RED_GRAPE_BUSH.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeVine);
            REGISTRY.put(ObjectRegistry.JUNGLE_WHITE_GRAPE_BUSH.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeVine);
            REGISTRY.put(ObjectRegistry.GRAPEVINE_STEM.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeStem);
            REGISTRY.put(NetherObjectRegistry.OBSIDIAN_STEM.get(),
                    VineryHarvesterMovementBehaviorExtensions::harvestGrapeStem);


        });
    }

    public static void harvestGrapeBush(HarvesterMovementBehaviour behaviour, MovementContext context, BlockPos
            pos, BlockState state, boolean replant, boolean partial) {
        if (!(state.getBlock() instanceof GrapeBush bush))
            return;
        int age = state.getValue(GrapeBush.AGE);
        if (age <= 0)
            return;
        if (!partial && age < 3)
            return;
        Level level = context.world;
        if (replant) {
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(GrapeBush.AGE, 1), 2);
        } else {
            BlockHelper.destroyBlock(level, pos, 1, $ -> {});
        }
//        CreateDelightCore.LOGGER.info("fruits: " + bush.type.getFruit() + "seeds:" + bush.type.getSeeds() + "bottle:" + bush.type.getBottle());
        behaviour.dropItem(context, new ItemStack(bush.type.getFruit(), level.random.nextInt(2) + 1));
    }

    public static void harvestGrapeVine(HarvesterMovementBehaviour behaviour, MovementContext context, BlockPos
            pos, BlockState state, boolean replant, boolean partial) {
        if (!(state.getBlock() instanceof GrapeVineBlock vineBlock))
            return;
        int age = state.getValue(GrapeBush.AGE);
        if (age <= 0)
            return;
        if (!partial && age < 3)
            return;
        Level level = context.world;
        if (replant) {
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(GrapeVineBlock.AGE, 1), 2);
        } else {
            BlockHelper.destroyBlock(level, pos, 1, $ -> {});
        }
        behaviour.dropItem(context, new ItemStack(vineBlock.type.getFruit(), level.random.nextInt(2) + 1));
    }
    public static void harvestGrapeStem(HarvesterMovementBehaviour behaviour, MovementContext context, BlockPos
            pos, BlockState state, boolean replant, boolean partial) {
        if (!(state.getBlock() instanceof StemBlock stemBlock))
            return;
        stemBlock.isMature(state);
        int age = state.getValue(StemBlock.AGE);
        if (age <= 0)
            return;
        if (!partial && age < 4)
            return;
        Level level = context.world;
        int x = 1 + level.random.nextInt(stemBlock.isMature(state) ? 2 : 1);
        int bonus = stemBlock.isMature(state) ? 2 : 1;
        behaviour.dropItem(context, new ItemStack(state.getValue(StemBlock.GRAPE).getFruit(), x + bonus));
        if (replant) {
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(StemBlock.AGE, 2), 2);

        } else {
            behaviour.dropItem(context, new ItemStack(state.getValue(StemBlock.GRAPE).getSeeds(), 1));
            level.setBlock(pos, state.setValue(StemBlock.GRAPE, GrapeTypeRegistry.NONE), 2);
        }
    }
}
