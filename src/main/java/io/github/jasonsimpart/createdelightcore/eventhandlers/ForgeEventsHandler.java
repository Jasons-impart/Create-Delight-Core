package io.github.jasonsimpart.createdelightcore.eventhandlers;

import io.github.jasonsimpart.createdelightcore.compat.cmr.LiquidCoolerFuelJsonLoader;
import net.minecraftforge.fml.ModList;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleManager;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderDataManager;
import io.github.jasonsimpart.createdelightcore.content.order.supply.SupplyCommissionBlockEntity;
import io.github.jasonsimpart.createdelightcore.content.order.supply.SupplyCommissionMenu;
import io.github.jasonsimpart.createdelightcore.content.order.supply.SupplyCommissionPlayerData;
import io.github.jasonsimpart.createdelightcore.network.CDNetwork;
import io.github.jasonsimpart.createdelightcore.network.SyncFuelMapsPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber
public class ForgeEventsHandler {
    @SubscribeEvent
    public static void discoverSupplyCatalogItems(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        var discovered = SupplyCommissionPlayerData.discoverInventorySupplies(player);
        if (discovered.isEmpty()) {
            return;
        }
        if (player.containerMenu instanceof SupplyCommissionMenu menu) {
            menu.refreshCatalogAfterDiscovery(player);
        }
        if (discovered.size() == 1) {
            ItemStack item = new ItemStack(BuiltInRegistries.ITEM.get(discovered.get(0)));
            player.displayClientMessage(Component.translatable(
                    "createdelightcore.supply_commission.item_discovered", item.getHoverName()), true);
        } else {
            player.displayClientMessage(Component.translatable(
                    "createdelightcore.supply_commission.items_discovered", discovered.size()), true);
        }
    }

    @SubscribeEvent
    public static void preventActiveSupplyTableBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof SupplyCommissionBlockEntity table
                && table.hasActiveCommissions()) {
            event.setCanceled(true);
            event.getPlayer().displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "createdelightcore.supply_commission.break_active"), true);
        }
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        if (ModList.get().isLoaded("cmr")) {
            event.addListener(LiquidCoolerFuelJsonLoader.INSTANCE);
        }
        event.addListener(OrderDataManager.INSTANCE);
        event.addListener(ConfigurationModuleManager.MODULE_RELOAD_LISTENER);
        event.addListener(ConfigurationModuleManager.MODE_RELOAD_LISTENER);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            ConfigurationModuleManager.refreshPlayerModules(event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(ConfigurationModuleManager::refreshPlayerModules);
        }
        SyncFuelMapsPacket packet = new SyncFuelMapsPacket();
        if (event.getPlayer() != null) {
            CDNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), packet);
        } else {
            CDNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
    }
}
