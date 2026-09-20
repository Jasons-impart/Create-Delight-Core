package io.github.jasonsimpart.compat.tacz;

import appeng.api.config.Actionable;
import appeng.block.networking.EnergyCellBlockItem;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Pack energy-gun charging, registered only when both TACZ and AE2 are installed. */
public final class TaczEnergyReloadCompat {
    private static final Set<ResourceLocation> ENERGY_GUNS = Set.of(
            ResourceLocation.parse("applied_armorer:moritz_mg_emg_prototype"),
            ResourceLocation.parse("applied_armorer:moritz_mg_hmg22")
    );

    private TaczEnergyReloadCompat() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(TaczEnergyReloadCompat::tick);
    }

    private static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.level().getGameTime() % 120 != 0) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null || !ENERGY_GUNS.contains(gun.getGunId(stack))) {
            return;
        }
        var index = TimelessAPI.getCommonGunIndex(gun.getGunId(stack));
        if (index.isEmpty()) {
            return;
        }
        var data = index.get().getGunData();
        int capacity = AttachmentDataUtils.getAmmoCountWithAttachment(stack, data);
        int current = gun.getCurrentAmmoCount(stack);
        if (capacity <= 0 || current >= capacity) {
            return;
        }
        double perRound = 16000.0 * data.getAmmoAmount() / capacity;
        if (!Double.isFinite(perRound) || perRound <= 0) {
            return;
        }
        List<Cell> cells = new ArrayList<>();
        double available = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack cell = player.getInventory().getItem(slot);
            if (cell.getItem() instanceof EnergyCellBlockItem item) {
                double power = item.getAECurrentPower(cell);
                if (Double.isFinite(power) && power > 0) {
                    cells.add(new Cell(slot, cell, power));
                    available += power;
                }
            }
        }
        cells.sort(Comparator.comparingDouble(Cell::power));
        int rounds = (int) Math.min(capacity - current, Math.floor(available / perRound));
        if (rounds <= 0) {
            return;
        }

        // Plan against single-item copies so insufficient extraction never consumes energy.
        // Like the old script, only one cell from each inventory stack participates per tick.
        double remaining = rounds * perRound;
        List<Debit> debits = new ArrayList<>();
        for (Cell cell : cells) {
            if (remaining <= 0) {
                break;
            }
            ItemStack single = cell.stack().copyWithCount(1);
            double extracted = ((EnergyCellBlockItem) single.getItem()).extractAEPower(single, remaining, Actionable.MODULATE);
            remaining -= extracted;
            debits.add(new Debit(cell, single));
        }
        if (remaining > 1.0E-6) {
            return;
        }
        var inventory = player.getInventory();
        ItemStack[] planned = new ItemStack[inventory.getContainerSize()];
        for (int slot = 0; slot < planned.length; slot++) planned[slot] = inventory.getItem(slot);
        List<ItemStack> splitCells = new ArrayList<>();
        for (Debit debit : debits) {
            int slot = debit.cell().slot();
            if (debit.cell().stack().getCount() > 1) {
                planned[slot] = debit.cell().stack().copyWithCount(debit.cell().stack().getCount() - 1);
                splitCells.add(debit.single());
            } else planned[slot] = debit.single();
        }
        // Reserve main-inventory destinations before spending power or granting ammo. Never drop a cell.
        for (var cell : splitCells) {
            int count = cell.getCount();
            for (int pass = 0; pass < 2 && count > 0; pass++) {
                for (int slot = 0; slot < inventory.items.size() && count > 0; slot++) {
                    var present = planned[slot];
                    if (pass == 0 ? present.isEmpty() || !ItemStack.isSameItemSameComponents(present, cell) : !present.isEmpty()) continue;
                    int moved = Math.min(count, Math.min(inventory.getMaxStackSize(), cell.getMaxStackSize()) - present.getCount());
                    if (moved <= 0) continue;
                    planned[slot] = cell.copyWithCount(present.getCount() + moved);
                    count -= moved;
                }
            }
            if (count > 0) return;
        }
        for (int slot = 0; slot < planned.length; slot++)
            if (planned[slot] != inventory.getItem(slot)) inventory.setItem(slot, planned[slot]);
        gun.setCurrentAmmoCount(stack, current + rounds);
        player.getInventory().setChanged();
        player.displayClientMessage(Component.translatable("message.createdelightcore.tacz.energy_reload",
                Component.literal(Integer.toString(rounds)).withStyle(ChatFormatting.GREEN),
                Component.literal((current + rounds) + " / " + capacity).withStyle(ChatFormatting.YELLOW)), true);
    }

    private record Cell(int slot, ItemStack stack, double power) {
    }

    private record Debit(Cell cell, ItemStack single) {
    }
}
