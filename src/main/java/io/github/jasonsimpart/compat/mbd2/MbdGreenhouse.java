package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineUIEvent;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;

final class MbdGreenhouse {
    private static final String REFUNDS = "createdelightcorePendingRefunds";
    private MbdGreenhouse() {}

    static void register() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(MbdGreenhouse::ui);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (com.lowdragmc.mbd2.common.machine.definition.config.event.MachineTickEvent event) -> {
                    if (MbdSmallProcessing.is(event.machine, "greenhouse_builder") && !event.machine.getLevel().isClientSide
                            && event.machine.getLevel().getGameTime() % 20 == 0) flushRefunds(event.machine);
                });
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (com.lowdragmc.mbd2.common.machine.definition.config.event.MachineDropsEvent event) -> {
                    if (!MbdSmallProcessing.is(event.machine, "greenhouse_builder")) return;
                    for (var tag : event.machine.getCustomData().getList(REFUNDS, net.minecraft.nbt.Tag.TAG_COMPOUND))
                        ItemStack.parse(event.machine.getLevel().registryAccess(), tag).ifPresent(event.drops::add);
                    event.machine.getCustomData().remove(REFUNDS);
                    event.machine.getHolder().setChanged();
                });
    }

    static int size(MBDMachine machine, String key) {
        return machine.getCustomData().contains(key) ? Math.clamp(machine.getCustomData().getInt(key), 3, 64) : 9;
    }

    static void ui(MachineUIEvent event) {
        if (!event.machine.getDefinition().id().equals(MbdCompat.id("greenhouse_builder")) || event.ui == null) return;
        for (var key : new String[]{"houseLength", "houseWidth", "houseHeight"}) {
            event.ui.selectId(key, TextField.class).forEach(field -> {
                field.setNumbersOnlyInt(3, 64);
                field.bind(DataBindingBuilder.string(() -> Integer.toString(size(event.machine, key)), value -> {
                    try {
                        event.machine.getCustomData().putInt(key, Math.clamp(Integer.parseInt(value), 3, 64));
                        event.machine.getHolder().setChanged();
                    } catch (NumberFormatException ignored) { }
                }).build());
            });
        }
        event.ui.selectId("buildButton", Button.class).forEach(button -> button.setOnServerClick(click -> build(event.machine, event.player)));
    }

    static int build(MBDMachine machine, Player player) {
        var level = machine.getLevel();
        if (level.isClientSide) return 0;
        flushRefunds(machine);
        if (machine.getCustomData().contains(REFUNDS)) return message(player, "refund_pending");
        var sample = ((ItemSlotCapabilityTrait) machine.getTraitByName("item_slot")).storage.getStackInSlot(0);
        if (sample.isEmpty()) return message(player, "no_material");
        if (!(sample.getItem() instanceof BlockItem block) || block.getClass() != BlockItem.class
                || !java.util.List.of(net.minecraft.world.level.block.Block.class,
                        net.minecraft.world.level.block.Blocks.GLASS.getClass(), net.minecraft.world.level.block.Blocks.WHITE_STAINED_GLASS.getClass(),
                        net.minecraft.world.level.block.Blocks.TINTED_GLASS.getClass()).contains(block.getBlock().getClass())
                || !sample.getComponentsPatch().isEmpty() || block.getBlock().defaultBlockState().hasBlockEntity()
                || !net.minecraft.world.level.block.Block.isShapeFullBlock(
                        block.getBlock().defaultBlockState().getCollisionShape(level, machine.getPos())))
            return message(player, "invalid_material");
        var builder = player == null ? net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(
                (net.minecraft.server.level.ServerLevel) level) : player;
        if (!builder.mayBuild() || builder.isSpectator()) return 0;
        var handlers = new ArrayList<IItemHandler>();
        for (var direction : Direction.values()) {
            var position = machine.getPos().relative(direction);
            var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, position, direction.getOpposite());
            if (handler == null) handler = level.getCapability(Capabilities.ItemHandler.BLOCK, position, null);
            if (handler != null && !handlers.contains(handler)) handlers.add(handler);
        }
        if (handlers.isEmpty()) return message(player, "no_inventory");
        int length = size(machine, "houseLength"), width = size(machine, "houseWidth"), height = size(machine, "houseHeight");
        int shellSize = length * width * height - (length - 2) * (width - 2) * (height - 2);
        if (shellSize > 2048) return message(player, "build_limit", 2048);
        int skipped = 0;
        var targets = new ArrayList<BlockPos>();
        for (int x = 0; x < length; x++) for (int y = 0; y < height; y++) for (int z = 0; z < width; z++) {
            if (x != 0 && x != length - 1 && y != 0 && y != height - 1 && z != 0 && z != width - 1) continue;
            var target = machine.getPos().offset(x - length / 2, y - 1, z - width / 2);
            if (!level.hasChunkAt(target) || level.isOutsideBuildHeight(target) || !level.getWorldBorder().isWithinBounds(target)) {
                return message(player, "outside_world");
            }
            if (level.isEmptyBlock(target)) {
                if (!level.mayInteract(builder, target) || !builder.mayUseItemAt(target, Direction.UP, sample)) return 0;
                targets.add(target);
            } else skipped++;
        }
        if (targets.isEmpty()) return message(player, "no_air", skipped);
        record Extraction(IItemHandler handler, int slot, int amount) {}
        var plan = new ArrayList<Extraction>();
        int remaining = targets.size();
        for (var handler : handlers) for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            var stack = handler.getStackInSlot(slot);
            if (!ItemStack.isSameItemSameComponents(sample, stack)) continue;
            var extracted = handler.extractItem(slot, remaining, true);
            if (!ItemStack.isSameItemSameComponents(sample, extracted)) continue;
            plan.add(new Extraction(handler, slot, extracted.getCount()));
            remaining -= extracted.getCount();
        }
        if (remaining != 0) return message(player, "not_enough_material", targets.size(), sample.getHoverName(), targets.size() - remaining);
        var withdrawn = new ArrayList<ItemStack>();
        for (var entry : plan) {
            var stack = entry.handler.extractItem(entry.slot, entry.amount, false);
            withdrawn.add(stack);
            if (stack.getCount() == entry.amount && ItemStack.isSameItemSameComponents(sample, stack)) continue;
            // Defensive rollback for handlers whose extraction disagrees with simulation.
            for (int i = 0; i < withdrawn.size(); i++) {
                var restore = plan.get(i);
                refund(machine, restore.handler, restore.slot, withdrawn.get(i));
            }
            return message(player, "extract_failed");
        }
        int placed = 0;
        for (var target : targets) {
            if (!level.isEmptyBlock(target)) continue;
            var snapshot = net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(), level, target);
            var state = block.getBlock().defaultBlockState();
            if (!level.setBlock(target, state, 2)) continue;
            if (net.neoforged.neoforge.event.EventHooks.onBlockPlace(builder, snapshot, Direction.UP)) {
                snapshot.restore(2);
                continue;
            }
            level.blockUpdated(target, state.getBlock());
            state.updateNeighbourShapes(level, target, 3);
            placed++;
        }
        int remainingRefund = targets.size() - placed;
        for (var entry : plan) {
            int count = Math.min(remainingRefund, entry.amount);
            if (count > 0) refund(machine, entry.handler, entry.slot, sample.copyWithCount(count));
            remainingRefund -= count;
        }
        level.playSound(null, machine.getPos(), net.minecraft.sounds.SoundEvents.STONE_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1, 1);
        message(player, "success", placed, skipped);
        return placed;
    }

    static void refund(MBDMachine machine, IItemHandler inventory, int slot, ItemStack stack) {
        var remaining = inventory.insertItem(slot, stack, false);
        if (!remaining.isEmpty()) remaining = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(inventory, remaining, false);
        if (!remaining.isEmpty() && !spawnRefund(machine, remaining)) {
            var pending = machine.getCustomData().getList(REFUNDS, net.minecraft.nbt.Tag.TAG_COMPOUND);
            pending.add(remaining.save(machine.getLevel().registryAccess()));
            machine.getCustomData().put(REFUNDS, pending);
            machine.getHolder().setChanged();
        }
    }

    static void flushRefunds(MBDMachine machine) {
        var data = machine.getCustomData();
        if (!data.contains(REFUNDS)) return;
        var pending = new net.minecraft.nbt.ListTag();
        for (var tag : data.getList(REFUNDS, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            var stack = ItemStack.parse(machine.getLevel().registryAccess(), tag).orElse(ItemStack.EMPTY);
            if (!stack.isEmpty() && !spawnRefund(machine, stack)) pending.add(tag);
        }
        if (pending.isEmpty()) data.remove(REFUNDS); else data.put(REFUNDS, pending);
        machine.getHolder().setChanged();
    }

    private static boolean spawnRefund(MBDMachine machine, ItemStack stack) {
        var pos = machine.getPos();
        var entity = new net.minecraft.world.entity.item.ItemEntity(machine.getLevel(),
                pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack);
        entity.setDefaultPickUpDelay();
        return machine.getLevel().addFreshEntity(entity);
    }

    private static int message(Player player, String key, Object... arguments) {
        if (player != null) player.sendSystemMessage(Component.translatable("message.createdelight.greenhouse_builder." + key, arguments));
        return 0;
    }
}
