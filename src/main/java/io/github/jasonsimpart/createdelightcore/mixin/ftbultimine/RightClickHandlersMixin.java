package io.github.jasonsimpart.createdelightcore.mixin.ftbultimine;

import com.google.common.collect.BiMap;
import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.QualityUtils;
import dev.ftb.mods.ftbultimine.BrokenItemHandler;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.RightClickHandlers;
import dev.ftb.mods.ftbultimine.mixin.AxeItemAccess;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.codehaus.plexus.util.dag.DAG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.properties.IToolProvider;

import java.util.*;


@Mixin(RightClickHandlers.class)
public class RightClickHandlersMixin {
    @Inject(method = "cropHarvesting", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/ItemCollection;add(Lnet/minecraft/world/item/ItemStack;)V"), remap = false)
    private static void cropHarvestingApplyQuality(ServerPlayer player, InteractionHand hand, BlockPos clickPos, Direction face, FTBUltiminePlayerData data, CallbackInfoReturnable<Integer> cir, @Local(ordinal = 1) BlockPos pos, @Local BlockState state, @Local ItemStack stack) {
        QualityUtils.applyQuality(stack, state, LevelData.get(player.level(), pos), player, player.level().getBlockState(pos.below()));
    }
    @Inject(method = "axeStripping", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void axeStrippingMixin(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data, CallbackInfoReturnable<Integer> cir) {

        Set<SoundEvent> sounds = new HashSet<>();
        BrokenItemHandler brokenItemHandler = new BrokenItemHandler();
        Level level = player.level();
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.getItem() instanceof IToolProvider toolProvider) {
            ToolData toolData = toolProvider.getToolData(itemStack);
            Collection<ToolAction> tools = toolData.getValues().stream()
                    .filter(tool -> toolData.getLevel(tool) > 0)
                    .filter(tool -> (ToolActions.AXE_STRIP.equals(tool) || ToolActions.AXE_SCRAPE.equals(tool) || ToolActions.AXE_WAX_OFF.equals(tool)))
                    .toList();
            boolean strip = tools.contains(ToolActions.AXE_STRIP);
            boolean scrape = tools.contains(ToolActions.AXE_SCRAPE);
            boolean waxOff = tools.contains(ToolActions.AXE_WAX_OFF);
            for (BlockPos pos : data.cachedPositions()) {
                BlockState state = player.level().getBlockState(pos);
                Optional<BlockState> stripping = Optional.ofNullable(AxeItem.getAxeStrippingState(state));
                Optional<BlockState> scraping = WeatheringCopper.getPrevious(state);
                Optional<BlockState> waxing = Optional.ofNullable((Block) ((BiMap) HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(state.getBlock())).map((block) -> block.withPropertiesOf(state));
                Optional<BlockState> actual = Optional.empty();
                if (stripping.isPresent() && strip) {
                    sounds.add(SoundEvents.AXE_STRIP);
                    actual = stripping;
                } else if (scraping.isPresent() && scrape) {
                    sounds.add(SoundEvents.AXE_SCRAPE);
                    level.levelEvent(player, 3005, pos, 0);
                    actual = scraping;
                } else if (waxing.isPresent() && waxOff) {
                    sounds.add(SoundEvents.AXE_WAX_OFF);
                    level.levelEvent(player, 3004, pos, 0);
                    actual = waxing;
                }

                if (actual.isPresent()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, pos, itemStack);
                    level.setBlock(pos, (BlockState) actual.get(), 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, (BlockState) actual.get()));
                    itemStack.hurtAndBreak(1, player, brokenItemHandler);
                    if (brokenItemHandler.isBroken) {
                        break;
                    }
                }
            }

            sounds.forEach((sound) -> {
                level.playSound(null, clickPos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            });
            cir.setReturnValue(sounds.size());
        }
    }
}
