package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.*;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageEntity;
import io.github.jasonsimpart.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import java.util.ArrayList;
import java.util.List;

final class MbdOrderDelivery {
    private MbdOrderDelivery() {}
    static void register() {
        NeoForge.EVENT_BUS.addListener(MbdOrderDelivery::tick);
        NeoForge.EVENT_BUS.addListener(MbdOrderDelivery::place);
        NeoForge.EVENT_BUS.addListener(MbdOrderDelivery::drops);
        NeoForge.EVENT_BUS.addListener(MbdOrderDelivery::removed);
    }
    private static void place(PlayerInteractEvent.RightClickBlock event) {
        if (event.getUseBlock() == net.neoforged.neoforge.common.util.TriState.FALSE) return;
        if (!event.getItemStack().is(ModItems.ORDER_DELIVERER_ITEM.get())
                || !BuiltInRegistries.BLOCK.getKey(event.getLevel().getBlockState(event.getPos()).getBlock()).equals(ResourceLocation.parse("create:white_table_cloth"))) return;
        var player = event.getEntity();
        if (!player.mayBuild() || !event.getLevel().mayInteract(player, event.getPos())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel().isClientSide) return;
        // Replacing a populated cloth would destroy its display/trade state.
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof TableClothBlockEntity cloth && !cloth.manuallyAddedItems.isEmpty()) return;
        if (event.getLevel().setBlockAndUpdate(event.getPos(), MBDRegistries.MACHINE_DEFINITIONS.get(MbdCompat.id("order_deliverer")).block().defaultBlockState())
                && !player.isCreative()) event.getItemStack().shrink(1);
    }
    private static void drops(MachineDropsEvent event) {
        if (!MbdSmallProcessing.is(event.machine, "order_deliverer")) return;
        event.drops.removeIf(stack -> stack.is(MBDRegistries.MACHINE_DEFINITIONS.get(MbdCompat.id("order_deliverer")).item()));
        event.drops.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:white_table_cloth"))));
        event.drops.add(ModItems.ORDER_DELIVERER_ITEM.toStack());
    }
    private static void removed(MachineRemovedEvent event) {
        if (MbdSmallProcessing.is(event.machine, "order_deliverer") && event.machine.getLevel() instanceof ServerLevel level) removeDrone(event.machine, level);
    }
    private static void removeDrone(MBDMachine machine, ServerLevel level) {
        var data = machine.getCustomData();
        if (data.hasUUID("droneUUID")) {
            var drone = level.getEntity(data.getUUID("droneUUID"));
            if (drone != null) drone.discard();
            data.remove("droneUUID");
            machine.getHolder().setChanged();
        }
    }
    private static void tick(MachineTickEvent event) {
        var machine = event.machine;
        if (!MbdSmallProcessing.is(machine, "order_deliverer") || !(machine.getLevel() instanceof ServerLevel level)) return;
        long time = level.getDayTime() % 24000;
        var data = machine.getCustomData();
        if (time < 1000 || time > 1100) removeDrone(machine, level);
        if (time == 1000 && !data.hasUUID("droneUUID")) {
            BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse("create_sa:brass_drone")).ifPresent(type -> {
                var drone = type.create(level);
                if (drone != null) {
                    drone.setPos(Vec3.atBottomCenterOf(machine.getPos().above()));
                    if (level.addFreshEntity(drone)) data.putUUID("droneUUID", drone.getUUID());
                    machine.getHolder().setChanged();
                }
            });
        }
        if (data.hasUUID("droneUUID")) {
            var drone = level.getEntity(data.getUUID("droneUUID"));
            if (drone != null) drone.setDeltaMovement(0, Math.sin(level.getGameTime() / 5d) * .05, 0);
        }
        if (time < 1100 || level.getGameTime() % 20 != 0) return;
        long day = level.getDayTime() / 24000;
        if (data.contains("settledDay") && data.getLong("settledDay") == day) return;
        removeDrone(machine, level);
        if (settle(machine)) {
            data.putLong("settledDay", day);
            machine.getHolder().setChanged();
        }
    }
    static boolean settle(MBDMachine machine) {
        var level = (ServerLevel) machine.getLevel();
        boolean settled = true, hasOrder = false;
        for (var direction : Direction.Plane.HORIZONTAL) {
            var segment = new ArrayList<TableClothBlockEntity>();
            ItemStack order = ItemStack.EMPTY;
            for (int index = 1; index <= 8; index++) {
                var pos = machine.getPos().relative(direction, index);
                if (!level.hasChunkAt(pos)) { settled = false; break; }
                var state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof com.simibubi.create.content.logistics.tableCloth.TableClothBlock)) break;
                if (!state.getValue(com.simibubi.create.content.logistics.tableCloth.TableClothBlock.HAS_BE))
                    level.setBlockAndUpdate(pos, state.setValue(com.simibubi.create.content.logistics.tableCloth.TableClothBlock.HAS_BE, true));
                if (!(level.getBlockEntity(pos) instanceof TableClothBlockEntity cloth)) break;
                var next = cloth.manuallyAddedItems.stream().filter(stack -> stack.is(ModItems.ORDER.get())).findFirst().orElse(ItemStack.EMPTY);
                if (!next.isEmpty() && !order.isEmpty()) { hasOrder = true; settled &= settleOnce(machine, segment, order); segment.clear(); }
                if (!next.isEmpty()) order = next;
                segment.add(cloth);
            }
            if (!order.isEmpty()) { hasOrder = true; settled &= settleOnce(machine, segment, order); }
        }
        return hasOrder && settled;
    }
    private static boolean settleOnce(MBDMachine machine, List<TableClothBlockEntity> segment, ItemStack order) {
        var level = (ServerLevel) machine.getLevel();
        var holder = segment.stream().filter(cloth -> cloth.manuallyAddedItems.stream().anyMatch(stack -> stack == order))
                .findFirst().orElse(null);
        if (holder == null) return false;
        long day = level.getDayTime() / 24000;
        var data = machine.getCustomData();
        var completed = data.getCompound("settledSegments");
        if (!completed.contains("day") || completed.getLong("day") != day) {
            completed = new CompoundTag();
            completed.putLong("day", day);
        }
        String key = Long.toString(holder.getBlockPos().asLong());
        if (completed.getBoolean(key)) return true;
        if (!settleSegment(level, segment, order)) return false;
        completed.putBoolean(key, true);
        data.put("settledSegments", completed);
        machine.getHolder().setChanged();
        return true;
    }
    static boolean settleSegment(ServerLevel level, List<TableClothBlockEntity> segment, ItemStack orderStack) {
        if (segment.stream().noneMatch(cloth -> cloth.manuallyAddedItems.stream().anyMatch(stack -> stack == orderStack))) return false;
        var goods = new ArrayList<ItemStack>();
        for (var cloth : segment) for (var stack : cloth.manuallyAddedItems) if (PackageItem.isPackage(stack)) {
            var content = PackageItem.getContents(stack);
            for (int slot = 0; slot < content.getSlots(); slot++) goods.add(content.getStackInSlot(slot));
        }
        var order = MbdOrders.info(orderStack);
        // Legacy anonymous orders have no reputation recipient. Owned orders wait for an online owner.
        if ((order.contains("ownerUUID") || order.contains("ownerName")) && MbdOrders.owner(level, order) == null) return false;
        var fulfillment = MbdOrders.fulfill(order, goods);
        if (fulfillment == null || segment.isEmpty()) return false;
        double score = fulfillment.score();
        var rewards = rewards(level, order, score);
        // An unavailable currency chain must not consume a completed order.
        if (rewards == null) return false;
        int cursor = 0;
        var displays = new java.util.LinkedHashMap<TableClothBlockEntity, List<ItemStack>>();
        for (var cloth : segment) {
            var display = new ArrayList<ItemStack>();
            for (var original : cloth.manuallyAddedItems) {
                var stack = original;
                if (stack == orderStack) {
                    stack = stack.copy();
                    stack.shrink(1);
                } else if (PackageItem.isPackage(stack)) {
                    var content = PackageItem.getContents(stack);
                    boolean changed = false, empty = true;
                    for (int slot = 0; slot < content.getSlots(); slot++) {
                        var remaining = fulfillment.remaining().get(cursor++);
                        changed |= remaining.getCount() != content.getStackInSlot(slot).getCount();
                        empty &= remaining.isEmpty();
                        content.setStackInSlot(slot, remaining);
                    }
                    if (changed) {
                        stack = empty ? ItemStack.EMPTY : stack.copy();
                        if (!empty) stack.set(com.simibubi.create.AllDataComponents.PACKAGE_CONTENTS,
                                com.simibubi.create.foundation.item.ItemHelper.containerContentsFromHandler(content));
                    }
                }
                if (!stack.isEmpty()) display.add(stack);
            }
            displays.put(cloth, display);
        }
        var start = segment.getFirst();
        var display = displays.get(start);
        var spawned = new ArrayList<PackageEntity>();
        for (int index = 0; index < rewards.size(); index += 9) {
            var parcel = PackageItem.containing(rewards.subList(index, Math.min(index + 9, rewards.size())));
            if (display.size() < 4) display.add(parcel);
            else {
                var entity = PackageEntity.fromItemStack(level, Vec3.atCenterOf(start.getBlockPos().above()), parcel);
                if (!level.addFreshEntity(entity)) {
                    spawned.forEach(PackageEntity::discard);
                    return false;
                }
                spawned.add(entity);
            }
        }
        // Only commit consumption after every reward has a destination.
        displays.forEach((cloth, items) -> {
            cloth.manuallyAddedItems.clear();
            cloth.manuallyAddedItems.addAll(items);
            cloth.notifyUpdate();
        });
        MbdOrders.award(level, order, score);
        return true;
    }
    private static List<ItemStack> rewards(ServerLevel level, CompoundTag order, double score) {
        var customer = MbdOrders.customer(order);
        var reward = customer.getAsJsonArray("reward");
        String table = reward == null ? "createdelightcore:orders/" + order.getString("type") : reward.get(0).getAsString().replace("createdelight:", "createdelightcore:");
        var loot = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(table)));
        var money = MbdFoodEconomy.money(MbdOrders.money(order) * score);
        if (money.isEmpty() || MbdFoodEconomy.coins(money).isEmpty()) return null;
        var output = new ArrayList<ItemStack>();
        double times = score * (reward == null ? 1 : reward.get(1).getAsDouble()) * order.getList("entries", net.minecraft.nbt.Tag.TAG_COMPOUND).size();
        for (int i = 0; i < times; i++) output.addAll(loot.getRandomItems(new LootParams.Builder(level).create(LootContextParamSets.EMPTY)));
        output.addAll(MbdFoodEconomy.coins(money));
        return output;
    }
}
