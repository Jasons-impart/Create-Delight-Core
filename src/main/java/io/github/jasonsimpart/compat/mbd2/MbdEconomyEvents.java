package io.github.jasonsimpart.compat.mbd2;

import io.github.jasonsimpart.registry.ModItems;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

final class MbdEconomyEvents {
    private static long auctionDayTime = Long.MIN_VALUE;
    private MbdEconomyEvents() {}
    static void register() {
        MbdSellBin.register();
        MbdOrderDelivery.register();
        NeoForge.EVENT_BUS.addListener(MbdEconomyEvents::open);
        NeoForge.EVENT_BUS.addListener(MbdEconomyEvents::auction);
        NeoForge.EVENT_BUS.addListener(MbdEconomyEvents::started);
        NeoForge.EVENT_BUS.addListener(MbdEconomyEvents::sync);
        NeoForge.EVENT_BUS.addListener(MbdEconomyEvents::clonePlayer);
    }
    private static void started(ServerStartedEvent event) {
        auctionDayTime = Long.MIN_VALUE;
        MbdRecipeValues.rebuild(event.getServer());
    }
    private static void sync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) MbdRecipeValues.rebuild(event.getPlayerList().getServer());
        var payload = MbdRecipeValues.snapshot();
        event.getRelevantPlayers().forEach(player -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, payload));
    }
    private static void clonePlayer(PlayerEvent.Clone event) {
        event.getEntity().getPersistentData().putInt("order_reputation", event.getOriginal().getPersistentData().getInt("order_reputation"));
    }
    private static void open(PlayerInteractEvent.RightClickItem event) {
        if (!event.getItemStack().is(ModItems.UNOPENED_ORDER.get())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel().isClientSide) return;
        var info = MbdOrders.create(event.getEntity());
        if (info.isEmpty()) return;
        var order = ModItems.ORDER.toStack();
        var data = new net.minecraft.nbt.CompoundTag();
        data.put("createdelightOrderInfo", info);
        order.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        if (event.getItemStack().getCount() == 1) {
            event.getEntity().setItemInHand(event.getHand(), order);
            return;
        }
        if (!event.getEntity().getInventory().add(order)) {
            var player = event.getEntity();
            var entity = new net.minecraft.world.entity.item.ItemEntity(event.getLevel(), player.getX(), player.getY(), player.getZ(), order);
            entity.setDefaultPickUpDelay();
            entity.setTarget(player.getUUID());
            if (!event.getLevel().addFreshEntity(entity)) return;
        }
        event.getItemStack().shrink(1);
    }
    private static void auction(LevelTickEvent.Post event) {
        var level = event.getLevel();
        if (level.isClientSide || level.dimension() != Level.OVERWORLD || !auctionDue(level.getDayTime())) return;
        if (!(TraderAPI.getApi().GetTrader(false, 0) instanceof AuctionHouseTrader trader)) return;
        int count = level.random.nextInt(4);
        for (int i = 0; i < count; i++) {
            var data = new AuctionTradeData((net.minecraft.world.entity.player.Player) null);
            data.setAuctionItems(new SimpleContainer(ModItems.UNOPENED_ORDER.toStack()));
            var bid = CoinValue.fromItemOrValue(BuiltInRegistries.ITEM.get(ResourceLocation.parse("createdeco:copper_coin")), 1);
            var start = CoinValue.fromItemOrValue(ModItems.GOLD_COIN.get(), 1).multiplyValue(.5 + level.random.nextFloat() * 1.5);
            if (bid.isEmpty() || MbdFoodEconomy.coins(bid).isEmpty() || start.isEmpty() || MbdFoodEconomy.coins(start).isEmpty()) continue;
            data.setMinBidDifferent(bid);
            data.setStartingBid(start);
            data.setDuration(3_600_000);
            trader.addTrade(data, null, false);
        }
    }
    static boolean auctionDue(long dayTime) {
        if (dayTime % 12000 != 0 || auctionDayTime == dayTime) return false;
        auctionDayTime = dayTime;
        return true;
    }
}
