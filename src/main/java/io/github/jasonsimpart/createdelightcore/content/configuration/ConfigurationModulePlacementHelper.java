package io.github.jasonsimpart.createdelightcore.content.configuration;

import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;
import java.util.function.Predicate;

public final class ConfigurationModulePlacementHelper implements IPlacementHelper {
    public static final ConfigurationModulePlacementHelper INSTANCE = new ConfigurationModulePlacementHelper();

    private static boolean registered;

    private ConfigurationModulePlacementHelper() {
    }

    public static void register() {
        if (!registered) {
            PlacementHelpers.register(INSTANCE);
            registered = true;
        }
    }

    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return stack -> stack.getItem() instanceof ConfigurationModuleItem;
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return state -> true;
    }

    @Override
    public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos,
                                     BlockHitResult hitResult) {
        ItemStack module = player.getMainHandItem();
        if (!(module.getItem() instanceof ConfigurationModuleItem)) {
            module = player.getOffhandItem();
        }
        return findOffset(player, level, state, pos, hitResult, module);
    }

    @Override
    public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos,
                                     BlockHitResult hitResult, ItemStack heldItem) {
        return findOffset(player, level, state, pos, hitResult, heldItem);
    }

    public static PlacementOffset findOffset(Player player, Level level, BlockState state, BlockPos pos,
                                             BlockHitResult hitResult, ItemStack moduleStack) {
        if (player.isShiftKeyDown()) {
            return PlacementOffset.fail();
        }
        Optional<BlockItem> target = ConfigurationModuleManager.getSnapshotTarget(moduleStack);
        if (target.isEmpty()) {
            return PlacementOffset.fail();
        }
        ItemStack targetStack = new ItemStack(target.get());
        for (IPlacementHelper delegate : PlacementHelpers.getHelpersView()) {
            if (delegate == INSTANCE || !delegate.matchesItem(targetStack) || !delegate.matchesState(state)) {
                continue;
            }
            PlacementOffset offset = delegate.getOffset(player, level, state, pos, hitResult, targetStack);
            if (offset.isSuccessful()) {
                return offset;
            }
        }
        return PlacementOffset.fail();
    }

    public static InteractionResult placeWithOffset(Level level, BlockItem blockItem, Player player,
                                                    InteractionHand hand, ItemStack targetStack,
                                                    BlockHitResult hitResult, PlacementOffset offset) {
        if (!offset.isSuccessful() || !offset.isReplaceable(level)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos newPos = offset.getBlockPos();
        if (!level.mayInteract(player, newPos)) {
            return InteractionResult.PASS;
        }

        BlockState placedState = offset.getTransform().apply(blockItem.getBlock().defaultBlockState());
        if (placedState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            FluidState fluidState = level.getFluidState(newPos);
            placedState = placedState.setValue(BlockStateProperties.WATERLOGGED,
                    fluidState.getType() == Fluids.WATER);
        }
        if (CatnipServices.HOOKS.playerPlaceSingleBlock(player, level, newPos, placedState)) {
            return InteractionResult.FAIL;
        }

        BlockState newState = level.getBlockState(newPos);
        SoundType soundType = newState.getSoundType(level, newPos, player);
        level.playSound(null, newPos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, newPos, GameEvent.Context.of(player, newState));
        player.awardStat(Stats.ITEM_USED.get(blockItem));
        newState.getBlock().setPlacedBy(level, newPos, newState, player, targetStack);

        if (player instanceof ServerPlayer serverPlayer) {
            UseOnContext targetContext = new UseOnContext(level, player, hand, targetStack, hitResult);
            CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, newPos, targetContext.getItemInHand());
        }
        return InteractionResult.SUCCESS;
    }
}
