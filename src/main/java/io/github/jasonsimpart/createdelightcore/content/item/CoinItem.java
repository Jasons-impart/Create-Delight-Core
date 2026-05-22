package io.github.jasonsimpart.createdelightcore.content.item;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.block.CoinPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class CoinItem extends Item {
    private final String coinTier;

    public CoinItem(String coinTier, Properties properties) {
        super(properties);
        this.coinTier = coinTier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Block block = ForgeRegistries.BLOCKS.getValue(CreateDelightCore.id(coinTier + "_coin_pile"));
        if (!(block instanceof CoinPileBlock coinPileBlock))
            return super.useOn(context);

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (clickedState.is(coinPileBlock) && clickedState.getValue(CoinPileBlock.LAYERS) < 8) {
            return placeCoinPile(context, clickedPos,
                    clickedState.setValue(CoinPileBlock.LAYERS, clickedState.getValue(CoinPileBlock.LAYERS) + 1));
        }

        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        BlockPos placePos = placeContext.getClickedPos();
        BlockState placeState = coinPileBlock.getStateForPlacement(placeContext);
        if (placeState == null || !level.getBlockState(placePos).canBeReplaced(placeContext)
                || !placeState.canSurvive(level, placePos)) {
            return InteractionResult.FAIL;
        }

        return placeCoinPile(context, placePos, placeState);
    }

    private InteractionResult placeCoinPile(UseOnContext context, BlockPos pos, BlockState state) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (!level.isClientSide) {
            level.setBlock(pos, state, 3);
            SoundType soundType = state.getSoundType(level, pos, player);
            level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
            if (player == null || !player.getAbilities().instabuild)
                stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
