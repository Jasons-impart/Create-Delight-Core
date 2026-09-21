package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachinePlacedEvent;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineTickEvent;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import java.util.ArrayList;
import io.github.jasonsimpart.util.ModIds;

final class MbdSellBin {
    private MbdSellBin() {}
    static void register() {
        NeoForge.EVENT_BUS.addListener(MbdSellBin::placed);
        NeoForge.EVENT_BUS.addListener(MbdSellBin::tick);
    }
    private static void placed(MachinePlacedEvent event) {
        if (MbdSmallProcessing.is(event.machine, "sell_bin") && event.player instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            event.machine.getCustomData().putUUID("owner", player.getUUID());
            event.machine.getHolder().setChanged();
        }
    }
    private static void tick(MachineTickEvent event) {
        var machine = event.machine;
        if (!MbdSmallProcessing.is(machine, "sell_bin") || machine.getLevel().isClientSide
                || machine.getLevel().getDayTime() % 24000 < 20 || machine.getLevel().getGameTime() % 20 != 0
                || !machine.getCustomData().hasUUID("owner")) return;
        long day = machine.getLevel().getDayTime() / 24000;
        if (machine.getCustomData().contains("soldDay") && machine.getCustomData().getLong("soldDay") == day) return;
        var player = machine.getLevel().getServer().getPlayerList().getPlayer(machine.getCustomData().getUUID("owner"));
        if (player != null) {
            if (sell(machine, player)) {
                machine.getCustomData().putLong("soldDay", day);
                machine.getHolder().setChanged();
            }
        }
    }
    static boolean sell(MBDMachine machine, ServerPlayer player) {
        var handler = MoneyAPI.getApi().GetPlayersMoneyHandler(player);
        return sell(machine, player, handler::insertMoney);
    }
    static boolean sell(MBDMachine machine, ServerPlayer player,
            java.util.function.BiFunction<io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue, Boolean,
                    io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue> deposit) {
        if (!(machine.getTraitByName("item_slot") instanceof com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait trait)) return false;
        // External access is deliberately input-only; settlement owns the internal storage.
        var inventory = trait.storage;
        var slots = new ArrayList<Integer>();
        var receipt = new ArrayList<Component>();
        double amount = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            var stack = inventory.getStackInSlot(slot);
            double price = MbdFoodEconomy.price(stack) * stack.getCount();
            // The old script paid <=1-value stacks without consuming them. Keep those unsold.
            if (!Double.isFinite(price) || price <= 1) continue;
            if (inventory.extractItem(slot, stack.getCount(), true).getCount() != stack.getCount()) continue;
            slots.add(slot);
            amount += price;
            String stars = "";
            if (net.neoforged.fml.ModList.get().isLoaded(ModIds.QUALITY_FOOD)) {
                var quality = io.github.jasonsimpart.compat.qualityfood.QualityFoodCompat.getQualityData(stack);
                if (quality != null && quality.level() > 0) stars = " " + "★".repeat(Math.min(quality.level(), 3));
            }
            receipt.add(stack.getHoverName().copy().append(stars).append(" ×" + stack.getCount() + " ").append(MbdFoodEconomy.text(MbdFoodEconomy.money(price))));
        }
        var money = MbdFoodEconomy.money(amount);
        if (slots.isEmpty()) return true;
        if (money.isEmpty() || MbdFoodEconomy.coins(money).isEmpty()) return false;
        if (!deposit.apply(money, true).isEmpty()) return false;
        // This inventory belongs to the machine; no asynchronous inventory mutation occurs in a server tick.
        for (int slot : slots) inventory.extractItem(slot, inventory.getStackInSlot(slot).getCount(), false);
        var remainder = deposit.apply(money, false);
        for (var coin : MbdFoodEconomy.coins(remainder)) if (!player.getInventory().add(coin)) player.drop(coin, false);
        machine.getHolder().setChanged();
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("message.createdelight.sell_bin_hint").withStyle(ChatFormatting.GOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(MbdFoodEconomy.text(money)));
        player.sendSystemMessage(Component.translatable("message.createdelightcore.sell_bin_receipt").withStyle(ChatFormatting.GOLD));
        receipt.forEach(player::sendSystemMessage);
        player.sendSystemMessage(Component.translatable("message.createdelight.sell_bin_total", MbdFoodEconomy.text(money)));
        var sound = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getOptional(net.minecraft.resources.ResourceLocation.parse("iceandfire:gold_pile_step"));
        sound.ifPresent(value -> player.playNotifySound(value, net.minecraft.sounds.SoundSource.BLOCKS, 1, 1));
        return true;
    }
}
