package io.github.jasonsimpart.compat.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import io.github.jasonsimpart.network.ChainCasingModifierPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** 0488 pipe editing and Alt chain casing, with no KubeJS/client-script dependency. */
public final class BasicInteractions {
    private static final ThreadLocal<Boolean> CHECKING_PERMISSION = ThreadLocal.withInitial(() -> false);
    private BasicInteractions() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, BasicInteractions::interact);
        NeoForge.EVENT_BUS.addListener(BasicInteractions::login);
    }

    private static void login(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        player.getPersistentData().remove(ChainCasingModifierPayload.KEY);
        player.sendSystemMessage(Component.translatable("message.createdelightcore.log_in", player.getName()));
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            io.github.jasonsimpart.server.DonorLogin.apply(serverPlayer);
    }

    private static void interact(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (CHECKING_PERMISSION.get() || event.getUseBlock() == net.neoforged.neoforge.common.util.TriState.FALSE
                || event.getHand() != InteractionHand.MAIN_HAND
                || !player.mayBuild() || player.isSpectator()) return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        boolean pipe = AllBlocks.FLUID_PIPE.has(state) && event.getItemStack().isEmpty();
        boolean alt = player.getPersistentData().getBoolean(ChainCasingModifierPayload.KEY);
        Block casing = event.getItemStack().is(AllBlocks.ANDESITE_CASING.asItem()) ? AllBlocks.ANDESITE_ENCASED_SHAFT.get()
                : event.getItemStack().is(AllBlocks.BRASS_CASING.asItem()) ? AllBlocks.BRASS_ENCASED_SHAFT.get() : null;
        boolean wrench = AllItems.WRENCH.isIn(event.getItemStack());
        boolean belt = alt && AllBlocks.BELT.has(state) && !player.isShiftKeyDown() && (casing != null || wrench);
        boolean shaft = alt && (AllBlocks.SHAFT.has(state) && !player.isShiftKeyDown() && casing != null
                || player.isShiftKeyDown() && wrench
                && (AllBlocks.ANDESITE_ENCASED_SHAFT.has(state) || AllBlocks.BRASS_ENCASED_SHAFT.has(state)));
        if (!pipe && !belt && !shaft) return;
        if (!level.isClientSide() && level.mayInteract(player, pos)) {
            if (pipe) {
                Direction face = player.isShiftKeyDown() ? event.getFace().getOpposite() : event.getFace();
                BlockState updated = togglePipe(state, face);
                if (updated == state) player.displayClientMessage(Component.translatable("message.createdelightcore.pipe")
                        .withStyle(ChatFormatting.RED), true);
                else {
                    level.setBlockAndUpdate(pos, updated);
                    level.playSound(null, pos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 0.6F, 1.2F);
                }
            } else if (belt) {
                BeltBlockEntity.CasingType type = wrench ? BeltBlockEntity.CasingType.NONE
                        : casing == AllBlocks.BRASS_ENCASED_SHAFT.get() ? BeltBlockEntity.CasingType.BRASS
                        : BeltBlockEntity.CasingType.ANDESITE;
                caseBelt(player, pos, type);
            } else caseShaft(player, pos, casing == null ? AllBlocks.SHAFT.get() : casing);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    static BlockState togglePipe(BlockState state, Direction face) {
        var property = PipeBlock.PROPERTY_BY_DIRECTION.get(face);
        long openings = PipeBlock.PROPERTY_BY_DIRECTION.values().stream().filter(state::getValue).count();
        return state.getValue(property) && openings <= 2 ? state : state.cycle(property);
    }

    static void caseShaft(Player player, BlockPos origin, Block target) {
        Level level = player.level();
        BlockState initial = level.getBlockState(origin);
        Direction.Axis axis = initial.getValue(BlockStateProperties.AXIS);
        boolean removing = target == AllBlocks.SHAFT.get();
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != axis) continue;
            for (int distance = 1; distance <= 64; distance++) {
                BlockPos pos = origin.relative(direction, distance);
                if (!level.hasChunkAt(pos)) break;
                BlockState state = level.getBlockState(pos);
                if (!state.is(initial.getBlock()) && (removing || !state.is(target))) break;
                if (state.getValue(BlockStateProperties.AXIS) != axis || !mayChange(player, pos, direction)) break;
                if (!state.is(target)) level.setBlockAndUpdate(pos, target.withPropertiesOf(state));
            }
        }
        if (!initial.is(target)) level.setBlockAndUpdate(origin, target.withPropertiesOf(initial));
    }

    static void caseBelt(Player player, BlockPos origin, BeltBlockEntity.CasingType type) {
        Level level = player.level();
        BeltBlockEntity segment = BeltHelper.getSegmentBE(level, origin);
        if (segment == null) return;
        BeltBlockEntity controller = segment.getControllerBE();
        if (controller == null) return;
        // Follow the actual controller; adjacent belts must never be modified.
        for (int i = Math.max(0, segment.index - 32); i <= Math.min(controller.beltLength - 1, segment.index + 32); i++) {
            BlockPos pos = BeltHelper.getPositionForOffset(controller, i);
            if (!level.hasChunkAt(pos) || !mayChange(player, pos, Direction.UP)) continue;
            BeltBlockEntity belt = BeltHelper.getSegmentBE(level, pos);
            if (belt != null && belt.getController().equals(segment.getController())) belt.setCasingType(type);
        }
    }

    private static boolean mayChange(Player player, BlockPos pos, Direction face) {
        if (!player.level().mayInteract(player, pos)) return false;
        // Give claim/protection mods the same veto for every affected neighbour.
        CHECKING_PERMISSION.set(true);
        try {
            var event = NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND,
                    pos, new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false)));
            return !event.isCanceled() && event.getUseBlock() != net.neoforged.neoforge.common.util.TriState.FALSE;
        } finally { CHECKING_PERMISSION.remove(); }
    }
}
